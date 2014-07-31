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

    private static final double DB_ALTERATION_CONSTANT = 10.0;

    /*
     * The managed audio data.
     */
    private byte[] originalPCMData;

    private byte[] originalUlawData;

    /*
     * The average (in decibels) associated with the altered audio.
     */
    private double alteredAverageDB;

    private double[] alteredSampledAudioDB;

    /*
     * The standard deviation associated with the altered audio.
     */

    /*
     * The decibel value associated with the loudest audio sample in the managed
     * audio data (in decibels).
     */
    private double signalMaxDB;

    /*
     * The decibel value associated with the quietest audio sample in the
     * managed audio data (in decibels).
     */
    private double signalMinDB;

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
        this.originalUlawData = ulawData;
        this.originalPCMData = AudioConvererterManager
                .getInstance()
                .convertAudio(ulawData, BMHAudioFormat.ULAW, BMHAudioFormat.PCM);
        this.alteredSampledAudioDB = new double[this.originalPCMData.length / 2];
        this.calculateBoundarySignals();
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
        return this.regulateAudioVolume(volumeAdjustment, true);
    }

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
    public byte[] regulateAudioVolume(double volumeAdjustment,
            boolean convertUlaw) throws UnsupportedAudioFormatException,
            AudioConversionException, AudioOverflowException {
        double sampleSize = this.originalPCMData.length / 2;
        double alteredDecibelSum = 0.;
        byte[] adjustedPCMData = new byte[this.originalPCMData.length];

        for (int i = 0; i < adjustedPCMData.length; i += 2) {
            short audioSample = (short) (((this.originalPCMData[i + 1] & 0xff) << 8) | (this.originalPCMData[i] & 0xff));

            audioSample = (short) (audioSample * volumeAdjustment);
            if (Math.abs(audioSample) > MAX_AMPLITUDE) {
                throw new AudioOverflowException(volumeAdjustment,
                        Math.abs(audioSample));
            }

            double audioDecibels = this
                    .calculateDecibels(Math.abs(audioSample));
            this.alteredSampledAudioDB[i / 2] = audioDecibels;
            if (audioSample != 0) {
                alteredDecibelSum += audioDecibels;
            } else {
                /*
                 * Exclude amplitudes of 0 due to the -INFINITY decibel value
                 * associated with them.
                 */
                --sampleSize;
            }

            adjustedPCMData[i] = (byte) audioSample;
            adjustedPCMData[i + 1] = (byte) (audioSample >> 8);
        }

        /*
         * Calculate the mean (in decibels) of the adjusted audio.
         */
        this.alteredAverageDB = alteredDecibelSum / sampleSize;

        if (convertUlaw) {
            return AudioConvererterManager.getInstance().convertAudio(
                    adjustedPCMData, BMHAudioFormat.PCM, BMHAudioFormat.ULAW);
        } else {
            return adjustedPCMData;
        }
    }

    /**
     * Adjusts the regulated audio data such that the minimum signal (in dB)
     * will be greater than or equal to the specified minimum signal strength
     * (in dB) and the maximum signal (in dB) will be less than or equal to the
     * specified maximum signal strength (in dB).
     * 
     * @param dbMin
     *            the specified minimum signal strength (in dB)
     * @param dbMax
     *            the specified maximum signal strength (in dB)
     * @return the adjusted audio data in ulaw format
     * @throws AudioOverflowException
     *             if the requested volume adjustment would generate invalid
     *             audio samples
     * @throws UnsupportedAudioFormatException
     *             when the final adjusted pcm to ulaw conversion fails
     * @throws AudioConversionException
     *             when the final adjusted pcm to ulaw conversion fails
     */
    public byte[] regulateAudioVolumeRange(double dbMin, double dbMax)
            throws AudioOverflowException, UnsupportedAudioFormatException,
            AudioConversionException {
        if (dbMin > dbMax) {
            throw new IllegalArgumentException(
                    "Error: dbMin must be less than dbMax!");
        }

        /*
         * Determine if the audio is already within range.
         */
        if (this.signalMinDB >= dbMin && this.signalMaxDB <= dbMax) {
            return this.originalUlawData;
        }

        /*
         * Determine if we are attenuating the audio or amplifying the audio.
         */
        double difference = 0.;
        if (this.signalMaxDB > dbMax) {
            // attenuate - audio is too loud
            difference = dbMax - this.signalMaxDB;
        } else if (this.signalMinDB < dbMin) {
            // amplify - audio is not loud enough
            difference = dbMin - this.signalMinDB;
        }

        double adjustmentRate = Math.pow(DB_ALTERATION_CONSTANT,
                (difference / AMPLITUDE_TO_DB_CONSTANT));
        byte[] alteredAudio = this.regulateAudioVolume(adjustmentRate, false);

        /* is the average still within range. */
        if (this.alteredAverageDB >= dbMin && this.alteredAverageDB <= dbMax) {
            return AudioConvererterManager.getInstance().convertAudio(
                    alteredAudio, BMHAudioFormat.PCM, BMHAudioFormat.ULAW);
        }

        /* if not, calculate the standard deviation. */
        double alteredAudioStdDev = this
                .calculateStandardDeviationAdjustedAudio();
        double adjustedDBMin = this.alteredAverageDB - alteredAudioStdDev;
        double adjustedDBMax = this.alteredAverageDB + alteredAudioStdDev;
        /* search for outliers */
        for (int i = 0; i < this.alteredSampledAudioDB.length; i++) {
            double audioDecibels = this.alteredSampledAudioDB[i];
            if (audioDecibels == Double.NEGATIVE_INFINITY) {
                /*
                 * skip the obvious outliers that do not make any contribution
                 * to the audio wave.
                 */
                continue;
            }

            if (audioDecibels >= adjustedDBMin
                    && audioDecibels <= adjustedDBMax) {
                /* audio falls within the allowed range */
                continue;
            }

            /* adjust the outlier individually based on the original data. */

            // Determine the index associated with the outlier
            int index = i * 2;
            /*
             * TODO: do we just change the individual outlier or do we check for
             * other outliers in immediate proximity to the original and then
             * adjust all of them together?
             */
            short audioSample = (short) (((this.originalPCMData[index + 1] & 0xff) << 8) | (this.originalPCMData[index] & 0xff));
            double sampleDB = this.calculateDecibels(Math.abs(audioSample));
            adjustmentRate = this.calculateAudioAdjustmentRate(sampleDB, dbMin,
                    dbMax);
            audioSample = (short) (audioSample * adjustmentRate);
            if (Math.abs(audioSample) > MAX_AMPLITUDE) {
                throw new AudioOverflowException(adjustmentRate,
                        Math.abs(audioSample));
            }

            /* update the originally adjusted audio sample. */
            alteredAudio[index] = (byte) audioSample;
            alteredAudio[index + 1] = (byte) (audioSample >> 8);
        }

        return AudioConvererterManager.getInstance().convertAudio(alteredAudio,
                BMHAudioFormat.PCM, BMHAudioFormat.ULAW);
    }

    /**
     * Returns the maximum amplitude associated with the audio in decibels (dB)
     * 
     * @return the maximum amplitude associated with the audio in decibels (dB)
     */
    public double getSignalMaxDB() {
        return this.signalMaxDB;
    }

    /**
     * Returns the minimum amplitude associated with the audio in decibels (dB)
     * 
     * @return the minimum amplitude associated with the audio in decibels (dB)
     */
    public double getSignalMinDB() {
        return this.signalMinDB;
    }

    /**
     * Determines the minimum and maximum amplitudes to calculate the min and
     * max decibels respectively associated with the managed audio data.
     */
    private void calculateBoundarySignals() {
        double runningMinAmplitude = 0.0;
        double runningMaxAmplitude = 0.0;

        for (int i = 0; i < this.originalPCMData.length; i += 2) {
            short amplitude = (short) (((this.originalPCMData[i + 1] & 0xff) << 8) | (this.originalPCMData[i] & 0xff));
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

        /* determine the maximum and minimum amplitude in dB */
        this.signalMaxDB = this.calculateDecibels(runningMaxAmplitude);
        this.signalMinDB = this.calculateDecibels(runningMinAmplitude);
    }

    /**
     * Converts the specified amplitude to decibels
     * 
     * @param amplitude
     *            the specified amplitude
     * @return the amplitude converted to decibels
     */
    private double calculateDecibels(double amplitude) {
        double amplitudeRatio = amplitude / MAX_AMPLITUDE;
        return AMPLITUDE_TO_DB_CONSTANT * Math.log10(amplitudeRatio);
    }

    /**
     * Calculates the standard deviation based on the decibel values associated
     * with the altered audio.
     * 
     * @return the calculated standard deviation
     */
    private double calculateStandardDeviationAdjustedAudio() {
        double differenceSquaredSum = 0.;
        double sampleCount = this.alteredSampledAudioDB.length;

        for (double audioDecibels : this.alteredSampledAudioDB) {
            if (audioDecibels == Double.NEGATIVE_INFINITY) {
                --sampleCount;
                continue;
            }
            differenceSquaredSum += Math.pow(
                    (audioDecibels - this.alteredAverageDB), 2);
        }

        return Math.sqrt(differenceSquaredSum / sampleCount);
    }

    /**
     * Compares the specified current decibel range to the specified minimum and
     * maximum and determines how the current range must be adjusted to fall
     * between the specified minimum and maximum.
     * 
     * @param currentDB
     *            the specified current decibel range
     * @param minDB
     *            the specified minimum
     * @param maxDB
     *            the specified maximum
     * @return the value that the audio should be adjusted by
     */
    private double calculateAudioAdjustmentRate(double currentDB, double minDB,
            double maxDB) {
        double difference = 0.;
        if (currentDB > maxDB) {
            // attenuate - audio is too loud
            difference = maxDB - currentDB;
        } else if (currentDB < minDB) {
            // amplify - audio is not loud enough
            difference = minDB - currentDB;
        }

        return Math.pow(DB_ALTERATION_CONSTANT,
                (difference / AMPLITUDE_TO_DB_CONSTANT));
    }
}