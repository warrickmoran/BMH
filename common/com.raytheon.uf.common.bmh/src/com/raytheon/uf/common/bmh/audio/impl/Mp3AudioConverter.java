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
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.LinkedList;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.AudioFormat.Encoding;

import com.raytheon.uf.common.bmh.audio.BMHAudioFormat;
import com.raytheon.uf.common.bmh.audio.ConversionNotSupportedException;

/**
 * The mp3 audio converter. Used to convert ulaw audio to mp3 audio. This
 * converter is dependent on the external program ffmpeg. This converter will
 * not be usable if ffmpeg is not present on the system the converter is used
 * on.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 3, 2014  3880       bkowal      Initial creation
 * Aug 7, 2015  4424       bkowal      Simplified command-line for wav to mp3.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class Mp3AudioConverter extends FFMpegAudioConverter {

    private static final BMHAudioFormat CONVERSION_FORMAT = BMHAudioFormat.MP3;

    private static final BMHAudioFormat[] SUPPORTED_FORMATS = new BMHAudioFormat[] { BMHAudioFormat.WAV };

    private static final String ffmpegInputFormatIdentifier = "wav";

    private static final String ffmpegOutputFormatIdentifier = "mp3";

    /**
     * Constructor
     */
    public Mp3AudioConverter() {
        super(CONVERSION_FORMAT, SUPPORTED_FORMATS);
    }

    protected Path prepareInput(final byte[] src, BMHAudioFormat srcFormat)
            throws IOException {
        final Path inputPath = Files.createTempFile(null,
                srcFormat.getExtension());

        try (OutputStream os = Files.newOutputStream(inputPath)) {
            final AudioFormat audioFormat = new AudioFormat(Encoding.ULAW,
                    8000, 8, 1, 1, 8000, true);
            final AudioInputStream audioInputStream = new AudioInputStream(
                    new ByteArrayInputStream(src), audioFormat, src.length);
            AudioSystem.write(audioInputStream, Type.WAVE, os);
        }

        return inputPath;
    }

    /*
     * ffmpeg -ar 8000 -f mulaw -i DENADRBOU_ENG_Paul_751_192220.ulaw -ar 44100
     * -ab 160k -y test_conv.mp3
     */

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.common.bmh.audio.impl.FFMpegAudioConverter#getFFMpegArgs
     * (java.nio.file.Path, java.nio.file.Path)
     */
    @Override
    protected List<String> getFFMpegArgs(Path inputFile, Path outputFile) {
        List<String> args = new LinkedList<>();
        args.add(FFMPG_FORCE_FORMAT);
        args.add(ffmpegInputFormatIdentifier);
        args.add(FFMPG_INPUT_NAME);
        args.add(inputFile.toString());
        args.add(FFMPG_OVERWRITE_OUT);
        args.add(outputFile.toString());

        return args;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.common.bmh.audio.impl.FFMpegAudioConverter#
     * verifyFFMpegRecognizedFormat()
     */
    @Override
    protected void verifyFFMpegRecognizedFormat()
            throws ConversionNotSupportedException {
        this.verifyFFMpegFormatSupport(ffmpegInputFormatIdentifier);
        this.verifyFFMpegFormatSupport(ffmpegOutputFormatIdentifier);
    }
}