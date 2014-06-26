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

import java.util.List;

import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

import com.raytheon.uf.common.bmh.datamodel.msg.InputMessage;

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
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class InputMessageDao extends AbstractBMHDao<InputMessage, Integer> {

    public InputMessageDao() {
        super(InputMessage.class);
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
        List<?> messages = txTemplate
                .execute(new TransactionCallback<List<?>>() {
            @Override
                    public List<?> doInTransaction(TransactionStatus status) {
                HibernateTemplate ht = getHibernateTemplate();
                return ht.findByNamedQueryAndNamedParam(
                        InputMessage.DUP_QUERY_NAME,
                        new String[] { "id", "afosid", "areaCodes", "mrd",
                                "effectiveTime", "expirationTime" },
                        new Object[] { message.getId(), message.getAfosid(),
                                message.getAreaCodes(), message.getMrd(),
                                message.getEffectiveTime(),
                                message.getExpirationTime() });
            }
                });
        for (Object obj : messages) {
            InputMessage dup = (InputMessage) obj;
            if (dup.getId() == message.getId()) {
                continue;
            } else if (!dup.getAfosid().equals(message.getAfosid())) {
                continue;
            }
            if (dup.getAreaCodeList().containsAll(message.getAreaCodeList())) {
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

}
