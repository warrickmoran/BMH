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
import java.net.InetAddress;
import java.net.UnknownHostException;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.JAXB;

import org.apache.qpid.url.URLSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MarkerFactory;

import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.bmh.comms.broadcast.BroadcastStreamServer;
import com.raytheon.bmh.comms.cluster.ClusterServer;
import com.raytheon.bmh.comms.dactransmit.DacTransmitServer;
import com.raytheon.bmh.comms.jms.JmsCommunicator;
import com.raytheon.bmh.comms.linetap.LineTapServer;
import com.raytheon.uf.common.bmh.broadcast.ILiveBroadcastMessage;
import com.raytheon.uf.common.bmh.notify.DacTransmitShutdownNotification;
import com.raytheon.uf.common.bmh.notify.config.ChangeTimeZoneConfigNotification;
import com.raytheon.uf.common.bmh.notify.config.CommsConfigNotification;
import com.raytheon.uf.common.bmh.notify.config.ConfigNotification.ConfigChangeType;
import com.raytheon.uf.common.bmh.notify.config.PracticeModeConfigNotification;
import com.raytheon.uf.common.bmh.notify.status.CommsManagerStatus;
import com.raytheon.uf.common.bmh.notify.status.DacHardwareStatusNotification;
import com.raytheon.uf.common.jms.notification.INotificationObserver;
import com.raytheon.uf.common.jms.notification.NotificationException;
import com.raytheon.uf.common.jms.notification.NotificationMessage;
import com.raytheon.uf.common.stats.StatisticsEvent;
import com.raytheon.uf.edex.bmh.BMHConstants;
import com.raytheon.uf.edex.bmh.comms.CommsConfig;
import com.raytheon.uf.edex.bmh.comms.DacChannelConfig;
import com.raytheon.uf.edex.bmh.comms.DacConfig;
import com.raytheon.uf.edex.bmh.dactransmit.DacTransmitArgParser;
import com.raytheon.uf.edex.bmh.dactransmit.ipc.DacTransmitCriticalError;

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
 * Oct 21, 2014  3655     bkowal      Use the new message types.
 * Nov 03, 2014  3525     bsteffen    Mark dac transmit errors in logs.
 * Nov 11, 2014  3762     bsteffen    Add load balancing of dac transmits.
 * Nov 15, 2014  3630     bkowal      Allow for retrieval of most recent config. Use
 *                                    dynamic log naming.
 * Nov 19, 2014  3817     bsteffen    Updates to send system status messages.
 * Nov 26, 2014  3821     bsteffen    Add SilenceAlarm
 * Dec 01, 2014  3797     bkowal      Support broadcast clustering.
 * Jan 22, 2015  3912     bsteffen    Add isConnectedRemote.
 * Jan 23, 2015  3912     bsteffen    Sleep more when starting many dac transmits.
 * Mar 11, 2015  4186     bsteffen    Report silence alarms in status
 * Apr 07, 2015  4370     rjpeter     Add jms and cluster listener for config changes.
 * Apr 15, 2015  4397     bkowal      Added {@link #transmitBMHStat(StatisticsEvent)}.
 * Apr 20, 2015  4394     bkowal      Handle {@link DacTransmitShutdownNotification}.
 * Apr 21, 2015  4407     bkowal      Restart a dac transmit process if it does not
 *                                    reconnect to a dac after a configurable amount of time.
 * Apr 22, 2015  4404     bkowal      Trigger a silence alarm when connection to a dac is lost.
 *                                    Clear any existing silence alarms on dac transmit shutdown.
 * Apr 23, 2015  4423     rferrel     Added {@link #changeTimeZone(String, String)}.
 * Apr 29, 2015  4394     bkowal      Handle shutdown notifications from non-clustered
 *                                    entities.
 * Jul 08, 2015  4636     bkowal      Updated to specify additional decibel level arguments.
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

    /**
     * The amount of time (in ms) that a dac transmit process cannot have a
     * connection to the dac before it is killed (and potentially restarted).
     */
    private static final long MAX_DAC_DISCONNECT_TIME = Long.getLong(
            "MaxDacDisconnectTime", TimeUtil.MILLIS_PER_MINUTE);

    /** NOT THREAD SAFE: only use from the run method */
    private final SimpleDateFormat logDateFormat = new SimpleDateFormat(
            "yyyyMMdd");

    private final boolean operational;

    private final Path configPath;

    private final DacTransmitServer transmitServer;

    private final LineTapServer lineTapServer;

    private final ClusterServer clusterServer;

    private final BroadcastStreamServer broadcastStreamServer;

    private final SilenceAlarm silenceAlarm;

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
    private volatile CommsConfig config;

    private final Map<DacTransmitKey, Process> startedProcesses = new HashMap<>();

    private final ConcurrentMap<DacTransmitKey, Long> disconnectedDacProcesses = new ConcurrentHashMap<>();

    /**
     * Create a comms manager, this will fail if there are problems starting
     * servers.
     */
    public CommsManager(boolean operational) {
        this.operational = operational;
        configPath = CommsConfig.getDefaultPath(operational);
        if (Files.exists(configPath)) {
            config = JAXB.unmarshal(configPath.toFile(), CommsConfig.class);
            validateConfig(config);
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
                    clusterServer, transmitServer, config, this);
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

        silenceAlarm = new SilenceAlarm(this);
    }

    /**
     * Log various config errors and remove dacs and channels that are not
     * useable. When the config file is being properly generated from edex there
     * should never be problems.
     * 
     * @param config
     */
    protected void validateConfig(CommsConfig config) {
        if (config.getDacTransmitStarter() == null) {
            logger.error("No dac transmit starter in the config file, dac transmits will not be started.");
        }
        Set<DacConfig> dacs = config.getDacs();
        if (dacs != null) {
            Iterator<DacConfig> dacIt = dacs.iterator();
            while (dacIt.hasNext()) {
                DacConfig dac = dacIt.next();
                if (dac.getIpAddress() == null) {
                    logger.error("A dac has been configured with no address. This dac will not be used.");
                    dacIt.remove();
                    continue;
                }
                List<DacChannelConfig> channels = dac.getChannels();
                if (channels != null) {
                    Iterator<DacChannelConfig> channelIt = channels.iterator();
                    while (channelIt.hasNext()) {
                        DacChannelConfig channel = channelIt.next();
                        if (channel.getTransmitterGroup() == null) {
                            logger.error(
                                    "A channel for dac at {} has been configured with no group. This channel will not be used.",
                                    dac.getIpAddress());
                            channelIt.remove();
                            continue;
                        }
                        if (channel.getPlaylistDirectory() == null) {
                            logger.error(
                                    "Group {} on dac at {} has no playlistDirectory and will not be used.",
                                    channel.getTransmitterGroup(),
                                    dac.getIpAddress());
                            channelIt.remove();
                            continue;
                        }
                        if (channel.getRadios() == null) {
                            logger.error(
                                    "Group {} on dac at {} has no radios and will not be used.",
                                    channel.getTransmitterGroup(),
                                    dac.getIpAddress());
                            channelIt.remove();
                            continue;
                        }
                    }
                }
            }
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
            addJmsConfigListener();
            addPracticeJmsShutdownListener();
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
            sendStatus();
            int sleeptime = 0;
            clusterServer.attempClusterConnections(config);
            try {
                if (config.getDacs() != null) {
                    boolean allDacsRunning = true;
                    for (DacConfig dac : config.getDacs()) {
                        for (DacChannelConfig channel : dac.getChannels()) {
                            DacTransmitKey key = new DacTransmitKey(dac,
                                    channel);
                            /*
                             * Verify that there is a dac transmit process that
                             * has connected to a Comms Manager. - ELSE - Verify
                             * that if a dac transmit process is connected to
                             * THIS Comms Manager that it has sync with the dac.
                             * If not, the countdown starts.
                             */
                            if (!transmitServer.isConnectedToDacTransmit(key)
                                    && !clusterServer.isConnected(key)
                                    && !clusterServer.isRequested(key)) {
                                launchDacTransmit(key, channel, false);
                                sleeptime += DAC_START_SLEEP_TIME;
                                allDacsRunning = false;
                            } else if (this.transmitServer
                                    .isConnectedToDacTransmit(key)
                                    && this.transmitServer
                                            .isConnectedToDac(key) == false) {
                                Long start = this.disconnectedDacProcesses
                                        .get(key);
                                long duration = 0;
                                if (start != null) {
                                    duration = System.currentTimeMillis()
                                            - start;
                                } else {
                                    this.disconnectedDacProcesses.put(key,
                                            System.currentTimeMillis());
                                }
                                logger.warn(
                                        "Dac Transmit for {} does not currently have sync with the dac (duration: {} ms).",
                                        channel.getTransmitterGroup(), duration);
                                /*
                                 * Determine if it is time to restart the
                                 * process based on how long the process has not
                                 * been connected to the dac.
                                 */
                                if (duration >= MAX_DAC_DISCONNECT_TIME) {
                                    this.disconnectedDacProcesses.remove(key);
                                    launchDacTransmit(key, channel, true);
                                    sleeptime += DAC_START_SLEEP_TIME;
                                    allDacsRunning = false;
                                }
                            }
                        }
                    }
                    clusterServer.balanceDacTransmits(allDacsRunning);
                }
            } catch (Throwable e) {
                logger.error("Error checking connection status.", e);
            }
            if (sleeptime == 0) {
                sleeptime = NORMAL_SLEEP_TIME;
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
                            reloadConfig(true);
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

    /**
     * Reloads the configuration from disk and optionally notifies other cluster
     * members.
     * 
     * @param notify
     */
    public void reloadConfig(boolean notify) {
        try {
            if (Files.size(configPath) > 0) {
                CommsConfig newConfig = JAXB.unmarshal(configPath.toFile(),
                        CommsConfig.class);
                reconfigure(newConfig);

                if (notify) {
                    clusterServer.sendConfigCheck();
                }
            }
        } catch (Throwable t) {
            logger.error("Cannot read new config file", t);
        }
    }

    /**
     * Set the time zone for server of the transmitter group.
     * 
     * @param timeZone
     * @param transmitterGroup
     */
    public synchronized void changeTimeZone(String timeZone,
            String transmitterGroup) {
        transmitServer.changeTimeZone(timeZone, transmitterGroup);
    }

    protected synchronized void reconfigure(CommsConfig newConfig) {
        logger.info("Checking configuration file.");

        validateConfig(newConfig);
        if (newConfig.equals(this.config)) {
            logger.info("Configuration file has not changed.");
            return;
        }

        logger.info("Reloading configuration file.");

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
        if ((jms != null)
                && !config.getJmsConnection().equals(
                        newConfig.getJmsConnection())) {
            jms.close();
            jms = null;
        }

        this.config = newConfig;
        transmitServer.reconfigure(config);
        lineTapServer.reconfigure(config);
        if ((jms == null) && (config.getJmsConnection() != null)) {
            try {
                jms = new JmsCommunicator(config, operational);
                jms.connect();
                addJmsConfigListener();
                addPracticeJmsShutdownListener();
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
        silenceAlarm.reconfigure(config);
    }

    protected void launchDacTransmit(DacTransmitKey key,
            DacChannelConfig channel, boolean force) {
        String group = channel.getTransmitterGroup();
        /*
         * If force has already been set. There is no need to determine if any
         * existing processed need to be terminated.
         */
        Process p = null;
        if (force == false) {
            p = startedProcesses.get(key);
            if (p != null) {
                try {
                    int status = p.exitValue();
                    if (status == 1) {
                        /*
                         * If we reach this point, a dac transmit process is
                         * already running that we have no way to interact with.
                         * So, the only option is to kill it and restart it.
                         */
                        logger.error(
                                "Dac transmit process existed because there is already an unconnected dac transmit process running for {}.",
                                group);
                        force = true;
                    } else {
                        logger.error("Dac transmit process has unexpectedly exited for "
                                + group + " with a status of " + status);
                    }
                } catch (IllegalThreadStateException e) {
                    logger.info("Dac transmit process is running but unconnected for "
                            + group);
                    force = true;
                }
            }
        }
        logger.info("Starting dac transmit for: " + group);
        if (config.getDacTransmitStarter() == null) {
            /* validateConfig should have already handled this. */
            return;
        }
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
        args.add(Double.toString(channel.getAudioDbTarget()));
        args.add("-" + DacTransmitArgParser.SAME_DB_TARGET_KEY);
        args.add(Double.toString(channel.getSameDbTarget()));
        args.add("-" + DacTransmitArgParser.ALERT_DB_TARGET_KEY);
        args.add(Double.toString(channel.getAlertDbTarget()));
        if (channel.getTimezone() != null) {
            args.add("-" + DacTransmitArgParser.TIMEZONE_KEY);
            args.add(channel.getTimezone());
        }

        ProcessBuilder startCommand = new ProcessBuilder(args);
        startCommand.environment().put("TRANSMITTER_GROUP", group);
        if (!operational) {
            startCommand.environment().put("BMH_LOG_BASE",
                    "dactransmit-practice");
        }

        /*
         * Send console output to a file. This is quite rudimentary, the dac
         * transmit process itself has a more refined log configuration that
         * will hopefully be used for all output but if any messages do make it
         * to stdout/stderr we do not want them interleaved with this process
         * logs and we do not want them to be silently discarded.
         */
        String logDate = logDateFormat.format(new Date());
        StringBuilder logFileName = new StringBuilder(64);
        logFileName.append("dactransmit-");
        if (!operational) {
            logFileName.append("practice-");
        }
        logFileName.append(group);
        logFileName.append("-console-");
        logFileName.append(logDate);
        logFileName.append(".log");
        Path logFilePath = Paths.get(BMHConstants.getBmhHomeDirectory())
                .resolve("logs").resolve(logFileName.toString());
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
     * Checks if a given key has an active connection on a remote comms maanger.
     */
    public boolean isConnectedRemote(DacTransmitKey key) {
        return clusterServer.isConnected(key);
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
        this.disconnectedDacProcesses.remove(key);
        if (jms != null) {
            jms.listenForPlaylistChanges(key, group, transmitServer);
        }
        transmitServer.dacConnected(key);
        clusterServer.dacConnectedLocal(key);
        broadcastStreamServer.dacConnected(key, group);
        sendStatus();
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
     * This method should be called when another cluster member would like to
     * take over a dac for load balancing.
     */
    public void dacRequestedRemote(DacTransmitKey key) {
        transmitServer.dacRequested(key);
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
        broadcastStreamServer.dacDisconnected(key, group);
        /*
         * ensure that the dac was not shutdown at the request of another member
         * of the cluster.
         */
        if (!clusterServer.isConnected(key) && !clusterServer.isRequested(key)) {
            this.silenceAlarm.handleDacDisconnect(group);
        } else {
            /*
             * maybe this should always be done when a dactransmit shuts down
             * regardless of it it will be started on a different cluster or
             * not?
             */
            this.silenceAlarm.clearSilenceAlarm(group);
        }
        attemptLaunchDacTransmits();
        sendStatus();
    }

    /**
     * This method should be called when another comms manager loses its
     * connection to a dac or if this comms manager loses its connection with
     * another comms manager. Any other components that may need to refresh
     * and/or notify will be informed.
     */
    public void dacDisconnectedRemote(DacTransmitKey key) {
        clusterServer.dacDisconnectedRemote(key);
        attemptLaunchDacTransmits();
    }

    /**
     * This method should be called by the SilenceAlarm whenever the an alarm
     * activates/deactivates.
     */
    public void silenceStatusChanged() {
        sendStatus();
    }

    /**
     * This method should be called when a dac transmit process has sent a
     * message that needs to be forwarded on to the rest of the world(edex and
     * cave mostly).
     */
    public void transmitDacStatus(Object statusObject) {
        if (jms != null) {
            jms.sendBmhStatus(statusObject);
        }
        if (statusObject instanceof DacHardwareStatusNotification) {
            silenceAlarm
                    .handleDacHardwareStatus((DacHardwareStatusNotification) statusObject);
        }
    }

    /**
     * This method should be used to forward a {@link StatisticsEvent} to EDEX
     * for storage.
     * 
     * @param event
     *            the {@link StatisticsEvent} to forward.
     */
    public void transmitBMHStat(StatisticsEvent event) {
        if (jms != null) {
            jms.sendBmhStat(event);
        }
    }

    /**
     * This method should be used to forward a
     * {@link DacTransmitShutdownNotification} to EDEX to potentially trigger an
     * update of the playlist state.
     * 
     * @param notification
     *            the {@link DacTransmitShutdownNotification} to forward.
     */
    public void transmitDacShutdown(
            DacTransmitShutdownNotification notification, DacTransmitKey key) {
        /*
         * Verify that another cluster member does not own the dac transmit.
         */
        if (!clusterServer.isConnected(key) && !clusterServer.isRequested(key)) {
            this.transmitDacShutdown(notification);
        }
    }

    /**
     * This method should be used to forward a
     * {@link DacTransmitShutdownNotification} to EDEX to potentially trigger an
     * update of the playlist state. This version of the method should only be
     * used when there is NO possibility of clustering influencing the
     * management of a dac transmit process.
     * 
     * @param notification
     *            the {@link DacTransmitShutdownNotification} to forward.
     */
    public void transmitDacShutdown(DacTransmitShutdownNotification notification) {
        if (this.jms != null) {
            this.jms.sendBmhStatus(notification);
        }

        /*
         * Clear the silence alarm to ensure that silence will not be reported
         * for a dac transmit that is not currently running.
         */
        this.silenceAlarm.clearSilenceAlarm(notification.getTransmitterGroup());
    }

    /**
     * This method should be called when an error has been received from a dac
     * transmit process that needs to be communicated to users.
     * 
     * @param e
     * @param group
     */
    public void errorReceived(DacTransmitCriticalError e, String group) {
        logger.error(
                MarkerFactory.getMarker("Dac Transmit"),
                "Critical error received from group: " + group + ": "
                        + e.getErrorMessage(), e.getThrowable());
    }

    /**
     * Forwards any {@link ILiveBroadcastMessage}s that are received by other
     * components to the {@link BroadcastStreamServer}.
     * 
     * @param msg
     *            the {@link ILiveBroadcastMessage} to forward
     */
    public void forwardDacBroadcastMsg(ILiveBroadcastMessage msg) {
        broadcastStreamServer.handleBroadcastMsgExternal(msg);
    }

    public CommsConfig getCurrentConfigState() {
        return this.config;
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
        jms.close();
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

    private void addJmsConfigListener() {
        if (jms != null) {
            jms.addObserver(CommsConfigNotification.getTopicName(operational),
                    new INotificationObserver() {
                        @Override
                        public void notificationArrived(
                                NotificationMessage[] messages) {
                            for (NotificationMessage message : messages) {
                                try {
                                    Object payload = message
                                            .getMessagePayload();
                                    if (payload instanceof CommsConfigNotification) {
                                        reloadConfig(false);
                                    } else if (payload instanceof ChangeTimeZoneConfigNotification) {
                                        ChangeTimeZoneConfigNotification n = (ChangeTimeZoneConfigNotification) payload;
                                        changeTimeZone(n.getTimeZone(),
                                                n.getTransmitterGroup());
                                    }
                                } catch (NotificationException e) {
                                    logger.error("Cannot handle notification",
                                            e);
                                }
                            }

                        }
                    });
        }
    }

    private void addPracticeJmsShutdownListener() {
        if ((jms != null) && !operational) {
            jms.addObserver("BMH.Practice.Config", new INotificationObserver() {

                @Override
                public void notificationArrived(NotificationMessage[] messages) {
                    for (NotificationMessage message : messages) {
                        try {
                            Object payload = message.getMessagePayload();
                            if (payload instanceof PracticeModeConfigNotification) {
                                PracticeModeConfigNotification notif = (PracticeModeConfigNotification) payload;
                                if (notif.getType() == ConfigChangeType.Delete) {
                                    shutdown();
                                }
                            }
                        } catch (NotificationException e) {
                            logger.error("Cannot handle notification", e);
                        }
                    }

                }
            });
        }
    }

    private void sendStatus() {
        if (jms != null) {
            try {
                CommsManagerStatus status = new CommsManagerStatus(InetAddress
                        .getLocalHost().getHostName());
                if (config.getDacs() != null) {
                    for (DacConfig dac : config.getDacs()) {
                        for (DacChannelConfig channel : dac.getChannels()) {
                            DacTransmitKey key = new DacTransmitKey(dac,
                                    channel);
                            if (transmitServer.isConnectedToDac(key)) {
                                status.addConnectedTransmitterGroup(channel
                                        .getTransmitterGroup());
                            }
                        }
                    }
                }
                status.setSilentTransmitterGroups(silenceAlarm
                        .getAlarmingGroups());
                jms.sendBmhStatus(status);
            } catch (UnknownHostException e) {
                logger.error(
                        "Unable to send status due to problems resolving hostname:",
                        e);

            }
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
