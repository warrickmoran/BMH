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
package com.raytheon.uf.common.bmh.audio.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.AudioFormat.Encoding;

import com.raytheon.uf.common.bmh.audio.BMHAudioFormat;
import com.raytheon.uf.common.bmh.audio.AudioConversionException;
import com.raytheon.uf.common.bmh.audio.UnsupportedAudioFormatException;

/**
 * The Wav Audio Converter. Used to convert ULAW and PCM audio files to WAV
 * audio files.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 16, 2014 3383       bkowal      Initial creation
 * Dec 3, 2014  3880       bkowal      Extend JavaRecognizedAudioConverter.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class WavAudioConverter extends JavaRecognizedAudioConverter {

    private static final BMHAudioFormat CONVERSION_FORMAT = BMHAudioFormat.WAV;

    private static final BMHAudioFormat[] SUPPORTED_FORMATS = new BMHAudioFormat[] {
            BMHAudioFormat.ULAW, BMHAudioFormat.PCM };

    private static final int FRAME_SIZE = 160;

    /**
     * Constructor
     */
    public WavAudioConverter() {
        super(CONVERSION_FORMAT, SUPPORTED_FORMATS);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.common.bmh.audio.IAudioConverter#convertAudio(byte[])
     */
    @Override
    public byte[] convertAudio(byte[] src, BMHAudioFormat srcFormat)
            throws AudioConversionException, UnsupportedAudioFormatException {
        super.verifySupportedAudioFormat(srcFormat);
        long numFrames = src.length / FRAME_SIZE;

        AudioFormat audioFormat = null;
        /*
         * TODO: may need to make the audio formats more dynamic or allow for
         * re-sampling if we ever receive pcm or ulaw data from outside the BMH
         * software (Need example data first).
         */
        if (srcFormat == BMHAudioFormat.ULAW) {
            audioFormat = new AudioFormat(Encoding.ULAW, 8000, 8, 1,
                    FRAME_SIZE, 8000, false);
        } else if (srcFormat == BMHAudioFormat.PCM) {
            audioFormat = new AudioFormat(Encoding.PCM_SIGNED, 8000, 16, 1,
                    FRAME_SIZE, 8000, true);
        }
        AudioInputStream audioInputStream = new AudioInputStream(
                new ByteArrayInputStream(src), audioFormat, numFrames);

        ByteArrayOutputStream destination = new ByteArrayOutputStream();
        try {
            AudioSystem.write(audioInputStream, Type.WAVE, destination);
        } catch (IOException e) {
            throw new AudioConversionException("Failed to convert the "
                    + srcFormat.toString() + " audio to wav audio!", e);
        }

        return destination.toByteArray();
    }
}