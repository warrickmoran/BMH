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

import java.io.ByteArrayInputStream;
import java.io.File;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.AudioFormat.Encoding;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConversionException;
import org.apache.commons.configuration.PropertiesConfiguration;

import com.raytheon.uf.common.status.IUFStatusHandler;

/**
 * Abstract representation of a test capability that is capable of producing
 * a WAV file as the final output.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 23, 2014 3304       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public abstract class AbstractWavFileGeneratingTest {
    private static final String DEFAULT_OUTPUT_DIRECTORY = File.separatorChar
            + "tmp";

    private static final String WAV_FILE_EXTENSION = ".wav";

    private final IUFStatusHandler statusHandler;

    private final String name;

    private final String inputDirectoryProperty;

    private final String outputDirectoryProperty;

    private String outputDirectory;

    protected AbstractWavFileGeneratingTest(IUFStatusHandler statusHandler,
            String name, String inputDirectoryProperty,
            String outputDirectoryProperty) {
        this.statusHandler = statusHandler;
        this.name = name.trim();
        this.inputDirectoryProperty = inputDirectoryProperty;
        this.outputDirectoryProperty = outputDirectoryProperty;
    }

    public void initialize() {
        statusHandler.info("Initializing the " + this.name + " Tester ...");

        String inputDirectory = System.getProperty(this.inputDirectoryProperty,
                null);
        this.outputDirectory = System.getProperty(this.outputDirectoryProperty,
                null);

        if (inputDirectory == null) {
            /*
             * If the input directory has not been set, do not continue. This
             * will eventually cause problems when the Spring Container attempts
             * to use the configuration property directly.
             */
            statusHandler
                    .error("Failed to retrieve the Test Input Directory from the configuration. Spring Container Crash Expected!");
            return;
        }

        File inputDirectoryFile = new File(inputDirectory);
        /* Attempt to create the specified input directory if it does not exist. */
        if (inputDirectoryFile.exists() == false) {
            statusHandler.info("Attempting to create Test Input Directory: "
                    + inputDirectory + " ...");

            /*
             * In this case, we do not care if the creation was successful or
             * not because the directory will eventually need to exist in order
             * to complete testing.
             */
            inputDirectoryFile.mkdirs();
        }

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

        statusHandler.info("Test Input Directory = " + inputDirectory);
        statusHandler.info("Test Output Directory = " + this.outputDirectory);
    }

    public Object process(File testFile) throws Exception {
        /*
         * Data integrity checks.
         */

        /* Verify a file has been provided and that the file exists. */
        if (testFile == null) {
            statusHandler.error("File cannot be NULL!");
            return null;
        }

        if (testFile.exists() == false) {
            statusHandler.error("The specified file: "
                    + testFile.getAbsolutePath() + " does not exist!");
            return null;
        }

        statusHandler.info("Processing input file: "
                + testFile.getAbsolutePath() + " ...");

        /* Read the file. */
        Configuration configuration = null;
        try {
            configuration = new PropertiesConfiguration(testFile);
        } catch (ConfigurationException e) {
            statusHandler.error("Failed to load the specified file: "
                    + testFile.getAbsolutePath() + "!", e);
            return null;
        }

        Object output = null;
        try {
            output = this.processInput(configuration,
                    testFile.getAbsolutePath());
        } catch (TestProcessingFailedException e) {
            /*
             * Input file processing failed, manually remove the file.
             */
            testFile.delete();

            throw new Exception("Failed to process input file: "
                    + testFile.getAbsolutePath() + "!", e);
        }

        statusHandler.info("Successfully processed input file: "
                + testFile.getAbsolutePath() + ".");
        return output;
    }

    protected abstract Object processInput(Configuration configuration,
            final String inputFileName) throws TestProcessingFailedException;

    protected boolean writeWavData(byte[] audioData, final String outputFileName) {
        /* Build the full path to the output wav file. */
        StringBuilder stringBuilder = new StringBuilder(this.outputDirectory);
        stringBuilder.append(File.separatorChar);
        stringBuilder.append(outputFileName);
        stringBuilder.append(WAV_FILE_EXTENSION);
        File outputWavFile = new File(stringBuilder.toString());

        long fileSize = audioData.length;
        int frameSize = 160;
        long numFrames = fileSize / frameSize;

        AudioFormat audioFormat = new AudioFormat(Encoding.ULAW, 8000, 8, 1,
                frameSize, 8000, false);
        boolean success = true;
        try {
            AudioInputStream audioInputStream = new AudioInputStream(
                    new ByteArrayInputStream(audioData), audioFormat, numFrames);
            AudioSystem.write(audioInputStream, Type.WAVE, outputWavFile);
            statusHandler
                    .info("Successfully wrote wav file: " + outputFileName);
        } catch (Exception e) {
            statusHandler.error("Failed to write wav file: " + outputFileName
                    + "!", e);
            success = false;
        }

        return success;
    }

    protected String getStringProperty(Configuration configuration,
            String propertyName, final String inputFileName)
            throws TestInputPropertyInvalidException,
            TestInputPropertyNotSetException {
        String propertyValue = null;
        try {
            propertyValue = configuration.getString(propertyName, null);
            if (propertyValue == null) {
                throw new TestInputPropertyNotSetException(inputFileName,
                        propertyName);
            }
        } catch (ConversionException e) {
            throw new TestInputPropertyInvalidException(inputFileName,
                    propertyName, e);
        }

        return propertyValue;
    }

    protected Double getDoubleProperty(Configuration configuration,
            String propertyName, final String inputFileName)
            throws TestInputPropertyInvalidException,
            TestInputPropertyNotSetException {
        Double propertyValue = null;
        try {
            propertyValue = configuration.getDouble(propertyName, null);
            if (propertyValue == null) {
                throw new TestInputPropertyNotSetException(inputFileName,
                        propertyName);
            }
        } catch (ConversionException e) {
            throw new TestInputPropertyInvalidException(inputFileName,
                    propertyName, e);
        }

        return propertyValue;
    }
}