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
package com.raytheon.uf.edex.bmh.dactransmit.dacsession;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.primitives.Ints;
import com.raytheon.uf.common.bmh.broadcast.ILiveBroadcastMessage;
import com.raytheon.uf.common.bmh.datamodel.playlist.PlaylistUpdateNotification;
import com.raytheon.uf.common.bmh.notify.LiveBroadcastSwitchNotification;
import com.raytheon.uf.common.bmh.notify.MessageBroadcastNotifcation;
import com.raytheon.uf.common.bmh.notify.MessageDelayedBroadcastNotification;
import com.raytheon.uf.common.bmh.notify.MessageNotBroadcastNotification;
import com.raytheon.uf.common.bmh.notify.MessagePlaybackStatusNotification;
import com.raytheon.uf.common.bmh.notify.NoPlaybackMessageNotification;
import com.raytheon.uf.common.bmh.notify.PlaylistSwitchNotification;
import com.raytheon.uf.common.bmh.notify.SAMEMessageTruncatedNotification;
import com.raytheon.uf.common.bmh.notify.status.DacHardwareStatusNotification;
import com.raytheon.uf.common.serialization.SerializationException;
import com.raytheon.uf.common.serialization.SerializationUtil;
import com.raytheon.uf.common.stats.StatisticsEvent;
import com.raytheon.uf.edex.bmh.dactransmit.events.CriticalErrorEvent;
import com.raytheon.uf.edex.bmh.dactransmit.events.ShutdownRequestedEvent;
import com.raytheon.uf.edex.bmh.dactransmit.ipc.ChangeDecibelTarget;
import com.raytheon.uf.edex.bmh.dactransmit.ipc.ChangeTimeZone;
import com.raytheon.uf.edex.bmh.dactransmit.ipc.ChangeTransmitters;
import com.raytheon.uf.edex.bmh.dactransmit.ipc.DacTransmitCriticalError;
import com.raytheon.uf.edex.bmh.dactransmit.ipc.DacTransmitRegister;
import com.raytheon.uf.edex.bmh.dactransmit.ipc.DacTransmitScanPlaylists;
import com.raytheon.uf.edex.bmh.dactransmit.ipc.DacTransmitShutdown;
import com.raytheon.uf.edex.bmh.dactransmit.ipc.DacTransmitStatus;
import com.raytheon.uf.edex.bmh.dactransmit.playlist.PrioritizableCallable;
import com.raytheon.uf.edex.bmh.dactransmit.playlist.PriorityBasedExecutorService;
import com.raytheon.uf.edex.bmh.dactransmit.playlist.ScanPlaylistDirectoryTask;

/**
 * 
 * Implements a thread for reading messages from the comms manager and also
 * provides methods for sending messages to the comms manager.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Jul 16, 2014  3399     bsteffen    Initial creation
 * Jul 22, 2014  3286     dgilling    Connect to event bus.
 * Jul 25, 2014  3286     dgilling    Support sending message playback updates
 *                                    to CommsManager.
 * Jul 29, 2014  3286     dgilling    Add dedicated ExecutorService for async
 *                                    post back of status to CommsManager, set
 *                                    TCP_NODELAY on IPC socket.
 * Jul 31, 2014  3286     dgilling    Send DacHardwareStatusNotification.
 * Aug 12, 2014  3486     bsteffen    Remove group from registration
 * Aug 14, 2014  3286     dgilling    Send DacTransmitCriticalError.
 * Aug 18, 2014  3532     bkowal      Support ChangeDecibelRange.
 * Aug 26, 2014  3486     bsteffen    Stop writerThread in shutdown.
 * Sep 4, 2014   3532     bkowal      Replace ChangeDecibelRange with ChangeDecibelTarget.
 * Oct 15, 2014  3655     bkowal      Support live broadcasting to the DAC.
 * Oct 21, 2014  3655     bkowal      Use the new message types.
 * Oct 21, 2014  3655     bkowal      Support LiveBroadcastSwitchNotification.
 * Oct 22, 2014  3687     bsteffen    Send hostname instead of address back to comms manager.
 * Nov 11, 2014  3762     bsteffen    Add delayed shutdown.
 * Jan 12, 2015  3968     bkowal      Handle {@link MessageBroadcastNotifcation}.
 * Jan 14, 2015  3969     bkowal      Handle {@link MessageNotBroadcastNotification}.
 * Jan 19, 2015  4002     bkowal      Handle {@link MessageDelayedBroadcastNotification}.
 * Feb 06, 2015  4071     bsteffen    Consolidate threading.
 * Feb 16, 2015  4107     bsteffen    Manually scan for playlist changes when the comms manager
 *                                    is down or has not sent a command to stop scanning.
 * Mar 20, 2015  4296     bsteffen    Catch all throwables from SerializationUtil, detect self connection.
 * Apr 15, 2015  4397     bkowal      Added {@link #forwardStatistics(StatisticsEvent)}.
 * Apr 24, 2015  4423     rferrel     Post {@link ChangeTimeZone} messages.
 * Apr 27, 2015  4397     bkowal      Initiate the transfer of {@link StatisticsEvent}s cached
 *                                    during startup to Comms Manager.
 * May 04, 2015  4452     bkowal      Handle {@link SAMEMessageTruncatedNotification}.
 * Jun 02, 2015  4369     rferrel     Handle {@link NoPlaybackMessageNotification}.
 * Jul 08, 2015  4636     bkowal      Support same and alert decibel levels.
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public final class CommsManagerCommunicator extends Thread {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final InetAddress commsHost = InetAddress.getLoopbackAddress();

    private final DacSessionConfig config;

    private final Object sendLock = new Object();

    private final EventBus eventBus;

    private final ExecutorService executorService;

    private boolean scan;

    private final ScanPlaylistDirectoryTask backupPlaylistTask;

    private Socket socket;

    private DacTransmitStatus statusToSend = new DacTransmitStatus(false);

    private transient boolean running = true;

    private final DacSession dacSession;

    public CommsManagerCommunicator(DacSession dacSession) {
        super("CommsManagerReaderThread");
        this.dacSession = dacSession;
        this.config = dacSession.getConfig();
        this.eventBus = dacSession.getEventBus();
        this.executorService = dacSession.getAsyncExecutor();
        backupPlaylistTask = new ScanPlaylistDirectoryTask(eventBus, dacSession
                .getConfig().getInputDirectory());
        /*
         * Allow the task to prepopulate the playlist, this is not important
         * enough to delay startup.
         */
        executorService.submit(backupPlaylistTask);
    }

    @Override
    public void run() {
        eventBus.register(this);

        InputStream inputStream = null;
        while (running) {
            if (socket == null) {
                OutputStream outputStream = null;
                synchronized (sendLock) {
                    try {
                        socket = new Socket(commsHost, config.getManagerPort());
                        if (socket.getLocalPort() == config.getManagerPort()) {
                            /*
                             * If the manager port is within the ephemeral range
                             * then on some OSs it is possible that the socket
                             * is connected to itself using TCP simultaneous
                             * open. This is only possible if the comms manager
                             * is not running and it causes the comms manager to
                             * fail to start because the port is in use so it is
                             * important to abort the connection ASAP.
                             */
                            logger.error(
                                    "Port reuse has been detected on port {}, connection to comms manager will be aborted.",
                                    config.getManagerPort());
                            disconnect();
                        } else {
                            socket.setTcpNoDelay(true);
                            inputStream = socket.getInputStream();
                            outputStream = socket.getOutputStream();
                        }
                    } catch (IOException e) {
                        logger.error("Unable to connect to comms manager", e);
                        disconnect();
                    }
                    if (socket != null) {
                        try {

                            DacTransmitRegister registration = new DacTransmitRegister(
                                    config.getInputDirectory().toString(),
                                    config.getDataPort(),
                                    config.getDacHostname(),
                                    Ints.toArray(config.getTransmitters()),
                                    config.getDbTarget(),
                                    config.getSameDbTarget(),
                                    config.getAlertDbTarget());
                            SerializationUtil.transformToThriftUsingStream(
                                    registration, outputStream);
                            if (statusToSend.isConnectedToDac()) {
                                SerializationUtil.transformToThriftUsingStream(
                                        statusToSend, outputStream);
                            }
                            this.dacSession.deliverAllStartupStats();
                        } catch (Throwable e) {
                            logger.error(
                                    "Unable to communicate with comms manager",
                                    e);
                            disconnect();
                        }
                    }
                }
            }
            if (socket == null || scan) {
                executorService.submit(backupPlaylistTask);
            }
            if (socket == null) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    /* just try again sooner */
                }
                continue;
            } else {
                try {
                    handleMessage(SerializationUtil.transformFromThrift(
                            Object.class, inputStream));
                } catch (Throwable e) {
                    logger.error("Error reading message from comms manager", e);
                    disconnect();
                }
            }
        }
    }

    private void disconnect() {
        synchronized (sendLock) {
            try {
                if (socket != null) {
                    try {
                        SerializationUtil.transformToThriftUsingStream(
                                new DacTransmitShutdown(),
                                socket.getOutputStream());
                    } catch (SerializationException e) {
                        logger.error(
                                "Unable to send shutdown message to comms manager",
                                e);
                    }
                    socket.close();
                    socket = null;
                }
            } catch (IOException ignorable) {
                logger.error("Error closing message to comms manager");
            }
        }
    }

    private void handleMessage(Object message) {
        if (message instanceof DacTransmitShutdown) {
            eventBus.post(new ShutdownRequestedEvent(
                    ((DacTransmitShutdown) message).isNow()));
        } else if (message instanceof PlaylistUpdateNotification) {
            eventBus.post(message);
        } else if (message instanceof ChangeTransmitters) {
            eventBus.post(message);
        } else if (message instanceof ChangeDecibelTarget) {
            eventBus.post(message);
        } else if (message instanceof ILiveBroadcastMessage) {
            eventBus.post(message);
        } else if (message instanceof LiveBroadcastSwitchNotification) {
            eventBus.post(message);
        } else if (message instanceof DacTransmitScanPlaylists) {
            scan = ((DacTransmitScanPlaylists) message).isScan();
        } else if (message instanceof ChangeTimeZone) {
            eventBus.post(message);
        } else {
            logger.error("Unrecognized message from comms manager of type "
                    + message.getClass().getSimpleName());
        }
    }

    public void shutdown() {
        running = false;
        disconnect();
    }

    public void sendConnectionStatus(boolean connected) {
        statusToSend = new DacTransmitStatus(connected);
        sendMessageToCommsManager(statusToSend);
    }

    public void sendDacHardwareStatus(DacHardwareStatusNotification status) {
        sendMessageToCommsManager(status);
    }

    public void sendDacLiveBroadcastMsg(ILiveBroadcastMessage msg) {
        sendMessageToCommsManager(msg);
    }

    public void forwardStatistics(StatisticsEvent event) {
        sendMessageToCommsManager(event);
    }

    @Subscribe
    public void sendPlaybackStatus(
            MessagePlaybackStatusNotification notification) {
        executorService.submit(new SendToCommsManagerTask(notification));
    }

    @Subscribe
    public void sendPlaylistSwitch(PlaylistSwitchNotification notification) {
        executorService.submit(new SendToCommsManagerTask(notification));
    }

    @Subscribe
    public void sendPlaylistSwitch(LiveBroadcastSwitchNotification notification) {
        executorService.submit(new SendToCommsManagerTask(notification));
    }

    @Subscribe
    public void handleCriticalError(CriticalErrorEvent e) {
        executorService.submit(new SendToCommsManagerTask(
                new DacTransmitCriticalError(e.getErrorMessage(), e
                        .getThrowable())));
    }

    @Subscribe
    public void handleMsgBroadcastNotification(
            MessageBroadcastNotifcation notification) {
        executorService.submit(new SendToCommsManagerTask(notification));
    }

    @Subscribe
    public void handleSAMEMessageTruncatedNotification(
            SAMEMessageTruncatedNotification notification) {
        executorService.submit(new SendToCommsManagerTask(notification));
    }

    @Subscribe
    public void handleMsgNotBroadcastNotification(
            MessageNotBroadcastNotification notification) {
        executorService.submit(new SendToCommsManagerTask(notification));
    }

    @Subscribe
    public void handleNoPlaybackMessageNotification(
            NoPlaybackMessageNotification notification) {
        executorService.submit(new SendToCommsManagerTask(notification));
    }

    @Subscribe
    public void handleBroadcastDelayedNotification(
            MessageDelayedBroadcastNotification notification) {
        executorService.submit(new SendToCommsManagerTask(notification));
    }

    private void sendMessageToCommsManager(final Object message) {
        synchronized (sendLock) {
            if (socket != null) {
                try {
                    SerializationUtil.transformToThriftUsingStream(message,
                            socket.getOutputStream());
                } catch (SerializationException | IOException e) {
                    logger.error("Error communicating with comms manager", e);
                }
            } else {
                logger.warn("Received notification that could not be sent to CommsManager: "
                        + message.toString());
            }
        }
    }

    private class SendToCommsManagerTask implements
            PrioritizableCallable<Object> {

        private final Object message;

        public SendToCommsManagerTask(Object message) {
            super();
            this.message = message;
        }

        @Override
        public Object call() {
            sendMessageToCommsManager(message);
            return null;
        }

        @Override
        public Integer getPriority() {
            return PriorityBasedExecutorService.PRIORITY_LOW;
        }

    }

}
