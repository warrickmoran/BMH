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

import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.raytheon.uf.common.bmh.audio.AbstractAudioConverter;
import com.raytheon.uf.common.bmh.audio.AudioConversionException;
import com.raytheon.uf.common.bmh.audio.BMHAudioFormat;
import com.raytheon.uf.common.bmh.audio.ConversionNotSupportedException;
import com.raytheon.uf.common.bmh.audio.UnsupportedAudioFormatException;
import com.raytheon.uf.common.util.RunProcess;

/**
 * Audio Conversion that is dependent on the existence of the ffmpeg
 * application. If ffmpeg is not present, the registration of all converters
 * that extend this class will fail.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 2, 2014  3880       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public abstract class FFMpegAudioConverter extends AbstractAudioConverter {

    private static final String FFMPEG = "ffmpeg";

    private static final String DEFAULT_FFMPEG = Paths
            .get(File.separator + "usr").resolve("bin").resolve(FFMPEG)
            .toString();

    private static final String BMH_FFMPEG_PROPERTY = "bmh.ffmpeg";

    private static final String CONSTANT_SPACE = " ";

    private static final String DECODE_ENCODE_SUPPORTED = "DE";

    /* Begin ffmpeg arguments */

    protected static final String FFMPG_FORMATS = "-formats";

    protected static final String FFMPG_FORCE_FORMAT = "-f";

    protected static final String FFMPG_INPUT_NAME = "-i";

    protected static final String FFMPG_OVERWRITE_OUT = "-y";

    protected static final String FFMPG_SAMPLE_FREQ = "-ar";

    protected static final String FFMPG_BITRATE = "-ab";

    /*
     * ffmpeg supports a significantly larger number of arguments. However, the
     * above arguments are all that we need for our current purpose.
     */

    /* End ffmpeg arguments */

    private final String ffmpegExe;

    private final boolean ffmpegFound;

    private List<String> ffmpegSupportedFormats;

    private RunProcess ffmpegProcess;

    /**
     * Constructor
     * 
     * @param outputFormat
     *            the supported destination format
     * @param supportedFormats
     *            the supported source (input) formats
     */
    public FFMpegAudioConverter(BMHAudioFormat outputFormat,
            BMHAudioFormat[] supportedFormats) {
        super(outputFormat, supportedFormats);
        this.ffmpegExe = System
                .getProperty(BMH_FFMPEG_PROPERTY, DEFAULT_FFMPEG);
        this.ffmpegFound = Files.exists(Paths.get(this.ffmpegExe));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.common.bmh.audio.IAudioConverter#verifyCompatibility()
     */
    @Override
    public final void verifyCompatibility()
            throws ConversionNotSupportedException {
        if (this.ffmpegFound) {
            this.verifyFFMpegRecognizedFormat();
            return;
        }

        StringBuilder reasonStr = new StringBuilder(
                "Unable to find the required ").append(FFMPEG)
                .append(" executable at the expected location: ")
                .append(this.ffmpegExe).append(". Please verify that ")
                .append(FFMPEG).append(" has been installed or set the ")
                .append(BMH_FFMPEG_PROPERTY)
                .append(" property to specify an alternate location.");

        throw new ConversionNotSupportedException(this.getOutputFormat(),
                reasonStr.toString());
    }

    /**
     * Run the ffmpeg executable with the specified arguments.
     * 
     * @param arguments
     *            the specified arguments for ffmpeg
     * @return the exit status of the ffmpeg executable
     * @throws Exception
     *             if ffmpeg cannot be started
     */
    private boolean runFFMpegCommand(List<String> arguments) throws Exception {
        /* build the ffmpeg command line */
        StringBuilder sb = new StringBuilder(this.ffmpegExe)
                .append(CONSTANT_SPACE);
        boolean first = true;
        for (String arg : arguments) {
            if (first) {
                first = false;
            } else {
                sb.append(CONSTANT_SPACE);
            }
            sb.append(arg);
        }

        try {
            this.ffmpegProcess = RunProcess.getRunProcess().exec(sb.toString());
        } catch (IOException e) {
            throw new Exception("Failed to run " + FFMPEG + "!", e);
        }

        /**
         * The ffmpeg process never prompts for input based on the way the
         * converters will be using it. So, we should never have to worry about
         * the processing hanging.
         */
        return this.ffmpegProcess.waitFor() == 0;
    }

    private void getFFMPegSupportedFormats() throws Exception {
        /*
         * prepare the arguments.
         */
        List<String> arguments = new ArrayList<>(1);
        arguments.add(FFMPG_FORMATS);
        if (this.runFFMpegCommand(arguments) == false) {
            throw new Exception(FFMPEG + " failed to run! ADDITIONAL INFO = "
                    + this.ffmpegProcess.getStderr());
        }

        this.ffmpegSupportedFormats = new ArrayList<>();
        boolean inHeader = true;
        /**
         * -- indicates the end of the header information printed by ffmpeg when
         * requesting a list of the supported formats
         */
        final String headerEnd = "--";
        /**
         * @{link RunProcess} merges all of the output into a single
         *        {@link String}. So, we need to split it apart and look at
         *        individual lines.
         */
        for (String output : this.ffmpegProcess.getStdout().split("\n")) {
            output = output.trim(); // ffmpeg adds additional padding to the
                                    // output
            if (inHeader) {
                if (headerEnd.equals(output)) {
                    /*
                     * we have found the end of the header output.
                     */
                    inHeader = false;
                }
            } else {
                /*
                 * Evaluate the formats that have been output.
                 */
                if (output.startsWith(DECODE_ENCODE_SUPPORTED) == false) {
                    /*
                     * both decoding and encoding are not supported.
                     */
                    continue;
                }
                /*
                 * extract the format identifier.
                 */
                // first, eliminate DECODE_ENCODE_SUPPORTED at the beginning of
                // the output string
                output = output.substring(2).trim();
                // next is the format identifier followed by a variable number
                // of spaces and descriptive text. extract the format
                // identifier.
                this.ffmpegSupportedFormats.add(output.split("\\s+")[0]);
            }
        }
    }

    /**
     * Verify that the installed ffmpeg on the current system is capable of
     * working with the specified format. The failure to recognize the specified
     * format may indicate the codecs and/or other external libraries are
     * missing.
     * 
     * @param ffmpegFormat
     *            the specified format. Note: this format String must correspond
     *            to the way that ffmpeg identifies the format. Run: ffmpeg
     *            -format to see the identifiers associated with each format
     *            that ffmpeg recognizes.
     * @return true, if the format is supported; false, otherwise
     */
    protected boolean verifyFFMpegFormatSupport(final String ffmpegFormat)
            throws ConversionNotSupportedException {
        if (this.ffmpegSupportedFormats == null
                || this.ffmpegSupportedFormats.isEmpty()) {
            /* build the list of supported formats. */
            try {
                this.getFFMPegSupportedFormats();
            } catch (Exception e) {
                throw new ConversionNotSupportedException(
                        this.getOutputFormat(),
                        "Failed to determine the formats supported by ffmpeg!",
                        e);
            }
        }

        return this.ffmpegSupportedFormats.contains(ffmpegFormat);
    }

    public final byte[] convertAudio(final byte[] src, BMHAudioFormat srcFormat)
            throws AudioConversionException, UnsupportedAudioFormatException {
        super.verifySupportedAudioFormat(srcFormat);

        // need to create temporary files for the audio input and output.
        Path inputPath = null;
        Path outputPath = null;
        try {
            // create the temporary files.
            inputPath = Files.createTempFile(null, srcFormat.getExtension());
            outputPath = Files.createTempFile(null, this.getOutputFormat()
                    .getExtension());

            // get the command line arguments for the output format.
            final List<String> args = this.getFFMpegArgs(inputPath, outputPath);
            if (this.runFFMpegCommand(args) == false) {
                // ffmpeg failed.
                throw new Exception(FFMPEG
                        + " failed to run! ADDITIONAL INFO = "
                        + this.ffmpegProcess.getStderr());
            }

            // ffmpeg always writes to stderr
            statusHandler.info("The audio conversion was successful: "
                    + this.ffmpegProcess.getStderr());

            // ffmpeg was successful. read the generated audio.
            return Files.readAllBytes(outputPath);
        } catch (Exception e) {
            throw new AudioConversionException("Failed to convert the "
                    + srcFormat.toString() + " audio to "
                    + this.getOutputFormat().toString() + " audio!", e);
        } finally {
            // purge the temporary files if they exist.
            if (inputPath != null) {
                try {
                    Files.deleteIfExists(inputPath);
                } catch (IOException e) {
                    statusHandler
                            .warn("Failed to delete the temporary audio input file: "
                                    + inputPath.toString() + ".");
                }
            }
            if (outputPath != null) {
                try {
                    Files.deleteIfExists(outputPath);
                } catch (IOException e) {
                    statusHandler
                            .warn("Failed to delete the temporary audio input file: "
                                    + outputPath.toString() + ".");
                }
            }
        }
    }

    /**
     * Builds a list of arguments specific to the desired output format that
     * will be used to run ffmpeg.
     * 
     * @param inputFile
     *            input file {@link Path}. Note: the file is only a temporary
     *            file that will be eliminated after use.
     * @param outputFile
     *            output file {@link Path}. Note: the file is only a temporary
     *            file that will be eliminated after use.
     * @return a {@link List} of arguments for ffmpeg
     */
    protected abstract List<String> getFFMpegArgs(final Path inputFile,
            final Path outputFile);

    /*
     * ffmpeg -ar 8000 -f mulaw -i DENADRBOU_ENG_Paul_751_192220.ulaw -ar 44100
     * -ab 160k -y test_conv.mp3
     */

    /**
     * In certain cases, the existence of ffmpeg will not be enough to verify
     * that the converter will successfully run on the current machine. It may
     * also be necessary to verify that the required codecs have been installed
     * so that ffmpeg will actually recognize the target conversion
     * {@link BMHAudioFormat}.
     * 
     * if audio conversion is not supported for the associated
     * {@link BMHAudioFormat} on the current machine.
     */
    protected abstract void verifyFFMpegRecognizedFormat()
            throws ConversionNotSupportedException;
}