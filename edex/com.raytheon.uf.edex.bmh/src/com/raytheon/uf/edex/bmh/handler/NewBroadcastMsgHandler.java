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

import com.raytheon.uf.common.bmh.BMH_CATEGORY;
import com.raytheon.uf.common.bmh.audio.BMHAudioFormat;
import com.raytheon.uf.common.bmh.broadcast.NewBroadcastMsgRequest;
import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastContents;
import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastFragment;
import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg;
import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsgGroup;
import com.raytheon.uf.common.bmh.datamodel.msg.InputMessage;
import com.raytheon.uf.common.bmh.datamodel.msg.ValidatedMessage;
import com.raytheon.uf.common.bmh.datamodel.msg.ValidatedMessage.LdadStatus;
import com.raytheon.uf.common.bmh.datamodel.msg.ValidatedMessage.TransmissionStatus;
import com.raytheon.uf.common.bmh.datamodel.transmitter.LdadConfig;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.notify.config.MessageActivationNotification;
import com.raytheon.uf.common.bmh.request.AbstractBMHServerRequest;
import com.raytheon.uf.common.bmh.trace.TraceableId;
import com.raytheon.uf.common.bmh.trace.TraceableUtil;
import com.raytheon.uf.common.serialization.SerializationException;
import com.raytheon.uf.common.serialization.SerializationUtil;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.edex.bmh.BMHConstants;
import com.raytheon.uf.edex.bmh.BMHJmsDestinations;
import com.raytheon.uf.edex.bmh.BmhMessageProducer;
import com.raytheon.uf.edex.bmh.audio.EdexAudioConverterManager;
import com.raytheon.uf.edex.bmh.dao.BroadcastMsgDao;
import com.raytheon.uf.edex.bmh.dao.InputMessageDao;
import com.raytheon.uf.edex.bmh.dao.LdadConfigDao;
import com.raytheon.uf.edex.bmh.dao.TransmitterDao;
import com.raytheon.uf.edex.bmh.dao.ValidatedMessageDao;
import com.raytheon.uf.edex.bmh.ldad.LdadMsg;
import com.raytheon.uf.edex.bmh.msg.logging.IMessageLogger;
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
 * Jan 05, 2015  #3651     bkowal      Use {@link IMessageLogger} to log message errors.
 * Jan 06, 2015  #3651     bkowal      Support AbstractBMHPersistenceLoggingDao.
 * Jan 20, 2015  #4010     bkowal      Include the selected transmitters in the
 *                                     input message equals comparison.
 * Feb 09, 2015  #4091     bkowal      Use {@link EdexAudioConverterManager}.
 * Mar 05, 2015  #4208     bsteffen    Throw Exception when message is submitted without changes.
 * Mar 10, 2015  #4255     bsteffen    Delay inactivating previous until new validates.
 * Apr 07, 2015  #4293     bkowal      Update existing input messages in every case.
 * Apr 15, 2015  #4293     bkowal      Notify the playlist manager when a message has been expired.
 * Apr 20, 2015  #4397     bkowal      Forward the message expiration request time when applicable.
 * Apr 27, 2015  #4397     bkowal      Set the {@link InputMessage} update date.
 * May 13, 2015  #4229     rferrel     Changes for traceId.
 * May 20, 2015  #4490     bkowal      Fixes for {@link TraceableId}.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */
public class NewBroadcastMsgHandler extends
        AbstractBMHLoggingServerRequestHandler<NewBroadcastMsgRequest> {

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

    private final LdadValidator ldadCheck;

    private final LdadValidator practiceLdadCheck;

    public NewBroadcastMsgHandler(final LdadValidator ldadCheck,
            final LdadValidator practiceLdadCheck,
            final IMessageLogger opMessageLogger,
            final IMessageLogger pracMessageLogger) {
        super(opMessageLogger, pracMessageLogger);
        this.ldadCheck = ldadCheck;
        this.practiceLdadCheck = practiceLdadCheck;
    }

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
        String traceId = request.getTraceId();
        // TODO: logging
        InputMessage inputMessage = request.getInputMessage();
        inputMessage = updateInputMessage(request);
        inputMessage.setUpdateDate(TimeUtil.newGmtCalendar());
        final boolean newInputMsg = inputMessage.getId() == 0;

        /*
         * Determine if we need to create a new Validated Message or if we need
         * to retrieve and update the existing Validated Message.
         */
        ValidatedMessageDao validatedMsgDao = new ValidatedMessageDao(
                request.isOperational(), this.getMessageLogger(request));
        ValidatedMessage validMsg = null;
        if (inputMessage.getId() != 0) {
            validMsg = validatedMsgDao.getValidatedMsgByInputMsg(inputMessage);
            validMsg.setTraceId(traceId);
        }
        if (validMsg == null) {
            validMsg = new ValidatedMessage(traceId);
        }

        List<String> unacceptableWords = UnacceptableWordFilter
                .check(inputMessage);
        validMsg.setInputMessage(inputMessage);
        if (unacceptableWords.isEmpty()) {
            LdadValidator ldadCheck = this.getLdadValidator(request);
            ldadCheck.validate(validMsg);
            validMsg.setTransmissionStatus(TransmissionStatus.ACCEPTED);
        } else {
            validMsg.setTransmissionStatus(TransmissionStatus.UNACCEPTABLE);
            validMsg.setLdadStatus(LdadStatus.UNACCEPTABLE);
        }
        Set<TransmitterGroup> transmitterGroups = this
                .retrieveTransmitterGroups(request);

        validMsg.setTransmitterGroups(transmitterGroups);
        if (!validMsg.isAccepted()) {
            throw new IllegalArgumentException(
                    inputMessage.getName()
                            + "("
                            + traceId
                            + ": "
                            + inputMessage.getAfosid()
                            + ") failed to validate because it contains the following unacceptable words: "
                            + unacceptableWords.toString());
        } else if (request.getMessageAudio() == null) {

            validatedMsgDao.persistCascade(validMsg);
            if (newInputMsg == false
                    && Boolean.FALSE.equals(inputMessage.getActive())) {
                /*
                 * If an existing message is in the inactive state, ensure that
                 * the playlist manager knows that it needs to be removed from
                 * any existing playlists. Any other updates will still be
                 * processed as-is; however, the playlist manager ignores all
                 * inactive messages by default.
                 */
                BmhMessageProducer.sendConfigMessage(
                        new MessageActivationNotification(inputMessage, request
                                .getExpireRequestTime()), request
                                .isOperational());
                /*
                 * If the message transitioned from active to inactive. Now that
                 * all edits are in-place, the normal update procedure will
                 * handle the message updates correctly. If an inactive message
                 * was updated, the message will be updated as it should be and
                 * the message will be ignored by the playlist manager.
                 */
            }
            // we are finished, send the validated message to the message
            // transformer.
            TraceableId tId = new TraceableId(validMsg.getId(),
                    validMsg.getTraceId());
            this.sendToDestination(
                    BMHJmsDestinations.getBMHTransformDestination(request),
                    SerializationUtil.transformToThrift(tId));
            return inputMessage.getId();
        }

        /*
         * Write the audio output. The audio in this case is uniform across all
         * transmitters and (when applicable) ldad configurations because
         * dictionaries cannot be applied.
         */
        Path recordedAudioOutputPath = this.writeOutputAudio(
                request.getMessageAudio(), inputMessage, traceId);

        /* build broadcast messages. */
        List<BroadcastMsg> broadcastRecords = this.buildBroadcastRecords(
                validMsg, transmitterGroups, recordedAudioOutputPath, request);

        // persist all entities.
        List<Object> entitiesToPersist = new LinkedList<Object>();
        // input message first
        entitiesToPersist.add(validMsg.getInputMessage());
        // the validated message next
        entitiesToPersist.add(validMsg);
        // any broadcast message(s) last
        entitiesToPersist.addAll(broadcastRecords);
        validatedMsgDao.persistAll(entitiesToPersist);

        validatedMsgDao.persistCascade(validMsg);
        if (newInputMsg == false
                && Boolean.FALSE.equals(inputMessage.getActive())) {
            BmhMessageProducer.sendConfigMessage(
                    new MessageActivationNotification(inputMessage, request
                            .getExpireRequestTime()), request.isOperational());
        }

        // send the broadcast message(s) to the playlist scheduler.
        List<Long> messageIdsToSend = new ArrayList<>();
        for (BroadcastMsg broadcastRecord : broadcastRecords) {
            messageIdsToSend.add(broadcastRecord.getId());
        }
        BroadcastMsgGroup messagesToSend = new BroadcastMsgGroup();
        messagesToSend.setIds(messageIdsToSend);
        messagesToSend.setTraceId(traceId);
        this.sendToDestination(
                BMHJmsDestinations.getBMHScheduleDestination(request),
                SerializationUtil.transformToThrift(messagesToSend));

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
                convertedAudio = EdexAudioConverterManager.getInstance()
                        .convertAudio(request.getMessageAudio(),
                                DEFAULT_TTS_FORMAT, ldadConfig.getEncoding());
            } catch (Exception e) {
                statusHandler
                        .error(BMH_CATEGORY.LDAD_ERROR,
                                TraceableUtil.createTraceMsgHeader(traceId)
                                        + "Failed to convert audio to the "
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
                        alternateRecordedAudioOutputPath, traceId);
            } catch (Exception e) {
                statusHandler
                        .error(BMH_CATEGORY.LDAD_ERROR,
                                traceId
                                        + " Failed to write audio: "
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
                statusHandler.error(BMH_CATEGORY.LDAD_ERROR, traceId
                        + " Failed to trigger the ldad dissemination of: "
                        + ldadMsg.getOutputName() + " for ldad configuration: "
                        + ldadConfig.getName() + " (id = " + ldadConfig.getId()
                        + ").", e);
            }
        }

        return inputMessage.getId();
    }

    /**
     * Check if this is a modification of a previous message. Returns the
     * original message if there is no previous message. Returns an updated
     * version of the previous message when one is found.
     */
    private InputMessage updateInputMessage(NewBroadcastMsgRequest request)
            throws EdexException, SerializationException {
        InputMessage inputMessage = request.getInputMessage();
        if (inputMessage.getId() == 0) {
            /*
             * Completely new input message - nothing to update.
             */
            return inputMessage;
        }

        /*
         * Retrieve the currently persisted input message for the id.
         */
        InputMessageDao inputMessageDao = new InputMessageDao(
                request.isOperational(), this.getMessageLogger(request));
        InputMessage previous = inputMessageDao.getByID(inputMessage.getId());
        if (inputMessage.equals(previous)) {
            throw new IllegalStateException(request.getTraceId()
                    + " Duplicate message will be ignored.");
        }

        /*
         * The original input message has been modified. Update the originally
         * persisted input message.
         */
        // The expiration date/time can be modified.
        previous.setExpirationTime(inputMessage.getExpirationTime());

        // The periodicity can be modified.
        previous.setPeriodicity(inputMessage.getPeriodicity());

        // The message can be activated or deactivated.
        previous.setActive(inputMessage.getActive());

        // The message contents can be altered.
        previous.setContent(inputMessage.getContent());

        return previous;
    }

    private LdadValidator getLdadValidator(NewBroadcastMsgRequest request) {
        return request.isOperational() ? this.ldadCheck
                : this.practiceLdadCheck;
    }

    private List<BroadcastMsg> buildBroadcastRecords(
            final ValidatedMessage validMsg,
            final Set<TransmitterGroup> transmitterGroups,
            Path recordedAudioOutputPath, final AbstractBMHServerRequest request) {
        List<BroadcastMsg> broadcastRecords = new ArrayList<>(
                transmitterGroups.size());
        BroadcastMsgDao broadcastMsgDao = new BroadcastMsgDao(
                request.isOperational(), this.getMessageLogger(request));
        for (TransmitterGroup transmitterGroup : transmitterGroups) {
            Calendar current = TimeUtil.newGmtCalendar();

            BroadcastMsg broadcastMsg = broadcastMsgDao
                    .getMessageByInputMessageAndGroup(
                            validMsg.getInputMessage(), transmitterGroup);
            if (broadcastMsg == null) {
                broadcastMsg = new BroadcastMsg();
                broadcastMsg.setCreationDate(current);
            }
            broadcastMsg.setUpdateDate(current);
            broadcastMsg.setTransmitterGroup(transmitterGroup);
            broadcastMsg.setInputMessage(validMsg.getInputMessage());

            // Build the fragment.
            BroadcastFragment fragment = new BroadcastFragment();
            fragment.setSsml(StringUtils.EMPTY);
            fragment.setVoice(null);
            fragment.setSuccess(true);

            fragment.setOutputName(recordedAudioOutputPath.toString());

            BroadcastContents contents = new BroadcastContents();
            contents.addFragment(fragment);
            broadcastMsg.addBroadcastContents(contents);

            broadcastRecords.add(broadcastMsg);
        }

        return broadcastRecords;
    }

    private Path writeOutputAudio(final byte[] audio, InputMessage inputMsg,
            String traceId) throws IOException {
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
        this.writeOutputAudio(audio, audioFilePath, traceId);

        return audioFilePath;
    }

    private void writeOutputAudio(final byte[] audio, final Path audioFilePath,
            String traceId) throws IOException {
        Files.write(audioFilePath, audio);
        statusHandler.info(traceId
                + " Successfully wrote Weather Message audio file: "
                + audioFilePath.toString() + ".");
    }

    private void sendToDestination(final String destinationURI,
            final Object message) throws EdexException {
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