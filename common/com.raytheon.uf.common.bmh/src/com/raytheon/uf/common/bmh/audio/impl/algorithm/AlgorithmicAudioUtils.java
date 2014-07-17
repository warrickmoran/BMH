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
package com.raytheon.uf.common.bmh.audio.impl.algorithm;

import java.io.IOException;
import java.util.Arrays;

import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

import com.raytheon.uf.common.bmh.audio.AudioConversionException;
import com.raytheon.uf.common.bmh.audio.BMHAudioFormat;
import com.sun.media.sound.AudioFloatFormatConverter;

/**
 * Currently a set of utility functions shared by the ulaw and pcm converters
 * (algorithms are primarily used to complete the conversions).
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 22, 2014 3383       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public final class AlgorithmicAudioUtils {

    /*
     * The compatible PCM format that will work throughout the BMH software.
     * With ffmpeg, we should be able to work with higher quality audio, if
     * necessary. The specified format is equivalent to the audio output
     * produced by the TTS Manager.
     */
    public static final AudioFormat COMPATIBLE_PCM_FORMAT = new AudioFormat(
            8000, 16, 1, true, false);

    /**
     * Utility Class. No Construction Allowed.
     */
    protected AlgorithmicAudioUtils() {
    }

    /**
     * Attempts to Re-sample the specified PCM Audio Stream.
     * 
     * @param audioInputStream
     *            the PCM Audio Stream to re-sample.
     * @param originalSampleLength
     *            the length of the original, source, PCM sample
     * @return the re-sampled PCM data
     * @throws AudioConversionException
     *             if the re-sampling proces fails
     */
    public static byte[] resamplePCMData(
            final AudioInputStream audioInputStream,
            final int originalSampleLength) throws AudioConversionException {

        byte[] resampledData = new byte[originalSampleLength];
        try (AudioInputStream resampledAudioInputStream = new AudioFloatFormatConverter()
                .getAudioInputStream(COMPATIBLE_PCM_FORMAT, audioInputStream);) {
            resampledAudioInputStream.read(resampledData);
        } catch (IllegalArgumentException | IOException e) {
            throw new AudioConversionException("Failed to resample PCM data!",
                    e);
        }

        return resampledData;
    }

    /**
     * Verifies that the encoding of the specified audio stream is one of the
     * specified encodings.
     * 
     * @param audioInputStream
     *            the specified audio stream
     * @param allowedEncodings
     *            the specified audio encodings
     * @param srcFormat
     *            the audio format associated with the specified audio stream
     * @throws AudioConversionException
     *             when the encoding of the specified audio stream is not one of
     *             the specified encodings
     */
    public static void verifyAudioEncoding(
            final AudioInputStream audioInputStream,
            final Encoding[] allowedEncodings, final BMHAudioFormat srcFormat)
            throws AudioConversionException {
        if (Arrays.asList(allowedEncodings).contains(
                audioInputStream.getFormat().getEncoding())) {
            return;
        }

        throw new AudioConversionException("Unexpected audio encoding: "
                + audioInputStream.getFormat().getEncoding().toString()
                + " for input audio format: " + srcFormat.toString() + "!");
    }
}