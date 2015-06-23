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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.raytheon.uf.common.bmh.StaticMessageIdentifier;
import com.raytheon.uf.common.bmh.datamodel.language.Language;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType.Designation;
import com.raytheon.uf.common.bmh.request.MessageTypeRequest;
import com.raytheon.uf.common.bmh.request.MessageTypeRequest.MessageTypeAction;
import com.raytheon.uf.common.bmh.request.MessageTypeResponse;
import com.raytheon.uf.common.bmh.request.MessageTypeValidationRequest;
import com.raytheon.uf.common.bmh.request.MessageTypeValidationResponse;
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
 * Sep 19, 2014   #3611    lvenable    Added method to get emergency override message types.
 * Oct 23, 2014   #3728    lvenable    Updated for getting AFOS IDs via designation.
 * Feb 10, 2015   #4085    bkowal      Added {@link #getStaticMessageAfosIds()}.
 * Mar 12, 2015   #4213    bkowal      {@link #getStaticMessageAfosIds()} now includes
 *                                     all station id messages.
 * Jun 23, 2015   #4572    bkowal      Added {@link #getMessageTypesForValidAfosIds(Set)}.
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

    public List<MessageType> getMessageTypesByDesignationAndLanguage(
            Designation designation, Language language) throws Exception {
        MessageTypeRequest mtRequest = new MessageTypeRequest();
        mtRequest.setAction(MessageTypeAction.GetByDesignationAndLanguage);
        mtRequest.setDesignation(designation);
        mtRequest.setLanguage(language);

        MessageTypeResponse mtResponse = (MessageTypeResponse) BmhUtils
                .sendRequest(mtRequest);
        if (mtResponse.getMessageTypeList() == null) {
            return Collections.emptyList();
        }

        return mtResponse.getMessageTypeList();
    }

    /**
     * Get a set of Message Type AFOS IDs by the specified designation.
     * 
     * @param designation
     *            Message Type designation
     * @return Set of AFOS IDs.
     * @throws Exception
     */
    public Set<String> getAfosIdsByDesignation(Designation designation)
            throws Exception {

        MessageTypeRequest mtRequest = new MessageTypeRequest();
        mtRequest.setAction(MessageTypeAction.GetAfosDesignation);
        mtRequest.setDesignation(designation);

        MessageTypeResponse mtResponse = (MessageTypeResponse) BmhUtils
                .sendRequest(mtRequest);
        List<MessageType> messageTypeList = mtResponse.getMessageTypeList();

        if (messageTypeList == null) {
            return Collections.emptySet();
        }

        Set<String> afosIdSet = new HashSet<String>(messageTypeList.size(), 1);
        for (MessageType mt : messageTypeList) {
            afosIdSet.add(mt.getAfosid());
        }

        return afosIdSet;
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

    /**
     * Retrieves any {@link MessageType}s that are associated with the specified
     * afos id. The provided response object,
     * {@link MessageTypeValidationResponse} also specifies which afos ids were
     * invalid and which afos ids were not associated with a {@link MessageType}
     * .
     * 
     * @param afosIds
     *            the specified afos ids
     * @return a {@link MessageTypeValidationResponse}
     * @throws Exception
     */
    public MessageTypeValidationResponse getMessageTypesForValidAfosIds(
            Set<String> afosIds) throws Exception {
        MessageTypeValidationRequest request = new MessageTypeValidationRequest();
        request.setAfosIds(afosIds);

        return (MessageTypeValidationResponse) BmhUtils.sendRequest(request);
    }

    /**
     * Get the message types that are of type emergency override.
     * 
     * @param comparator
     *            Comparator to sort the message types.
     * @return List of emergency override message types.
     * @throws Exception
     */
    public List<MessageType> getEmergencyOverrideMsgTypes(
            Comparator<MessageType> comparator) throws Exception {
        MessageTypeRequest req = new MessageTypeRequest();
        req.setAction(MessageTypeAction.GetEmergencyOverrideMsgTypes);
        req.setEmergencyOverride(true);

        MessageTypeResponse response = (MessageTypeResponse) BmhUtils
                .sendRequest(req);

        List<MessageType> messageTypeList = response.getMessageTypeList();
        if (messageTypeList == null) {
            return Collections.emptyList();
        }

        if (comparator != null && messageTypeList.isEmpty() == false) {
            Collections.sort(messageTypeList, comparator);
        }

        return messageTypeList;
    }

    /**
     * Returns the afos ids for all {@link MessageType}(s) that are considered
     * static.
     * 
     * @return a {@link Set} of all afos ids associated with static
     *         {@link MessageType}(s).
     * @throws Exception
     */
    public Set<String> getStaticMessageAfosIds() throws Exception {
        Set<String> timeAfosIds = this
                .getAfosIdsByDesignation(StaticMessageIdentifier.timeDesignation);
        Set<String> stationIdAfosIds = this
                .getAfosIdsByDesignation(StaticMessageIdentifier.stationIdDesignation);
        Set<String> staticAfosIds = new HashSet<>(timeAfosIds.size()
                + stationIdAfosIds.size(), 1.0f);
        staticAfosIds.addAll(timeAfosIds);
        staticAfosIds.addAll(stationIdAfosIds);

        return staticAfosIds;
    }
}
