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

import org.apache.commons.lang.math.DoubleRange;
import org.apache.commons.lang.math.Range;

import com.raytheon.uf.common.bmh.audio.AudioConversionException;
import com.raytheon.uf.common.bmh.audio.BMHAudioConstants;
import com.raytheon.uf.common.bmh.audio.UnsupportedAudioFormatException;
import com.raytheon.uf.common.bmh.audio.impl.algorithm.PCMToUlawAlgorithm;
import com.raytheon.uf.common.bmh.audio.impl.algorithm.UlawToPCMAlgorithm;

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
 * Feb 09, 2015 4091       bkowal      Use {@link EdexAudioConverterManager}.
 * Apr 09, 2015 4365       bkowal      Eliminated unnecessary byte[] creation. Reuse arrays
 *                                     during conversions and regulation.
 * Jun 29, 2015 4602       bkowal      Support both audio attenuation and amplification.
 * Jul 01, 2015 4602       bkowal      Do not attenuate/amplify extremely quiet audio.
 * Jul 13, 2015 4636       bkowal      Do not alter extremely quiet audio.
 * Jul 14, 2015 4636       rjpeter     Check entire stream for max.
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class AudioRegulator {
    private static final double DB_SILENCE_LIMIT = -40.;

    private long duration;

    /**
     * Adjusts the regulated audio data according to the specified volume
     * adjustment amount
     * 
     * @param sample
     *            the audio byte data to regulate
     * @param volumeAdjustment
     *            the specified volume adjustment amount ( < 1.0 will decrease
     *            the audio level; > 1.0 will increase the audio level).
     * @param offset
     *            the offset within the sample byte array to start the
     *            regulation
     * @param length
     *            the number of bytes in the sample array to regulate after the
     *            offset
     * @return the adjusted audio data
     * @throws AudioConversionException
     *             when the final adjusted pcm to ulaw conversion fails
     * @throws UnsupportedAudioFormatException
     *             when the final adjusted pcm to ulaw conversion fails
     * @throws AudioOverflowException
     *             if the requested volume adjustment would generate invalid
     *             audio samples
     */
    private void regulateAudioVolume(byte[] sample,
            final double volumeAdjustment, int offset, int length)
            throws UnsupportedAudioFormatException, AudioConversionException,
            AudioOverflowException {
        for (int i = offset; i < (offset + length); i += 2) {
            short audioSample = (short) (((sample[i + 1] & 0xff) << 8) | (sample[i] & 0xff));
            double calculation = audioSample * volumeAdjustment;
            if (Math.abs(calculation) > BMHAudioConstants.MAX_AMPLITUDE) {
                audioSample = (short) BMHAudioConstants.MAX_AMPLITUDE;
            } else {
                audioSample = (short) calculation;
            }

            sample[i] = (byte) audioSample;
            sample[i + 1] = (byte) (audioSample >> 8);
        }
    }

    public byte[] regulateAudioVolume(final byte[] ulawData,
            final double dbTarget, final int sampleSize)
            throws AudioOverflowException, UnsupportedAudioFormatException,
            AudioConversionException {
        long start = System.currentTimeMillis();

        byte[] pcmData = new byte[sampleSize * 2];
        int length = sampleSize;
        double maxValue = -Double.MAX_VALUE;

        for (int i = 0; i < ulawData.length; i += sampleSize) {
            if (i + length > ulawData.length) {
                length = ulawData.length - i;
            }

            UlawToPCMAlgorithm.convert(ulawData, i, length, pcmData);
            Range decRange = this.calculateBoundarySignals(pcmData, 0,
                    length * 2);
            if (decRange.getMaximumDouble() != Double.NEGATIVE_INFINITY) {
                maxValue = Math.max(maxValue, decRange.getMaximumDouble());
            }
        }

        double difference = dbTarget - maxValue;
        double adjustmentRate = Math.pow(
                BMHAudioConstants.DB_ALTERATION_CONSTANT,
                (difference / BMHAudioConstants.AMPLITUDE_TO_DB_CONSTANT));
        length = sampleSize;
        for (int i = 0; i < ulawData.length; i += sampleSize) {
            if (i + length > ulawData.length) {
                length = ulawData.length - i;
            }

            UlawToPCMAlgorithm.convert(ulawData, i, length, pcmData);
            this.adjustAudioSamplePCM(pcmData, adjustmentRate, 0, length * 2);
            PCMToUlawAlgorithm.convert(pcmData, 0, length * 2, ulawData, i);
        }

        this.duration = System.currentTimeMillis() - start;
        return ulawData;
    }

    private void adjustAudioSamplePCM(final byte[] sample,
            final double adjustmentRate, int offset, int length)
            throws UnsupportedAudioFormatException, AudioConversionException,
            AudioOverflowException {
        Range decibelRange = this.calculateBoundarySignals(sample, offset,
                length);
        if ((decibelRange.getMinimumDouble() == Double.NEGATIVE_INFINITY && decibelRange
                .getMaximumDouble() == Double.NEGATIVE_INFINITY)
                || decibelRange.getMaximumDouble() <= DB_SILENCE_LIMIT) {
            return;
        }

        this.regulateAudioVolume(sample, adjustmentRate, offset, length);
    }

    /**
     * Determines the minimum and maximum amplitudes to calculate the min and
     * max decibels respectively associated with the managed audio data.
     * 
     * @param audio
     *            the managed audio data
     * @param offset
     *            determines the first index in the managed audio array that
     *            should be used when calculating the boundary signals
     * @param length
     *            the number of bytes from the offset to use when calculating
     *            the boundary signals
     * @return the calculated decibel range
     */
    private Range calculateBoundarySignals(final byte[] audio, int offset,
            int length) {
        double runningMinAmplitude = 0.0;
        double runningMaxAmplitude = 0.0;

        for (int i = offset; i < (offset + length); i += 2) {
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