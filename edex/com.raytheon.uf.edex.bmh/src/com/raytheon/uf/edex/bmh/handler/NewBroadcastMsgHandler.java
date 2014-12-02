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
package com.raytheon.uf.edex.bmh.handler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;

import com.raytheon.uf.common.bmh.BMH_CATEGORY;
import com.raytheon.uf.common.bmh.audio.AudioConvererterManager;
import com.raytheon.uf.common.bmh.audio.BMHAudioFormat;
import com.raytheon.uf.common.bmh.broadcast.NewBroadcastMsgRequest;
import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastFragment;
import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg;
import com.raytheon.uf.common.bmh.datamodel.msg.InputMessage;
import com.raytheon.uf.common.bmh.datamodel.msg.ValidatedMessage;
import com.raytheon.uf.common.bmh.datamodel.msg.ValidatedMessage.LdadStatus;
import com.raytheon.uf.common.bmh.datamodel.msg.ValidatedMessage.TransmissionStatus;
import com.raytheon.uf.common.bmh.datamodel.transmitter.LdadConfig;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.notify.config.MessageActivationNotification;
import com.raytheon.uf.common.serialization.SerializationException;
import com.raytheon.uf.common.serialization.SerializationUtil;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.edex.bmh.BMHConstants;
import com.raytheon.uf.edex.bmh.BMHJmsDestinations;
import com.raytheon.uf.edex.bmh.BmhMessageProducer;
import com.raytheon.uf.edex.bmh.dao.InputMessageDao;
import com.raytheon.uf.edex.bmh.dao.LdadConfigDao;
import com.raytheon.uf.edex.bmh.dao.TransmitterDao;
import com.raytheon.uf.edex.bmh.dao.ValidatedMessageDao;
import com.raytheon.uf.edex.bmh.ldad.LdadMsg;
import com.raytheon.uf.edex.bmh.msg.validator.LdadValidator;
import com.raytheon.uf.edex.bmh.msg.validator.UnacceptableWordFilter;
import com.raytheon.uf.edex.bmh.status.BMHStatusHandler;
import com.raytheon.uf.edex.bmh.status.IBMHStatusHandler;
import com.raytheon.uf.edex.core.EDEXUtil;
import com.raytheon.uf.edex.core.EdexException;

/**
 * Handles the creation and dissemination of user-generated weather messages.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 23, 2014  #3748     bkowal      Initial creation
 * Oct 26, 2014  #3748     bkowal      Passed audio data to broadcast msg creation.
 * Oct 28, 2014  #3759     bkowal      Support practice mode.
 * Oct 31, 2014  #3778     bsteffen    When only activation changes do not create new message.
 * Nov 18, 2014  #3807     bkowal      Use BMHJmsDestinations.
 * Nov 20, 2014  #3385     bkowal      Complete ldad validation of messages.
 * Nov 21, 2014  #3385     bkowal      Support ldad dissemination of recorded audio.
 * Dec 02, 2014  #3614     bsteffen    Check for unacceptable words.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */
public class NewBroadcastMsgHandler extends
        AbstractBMHServerRequestHandler<NewBroadcastMsgRequest> {

    private static final IBMHStatusHandler statusHandler = BMHStatusHandler
            .getInstance(NewBroadcastMsgHandler.class);

    private static final ThreadLocal<SimpleDateFormat> TODAY_DATED_DIRECTORY_FORMAT = TimeUtil
            .buildThreadLocalSimpleDateFormat("yyMMdd",
                    TimeZone.getTimeZone("GMT"));

    private static final ThreadLocal<SimpleDateFormat> FILE_NAME_TIME_FORMAT = TimeUtil
            .buildThreadLocalSimpleDateFormat("HHmmss",
                    TimeZone.getTimeZone("GMT"));

    private static final String AUDIO_DIRECTORY = "audio";

    private static final String WX_MESSAGES_DIRECTORY = "WXMessages";

    private static final BMHAudioFormat DEFAULT_TTS_FORMAT = BMHAudioFormat.ULAW;

    private static final String DEFAULT_TTS_FILE_EXTENSION = DEFAULT_TTS_FORMAT
            .getExtension();

    private Path wxMessagesPath;

    public void initialize() {
        wxMessagesPath = Paths.get(BMHConstants.getBmhDataDirectory(),
                AUDIO_DIRECTORY, WX_MESSAGES_DIRECTORY);

        if (Files.exists(wxMessagesPath) == false) {
            try {
                Files.createDirectories(wxMessagesPath);
            } catch (IOException e) {
                // TODO: create BMH category.
                statusHandler.error(BMH_CATEGORY.UNKNOWN,
                        "Failed to create the Weather Messages audio directory: "
                                + wxMessagesPath.toString() + "!", e);
                return;
            }
        }

        statusHandler.info("Using Weather Messages audio directory: "
                + wxMessagesPath.toString());
    }

    @Override
    public Object handleRequest(NewBroadcastMsgRequest request)
            throws Exception {
        // TODO: logging

        InputMessage inputMessage = request.getInputMessage();
        if (inputMessage.getId() != 0) {
            /*
             * This is an update need to check what changed, if it is only the
             * active/inactive flag then apply the change directly to this
             * entry, if it is any other change then need to mark the old
             * message as inactive and then process the change as a new message.
             */
            InputMessageDao inputMessageDao = new InputMessageDao(
                    request.isOperational());
            InputMessage previous = inputMessageDao.getByID(inputMessage
                    .getId());
            boolean activeChanged = false;
            if (inputMessage.getActive() != null) {
                activeChanged = !inputMessage.getActive().equals(
                        previous.getActive());
            } else if (previous.getActive() != null) {
                activeChanged = true;
            }
            boolean nothingElseChanged = true;
            if (activeChanged) {
                EqualsBuilder builder = new EqualsBuilder();
                builder.append(previous.getName(), inputMessage.getName());
                builder.append(previous.getLanguage(),
                        inputMessage.getLanguage());
                builder.append(previous.getAfosid(), inputMessage.getAfosid());
                builder.append(previous.getCreationTime(),
                        inputMessage.getCreationTime());
                builder.append(previous.getEffectiveTime(),
                        inputMessage.getEffectiveTime());
                builder.append(previous.getPeriodicity(),
                        inputMessage.getPeriodicity());
                builder.append(previous.getMrd(), inputMessage.getMrd());
                builder.append(previous.getConfirm(), inputMessage.getConfirm());
                builder.append(previous.getInterrupt(),
                        inputMessage.getInterrupt());
                builder.append(previous.getAlertTone(),
                        inputMessage.getAlertTone());
                builder.append(previous.getNwrsameTone(),
                        inputMessage.getNwrsameTone());
                builder.append(previous.getAreaCodes(),
                        inputMessage.getAreaCodes());
                builder.append(previous.getExpirationTime(),
                        inputMessage.getExpirationTime());
                builder.append(previous.getContent(), inputMessage.getContent());
                builder.append(previous.isValidHeader(),
                        inputMessage.isValidHeader());
                nothingElseChanged = builder.isEquals();
            }
            if (activeChanged && nothingElseChanged) {
                inputMessageDao.persist(inputMessage);
                BmhMessageProducer.sendConfigMessage(
                        new MessageActivationNotification(inputMessage),
                        request.isOperational());
                return inputMessage.getId();
            } else if (!Boolean.FALSE.equals(previous.getActive())) {
                previous.setActive(false);
                inputMessageDao.persist(previous);
                BmhMessageProducer.sendConfigMessage(
                        new MessageActivationNotification(previous),
                        request.isOperational());
            }
            inputMessage.setId(0);
        }

        LdadValidator ldadCheck = new LdadValidator(request.isOperational());

        // Build a validated message.
        ValidatedMessage validMsg = new ValidatedMessage();
        List<String> unacceptableWords = UnacceptableWordFilter
                .check(inputMessage);
        validMsg.setInputMessage(inputMessage);
        if (unacceptableWords.isEmpty()) {
            ldadCheck.validate(validMsg);
            validMsg.setTransmissionStatus(TransmissionStatus.ACCEPTED);
        } else {
            validMsg.setTransmissionStatus(TransmissionStatus.UNACCEPTABLE);
            validMsg.setLdadStatus(LdadStatus.UNACCEPTABLE);
        }
        Set<TransmitterGroup> transmitterGroups = this
                .retrieveTransmitterGroups(request);

        validMsg.setTransmitterGroups(transmitterGroups);

        ValidatedMessageDao validatedMsgDao = new ValidatedMessageDao(
                request.isOperational());
        if (!validMsg.isAccepted()) {
            validatedMsgDao.persistCascade(validMsg);
            throw new IllegalArgumentException(
                    inputMessage.getName()
                            + "("
                            + inputMessage.getAfosid()
                            + ") failed to validate because it contains the following unacceptable words: "
                            + unacceptableWords.toString());
        } else if (request.getMessageAudio() == null) {
            validatedMsgDao.persistCascade(validMsg);

            // we are finished, send the validated message to the message
            // transformer.
            this.sendToDestination(
                    BMHJmsDestinations.getBMHTransformDestination(request),
                    validMsg.getId());

            return inputMessage.getId();
        }

        /*
         * Write the audio output. The audio in this case is uniform across all
         * transmitters and (when applicable) ldad configurations because
         * dictionaries cannot be applied.
         */
        Path recordedAudioOutputPath = this.writeOutputAudio(
                request.getMessageAudio(), inputMessage);

        /* build broadcast messages. */
        List<BroadcastMsg> broadcastRecords = this.buildBroadcastRecords(
                validMsg, transmitterGroups, recordedAudioOutputPath);

        // persist all entities.
        List<Object> entitiesToPersist = new LinkedList<Object>();
        // input message first
        entitiesToPersist.add(validMsg.getInputMessage());
        // the validated message next
        entitiesToPersist.add(validMsg);
        // any broadcast message(s) last
        entitiesToPersist.addAll(broadcastRecords);
        validatedMsgDao.persistAll(entitiesToPersist);

        // send the broadcast message(s) to the playlist scheduler.
        for (BroadcastMsg broadcastRecord : broadcastRecords) {
            this.sendToDestination(
                    BMHJmsDestinations.getBMHScheduleDestination(request),
                    broadcastRecord.getId());
        }

        /*
         * we only reach this point if the message is a completely new recorded
         * audio message or if the audio has been altered in an existing
         * message. The {@link InputMessage} content is altered every time new
         * audio is recorded.
         */
        // will it be necessary to trigger ldad dissemination?
        if (LdadStatus.ACCEPTED.equals(validMsg.getLdadStatus()) == false) {
            /*
             * no ldad configuration exists.
             */
            return inputMessage.getId();
        }

        /*
         * ldad configuration exists. we will not be able to apply dictionaries
         * because the audio already exists. It is only a matter of ensuring
         * that the audio exists in the required formats. ldad dissemination
         * failure will not prevent scheduling the audio for broadcast. However,
         * the user that submitted the request will be notified via AlertViz
         * that an audio dissemination will not be completed.
         */

        // retrieve the ldad configurations
        LdadConfigDao ldadConfigDao = new LdadConfigDao(request.isOperational());
        List<LdadConfig> ldadConfigList = ldadConfigDao
                .getLdadConfigsForMsgType(inputMessage.getAfosid());

        // prepare to build the ldad messages
        Map<LdadConfig, LdadMsg> ldadConfigMsgMap = new HashMap<>(
                ldadConfigList.size(), 1.0f);
        Map<BMHAudioFormat, Path> audioFormatsPathMap = new HashMap<>(
                BMHAudioFormat.values().length, 1.0f);
        // the default format.
        audioFormatsPathMap.put(DEFAULT_TTS_FORMAT, recordedAudioOutputPath);
        for (LdadConfig ldadConfig : ldadConfigList) {
            LdadMsg ldadMsg = new LdadMsg();
            ldadMsg.setLdadId(ldadConfig.getId());
            ldadMsg.setAfosid(inputMessage.getAfosid());
            ldadMsg.setEncoding(ldadConfig.getEncoding());
            ldadMsg.setSuccess(true);
            Path ldadOutputName = audioFormatsPathMap.get(ldadConfig
                    .getEncoding());
            if (ldadOutputName != null) {
                // audio already exists in the required format.
                ldadMsg.setOutputName(ldadOutputName.toString());
                ldadConfigMsgMap.put(ldadConfig, ldadMsg);
                continue;
            }

            /*
             * audio does not exist in the required format. audio in the default
             * format needs to be converted to the required format.
             */

            /*
             * determine the name of the new audio output file using the
             * alternate file extension. The Java file API does not provide
             * support for interacting with file extensions; so, basic string
             * operations will be used.
             */
            String containingDirectoryString = recordedAudioOutputPath
                    .getParent().toString();
            String audioFileNameString = recordedAudioOutputPath.getFileName()
                    .toString();
            audioFileNameString = StringUtils.removeEnd(audioFileNameString,
                    DEFAULT_TTS_FILE_EXTENSION);
            audioFileNameString += ldadConfig.getEncoding().getExtension();
            Path alternateRecordedAudioOutputPath = Paths.get(
                    containingDirectoryString, audioFileNameString);

            /*
             * attempt the conversion
             */
            byte[] convertedAudio = null;
            try {
                convertedAudio = AudioConvererterManager.getInstance()
                        .convertAudio(request.getMessageAudio(),
                                DEFAULT_TTS_FORMAT, ldadConfig.getEncoding());
            } catch (Exception e) {
                statusHandler
                        .error(BMH_CATEGORY.LDAD_ERROR,
                                "Failed to convert audio to the "
                                        + ldadConfig.getEncoding().toString()
                                        + " format for ldad configuration: "
                                        + ldadConfig.getName()
                                        + " (id = "
                                        + ldadConfig.getId()
                                        + "). Scheduled audio will not be disseminated for this configuration.",
                                e);
                // skip dissemination for this configuration.
                continue;
            }

            /*
             * write the converted audio.
             */
            try {
                this.writeOutputAudio(convertedAudio,
                        alternateRecordedAudioOutputPath);
            } catch (Exception e) {
                statusHandler
                        .error(BMH_CATEGORY.LDAD_ERROR,
                                "Failed to write audio: "
                                        + alternateRecordedAudioOutputPath
                                                .toString()
                                        + " in an alternate format for ldad configuration: "
                                        + ldadConfig.getName()
                                        + " (id = "
                                        + ldadConfig.getId()
                                        + "). Scheduled audio will not be disseminated for this configuration.",
                                e);
            }
            /*
             * cache the converted audio path.
             */
            audioFormatsPathMap.put(ldadConfig.getEncoding(),
                    alternateRecordedAudioOutputPath);

            /*
             * complete the ldad message and continue.
             */
            ldadMsg.setOutputName(alternateRecordedAudioOutputPath.toString());
            ldadConfigMsgMap.put(ldadConfig, ldadMsg);
        }

        /*
         * Ldad messages have been generated. Submit the messages to trigger
         * dissemination.
         */
        for (LdadConfig ldadConfig : ldadConfigMsgMap.keySet()) {
            LdadMsg ldadMsg = ldadConfigMsgMap.get(ldadConfig);
            try {
                this.sendToDestination(
                        BMHJmsDestinations.getBMHLdadDestination(request),
                        SerializationUtil.transformToThrift(ldadMsg));
            } catch (Exception e) {
                statusHandler.error(
                        BMH_CATEGORY.LDAD_ERROR,
                        "Failed to trigger the ldad dissemination of: "
                                + ldadMsg.getOutputName()
                                + " for ldad configuration: "
                                + ldadConfig.getName() + " (id = "
                                + ldadConfig.getId() + ").", e);
            }
        }

        return inputMessage.getId();
    }

    private List<BroadcastMsg> buildBroadcastRecords(
            final ValidatedMessage validMsg,
            final Set<TransmitterGroup> transmitterGroups,
            Path recordedAudioOutputPath) throws IOException {
        List<BroadcastMsg> broadcastRecords = new ArrayList<>(
                transmitterGroups.size());
        for (TransmitterGroup transmitterGroup : transmitterGroups) {
            Calendar current = TimeUtil.newGmtCalendar();

            BroadcastMsg broadcastMsg = new BroadcastMsg();
            broadcastMsg.setCreationDate(current);
            broadcastMsg.setUpdateDate(current);
            broadcastMsg.setTransmitterGroup(transmitterGroup);
            broadcastMsg.setInputMessage(validMsg.getInputMessage());

            // Build the fragment.
            BroadcastFragment fragment = new BroadcastFragment();
            fragment.setSsml(StringUtils.EMPTY);
            // TODO: handle voice. Allow NULL values? Should we have a constant
            // for user voice?
            fragment.setVoice(null);
            fragment.setSuccess(true);

            fragment.setOutputName(recordedAudioOutputPath.toString());

            broadcastMsg.addFragment(fragment);
            broadcastRecords.add(broadcastMsg);
        }

        return broadcastRecords;
    }

    private Path writeOutputAudio(final byte[] audio, InputMessage inputMsg)
            throws IOException {
        final String fileNamePartsSeparator = "_";

        Date current = TimeUtil.newGmtCalendar().getTime();
        Path datedWxMsgDirectory = this.wxMessagesPath
                .resolve(TODAY_DATED_DIRECTORY_FORMAT.get().format(current));

        Files.createDirectories(datedWxMsgDirectory);

        StringBuilder fileName = new StringBuilder(inputMsg.getAfosid());
        fileName.append(fileNamePartsSeparator);
        fileName.append(inputMsg.getName());
        fileName.append(fileNamePartsSeparator);
        fileName.append(FILE_NAME_TIME_FORMAT.get().format(current));
        fileName.append(DEFAULT_TTS_FILE_EXTENSION);

        Path audioFilePath = datedWxMsgDirectory.resolve(fileName.toString());
        this.writeOutputAudio(audio, audioFilePath);

        return audioFilePath;
    }

    private void writeOutputAudio(final byte[] audio, final Path audioFilePath)
            throws IOException {
        Files.write(audioFilePath, audio);
        statusHandler.info("Successfully wrote Weather Message audio file: "
                + audioFilePath.toString() + ".");
    }

    private void sendToDestination(final String destinationURI,
            final Object message) throws EdexException, SerializationException {
        EDEXUtil.getMessageProducer().sendAsyncUri(destinationURI, message);
    }

    private Set<TransmitterGroup> retrieveTransmitterGroups(
            NewBroadcastMsgRequest request) {
        TransmitterDao transmitterDao = new TransmitterDao(
                request.isOperational());
        Set<TransmitterGroup> transmitterGroups = new HashSet<>(request
                .getSelectedTransmitters().size(), 1.0f);
        for (Transmitter transmitter : request.getSelectedTransmitters()) {
            Transmitter fullRecord = transmitterDao
                    .getByID(transmitter.getId());
            if (fullRecord == null || fullRecord.getTransmitterGroup() == null) {
                continue;
            }
            transmitterGroups.add(transmitter.getTransmitterGroup());
        }

        return transmitterGroups;
    }
}