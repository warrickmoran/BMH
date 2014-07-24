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
package com.raytheon.uf.common.bmh.audio;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.HashMap;

import org.apache.commons.io.FilenameUtils;

import com.raytheon.uf.common.bmh.audio.impl.PcmAudioConverter;
import com.raytheon.uf.common.bmh.audio.impl.UlawAudioConverter;
import com.raytheon.uf.common.bmh.audio.impl.WavAudioConverter;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

/**
 * Keeps track of the registered audio converters and the supported audio types.
 * Used to invoke the audio converters to complete audio conversion.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 15, 2014 3383       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class AudioConvererterManager {
    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(AudioConvererterManager.class);

    private static final AudioConvererterManager instance = new AudioConvererterManager();

    private Map<BMHAudioFormat, IAudioConverter> registeredAudioConverters;

    /**
     * Constructor
     */
    protected AudioConvererterManager() {
        this.registeredAudioConverters = new HashMap<>();
        this.initialize();
        statusHandler.info("Initialization Complete!");
    }

    /**
     * Returns an instance of the Audio Converter Manager
     * 
     * @return an instance of the Audio Converter Manager
     */
    public static AudioConvererterManager getInstance() {
        return instance;
    }

    /**
     * Initializes the Audio Converter Manager
     */
    private void initialize() {
        statusHandler.info("Initializing the Audio Converter Manager ...");
        /* Register the audio conversions that are natively supported by Java. */

        // Presently, there is no perceived benefit from allowing and using a
        // spring-based registration process. If other plugins and/or projects
        // that would be dependent on BMH want to contribute audio converters,
        // then a spring-based registration process would be beneficial.
        this.registerConverter(new WavAudioConverter());
        this.registerConverter(new UlawAudioConverter());
        this.registerConverter(new PcmAudioConverter());
    }

    /**
     * Registers an audio converter so that it can now be used to convert audio
     * 
     * @param converter
     *            the audio converter to register
     */
    public void registerConverter(IAudioConverter converter) {
        synchronized (this.registeredAudioConverters) {
            if (this.registeredAudioConverters.containsKey(converter
                    .getOutputFormat())) {
                // warn and replace the previously registered converter.
                statusHandler
                        .warn("A converter has already been registered for the: "
                                + converter.getOutputFormat().toString()
                                + " audio type! The converter that was previously registered will be replaced.");
            }
            this.registeredAudioConverters.put(converter.getOutputFormat(),
                    converter);
            statusHandler
                    .info("Successfully registered an audio converter for the "
                            + converter.getOutputFormat().toString()
                            + " format.");
        }
    }

    /**
     * Uses an audio converter associated with the specified desired format to
     * generate and return the raw converted audio
     * 
     * @param source
     *            the raw source audio bytes
     * @param sourceFormat
     *            the current audio format
     * @param destinationFormat
     *            the desired audio format
     * @return the raw converted audio
     * @throws UnsupportedAudioFormatException
     *             when an unrecognized and/or unsupported audio format is
     *             encountered
     * @throws AudioConversionException
     *             when the audio conversion fails
     */
    public byte[] convertAudio(byte[] source, BMHAudioFormat sourceFormat,
            BMHAudioFormat destinationFormat)
            throws UnsupportedAudioFormatException, AudioConversionException {
        /* Does a converter exist for the output audio type? */
        IAudioConverter converter = this.registeredAudioConverters
                .get(destinationFormat);
        if (converter == null) {
            throw new AudioConversionException(
                    "No audio converter has been registered for the "
                            + destinationFormat.toString() + " audio format!");
        }

        /* Does the converter support the provided input audio type? */
        converter.verifySupportedAudioFormat(sourceFormat);

        return converter.convertAudio(source, sourceFormat);
    }

    /**
     * Performs the audio conversion provided that the necessary converter has
     * been registered.
     * 
     * @param sourceFile
     *            the audio file to convert
     * @param destinationFile
     *            the file to write the converted audio to
     * @throws UnsupportedAudioFormatException
     *             when an unrecognized and/or unsupported audio format is
     *             encountered
     * @throws AudioConversionException
     *             when the audio conversion fails
     */
    public void convertAudio(File sourceFile, File destinationFile)
            throws UnsupportedAudioFormatException, AudioConversionException {
        /* Verify that the source file exists. */
        if (sourceFile.exists() == false) {
            throw new AudioConversionException("The specified source file: "
                    + sourceFile.getAbsolutePath() + " does not exist!");
        }

        /* Determine the location of the destination directory. */
        final File destinationDirectory = new File(
                FilenameUtils.getFullPath(destinationFile.getAbsolutePath()));
        /* Verify that the specified destination exists. */
        if (destinationDirectory.exists() == false) {
            /* Attempt to create the destination directory. */
            if (destinationDirectory.mkdirs() == false) {
                throw new AudioConversionException(
                        "The specified destination directory: "
                                + destinationDirectory.getAbsolutePath()
                                + " does not exist. Attempts to create the directory have failed!");
            }
        }

        /* Determine the input audio type. */
        final String inputExtension = FilenameUtils.getExtension(sourceFile
                .getAbsolutePath());

        /* Determine the output audio type. */
        final String outputExtension = FilenameUtils
                .getExtension(destinationFile.getAbsolutePath());

        /* Is the input audio type supported? */
        final BMHAudioFormat inputFormat = BMHAudioFormat
                .lookupByExtension(inputExtension);
        if (inputFormat == null) {
            throw new UnsupportedAudioFormatException(inputExtension);
        }

        /* Is the output audio type supported? */
        final BMHAudioFormat outputFormat = BMHAudioFormat
                .lookupByExtension(outputExtension);
        if (outputFormat == null) {
            throw new UnsupportedAudioFormatException(outputExtension);
        }

        final Path sourceFilePath = FileSystems.getDefault().getPath(
                sourceFile.getAbsolutePath());

        /* Read the input bytes. */
        byte[] source = null;
        try {
            source = Files.readAllBytes(sourceFilePath);
        } catch (IOException e) {
            throw new AudioConversionException(
                    "Failed to read the source file: "
                            + sourceFile.getAbsolutePath() + "!", e);
        }

        byte[] destination = this.convertAudio(source, inputFormat,
                outputFormat);
        try {
            Files.write(
                    FileSystems.getDefault().getPath(
                            destinationFile.getAbsolutePath()), destination,
                    StandardOpenOption.CREATE);
        } catch (IOException e) {
            throw new AudioConversionException(
                    "Failed to write the destination file: "
                            + destinationFile.getAbsolutePath() + "!", e);
        }

        statusHandler.info("Audio conversion from format "
                + inputFormat.toString() + " to format "
                + outputFormat.toString() + " was successful!");
    }

    /**
     * Returns the supported audio formats based on the converters that have
     * been registered.
     * 
     * @return the supported audio formats.
     */
    public BMHAudioFormat[] getSupportedFormats() {
        synchronized (this.registeredAudioConverters) {
            return this.registeredAudioConverters.keySet().toArray(
                    new BMHAudioFormat[0]);
        }
    }
}