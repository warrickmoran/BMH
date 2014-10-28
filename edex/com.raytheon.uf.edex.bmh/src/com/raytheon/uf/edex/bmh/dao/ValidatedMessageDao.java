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

import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;

import com.raytheon.uf.common.bmh.datamodel.msg.InputMessage;
import com.raytheon.uf.common.bmh.datamodel.msg.ValidatedMessage;

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
 * 
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class ValidatedMessageDao extends
        AbstractBMHDao<ValidatedMessage, Integer> {

    public ValidatedMessageDao() {
        super(ValidatedMessage.class);
    }

    public ValidatedMessageDao(boolean operational) {
        super(operational, ValidatedMessage.class);
    }

    public void persistCascade(final ValidatedMessage msg) {
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
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
}