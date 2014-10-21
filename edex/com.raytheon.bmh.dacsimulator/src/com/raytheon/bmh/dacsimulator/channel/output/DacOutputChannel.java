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
package com.raytheon.bmh.dacsimulator.channel.output;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simulates an output channel on the DAC device. This simulator does not allow
 * multiple broadcast clients to send to the same output channel, like on the
 * real DAC device. On the DAC device multiple transmissions addressed to the
 * same output channel appear to overlay the audio on top of each other, which
 * was too complicated for this simulator to perform in a timely fashion.
 * <p>
 * On the real DAC device, if no audio has been received for some period of time
 * (1 minute?), a pre-recorded maintenance message will play. This simulator
 * does not currently have that capability.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 20, 2014  #3688     dgilling     Initial creation
 * 
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

public class DacOutputChannel {

    private static final int VOICE_STATUS_SILENCE = 0;

    private static final int VOICE_STATUS_IP_AUDIO = 1;

    private static final int VOICE_STATUS_MAINTENANCE = 2;

    private static final int DEFAULT_BUFFER_SIZE = 4;

    /*
     * When a channel has not begun or has stopped transmission, we will
     * broadcast silence instead.
     * 
     * TODO??? Simulate the maintenance message.
     */
    private static final byte[] SILENCE_PAYLOAD;

    private static final byte SILENCE = (byte) 0xFF;

    private static final int CHANNEL_PAYLOAD_SIZE = 160;

    static {
        SILENCE_PAYLOAD = new byte[CHANNEL_PAYLOAD_SIZE];
        Arrays.fill(SILENCE_PAYLOAD, SILENCE);
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Queue<byte[]> packetBuffer;

    private final AtomicInteger voiceStatus;

    private final int channelNum;

    /**
     * Constructor.
     * 
     * @param channelNum
     *            Output channel number this instance corresponds to.
     */
    public DacOutputChannel(int channelNum) {
        this.packetBuffer = new ArrayDeque<>(DEFAULT_BUFFER_SIZE);
        this.voiceStatus = new AtomicInteger(0);
        this.channelNum = channelNum;
    }

    /**
     * Retrieves the current voice status for this channel.
     * 
     * @return The current voice status for this channel. 0 indicates no audio
     *         is playing. 1 indicates that is broadcasting audio received over
     *         the IP channel. 2 indicates it is playing the pre-recorded
     *         maintenance message.
     */
    public int getVoiceStatus() {
        return voiceStatus.get();
    }

    /**
     * Sends an audio packet to this output channel for "broadcast".
     * 
     * @param audio
     *            The audio packet to "broadcast" over this channel.
     */
    public void send(byte[] audio) {
        packetBuffer.offer(audio);
    }

    /**
     * Simulates a single broadcast cycle. Flushes audio for the channel and
     * returns the last audio packet "broadcast". If an audio packet was played,
     * its contents are returned.
     * <p>
     * If multiple packets were sent to this channel to be broadcast for this
     * one cycle, a warning is logged. In this case, the first audio packet into
     * this class's packet buffer is the packet returned.
     * 
     * @return The broadcasted audio packet.
     */
    public byte[] getLastBroadcastPacket() {
        byte[] retVal = packetBuffer.poll();
        if (retVal != null) {
            voiceStatus.set(VOICE_STATUS_IP_AUDIO);
        } else {
            retVal = SILENCE_PAYLOAD;
            voiceStatus.set(VOICE_STATUS_SILENCE);
        }

        if (!packetBuffer.isEmpty()) {
            logger.warn(
                    "Multiple broadcast clients are trying to send to output channel {}.",
                    channelNum);
        }
        packetBuffer.clear();

        return retVal;
    }
}
