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
package com.raytheon.bmh.dactransmit.dacsession;

import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;

import com.raytheon.bmh.dactransmit.playlist.DacMessagePlaybackData;
import com.raytheon.bmh.dactransmit.playlist.PlaylistScheduler;
import com.raytheon.bmh.dactransmit.rtp.RtpPacketIn;
import com.raytheon.uf.common.bmh.audio.AudioPacketLogger;
import com.raytheon.uf.common.bmh.dac.dacsession.DacSessionConstants;
import com.raytheon.uf.common.bmh.notify.MessageBroadcastNotifcation;
import com.raytheon.uf.common.bmh.notify.MessagePlaybackStatusNotification;
import com.raytheon.uf.common.bmh.notify.SAMEMessageTruncatedNotification;
import com.raytheon.uf.edex.bmh.msg.logging.DefaultMessageLogger;
import com.raytheon.uf.edex.bmh.msg.logging.ErrorActivity.BMH_ACTIVITY;
import com.raytheon.uf.edex.bmh.msg.logging.ErrorActivity.BMH_COMPONENT;

/**
 * Thread for sending audio data to the DAC. Runs on a cycle time of 20 ms.
 * Every cycle this class will read the next chunk of audio data to send to the
 * DAC, package it up and transmit it via UDP. This process will continue until
 * this process has exhausted all audio files in its playlist.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 02, 2014  #3286     dgilling     Initial creation
 * Jul 14, 2014  #3286     dgilling     Use logback for logging, integrated
 *                                      PlaylistScheduler to feed audio files.
 * Jul 16, 2014  #3286     dgilling     Use event bus.
 * Jul 24, 2014  #3286     dgilling     Support interrupt messages.
 * Jul 25, 2014  #3286     dgilling     Support sending playback status to
 *                                      CommsManager.
 * Aug 08, 2014  #3286     dgilling     Support halting playback when sync is
 *                                      lost and resuming when sync is regained.
 * Aug 12, 2014  #3486     bsteffen     Allow changing transmitters
 * Aug 18, 2014  #3532     bkowal       Add transmitter decibel range. Adjust the
 *                                      audio before transmitting it.
 * Aug 24, 2014  3558      rjpeter      Added catch to main run.
 * Aug 25, 2014  #3286     dgilling     Re-organize try-catch blocks to keep 
 *                                      thread always running.
 * Aug 26, 2014  #3286     dgilling     Pause transmission when there is no
 *                                      data to send.
 * Oct 03, 2014  #3485     bsteffen     End playback after playing a message.
 * Oct 15, 2014  #3655     bkowal       Updated to use AbstractTransmitThread abstraction.
 * Oct 17, 2014  #3655     bkowal       Move tones to common.
 * Oct 21, 2014  #3655     bkowal       Support delaying interrupts.
 * Oct 29, 2014  #3774     bsteffen     Log Packets
 * Nov 11, 2014  #3762     bsteffen     Add delayed shutdown.
 * Dec 11, 2014  #3651     bkowal       Use {@link DefaultMessageLogger} to log msg activity.
 * Jan 05, 2015  #3651     bkowal       Use {@link DefaultMessageLogger} to log msg errors.
 * Jan 09, 2015  #3942     rjpeter      Add tracking of time to get playbackStatus.
 * Jan 12, 2015  #3968     bkowal       Publish {@link MessageBroadcastNotifcation} for messages
 *                                      that require broadcast confirmation.
 * Jan 14, 2015  #3969     bkowal       Use updated {@link MessageBroadcastNotifcation}.
 * Jan 22, 2015  #3912     bsteffen     Add more frequent packet logging and include intermessage intervals.
 * Feb 02, 2015  #4093     bsteffen     Add shutdown hook.
 * Feb 06, 2015  #4071     bsteffen     Consolidate threading.
 * Feb 11, 2015  #4098     bsteffen     Allow pause to maintain packet sequence numbers.
 * Feb 26, 2015  #4187     rjpeter      Added keepRunning check to allow shutdown when thread doesn't have sync.
 * Mar 06, 2015  #4188     bsteffen     Track interrupts only in PlaylistScheduler.
 * Apr 16, 2015  #4405     rjpeter      Update to have hasSync initialized.
 * May 04, 2015  #4452     bkowal       Post a {@link SAMEMessageTruncatedNotification} when a SAME
 *                                      Message will be truncated during broadcast.
 * May 13, 2015  #4429     rferrel      Changes to {@link DefaultMessageLogger} for traceId.
 * May 26, 2015  #4481     bkowal       Allow broadcasts to interrupt the no messages loop.
 * Jun 01, 2015  #4490     bkowal       Use new {@link SAMEMessageTruncatedNotification} constructor.
 * Jun 02, 2015  #4369     rferrel      Send NoPlaybackMessageNotification when no messages to play.
 * Jun 16, 2015  4482      rjpeter      Reset packet logger on pause.
 * Jan 21, 2015  5278      bkowal       Prevent rare audio NPE. Originally only happened due to
 *                                      conflicting future playlists being allowed to remain.
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

public final class DataTransmitThread extends AbstractTransmitThread {

    private final PlaylistScheduler playlistMgr;

    private final ExecutorService executorService;

    /*
     * Flag used to indicate if the process is shutting down, shutdown should
     * occur before playing a new message.
     */
    private volatile boolean keepRunning;

    private boolean playingInterrupt;

    private volatile boolean warnNoData;

    /*
     * Flag indicating the current message should be stopped, either for a live
     * broadcast or for an immediate process shutdown.
     */
    private volatile boolean pausePlayback;

    private final Semaphore pauseLock = new Semaphore(1);

    /**
     * Constructor for this thread. Attempts to open a {@code DatagramSocket}
     * and connect to DAC IP endpoint specified by IP address and port.
     * 
     * @param eventBus
     *            Reference back to the application-wide {@code EventBus}
     *            instance for receiving necessary status events.
     * @param playlistMgr
     *            {@code PlaylistManager} reference used to retrieve next file
     *            to send to DAC.
     * @param transmitters
     *            List of destination transmitters for this audio data.
     * @param dacAdress
     *            {@code InetAddress} of DAC IP endpoint.
     * @param dataPort
     *            Port to send data over.
     * @param hasSync
     *            If currently sync'd to DAC.
     * @throws SocketException
     *             If the socket could not be opened.
     */
    public DataTransmitThread(DacSession dacSession,
            final PlaylistScheduler playlistMgr, boolean hasSync)
            throws SocketException {
        super("DataTransmitThread", dacSession.getEventBus(), dacSession
                .getConfig().getDacAddress(), dacSession.getConfig()
                .getDataPort(), dacSession.getConfig().getTransmitters(),
                hasSync);
        this.playlistMgr = playlistMgr;
        this.executorService = dacSession.getAsyncExecutor();
        this.keepRunning = true;
        this.playingInterrupt = false;
        this.warnNoData = true;
        Runtime.getRuntime().addShutdownHook(
                new Thread("ShuttingDownDataTransmit") {

                    @Override
                    public void run() {
                        DataTransmitThread dtt = DataTransmitThread.this;
                        if (dtt.isAlive()) {
                            logger.info("Stopping data transmit thread due to process shutdown.");
                            shutdown(true);
                            try {
                                dtt.join();
                            } catch (InterruptedException e) {
                                logger.error(
                                        "Failed to wait for data transmit thread shutdown.",
                                        e);
                            }
                        }
                    }

                });
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {
        AudioPacketLogger allPacketLog = new AudioPacketLogger(getName(),
                logger, 10);
        try {
            try {
                this.pauseLock.acquire();
            } catch (InterruptedException e) {
                logger.error(
                        "Unexpected interrupt while acquiring pause lock.", e);
            }
            long nextPacketTime = System.currentTimeMillis();
            eventBus.register(this);
            OUTER_LOOP: while (keepRunning) {
                AudioPacketLogger messagePacketLog = null;
                try {
                    while (this.pausePlayback) {
                        logger.info("Pausing the playback of the current playlist.");
                        this.pauseLock.release();
                        allPacketLog.reset();
                        /*
                         * Sleeping gives the pausing thread time to steal the
                         * lock.
                         */
                        Thread.sleep(5);
                        this.pauseLock.acquire();
                        logger.info("Resuming the playback of the current playlist.");
                    }

                    DacMessagePlaybackData playbackData = playlistMgr.next();
                    while ((playbackData == null) && keepRunning) {
                        if (warnNoData) {
                            logger.error("There are no valid messages available for transmit.");
                            playlistMgr.sendNoPlaybackNotification();
                            warnNoData = false;
                        }

                        if (pausePlayback && keepRunning) {
                            warnNoData = true;
                            continue OUTER_LOOP;
                        }

                        playbackData = playlistMgr.next();
                        if (playbackData == null) {
                            // FIXME: extract to constant once reasonable
                            // default has been established
                            Thread.sleep(1000);
                        } else {
                            logger.info("DataTransmitThread has resumed transmission.");
                            warnNoData = true;
                        }
                    }

                    if (!keepRunning) {
                        continue;
                    }

                    if (playbackData.getAudio() == null) {
                        logger.error("Failed to retrieve audio for message: "
                                + playbackData.getMessage()
                                + ". The message will be skipped.");
                        continue;
                    }

                    /*
                     * we set playing the playingInterrupt flag here in case
                     * this is a startup scenario and we had unplayed interrupts
                     * to start.
                     */
                    playingInterrupt = playbackData.isInterrupt();
                    messagePacketLog = new AudioPacketLogger("Transmit "
                            + playbackData.getMessage().getMessageType(),
                            logger, 600);

                    if (playbackData.requiresConfirmation()) {
                        /*
                         * Message confirmation notification.
                         */
                        this.eventBus.post(new MessageBroadcastNotifcation(
                                playbackData.getMessage().getBroadcastId()));
                    }
                    if (playbackData.requiresToneTruncationNotification()) {
                        /*
                         * SAME Message truncation notification.
                         */
                        this.eventBus
                                .post(new SAMEMessageTruncatedNotification(
                                        playbackData.getMessage()));
                    }
                    while ((playbackData.hasRemaining())
                            && (playingInterrupt || (!playlistMgr
                                    .hasInterrupt()))) {
                        try {
                            while (!hasSync && !pausePlayback && keepRunning) {
                                Thread.sleep(DataTransmitConstants.DEFAULT_CYCLE_TIME);

                                if (hasSync && onSyncRestartMessage) {
                                    playbackData.resetAudio();
                                }
                            }

                            if (pausePlayback) {
                                if (keepRunning) {
                                    continue OUTER_LOOP;
                                } else {
                                    playbackData.writePosition();
                                    return;
                                }
                            }

                            byte[] nextPayload = new byte[DacSessionConstants.SINGLE_PAYLOAD_SIZE];

                            long t0 = System.currentTimeMillis();
                            MessagePlaybackStatusNotification playbackStatus = playbackData
                                    .get(nextPayload);
                            long t1 = System.currentTimeMillis();
                            long totalTime = t1 - t0;

                            if (totalTime > 5) {
                                logger.debug("Time to get playback status: "
                                        + (totalTime));
                            }

                            if (playbackStatus != null) {
                                logger.debug("Posting playback status update: "
                                        + playbackStatus.toString());
                                eventBus.post(playbackStatus);
                            }

                            RtpPacketIn rtpPacket = buildRtpPacket(
                                    previousPacket, nextPayload);
                            long sleepTime = nextPacketTime
                                    - System.currentTimeMillis();
                            if (sleepTime > 0) {
                                try {
                                    Thread.sleep(sleepTime);
                                } catch (InterruptedException e) {
                                    logger.error("Thread sleep interrupted.", e);
                                    DefaultMessageLogger
                                            .getInstance()
                                            .logError(
                                                    null,
                                                    BMH_COMPONENT.DAC_TRANSMIT,
                                                    BMH_ACTIVITY.AUDIO_BROADCAST,
                                                    playbackData.getMessage(),
                                                    e);
                                }
                            }
                            sendPacket(rtpPacket);
                            nextPacketTime = nextPacketTime + packetInterval;
                            /*
                             * Never send before FAST_CYCLE_TIME because it
                             * increases the risk that packets will arrive out
                             * of order. Do not try to catch up if more than one
                             * packet behind, the control status thread will
                             * eventually adjust the rate more accurately based
                             * off the actual buffer levels.
                             */
                            nextPacketTime = Math
                                    .max(System.currentTimeMillis()
                                            + DataTransmitConstants.FAST_CYCLE_TIME,
                                            nextPacketTime);
                            messagePacketLog.packetProcessed();
                            allPacketLog.packetProcessed();
                            previousPacket = rtpPacket;
                        } catch (Throwable t) {
                            logger.error(
                                    "Uncaught exception thrown from message playback loop.",
                                    t);
                            DefaultMessageLogger.getInstance().logError(null,
                                    BMH_COMPONENT.DAC_TRANSMIT,
                                    BMH_ACTIVITY.AUDIO_BROADCAST,
                                    playbackData.getMessage(), t);
                        }
                    }

                    executorService.submit(playbackData.getEndPlayBackTask());
                    // broadcast of the message has finished, log it.
                    DefaultMessageLogger.getInstance().logBroadcastActivity(
                            playbackData.getMessage(),
                            playbackData.getMessage());
                    playingInterrupt = false;
                } catch (Throwable t) {
                    logger.error("Uncaught exception thrown from main loop.", t);
                } finally {
                    if (messagePacketLog != null) {
                        messagePacketLog.close();
                    }
                }
            }
        } finally {
            socket.disconnect();
            socket.close();
            allPacketLog.close();
        }
    }

    /**
     * Informs this thread that it should begin shutdown. This thread will not
     * die until it has reached the end of the current message. If your code
     * desires to block until this thread dies, use {@link #join()} or
     * {@link #isAlive()}.
     * 
     * @see #join()
     * @see #isAlive()
     */
    public void shutdown(boolean now) {
        keepRunning = false;
        pausePlayback = now | pausePlayback;
    }

    public RtpPacketIn pausePlayback() {
        this.pausePlayback = true;
        try {
            this.pauseLock.acquire();
        } catch (InterruptedException e) {
            // TODO: actually throw exception to notify that current audio
            // stream could not be paused.
        }
        return this.previousPacket;
    }

    public void resumePlayback(RtpPacketIn previousPacket) {
        this.previousPacket = previousPacket;
        this.pausePlayback = false;
        this.pauseLock.release();
    }

    public boolean isPlayingInterrupt() {
        return this.playingInterrupt;
    }

}