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
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.AsyncEventBus;
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
import com.raytheon.uf.edex.bmh.dactransmit.events.DacStatusUpdateEvent;
import com.raytheon.uf.edex.bmh.dactransmit.events.LostSyncEvent;
import com.raytheon.uf.edex.bmh.dactransmit.events.RegainSyncEvent;
import com.raytheon.uf.edex.bmh.dactransmit.events.ShutdownRequestedEvent;
import com.raytheon.uf.edex.bmh.dactransmit.events.handlers.IDacStatusUpdateEventHandler;
import com.raytheon.uf.edex.bmh.dactransmit.events.handlers.IShutdownRequestEventHandler;
import com.raytheon.uf.edex.bmh.dactransmit.playlist.PlaylistDirectoryObserver;
import com.raytheon.uf.edex.bmh.dactransmit.playlist.PlaylistScheduler;
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
 * 
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

public final class DacSession implements IDacStatusUpdateEventHandler,
        IShutdownRequestEventHandler, IDacSession {

    private static final int RESEND_HARDWARE_STATUS_MS = 30 * 1000;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final DacSessionConfig config;

    private final PlaylistScheduler playlistMgr;

    private final ExecutorService notificationExecutor;

    private final EventBus eventBus;

    private final DataTransmitThread dataThread;

    private LiveBroadcastTransmitThread broadcastThread;

    private final ControlStatusThread controlThread;

    private final PlaylistDirectoryObserver newPlaylistObserver;

    private volatile boolean isRunning;

    private CommsManagerCommunicator commsManager;

    private final Semaphore shutdownSignal;

    private DacStatusMessage previousStatus;

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
        this.notificationExecutor = Executors
                .newSingleThreadExecutor(new NamedThreadFactory("EventBus"));
        this.eventBus = new AsyncEventBus("DAC-Transmit", notificationExecutor);
        this.playlistMgr = new PlaylistScheduler(
                this.config.getInputDirectory(), this.eventBus,
                this.config.getDbTarget(), this.config.getTimezone());
        this.controlThread = new ControlStatusThread(this,
                this.config.getDacAddress(), this.config.getControlPort());
        this.dataThread = new DataTransmitThread(this.eventBus, playlistMgr,
                this.config.getDacAddress(), this.config.getDataPort(),
                this.config.getTransmitters());
        this.commsManager = new CommsManagerCommunicator(this.config,
                this.eventBus);
        if (Boolean.getBoolean("enableDirectoryObserver")) {
            this.newPlaylistObserver = new PlaylistDirectoryObserver(
                    this.config.getInputDirectory(), this.eventBus);
        } else {
            this.newPlaylistObserver = null;
        }
        this.shutdownSignal = new Semaphore(1);
        this.previousStatus = null;
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
        logger.info("Obtaining sync with DAC.");

        commsManager.start();
        eventBus.register(this);

        controlThread.performInitialSync();

        logger.info("Obtained sync with DAC and beginning transmission.");

        dataThread.start();
        controlThread.start();
        if (newPlaylistObserver != null) {
            newPlaylistObserver.start();
        }
        eventBus.register(playlistMgr);

        isRunning = true;
    }

    /**
     * Performs orderly shutdown of this {@code DacSession} instance. Will wait
     * for the current message being transmitted to finish before completing.
     * After calling this method, the instance cannot be restarted; a new one
     * must be created.
     * 
     * @throws InterruptedException
     *             If any thread interrupts the current thread while waiting for
     *             all the child threads to die.
     */
    public void shutdown(boolean now) throws InterruptedException {
        logger.info("Initiating shutdown...");

        dataThread.shutdown(now);
        playlistMgr.shutdown();
        dataThread.join();

        controlThread.shutdown();
        controlThread.join();

        commsManager.shutdown();

        if (newPlaylistObserver != null) {
            newPlaylistObserver.shutdown();
        }

        notificationExecutor.shutdown();

        isRunning = false;

        shutdownSignal.release();
    }

    /**
     * Blocking method that allows the main thread to wait until the DacSession
     * has been shutdown.
     */
    @Override
    public SHUTDOWN_STATUS waitForShutdown() {
        if (isRunning) {
            try {
                shutdownSignal.acquire();
            } catch (InterruptedException e) {
                logger.error(
                        "DacSession.waitForShutdown() interrupted by another thread.",
                        e);
            } finally {
                shutdownSignal.release();
            }
        }

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
        newStatus.setSequenceNumber(dataThread.getLastSequenceNumber());
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
        try {
            shutdown(e.isNow());
        } catch (InterruptedException e1) {
            logger.error("Shutdown interrupted.", e1);
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
         * Delay any further interrupts ...
         */
        this.playlistMgr.lockInterrupts(startCommand.getType());
        this.dataThread.lockInterrupts();

        /*
         * Prepare the live streaming thread.
         */
        try {
            this.broadcastThread = new LiveBroadcastTransmitThread(
                    this.eventBus, this.config.getDacAddress(),
                    this.config.getDataPort(), this.config.getTransmitters(),
                    startCommand.getBroadcastId(), this.dataThread,
                    this.commsManager, config, this.config.getDbTarget(),
                    startCommand.getType());
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

    private void shutdownLiveBroadcast(final String broadcastId) {
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
        this.dataThread.resumeInterrupts();
    }

    /*
     * 
     * END Live Broadcast Handling Methods
     */
}