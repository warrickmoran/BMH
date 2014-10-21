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
package com.raytheon.uf.common.bmh.tones;

import com.raytheon.uf.common.bmh.tones.data.Tone;

/**
 * Used to encode a {@link Tone} into an audio signal.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 20, 2014 3304       bkowal      Initial creation
 * Oct 17, 2014 3655       bkowal      Move tones to common.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class ToneGenerator {
    private static final int DEFAULT_SAMPLE_COUNT = 1024;

    private static final int DEFAULT_SAMPLE_RATE = 8000;

    /* Dynamically calculated sine wave based on a specified amplitude. */
    private short[] sineWave = new short[DEFAULT_SAMPLE_COUNT];

    /* Sample rate that will be used in the calculation */
    private double sampleRate;

    /*
     * Used during generation of the tone when interpolation is enabled. Tracks
     * the current position in the tone.
     */
    private double phase;

    /*
     * The actual number of samples; note: not an Integer
     */
    private double actualNumSamples;

    /*
     * The number of samples that were actually used. A rounding of the actual
     * number of samples because it is not possible to calculate a partial
     * sample.
     */
    private int usedNumSamples;

    /*
     * Simple boolean value that indicates whether or not the data will need to
     * be interpolated.
     */
    private boolean interpolate;

    /**
     * Constructor
     */
    public ToneGenerator() {
        this.sampleRate = DEFAULT_SAMPLE_RATE;
        this.interpolate = true;
        this.actualNumSamples = 0.0;
        this.usedNumSamples = 0;
        this.phase = 0.0;
    }

    /**
     * Calculates the sine wave based on the specified amplitude.
     * 
     * @param amplitude
     *            the specified amplitude.
     */
    private void initialize(double amplitude) {
        this.actualNumSamples = 0;
        this.usedNumSamples = 0;
        double radsPerBin = 2.0 * Math.PI / (double) DEFAULT_SAMPLE_COUNT;

        for (int i = 0; i < DEFAULT_SAMPLE_COUNT; i++) {
            this.sineWave[i] = (short) Math.round(amplitude
                    * Math.sin(radsPerBin * (double) i));
        }
    }

    /**
     * Completes the encoding process and produces a tone in PCM format. Based
     * on ToneGenerator::encode(Tone& tone, short *buff) in fskEncoder.C in the
     * original BMH source code.
     * 
     * @param tone
     *            the tone data to encode
     * @return the encoded data
     */
    public short[] encode(Tone tone) {
        this.initialize(tone.getAmplitude());

        // calculate exact number of bits per sample
        double exactNumSamplesPerBit = this.sampleRate * tone.getDuration();

        // calculate the number of samples that will be used to encode this bit
        int usedNumSamplesPerBit = (int) exactNumSamplesPerBit;

        // update the running totals
        this.actualNumSamples += exactNumSamplesPerBit;
        this.usedNumSamples += usedNumSamplesPerBit;

        // determine if the cumulative fraction part has wrapped into a new
        // integer.
        if ((int) this.actualNumSamples > this.usedNumSamples) {
            ++usedNumSamplesPerBit;
            ++this.usedNumSamples;
        }

        double interval = tone.getFrequency() / this.sampleRate
                * (double) DEFAULT_SAMPLE_COUNT;

        short[] output = new short[usedNumSamplesPerBit];

        for (int i = 0; i < usedNumSamplesPerBit; i++) {
            this.phase += interval;
            int idx = (int) this.phase % DEFAULT_SAMPLE_COUNT;
            if (this.interpolate) {
                double frac = this.phase - Math.floor(this.phase);
                if (frac > 0.5) {
                    if (idx + 1 < DEFAULT_SAMPLE_COUNT) {
                        output[i] = this.sineWave[idx + 1];
                    } else {
                        output[i] = this.sineWave[0];
                    }
                } else {
                    output[i] = this.sineWave[idx];
                }
            } else {
                output[i] = this.sineWave[i];
            }
        }

        return output;
    }

    /**
     * Returns the total number of samples based on duration of the specified
     * tone.
     * 
     * @param tone
     *            the specified tone
     * @return the total number of samples
     */
    public double getNumberOfSamples(Tone tone) {
        return this.sampleRate * tone.getDuration();
    }

    /**
     * Returns the current sample rate
     * 
     * @return the current sample rate
     */
    public double getSampleRate() {
        return sampleRate;
    }

    /**
     * Sets the sample rate
     * 
     * @param sampleRate
     *            the sample rate
     */
    public void setSampleRate(double sampleRate) {
        this.sampleRate = sampleRate;
    }

    /**
     * Returns whether or not the data will be interpolated
     * 
     * @return { true : if the data will be interpolated; false : otherwise }
     */
    public boolean isInterpolate() {
        return interpolate;
    }

    /**
     * Sets the interpolation flag
     * 
     * @param interpolate
     *            the interpolation flag
     */
    public void setInterpolate(boolean interpolate) {
        this.interpolate = interpolate;
    }
}
