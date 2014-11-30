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

import org.apache.commons.lang.math.DoubleRange;
import org.apache.commons.lang.math.Range;

import com.raytheon.uf.common.bmh.audio.AudioConvererterManager;
import com.raytheon.uf.common.bmh.audio.AudioConversionException;
import com.raytheon.uf.common.bmh.audio.BMHAudioConstants;
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
 * Aug 25, 2014 3552       bkowal      Algorithm updates to ensure that audio
 *                                     samples are not adjusted twice { attenuated the
 *                                     first time and amplified the second or vice
 *                                     verse }.
 * Sep 3, 2014  3532       bkowal      Updated the algorithm to use a target instead of
 *                                     a range.
 * Nov 24, 2014 3863       bkowal      Use {@link BMHAudioConstants}.
 * 
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class AudioRegulator {
    private long duration;

    /**
     * Adjusts the regulated audio data according to the specified volume
     * adjustment amount
     * 
     * @param volumeAdjustment
     *            the specified volume adjustment amount ( < 1.0 will decrease
     *            the audio level; > 1.0 will increase the audio level).
     * @param convertUlaw
     *            the adjusted audio data will be converted to ulaw when TRUE
     * @return the adjusted audio data
     * @throws AudioConversionException
     *             when the final adjusted pcm to ulaw conversion fails
     * @throws UnsupportedAudioFormatException
     *             when the final adjusted pcm to ulaw conversion fails
     * @throws AudioOverflowException
     *             if the requested volume adjustment would generate invalid
     *             audio samples
     */
    private byte[] regulateAudioVolume(final byte[] sample,
            final double volumeAdjustment)
            throws UnsupportedAudioFormatException, AudioConversionException,
            AudioOverflowException {
        byte[] adjustedPCMData = new byte[sample.length];

        for (int i = 0; i < adjustedPCMData.length; i += 2) {
            short audioSample = (short) (((sample[i + 1] & 0xff) << 8) | (sample[i] & 0xff));
            audioSample = (short) (audioSample * volumeAdjustment);
            if (Math.abs(audioSample) > BMHAudioConstants.MAX_AMPLITUDE) {
                throw new AudioOverflowException(volumeAdjustment,
                        Math.abs(audioSample));
            }

            adjustedPCMData[i] = (byte) audioSample;
            adjustedPCMData[i + 1] = (byte) (audioSample >> 8);
        }

        return adjustedPCMData;
    }

    public byte[] regulateAudioVolume(final byte[] ulawData,
            final double dbTarget, final int sampleSize)
            throws AudioOverflowException, UnsupportedAudioFormatException,
            AudioConversionException {
        long start = System.currentTimeMillis();
        final byte[] pcmData = AudioConvererterManager
                .getInstance()
                .convertAudio(ulawData, BMHAudioFormat.ULAW, BMHAudioFormat.PCM);
        final int scaledSampleSize = sampleSize * 2;
        ByteBuffer buffer = ByteBuffer.allocate(pcmData.length);

        final int overflowCount = pcmData.length % scaledSampleSize;
        final int numberSamples = (pcmData.length - overflowCount)
                / scaledSampleSize;
        byte[] sample = new byte[scaledSampleSize];
        for (int i = 0; i < numberSamples; i++) {
            System.arraycopy(pcmData, i * scaledSampleSize, sample, 0,
                    scaledSampleSize);
            byte[] adjustedSample = this.regulateAudioSamplePCM(sample,
                    dbTarget);
            buffer.put(adjustedSample);
        }
        if (overflowCount > 0) {
            sample = new byte[overflowCount];
            System.arraycopy(pcmData, numberSamples * scaledSampleSize, sample,
                    0, overflowCount);
            byte[] adjustedSample = this.regulateAudioSamplePCM(sample,
                    dbTarget);
            buffer.put(adjustedSample);
        }

        byte[] convertedAudio = AudioConvererterManager.getInstance()
                .convertAudio(buffer.array(), BMHAudioFormat.PCM,
                        BMHAudioFormat.ULAW);
        this.duration = System.currentTimeMillis() - start;
        return convertedAudio;
    }

    private byte[] regulateAudioSamplePCM(final byte[] sample,
            final double dbTarget) throws UnsupportedAudioFormatException,
            AudioConversionException, AudioOverflowException {
        Range decibelRange = this.calculateBoundarySignals(sample);

        if ((decibelRange.getMinimumDouble() == Double.NEGATIVE_INFINITY && decibelRange
                .getMaximumDouble() == Double.NEGATIVE_INFINITY)
                || decibelRange.getMaximumDouble() <= dbTarget) {
            return sample;
        }

        double difference = dbTarget - decibelRange.getMaximumDouble();
        double adjustmentRate = Math.pow(
                BMHAudioConstants.DB_ALTERATION_CONSTANT,
                (difference / BMHAudioConstants.AMPLITUDE_TO_DB_CONSTANT));

        return this.regulateAudioVolume(sample, adjustmentRate);
    }

    /**
     * Determines the minimum and maximum amplitudes to calculate the min and
     * max decibels respectively associated with the managed audio data.
     * 
     * @param audio
     *            the managed audio data
     * @return the calculated decibel range
     */
    private Range calculateBoundarySignals(final byte[] audio) {
        double runningMinAmplitude = 0.0;
        double runningMaxAmplitude = 0.0;

        for (int i = 0; i < audio.length; i += 2) {
            short amplitude = (short) (((audio[i + 1] & 0xff) << 8) | (audio[i] & 0xff));
            amplitude = (short) Math.abs(amplitude);

            if (i == 0) {
                runningMinAmplitude = amplitude;
                runningMaxAmplitude = amplitude;
            } else {
                /*
                 * We use the quietest audible signal to calculate the minimum
                 * amplitude rather than the lack of audio. For the lack of
                 * audio, when the amplitude is 0, the associated dB value is
                 * -infinity.
                 */
                if ((runningMinAmplitude == 0 || amplitude < runningMinAmplitude)
                        && amplitude != 0) {
                    runningMinAmplitude = amplitude;
                }
                if (amplitude > runningMaxAmplitude) {
                    runningMaxAmplitude = amplitude;
                }
            }
        }

        return new DoubleRange(this.calculateDecibels(runningMinAmplitude),
                this.calculateDecibels(runningMaxAmplitude));
    }

    /**
     * Converts the specified amplitude to decibels
     * 
     * @param amplitude
     *            the specified amplitude
     * @return the amplitude converted to decibels
     */
    private double calculateDecibels(double amplitude) {
        amplitude = Math.abs(amplitude);
        double amplitudeRatio = amplitude / BMHAudioConstants.MAX_AMPLITUDE;
        return BMHAudioConstants.AMPLITUDE_TO_DB_CONSTANT
                * Math.log10(amplitudeRatio);
    }

    /**
     * @return the duration
     */
    public long getDuration() {
        return duration;
    }
}