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

import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;

import com.raytheon.uf.common.bmh.datamodel.msg.InputMessage;
import com.raytheon.uf.common.bmh.datamodel.msg.ValidatedMessage;
import com.raytheon.uf.common.bmh.trace.TraceableId;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.edex.bmh.msg.logging.IMessageLogger;

/**
 * 
 * DAO for {@link ValidatedMessage} Objects.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Jun 23, 2014  3283     bsteffen    Initial creation
 * Sep 02, 2014  3568     bkowal      Added getValidatedMsgByInputMsg
 * Oct 06, 2014  3687     bsteffen    Add operational flag to constructor.
 * Apr 16, 2015  4396     rferrel     Added {@link #getAllUnexpiredMessages(Calendar)}.
 * May 13, 2015  4429     rferrel     Added {@link #getByTraceableId(TraceableId)}.
 * Aug 10, 2015  4723     bkowal      Added {@link #getExpiredNonDeliveredMessages(Calendar)}.
 * Nov 24, 2015  5127     rjpeter     Updated persistCascade to set lastUpdateTime.
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class ValidatedMessageDao extends
        AbstractBMHPersistenceLoggingDao<ValidatedMessage, Integer> {

    public ValidatedMessageDao(final IMessageLogger messageLogger) {
        super(ValidatedMessage.class, messageLogger);
    }

    public ValidatedMessageDao(boolean operational,
            final IMessageLogger messageLogger) {
        super(operational, ValidatedMessage.class, messageLogger);
    }

    public void persistCascade(final ValidatedMessage msg) {
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                msg.getInputMessage().setLastUpdateTime(TimeUtil.newDate());
                persist(msg.getInputMessage());
                persist(msg);
            }
        });
    }

    public ValidatedMessage getValidatedMsgByInputMsg(
            final InputMessage inputMsg) {
        List<?> messages = findByNamedQueryAndNamedParam(
                ValidatedMessage.GET_VALIDATED_MSG_FOR_INPUT_MSG,
                "inputMessage", inputMsg);

        if ((messages == null) || messages.isEmpty()) {
            return null;
        }

        if ((messages.get(0) instanceof ValidatedMessage) == false) {
            return null;
        }

        return (ValidatedMessage) messages.get(0);
    }

    public List<ValidatedMessage> getExpiredNonDeliveredMessages(
            Calendar currentTime) {
        List<?> returnObjects = findByNamedQueryAndNamedParam(
                ValidatedMessage.GET_EXPIRED_VALIDATED_NON_DELIVERED_MSGS,
                "exprTime", currentTime);
        if (returnObjects.isEmpty()) {
            return Collections.emptyList();
        }

        List<ValidatedMessage> validatedMessages = new ArrayList<>(
                returnObjects.size());
        for (Object object : returnObjects) {
            validatedMessages.add((ValidatedMessage) object);
        }

        return validatedMessages;
    }

    @SuppressWarnings("unchecked")
    public List<ValidatedMessage> getAllUnexpiredMessages(Calendar currentTime) {
        return (List<ValidatedMessage>) findByNamedQueryAndNamedParam(
                ValidatedMessage.ALL_UNEXPIRED_VALIDATED_MSGS, "currentTime",
                currentTime);
    }

    /**
     * Get Validated Message using the TraceableId.
     * 
     * @param tId
     * @return validMessage
     */
    public ValidatedMessage getByTraceableId(TraceableId tId) {
        ValidatedMessage validatedMessage = getByID(tId.getId());
        validatedMessage.setTraceId(tId.getTraceId());
        return validatedMessage;
    }
}