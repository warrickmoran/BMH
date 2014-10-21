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

import java.util.Collection;
import java.util.Collections;

/**
 * Data structure for storing audio packets received from a broadcast client.
 * Stores the audio data and which output channels should broadcast this audio.
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

public class AudioPacket {

    private final byte[] audioData;

    private final Collection<Integer> outputChannels;

    /**
     * Constructor.
     * 
     * @param audioData
     *            The audio packet to broadcast.
     * @param outputChannels
     *            Which output channels to use when broadcasting this data.
     */
    public AudioPacket(byte[] audioData, Collection<Integer> outputChannels) {
        this.audioData = audioData;
        this.outputChannels = Collections
                .unmodifiableCollection(outputChannels);
    }

    public byte[] getAudioData() {
        return audioData;
    }

    public Collection<Integer> getOutputChannels() {
        return outputChannels;
    }
}
