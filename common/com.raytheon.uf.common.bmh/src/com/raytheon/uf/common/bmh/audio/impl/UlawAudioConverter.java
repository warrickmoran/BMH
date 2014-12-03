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
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.AudioFormat.Encoding;

import com.raytheon.uf.common.bmh.audio.BMHAudioFormat;
import com.raytheon.uf.common.bmh.audio.AudioConversionException;
import com.raytheon.uf.common.bmh.audio.UnsupportedAudioFormatException;
import com.raytheon.uf.common.bmh.audio.impl.algorithm.AlgorithmicAudioUtils;
import com.raytheon.uf.common.bmh.audio.impl.algorithm.CompressInputStream;
import com.raytheon.uf.common.bmh.audio.impl.algorithm.CompressInputStream.COMPRESSION_TYPE;

/**
 * The Ulaw audio converter. Used to convert PCM audio to ULAW audio.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 17, 2014 3383       bkowal      Initial creation
 * Dec 3, 2014  3880       bkowal      Extend JavaRecognizedAudioConverter.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class UlawAudioConverter extends JavaRecognizedAudioConverter {

    private static final BMHAudioFormat CONVERSION_FORMAT = BMHAudioFormat.ULAW;

    private static final BMHAudioFormat[] SUPPORTED_FORMATS = new BMHAudioFormat[] {
            BMHAudioFormat.PCM, BMHAudioFormat.WAV };

    /**
     * Constructor
     */
    public UlawAudioConverter() {
        super(CONVERSION_FORMAT, SUPPORTED_FORMATS);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.common.bmh.audio.IAudioConverter#convertAudio(byte[],
     * com.raytheon.uf.common.bmh.audio.AUDIO_FORMAT)
     */
    @Override
    public byte[] convertAudio(byte[] src, BMHAudioFormat srcFormat)
            throws AudioConversionException, UnsupportedAudioFormatException {
        super.verifySupportedAudioFormat(srcFormat);

        /*
         * When the input is in wav format, there is additional verification
         * that must be done. Additionally, the wav file may already contain
         * data in the correct format.
         */
        if (srcFormat == BMHAudioFormat.WAV) {
            try (AudioInputStream audioInputStream = AudioSystem
                    .getAudioInputStream(new ByteArrayInputStream(src));) {

                AlgorithmicAudioUtils.verifyAudioEncoding(audioInputStream,
                        new Encoding[] { Encoding.PCM_SIGNED, Encoding.ULAW },
                        srcFormat);

                /* Does the wav file already contain data in ulaw format? */
                if (audioInputStream.getFormat().getEncoding() == AudioFormat.Encoding.ULAW) {
                    /*
                     * TODO: does ulaw audio need to be resampled?
                     */
                    return src;
                }

                /*
                 * Ensure that the PCM data has the correct format. Down-sample,
                 * if necessary.
                 */
                src = AlgorithmicAudioUtils.resamplePCMData(audioInputStream,
                        src.length);

            } catch (UnsupportedAudioFileException | IOException e) {
                throw new AudioConversionException("Failed to convert the "
                        + srcFormat.toString() + " audio to ulaw audio!", e);
            }
        }

        /* Complete the pcm to ulaw conversion. */
        try (CompressInputStream inputStream = new CompressInputStream(
                new ByteArrayInputStream(src), COMPRESSION_TYPE.ULAW);) {
            /*
             * the destination byte array will be half the size of the source
             * byte array
             */
            byte[] ulawArray = new byte[src.length >> 1];
            inputStream.read(ulawArray);
            return ulawArray;
        } catch (IOException e) {
            throw new AudioConversionException("Failed to convert the "
                    + srcFormat.toString() + " audio to ulaw audio!", e);
        }
    }
}