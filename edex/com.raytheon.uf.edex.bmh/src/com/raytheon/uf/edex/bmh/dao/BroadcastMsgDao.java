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
import java.util.List;

import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

import com.raytheon.uf.common.bmh.datamodel.language.Language;
import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;

/**
 * BMH DAO for {@link BroadcastMsg}.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Jun 26, 2014  3302     bkowal      Initial creation
 * Jul 10, 2014  3285     bsteffen    Add getMessagesByAfosid()
 * Aug 20, 2014  3432     mpduff      Added getMessageByBroadcastId, fixed GetMesageByAfosid
 * Aug 24, 2014  3432     mpduff      Fixed getMessageByBroadcastId
 * Sep 03, 2014  3554     bsteffen    Add getUnexpiredBroadcastMsgsByAfosIDAndGroup
 * Aug 29, 2014  3568     bkowal      Added getMessageExistence
 * Oct 06, 2014  3687     bsteffen    Add operational flag to constructor.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class BroadcastMsgDao extends AbstractBMHDao<BroadcastMsg, Long> {

    public BroadcastMsgDao() {
        super(BroadcastMsg.class);
    }

    public BroadcastMsgDao(boolean operational) {
        super(operational, BroadcastMsg.class);
    }

    public List<BroadcastMsg> getMessagesByAfosid(final String afosid) {
        List<?> messages = txTemplate
                .execute(new TransactionCallback<List<?>>() {
                    @Override
                    public List<?> doInTransaction(TransactionStatus status) {
                        HibernateTemplate ht = getHibernateTemplate();
                        return ht.findByNamedQueryAndNamedParam(
                                BroadcastMsg.GET_MSGS_BY_AFOS_ID,
                                new String[] { "afosId" },
                                new String[] { afosid });
                    }
                });
        @SuppressWarnings("unchecked")
        List<BroadcastMsg> result = (List<BroadcastMsg>) messages;
        return result;
    }

    public List<BroadcastMsg> getUnexpiredMessagesByAfosidAndGroup(
            final String afosid, final Calendar expirationTime,
            final TransmitterGroup group) {
        List<?> messages = txTemplate
                .execute(new TransactionCallback<List<?>>() {
                    @Override
                    public List<?> doInTransaction(TransactionStatus status) {
                        HibernateTemplate ht = getHibernateTemplate();
                        return ht.findByNamedQueryAndNamedParam(
                                        BroadcastMsg.GET_UNEXPIRED_MSGS_BY_AFOS_ID_AND_GROUP,
                                        new String[] {
                                        "afosID", "expirationTime", "group" },
                                new Object[] { afosid, expirationTime, group });
                    }
                });
        @SuppressWarnings("unchecked")
        List<BroadcastMsg> result = (List<BroadcastMsg>) messages;
        return result;
    }

    public BroadcastMsg getMessageExistence(final TransmitterGroup group,
            final String afosId, final Language language) {
        List<?> messages = txTemplate
                .execute(new TransactionCallback<List<?>>() {
                    @Override
                    public List<?> doInTransaction(TransactionStatus status) {
                        HibernateTemplate ht = getHibernateTemplate();
                        return ht
                                .findByNamedQueryAndNamedParam(
                                        BroadcastMsg.GET_MSGS_BY_AFOS_ID_GROUP_AND_LANGUAGE,
                                        new String[] { "afosId", "group",
                                                "language" }, new Object[] {
                                                afosId, group, language });
                    }
                });

        if (messages == null || messages.isEmpty()) {
            return null;
        }

        if (messages.get(0) instanceof BroadcastMsg == false) {
            return null;
        }

        return (BroadcastMsg) messages.get(0);
    }

    public List<BroadcastMsg> getMessageByBroadcastId(Long broadcastMessageId) {
        BroadcastMsg msg = this.getByID(broadcastMessageId);
        List<BroadcastMsg> results = new ArrayList<>();
        results.add(msg);

        return results;
    }
}