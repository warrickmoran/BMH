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
import java.io.IOException;
import java.util.UUID;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConversionException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.raytheon.uf.common.bmh.datamodel.language.Language;
import com.raytheon.uf.common.bmh.datamodel.language.TtsVoice;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.edex.bmh.msg.BroadcastMsg;
import com.raytheon.uf.edex.bmh.msg.BroadcastMsgBody;
import com.raytheon.uf.edex.bmh.msg.BroadcastMsgHeader;
import com.raytheon.uf.edex.bmh.test.AbstractWavFileGeneratingTest;
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

    private static final TtsVoice DEFAULT_TTS_VOICE = constructDefaultTtsVoice();

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

    /**
     * Creates an instance of a TTS Voice based on the NeoSpeech Julie voice.
     * Julie was used based on the current TTS Server setup in Omaha.
     * 
     * @return the TtsVoice that is constructed
     */
    private static TtsVoice constructDefaultTtsVoice() {
        /*
         * This could optionally be configurable via the test properties file.
         */
        TtsVoice voice = new TtsVoice();
        voice.setVoiceNumber(103);
        voice.setVoiceName("Julie");
        voice.setLanguage(Language.ENGLISH);
        voice.setMale(false);

        return voice;
    }

    public TTSManagerTester() {
        super(statusHandler, TEST_NAME, TTS_DIRECTORY_INPUT_PROPERTY,
                TTS_DIRECTORY_OUTPUT_PROPERTY);
    }

    @Override
    protected Object processInput(Configuration configuration,
            final String inputFileName) throws TestProcessingFailedException {
        String ssmlMessage = super.getStringProperty(configuration,
                INPUT_PROPERTIES.TTS_MESSAGE_PROPERTY, inputFileName);
        String afosID = null;
        try {
            super.getStringProperty(configuration,
                    INPUT_PROPERTIES.TTS_AFOS_ID_PROPERTY, inputFileName);
        } catch (TestProcessingFailedException e) {
            statusHandler.info("The " + INPUT_PROPERTIES.TTS_AFOS_ID_PROPERTY
                    + " property has not been set in input file: "
                    + inputFileName + "! Generating a default afos id.");
            afosID = String.valueOf(System.currentTimeMillis());
        }

        TtsVoice voice = this.buildVoiceFromInput(configuration, inputFileName);

        BroadcastMsg message = new BroadcastMsg(UUID.randomUUID().toString());
        message.setHeader(new BroadcastMsgHeader());
        BroadcastMsgBody messageBody = new BroadcastMsgBody();
        messageBody.setAfosID(afosID);
        messageBody.setSsml(ssmlMessage);
        messageBody.setVoice(voice);
        message.setBody(messageBody);

        return message;
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
        if (message == null || message.getBody() == null) {
            statusHandler
                    .handle(Priority.PROBLEM,
                            "Receieved an uninitialized or incomplete Broadcast Message to process!");
            return;
        }

        final String messageID = message.getId();

        statusHandler.info("Processing message: " + messageID);

        if (message.getBody().isSuccess() == false) {
            statusHandler
                    .error("TTS Conversion was unsuccessful; skipping message ["
                            + messageID + "]!");
            return;
        }

        if (message.getBody().getOutputName() == null
                || message.getBody().getOutputName().isEmpty()) {
            statusHandler
                    .error("Output file name has not been set on the message; skipping message ["
                            + messageID + "]!");
            return;
        }

        File outputUlawFile = new File(message.getBody().getOutputName());
        if (outputUlawFile.exists() == false) {
            statusHandler.error("Specified output file: "
                    + outputUlawFile.getAbsolutePath()
                    + " does not exist; skipping message [" + messageID + "]!");
            return;
        }

        /* Get the name of the file, itself. */
        String filename = FilenameUtils.getBaseName(outputUlawFile
                .getAbsolutePath());

        /* Retrieve the contents of the file. */
        byte[] data;
        try {
            data = FileUtils.readFileToByteArray(outputUlawFile);
        } catch (IOException e) {
            statusHandler.error(
                    "Failed to read ulaw file: "
                            + outputUlawFile.getAbsolutePath()
                            + "; skipping message [" + messageID + "]!", e);
            return;
        }
        boolean success = super.writeWavData(data, filename);

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