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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

import com.raytheon.uf.common.bmh.datamodel.language.Language;
import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.edex.bmh.msg.logging.IMessageLogger;

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
 * Oct 23, 2014  3748     bkowal      Added getMessagesByInputMsgId
 * Nov 26, 2014  3613     bsteffen    Add getMessageByFragmentPath
 * Dec 08, 2014  3864     bsteffen    Redo some of the playlist manager queries.
 * Jan 06, 2015  3651     bkowal      Support AbstractBMHPersistenceLoggingDao.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class BroadcastMsgDao extends
        AbstractBMHPersistenceLoggingDao<BroadcastMsg, Long> {

    public BroadcastMsgDao(final IMessageLogger messageLogger) {
        super(BroadcastMsg.class, messageLogger);
    }

    public BroadcastMsgDao(boolean operational,
            final IMessageLogger messageLogger) {
        super(operational, BroadcastMsg.class, messageLogger);
    }

    @SuppressWarnings("unchecked")
    public List<BroadcastMsg> getUnexpiredMessagesByAfosidsAndGroup(
            final List<String> afosids, final Calendar expirationTime,
            final TransmitterGroup group) {
        return (List<BroadcastMsg>) findByNamedQueryAndNamedParam(
                BroadcastMsg.GET_UNEXPIRED_MSGS_BY_AFOS_IDS_AND_GROUP,
                new String[] { "afosIDs", "expirationTime", "group" },
                new Object[] { afosids, expirationTime, group });
    }

    public BroadcastMsg getMessageExistence(final TransmitterGroup group,
            final String afosId, final Language language) {
        @SuppressWarnings("unchecked")
        List<BroadcastMsg> messages = (List<BroadcastMsg>) findByNamedQueryAndNamedParam(
                BroadcastMsg.GET_MSGS_BY_AFOS_ID_GROUP_AND_LANGUAGE,
                new String[] { "afosId", "group", "language" }, new Object[] {
                        afosId, group, language });

        if ((messages == null) || messages.isEmpty()) {
            return null;
        }

        if ((messages.get(0) instanceof BroadcastMsg) == false) {
            return null;
        }

        return messages.get(0);
    }

    public List<BroadcastMsg> getMessagesByInputMsgId(final int inputMsgId) {
        return txTemplate
                .execute(new TransactionCallback<List<BroadcastMsg>>() {
                    @Override
                    public List<BroadcastMsg> doInTransaction(
                            TransactionStatus status) {
                        List<?> objects = findByNamedQueryAndNamedParam(
                                BroadcastMsg.GET_MSGS_BY_INPUT_MSG,
                                "inputMsgId", inputMsgId);

                        if ((objects == null) || objects.isEmpty()) {
                            return Collections.emptyList();
                        }

                        List<BroadcastMsg> msgs = new ArrayList<BroadcastMsg>(
                                objects.size());
                        for (Object object : objects) {
                            if (object instanceof BroadcastMsg) {
                                msgs.add((BroadcastMsg) object);
                            }
                        }

                        return msgs;
                    }
                });
    }

    public List<BroadcastMsg> getMessageByBroadcastId(Long broadcastMessageId) {
        BroadcastMsg msg = this.getByID(broadcastMessageId);
        List<BroadcastMsg> results = new ArrayList<>();
        results.add(msg);

        return results;
    }

    public BroadcastMsg getMessageByFragmentPath(Path path) {
        List<?> messages = findByNamedQueryAndNamedParam(
                BroadcastMsg.GET_MSG_BY_FRAGMENT_PATH, "path", path
                        .toAbsolutePath().toString());
        if (messages.isEmpty()) {
            return null;
        } else {
            return (BroadcastMsg) messages.get(0);
        }
    }
}