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

import java.net.InetAddress;
import java.net.SocketException;
import java.util.Collection;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.raytheon.uf.common.bmh.audio.AudioPacketLogger;
import com.raytheon.uf.common.bmh.dac.dacsession.DacSessionConstants;
import com.raytheon.uf.common.bmh.notify.MessagePlaybackStatusNotification;
import com.raytheon.uf.edex.bmh.dactransmit.events.InterruptMessageReceivedEvent;
import com.raytheon.uf.edex.bmh.dactransmit.events.handlers.IInterruptMessageReceivedHandler;
import com.raytheon.uf.edex.bmh.dactransmit.playlist.DacMessagePlaybackData;
import com.raytheon.uf.edex.bmh.dactransmit.playlist.PlaylistScheduler;
import com.raytheon.uf.edex.bmh.dactransmit.rtp.RtpPacketIn;
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
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

public final class DataTransmitThread extends AbstractTransmitThread implements
        IInterruptMessageReceivedHandler {

    private final PlaylistScheduler playlistMgr;

    /*
     * Flag used to indicate if the process is shutting down, shutdown should
     * occur before playing a new message.
     */
    private volatile boolean keepRunning;

    private final AtomicInteger interruptsAvailable;

    private boolean playingInterrupt;

    /**
     * Flag indicating that any interrupts should be ignored until further
     * notice. This flag is used to ensure that interrupts will not interfere
     * with the initialization of a live broadcast.
     */
    private volatile boolean delayInterrupts;

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
     * @throws SocketException
     *             If the socket could not be opened.
     */
    public DataTransmitThread(final EventBus eventBus,
            final PlaylistScheduler playlistMgr, InetAddress address, int port,
            Collection<Integer> transmitters) throws SocketException {
        super("DataTransmitThread", eventBus, address, port, transmitters);
        this.playlistMgr = playlistMgr;
        this.keepRunning = true;
        this.interruptsAvailable = new AtomicInteger(0);
        this.playingInterrupt = false;
        this.warnNoData = true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {
        try {
            eventBus.register(this);

            OUTER_LOOP: while (keepRunning) {
                try {
                    while (this.pausePlayback) {
                        logger.info("Pausing the playback of the current playlist.");
                        this.pauseLock.acquire();
                        this.pauseLock.release();
                        logger.info("Resuming the playback of the current playlist.");
                    }

                    if ((this.delayInterrupts == false)
                            && (interruptsAvailable.get() > 0)) {
                        interruptsAvailable.decrementAndGet();
                    }

                    DacMessagePlaybackData playbackData = playlistMgr.next();
                    while ((playbackData == null) && keepRunning) {
                        if (warnNoData) {
                            logger.error("There are no valid messages available for transmit.");
                            warnNoData = false;
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

                    /*
                     * we set playing the playingInterrupt flag here in case
                     * this is a startup scenario and we had unplayed interrupts
                     * to start.
                     */
                    playingInterrupt = playbackData.isInterrupt();
                    AudioPacketLogger packetLog = new AudioPacketLogger(
                            "Transmit "
                                    + playbackData.getMessage()
                                            .getMessageType(), logger, 600);
                    while ((playbackData.hasRemaining())
                            && (playingInterrupt || (interruptsAvailable.get() == 0))) {
                        try {
                            while (!hasSync && !pausePlayback) {
                                Thread.sleep(DataTransmitConstants.DEFAULT_CYCLE_TIME);

                                if (hasSync && onSyncRestartMessage) {
                                    playbackData.resetAudio();
                                }
                            }

                            if (pausePlayback) {
                                continue OUTER_LOOP;
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

                            sendPacket(rtpPacket);
                            packetLog.packetProcessed();
                            previousPacket = rtpPacket;

                            Thread.sleep(nextCycleTime);
                        } catch (InterruptedException e) {
                            logger.error("Thread sleep interrupted.", e);
                            DefaultMessageLogger.getInstance().logError(
                                    BMH_COMPONENT.DAC_TRANSMIT,
                                    BMH_ACTIVITY.AUDIO_BROADCAST,
                                    playbackData.getMessage(), e);
                        } catch (Throwable t) {
                            logger.error(
                                    "Uncaught exception thrown from message playback loop.",
                                    t);
                            DefaultMessageLogger.getInstance().logError(
                                    BMH_COMPONENT.DAC_TRANSMIT,
                                    BMH_ACTIVITY.AUDIO_BROADCAST,
                                    playbackData.getMessage(), t);
                        }
                    }
                    packetLog.close();
                    playbackData.endPlayback();
                    // broadcast of the message has finished, log it.
                    DefaultMessageLogger.getInstance().logBroadcastActivity(
                            playbackData.getMessage());
                    playingInterrupt = false;
                } catch (Throwable t) {
                    logger.error("Uncaught exception thrown from main loop.", t);
                }
            }
        } finally {
            socket.disconnect();
            socket.close();
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

    public void pausePlayback() {
        try {
            this.pauseLock.acquire();
        } catch (InterruptedException e) {
            // TODO: actually throw exception to notify that current audio
            // stream could not be paused.
        }
        this.pausePlayback = true;
    }

    public void resumePlayback() {
        this.pausePlayback = false;
        this.pauseLock.release();
    }

    public boolean isPlayingInterrupt() {
        return this.playingInterrupt;
    }

    @Override
    @Subscribe
    public void handleInterruptMessage(InterruptMessageReceivedEvent e) {
        interruptsAvailable.incrementAndGet();
        logger.info("Received new interrupt: " + e.getPlaylist().toString());
    }

    public void lockInterrupts() {
        logger.info("Delaying interrupt playback. No interrupts will be played until further notice.");
        this.delayInterrupts = true;
    }

    public void resumeInterrupts() {
        logger.info("Resuming interrupt playback.");
        this.delayInterrupts = false;
    }
}