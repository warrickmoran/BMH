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
package com.raytheon.uf.edex.bmh.test;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;

import org.apache.commons.io.FileUtils;

import com.raytheon.uf.common.bmh.audio.BMHAudioFormat;
import com.raytheon.uf.common.bmh.audio.AudioConversionException;
import com.raytheon.uf.common.bmh.audio.UnsupportedAudioFormatException;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.edex.bmh.audio.EdexAudioConverterManager;

/**
 * Abstract representation of a test capability that is capable of producing a
 * WAV file as the final output.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 23, 2014 3304       bkowal      Initial creation
 * Jul 1, 2014  3302       bkowal      Improved Exception Handling.
 * Jul 8, 2014  3302       bkowal      Re-factor. Abstract common functionality
 *                                     into a new parent class.
 * Jul 17, 2014 3383       bkowal      Updated to use the Audio Conversion API.
 * Dec 3, 2014  3880       bkowal      Deprecated.
 * Feb 09, 2015 4091       bkowal      Use {@link EdexAudioConverterManager}.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */
@Deprecated
public abstract class AbstractWavFileGeneratingTest extends AbstractBMHTester {
    private static final String DEFAULT_OUTPUT_DIRECTORY = File.separatorChar
            + "tmp";

    private static final String WAV_FILE_EXTENSION = ".wav";

    private final String inputDirectoryProperty;

    private final String outputDirectoryProperty;

    private String outputDirectory;

    protected AbstractWavFileGeneratingTest(IUFStatusHandler statusHandler,
            String name, String inputDirectoryProperty,
            String outputDirectoryProperty) {
        super(statusHandler, name);
        this.inputDirectoryProperty = inputDirectoryProperty;
        this.outputDirectoryProperty = outputDirectoryProperty;
    }

    @Override
    public void initialize() {
        statusHandler.info("Initializing the " + this.name + " Tester ...");

        if (this.validateTestInputs(this.inputDirectoryProperty) == false) {
            return;
        }
        this.outputDirectory = System.getProperty(this.outputDirectoryProperty,
                null);

        if (this.outputDirectory == null) {
            statusHandler
                    .warn("Failed to retrieve the Test Output Directory from the configuration. Using the default output directory: "
                            + DEFAULT_OUTPUT_DIRECTORY);
            this.outputDirectory = DEFAULT_OUTPUT_DIRECTORY;
        } else {
            File outputDirectoryFile = new File(this.outputDirectory);
            /*
             * Attempt to create the specified output directory if it does not
             * exist.
             */
            if (outputDirectoryFile.exists() == false) {
                statusHandler
                        .info("Attempting to create Test Output Directory: "
                                + this.outputDirectory + " ...");
                if (outputDirectoryFile.mkdirs() == false) {
                    statusHandler
                            .warn("Failed to create Test Output Directory. Using the default output directory: "
                                    + DEFAULT_OUTPUT_DIRECTORY);
                    this.outputDirectory = DEFAULT_OUTPUT_DIRECTORY;
                }
            }
        }

        statusHandler.info("Test Output Directory = " + this.outputDirectory);
    }

    protected boolean writeWavData(BMHAudioFormat format, byte[] audioData,
            final String outputFileName) {
        File outputWavFile = new File(this.buildOutputFilePath(outputFileName));

        try {
            byte[] destination = EdexAudioConverterManager.getInstance()
                    .convertAudio(audioData, format, BMHAudioFormat.WAV);

            FileUtils.writeByteArrayToFile(outputWavFile, destination);
            statusHandler.info("Successfully wrote wav file: "
                    + outputWavFile.getAbsolutePath());
        } catch (IOException | UnsupportedAudioFormatException
                | AudioConversionException e) {
            statusHandler.error("Failed to create the wav file: "
                    + outputWavFile.getAbsolutePath(), e);
            return false;
        }

        return true;
    }

    protected boolean writeWavData(File sourceFile, final String outputFileName) {
        return false;
    }

    private String buildOutputFilePath(final String outputFileName) {
        return FileSystems
                .getDefault()
                .getPath(this.outputDirectory,
                        outputFileName + WAV_FILE_EXTENSION).toString();
    }
}