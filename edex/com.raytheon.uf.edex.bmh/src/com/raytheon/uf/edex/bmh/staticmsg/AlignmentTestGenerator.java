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
package com.raytheon.uf.edex.bmh.staticmsg;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.raytheon.uf.common.bmh.audio.BMHAudioFormat;
import com.raytheon.uf.common.bmh.datamodel.language.TtsVoice;
import com.raytheon.uf.common.bmh.request.TextToSpeechRequest;
import com.raytheon.uf.common.bmh.tones.ToneGenerationException;
import com.raytheon.uf.common.bmh.tones.TonesManager;
import com.raytheon.uf.common.bmh.tones.TonesManager.TransferType;
import com.raytheon.uf.edex.bmh.BMHConfigurationException;
import com.raytheon.uf.edex.bmh.BMHConstants;
import com.raytheon.uf.edex.bmh.dao.TtsVoiceDao;
import com.raytheon.uf.edex.bmh.handler.TextToSpeechHandler;
import com.raytheon.uf.edex.bmh.status.BMHStatusHandler;
import com.raytheon.uf.edex.bmh.status.IBMHStatusHandler;
import com.raytheon.uf.edex.bmh.tts.TTSSynthesisFactory;

/**
 * Generates maintenance audio messages that will be streamed to the dac when
 * invoked by the Transmitter Alignment dialog.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 27, 2014 3630       bkowal      Initial creation
 * Dec 12, 2014 3603       bsteffen    Move MAINTENANCE_DATA_DIRECTORY to BMHConstants
 * Mar 23, 2015 4299       bkowal      Do not add padding to the alignment tones.
 * Jun 08, 2015 4403       bkowal      Updated text content field in {@link TextToSpeechRequest}.
 * Jun 11, 2015 4490       bkowal      Initialized by Spring.
 * Jul 08, 2015 4636       bkowal      Support transfer tone transmitter alignment tests.
 * Jul 13, 2015 4636       bkowal      Create separate alignment test files for 1800 and 2400 Hz
 *                                     transfer tones.
 * May 23, 2016 5667       bkowal      Changed {@link #SAME} to "" instead of a valid SAME String.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class AlignmentTestGenerator {

    private static final IBMHStatusHandler statusHandler = BMHStatusHandler
            .getInstance(AlignmentTestGenerator.class);

    /*
     * The following information is used to generate the audio that will be used
     * for maintenance dac testing.
     * 
     * We can optionally load these from an external, user-editable properties
     * file.
     */

    private static final String TEXT = "This is a DAC Maintenance Test.";

    private static final double ALERT_AMPLITUDE = 8192.0;

    private static final double ALERT_DURATION = 30.0;

    /*
     * The empty string ensures that the generated tones only consist of the
     * preamble.
     */
    private static final String SAME = StringUtils.EMPTY;

    private Path audioMaintenancePath;

    private Path maintenanceTextPath;

    private static final String TEXT_ULAW_NAME = "maintenanceText"
            + BMHAudioFormat.ULAW.getExtension();

    private Path maintenanceAlertPath;

    private static final String ALERT_ULAW_NAME = "maintenanceAlert"
            + BMHAudioFormat.ULAW.getExtension();

    private Path maintenanceSamePath;

    private static final String SAME_ULAW_NAME = "maintenanceSame"
            + BMHAudioFormat.ULAW.getExtension();

    private Path maintenance18TransferPath;

    private static final String TRANSFER_18_ULAW_NAME = "maintenance18Transfer"
            + BMHAudioFormat.ULAW.getExtension();

    private Path maintenance24TransferPath;

    private static final String TRANSFER_24_ULAW_NAME = "maintenance24Transfer"
            + BMHAudioFormat.ULAW.getExtension();

    private final TextToSpeechHandler ttsHandler;

    /* Output root subdirectories */
    private final String bmhDataDirectory;

    private TtsVoiceDao ttsVoiceDao;

    public AlignmentTestGenerator(final TextToSpeechHandler ttsHandler,
            final String bmhDataDirectory) {
        this.ttsHandler = ttsHandler;
        this.bmhDataDirectory = bmhDataDirectory;
    }

    public void initialize() {
        this.validateDaos();

        audioMaintenancePath = Paths.get(this.bmhDataDirectory,
                BMHConstants.AUDIO_DATA_DIRECTORY,
                BMHConstants.MAINTENANCE_DATA_DIRECTORY);

        final String audioMaintenanceDirectory = audioMaintenancePath
                .toString();
        statusHandler.info("BMH Audio Maintenance Directory is: "
                + audioMaintenanceDirectory);

        maintenanceTextPath = Paths.get(audioMaintenanceDirectory,
                TEXT_ULAW_NAME);
        maintenanceAlertPath = Paths.get(audioMaintenanceDirectory,
                ALERT_ULAW_NAME);
        maintenanceSamePath = Paths.get(audioMaintenanceDirectory,
                SAME_ULAW_NAME);
        maintenance18TransferPath = Paths.get(audioMaintenanceDirectory,
                TRANSFER_18_ULAW_NAME);
        maintenance24TransferPath = Paths.get(audioMaintenanceDirectory,
                TRANSFER_24_ULAW_NAME);
    }

    public void process() throws StaticGenerationException,
            BMHConfigurationException {
        /*
         * Generate any maintenance audio that does not exist.
         */
        if (Files.exists(audioMaintenancePath) == false) {
            try {
                Files.createDirectories(audioMaintenancePath);
            } catch (IOException e) {
                throw new BMHConfigurationException(
                        "Failed to create the root audio maintenance directory for pre-synthesized maintenance messages: "
                                + audioMaintenancePath.toString(), e);
            }
        }

        if (Files.exists(maintenanceTextPath) == false) {
            this.generateText();
        }
        if (Files.exists(maintenanceAlertPath) == false) {
            this.generateAlert();
        }
        if (Files.exists(maintenanceSamePath) == false) {
            this.generateSame();
        }
        if (Files.exists(maintenance18TransferPath) == false) {
            this.generateTransfer();
        }
        if (Files.exists(maintenance24TransferPath) == false) {
            this.generateTransfer();
        }
    }

    private void generateText() throws StaticGenerationException {
        statusHandler.info("Generating text maintenance file: "
                + maintenanceTextPath.toString());

        TextToSpeechRequest request = new TextToSpeechRequest();
        try {
            request.setVoice(this.getTtsVoice());
        } catch (BMHConfigurationException e) {
            throw new StaticGenerationException(
                    "Failed to generate the text maintenance audio!", e);
        }
        request.setTimeout(TTSSynthesisFactory.NO_TIMEOUT);
        request.setContent(TEXT);

        byte[] audio = null;
        try {
            request = (TextToSpeechRequest) this.ttsHandler
                    .handleRequest(request);
            audio = request.getByteData();
            if (audio == null) {
                throw new StaticGenerationException(
                        "Failed to synthesize the text maintenance audio! REASON = "
                                + request.getStatus());
            }
        } catch (Exception e) {
            throw new StaticGenerationException(
                    "Failed to generate the text maintenance audio!", e);
        }

        try {
            Files.write(maintenanceTextPath, audio);
        } catch (IOException e) {
            throw new StaticGenerationException(
                    "Failed to write the text maintenance audio to file: "
                            + maintenanceTextPath.toString(), e);
        }
    }

    /*
     * Retrieve the identifier of a tts voice that is available in the system.
     */
    private int getTtsVoice() throws BMHConfigurationException {
        List<TtsVoice> availableVoices = this.ttsVoiceDao.getAll();
        if (availableVoices == null || availableVoices.isEmpty()) {
            throw new BMHConfigurationException(
                    "Failed to retrieve a TTS Voice for synthesis. Please verify that any TTS Voices have been configured in the system.");
        }

        return availableVoices.get(0).getVoiceNumber();
    }

    private void generateAlert() throws StaticGenerationException {
        statusHandler.info("Generating alert maintenance file: "
                + maintenanceAlertPath.toString());

        byte[] audio = null;
        try {
            audio = TonesManager.generateAlertTone(ALERT_AMPLITUDE,
                    ALERT_DURATION);
        } catch (ToneGenerationException e) {
            throw new StaticGenerationException(
                    "Failed to generate the alert tone maintenance audio!", e);
        }

        try {
            Files.write(maintenanceAlertPath, audio);
        } catch (IOException e) {
            throw new StaticGenerationException(
                    "Failed to write the alert tone maintenance audio to file: "
                            + maintenanceTextPath.toString(), e);
        }
    }

    private void generateSame() throws StaticGenerationException {
        statusHandler.info("Generating same maintenance file: "
                + maintenanceSamePath.toString());

        byte[] audio = null;
        try {
            audio = TonesManager.generateSAMETone(SAME, 0);
        } catch (ToneGenerationException e) {
            throw new StaticGenerationException(
                    "Failed to generate the same tone maintenance audio!", e);
        }

        try {
            Files.write(maintenanceSamePath, audio);
        } catch (IOException e) {
            throw new StaticGenerationException(
                    "Failed to write the same tone maintenance audio to file: "
                            + maintenanceTextPath.toString(), e);
        }
    }

    private void generateTransfer() throws StaticGenerationException {
        boolean write18 = false;
        boolean write24 = false;
        if (Files.exists(maintenance18TransferPath) == false) {
            statusHandler.info("Generating transfer maintenance file: "
                    + maintenance18TransferPath.toString());
            write18 = true;
        }
        if (Files.exists(maintenance24TransferPath) == false) {
            statusHandler.info("Generating transfer maintenance file: "
                    + maintenance24TransferPath.toString());
            write24 = true;
        }

        byte[] audio;
        try {
            audio = TonesManager
                    .generateTransferTone(TransferType.PRIMARY_TO_SECONDARY);
        } catch (ToneGenerationException e) {
            throw new StaticGenerationException(
                    "Failed to generate the transfer tone audio!", e);
        }

        if (write18) {
            byte[] audioToWrite = Arrays
                    .copyOfRange(audio, 0, audio.length / 2);
            try {
                Files.write(maintenance18TransferPath, audioToWrite);
            } catch (IOException e) {
                throw new StaticGenerationException(
                        "Failed to write the transfer tone maintenance audio to file: "
                                + maintenance18TransferPath.toString(), e);
            }
        }

        if (write24) {
            try {
                byte[] audioToWrite = Arrays.copyOfRange(audio,
                        audio.length / 2, audio.length);
                Files.write(maintenance24TransferPath, audioToWrite);
            } catch (IOException e) {
                throw new StaticGenerationException(
                        "Failed to write the transfer tone maintenance audio to file: "
                                + maintenance24TransferPath.toString(), e);
            }
        }
    }

    private void validateDaos() throws IllegalStateException {
        if (this.ttsVoiceDao == null) {
            throw new IllegalStateException(
                    "TtsVoiceDao has not been set on the AlignmentTestGenerator");
        }
    }

    public TtsVoiceDao getTtsVoiceDao() {
        return ttsVoiceDao;
    }

    public void setTtsVoiceDao(TtsVoiceDao ttsVoiceDao) {
        this.ttsVoiceDao = ttsVoiceDao;
    }

    /**
     * @return the maintenanceTextPath
     */
    public Path getMaintenanceTextPath() {
        return maintenanceTextPath;
    }

    /**
     * @return the maintenanceAlertPath
     */
    public Path getMaintenanceAlertPath() {
        return maintenanceAlertPath;
    }

    /**
     * @return the maintenanceSamePath
     */
    public Path getMaintenanceSamePath() {
        return maintenanceSamePath;
    }

    /**
     * @return the maintenanceTransferPath
     */
    public Path getMaintenance18TransferPath() {
        return maintenance18TransferPath;
    }

    public Path getMaintenance24TransferPath() {
        return maintenance24TransferPath;
    }
}