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
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.raytheon.uf.common.bmh.audio.AudioRetrievalException;
import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastFragment;
import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg;
import com.raytheon.uf.common.bmh.datamodel.msg.InputMessage;
import com.raytheon.uf.common.bmh.datamodel.msg.ValidatedMessage;
import com.raytheon.uf.common.bmh.request.InputMessageAudioData;
import com.raytheon.uf.common.bmh.request.InputMessageAudioResponse;
import com.raytheon.uf.common.bmh.request.InputMessageRequest;
import com.raytheon.uf.common.bmh.request.InputMessageResponse;
import com.raytheon.uf.edex.bmh.dao.BroadcastMsgDao;
import com.raytheon.uf.edex.bmh.dao.InputMessageDao;
import com.raytheon.uf.edex.bmh.dao.ValidatedMessageDao;

/**
 * Handles any requests to get or modify the state of {@link InputMessage}s.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 16, 2014   3728     lvenable     Initial creation
 * Oct 23, 2014   3748     bkowal       Retrieve audio for audio input messages
 * Oct 24, 2014   3478     bkowal       Completed audio retrieval implementation
 * Nov 02, 2014   3785     mpduff       Add ValidatedMessage when getting by PkId
 * Nov 03, 2014   3790     lvenable     Updated enum name.
 * Nov 05, 2014   3748     bkowal       Created validated msg dao based on mode.
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */

public class InputMessageHandler extends
        AbstractBMHServerRequestHandler<InputMessageRequest> {

    @Override
    public Object handleRequest(InputMessageRequest request) throws Exception {
        InputMessageResponse inputMessageResponse = new InputMessageResponse();

        switch (request.getAction()) {
        case AllInputMessages:
            inputMessageResponse = getAllInputMessages(request);
            break;
        case ListIdNameAfosCreationActive:
            inputMessageResponse = getIdNameAfosCreation(request);
            break;
        case GetByPkId:
            inputMessageResponse = getByPkId(request);
            break;
        default:
            throw new UnsupportedOperationException(this.getClass()
                    .getSimpleName()
                    + " cannot handle action "
                    + request.getAction());
        }

        return inputMessageResponse;
    }

    /**
     * Get a list on input messages containing the Id, Name, Afos Id, and
     * Creation time.
     * 
     * @param request
     *            Input Message request.
     * @return Input message response containing the requested information.
     */
    private InputMessageResponse getIdNameAfosCreation(
            InputMessageRequest request) {
        InputMessageDao dao = new InputMessageDao(request.isOperational());
        InputMessageResponse response = new InputMessageResponse();

        List<InputMessage> inputMessageList = dao
                .getInputMsgsIdNameAfosCreation();
        response.setInputMessageList(inputMessageList);

        return response;
    }

    /**
     * Get all of the input messages fully populated with data.
     * 
     * @param request
     *            Input Message request.
     * @return Input message response containing the requested information.
     */
    private InputMessageResponse getAllInputMessages(InputMessageRequest request) {
        InputMessageDao dao = new InputMessageDao(request.isOperational());
        InputMessageResponse response = new InputMessageResponse();

        List<InputMessage> inputMessageList = dao.getInputMessages();
        response.setInputMessageList(inputMessageList);

        return response;
    }

    /**
     * Get the input message by the primary key ID.
     * 
     * @param request
     *            Input Message request.
     * @return Input message response containing the requested information.
     * @throws Exception
     */
    private InputMessageResponse getByPkId(InputMessageRequest request)
            throws Exception {
        InputMessageDao dao = new InputMessageDao(request.isOperational());
        InputMessageAudioResponse response = new InputMessageAudioResponse();

        InputMessage im = dao.getByID(request.getPkId());
        // retrieve audio
        response.setAudioDataList(this.getAudioContent(request));
        response.addInputMessage(im);

        ValidatedMessageDao validatedMsgDao = new ValidatedMessageDao(
                request.isOperational());
        ValidatedMessage validatedMsg = validatedMsgDao
                .getValidatedMsgByInputMsg(im);
        if (validatedMsg == null) {
            throw new Exception("Data Error:  Input message " + im.getId()
                    + " is missing validated message.");
        }
        response.setValidatedMessage(validatedMsg);

        return response;
    }

    private List<InputMessageAudioData> getAudioContent(
            InputMessageRequest request) throws AudioRetrievalException {
        BroadcastMsgDao broadcastMsgDao = new BroadcastMsgDao(
                request.isOperational());
        List<BroadcastMsg> msgs = broadcastMsgDao
                .getMessagesByInputMsgId(request.getPkId());
        if (msgs.isEmpty()) {
            return Collections.emptyList();
        }

        List<InputMessageAudioData> audioDataRecords = new ArrayList<>(
                msgs.size());
        OUTER_LOOP: for (BroadcastMsg broadcastMsg : msgs) {
            /*
             * Verify that fragments exist.
             */
            if (broadcastMsg.getFragments() == null
                    || broadcastMsg.getFragments().isEmpty()) {
                throw new AudioRetrievalException(
                        "Unable to find audio information associated with broadcast msg: "
                                + broadcastMsg.getId() + " for input msg: "
                                + request.getPkId() + ".");
            }

            InputMessageAudioData audioDataRecord = new InputMessageAudioData();
            audioDataRecord.setTransmitterGroupName(broadcastMsg
                    .getTransmitterGroup().getName());
            List<byte[]> audioData = new LinkedList<>();
            int totalByteCount = 0;
            for (int i = 0; i < broadcastMsg.getFragments().size(); i++) {
                BroadcastFragment fragment = broadcastMsg.getFragments().get(i);
                if (fragment.isSuccess() == false) {
                    /* audio generation failed - audio playback disabled */
                    audioDataRecord.setSuccess(false);
                    audioDataRecords.add(audioDataRecord);
                    continue OUTER_LOOP;
                }

                byte[] audio = this.readAudioFragment(fragment,
                        broadcastMsg.getId(), request.getPkId());
                totalByteCount += audio.length;
                audioData.add(audio);
            }

            /* audio generation successful. */
            audioDataRecord.setSuccess(true);

            /*
             * Construct the complete byte array.
             */
            if (audioData.size() == 1) {
                /*
                 * only one record, can be used as-is
                 */
                audioDataRecord.setAudio(audioData.get(0));
            } else {

                /*
                 * Need to merge the bytes from the separate fragments into a
                 * single array.
                 */
                ByteBuffer audioBuffer = ByteBuffer.allocate(totalByteCount);
                for (byte[] audioFragment : audioData) {
                    audioBuffer.put(audioFragment);
                }
                audioDataRecord.setAudio(audioBuffer.array());
            }

            /* calculate the duration in seconds */
            final long playbackTimeMS = totalByteCount / 160L * 20L;
            // swt component expects an Integer
            final int playbackTimeS = (int) playbackTimeMS / 1000;
            audioDataRecord.setAudioDuration(playbackTimeS);

            /* build the formatted duration string */
            int durationHours = 0;
            int durationMinutes = 0;
            int durationSeconds = playbackTimeS;
            if (durationSeconds > 59) {
                int remainingS = durationSeconds % 60;
                durationMinutes = (durationSeconds - remainingS) / 60;
                durationSeconds = remainingS;
            }
            if (durationMinutes > 59) {
                int remainingM = durationMinutes % 60;
                durationHours = (durationMinutes - remainingM) / 60;
                durationMinutes = remainingM;
            }
            StringBuilder formattedDuration = new StringBuilder();
            // only include hours if > 0
            if (durationHours > 0) {
                formattedDuration.append(StringUtils.leftPad(
                        Integer.toString(durationHours), 2, "0"));
                formattedDuration.append(":");
            }
            formattedDuration.append(StringUtils.leftPad(
                    Integer.toString(durationMinutes), 2, "0"));
            formattedDuration.append(":");
            formattedDuration.append(StringUtils.leftPad(
                    Integer.toString(durationSeconds), 2, "0"));
            audioDataRecord.setFormattedAudioDuration(formattedDuration
                    .toString());

            audioDataRecords.add(audioDataRecord);
        }

        Collections.sort(audioDataRecords);
        return audioDataRecords;
    }

    private byte[] readAudioFragment(final BroadcastFragment fragment,
            final long broadcastId, final int inputId)
            throws AudioRetrievalException {
        /*
         * verify that the audio file has been set and that it exists.
         */
        if (fragment.getOutputName() == null
                || fragment.getOutputName().trim().isEmpty()) {
            throw new AudioRetrievalException(
                    "Unable to find file information associated with broadcast fragment: "
                            + fragment.getId()
                            + " associated with broadcast msg: " + broadcastId
                            + " for input msg: " + inputId + ".");
        }

        Path audioFilePath = Paths.get(fragment.getOutputName());
        if (Files.exists(audioFilePath) == false) {
            throw new AudioRetrievalException("Audio file: "
                    + fragment.getOutputName()
                    + " associated with broadcast fragment: "
                    + fragment.getId() + " associated with broadcast msg: "
                    + broadcastId + " for input msg: " + inputId
                    + " does not exist.");
        }

        /*
         * Attempt to read the audio.
         */
        byte[] audioDataContents = null;
        try {
            audioDataContents = Files.readAllBytes(audioFilePath);
        } catch (IOException e) {
            throw new AudioRetrievalException("Failed to read audio file: "
                    + audioFilePath.toString() + " for input msg: " + inputId
                    + ".");
        }

        return audioDataContents;
    }
}