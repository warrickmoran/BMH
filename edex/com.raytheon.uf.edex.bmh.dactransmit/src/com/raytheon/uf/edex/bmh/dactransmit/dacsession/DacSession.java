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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.raytheon.uf.common.bmh.broadcast.BroadcastStatus;
import com.raytheon.uf.common.bmh.broadcast.BroadcastTransmitterConfiguration;
import com.raytheon.uf.common.bmh.broadcast.LiveBroadcastCommand;
import com.raytheon.uf.common.bmh.broadcast.LiveBroadcastPlayCommand;
import com.raytheon.uf.common.bmh.broadcast.LiveBroadcastStartCommand;
import com.raytheon.uf.common.bmh.broadcast.OnDemandBroadcastConstants.MSGSOURCE;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.notify.status.DacHardwareStatusNotification;
import com.raytheon.uf.common.bmh.stats.DeliveryTimeEvent;
import com.raytheon.uf.common.stats.StatisticsEvent;
import com.raytheon.uf.edex.bmh.dactransmit.events.DacStatusUpdateEvent;
import com.raytheon.uf.edex.bmh.dactransmit.events.LostSyncEvent;
import com.raytheon.uf.edex.bmh.dactransmit.events.RegainSyncEvent;
import com.raytheon.uf.edex.bmh.dactransmit.events.ShutdownRequestedEvent;
import com.raytheon.uf.edex.bmh.dactransmit.events.handlers.IDacStatusUpdateEventHandler;
import com.raytheon.uf.edex.bmh.dactransmit.events.handlers.IShutdownRequestEventHandler;
import com.raytheon.uf.edex.bmh.dactransmit.playlist.PlaylistDirectoryObserver;
import com.raytheon.uf.edex.bmh.dactransmit.playlist.PlaylistScheduler;
import com.raytheon.uf.edex.bmh.dactransmit.playlist.PriorityBasedExecutorService;
import com.raytheon.uf.edex.bmh.dactransmit.util.NamedThreadFactory;

/**
 * Manages a transmission session to the DAC. Class pre-buffers all audio data
 * that will be sent to the DAC, and then fires 2 threads to manage the session.
 * First thread simply sends the audio in RTP-like packets until files have been
 * played. Second thread manages the control/status channel and maintains a sync
 * with the DAC and sends heartbeat messages to keep the connection alive.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 01, 2014  #3286     dgilling     Initial creation
 * Jul 14, 2014  #3286     dgilling     Switched to PlaylistScheduler to feed
 *                                      audio data to DataTransmitThread.
 * Jul 17, 2014  #3399     bsteffen     Add comms manager communication.
 * Jul 16, 2014  #3286     dgilling     Initial event bus implementation, add
 *                                      shutdown() method.
 * Jul 24, 2014  #3286     dgilling     Fix NullPointerException in 
 *                                      waitForShutdown().
 * Jul 29, 2014  #3286     dgilling     Use NamedThreadFactory.
 * Jul 31, 2014  #3286     dgilling     Send DacHardwareStatusNotification back
 *                                      to CommsManager.
 * Aug 08, 2014  #3286     dgilling     Inform CommsManager when sync is lost
 *                                      or re-gained.
 * Aug 12, 2014  #3486     bsteffen     Remove tranmistter group name
 * Aug 18, 2014  #3532     bkowal       Add transmitter decibel range
 * Aug 26, 2014  #3286     dgilling     Make construction of playlist directory
 *                                      observer optional.
 * Sep 4, 2014   #3532     bkowal       Use a decibel target instead of a range.
 * Oct 2, 2014   #3642     bkowal       Add transmitter timezone
 * Oct 15, 2014  #3655     bkowal       Support live broadcasting to the DAC.
 * Oct 21, 2014  #3655     bkowal       Use the new message types. Improve error
 *                                      handling.
 * Oct 21, 2014  #3655     bkowal       Delay the playback of interrupts during a live
 *                                      broadcast.
 * Nov 1, 2014   #3655     bkowal       Improved how data is shared between the main dac 
 *                                      thread and the live broadcast thread.
 * Nov 4, 2014   #3655     bkowal       Eliminate audio echo. Decrease buffer delay.
 * Nov 7, 2014   #3630     bkowal       Implement IDacSession
 * Nov 10, 2014  #3630     bkowal       Re-factor to support on-demand broadcasting.
 * Nov 11, 2014  #3762     bsteffen     Add delayed shutdown.
 * Nov 17, 2014  #3808     bkowal       Support broadcast live. Initial transition to
 *                                      transmitter group.
 * Nov 11, 2014  #3817     bsteffen     Periodically resend status.
 * Nov 21, 2014  #3845     bkowal       Transition to transmitter group is complete.
 * Jan 19, 2014  #3912     bsteffen     Handle events from control thread directly.
 * Jan 19, 2015  #4002     bkowal       Specify the type of live broadcast when delaying
 *                                      interrupts.
 * Feb 06, 2015  #4071     bsteffen     Consolidate threading.
 * Feb 11, 2015  #4098     bsteffen     Track packet sequence when switching to live.
 * Mar 05, 2015  #4229     bkowal       Include the broadcast id of the broadcast that could not
 *                                      be serviced because the dac was busy in the status.
 * Mar 06, 2015  #4188     bsteffen     Track interrupts only in PlaylistScheduler.
 * Apr 02, 2015  #4325     bsteffen     Do not sync on main thread.
 * Apr 15, 2015  #4397     bkowal       Provide additional information to the live broadcast thread.
 * Apr 16, 2015  #4405     rjpeter      Fail live broadcast if we don't have sync to dac.
 * Apr 27, 2015  #4397     bkowal       Added {@link #handleDeliveryTimeStat(DeliveryTimeEvent)} and
 *                                      {@link #deliverAllStartupStats()}.
 * Jul 08, 2015  #4636     bkowal       Support same and alert decibel levels.
 * Jul 28, 2015  #4686     bkowal       Moved statistics to common.
 * Oct 26, 2015  #5034     bkowal       Added {@link #checkForActiveLiveBroadcast()}.
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

public final class DacSession implements IDacStatusUpdateEventHandler,
        IShutdownRequestEventHandler, IDacSession {

    private static final int RESEND_HARDWARE_STATUS_MS = 30 * 1000;

    private static final int ASYNC_THREADS = Integer.getInteger(
            "DacSessionAsyncThreads", 2);

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final DacSessionConfig config;

    private final PlaylistScheduler playlistMgr;

    private final ExecutorService asyncExecutor;

    private final EventBus eventBus;

    private final DataTransmitThread dataThread;

    private LiveBroadcastTransmitThread broadcastThread;

    private final ControlStatusThread controlThread;

    private final PlaylistDirectoryObserver newPlaylistObserver;

    private final CommsManagerCommunicator commsManager;

    private final Semaphore shutdownSignal;

    private DacStatusMessage previousStatus;

    private final List<StatisticsEvent> undeliveredStatsList = new ArrayList<>();

    private long nextSendHardwareStatusTime = 0;

    /**
     * Constructor for the {@code DacSession} class. Reads the input directory
     * and buffers all audio files found.
     * 
     * @param config
     *            The paramters for this session.
     * @throws IOException
     *             If the specified input directory could not be read.
     */
    public DacSession(final DacSessionConfig config) throws IOException {
        this.config = config;
        this.asyncExecutor = new PriorityBasedExecutorService(ASYNC_THREADS,
                new NamedThreadFactory("DacSession-AsyncTasks"));
        this.eventBus = new EventBus("DAC-Transmit");
        this.playlistMgr = new PlaylistScheduler(this);
        this.controlThread = new ControlStatusThread(this,
                this.config.getDacAddress(), this.config.getControlPort());
        this.dataThread = new DataTransmitThread(this, playlistMgr, false);
        this.commsManager = new CommsManagerCommunicator(this);
        if (Boolean.getBoolean("enableDirectoryObserver")) {
            this.newPlaylistObserver = new PlaylistDirectoryObserver(this);
        } else {
            this.newPlaylistObserver = null;
        }
        this.shutdownSignal = new Semaphore(1);
        this.previousStatus = null;
    }

    /**
     * Returns the sessions event bus. This bus is synchronous and some events
     * are fired from threads with strict time constraints so anything
     * subscribing to the events should be able to respond quickly or
     * Asynchronously.
     */
    public EventBus getEventBus() {
        return this.eventBus;
    }

    /**
     * Common executor service for handling background events that need to be
     * processed during a dac session.
     */
    public ExecutorService getAsyncExecutor() {
        return this.asyncExecutor;
    }

    public DacSessionConfig getConfig() {
        return this.config;
    }

    /**
     * Starts this transmission session with the DAC. Runs until it hits the end
     * of the audio playlist.
     * 
     * @throws IOException
     *             If a transmission error occurs with the DAC.
     */
    @Override
    public void startPlayback() throws IOException {
        try {
            shutdownSignal.acquire();
        } catch (InterruptedException e) {
            // don't care
        }

        logger.info("Starting audio playback.");
        logger.info("Session configuration: " + config.toString());

        commsManager.start();
        eventBus.register(this);

        dataThread.start();
        controlThread.start();
        if (newPlaylistObserver != null) {
            newPlaylistObserver.start();
        }
        eventBus.register(playlistMgr);
    }

    /**
     * Blocking method that allows the main thread to wait until the DacSession
     * has been shutdown.
     */
    @Override
    public SHUTDOWN_STATUS waitForShutdown() {
        try {
            shutdownSignal.acquire();
        } catch (InterruptedException e) {
            logger.error(
                    "DacSession was interrupted while waiting to shutdown.", e);
        }

        logger.info("Initiating shutdown...");

        try {
            dataThread.join();
        } catch (InterruptedException e) {
            logger.error(
                    "DacSession was interrupted while waiting for the data thread.",
                    e);
        }

        controlThread.shutdown();
        try {
            controlThread.join();
        } catch (InterruptedException e) {
            logger.error(
                    "DacSession was interrupted while waiting for the control thread.",
                    e);
        }
        commsManager.shutdown();

        if (newPlaylistObserver != null) {
            newPlaylistObserver.shutdown();
        }

        asyncExecutor.shutdown();

        return SHUTDOWN_STATUS.SUCCESS;
    }

    @Override
    public void receivedDacStatus(DacStatusUpdateEvent e) {
        LiveBroadcastTransmitThread liveThread = this.broadcastThread;
        if (liveThread != null) {
            liveThread.receivedDacStatus(e);
        }
        dataThread.receivedDacStatus(e);
        DacStatusMessage newStatus = e.getStatus();
        AbstractTransmitThread currentPlayingThread = broadcastThread;
        if (currentPlayingThread == null) {
            currentPlayingThread = dataThread;
        }
        newStatus.setSequenceNumber(currentPlayingThread
                .getLastSequenceNumber());
        DacHardwareStatusNotification notify = newStatus.validateStatus(config,
                previousStatus);
        previousStatus = newStatus;
        commsManager.sendConnectionStatus(true);
        if (notify == null
                && System.currentTimeMillis() > nextSendHardwareStatusTime) {
            notify = newStatus.buildNotification(config);
        }
        if (notify != null) {
            commsManager.sendDacHardwareStatus(notify);
            nextSendHardwareStatusTime = System.currentTimeMillis()
                    + RESEND_HARDWARE_STATUS_MS;
        }
    }

    @Override
    public void lostDacSync(LostSyncEvent e) {
        LiveBroadcastTransmitThread liveThread = this.broadcastThread;
        if (liveThread != null) {
            liveThread.lostDacSync(e);
        }
        dataThread.lostDacSync(e);
        commsManager.sendConnectionStatus(false);
    }

    @Override
    public void regainDacSync(RegainSyncEvent e) {
        LiveBroadcastTransmitThread liveThread = this.broadcastThread;
        if (liveThread != null) {
            liveThread.regainDacSync(e);
        }
        dataThread.regainDacSync(e);
        commsManager.sendConnectionStatus(true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.edex.bmh.dactransmit.events.handlers.
     * IShutdownRequestEventHandler
     * #handleShutdownRequest(com.raytheon.uf.edex.bmh
     * .dactransmit.events.ShutdownRequestedEvent)
     */
    @Override
    @Subscribe
    public void handleShutdownRequest(ShutdownRequestedEvent e) {
        dataThread.shutdown(e.isNow());
        shutdownSignal.release();
    }

    /**
     * Will either send the specified {@link DeliveryTimeEvent} directly to the
     * Comms Manager or cache it until communication has been established with
     * the Comms Manager.
     * 
     * @param event
     *            the specified {@link DeliveryTimeEvent}
     */
    public void handleDeliveryTimeStat(DeliveryTimeEvent event) {
        if (this.commsManager != null && this.commsManager.isAlive()) {
            this.commsManager.forwardStatistics(event);
        } else {
            synchronized (this.undeliveredStatsList) {
                this.undeliveredStatsList.add(event);
            }
        }
    }

    /**
     * Used to forward any @{link StatisticsEvent} to the Comms Manager that
     * were cached during initial startup of the dac transmit session.
     */
    public void deliverAllStartupStats() {
        synchronized (this.undeliveredStatsList) {
            for (int i = 0; i < this.undeliveredStatsList.size(); i++) {
                StatisticsEvent event = this.undeliveredStatsList.remove(i);
                this.commsManager.forwardStatistics(event);
            }
        }
    }

    /*
     * 
     * BEGIN Live Broadcast Handling Methods
     */

    @Subscribe
    public void handleLiveBroadcastCommand(final LiveBroadcastCommand command) {
        logger.info("Received live broadcast {} command for broadcast {}.",
                command.getAction().toString(), command.getBroadcastId());
        switch (command.getAction()) {
        case PREPARE:
            this.prepareLiveBroadcast((LiveBroadcastStartCommand) command);
            break;
        case PLAY:
            this.handleLiveBroadcastData((LiveBroadcastPlayCommand) command);
            break;
        case STOP:
            this.shutdownLiveBroadcast(command.getBroadcastId());
            break;
        case TRIGGER:
            this.triggerLiveBroadcast(command.getBroadcastId());
            break;
        }
    }

    private void prepareLiveBroadcast(
            final LiveBroadcastStartCommand startCommand) {

        final BroadcastTransmitterConfiguration config = startCommand
                .getTransmitterGroupConfigurationMap().values().iterator()
                .next();
        final String transmitterName = config.getTransmitterGroup().getName();

        /*
         * Is an interrupt currently playing?
         */
        // also need to verify that a live broadcast streaming session is not
        // already running.
        if (this.dataThread.isPlayingInterrupt()) {
            logger.info(
                    "Unable to fulfill live broadcast request; an interrupt is currently playing on transmitter group {}.",
                    transmitterName);
            this.notifyLiveClientFailure(startCommand.getBroadcastId(),
                    startCommand.getTransmitterGroups(),
                    "An interrupt is currently already playing on Transmitter Group "
                            + transmitterName + ".", null);
            return;
        }

        /*
         * Don't have sync to dac
         */
        if (this.controlThread.isSynced() == false) {
            logger.info("Unable to fulfill live broadcast request; do not currently have dac sync.");
            this.notifyLiveClientFailure(startCommand.getBroadcastId(),
                    startCommand.getTransmitterGroups(),
                    "Do no have sync to DAC for Transmitter Group "
                            + transmitterName + ".", null);
            return;
        }

        /*
         * Delay any further interrupts ...
         */
        this.playlistMgr.lockInterrupts(startCommand.getType());

        /*
         * Prepare the live streaming thread.
         */
        try {
            this.broadcastThread = new LiveBroadcastTransmitThread(
                    this.eventBus, this.config.getDacAddress(),
                    this.config.getDataPort(), this.config.getTransmitters(),
                    startCommand.getBroadcastId(), this.dataThread,
                    this.commsManager, config, this.config.getDbTarget(),
                    this.config.getSameDbTarget(),
                    this.config.getAlertDbTarget(), startCommand.getType(),
                    startCommand.getRequestTime(), true);
        } catch (IOException e) {
            logger.error("Failed to create a thread for broadcast "
                    + startCommand.getBroadcastId() + "!", e);

            // failure - notify the client.
            this.notifyLiveClientFailure(startCommand.getBroadcastId(),
                    startCommand.getTransmitterGroups(),
                    "Failed to create a broadcast thread.", e);

            this.broadcastThread = null;
            return;
        }

        BroadcastStatus status = new BroadcastStatus();
        status.setMsgSource(MSGSOURCE.DAC);
        status.setStatus(true);
        status.setBroadcastId(startCommand.getBroadcastId());
        status.setTransmitterGroups(startCommand.getTransmitterGroups());
        commsManager.sendDacLiveBroadcastMsg(status);
    }

    public void handleLiveBroadcastData(LiveBroadcastPlayCommand playCommand) {
        if (this.broadcastThread.isError()) {
            /*
             * Do not process commands if the thread is in an error state. The
             * error state indicates that shutdown is imminent.
             */
            return;
        }

        this.broadcastThread.playAudio(playCommand.getAudio());
    }

    private void notifyLiveClientFailure(final String broadcastId,
            List<TransmitterGroup> transmitterGroups, final String message,
            final Exception exception) {
        BroadcastStatus status = new BroadcastStatus();
        status.setMsgSource(MSGSOURCE.DAC);
        status.setStatus(false);
        status.setBroadcastId(broadcastId);
        status.setTransmitterGroups(transmitterGroups);
        status.setMessage(message);
        status.setException(exception);

        commsManager.sendDacLiveBroadcastMsg(status);
    }

    private void triggerLiveBroadcast(final String broadcastId) {
        if (this.broadcastThread == null) {
            logger.error(
                    "Failed to start live broadcast {}. The broadcast has not been initialized yet.",
                    broadcastId);
            this.notifyLiveClientFailure(broadcastId, null,
                    "The broadcast has not been initialized yet.", null);
            return;
        }
        this.broadcastThread.start();
    }

    public void shutdownLiveBroadcast(final String broadcastId) {
        logger.info("Shutting down live broadcast ... {}", broadcastId);
        if (this.broadcastThread != null) {
            this.broadcastThread.shutdown();
            try {
                this.broadcastThread.join();
            } catch (InterruptedException e) {
                logger.warn(
                        "Interrupted while waiting for the live broadcast thread to stop for broadcast {}.",
                        broadcastId);
            }
        }

        this.broadcastThread = null;

        /*
         * Resume interrupts.
         */
        this.playlistMgr.resumeInterrupts();
    }

    /**
     * Checks for and returns the id of any live broadcast that may be running.
     * 
     * @return the id of the currently running live broadcast or {@code null} if
     *         a live broadcast is not currently running.
     */
    public String checkForActiveLiveBroadcast() {
        if (this.broadcastThread != null) {
            return this.broadcastThread.getBroadcastId();
        }
        return null;
    }

    /*
     * 
     * END Live Broadcast Handling Methods
     */
}
