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
package com.raytheon.uf.viz.bmh.ui.dialogs.msgtypes;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.common.bmh.request.MessageTypeRequest;
import com.raytheon.uf.common.bmh.request.MessageTypeRequest.MessageTypeAction;
import com.raytheon.uf.common.bmh.request.MessageTypeResponse;
import com.raytheon.uf.viz.bmh.data.BmhUtils;

/**
 * DataManager for the Message Type data.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 17, 2014  #3490     lvenable    Initial creation
 * Aug 18, 2014   3411     mpduff      Added saveMessageType()
 * Sep 15, 2014   #3610    lvenable    Moved getMessageType functionality into this
 *                                     class from the BroadcastCycleDataManager.
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */

public class MessageTypeDataManager {

    /**
     * Get a list of all the message types.
     * 
     * @param comparator
     *            Comparator used for sorting, null for no sorting.
     * @return A list of all the message types.
     * @throws Exception
     */
    public List<MessageType> getMessageTypes(Comparator<MessageType> comparator)
            throws Exception {
        List<MessageType> messageTypeList = null;

        MessageTypeRequest mtRequest = new MessageTypeRequest();
        mtRequest.setAction(MessageTypeAction.AllMessageTypes);
        MessageTypeResponse mtResponse = null;

        mtResponse = (MessageTypeResponse) BmhUtils.sendRequest(mtRequest);
        messageTypeList = mtResponse.getMessageTypeList();

        if (messageTypeList == null) {
            messageTypeList = Collections.emptyList();
        }

        if (comparator != null && messageTypeList.isEmpty() == false) {
            Collections.sort(messageTypeList, comparator);
        }

        return messageTypeList;
    }

    /**
     * Get the list of all the message types with only the Afos ID and Title
     * populated.
     * 
     * @param comparator
     *            Comparator for ordering the message types.
     * @return List of message types.
     * @throws Exception
     */
    public List<MessageType> getMsgTypesAfosIdTitle(
            Comparator<MessageType> comparator) throws Exception {

        List<MessageType> messageTypeList = null;

        MessageTypeRequest mtRequest = new MessageTypeRequest();
        mtRequest.setAction(MessageTypeAction.GetAfosIdTitle);
        MessageTypeResponse mtResponse = null;

        mtResponse = (MessageTypeResponse) BmhUtils.sendRequest(mtRequest);
        messageTypeList = mtResponse.getMessageTypeList();

        if (messageTypeList == null) {
            messageTypeList = Collections.emptyList();
        }

        if (comparator != null && messageTypeList.isEmpty() == false) {
            Collections.sort(messageTypeList, comparator);
        }

        return messageTypeList;
    }

    /**
     * Delete the selected message type.
     * 
     * @param msgType
     *            The message type to be deleted.
     * @throws Exception
     */
    public void deleteMessageType(MessageType msgType) throws Exception {
        MessageTypeRequest mtRequest = new MessageTypeRequest();
        mtRequest.setAction(MessageTypeAction.Delete);
        mtRequest.setMessageType(msgType);

        BmhUtils.sendRequest(mtRequest);
    }

    /**
     * Save the {@link MessageType}
     * 
     * @param selectedMsgType
     *            to save
     * @return saved Object
     * @throws Exception
     */
    public MessageType saveMessageType(MessageType selectedMsgType)
            throws Exception {
        MessageTypeRequest req = new MessageTypeRequest();
        req.setMessageType(selectedMsgType);
        req.setAction(MessageTypeAction.Save);

        MessageTypeResponse response = (MessageTypeResponse) BmhUtils
                .sendRequest(req);

        return response.getMessageTypeList().get(0);
    }

    /**
     * Get the {@link MessageType} object for the associated afosId
     * 
     * @param afosId
     *            The afosId
     * @return The MessageType
     * @throws Exception
     */
    public MessageType getMessageType(String afosId) throws Exception {
        MessageTypeRequest req = new MessageTypeRequest();
        req.setAction(MessageTypeAction.GetByAfosId);
        req.setAfosId(afosId);

        MessageTypeResponse response = (MessageTypeResponse) BmhUtils
                .sendRequest(req);

        List<MessageType> list = response.getMessageTypeList();
        if ((list != null) && (list.size() > 0)) {
            return list.get(0);
        }

        return null;
    }
}
