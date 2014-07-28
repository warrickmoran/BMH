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
package com.raytheon.uf.edex.bmh.comms;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXB;

import org.apache.qpid.url.URLSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raytheon.uf.common.bmh.notify.MessagePlaybackStatusNotification;
import com.raytheon.uf.common.bmh.notify.PlaylistSwitchNotification;
import com.raytheon.uf.edex.bmh.BMHConstants;
import com.raytheon.uf.edex.bmh.comms.config.CommsConfig;
import com.raytheon.uf.edex.bmh.comms.config.DacChannelConfig;
import com.raytheon.uf.edex.bmh.comms.config.DacConfig;
import com.raytheon.uf.edex.bmh.dactransmit.DacTransmitArgParser;
import com.raytheon.uf.edex.bmh.dactransmit.ipc.DacTransmitStatus;

/**
 * 
 * Base class for comms manager application. Ensures that all dac transmit
 * processes are started and communicating and manages all communication with
 * edex and cave.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Jul 16, 2014  3399     bsteffen    Initial creation
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class CommsManager  {

    private static final Logger logger = LoggerFactory
            .getLogger(CommsManager.class);

    private final DacTransmitServer transmitServer;

    private final JmsCommunicator jms;

    private final CommsConfig config;

    private final Map<String, Process> startedProcesses = new HashMap<>();

    /**
     * Create a comms manager, this will fail if there is probelms with the
     * config file.
     */
    public CommsManager() {
        config = JAXB.unmarshal(new File(BMHConstants.getBmhDataDirectory()
                + File.separator + "conf" + File.separator + "comms.xml"),
                CommsConfig.class);
        transmitServer = new DacTransmitServer(this, config);
        JmsCommunicator jms = null;
        try {
            jms = new JmsCommunicator(config);
        } catch (URLSyntaxException e) {
            logger.error(
                    "Error parsing jms connection url, jms will be disabled.",
                    e);
        }
        this.jms = jms;
        this.jms.connect();
    }

    /**
     * Run the comms manager, this will potentially run forever.
     */
    public void run() {
        transmitServer.start();
        while (transmitServer.isAlive()) {
            try {
                /*
                 * Set of all connected groups so any not in configuration can
                 * be shut down.
                 */
                Set<String> unconfiguredGroups = new HashSet<>(
                        transmitServer.getConnectedGroups());
                for (DacConfig dac : config.getDacs()) {
                    for (DacChannelConfig channel : dac.getChannels()) {
                        String group = channel.getTransmitterGroup();
                        DacTransmitCommunicator comms = transmitServer
                                .getCommunicator(group);
                        if (comms == null) {
                            // TODO this is where cluster checks can go.
                            launchDacTransmit(dac, channel);
                        } else if (!comms.isConnectedToDac()) {
                            startedProcesses.remove(group);
                            logger.error("Cannot connect to DAC for " + group);
                        }
                        unconfiguredGroups.remove(group);
                    }
                }
                for (String group : unconfiguredGroups) {
                    transmitServer.shutdownGroup(group);
                }
            } catch (Throwable e) {
                logger.error("Error checking connection status.", e);
                /*
                 * TODO If this fails multiple times may want to try failing
                 * over or reseting some dac transmit application.
                 */
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                /* Don't care */
            }
        }
    }

    protected void launchDacTransmit(DacConfig dac, DacChannelConfig channel) {
        String group = channel.getTransmitterGroup();
        Process p = startedProcesses.get(group);
        if (p != null) {
            try {
                int status = p.exitValue();
                logger.error("Dac transmit process has unexpectedly exited for "
                        + group + " with a status of " + status);
            } catch (IllegalThreadStateException e) {
                logger.info("Dac transmit process is running but unconnected for "
                        + group);
                return;
            }
        }
        logger.info("Starting dac transmit for: " + group);
        List<String> args = new ArrayList<>();
        args.add(config.getDacTransmitStarter());
        args.add("-" + DacTransmitArgParser.DAC_HOSTNAME_OPTION_KEY);
        args.add(dac.getIpAddress());
        args.add("-" + DacTransmitArgParser.DATA_PORT_OPTION_KEY);
        args.add(Integer.toString(channel.getDataPort()));
        if (channel.getControlPort() != null) {
            args.add("-" + DacTransmitArgParser.CONTROL_PORT_OPTION_KEY);
            args.add(channel.getControlPort().toString());
        }
        args.add("-" + DacTransmitArgParser.TRANSMITTER_OPTION_KEY);
        StringBuilder radios = new StringBuilder(4);
        for (int radio : channel.getRadios()) {
            radios.append(radio);
        }
        args.add(radios.toString());
        args.add("-" + DacTransmitArgParser.TRANSMITTER_GROUP_OPTION_KEY);
        args.add(group);
        args.add("-" + DacTransmitArgParser.INPUT_DIR_OPTION_KEY);
        args.add(BMHConstants.getBmhDataDirectory() + File.separator + "data"
                + File.separator + "playlist" + File.separator + group);
        args.add("-" + DacTransmitArgParser.COMMS_MANAGER_PORT_OPTION_KEY);
        args.add(Integer.toString(config.getIpcPort()));
        ProcessBuilder startCommand = new ProcessBuilder(args);
        // TODO what to do with IO?
        startCommand.inheritIO();
        try {
            p = startCommand.start();
            startedProcesses.put(group, p);
        } catch (IOException e) {
            logger.error("Unable to start dac transmit for " + group, e);
        }
    }

    public static void main(String[] args) {
        new CommsManager().run();
    }

    public void dacStatusChanged(DacTransmitCommunicator communicator,
            DacTransmitStatus status) {
        String group = communicator.getGroupName();
        if (status.isConnectedToDac()) {
            logger.info(group
                    + " is now connected to the dac");
            jms.addQueueObserver("BMH.Playlist." + group,
                    new PlaylistNotificationObserver(communicator));
        } else {
            jms.removeQueueObserver("BMH.Playlist." + group, null,
                    new PlaylistNotificationObserver(communicator));
            logger.info(group
                    + " is now disconnected from the dac");
        }
    }

    public void playlistSwitched(PlaylistSwitchNotification notification){
        jms.sendStatus(notification);
    }

    public void messagePlaybackStatusArrived(
            MessagePlaybackStatusNotification notification) {
        jms.sendStatus(notification);
    }

}
