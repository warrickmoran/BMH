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

import com.raytheon.uf.common.bmh.audio.AudioConvererterManager;
import com.raytheon.uf.common.bmh.audio.AudioConversionException;
import com.raytheon.uf.common.bmh.audio.BMHAudioFormat;
import com.raytheon.uf.common.bmh.audio.UnsupportedAudioFormatException;

/**
 * BMH Audio Regulator.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 23, 2014 3424       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class AudioRegulator {

    /*
     * The maximum amplitude that can be associated with the PCM audio samples
     * recognized and supported by the BMH components.
     */
    private static final double MAX_AMPLITUDE = 32767.0;

    /*
     * Reference: http://www.rapidtables.com/electric/decibel.htm
     * 
     * The "Amplitude ratio to dB conversion" formula.
     */
    private static final double AMPLITUDE_TO_DB_CONSTANT = 20.0;

    /*
     * The managed audio data.
     */
    private byte[] originalPCMData;

    /*
     * The decibel value associated with the loudest audio sample in the managed
     * audio data.
     */
    private double signalMax;

    /**
     * Constructor
     * 
     * @param ulawData
     *            the audio data that will be regulated - in ulaw format
     * @throws UnsupportedAudioFormatException
     *             if ulaw to pcm conversion fails
     * @throws AudioConversionException
     *             if ulaw to pcm conversion fails
     */
    public AudioRegulator(final byte[] ulawData)
            throws UnsupportedAudioFormatException, AudioConversionException {
        this.originalPCMData = AudioConvererterManager
                .getInstance()
                .convertAudio(ulawData, BMHAudioFormat.ULAW, BMHAudioFormat.PCM);
        this.calculateMaximumSignal();
    }

    /**
     * Adjusts the regulated audio data according to the specified volume
     * adjustment amount
     * 
     * @param volumeAdjustment
     *            the specified volume adjustment amount ( < 1.0 will decrease
     *            the audio level; > 1.0 will increase the audio level).
     * @return the adjusted audio data in ulaw format
     * @throws AudioConversionException
     *             when the final adjusted pcm to ulaw conversion fails
     * @throws UnsupportedAudioFormatException
     *             when the final adjusted pcm to ulaw conversion fails
     * @throws AudioOverflowException
     *             if the requested volume adjustment would generate invalid
     *             audio samples
     */
    public byte[] regulateAudioVolume(double volumeAdjustment)
            throws UnsupportedAudioFormatException, AudioConversionException,
            AudioOverflowException {
        byte[] adjustedPCMData = new byte[this.originalPCMData.length];

        for (int i = 0; i < adjustedPCMData.length; i += 2) {
            short audioSample = (short) (((this.originalPCMData[i + 1] & 0xff) << 8) | (this.originalPCMData[i] & 0xff));

            audioSample = (short) (audioSample * volumeAdjustment);
            if (Math.abs(audioSample) > MAX_AMPLITUDE) {
                throw new AudioOverflowException(volumeAdjustment,
                        Math.abs(audioSample));
            }

            adjustedPCMData[i] = (byte) audioSample;
            adjustedPCMData[i + 1] = (byte) (audioSample >> 8);
        }

        return AudioConvererterManager.getInstance().convertAudio(
                adjustedPCMData, BMHAudioFormat.PCM, BMHAudioFormat.ULAW);
    }

    /**
     * Returns the maximum amplitude associated with the audio in decibels (dB)
     * 
     * @return the maximum amplitude associated with the audio in decibels (dB)
     */
    public double getSignalMax() {
        return this.signalMax;
    }

    /**
     * Determines the maximum amplitude to calculate the max decibels associated
     * with the managed audio data.
     */
    private void calculateMaximumSignal() {
        double runningMaxAmplitude = 0.0;

        for (int i = 0; i < this.originalPCMData.length; i += 2) {
            short amplitude = (short) (((this.originalPCMData[i + 1] & 0xff) << 8) | (this.originalPCMData[i] & 0xff));
            amplitude = (short) Math.abs(amplitude);

            if (amplitude > runningMaxAmplitude) {
                runningMaxAmplitude = amplitude;
            }
        }

        double amplitudeRatio = runningMaxAmplitude / MAX_AMPLITUDE;
        this.signalMax = AMPLITUDE_TO_DB_CONSTANT * Math.log10(amplitudeRatio);
    }
}