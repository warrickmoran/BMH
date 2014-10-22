/**
 * This software was developed and / or modified by Raytheon Company,
 * pursuant to Contract DG133W-05-CQ-1067 with the US Government.
 * 
 * U.S. EXPORT CONTROLLED TECHNICAL DATA
 * This software product contains export-restricted data whose
 * export/transfer/disclosure is restricted by U.S. law. Dissemination
 * to non-U.S. persons whether in the United States or abroad requires
 * an export license or other authorization.
 * 
 * Contractor Name:        Raytheon Company
 * Contractor Address:     6825 Pine Street, Suite 340
 *                         Mail Stop B8
 *                         Omaha, NE 68106
 *                         402.291.0100
 * 
 * See the AWIPS II Master Rights File ("Master Rights File.pdf") for
 * further licensing information.
 **/
package com.raytheon.uf.edex.bmh.practice;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raytheon.uf.common.bmh.datamodel.dac.Dac;
import com.raytheon.uf.common.bmh.notify.config.ConfigNotification.ConfigChangeType;
import com.raytheon.uf.common.bmh.notify.config.PracticeModeConfigNotification;
import com.raytheon.uf.edex.bmh.dao.DacDao;

/**
 * 
 * Manages external processes that are run during practice mode including dac
 * simulators and comms manager.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Oct 21, 2014  2687     bsteffen     Initial creation
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class PracticeManager {

    private static final Logger logger = LoggerFactory
            .getLogger(PracticeManager.class);

    private String commsManagerStarter = "/awips2/bmh/bin/commsmanager.sh";

    private String dacSimulatorStarter = "/awips2/bmh/bin/dacsimulator.sh";

    private String dacSimulatorStopper = "/awips2/bmh/bin/stop_dacsimulator.sh";

    public void handleNotification(PracticeModeConfigNotification notification) {
        if (notification.getType() == ConfigChangeType.Update) {
            handlePracticeStartup();
        } else {
            handlePracticeShutdown();
        }
    }

    private void handlePracticeStartup() {
        boolean startedDac = false;
        DacDao dacDao = new DacDao(false);
        for (Dac dac : dacDao.getAll()) {
            InetAddress address;
            try {
                address = InetAddress.getByName(dac.getAddress());
                if (NetworkInterface.getByInetAddress(address) != null) {
                    launchDacSimulator(dac);
                    startedDac = true;
                }
            } catch (UnknownHostException e) {
                logger.error("Ignoring unknown Host({}) for practice mode.",
                        dac.getAddress(), e);
            } catch (SocketException e) {
                logger.error("Ignoring host({}) for practice mode.",
                        dac.getAddress(), e);
            }
        }
        if (!startedDac) {
            NetworkInterface nic = null;
            InetAddress address = null;
            try {
                Enumeration<NetworkInterface> nics = NetworkInterface
                        .getNetworkInterfaces();
                if (nics != null) {
                    while (nics.hasMoreElements() && address == null) {
                        nic = nics.nextElement();
                        Enumeration<InetAddress> addresses = nic
                                .getInetAddresses();
                        while (addresses.hasMoreElements() && address == null) {
                            address = addresses.nextElement();
                            if (address.isLoopbackAddress()) {
                                address = null;
                            } else if (address.isLinkLocalAddress()) {
                                address = null;
                            }
                        }
                    }
                }
            } catch (SocketException e) {
                logger.error("Unable to determine address for dac.", e);
            }
            if (address != null) {
                Dac dac = new Dac();
                dac.setAddress(address.getHostAddress());
                dac.setReceiveAddress("239.255.86.75");
                dac.setReceivePort(31000);
                dac.setDataPorts(new HashSet<>(Arrays.<Integer> asList(31002,
                        31004, 31006, 31008)));
                dac.setName(address.getHostName());
                dacDao.persist(dac);
                // TODO if we send notification of dac changes then this needs
                // to do that.
                launchDacSimulator(dac);
            }

        }
        launchCommsManager();
    }

    private void handlePracticeShutdown() {
        stopDacSimulators();
        /* Comms manager is listening to jms and will stop itself. */
    }

    private void launchDacSimulator(Dac dac) {
        List<String> command = new ArrayList<>();
        command.add(dacSimulatorStarter);
        command.add("-p");
        /*
         * get lowest port and validate other ports are sequential. Its
         * acceptable if not all ports are in the config but extra ports are not
         * supported.
         */
        NavigableSet<Integer> ports = new TreeSet<>(dac.getDataPorts());
        Integer first = ports.pollFirst();
        ports.remove(first + 2);
        ports.remove(first + 4);
        ports.remove(first + 6);
        if (!ports.isEmpty()) {
            logger.error(
                    "Unable to start dac simulator because of nonsequential port selection: {}",
                    dac.getDataPorts());
        }
        command.add(first.toString());
        command.add("-d");
        command.add(dac.getReceiveAddress());
        command.add("-r");
        command.add(Integer.toString(dac.getReceivePort()));
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.inheritIO();
            pb.start();
        } catch (IOException e) {
            logger.error("Unable to start dac simulator.", e);
        }
    }

    private void stopDacSimulators() {
        try {
            new ProcessBuilder(dacSimulatorStopper).start();
        } catch (IOException e) {
            logger.error("Unable to start practice comms manager.", e);
        }
    }

    private void launchCommsManager() {
        try {
            new ProcessBuilder(commsManagerStarter, "-p").start();
        } catch (IOException e) {
            logger.error("Unable to start practice comms manager.", e);
        }
    }

}
