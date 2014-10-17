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
package com.raytheon.bmh.dacsimulator.channel;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.raytheon.bmh.dacsimulator.events.SyncLostEvent;
import com.raytheon.bmh.dacsimulator.events.SyncObtainedEvent;

/**
 * This thread is responsible for sending heartbeat/status messages back to the
 * connected transmission client for this channel. Provides this status by
 * sending a string in the following format:
 * 
 * <pre>
 * 0Voltage1,Voltage2,CurrentBuffer,LEVEL1,LEVEL2,LEVEL3,LEVEL4,ABCD,Recoverable_Packet_Error,Unrecoverable_Packet_Error
 * </pre>
 * 
 * Where:
 * <ul>
 * <li>Voltage1: is the current voltage reading of the primary power supply.</li>
 * <li>Voltage2: is the current voltage reading of the secondary power supply.</li>
 * <li>CurrentBuffer: is the number of packets stored in this channel's
 * JitterBuffer.</li>
 * <li>LEVEL1-LEVEL4: are the the output gain levels of the 4 radio channels.</li>
 * <li>ABCD: are the audio detection values on radio 1 to 4. A corresponds to
 * radio 1, B to radio 2, C to radio 3, and D to radio 4. When the value is 0,
 * it means no audio is playing. When the value is 1, the radio is broadcasting
 * IP audio. When the value is 2, it means the maintenance message is playing.</li>
 * <li>Recoverable_Packet_Error: is the number of out of order packets that were
 * able to be reordered to the correct spot since the last status message.</li>
 * <li>Unrecoverable_Packet_Error: is the number of out of order packets that
 * were not able to be reordered to the correct spot since the last status
 * message.</li>
 * </ul>
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 15, 2014  #3688     dgilling     Initial creation
 * 
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

public class DacHeartbeatSendThread extends Thread {

    private static final int VOICE_STATUS_SILENCE = 0;

    private static final int VOICE_STATUS_IP_AUDIO = 1;

    private static final int VOICE_STATUS_MAINTENANCE = 2;

    private static final String VOLTAGE_LEVEL = "12.0";

    private static final String OUTPUT_GAIN = "0.0";

    private static final char DELIMITER = ',';

    private static final long THREAD_CYCLE_TIME = 100; // in ms

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final DatagramSocket socket;

    private final AtomicBoolean hasSync;

    private final AtomicReference<InetAddress> syncHost;

    private final AtomicInteger syncPort;

    private final EventBus eventBus;

    private final JitterBuffer myBuffer;

    private final List<JitterBuffer> allBuffers;

    /**
     * Constructor.
     * 
     * @param channelNumber
     *            Channel number this thread is simulating for.
     * @param myBuffer
     *            This channel's {@code JitterBuffer}.
     * @param allBuffers
     *            The {@code JitterBuffer}s for all channels.
     * @param eventBus
     *            {@code EventBus} instance so this thread knows when sync is
     *            gained or lost.
     * @throws SocketException
     *             If the socket could not be opened, or the socket could not
     *             bind to a local port.
     */
    public DacHeartbeatSendThread(int channelNumber, JitterBuffer myBuffer,
            List<JitterBuffer> allBuffers, EventBus eventBus)
            throws SocketException {
        super("DacHeartbeatSendThread-Channel" + channelNumber);
        this.socket = new DatagramSocket();
        this.hasSync = new AtomicBoolean(false);
        this.syncHost = new AtomicReference<>();
        this.syncPort = new AtomicInteger();
        this.myBuffer = myBuffer;
        this.allBuffers = allBuffers;
        this.eventBus = eventBus;
        this.eventBus.register(this);
    }

    @Override
    public void run() {
        while (hasSync.get()) {
            try {
                String heartbeatMsg = buildHeartbeatMessage();
                byte[] msgBytes = heartbeatMsg
                        .getBytes(StandardCharsets.US_ASCII);
                DatagramPacket packet = new DatagramPacket(msgBytes,
                        msgBytes.length, syncHost.get(), syncPort.get());
                try {
                    socket.send(packet);
                } catch (IOException e) {
                    logger.error("Unable to send heartbeat packet to host "
                            + syncHost.get(), e);
                }

                try {
                    Thread.sleep(THREAD_CYCLE_TIME);
                } catch (InterruptedException e) {
                    logger.warn(
                            "Something interrupted a sleeping DacHeartbeatSendThread.",
                            e);
                }
            } catch (Throwable t) {
                logger.error(
                        "Unhandled exception thrown by DacHeartbeatSendThread.",
                        t);
            }
        }
    }

    private String buildHeartbeatMessage() {
        /*
         * Because we're not a real DAC, we have to fake some of the values just
         * so we send a properly-formatted message. For now, we'll fake the PSU
         * voltages, the output gain level for each channel, and the packet
         * error values.
         * 
         * We also only have 2 states for voice status: broadcasting or silence.
         * If we had a way to track how long we've been broadcasting silence, we
         * would could also support the maintenance message voice status.
         * 
         * FIXME: Track packet ordering so we can send back packet error
         * numbers.
         * 
         * TODO: Support maintenance message??
         */
        StringBuilder statusMsg = new StringBuilder();
        statusMsg.append('0');
        statusMsg.append(VOLTAGE_LEVEL).append(DELIMITER).append(VOLTAGE_LEVEL)
                .append(DELIMITER);
        statusMsg.append(myBuffer.size()).append(DELIMITER);
        statusMsg.append(OUTPUT_GAIN).append(DELIMITER).append(OUTPUT_GAIN)
                .append(DELIMITER).append(OUTPUT_GAIN).append(DELIMITER)
                .append(OUTPUT_GAIN).append(DELIMITER);

        /*
         * We're faking how to get voice status at the moment. Because the DAC
         * has the ability to send 1 data stream to multiple channels, directly
         * mapping each jitter buffer to a channel's voice status isn't always
         * correct.
         * 
         * FIXME: support a multiple output addressing feature of DAC.
         */
        for (JitterBuffer buffer : allBuffers) {
            int voiceStatus = buffer.isReadyForBroadcast() ? VOICE_STATUS_IP_AUDIO
                    : VOICE_STATUS_SILENCE;
            statusMsg.append(voiceStatus);
        }
        statusMsg.append(DELIMITER);

        statusMsg.append(0).append(DELIMITER);
        statusMsg.append(0);

        String retVal = statusMsg.toString();
        logger.debug("Sending back status message: {}.", retVal);
        return retVal;
    }

    private void setSyncPartner(InetAddress host, int port) {
        hasSync.set(true);
        syncHost.set(host);
        syncPort.set(port);
    }

    private void resetSync() {
        hasSync.set(false);
        syncHost.set(null);
        syncPort.set(0);
    }

    @Subscribe
    public void handleSyncObtained(SyncObtainedEvent event) {
        setSyncPartner(event.getSyncHost(), event.getSyncPort());
        start();
    }

    @Subscribe
    public void handleSyncLost(SyncLostEvent event) {
        resetSync();
    }

    public void shutdown() {
        eventBus.unregister(this);
        resetSync();
        socket.close();
    }
}
