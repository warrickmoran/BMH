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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.primitives.Ints;
import com.raytheon.bmh.dacsimulator.events.SyncLostEvent;
import com.raytheon.bmh.dacsimulator.events.SyncObtainedEvent;
import com.raytheon.uf.common.bmh.audio.AudioPacketLogger;

/**
 * Receives the audio broadcast data stream from the connected transmission
 * client.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 03, 2014  #3688     dgilling     Initial creation
 * Oct 21, 2014  #3688     dgilling     Support RTP packet's addressing bits.
 * Oct 24, 2014  #3688     dgilling     Fix ability to disconnect/reconnect.
 * Oct 29, 2014  #3774     bsteffen     Log Packets
 * 
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

public class DacReceiveDataThread extends Thread {

    private static final int DEFAULT_TIMEOUT = 1000;

    /**
     * Total size (in bytes) of the specialized RTP packets sent to the DAC.
     * Value is 340 bytes (12 bytes for the RTP header, 8 bytes for the
     * extension header, and 320 bytes for the 2 160 byte payloads).
     */
    public static final int RTP_PACKET_SIZE = 340;

    /**
     * Offset into the RTP packet structure where the audio data is located.
     */
    public static final int AUDIO_DATA_OFFSET = 20;

    /**
     * Offset into the RTP packet structure where the bytes for output channel
     * addressing are located.
     */
    public static final int ADDRESSING_BYTES_OFFSET = 16;

    /**
     * Number of bytes used to store addressing information. Right now only the
     * last 4 bits of the last byte can be set, but we'll extract the whole 4
     * bytes for future support.
     */
    private static final int ADDRESSING_BYTES_LENGTH = 4;

    /**
     * The size (in bytes) of one of the payload fields that is part of the
     * RTP-like packets sent to the DAC.
     */
    public static final int SINGLE_PAYLOAD_SIZE = 160;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final JitterBuffer buffer;

    private final DatagramSocket socket;

    private final AtomicBoolean hasSync;

    private final AtomicReference<InetAddress> syncHost;

    private final EventBus eventBus;

    private volatile boolean keepRunning;

    private Object wakeMonitor;

    /**
     * Constructor.
     * 
     * @param buffer
     *            {@code JitterBuffer} for storing received data packets.
     * @param listenPort
     *            Port to listen for transmission on.
     * @param channelNumber
     *            Channel number we're simulating.
     * @param eventBus
     *            {@code EventBus} instance to listen for sync lost/gained
     *            events.
     * @throws SocketException
     *             If the socket could not be opened, or the socket could not
     *             bind to a local port.
     */
    public DacReceiveDataThread(JitterBuffer buffer, int listenPort,
            int channelNumber, EventBus eventBus) throws SocketException {
        super("DacReceiveDataThread-Channel" + channelNumber);
        this.buffer = buffer;
        this.socket = new DatagramSocket(listenPort);
        this.socket.setSoTimeout(DEFAULT_TIMEOUT);
        this.hasSync = new AtomicBoolean(false);
        this.syncHost = new AtomicReference<>();
        this.eventBus = eventBus;
        this.eventBus.register(this);
        this.keepRunning = true;
        this.wakeMonitor = new Object();
    }

    @Override
    public void run() {
        byte[] receiveBuffer = new byte[RTP_PACKET_SIZE];

        while (keepRunning) {

            try (AudioPacketLogger packetLog = new AudioPacketLogger("receive",
                    logger, 300)) {
                synchronized (wakeMonitor) {
                    wakeMonitor.wait();
                }
                while (hasSync.get()) {
                    try {
                        try {
                            DatagramPacket packet = new DatagramPacket(
                                    receiveBuffer, RTP_PACKET_SIZE);
                            socket.receive(packet);

                            if (syncHost.get().equals(packet.getAddress())) {
                                AudioPacket nextAudioPacket = extractAudioPacket(packet);
                                if (nextAudioPacket != null) {
                                    packetLog.packetProcessed();
                                    buffer.add(nextAudioPacket);
                                }
                            }
                        } catch (IOException e) {
                            logger.error(
                                    "IOException thrown while receiving audio packet from "
                                            + syncHost.get(), e);
                        }
                    } catch (Throwable t) {
                        logger.error(
                                "Unhandled exception thrown by DacReceiveDataThread.",
                                t);
                    }
                }
            } catch (Throwable t) {
                logger.error(
                        "Unhandled exception thrown by DacReceiveDataThread.",
                        t);
            }
        }
    }

    private AudioPacket extractAudioPacket(DatagramPacket packet) {
        /*
         * TODO: extract sequencing values when we upgrade JitterBuffer to
         * support packet reordering
         */

        if (packet.getLength() >= RTP_PACKET_SIZE) {
            byte[] payload = packet.getData();

            int addressValues = Ints.fromByteArray(Arrays.copyOfRange(payload,
                    ADDRESSING_BYTES_OFFSET, ADDRESSING_BYTES_OFFSET
                            + ADDRESSING_BYTES_LENGTH));

            Collection<Integer> outputChannels = new HashSet<>();
            int channelNum = 1;
            while (addressValues != 0) {
                if ((addressValues & 1) == 1) {
                    outputChannels.add(channelNum);
                }
                addressValues = addressValues >>> 1;
                channelNum++;
            }

            byte[] audioData = Arrays.copyOfRange(payload, AUDIO_DATA_OFFSET
                    + SINGLE_PAYLOAD_SIZE, AUDIO_DATA_OFFSET
                    + SINGLE_PAYLOAD_SIZE + SINGLE_PAYLOAD_SIZE);

            return new AudioPacket(audioData, outputChannels);
        }

        return null;
    }

    private void setSyncPartner(InetAddress host) {
        hasSync.set(true);
        syncHost.set(host);
    }

    private void resetSync() {
        hasSync.set(false);
        syncHost.set(null);
    }

    @Subscribe
    public void handleSyncObtained(SyncObtainedEvent event) {
        setSyncPartner(event.getSyncHost());
        wakeThread();
    }

    @Subscribe
    public void handleSyncLost(SyncLostEvent event) {
        resetSync();
    }

    private void wakeThread() {
        synchronized (wakeMonitor) {
            wakeMonitor.notifyAll();
        }
    }

    public void shutdown() {
        keepRunning = false;
        wakeThread();
        eventBus.unregister(this);
        resetSync();
        socket.close();
    }
}
