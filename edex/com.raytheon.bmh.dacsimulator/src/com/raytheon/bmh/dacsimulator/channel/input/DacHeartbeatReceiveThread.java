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
package com.raytheon.bmh.dacsimulator.channel.input;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.raytheon.bmh.dacsimulator.events.SyncLostEvent;
import com.raytheon.bmh.dacsimulator.events.SyncObtainedEvent;

/**
 * Thread responsible for listening to sync or heartbeat messages from the
 * DacTransmit application instance. No other activity can happen on a channel
 * until the initial sync is received.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 03, 2014  #3688     dgilling     Initial creation
 * Nov 10, 2014  #3762     bsteffen     Allow timeout when another host is trying to connect.
 * 
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

public class DacHeartbeatReceiveThread extends Thread {

    /**
     * A sync with the DAC lasts a total of 5 seconds beyond the last received
     * valid sync or heartbeat message. Hence, we will set the timeout on our
     * receive socket to 5 seconds and if no valid sync is received by then, we
     * invalidate the sync.
     */
    private static final int DEFAULT_SOCKET_TIMEOUT = 5000; // in ms

    private static final int RECEIVE_BUFFER_SIZE = 256;

    private static final String INITIAL_SYNC_MSG = "01000";

    private static final String HEARTBEAT_SYNC_MSG = "00000";

    private static final byte[] REJECT_SYNC_MSG = "X----"
            .getBytes(StandardCharsets.US_ASCII);

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final DatagramSocket listenSocket;

    private boolean hasSync;

    private InetAddress syncHost;

    private int syncPort;

    private final EventBus eventBus;

    private long lastReceived;

    private volatile boolean keepRunning;

    private int timeoutPeriod;

    /**
     * Constructor.
     * 
     * @param port
     *            Port to listen on for sync messages.
     * @param channelNumber
     *            Channel number this thread is a part of.
     * @param eventBus
     *            EventBus instance for letting other threads know when a sync
     *            has been gained or lost.
     * @throws SocketException
     *             If the socket could not be opened, or the socket could not
     *             bind to a local port.
     */
    public DacHeartbeatReceiveThread(int port, int channelNumber,
            EventBus eventBus) throws SocketException {
        super("DacHeartbeatReceiveThread-Channel" + channelNumber);
        this.listenSocket = new DatagramSocket(port);
        this.hasSync = false;
        this.syncHost = null;
        this.syncPort = -Integer.MAX_VALUE;
        this.eventBus = eventBus;
        this.lastReceived = 0L;
        this.keepRunning = true;
        this.timeoutPeriod = DEFAULT_SOCKET_TIMEOUT;
    }

    @Override
    public void run() {
        while (keepRunning) {
            try {
                try {
                    DatagramPacket packet = new DatagramPacket(
                            new byte[RECEIVE_BUFFER_SIZE], RECEIVE_BUFFER_SIZE);
                    listenSocket.setSoTimeout(timeoutPeriod);
                    listenSocket.receive(packet);

                    String receivedMsg = new String(packet.getData(), 0,
                            packet.getLength(), StandardCharsets.US_ASCII);
                    InetAddress packetHost = packet.getAddress();

                    if (hasSync) {
                        if (syncHost.equals(packetHost)) {
                            if ((INITIAL_SYNC_MSG.equals(receivedMsg))
                                    || (HEARTBEAT_SYNC_MSG.equals(receivedMsg))) {
                                lastReceived = System.currentTimeMillis();
                                timeoutPeriod = DEFAULT_SOCKET_TIMEOUT;
                            } else {
                                logger.warn(
                                        "Received malformed sync packet from {}. Contents: {}",
                                        syncHost, receivedMsg);
                                checkTimeout();
                            }
                        } else if (checkTimeout()) {
                            attemptInitialSync(receivedMsg, packet);
                        } else {
                            logger.warn(
                                    "Rejecting attempt to sync from host {} because this channel is already synced with {}.",
                                    packetHost, syncHost);
                            DatagramPacket rejectSync = new DatagramPacket(
                                    REJECT_SYNC_MSG, REJECT_SYNC_MSG.length,
                                    packetHost, packet.getPort());
                            listenSocket.send(rejectSync);
                        }
                    } else {
                        attemptInitialSync(receivedMsg, packet);
                    }
                } catch (SocketTimeoutException e) {
                    if (hasSync) {
                        syncLost();
                    }
                } catch (IOException e) {
                    logger.error(
                            "IOException thrown while waiting to receive heartbeat.",
                            e);
                }
            } catch (Throwable t) {
                logger.error(
                        "Unhandled exception thrown by DacHeartbeatReceiveThread.",
                        t);
            }
        }
    }

    /**
     * Determine the remaining {@link #timeoutPeriod} if any.
     * 
     * @return true if timeout has occured, false if timeout has not occured.
     */
    private boolean checkTimeout() {
        long heartbeatDeadline = lastReceived + DEFAULT_SOCKET_TIMEOUT;
        long nextTimeout = heartbeatDeadline - System.currentTimeMillis();

        if (nextTimeout > 0) {
            timeoutPeriod = (int) nextTimeout;
            return false;
        } else {
            syncLost();
            return true;
        }
    }

    /**
     * Called when a packet is recieved and hasSync=false, the packet should be
     * an initial sync packet, in which case we set up the sync, otherwise log
     * an appropriate error.
     */
    private void attemptInitialSync(String receivedMsg, DatagramPacket packet) {
        if (INITIAL_SYNC_MSG.equals(receivedMsg)) {
            hasSync = true;
            syncHost = packet.getAddress();
            syncPort = packet.getPort();
            lastReceived = System.currentTimeMillis();

            logger.info("Received initial sync from {}.", syncHost);

            eventBus.post(new SyncObtainedEvent(syncHost, syncPort));
        } else if (HEARTBEAT_SYNC_MSG.equals(receivedMsg)) {
            logger.warn("Received heartbeat packet from unsynced host {}.",
                    packet.getAddress());
        } else {
            logger.warn(
                    "Received malformed packet from unsynced host {}. Contents: {}",
                    packet.getAddress(), receivedMsg);
        }
    }

    private void syncLost() {
        logger.info("Lost sync with {}:{}.", syncHost, syncPort);

        hasSync = false;
        syncHost = null;
        syncPort = -Integer.MAX_VALUE;
        timeoutPeriod = DEFAULT_SOCKET_TIMEOUT;

        eventBus.post(new SyncLostEvent());
    }

    public void shutdown() {
        keepRunning = false;
        listenSocket.close();
    }
}
