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
package com.raytheon.uf.edex.bmh.test.tts;

import java.io.File;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConversionException;
import org.apache.commons.io.FilenameUtils;

import com.raytheon.uf.common.bmh.datamodel.language.Language;
import com.raytheon.uf.common.bmh.datamodel.language.TtsVoice;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg;
import com.raytheon.uf.common.bmh.datamodel.msg.InputMessage;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.edex.bmh.dao.TransmitterGroupDao;
import com.raytheon.uf.edex.bmh.dao.TtsVoiceDao;
import com.raytheon.uf.edex.bmh.test.AbstractWavFileGeneratingTest;
import com.raytheon.uf.edex.bmh.test.TestDataUtil;
import com.raytheon.uf.edex.bmh.test.TestProcessingFailedException;

/**
 * NOT OPERATIONAL CODE! Created to test the TTS Manager. Refer to the data/tts
 * directory in this plugin for a template input properties file.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 11, 2014 3228       bkowal      Initial creation
 * Jun 23, 2014 3304       bkowal      Re-factored
 * Jun 24, 2014 3302       bkowal      Updated to use the BroadcastMsg Entity.
 * Jul 1, 2014  3302       bkowal      Updated to use the db when testing.
 * Jul 7, 2014  3302       bkowal      Re-factor: Use the Test Data Util, ...
 * Jul 17, 2014 3383       bkowal      Updated to use the Audio Conversion API.
 * Jul 23, 2014 3424       bkowal      Will now optionally adjust the audio volume
 *                                     post-TTS generation based on the optional
 *                                     'tts.volume.adjust' property.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class TTSManagerTester extends AbstractWavFileGeneratingTest {
    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(TTSManagerTester.class);

    private static final String TEST_NAME = "TTS Manager";

    private static final String TTS_DIRECTORY_INPUT_PROPERTY = "bmh.tts.test.directory.input";

    private static final String TTS_DIRECTORY_OUTPUT_PROPERTY = "bmh.tts.test.directory.output";

    private static final String TTS_VOLUME_PROPERTY = "tts.volume.adjust";

    private static final double DEFAULT_TTS_VOLUME = 1.0;

    private static final String TTS_TEST_AFOS_ID = "TTSTEST1X";

    private static final String TTS_TEST_TRANSMITTER_GROUP = "TTSTESTGROUP";

    private static final String TTS_TEST_PROGRAM_NAME = "TTSTESTPROGRAM";

    private static final TtsVoice DEFAULT_TTS_VOICE = TestDataUtil
            .constructDefaultTtsVoice();

    private final TtsVoiceDao ttsVoiceDao;

    private final TransmitterGroupDao transmitterGroupDao;

    private double configuredTTSVolume;

    private static final class INPUT_PROPERTIES {
        public static final String TTS_AFOS_ID_PROPERTY = "tts.afosid";

        public static final String TTS_MESSAGE_PROPERTY = "tts.message";

        private static final String TTS_VOICE_PROPERTY_PREFIX = "tts.voice.";

        private static final String TTS_VOICE_NUMBER_PROPERTY = TTS_VOICE_PROPERTY_PREFIX
                + "number";

        private static final String TTS_VOICE_NAME_PROPERTY = TTS_VOICE_PROPERTY_PREFIX
                + "name";

        private static final String TTS_VOICE_LANGUAGE_PROPERTY = TTS_VOICE_PROPERTY_PREFIX
                + "language";

        private static final String TTS_VOICE_MALE_PROPERTY = TTS_VOICE_PROPERTY_PREFIX
                + "male";
    }

    public TTSManagerTester() {
        super(statusHandler, TEST_NAME, TTS_DIRECTORY_INPUT_PROPERTY,
                TTS_DIRECTORY_OUTPUT_PROPERTY);
        this.ttsVoiceDao = new TtsVoiceDao();
        this.transmitterGroupDao = new TransmitterGroupDao();

        String ttsVolume = System.getProperty(TTS_VOLUME_PROPERTY, null);
        if (ttsVolume == null) {
            this.configuredTTSVolume = DEFAULT_TTS_VOLUME;
        }

        try {
            this.configuredTTSVolume = Double.parseDouble(ttsVolume);
        } catch (NumberFormatException e) {
            statusHandler
                    .info("An invalid quantity has been specified for the TTS Volume Adjustment: "
                            + ttsVolume + "! Using the default ...");
            this.configuredTTSVolume = DEFAULT_TTS_VOLUME;
        }
        statusHandler.info("TTS Volume Adjustment = "
                + this.configuredTTSVolume);
    }

    @Override
    protected Object processInput(Configuration configuration,
            final String inputFileName) throws TestProcessingFailedException {
        String ssmlMessage = super.getStringProperty(configuration,
                INPUT_PROPERTIES.TTS_MESSAGE_PROPERTY, inputFileName);

        String afosid = null;
        try {
            afosid = super.getStringProperty(configuration,
                    INPUT_PROPERTIES.TTS_AFOS_ID_PROPERTY, inputFileName);
        } catch (TestProcessingFailedException e) {
            statusHandler.info("The " + INPUT_PROPERTIES.TTS_AFOS_ID_PROPERTY
                    + " property has not been set in input file: "
                    + inputFileName + "! Generating a default afos id.");
            afosid = TTS_TEST_AFOS_ID;
        }

        TtsVoice voice = this.buildVoiceFromInput(configuration, inputFileName);
        /*
         * Verify that the voice this test case will use is present in the
         * database.
         */
        if (this.ttsVoiceDao.getByID(voice.getVoiceNumber()) == null) {
            /*
             * Create a record for the specified test voice.
             */
            TestDataUtil.persistTtsVoice(voice);
            statusHandler.info("Created Test Tts Voice with id: "
                    + voice.getVoiceNumber());
        } else {
            statusHandler.info("Using existing Test Tts Voice with id: "
                    + voice.getVoiceNumber());
        }
        /*
         * Create an InputMessage to associate with the test record.
         * 
         * Only setting the minimum required fields for the purposes of this
         * test.
         */
        InputMessage inputMessage = TestDataUtil
                .checkForExistingTestInputMessage(afosid);
        if (inputMessage == null) {
            /*
             * Create a new InputMessage for the purposes of this test.
             */
            inputMessage = TestDataUtil.persistInputMessage(afosid, null);
            statusHandler.info("Created Test Input Message with id: "
                    + inputMessage.getId());
        } else {
            statusHandler.info("Using existing Test Input Message with id: "
                    + inputMessage.getId());
        }
        /*
         * Determine if there is an existing transmitter group that can be used.
         */
        TransmitterGroup transmitterGroup = this
                .checkForExistingTransmitterGroup();
        if (transmitterGroup == null) {
            transmitterGroup = TestDataUtil.persistTransmitterGroup(
                    TTS_TEST_TRANSMITTER_GROUP, TTS_TEST_PROGRAM_NAME, null);
            statusHandler.info("Created Test Transmitter Group with id: "
                    + transmitterGroup.getName());
        } else {
            statusHandler.info("Using existing Transmitter Group with id: "
                    + transmitterGroup.getName());
        }

        BroadcastMsg message = new BroadcastMsg();
        message.setInputMessage(inputMessage);
        message.setTransmitterGroup(transmitterGroup);
        message.setSsml(ssmlMessage);
        message.setVoice(voice);

        return message;
    }

    private TransmitterGroup checkForExistingTransmitterGroup() {
        List<?> results = this.transmitterGroupDao.loadAll();

        if (results == null || results.isEmpty()) {
            return null;
        }

        /*
         * Do not care which Transmitter Group is specified. Just need to create
         * the relation.
         */
        Object result = results.get(0);
        if (result instanceof TransmitterGroup) {
            return (TransmitterGroup) result;
        }

        return null;
    }

    /**
     * Converts the output file specified in the provided Broadcast Message to
     * wav file format. The wav file is written to the configurable output
     * directory.
     * 
     * @param message
     *            the provided Broadcast Message
     */
    public void convertToWav(BroadcastMsg message) {
        if (message == null) {
            statusHandler
                    .handle(Priority.PROBLEM,
                            "Receieved an uninitialized or incomplete Broadcast Message to process!");
            return;
        }

        final long messageID = message.getId();

        statusHandler.info("Processing message: " + messageID);

        if (message.isSuccess() == false) {
            statusHandler
                    .error("TTS Conversion was unsuccessful; skipping message ["
                            + messageID + "]!");
            return;
        }

        if (message.getOutputName() == null
                || message.getOutputName().trim().isEmpty()) {
            statusHandler
                    .error("Output file name has not been set on the message; skipping message ["
                            + messageID + "]!");
            return;
        }

        File outputUlawFile = new File(message.getOutputName().trim());
        if (outputUlawFile.exists() == false) {
            statusHandler.error("Specified output file: "
                    + outputUlawFile.getAbsolutePath()
                    + " does not exist; skipping message [" + messageID + "]!");
            return;
        }

        /* Get the name of the file, itself. */
        String filename = FilenameUtils.getBaseName(outputUlawFile
                .getAbsolutePath());

        boolean success = super.writeWavData(outputUlawFile, filename);

        if (success) {
            statusHandler.info("Successfully processed message: " + messageID
                    + ".");
        } else {
            statusHandler
                    .error("Failed to process message: " + messageID + "!");
        }
    }

    /**
     * Attempts to build a TtsVoice using the configuration information provided
     * in the Input properties file.
     * 
     * @param configuration
     *            the configuration information
     * @param configurationFilePath
     *            the location of the Input properties file
     * @return the TtsVoice that is built; otherwise, the default tts voice if
     *         incomplete or insufficient information has been provided in the
     *         configuration
     */
    private TtsVoice buildVoiceFromInput(Configuration configuration,
            final String configurationFilePath) {
        Integer voiceNumber = null;
        String voiceName = null;
        String languageIdentifier = null;
        Boolean male = false;

        /* Attempt to retrieve the expected properties. */
        try {
            voiceNumber = configuration.getInteger(
                    INPUT_PROPERTIES.TTS_VOICE_NUMBER_PROPERTY, null);
        } catch (ConversionException e) {
            statusHandler.warn("Invalid value specified for property "
                    + INPUT_PROPERTIES.TTS_VOICE_NUMBER_PROPERTY
                    + " in input file: " + configurationFilePath
                    + "! Using default voice.", e.getLocalizedMessage());
            return DEFAULT_TTS_VOICE;
        }

        try {
            voiceName = configuration.getString(
                    INPUT_PROPERTIES.TTS_VOICE_NAME_PROPERTY, null);
        } catch (ConversionException e) {
            statusHandler.warn("Invalid value specified for property "
                    + INPUT_PROPERTIES.TTS_VOICE_NAME_PROPERTY
                    + " in input file: " + configurationFilePath
                    + "! Using default voice.", e.getLocalizedMessage());
            return DEFAULT_TTS_VOICE;
        }

        try {
            languageIdentifier = configuration.getString(
                    INPUT_PROPERTIES.TTS_VOICE_LANGUAGE_PROPERTY, null);
        } catch (ConversionException e) {
            statusHandler.warn("Invalid value specified for property "
                    + INPUT_PROPERTIES.TTS_VOICE_LANGUAGE_PROPERTY
                    + " in input file: " + configurationFilePath
                    + "! Using default voice.", e.getLocalizedMessage());
            return DEFAULT_TTS_VOICE;
        }

        try {
            male = configuration.getBoolean(
                    INPUT_PROPERTIES.TTS_VOICE_MALE_PROPERTY, null);
        } catch (ConversionException e) {
            statusHandler.warn("Invalid value specified for property "
                    + INPUT_PROPERTIES.TTS_VOICE_MALE_PROPERTY
                    + " in input file: " + configurationFilePath
                    + "! Using default voice.", e.getLocalizedMessage());
            return DEFAULT_TTS_VOICE;
        }

        /* Verify that language identifier has been set. */
        if (languageIdentifier == null) {
            statusHandler.info("The "
                    + INPUT_PROPERTIES.TTS_VOICE_LANGUAGE_PROPERTY
                    + " property has not been set in input file: "
                    + configurationFilePath + "! Using default voice.");
            return DEFAULT_TTS_VOICE;
        }

        Language language = null;
        /* Attempt to map the language identifier to a Language. */
        for (Language languageValue : Language.values()) {
            if (languageIdentifier.equals(languageValue.getIdentifier())) {
                language = languageValue;
                break;
            }
        }

        /* Verify that the specified identifier actually maps to a Language. */
        if (language == null) {
            statusHandler
                    .info("The specified language identifier: "
                            + languageIdentifier
                            + " in input file: "
                            + configurationFilePath
                            + " does not map to a valid language! Using default voice.");
            return DEFAULT_TTS_VOICE;
        }

        /* Verify that the other required properties have been set. */
        if (voiceNumber == null || voiceName == null || male == null) {
            statusHandler
                    .info("All required required language properties have not been set in input file: "
                            + configurationFilePath + "! Using default voice.");
            return DEFAULT_TTS_VOICE;
        }

        /*
         * Sufficient information has been supplied. Build and return the
         * user-specified voice.
         */
        TtsVoice voice = new TtsVoice();
        voice.setVoiceNumber(voiceNumber);
        voice.setVoiceName(voiceName);
        voice.setLanguage(language);
        voice.setMale(male);

        return voice;
    }
}