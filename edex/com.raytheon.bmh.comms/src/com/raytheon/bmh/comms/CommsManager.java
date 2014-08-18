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
package com.raytheon.bmh.comms;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.JAXB;

import org.apache.qpid.url.URLSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raytheon.bmh.comms.dactransmit.DacTransmitCommunicator;
import com.raytheon.bmh.comms.dactransmit.DacTransmitServer;
import com.raytheon.bmh.comms.jms.JmsCommunicator;
import com.raytheon.bmh.comms.jms.PlaylistNotificationObserver;
import com.raytheon.bmh.comms.linetap.LineTapServer;
import com.raytheon.uf.common.bmh.notify.DacHardwareStatusNotification;
import com.raytheon.uf.common.bmh.notify.MessagePlaybackStatusNotification;
import com.raytheon.uf.common.bmh.notify.PlaylistSwitchNotification;
import com.raytheon.uf.edex.bmh.comms.CommsConfig;
import com.raytheon.uf.edex.bmh.comms.DacChannelConfig;
import com.raytheon.uf.edex.bmh.comms.DacConfig;
import com.raytheon.uf.edex.bmh.dactransmit.DacTransmitArgParser;
import com.raytheon.uf.edex.bmh.dactransmit.ipc.DacTransmitCriticalError;
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
 * Jul 31, 2014  3286     dgilling    Wire up DacHardwareStatusNotification.
 * Aug 04, 2014  3487     bsteffen    Add lineTapServer
 * Aug 12, 2014  3486     bsteffen    Watch for config changes
 * Aug 14, 2014  3286     dgilling    Support receiving critical errors from 
 *                                    DacTransmit.
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class CommsManager {

    private static final Logger logger = LoggerFactory
            .getLogger(CommsManager.class);

    private Path configPath;

    private CommsConfig config;

    private DacTransmitServer transmitServer;

    private LineTapServer lineTapServer;

    private JmsCommunicator jms;

    private final Map<DacTransmitKey, Process> startedProcesses = new HashMap<>();

    /**
     * Create a comms manager, this will fail if there are problems starting
     * servers.
     */
    public CommsManager() {
        configPath = CommsConfig.getDefaultPath();
        if (Files.exists(configPath)) {
            config = JAXB.unmarshal(configPath.toFile(), CommsConfig.class);
        } else {
            logger.error("No config found, using default values.");
            config = new CommsConfig();
            JAXB.marshal(config, configPath.toFile());
        }
        try {
            transmitServer = new DacTransmitServer(this, config);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Unable to start dac transmit server.", e);
        }
        try {
            lineTapServer = new LineTapServer(config);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to start line tap server.",
                    e);
        }
        try {
            jms = new JmsCommunicator(config);
        } catch (URLSyntaxException e) {
            logger.error(
                    "Error parsing jms connection url, jms will be disabled.",
                    e);
        }
    }

    /**
     * Run the comms manager, this will watch for changes to the config and also
     * periodically launch processes for any channels in the config that do not
     * have a running dac transmit process.
     */
    public void run() {
        WatchService configWatcher = null;
        try {
            configWatcher = configPath.getFileSystem().newWatchService();
            configPath.getParent().register(configWatcher,
                    StandardWatchEventKinds.ENTRY_MODIFY);
        } catch (IOException e) {
            logger.error(
                    "Cannot monitor config file, config changes will not take affect.",
                    e);
        }
        transmitServer.start();
        lineTapServer.start();
        jms.connect();
        while (transmitServer.isAlive() && lineTapServer.isAlive()) {
            WatchKey wkey = null;
            try {
                if (configWatcher == null) {
                    Thread.sleep(1000);
                } else {
                    wkey = configWatcher.poll(1, TimeUnit.SECONDS);
                }
            } catch (InterruptedException e) {
                /* Don't care */
            }
            if (wkey != null) {
                for (WatchEvent<?> e : wkey.pollEvents()) {
                    try {
                        Path modFile = (Path) e.context();
                        modFile = configPath.resolveSibling(modFile);
                        if (Files.exists(modFile) && Files.exists(configPath)
                                && Files.isSameFile(configPath, modFile)) {
                            CommsConfig config = JAXB.unmarshal(
                                    configPath.toFile(),
                                    CommsConfig.class);
                            reconfigure(config);
                        }
                    } catch (Throwable t) {
                        logger.error("Cannot read new config file", t);
                    }

                }
                wkey.reset();
            }
            try {
                if (config.getDacs() != null) {
                    for (DacConfig dac : config.getDacs()) {
                        for (DacChannelConfig channel : dac.getChannels()) {
                            DacTransmitKey key = new DacTransmitKey(dac,
                                    channel);
                            if (transmitServer.isConnected(key)) {
                                startedProcesses.remove(key);
                            } else {
                                launchDacTransmit(key, channel);
                            }
                        }
                    }
                }
            } catch (Throwable e) {
                logger.error("Error checking connection status.", e);
                /*
                 * TODO If this fails multiple times may want to try failing
                 * over or reseting some dac transmit application.
                 */
            }
        }
        if (configWatcher != null) {
            try {
                configWatcher.close();
            } catch (IOException e) {
                logger.error("Unexpected error monitoring config file.", e);
            }
        }
    }

    public void reconfigure(CommsConfig newConfig) {
        if (newConfig.equals(this.config)) {
            return;
        }
        if (newConfig.getDacTransmitPort() != config.getDacTransmitPort()) {
            try {
                transmitServer.changePort(newConfig.getDacTransmitPort());
            } catch (IOException e) {
                logger.error("Unable to switch dac transmit server port, port will remain: "
                        + config.getDacTransmitPort());
                newConfig.setDacTransmitPort(config.getDacTransmitPort());
            }
        }
        if (newConfig.getLineTapPort() != config.getLineTapPort()) {
            try {
                lineTapServer.changePort(newConfig.getLineTapPort());
            } catch (IOException e) {
                logger.error("Unable to switch line tap server port, port will remain: "
                        + config.getDacTransmitPort());
                newConfig.setLineTapPort(config.getLineTapPort());
            }
        }
        if (jms != null
                && !config.getJmsConnection().equals(
                        newConfig.getJmsConnection())) {
            jms.disconnect();
            jms = null;
        }
        this.config = newConfig;
        transmitServer.reconfigure(config);
        lineTapServer.reconfigure(config);
        if (jms == null) {
            try {
                jms = new JmsCommunicator(config);
                jms.connect();
            } catch (URLSyntaxException e) {
                logger.error(
                        "Error parsing jms connection url, jms will be disabled.",
                        e);
            }
        }
        for (Entry<DacTransmitKey, Process> e : startedProcesses.entrySet()) {
            /*
             * Destroy any processes we have started that have not connected
             * since they may have bad config values.
             */
            logger.debug("Stopping unconnected process for "
                    + e.getKey().getInputDirectory());
            e.getValue().destroy();
        }
    }

    protected void launchDacTransmit(DacTransmitKey key,
            DacChannelConfig channel) {
        String group = channel.getTransmitterGroup();
        Process p = startedProcesses.get(key);
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
        args.add(key.getDacAddress());
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
        args.add("-" + DacTransmitArgParser.INPUT_DIR_OPTION_KEY);
        args.add(channel.getInputDirectory().toString());
        args.add("-" + DacTransmitArgParser.COMMS_MANAGER_PORT_OPTION_KEY);
        args.add(Integer.toString(config.getDacTransmitPort()));
        ProcessBuilder startCommand = new ProcessBuilder(args);
        startCommand.environment().put("TRANSMITTER_GROUP", group);
        startCommand.inheritIO();
        try {
            p = startCommand.start();
            startedProcesses.put(key, p);
        } catch (IOException e) {
            logger.error("Unable to start dac transmit for " + group, e);
        }
    }

    public void dacStatusChanged(DacTransmitCommunicator communicator,
            DacTransmitStatus status) {
        String group = communicator.getGroupName();
        if (status.isConnectedToDac()) {
            logger.info(group + " is now connected to the dac");
            jms.addQueueObserver("BMH.Playlist." + group,
                    new PlaylistNotificationObserver(communicator));
        } else {
            jms.removeQueueObserver("BMH.Playlist." + group, null,
                    new PlaylistNotificationObserver(communicator));
            logger.info(group + " is now disconnected from the dac");
        }
    }

    public void playlistSwitched(PlaylistSwitchNotification notification) {
        jms.sendStatus(notification);
    }

    public void messagePlaybackStatusArrived(
            MessagePlaybackStatusNotification notification) {
        jms.sendStatus(notification);
    }

    public void hardwareStatusArrived(DacHardwareStatusNotification notification) {
        jms.sendStatus(notification);
    }

    public void errorReceived(DacTransmitCriticalError e, String group) {
        // TODO send to alertviz via EDEX
        logger.error(
                "Critical error received from group: " + group + ": "
                        + e.getErrorMessage(), e.getThrowable());
    }

    public static void main(String[] args) {
        new CommsManager().run();
    }

}
