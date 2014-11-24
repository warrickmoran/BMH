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
package com.raytheon.uf.viz.bmh.ui.recordplayback;

import com.raytheon.uf.common.bmh.audio.AudioConvererterManager;
import com.raytheon.uf.common.bmh.audio.AudioConversionException;
import com.raytheon.uf.common.bmh.audio.BMHAudioConstants;
import com.raytheon.uf.common.bmh.audio.BMHAudioFormat;
import com.raytheon.uf.common.bmh.audio.UnsupportedAudioFormatException;

/**
 * Utility to calculate the average decibel level of a sequence of audio bytes.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 24, 2014            bkowal     Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public abstract class DecibelCalculator {

    private static final int SAMPLE_BYTES = 2;

    /**
     * 
     */
    protected DecibelCalculator() {
    }

    /**
     * Calculates the average decibel level of the specified audio array. The
     * specified audio array must be in ulaw format.
     * 
     * @param audioData
     *            the specified audio array in ulaw format.
     * @return the calculated average decibel level
     * @throws UnsupportedAudioFormatException
     *             if ulaw and/or pcm format are not recognized by the converter
     * @throws AudioConversionException
     *             if the ulaw to pcm conversion fails
     */
    public static double calculateAudioDecibels(final byte[] audioData)
            throws UnsupportedAudioFormatException, AudioConversionException {
        /*
         * Convert the audio data to pcm format.
         */
        final byte[] pcmData = AudioConvererterManager.getInstance()
                .convertAudio(audioData, BMHAudioFormat.ULAW,
                        BMHAudioFormat.PCM);

        /*
         * We need to calculate the signal strength of every sample within the
         * provided audio data and average them together.
         */
        short runningAmplitudeTotal = 0;
        int totalSampleCount = (pcmData.length / SAMPLE_BYTES);
        for (int i = 0; i < pcmData.length; i += 2) {
            short amplitude = (short) (((pcmData[i + 1] & 0xff) << 8) | (pcmData[i] & 0xff));

            runningAmplitudeTotal += amplitude;
        }

        final short amplitudeAverage = (short) (runningAmplitudeTotal / totalSampleCount);

        /*
         * Convert the signal strength to decibels.
         */
        double amplitudeRatio = (double) amplitudeAverage
                / BMHAudioConstants.MAX_AMPLITUDE;
        return BMHAudioConstants.AMPLITUDE_TO_DB_CONSTANT
                * Math.log10(amplitudeRatio);
    }
}