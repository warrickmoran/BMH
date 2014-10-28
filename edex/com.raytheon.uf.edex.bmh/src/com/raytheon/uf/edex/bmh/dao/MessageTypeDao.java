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
package com.raytheon.uf.edex.bmh.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType.Designation;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageTypeSummary;

/**
 * BMH DAO for {@link MessageType}.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Jun 24, 2014  3302     bkowal      Initial creation
 * Aug 05, 2014  3490     lvenable    added getMessageTypes()
 * Sep 02, 2014  3568     bkowal      Added getMessageTypeForDesignation
 * Sep 15, 2014  3610     lvenable    Added methods to get message type
 *                                    Afos ID and title.
 * Sep 19, 2014  3611     lvenable    Added emergency override.
 * Oct 06, 2014  3687     bsteffen    Add operational flag to constructor.
 * Oct 13, 2014  3654     rjpeter     Updated to use MessageTypeSummary.
 * Oct 23, 2014  3728     lvenable    Added method to get AFOS IDs by designation.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class MessageTypeDao extends AbstractBMHDao<MessageType, Integer> {

    public MessageTypeDao() {
        super(MessageType.class);
    }

    public MessageTypeDao(boolean operational) {
        super(operational, MessageType.class);
    }

    /**
     * Get a list of fully populate message types.
     * 
     * @return List of message types.
     */
    public List<MessageType> getMessgeTypes() {
        List<MessageType> messageTypeList = this.loadAll();
        if (messageTypeList == null) {
            return Collections.emptyList();
        }
        return messageTypeList;
    }

    /**
     * Get the list of message types with the Afos ID and Title populated.
     * 
     * @return List of Message Types.
     */
    public List<MessageType> getMessgeTypeAfosIdTitle() {
        List<Object[]> objectList = getMessageTypeByQuery(MessageType.GET_MESSAGETYPE_AFOSID_TITLE);

        if (objectList == null) {
            return Collections.emptyList();
        }

        List<MessageType> messageTypeList = createMessageTypeAfosIdTitle(objectList);
        return messageTypeList;
    }

    /**
     * The the message type information by the query passed in.
     * 
     * @param messageTypeQuery
     *            Message type query to use.
     * @return List of data objects.
     */
    @SuppressWarnings("unchecked")
    private List<Object[]> getMessageTypeByQuery(final String messageTypeQuery) {
        return (List<Object[]>) findByNamedQuery(messageTypeQuery);
    }

    /**
     * Convert the list of object data into a list of message type data.
     * 
     * @param objectList
     *            List of objects.
     * @return List of message types.
     */
    private List<MessageType> createMessageTypeAfosIdTitle(
            List<Object[]> objectList) {
        List<MessageType> messageTypeList = new ArrayList<MessageType>(
                objectList.size());

        for (Object[] objArray : objectList) {
            MessageType mt = new MessageType();
            mt.setAfosid((String) objArray[0]);
            mt.setTitle((String) objArray[1]);
            messageTypeList.add(mt);
        }

        return messageTypeList;
    }

    /**
     * Looks up the MessageType for the given afosId.
     * 
     * @param afosId
     * @return
     */
    public MessageType getByAfosId(final String afosId) {
        @SuppressWarnings("unchecked")
        List<MessageType> types = (List<MessageType>) findByNamedQueryAndNamedParam(
                MessageType.GET_MESSAGETYPE_FOR_AFOSID, "afosid", afosId);
        if ((types != null) && !types.isEmpty()) {
            return types.get(0);
        }

        return null;
    }

    /**
     * Get the list of emergency override message types.
     * 
     * @param eoFlag
     *            Emergency Override flag to indicate if you want the message
     *            types that are emergency override or not.
     * @return List of message types.
     */
    public List<MessageType> getEmergencyOverride(final boolean eoFlag) {
        @SuppressWarnings("unchecked")
        List<MessageType> types = (List<MessageType>) findByNamedQueryAndNamedParam(
                MessageType.GET_MESSAGETYPE_FOR_EMERGENCYOVERRIDE,
                "emergencyOverride", eoFlag);
        if (types == null) {
            return Collections.emptyList();
        }

        return types;
    }

    /**
     * Returns a list of {@link MessageType} for the specified
     * {@link Designation}.
     * 
     * @param designation
     * @return
     */
    public List<MessageType> getMessageTypeForDesignation(
            final Designation designation) {
        List<?> types = findByNamedQueryAndNamedParam(
                MessageType.GET_MESSAGETYPE_FOR_DESIGNATION, "designation",
                designation);

        if ((types == null) || types.isEmpty()) {
            return null;
        }

        List<MessageType> messageTypes = new ArrayList<>(types.size());
        for (Object type : types) {
            if (type instanceof MessageType) {
                messageTypes.add((MessageType) type);
            }
        }

        return messageTypes;
    }

    /**
     * Returns a set of replacement afos ids for a given afos id.
     * 
     * @param afosId
     * @return
     */
    @SuppressWarnings("unchecked")
    public Set<String> getReplacementAfosIdsForAfosId(final String afosId) {
        List<MessageTypeSummary> msgTypes = (List<MessageTypeSummary>) findByNamedQueryAndNamedParam(
                MessageType.GET_REPLACEMENT_AFOSIDS, "afosid", afosId);
        Set<String> rval = new HashSet<>(
                msgTypes == null ? 1 : msgTypes.size(), 1);

        if (msgTypes != null) {
            for (MessageTypeSummary msgType : msgTypes) {
                rval.add(msgType.getAfosid());
            }
        }

        return rval;
    }

    /**
     * Get a list of message types that match the specified designation.
     * 
     * @param designation
     *            Message Type designation.
     * @return List of message types.
     */
    @SuppressWarnings("unchecked")
    public List<MessageType> getAfosIdDesignation(final Designation designation) {
        List<Object[]> objectList = (List<Object[]>) findByNamedQueryAndNamedParam(
                MessageType.GET_MESSAGETYPE_AFOSID_DESIGNATION, "designation",
                designation);

        if (objectList == null) {
            return Collections.emptyList();
        }

        List<MessageType> messageTypeList = new ArrayList<MessageType>(
                objectList.size());

        for (Object[] objArray : objectList) {
            MessageType mt = new MessageType();
            mt.setAfosid((String) objArray[0]);
            mt.setDesignation(Designation.valueOf((String) objArray[1]));
            messageTypeList.add(mt);
        }

        return messageTypeList;
    }
}