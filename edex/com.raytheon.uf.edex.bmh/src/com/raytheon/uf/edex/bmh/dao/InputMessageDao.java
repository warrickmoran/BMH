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
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import com.raytheon.uf.common.bmh.datamodel.msg.InputMessage;
import com.raytheon.uf.edex.bmh.msg.logging.IMessageLogger;

/**
 * 
 * DAO for {@link InputMessage} objects.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Jun 23, 2014  3283     bsteffen    Initial creation
 * Aug 15, 2014  3432     mpduff      Added getPeriodicMessages
 * Oct 06, 2014  3687     bsteffen    Add operational flag to constructor.
 * Nov 03, 2014  3790     lvenable    Added code for the active field.
 * Nov 26, 2014  3613     bsteffen    Add getPurgableMessages
 * Jan 06, 2015  3651     bkowal      Support AbstractBMHPersistenceLoggingDao.
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class InputMessageDao extends
        AbstractBMHPersistenceLoggingDao<InputMessage, Integer> {

    public InputMessageDao(final IMessageLogger messageLogger) {
        super(InputMessage.class, messageLogger);
    }

    public InputMessageDao(boolean operational,
            final IMessageLogger messageLogger) {
        super(operational, InputMessage.class, messageLogger);
    }

    /**
     * Search the database for any messages which can be considered duplicates
     * of this message.
     * 
     * @param message
     *            InputMessage to find duplicates.
     * @return true if duplicates exist, false otherwise
     * @see InputMessage#equalsExceptId(Object)
     */
    public boolean checkDuplicate(final InputMessage message) {
        List<?> messages = findByNamedQueryAndNamedParam(
                InputMessage.DUP_QUERY_NAME,
                new String[] { "id", "afosid", "mrd", "effectiveTime",
                        "expirationTime" },
                new Object[] { message.getId(), message.getAfosid(),
                        message.getMrd(), message.getEffectiveTime(),
                        message.getExpirationTime() });
        for (Object obj : messages) {
            InputMessage dup = (InputMessage) obj;
            if (dup.getId() == message.getId()) {
                continue;
            } else if (!dup.getAfosid().equals(message.getAfosid())) {
                continue;
            } else if (!dup.getAreaCodeList().containsAll(
                    message.getAreaCodeList())) {
                continue;
            }
            int mrd = message.getMrdId();
            if (mrd != -1 && mrd == dup.getMrdId()) {
                return true;
            } else if (dup.getContent().equals(message.getContent())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get a list of Periodic messages
     * 
     * @return
     */
    public List<InputMessage> getPeriodicMessages() {
        // TODO optimize this query
        List<InputMessage> allObjects = this.loadAll();
        if (allObjects == null) {
            return Collections.emptyList();
        }

        List<InputMessage> messageList = new ArrayList<InputMessage>();
        for (Object obj : allObjects) {
            InputMessage msg = (InputMessage) obj;
            if (msg.isPeriodic()) {
                messageList.add(msg);
            }
        }

        return messageList;
    }

    /**
     * Get all of the input messages fully populated.
     * 
     * @return List of Input Messages.
     */
    public List<InputMessage> getInputMessages() {
        List<InputMessage> inputMessageList = this.loadAll();
        if (inputMessageList == null) {
            return Collections.emptyList();
        }
        return inputMessageList;
    }

    /**
     * Get all of the input messages with only the Id, Name, Afos Id, and
     * Creation time populated.
     * 
     * @return List of input messages.
     */
    public List<InputMessage> getInputMsgsIdNameAfosCreation() {
        List<Object[]> objectList = getInputMessagesByQuery(InputMessage.GET_INPUT_MSGS_ID_NAME_AFOS_CREATION);

        if (objectList == null) {
            return Collections.emptyList();
        }

        List<InputMessage> inputMessages = createInputMessageIdNameAfosCreation(objectList);

        return inputMessages;
    }

    /**
     * Get the input messages using the specified query.
     * 
     * @param inputMessageQuery
     *            Query.
     * @return List of input message objects.
     */
    @SuppressWarnings("unchecked")
    private List<Object[]> getInputMessagesByQuery(
            final String inputMessageQuery) {
        return (List<Object[]>) findByNamedQuery(inputMessageQuery);
    }

    /**
     * Create the list of input messages.
     * 
     * @param objectList
     *            List of object arrays.
     * @return List of input messages.
     */
    private List<InputMessage> createInputMessageIdNameAfosCreation(
            List<Object[]> objectList) {

        List<InputMessage> imList = new ArrayList<InputMessage>(
                objectList.size());

        for (Object[] objArray : objectList) {
            InputMessage im = new InputMessage();
            im.setId((int) objArray[0]);
            im.setName((String) objArray[1]);
            im.setAfosid((String) objArray[2]);
            im.setCreationTime((Calendar) objArray[3]);
            im.setActive((Boolean) objArray[4]);
            imList.add(im);
        }

        return imList;
    }

    @SuppressWarnings("unchecked")
    public List<InputMessage> getPurgableMessages(final Calendar purgeTime) {
        return (List<InputMessage>) findByNamedQueryAndNamedParam(
                InputMessage.PURGE_QUERY_NAME, "purgeTime", purgeTime);

    }

}
