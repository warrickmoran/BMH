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
package com.raytheon.bmh.dacsimulator;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raytheon.bmh.dacsimulator.channel.output.DacSimulatedBroadcast;

/**
 * This thread simulates the rebroadcast feature of the DAC device. It combines
 * the output from the DAC's channels into a single audio stream that is sent to
 * a configured host/port.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 03, 2014  #3688     dgilling     Initial creation
 * Oct 21, 2014  #3688     dgilling     Support packet addressing.
 * 
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

public class DacRebroadcastThread extends Thread {

    private static final long DEFAULT_CYCLE_TIME = 20; // in ms

    private static final int CHANNEL_PAYLOAD_SIZE = 160;

    /**
     * Corresponds to Version: 2, Padding: 0, Extension: 0, CSRC count: 0
     */
    private static final byte FLAGS = (byte) 0x80;

    /**
     * Corresponds to Marker: 0, Payload type: 121
     */
    private static final byte MARKER_PAYLOAD_TYPE = (byte) 0x79;

    private static final int PACKET_HEADER_SIZE = 12; // bytes

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final DatagramSocket socket;

    private final InetSocketAddress destination;

    private final DacSimulatedBroadcast broadcaster;

    private final int numChannels;

    /**
     * RTP spec's synchronization source identifier. Provides a means to
     * uniquely identify sessions.
     */
    private final int ssrc;

    /*
     * Sequencing number for packets sent in this stream. In the protocol
     * specification, this is actually an unsigned short.
     */
    private short sequenceNumber;

    /*
     * The timestamp for the current packet from the start (measured in ms) . In
     * the protocol specification, this is actually an unsigned 32-bit int.
     */
    private int timestamp;

    /**
     * Constructor.
     * 
     * @param destAddress
     *            Host name of IP address to broadcast the audio streams to.
     * @param port
     *            Port to send the audio streams to.
     * @param broadcaster
     *            {@code DacSimulatedBroadcast} instance needed for getting the
     *            audio packets being broadcast.
     * @param numChannels
     *            Number of output channels.
     * @throws IOException
     *             If the socket could not be opened, or the socket could not
     *             bind to a local port.
     */
    public DacRebroadcastThread(InetAddress destAddress, int port,
            DacSimulatedBroadcast broadcaster, int numChannels)
            throws IOException {
        super("DacRebroadcastThread");
        this.destination = new InetSocketAddress(destAddress, port);
        this.socket = new DatagramSocket();
        this.broadcaster = broadcaster;
        this.numChannels = numChannels;

        this.ssrc = new Random().nextInt();
        this.sequenceNumber = 0;
        this.timestamp = 0;
    }

    @Override
    public void run() {
        while (true) {
            try {
                long nextSleepTime = DEFAULT_CYCLE_TIME;

                try {
                    long t0 = System.currentTimeMillis();

                    byte[] payload = buildPacket();
                    DatagramPacket packet = new DatagramPacket(payload,
                            payload.length, destination);
                    socket.send(packet);

                    long t1 = System.currentTimeMillis();

                    nextSleepTime = DEFAULT_CYCLE_TIME - (t1 - t0);
                    if (nextSleepTime < 0) {
                        logger.warn("Building rebroadcast packet is taking too long. Audio stream playback may be affected.");
                        nextSleepTime = 0;
                    }
                } catch (IOException e) {
                    String msg = "Error sending packet to "
                            + destination.toString() + ".";
                    logger.error(msg, e);
                }

                try {
                    Thread.sleep(nextSleepTime);
                } catch (InterruptedException e) {
                    logger.error("Something interrupted our sleep.", e);
                }
            } catch (Exception e) {
                logger.error("Unhandled exception in DacRebroadcastThread.", e);
            }
        }
    }

    private byte[] buildPacket() {
        int packetSize = PACKET_HEADER_SIZE
                + (numChannels * CHANNEL_PAYLOAD_SIZE);
        ByteBuffer packet = ByteBuffer.allocate(packetSize);

        packet.put(FLAGS);
        packet.put(MARKER_PAYLOAD_TYPE);
        packet.putShort(sequenceNumber);
        packet.putInt(timestamp);
        packet.putInt(ssrc);

        sequenceNumber++;
        timestamp += 20;

        Collection<byte[]> audioData = broadcaster.broadcast();
        for (byte[] audioChannel : audioData) {
            packet.put(audioChannel);
        }

        return packet.array();
    }
}
