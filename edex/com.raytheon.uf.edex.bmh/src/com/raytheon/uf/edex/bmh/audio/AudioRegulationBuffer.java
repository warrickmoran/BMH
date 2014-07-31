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
package com.raytheon.uf.edex.bmh.audio;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.ArrayList;

import com.raytheon.uf.common.bmh.audio.AudioConversionException;
import com.raytheon.uf.common.bmh.audio.UnsupportedAudioFormatException;

/**
 * Utilizes {@link AudioRegulator} to regulate individual samples of audio data
 * as it is buffered.
 * 
 * TODO: the current state of this class does not necessarily reflect the final
 * implementation of this class. The structure and the utility of this class
 * will most likely be altered depending on where this class is used and how
 * this class is used.
 * 
 * TODO: depending on how much control we will actually have over the buffer as
 * it is streamed out. Consider adjusting the audio using a type of sliding
 * window such that the buffered audio would be adjusted with every addition to
 * the buffer.
 * 
 * Remember, for the ULAW data used by BMH, there are 8000 samples in a second
 * and each sample is 8 bits. So, 4000 samples (4000 bytes worth of data) would
 * be required to alter 0.5 seconds worth of audio at a time.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 31, 2014 3424       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */
public class AudioRegulationBuffer {

    /*
     * The total number of bytes to store in a buffer before regulating the
     * audio.
     */
    private final int totalSampleSize;

    /*
     * A list of audio regulation rules. Allows a single instantiation of this
     * class to be used for multiple transmitters.
     */
    private final List<AudioRegulatoryParameters> audioAdjustments;

    /*
     * The number of bytes that are currently buffered.
     */
    private int currentBufferSize;

    /*
     * The buffered data.
     */
    private ByteBuffer sampleBuffer;

    /*
     * A list of instantiated classes that implement {@link
     * IAudioRegulatoryListener} that will be notified when audio samples are
     * regulated.
     */
    private List<IAudioRegulatoryListener> listeners;

    /**
     * Constructor
     * 
     * @param totalSampleSize
     *            buffer size before audio is regulated
     * @param audioAdjustments
     *            a list of audio regulation rules
     */
    public AudioRegulationBuffer(final int totalSampleSize,
            List<AudioRegulatoryParameters> audioAdjustments) {
        this.totalSampleSize = totalSampleSize;
        this.audioAdjustments = audioAdjustments;
        this.sampleBuffer = ByteBuffer.allocate(totalSampleSize);
        this.listeners = new ArrayList<>();
    }

    /**
     * Adds the specified audio data to the buffer. The audio in the buffer will
     * be regulated whenever the buffer reaches maximum capacity.
     * 
     * @param audioData
     *            the audio data to buffer
     * @throws UnsupportedAudioFormatException
     *             when audio conversion fails during the regulation process
     * @throws AudioConversionException
     *             when audio conversion fails during the regulation process
     * @throws AudioOverflowException
     *             when an invalid audio sample is generated due to the
     *             regulation process
     */
    public void bufferAudio(byte[] audioData)
            throws UnsupportedAudioFormatException, AudioConversionException,
            AudioOverflowException {
        if (this.currentBufferSize + audioData.length < this.totalSampleSize) {
            this.sampleBuffer.put(audioData);
            this.currentBufferSize += audioData.length;
            return;
        }

        int offset = 0;
        while (this.currentBufferSize + (audioData.length - offset) > this.totalSampleSize) {
            int remaining = this.totalSampleSize - this.currentBufferSize;
            this.sampleBuffer.put(audioData, offset, remaining);
            this.currentBufferSize = this.totalSampleSize;
            this.adjustAudio();
            offset += remaining;
        }
        this.sampleBuffer.put(audioData, offset, audioData.length - offset);
        this.currentBufferSize = (audioData.length - offset);
    }

    /**
     * Forces the regulation of any remaining audio in the buffer.
     * 
     * @throws UnsupportedAudioFormatException
     *             when audio conversion fails during the regulation process
     * @throws AudioConversionException
     *             when audio conversion fails during the regulation process
     * @throws AudioOverflowException
     *             when an invalid audio sample is generated due to the
     *             regulation process
     */
    public void flushBuffer() throws UnsupportedAudioFormatException,
            AudioConversionException, AudioOverflowException {
        if (this.currentBufferSize == 0) {
            return;
        }
        this.adjustAudio();
    }

    /**
     * Performs the audio regulation and notifies any registered listeners.
     * 
     * @throws UnsupportedAudioFormatException
     *             when audio conversion fails during the regulation process
     * @throws AudioConversionException
     *             when audio conversion fails during the regulation process
     * @throws AudioOverflowException
     *             when an invalid audio sample is generated due to the
     *             regulation process
     */
    private void adjustAudio() throws UnsupportedAudioFormatException,
            AudioConversionException, AudioOverflowException {
        byte[] sourceData = this.sampleBuffer.array();
        if (this.currentBufferSize < this.totalSampleSize) {
            sourceData = new byte[this.currentBufferSize];
            this.sampleBuffer.get(sourceData);
        }
        AudioRegulator audioRegulator = new AudioRegulator(sourceData);
        for (AudioRegulatoryParameters parameter : this.audioAdjustments) {
            byte[] adjustedAudio = audioRegulator.regulateAudioVolumeRange(
                    parameter.getDbMin(), parameter.getDbMax());
            for (IAudioRegulatoryListener listener : this.listeners) {
                listener.notifyAudioAdjusted(parameter.getId(), adjustedAudio);
            }
        }
        this.sampleBuffer.clear();
        this.currentBufferSize = 0;
    }

    /**
     * Returns the size of the current data buffer
     * 
     * @return the size of the current data buffer
     */
    public int getCurrentBufferSize() {
        return this.currentBufferSize;
    }

    /**
     * Registers an audio regulation listener
     * 
     * @param listener
     *            the audio regulation listener to register
     */
    public void registerListener(IAudioRegulatoryListener listener) {
        this.listeners.add(listener);
    }

    /**
     * Unregisters an audio regulation listener
     * 
     * @param listener
     *            the audio regulation listener to unregister
     */
    public void unregisterListener(IAudioRegulatoryListener listener) {
        this.listeners.remove(listener);
    }
}