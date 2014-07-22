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

import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.raytheon.uf.common.bmh.audio.AbstractAudioConverter;
import com.raytheon.uf.common.bmh.audio.AudioConversionException;
import com.raytheon.uf.common.bmh.audio.BMHAudioFormat;
import com.raytheon.uf.common.bmh.audio.UnsupportedAudioFormatException;
import com.raytheon.uf.common.bmh.audio.impl.algorithm.AlgorithmicAudioUtils;
import com.raytheon.uf.common.bmh.audio.impl.algorithm.DecompressInputStream;
import com.raytheon.uf.common.bmh.audio.impl.algorithm.DecompressInputStream.DECOMPRESSION_TYPE;

/**
 * The Pcm audio converter. Used to convert ULAW audio to PCM audio.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 21, 2014 3383       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class PcmAudioConverter extends AbstractAudioConverter {

    private static final BMHAudioFormat CONVERSION_FORMAT = BMHAudioFormat.PCM;

    private static final BMHAudioFormat[] SUPPORTED_FORMATS = new BMHAudioFormat[] {
            BMHAudioFormat.ULAW, BMHAudioFormat.WAV };

    public PcmAudioConverter() {
        super(CONVERSION_FORMAT, SUPPORTED_FORMATS);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.common.bmh.audio.IAudioConverter#convertAudio(byte[],
     * com.raytheon.uf.common.bmh.audio.BMHAudioFormat)
     */
    @Override
    public byte[] convertAudio(byte[] src, BMHAudioFormat srcFormat)
            throws AudioConversionException, UnsupportedAudioFormatException {
        super.verifySupportedAudioFormat(srcFormat);

        /*
         * When the input is in wav format, there is additional verification
         * that must be done. Additionally, the wav file may already contain
         * data in the correct format (less, likely in this case).
         */
        if (srcFormat == BMHAudioFormat.WAV) {
            try (AudioInputStream audioInputStream = AudioSystem
                    .getAudioInputStream(new ByteArrayInputStream(src));) {
                AlgorithmicAudioUtils.verifyAudioEncoding(audioInputStream,
                        new Encoding[] { Encoding.PCM_SIGNED, Encoding.ULAW },
                        srcFormat);

                /* Does the wav file already contain data in pcm format? */
                if (audioInputStream.getFormat().getEncoding() == AudioFormat.Encoding.PCM_SIGNED) {
                    return AlgorithmicAudioUtils.resamplePCMData(
                            audioInputStream, src.length);
                }

                /*
                 * TODO: does ulaw need to be re-sampled? ulaw is not common in
                 * WAV files. The majority of the WAV files included on Linux
                 * are in PCM format.
                 */
            } catch (UnsupportedAudioFileException | IOException e) {
                throw new AudioConversionException("Failed to convert the "
                        + srcFormat.toString() + " audio to pcm audio!", e);
            }
        }

        /* Complete the ulaw to pcm conversion. */
        try (DecompressInputStream inputStream = new DecompressInputStream(
                new ByteArrayInputStream(src), DECOMPRESSION_TYPE.ULAW);) {
            byte[] pcmArray = new byte[src.length << 1];
            inputStream.read(pcmArray);
            return pcmArray;
        } catch (IOException e) {
            throw new AudioConversionException("Failed to convert the "
                    + srcFormat.toString() + " audio to pcm audio!", e);
        }
    }
}