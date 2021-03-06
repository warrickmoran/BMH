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

import org.springframework.util.CollectionUtils;

import com.raytheon.uf.common.bmh.datamodel.language.Language;
import com.raytheon.uf.common.bmh.datamodel.msg.InputMessage;
import com.raytheon.uf.common.bmh.datamodel.msg.InputMessage.Origin;
import com.raytheon.uf.common.time.util.TimeUtil;
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
 * Jan 02, 2014   3833    lvenable    Added method to get unexpired messages.
 * Jan 06, 2015  3651     bkowal      Support AbstractBMHPersistenceLoggingDao.
 * Mar 25, 2015  4290     bsteffen    Switch to global replacement.
 * Apr 01, 2015  4326     bsteffen    Allow reuse of MRD after old message expires.
 * Apr 16, 2015  4395     rferrel     Added {@link #getAllUnexpiredInputMessages(Calendar)}.
 * May 11, 2015  4476     bkowal      Added {@link #getAllWithAfosIdAndName(String, String)}.
 * May 18, 2015  4483     bkowal      Contents are now used in {@link #checkDuplicate(InputMessage)}.
 * Nov 16, 2015  5127     rjpeter     Added getActiveWithAfosidAndAreaCodesAndNoMrd, overrode saveOrUpdate
 *                                    to set lastUpdateTime.
 * Feb 04, 2016  5308     rjpeter     Removed checkDuplicate, getPeriodicMessages, and getInputMessages.
<<<<<<< HEAD
 * May 17, 2017  19315      xwei      Updated createInputMessageIdNameAfosCreation so Effective Time can be assigned
=======
 * Jan 19, 2017  6078     bkowal      Updated {@link #createInputMessageIdNameAfosCreation(List)} to
 *                                    handle retrieval of the origin column.
>>>>>>> origin/omaha_17.3.1
 * </pre>
 * 
 * @author bsteffen
 */
public class InputMessageDao
        extends AbstractBMHPersistenceLoggingDao<InputMessage, Integer> {

    public InputMessageDao(final IMessageLogger messageLogger) {
        super(InputMessage.class, messageLogger);
    }

    public InputMessageDao(boolean operational,
            final IMessageLogger messageLogger) {
        super(operational, InputMessage.class, messageLogger);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.edex.bmh.dao.AbstractBMHPersistenceLoggingDao#
     * saveOrUpdate (java.lang.Object)
     */
    @Override
    public void saveOrUpdate(Object obj) {
        if (obj instanceof InputMessage) {
            InputMessage msg = (InputMessage) obj;
            msg.setLastUpdateTime(TimeUtil.newDate());
        }

        super.saveOrUpdate(obj);
    }

    /**
     * Get all of the input messages with only the Id, Name, Afos Id, and
     * Creation time populated.
     * 
     * @return List of input messages.
     */
    public List<InputMessage> getInputMsgsIdNameAfosCreation() {
        List<Object[]> objectList = getInputMessagesByQuery(
                InputMessage.GET_INPUT_MSGS_ID_NAME_AFOS_CREATION);

        if (objectList == null) {
            return Collections.emptyList();
        }

        List<InputMessage> inputMessages = createInputMessageIdNameAfosCreation(
                objectList);

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

        List<InputMessage> imList = new ArrayList<>(objectList.size());

        for (Object[] objArray : objectList) {
            InputMessage im = new InputMessage();
            im.setId((int) objArray[0]);
            im.setName((String) objArray[1]);
            im.setAfosid((String) objArray[2]);
            im.setCreationTime((Calendar) objArray[3]);
            im.setActive((Boolean) objArray[4]);
            im.setEffectiveTime((Calendar) objArray[5]);      
            im.setOrigin((Origin) objArray[6]);
            imList.add(im);
        }

        return imList;
    }
    
    
    @SuppressWarnings("unchecked")
    public List<InputMessage> getPurgableMessages(final Calendar purgeTime) {
        return (List<InputMessage>) findByNamedQueryAndNamedParam(
                InputMessage.PURGE_QUERY_NAME, "purgeTime", purgeTime);

    }

    @SuppressWarnings("unchecked")
    public List<InputMessage> getUnexpiredInputMessages(
            final Calendar currentTime) {

        List<Object[]> objectList = (List<Object[]>) findByNamedQueryAndNamedParam(
                InputMessage.UNEXPIRED_QUERY_NAME, "currentTime", currentTime);

        return createInputMessageIdNameAfosCreation(objectList); 
    }

    @SuppressWarnings("unchecked")
    public List<InputMessage> getAllUnexpiredInputMessages(
            final Calendar currentTime) {
        return (List<InputMessage>) findByNamedQueryAndNamedParam(
                InputMessage.ALL_UNEXPIRED_QUERY_NAME, "currentTime",
                currentTime);
    }

    public List<InputMessage> getActiveWithAfosidAndAreaCodes(String afosid,
            String areaCodes, Calendar expireAfter, Language language) {
        String[] names = { "afosid", "areaCodes", "expireAfter", "language" };
        Object[] values = { afosid, areaCodes, expireAfter, language };
        @SuppressWarnings("unchecked")
        List<InputMessage> result = (List<InputMessage>) findByNamedQueryAndNamedParam(
                InputMessage.ACTIVE_WITH_AFOSID_AND_AREACODES_QUERY_NAME, names,
                values);
        return result;
    }

    public List<InputMessage> getActiveWithAfosidAndAreaCodesAndNoMrd(
            String afosid, String areaCodes, Calendar expireAfter,
            Language language) {
        String[] names = { "afosid", "areaCodes", "expireAfter", "language" };
        Object[] values = { afosid, areaCodes, expireAfter, language };
        @SuppressWarnings("unchecked")
        List<InputMessage> result = (List<InputMessage>) findByNamedQueryAndNamedParam(
                InputMessage.ACTIVE_WITH_AFOSID_AND_AREACODES_AND_NO_MRD_QUERY_NAME,
                names, values);
        return result;
    }

    public List<InputMessage> getActiveWithMrdLike(String mrdLike,
            Calendar expireAfter, Language language) {
        String[] names = { "mrdLike", "expireAfter", "language" };
        Object[] values = { mrdLike, expireAfter, language };
        @SuppressWarnings("unchecked")
        List<InputMessage> result = (List<InputMessage>) findByNamedQueryAndNamedParam(
                InputMessage.ACTIVE_WITH_MRD_LIKE_QUERY_NAME, names, values);
        return result;
    }

    public List<InputMessage> getAllWithAfosIdAndName(String afosId,
            String name) {
        final String[] names = { "afosid", "name" };
        final String[] values = { afosId, name };
        List<?> results = this.findByNamedQueryAndNamedParam(
                InputMessage.ALL_WITH_NAME_AND_AFOSID, names, values);
        if (CollectionUtils.isEmpty(results)) {
            return Collections.emptyList();
        }

        List<InputMessage> inputMessages = new ArrayList<>(results.size());
        for (Object object : results) {
            inputMessages.add((InputMessage) object);
        }
        return inputMessages;
    }
}
