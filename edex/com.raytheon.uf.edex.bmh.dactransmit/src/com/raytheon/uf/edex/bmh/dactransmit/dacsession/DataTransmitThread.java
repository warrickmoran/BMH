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
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.primitives.Ints;
import com.raytheon.uf.common.bmh.notify.MessagePlaybackStatusNotification;
import com.raytheon.uf.edex.bmh.dactransmit.events.DacStatusUpdateEvent;
import com.raytheon.uf.edex.bmh.dactransmit.events.InterruptMessageReceivedEvent;
import com.raytheon.uf.edex.bmh.dactransmit.events.LostSyncEvent;
import com.raytheon.uf.edex.bmh.dactransmit.events.RegainSyncEvent;
import com.raytheon.uf.edex.bmh.dactransmit.events.handlers.IDacStatusUpdateEventHandler;
import com.raytheon.uf.edex.bmh.dactransmit.events.handlers.IInterruptMessageReceivedHandler;
import com.raytheon.uf.edex.bmh.dactransmit.ipc.ChangeTransmitters;
import com.raytheon.uf.edex.bmh.dactransmit.playlist.DacMessagePlaybackData;
import com.raytheon.uf.edex.bmh.dactransmit.playlist.PlaylistScheduler;
import com.raytheon.uf.edex.bmh.dactransmit.rtp.RtpPacketIn;
import com.raytheon.uf.edex.bmh.dactransmit.rtp.RtpPacketInFactory;

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
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

public final class DataTransmitThread extends Thread implements
        IDacStatusUpdateEventHandler, IInterruptMessageReceivedHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final EventBus eventBus;

    private final InetAddress address;

    private final int port;

    private Collection<Integer> transmitters;

    private final DatagramSocket socket;

    private final PlaylistScheduler playlistMgr;

    private RtpPacketIn previousPacket;

    private volatile boolean keepRunning;

    private long nextCycleTime;

    private final AtomicInteger interruptsAvailable;

    private boolean playingInterrupt;

    private volatile boolean hasSync;

    private volatile boolean onSyncRestartMessage;

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
        super("DataTransmitThread");
        this.eventBus = eventBus;
        this.address = address;
        this.port = port;
        this.transmitters = transmitters;
        this.playlistMgr = playlistMgr;
        this.previousPacket = null;
        this.keepRunning = true;
        this.nextCycleTime = DataTransmitConstants.INITIAL_CYCLE_TIME;
        this.socket = new DatagramSocket();
        this.interruptsAvailable = new AtomicInteger(0);
        this.playingInterrupt = false;
        this.hasSync = true;
        this.onSyncRestartMessage = false;
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
                    if (interruptsAvailable.get() > 0) {
                        interruptsAvailable.decrementAndGet();
                    }

                    DacMessagePlaybackData playbackData = playlistMgr.next();
                    // we set playing the playingInterrupt flag here in case
                    // this is
                    // a startup scenario and we had unplayed interrupts to
                    // start.
                    playingInterrupt = playbackData.isInterrupt();

                    while ((playbackData.hasRemaining())
                            && (playingInterrupt || (interruptsAvailable.get() == 0))) {
                        try {
                            while (!hasSync && keepRunning) {
                                Thread.sleep(DataTransmitConstants.DEFAULT_CYCLE_TIME);

                                if (!keepRunning) {
                                    continue OUTER_LOOP;
                                }

                                if (hasSync && onSyncRestartMessage) {
                                    playbackData.resetAudio();
                                }
                            }

                            byte[] nextPayload = new byte[DacSessionConstants.SINGLE_PAYLOAD_SIZE];

                            MessagePlaybackStatusNotification playbackStatus = playbackData
                                    .get(nextPayload);
                            if (playbackStatus != null) {
                                logger.debug("Posting playback status update: "
                                        + playbackStatus.toString());
                                eventBus.post(playbackStatus);
                            }

                            RtpPacketIn rtpPacket = buildRtpPacket(
                                    previousPacket, nextPayload);

                            sendPacket(rtpPacket);

                            previousPacket = rtpPacket;

                            Thread.sleep(nextCycleTime);
                        } catch (InterruptedException e) {
                            logger.error("Thread sleep interrupted.", e);
                        } catch (Throwable t) {
                            logger.error(
                                    "Uncaught exception thrown from message playback loop.",
                                    t);
                        }
                    }

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
    public void shutdown() {
        keepRunning = false;
    }

    private RtpPacketIn buildRtpPacket(final RtpPacketIn previousPacket,
            final byte[] nextPayload) {
        RtpPacketInFactory factory = RtpPacketInFactory.getInstance();
        if (previousPacket != null) {
            factory.fromPacket(previousPacket, false)
                    .setPreviousPayload(previousPacket.getCurrentPayload())
                    .setCurrentPayload(nextPayload)
                    .incrementSequenceNum(
                            DataTransmitConstants.SEQUENCE_INCREMENT)
                    .incrementTimestamp(
                            DataTransmitConstants.TIMESTAMP_INCREMENT)
                    .setTransmitters(transmitters);
        } else {
            factory = factory.setCurrentPayload(nextPayload)
                    .setSequenceNumber(0).setTimestamp(0)
                    .setTransmitters(transmitters);
        }

        return factory.create();
    }

    private void sendPacket(final RtpPacketIn packet) {
        try {
            byte[] rawPacket = packet.encode();
            DatagramPacket finalizedPacket = buildPacket(rawPacket);
            socket.send(finalizedPacket);
        } catch (IOException e) {
            logger.error("Error sending RTP packet to DAC.", e);
        }
    }

    private DatagramPacket buildPacket(byte[] buf) {
        return new DatagramPacket(buf, buf.length, address, port);
    }

    @Override
    @Subscribe
    public void receivedDacStatus(DacStatusUpdateEvent e) {
        int differenceFromWatermark = DataTransmitConstants.WATERMARK_PACKETS_IN_BUFFER
                - e.getStatus().getBufferSize();

        if (differenceFromWatermark <= 0) {
            nextCycleTime = DataTransmitConstants.DEFAULT_CYCLE_TIME;
        } else {
            int packetsToSendUntilNextStatus = Math
                    .abs(differenceFromWatermark) + 5;
            nextCycleTime = 100L / packetsToSendUntilNextStatus;
            // logger.debug("Speeding up cycle time to: " + nextCycleTime);
        }
    }

    @Override
    @Subscribe
    public void handleInterruptMessage(InterruptMessageReceivedEvent e) {
        interruptsAvailable.incrementAndGet();
        logger.info("Received new interrupt: " + e.getPlaylist().toString());
    }

    @Subscribe
    public void lostDacSync(LostSyncEvent e) {
        logger.error("Application has lost sync with the DAC. Terminating data transmission.");
        hasSync = false;
    }

    @Subscribe
    public void regainDacSync(RegainSyncEvent e) {
        if (e.getDownTime() >= DataTransmitConstants.SYNC_DOWNTIME_RESTART_THRESHOLD) {
            logger.info("Application has re-gained sync with the DAC. Restarting transmission from beginning of current message.");
            onSyncRestartMessage = true;
        } else {
            logger.info("Application has re-gained sync with the DAC. Resuming transmission.");
            onSyncRestartMessage = false;
        }
        hasSync = true;
    }

    @Subscribe
    public void changeTransmitters(ChangeTransmitters changeEvent) {
        transmitters = Ints.asList(changeEvent.getTransmitters());
    }
}