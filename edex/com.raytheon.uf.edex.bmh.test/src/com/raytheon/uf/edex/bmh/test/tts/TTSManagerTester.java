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
import java.io.FileInputStream;
import java.util.UUID;

import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConversionException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FilenameUtils;

import com.raytheon.uf.common.bmh.datamodel.language.Language;
import com.raytheon.uf.common.bmh.datamodel.language.TtsVoice;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.edex.bmh.msg.BroadcastMsg;
import com.raytheon.uf.edex.bmh.msg.BroadcastMsgBody;
import com.raytheon.uf.edex.bmh.msg.BroadcastMsgHeader;

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
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class TTSManagerTester {
    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(TTSManagerTester.class);

    private static final String DEFAULT_OUTPUT_DIRECTORY = File.separatorChar
            + "tmp";

    private static final String TTS_DIRECTORY_INPUT_PROPERTY = "bmh.tts.test.directory.input";

    private static final String TTS_DIRECTORY_OUTPUT_PROPERTY = "bmh.tts.test.directory.output";

    private static final String WAV_FILE_EXTENSION = ".wav";

    private static final TtsVoice DEFAULT_TTS_VOICE = constructDefaultTtsVoice();

    private String ttsOutputDirectory;

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

    public void initialize() {
        statusHandler.info("Initializing the TTS Manager Tester ...");

        String ttsInputDirectory = System
                .getProperty(TTS_DIRECTORY_INPUT_PROPERTY);
        this.ttsOutputDirectory = System
                .getProperty(TTS_DIRECTORY_OUTPUT_PROPERTY);

        if (ttsInputDirectory == null) {
            /*
             * If the input directory has not been set, do not continue. This
             * will eventually cause problems when the Spring Container attempts
             * to use the configuration property directly.
             */
            statusHandler
                    .error("Failed to retrieve the Test Input Directory from the configuration. Spring Container Crash Expected!");
            return;
        }

        File inputDirectoryFile = new File(ttsInputDirectory);
        /* Attempt to create the specified input directory if it does not exist. */
        if (inputDirectoryFile.exists() == false) {
            statusHandler.info("Attempting to create Test Input Directory: "
                    + ttsInputDirectory + " ...");
            /*
             * In this case, we do not care if the creation was successful or
             * not because the directory will eventually need to exist in order
             * to complete testing.
             */
            inputDirectoryFile.mkdirs();
        }

        if (this.ttsOutputDirectory == null) {
            statusHandler
                    .warn("Failed to retrieve the Test Output Directory from the configuration. Using the default output directory: "
                            + DEFAULT_OUTPUT_DIRECTORY);
            this.ttsOutputDirectory = DEFAULT_OUTPUT_DIRECTORY;
        } else {
            File outputDirectoryFile = new File(this.ttsOutputDirectory);
            /*
             * Attempt to create the specified output directory if it does not
             * exist.
             */
            if (outputDirectoryFile.exists() == false) {
                statusHandler
                        .info("Attempting to create Test Output Directory: "
                                + this.ttsOutputDirectory + " ...");
                if (outputDirectoryFile.mkdirs() == false) {
                    statusHandler
                            .warn("Failed to create Test Output Directory. Using the default output directory: "
                                    + DEFAULT_OUTPUT_DIRECTORY);
                    this.ttsOutputDirectory = DEFAULT_OUTPUT_DIRECTORY;
                }
            }
        }

        statusHandler.info("Test Input Directory = " + ttsInputDirectory);
        statusHandler
                .info("Test Output Directory = " + this.ttsOutputDirectory);
    }

    /**
     * Processes the input test file
     * 
     * @param testFile
     *            the input test file
     * @return the BroadcastMsg that is constructed
     * @throws Exception
     *             if a BroadcastMsg was not successfully constructed based on
     *             the input test file.
     */
    public BroadcastMsg process(File testFile) throws Exception {
        BroadcastMsg message = this.generateTestBroadcastMessage(testFile);

        if (message != null) {
            statusHandler
                    .info("Successfully generated a Broadcast Message with id: "
                            + message.getId()
                            + " based on input file: "
                            + testFile.getAbsolutePath());
            return message;
        }

        /* Manually delete the input file on failure. */
        testFile.delete();

        throw new Exception(
                "Failed to generate a broadcast message based on input file: "
                        + testFile.getAbsolutePath() + "!");
    }

    /**
     * Attempts to generate a broadcast message using information provided in a
     * simple Java properties file.
     * 
     * @param testFile
     *            the properties file
     * @return the constructed BroadcastMsg object or NULL if construction fails
     */
    private BroadcastMsg generateTestBroadcastMessage(File testFile) {
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

        String ssmlMessage = null;
        String afosID = null;

        try {
            ssmlMessage = configuration.getString(
                    INPUT_PROPERTIES.TTS_MESSAGE_PROPERTY, null);
            if (ssmlMessage == null) {
                statusHandler.error("Invalid input file: "
                        + testFile.getAbsolutePath() + "! The "
                        + INPUT_PROPERTIES.TTS_MESSAGE_PROPERTY
                        + " property must be set!");
                return null;
            }
        } catch (ConversionException e) {
            statusHandler.error("Invalid value specified for property "
                    + INPUT_PROPERTIES.TTS_MESSAGE_PROPERTY
                    + " in input file: " + testFile.getAbsolutePath() + "!", e);
            return null;
        }

        try {
            afosID = configuration.getString(
                    INPUT_PROPERTIES.TTS_AFOS_ID_PROPERTY, null);
            if (afosID == null) {
                statusHandler.info("The "
                        + INPUT_PROPERTIES.TTS_AFOS_ID_PROPERTY
                        + " property has not been set in input file: "
                        + testFile.getAbsolutePath()
                        + "! Generating a default afos id.");
                afosID = String.valueOf(System.currentTimeMillis());
            }
        } catch (ConversionException e) {
            statusHandler.warn("Invalid value specified for property "
                    + INPUT_PROPERTIES.TTS_AFOS_ID_PROPERTY
                    + " in input file: " + testFile.getAbsolutePath()
                    + "! Generating a default afos id.",
                    e.getLocalizedMessage());
            afosID = String.valueOf(System.currentTimeMillis());
        }

        TtsVoice voice = this.buildVoiceFromInput(configuration,
                testFile.getAbsolutePath());

        BroadcastMsg message = new BroadcastMsg(UUID.randomUUID().toString());
        message.setHeader(new BroadcastMsgHeader());
        BroadcastMsgBody messageBody = new BroadcastMsgBody();
        messageBody.setAfosID(afosID);
        messageBody.setSsml(ssmlMessage);
        messageBody.setVoice(voice);
        message.setBody(messageBody);

        statusHandler.info("Successfully processed input file: "
                + testFile.getAbsolutePath() + "!");

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

        /* Construct the full name of the output file. */
        String fullWavFileName = this.ttsOutputDirectory + File.separatorChar
                + filename + WAV_FILE_EXTENSION;
        File outputWavFile = new File(fullWavFileName);

        long fileSize = outputUlawFile.length();
        int frameSize = 160;
        long numFrames = fileSize / frameSize;

        AudioFormat audioFormat = new AudioFormat(Encoding.ULAW, 8000, 8, 1,
                frameSize, 50, true);
        boolean success = true;
        try {
            AudioInputStream audioInputStream = new AudioInputStream(
                    new FileInputStream(outputUlawFile), audioFormat, numFrames);
            AudioSystem.write(audioInputStream, Type.WAVE, outputWavFile);
        } catch (Exception e) {
            statusHandler.error("Failed to write wav file: " + fullWavFileName
                    + "!", e);
            success = false;
        }

        if (success) {
            statusHandler.info("Successfully wrote wav file: "
                    + fullWavFileName);
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