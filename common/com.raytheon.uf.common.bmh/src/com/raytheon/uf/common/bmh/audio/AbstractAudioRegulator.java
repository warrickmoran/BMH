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
package com.raytheon.uf.common.bmh.audio;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.Range;

import com.raytheon.uf.common.bmh.audio.impl.algorithm.PCMToUlawAlgorithm;
import com.raytheon.uf.common.bmh.audio.impl.algorithm.UlawToPCMAlgorithm;

/**
 * Generic audio regulator that implements common functionality.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 25, 2015 4771       bkowal      Initial creation
 * Oct 14, 2015 4984       rjpeter     Fix sign on clipping.
 * Nov 04, 2015 5068       rjpeter     Switch audio units from dB to amplitude.
 * Feb 09, 2016 5082       bkowal      Updates for Apache commons lang 3.
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public abstract class AbstractAudioRegulator implements IAudioRegulator {

    protected final AudioRegulationConfiguration configuration;

    private final List<byte[]> audioCollectionToRegulate;

    /**
     * The peak amplitude across the entire audio {@link Collection}.
     */
    private short maxAmplitude = Short.MIN_VALUE;

    /**
     * The minimum amplitude across the entire audio {@link Collection}.
     */
    private short minAmplitude = Short.MAX_VALUE;

    private long duration;

    public AbstractAudioRegulator(
            final AudioRegulationConfiguration configuration) {
        this(configuration, null);
    }

    public AbstractAudioRegulator(
            final AudioRegulationConfiguration configuration,
            final List<byte[]> audioCollectionToRegulate) {
        this.configuration = configuration;
        this.audioCollectionToRegulate = audioCollectionToRegulate;
    }

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
    protected void regulateAudioVolume(byte[] sample,
            final double volumeAdjustment, int offset, int length)
            throws UnsupportedAudioFormatException, AudioConversionException,
            AudioOverflowException {
        for (int i = offset; i < (offset + length); i += 2) {
            short audioSample = (short) (((sample[i + 1] & 0xff) << 8) | (sample[i] & 0xff));
            double calculation = audioSample * volumeAdjustment;
            if (Math.abs(calculation) > BMHAudioConstants.MAX_AMPLITUDE) {
                audioSample = (short) (Math.signum(calculation) * BMHAudioConstants.MAX_AMPLITUDE);
            } else {
                audioSample = (short) calculation;
            }

            sample[i] = (byte) audioSample;
            sample[i + 1] = (byte) (audioSample >> 8);
        }
    }

    @Override
    public byte[] regulateAudioVolume(final byte[] ulawData,
            final short amplitude, final int sampleSize)
            throws AudioOverflowException, UnsupportedAudioFormatException,
            AudioConversionException {
        long start = System.currentTimeMillis();

        byte[] pcmData = new byte[sampleSize * 2];
        int length = sampleSize;
        short maxValue = -Short.MAX_VALUE;

        for (int i = 0; i < ulawData.length; i += sampleSize) {
            if (i + length > ulawData.length) {
                length = ulawData.length - i;
            }

            UlawToPCMAlgorithm.convert(ulawData, i, length, pcmData);
            Range<? extends Number> range = this.calculateBoundarySignals(
                    pcmData, 0, length * 2);
            maxValue = (short) Math.max(maxValue, range.getMaximum()
                    .shortValue());
        }

        double adjustmentRate = amplitude / (double) maxValue;
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

    @Override
    public List<byte[]> regulateAudioCollection(final short amplitude)
            throws Exception {
        if (this.audioCollectionToRegulate == null) {
            throw new IllegalStateException(
                    "This regulator has not been initialized in a way that allows for the regulation of an audio collection!");
        }

        /*
         * Determine the amplitude ranges.
         */
        byte[] pcmAudio = new byte[this.audioCollectionToRegulate.get(0).length * 2];
        for (byte[] ulawAudio : this.audioCollectionToRegulate) {
            /*
             * Have to create the conversion array every time because we cannot
             * be certain that all audio arrays in the {@link Collection} are
             * the same length.
             * 
             * TODO: Worth tracking the maximum length so that a single array
             * can be created for use during the actual conversion process and
             * only a subset of the elements would be touched, when necessary?
             */
            if (pcmAudio.length != ulawAudio.length * 2) {
                pcmAudio = new byte[ulawAudio.length * 2];
            }
            UlawToPCMAlgorithm
                    .convert(ulawAudio, 0, ulawAudio.length, pcmAudio);
            Range<? extends Number> range = this.calculateBoundarySignals(
                    pcmAudio, 0, pcmAudio.length);
            this.maxAmplitude = (short) Math.max(range.getMaximum()
                    .shortValue(), this.maxAmplitude);
            this.minAmplitude = (short) Math.min(range.getMinimum()
                    .shortValue(), this.minAmplitude);
        }

        /*
         * Determine if the audio will need to be regulated based on the
         * amplitude range?
         */
        if (this.skipAudio(this.minAmplitude, this.maxAmplitude)) {
            return this.audioCollectionToRegulate;
        }

        /*
         * Calculate the amount of adjustment required.
         */
        double adjustmentRate = amplitude / (double) this.maxAmplitude;
        /*
         * Alter the audio.
         */
        for (byte[] ulawAudio : this.audioCollectionToRegulate) {
            if (pcmAudio.length != ulawAudio.length * 2) {
                pcmAudio = new byte[ulawAudio.length * 2];
            }
            UlawToPCMAlgorithm
                    .convert(ulawAudio, 0, ulawAudio.length, pcmAudio);
            this.regulateAudioVolume(pcmAudio, adjustmentRate, 0,
                    pcmAudio.length);
            PCMToUlawAlgorithm.convert(pcmAudio, 0, pcmAudio.length, ulawAudio,
                    0);
        }

        return this.audioCollectionToRegulate;
    }

    protected void adjustAudioSamplePCM(final byte[] sample,
            final double adjustmentRate, int offset, int length)
            throws UnsupportedAudioFormatException, AudioConversionException,
            AudioOverflowException {
        Range<? extends Number> amplitudeRange = this.calculateBoundarySignals(
                sample, offset, length);
        if (this.skipAudio(amplitudeRange.getMinimum().shortValue(),
                amplitudeRange.getMaximum().shortValue())) {
            return;
        }

        this.regulateAudioVolume(sample, adjustmentRate, offset, length);
    }

    protected abstract Range<? extends Number> calculateBoundarySignals(
            final byte[] audio, int offset, int length);

    /**
     * Determines if a segment of audio should be skipped based on the
     * {@link AudioRegulationConfiguration} settings and the specified minimum
     * amplitude and the specified maximum amplitude.
     * 
     * @param minAmplitude
     *            the specified minimum amplitude.
     * @param maxAmplitude
     *            the specified maximum amplitude.
     * @return true, if the audio can be skipped; false, otherwise
     */
    protected boolean skipAudio(final double minAmplitude,
            final double maxAmplitude) {
        return (maxAmplitude <= this.configuration.getAmplitudeSilenceLimit() && this.configuration
                .isDisableSilenceLimit() == false)
                || (maxAmplitude >= this.configuration.getAmplitudeMaxLimit() && this.configuration
                        .isDisableMaxLimit() == false);
    }

    /**
     * @return the duration
     */
    @Override
    public long getDuration() {
        return duration;
    }
}