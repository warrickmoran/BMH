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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.raytheon.bmh.dacsimulator.channel.input.AudioPacket;
import com.raytheon.bmh.dacsimulator.channel.input.JitterBuffer;

/**
 * Simulates the DAC's broadcast process by reading the next packet off all the
 * input channels' {@code JitterBuffer}s and mapping them to an output channel
 * and then the data for each channel is transmitted to the radio. Since the
 * simulated DAC has no radio, the output channels hold the packet until the
 * next rebroadcast cycle hits.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 21, 2014  #3688     dgilling     Initial creation
 * 
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

public class DacSimulatedBroadcast {

    private final List<DacOutputChannel> outputChannels;

    private final Collection<JitterBuffer> inputBuffers;

    /**
     * Constructor.
     * 
     * @param numChannels
     *            Number of output channels to simulate.
     * @param buffers
     *            The {@code JitterBuffer}s from each of the input channels.
     */
    public DacSimulatedBroadcast(int numChannels,
            Collection<JitterBuffer> buffers) {
        this.inputBuffers = buffers;
        this.outputChannels = new ArrayList<>(numChannels);
        for (int i = 1; i <= numChannels; i++) {
            this.outputChannels.add(new DacOutputChannel(i));
        }
    }

    /**
     * Returns the voice status for all the available output channels in the
     * string format required by the heartbeat message.
     * 
     * @return The voice status string which indicates the voice status for all
     *         the output channels.
     */
    public CharSequence getVoiceStatus() {
        StringBuilder retVal = new StringBuilder(outputChannels.size());
        for (DacOutputChannel channel : outputChannels) {
            retVal.append(channel.getVoiceStatus());
        }

        return retVal;
    }

    /**
     * Simulates a single broadcast cycle on all the available output channels.
     * 
     * @return The bytes broadcast over all the channels.
     */
    public Collection<byte[]> broadcast() {
        for (JitterBuffer buffer : inputBuffers) {
            if (buffer.isReadyForBroadcast()) {
                AudioPacket packet = buffer.get();
                byte[] audio = packet.getAudioData();
                for (Integer destination : packet.getOutputChannels()) {
                    outputChannels.get(destination - 1).send(audio);
                }
            }
        }

        Collection<byte[]> outputAudio = new ArrayList<>(outputChannels.size());
        for (DacOutputChannel channel : outputChannels) {
            outputAudio.add(channel.getLastBroadcastPacket());
        }

        return outputAudio;
    }
}
