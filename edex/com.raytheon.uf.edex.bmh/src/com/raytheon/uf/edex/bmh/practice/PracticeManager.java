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
import java.lang.ProcessBuilder.Redirect;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raytheon.uf.common.bmh.datamodel.dac.Dac;
import com.raytheon.uf.common.bmh.notify.config.ConfigNotification.ConfigChangeType;
import com.raytheon.uf.common.bmh.notify.config.DacConfigNotification;
import com.raytheon.uf.common.bmh.notify.config.PracticeModeConfigNotification;
import com.raytheon.uf.common.bmh.trace.ITraceable;
import com.raytheon.uf.common.bmh.trace.TraceableUtil;
import com.raytheon.uf.common.serialization.SerializationException;
import com.raytheon.uf.edex.bmh.BMHConstants;
import com.raytheon.uf.edex.bmh.BmhMessageProducer;
import com.raytheon.uf.edex.bmh.dao.AbstractBMHDao;
import com.raytheon.uf.edex.bmh.dao.DacDao;
import com.raytheon.uf.edex.core.EdexException;
import com.raytheon.uf.edex.database.cluster.ClusterLocker;
import com.raytheon.uf.edex.database.cluster.ClusterTask;

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
 * Oct 21, 2014  3687     bsteffen    Initial creation
 * Nov 19, 2014  3817     bsteffen    Return boolean from checkTimeout to indicate if practice mode is running
 * Jan 14, 2014  3926     bsteffen    When creating a dac, pick the ports differently
 * Feb 03, 2015  4056     bsteffen    Extract DEFAULT_RECEIVE_ADDRESS to Dac.
 * May 28, 2015  4429     rjpeter     Add ITraceable.
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class PracticeManager {

    private static final Logger logger = LoggerFactory
            .getLogger(PracticeManager.class);

    private static final String LOCK_NAME = "practice";

    private static final String LOCK_DETAILS = "start_time";

    private final SimpleDateFormat logDateFormat = new SimpleDateFormat(
            "yyyyMMdd");

    private final ClusterLocker locker = new ClusterLocker(
            AbstractBMHDao.BMH_PRACTICE_DATABASE_NAME);

    private String commsManagerStarter;

    private String dacSimulatorStarter;

    private String dacSimulatorStopper;

    private int timeoutMinutes;

    /**
     * Boolean to make it easier not to start practice mode twice.
     */
    private volatile boolean running = false;

    public void handleModeNotification(
            PracticeModeConfigNotification notification) {
        if (notification.getType() == ConfigChangeType.Update) {
            handlePracticeStartup(notification);
        } else {
            handlePracticeShutdown();
        }
    }

    public void handleDacNotification(DacConfigNotification notification) {
        try {
            InetAddress address = InetAddress.getByName(notification
                    .getAddress());
            if (NetworkInterface.getByInetAddress(address) != null) {
                /* This is a local address. */
                logger.info("Restarting dac simulators.");
                stopDacSimulators();
                startDacSimulators();
            }
        } catch (UnknownHostException | SocketException e) {
            logger.error("Unable to process dac notification.");
        }
    }

    /**
     * Determine if the practice mode processes need to be shut down.
     * 
     * @return true if practice mode is running, false if not.
     */
    public boolean checkPracticeTimeout() {
        long time = System.currentTimeMillis();
        ClusterTask task = locker.lookupLock(LOCK_NAME, LOCK_DETAILS);
        long runTime = time - task.getLastExecution();
        if (task.isRunning() && runTime > timeoutMinutes * 60 * 1000l) {
            logger.info("Practice mode has timed out after {} minutes",
                    runTime / 1000 / 60);
            try {
                BmhMessageProducer
                        .sendConfigMessage(
                                new PracticeModeConfigNotification(
                                        ConfigChangeType.Delete,
                                        TraceableUtil
                                                .createCurrentTraceId("Edex-PracticeMode-TimeOut")),
                                false);
            } catch (EdexException | SerializationException e) {
                logger.error("Unable to stop practice mode.", e);
            }
            return false;
        }
        return task.isRunning();
    }

    private synchronized void handlePracticeStartup(ITraceable traceable) {

        logger.info("Starting practice mode.");
        /*
         * No timeout will allow us to always set running to true and update the
         * time.
         */
        locker.lock(LOCK_NAME, LOCK_DETAILS, 0l, false);
        if (running) {
            return;
        }

        boolean startedDac = startDacSimulators();

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
                dac.setReceiveAddress(Dac.DEFAULT_RECEIVE_ADDRESS);
                int basePort = 30000 + ((Math.abs(address.hashCode()) % 1000) * 10);

                dac.setReceivePort(basePort);
                dac.setDataPorts(new HashSet<>(Arrays.<Integer> asList(
                        basePort + 2, basePort + 4, basePort + 6, basePort + 8)));
                String name = address.getHostName();
                if (name != null) {
                    int index = name.indexOf('.');
                    if (index > 0) {
                        name = name.substring(0, index);
                    }
                    dac.setName(name);
                } else {
                    dac.setName(address.getHostAddress());
                }
                logger.info("Creating a new dac for {}", dac.getName());
                new DacDao(false).persist(dac);
                try {
                    BmhMessageProducer.sendConfigMessage(
                            new DacConfigNotification(ConfigChangeType.Update,
                                    dac, traceable), false);
                } catch (EdexException | SerializationException e) {
                    logger.error("Unable to properly configure new dac.", e);
                }
            }

        }
        launchCommsManager();
        running = true;
    }

    private boolean startDacSimulators() {
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
        return startedDac;
    }

    private synchronized void handlePracticeShutdown() {
        logger.info("Stopping practice mode.");
        running = false;
        stopDacSimulators();
        locker.unlock(LOCK_NAME, LOCK_DETAILS);
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
            String logDate = logDateFormat.format(new Date());
            StringBuilder logFileName = new StringBuilder(64);
            logFileName.append("dacsimulator-");
            logFileName.append(dac.getName());
            logFileName.append("-console-");
            logFileName.append(logDate);
            logFileName.append(".log");
            Path logFilePath = Paths.get(BMHConstants.getBmhHomeDirectory())
                    .resolve("logs").resolve(logFileName.toString());
            pb.redirectOutput(Redirect.appendTo(logFilePath.toFile()));
            pb.redirectError(Redirect.appendTo(logFilePath.toFile()));
            pb.environment().put("DAC_NAME", dac.getName());
            pb.start();
        } catch (IOException e) {
            logger.error("Unable to start dac simulator.", e);
        }
    }

    private void stopDacSimulators() {
        try {
            new ProcessBuilder(dacSimulatorStopper).inheritIO().start();
        } catch (IOException e) {
            logger.error("Unable to start practice comms manager.", e);
        }
    }

    private void launchCommsManager() {
        try {
            ProcessBuilder pb = new ProcessBuilder(commsManagerStarter, "-p");
            String logDate = logDateFormat.format(new Date());
            StringBuilder logFileName = new StringBuilder(64);
            logFileName.append("commsmanager-practice-console-");
            logFileName.append(logDate);
            logFileName.append(".log");
            Path logFilePath = Paths.get(BMHConstants.getBmhHomeDirectory())
                    .resolve("logs").resolve(logFileName.toString());
            pb.redirectOutput(Redirect.appendTo(logFilePath.toFile()));
            pb.redirectError(Redirect.appendTo(logFilePath.toFile()));
            pb.start();
        } catch (IOException e) {
            logger.error("Unable to start practice comms manager.", e);
        }
    }

    public void setCommsManagerStarter(String commsManagerStarter) {
        this.commsManagerStarter = commsManagerStarter;
    }

    public void setDacSimulatorStarter(String dacSimulatorStarter) {
        this.dacSimulatorStarter = dacSimulatorStarter;
    }

    public void setDacSimulatorStopper(String dacSimulatorStopper) {
        this.dacSimulatorStopper = dacSimulatorStopper;
    }

    public void setTimeoutMinutes(int timeoutMinutes) {
        this.timeoutMinutes = timeoutMinutes;
    }

}
