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
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.JAXB;
import org.apache.qpid.url.URLSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raytheon.bmh.comms.broadcast.BroadcastStreamServer;
import com.raytheon.bmh.comms.cluster.ClusterServer;
import com.raytheon.bmh.comms.dactransmit.DacTransmitServer;
import com.raytheon.bmh.comms.jms.JmsCommunicator;
import com.raytheon.bmh.comms.linetap.LineTapServer;
import com.raytheon.uf.edex.bmh.BMHConstants;
import com.raytheon.uf.edex.bmh.comms.CommsConfig;
import com.raytheon.uf.edex.bmh.comms.DacChannelConfig;
import com.raytheon.uf.edex.bmh.comms.DacConfig;
import com.raytheon.uf.edex.bmh.dactransmit.DacTransmitArgParser;
import com.raytheon.uf.edex.bmh.dactransmit.ipc.DacTransmitCriticalError;
import com.raytheon.uf.edex.bmh.dactransmit.ipc.IDacLiveBroadcastMsg;

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
 * Aug 18, 2014  3532     bkowal      Pass the supported decibel range as an
 *                                    argument to the Dac Transmitter.
 * Sep 04, 2014  3532     bkowal      Use a decibel target instead of a range.
 * Sep 23, 2014  3485     bsteffen    Additional event handling to support clustering.
 * Sep 29, 2014  3291     bkowal      Use bmh home to look for configuration.
 * Oct 01, 2014  3665     bsteffen    Add force start flag to dac transmit starter.
 * Oct 2, 2014   3642     bkowal      Pass the timezone associated with the transmitter
 *                                    group as an argument to the Dac Transmitter.
 * Oct 10, 2014  3656     bkowal      Initial implementation of live audio streaming from
 *                                    Viz to the Comms Manager.
 * Oct 15, 2014  3655     bkowal      Support live broadcasting to the DAC.
 * Oct 16, 2014  3687     bsteffen    Implement practice mode.
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class CommsManager {

    private static final Logger logger = LoggerFactory
            .getLogger(CommsManager.class);

    /**
     * The amount of time(in ms) to wait after startup before launching dac
     * transmit processes for any dacs that are not connected. A shorter
     * interval will make it more likely that not all dac transmits have tried
     * to connect which can result in duplicate processes. A longer interval
     * creates the possibility of radio silence when a dac transmit needs to be
     * started.
     */
    private static final int FIRST_SLEEP_TIME = Integer.getInteger(
            "CommsFirstSleepInterval", 1000);

    /**
     * The amount of time(in ms) to wait between attempts to start new dac
     * transmit processes when needed.
     */
    private static final int DAC_START_SLEEP_TIME = Integer.getInteger(
            "CommsDacStartSleepInterval", 5000);

    /**
     * The amount of time(in ms) to wait before checking the status of the dacs.
     * Any important events(like disconnects) will interrupt the waiting so this
     * can be a long time to avoid wasting cpu.
     */
    private static final int NORMAL_SLEEP_TIME = Integer.getInteger(
            "CommsSleepInterval", 30000);

    /** NOT THREAD SAFE: only use from the run method */
    private final SimpleDateFormat logDateFormat = new SimpleDateFormat(
            "yyyyMMdd");

    private final boolean operational;

    private final Path configPath;

    private final DacTransmitServer transmitServer;

    private final LineTapServer lineTapServer;

    private final ClusterServer clusterServer;

    private final BroadcastStreamServer broadcastStreamServer;

    /**
     * This is the thread currently executing in a loop in the {@link #run()}
     * method. This thread sleeps and periodically wakes up to check connections
     * and start services as needed. If a change occurs that may require
     * connections or services that are made during the run method then this
     * thread can be interrupted to immediately perform any necessary actions.
     */
    private volatile Thread mainThread;

    private JmsCommunicator jms;

    /**
     * Holds the current config from {@link #configPath}, The
     * {@link #mainThread} is watching this path and will update this field with
     * changes. To maintain consistent state, after construction this should
     * only be accessed from the thread executing the {@link #run()} method.
     */
    private CommsConfig config;

    private final Map<DacTransmitKey, Process> startedProcesses = new HashMap<>();

    /**
     * Create a comms manager, this will fail if there are problems starting
     * servers.
     */
    public CommsManager(boolean operational) {
        this.operational = operational;
        configPath = CommsConfig.getDefaultPath(operational);
        if (Files.exists(configPath)) {
            config = JAXB.unmarshal(configPath.toFile(), CommsConfig.class);
            logger.info("Successfully loaded config file at {}", configPath);
        } else if (operational) {
            logger.error("No config found at {}, using default values.",
                    configPath);
            config = new CommsConfig();
            JAXB.marshal(config, configPath.toFile());
        } else {
            throw new IllegalStateException("No config file found at "
                    + configPath);
        }
        try {
            transmitServer = new DacTransmitServer(this, config);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Unable to start dac transmit server.", e);
        }
        try {
            clusterServer = new ClusterServer(this, config);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to start cluster server.",
                    e);
        }
        try {
            lineTapServer = new LineTapServer(config);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to start line tap server.",
                    e);
        }
        try {
            this.broadcastStreamServer = new BroadcastStreamServer(
                    clusterServer, transmitServer, config);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Unable to start the broadcast stream server.", e);
        }
        try {
            if (config.getJmsConnection() != null) {
                jms = new JmsCommunicator(config, operational);
            } else {
                logger.warn("No jms connection specified, jms will be disabled.");
            }
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
        mainThread = Thread.currentThread();
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
        clusterServer.start();
        lineTapServer.start();
        broadcastStreamServer.start();
        if (jms != null) {
            jms.connect();
        }
        clusterServer.attempClusterConnections(config);
        try {
            /*
             * This sleep enables any existing processes to connect before
             * launching new dac transmit processes.
             */
            Thread.sleep(FIRST_SLEEP_TIME);
        } catch (InterruptedException e1) {
            /* Just start processing right away. */
        }
        while (transmitServer.isAlive() && lineTapServer.isAlive()
                && clusterServer.isAlive() && broadcastStreamServer.isAlive()) {
            int sleeptime = NORMAL_SLEEP_TIME;
            clusterServer.attempClusterConnections(config);
            try {
                if (config.getDacs() != null) {
                    for (DacConfig dac : config.getDacs()) {
                        for (DacChannelConfig channel : dac.getChannels()) {
                            DacTransmitKey key = new DacTransmitKey(dac,
                                    channel);
                            if (!transmitServer.isConnectedToDacTransmit(key)
                                    && !clusterServer.isConnected(key)) {
                                launchDacTransmit(key, channel);
                                sleeptime = DAC_START_SLEEP_TIME;
                            }
                        }
                    }
                }
            } catch (Throwable e) {
                logger.error("Error checking connection status.", e);
            }
            WatchKey wkey = null;
            try {
                if (configWatcher == null) {
                    Thread.sleep(sleeptime);
                } else {
                    wkey = configWatcher.poll(sleeptime, TimeUnit.MILLISECONDS);
                }
            } catch (InterruptedException e) {
                /* Check dacs immediately. */
            }
            if (wkey != null) {
                for (WatchEvent<?> e : wkey.pollEvents()) {
                    try {
                        Path modFile = (Path) e.context();
                        modFile = configPath.resolveSibling(modFile);
                        if (Files.exists(modFile) && Files.exists(configPath)
                                && Files.isSameFile(configPath, modFile)) {
                            logger.info("Reloading configuration file.");
                            CommsConfig config = JAXB.unmarshal(
                                    configPath.toFile(), CommsConfig.class);
                            reconfigure(config);
                        }
                    } catch (Throwable t) {
                        logger.error("Cannot read new config file", t);
                    }

                }
                wkey.reset();
            }
        }
        if (configWatcher != null) {
            try {
                configWatcher.close();
            } catch (IOException e) {
                logger.error("Unexpected error monitoring config file.", e);
            }
        }
        mainThread = null;
    }

    protected void reconfigure(CommsConfig newConfig) {
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
        if (newConfig.getClusterPort() != config.getClusterPort()) {
            try {
                clusterServer.changePort(newConfig.getClusterPort());
            } catch (IOException e) {
                logger.error("Unable to switch cluster server port, port will remain: "
                        + config.getClusterPort());
                newConfig.setClusterPort(config.getClusterPort());
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
        if (jms == null && config.getJmsConnection() != null) {
            try {
                jms = new JmsCommunicator(config, operational);
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
        boolean force = false;
        String group = channel.getTransmitterGroup();
        Process p = startedProcesses.get(key);
        if (p != null) {
            try {
                int status = p.exitValue();
                logger.error("Dac transmit process has unexpectedly exited for "
                        + group + " with a status of " + status);
                force = true;
            } catch (IllegalThreadStateException e) {
                logger.info("Dac transmit process is running but unconnected for "
                        + group);
                return;
            }
        }
        logger.info("Starting dac transmit for: " + group);
        List<String> args = new ArrayList<>();
        args.add(config.getDacTransmitStarter());
        if (force) {
            args.add("-k");
        }
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
        args.add("-" + DacTransmitArgParser.TRANSMISSION_DB_TARGET_KEY);
        args.add(Double.toString(channel.getDbTarget()));
        args.add("-" + DacTransmitArgParser.TIMEZONE_KEY);
        args.add(channel.getTimezone());

        ProcessBuilder startCommand = new ProcessBuilder(args);
        startCommand.environment().put("TRANSMITTER_GROUP", group);

        /*
         * Send console output to a file. This is quite rudimentary, the dac
         * transmit process itself has a more refined log configuration that
         * will hopefully be used for all output but if any messages do make it
         * to stdout/stderr we do not want them interleaved with this process
         * logs and we do not want them to be silently discarded.
         */
        String logDate = logDateFormat.format(new Date());
        String logFileName = "dactransmit-" + group + "-console-" + logDate
                + ".log";
        Path logFilePath = Paths.get(BMHConstants.getBmhHomeDirectory())
                .resolve("logs").resolve(logFileName);
        startCommand.redirectOutput(Redirect.appendTo(logFilePath.toFile()));
        startCommand.redirectError(Redirect.appendTo(logFilePath.toFile()));
        try {
            p = startCommand.start();
            startedProcesses.put(key, p);
        } catch (IOException e) {
            logger.error("Unable to start dac transmit for " + group, e);
        }
    }

    /**
     * Method to call when an event has occurred which may require a new dac
     * transmit process to be created. The current implementation just wakes up
     * the main thread rather than checking the config immediately to avoid any
     * synchronization problems.
     * 
     */
    protected void attemptLaunchDacTransmits() {
        Thread mainThread = this.mainThread;
        if (mainThread != null) {
            mainThread.interrupt();
        }
    }

    /**
     * This method should be called when a dac transmit process has connected to
     * the comms manager. This does not indicate that the dac transmit is
     * connected to a dac. {@link #dacConnectedLocal(DacTransmitKey, String)}
     * should be called when the dac transmit is successfully communicating with
     * a dac. Any other components that may need to refresh and/or notify will
     * be informed.
     */
    public void dacTransmitConnected(DacTransmitKey key, String group) {
        logger.info("dac transmit connected for {}", group);
        startedProcesses.remove(key);
    }

    /**
     * This method should be called when this process is connected to a dac
     * transmit process that is successfully communicating with a dac. Any other
     * components that may need to refresh and/or notify will be informed.
     */
    public void dacConnectedLocal(DacTransmitKey key, String group) {
        logger.info("{} is now connected to the dac", group);
        if (jms != null) {
            jms.listenForPlaylistChanges(key, group, transmitServer);
        }
        transmitServer.dacConnected(key);
        clusterServer.dacConnectedLocal(key);
        broadcastStreamServer.dacConnected(key, group);
    }

    /**
     * This method should be called when this process is connected to another
     * comms manager that is successfully communicating with a dac. Any other
     * components that may need to refresh and/or notify will be informed.
     */
    public void dacConnectedRemote(DacTransmitKey key) {
        transmitServer.dacConnected(key);
    }

    /**
     * This method should be called when a dac transmit process has disconnected
     * from the comms manager. If the dac transmit was connected to a dac than
     * {@link #dacConnectedLocal(DacTransmitKey, String) should also be called.
     * Any other components that may need to refresh and/or notify will be
     * informed.
     */
    public void dacTransmitDisconnected(DacTransmitKey key, String group) {
        logger.info("dac transmit disconnected for {}", group);
        transmitServer.dacTransmitDisconnected(key);
        broadcastStreamServer.dacDisconnected(key, group);
        attemptLaunchDacTransmits();
    }

    /**
     * This method should be called when there is no longer a dac transmit
     * process connected to a dac connected to this comms manager either because
     * the dac has disconnected or the dac transmit process has died or been
     * disconnected. Any other components that may need to refresh and/or notify
     * will be informed.
     */
    public void dacDisconnectedLocal(DacTransmitKey key, String group) {
        logger.info("{} is now disconnected from the dac", group);
        if (jms != null) {
            jms.unlistenForPlaylistChanges(key, group, transmitServer);
        }
        clusterServer.dacDisconnectedLocal(key);
        attemptLaunchDacTransmits();
    }

    /**
     * This method should be called when another comms manager loses its
     * connection to a dac or if this comms manager loses its connection with
     * another comms manager. Any other components that may need to refresh
     * and/or notify will be informed.
     */
    public void dacDisconnectedRemote() {
        attemptLaunchDacTransmits();
    }

    /**
     * This method should be called when a dac transmit process has sent a
     * message that needs to be forwarded on to the rest of the world(edex and
     * cave mostly).
     */
    public void transmitDacStatus(Object statusObject) {
        if (jms != null) {
            jms.sendDacStatus(statusObject);
        }
    }

    /**
     * This method should be called when an error has been received from a dac
     * transmit process that needs to be communicated to users.
     * 
     * @param e
     * @param group
     */
    public void errorReceived(DacTransmitCriticalError e, String group) {
        // TODO send to alertviz via EDEX
        logger.error(
                "Critical error received from group: " + group + ": "
                        + e.getErrorMessage(), e.getThrowable());
    }

    public void forwardDacBroadcastMsg(IDacLiveBroadcastMsg msg) {
        broadcastStreamServer.handleDacBroadcastMsg(msg);
    }

    /**
     * Shutdown this comms mamanger and all connected dac transmit processes.
     */
    public void shutdown() {
        logger.info("Comms manager is shutting down.");
        transmitServer.shutdown();
        broadcastStreamServer.shutdown();
        clusterServer.shutdown();
        lineTapServer.shutdown();
        try {
            transmitServer.join();
            broadcastStreamServer.join();
            clusterServer.join();
            transmitServer.join();
        } catch (InterruptedException e) {
            logger.error("Unexpected interruption while shutting down.", e);
        }
        Thread mainThread = this.mainThread;
        if (mainThread != null) {
            mainThread.interrupt();
        }
    }

    public static void main(String[] args) {
        boolean operational = true;
        for (String arg : args) {
            if (arg.equals("-p")) {
                operational = false;
            } else {
                logger.error(
                        "Unsupported argument: {}.\n  The only supported argument is -p for practice mode.",
                        arg);
                System.exit(1);
            }
        }

        new CommsManager(operational).run();
    }
}
