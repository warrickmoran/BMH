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
package com.raytheon.uf.edex.bmh.handler;

import java.util.ArrayList;
import java.util.List;

import com.raytheon.uf.common.bmh.broadcast.ExpireBroadcastMsgRequest;
import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg;
import com.raytheon.uf.common.bmh.notify.config.MessageForcedExpirationNotification;
import com.raytheon.uf.edex.bmh.BmhMessageProducer;
import com.raytheon.uf.edex.bmh.dao.BroadcastMsgDao;
import com.raytheon.uf.edex.bmh.msg.logging.IMessageLogger;
import com.raytheon.uf.edex.bmh.status.BMHStatusHandler;
import com.raytheon.uf.edex.bmh.status.IBMHStatusHandler;

/**
 * Handles expiring broadcast messages on specific Transmitters.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 15, 2015 4293       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class ExpireBroadcastMsgHandler extends
        AbstractBMHLoggingServerRequestHandler<ExpireBroadcastMsgRequest> {

    private final IBMHStatusHandler statusHandler = BMHStatusHandler
            .getInstance(getClass());

    protected ExpireBroadcastMsgHandler(IMessageLogger opMessageLogger,
            IMessageLogger pracMessageLogger) {
        super(opMessageLogger, pracMessageLogger);
    }

    @Override
    public Object handleRequest(ExpireBroadcastMsgRequest request)
            throws Exception {
        if (request.getExpiredBroadcastMsgs().isEmpty()) {
            return null;
        }

        List<Long> expiredIds = new ArrayList<>(request
                .getExpiredBroadcastMsgs().size());
        List<BroadcastMsg> expiredMsgs = request.getExpiredBroadcastMsgs();
        for (BroadcastMsg msg : expiredMsgs) {
            msg.setForcedExpiration(Boolean.TRUE);
            expiredIds.add(msg.getId());
            statusHandler.info("Forcefully expiring broadcast message: "
                    + msg.getId() + ".");
        }

        final BroadcastMsgDao broadcastMsgDao = new BroadcastMsgDao(
                request.isOperational(), this.getMessageLogger(request));
        broadcastMsgDao.persistAll(expiredMsgs);

        /*
         * Notify the playlist manager.
         */
        MessageForcedExpirationNotification notification = new MessageForcedExpirationNotification();
        notification.setBroadcastIds(expiredIds);
        BmhMessageProducer.sendConfigMessage(notification,
                request.isOperational());

        return null;
    }
}