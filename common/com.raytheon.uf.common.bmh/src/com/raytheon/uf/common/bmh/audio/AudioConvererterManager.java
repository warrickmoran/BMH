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

import java.util.Map;
import java.util.HashMap;

import com.raytheon.uf.common.bmh.audio.impl.Mp3AudioConverter;
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
 * Dec 3, 2014  3880       bkowal      Remove test code. Verify arguments
 *                                     to the convertAudio method.
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
        this.registerConverter(new Mp3AudioConverter());
    }

    /**
     * Registers an audio converter so that it can now be used to convert audio
     * 
     * @param converter
     *            the audio converter to register
     */
    public void registerConverter(IAudioConverter converter) {
        synchronized (this.registeredAudioConverters) {
            /**
             * verify that the audio conversion is supported on this machine.
             */
            try {
                converter.verifyCompatibility();
            } catch (ConversionNotSupportedException e) {
                statusHandler.error(
                        "Failed to register an audio converter for the "
                                + converter.getOutputFormat().toString()
                                + " format.", e);
            }

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

        if (source == null) {
            throw new IllegalArgumentException(
                    "Required argument source can not be NULL.");
        }
        if (sourceFormat == null) {
            throw new IllegalArgumentException(
                    "Required argument sourceFormat can not be NULL.");
        }
        if (destinationFormat == null) {
            throw new IllegalArgumentException(
                    "Required argument destinationFormat can not be NULL.");
        }

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