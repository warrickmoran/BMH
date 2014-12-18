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

import java.util.List;

import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg;
import com.raytheon.uf.common.bmh.request.BroadcastMsgRequest;
import com.raytheon.uf.common.bmh.request.BroadcastMsgResponse;
import com.raytheon.uf.edex.bmh.dao.BroadcastMsgDao;

/**
 * Handles any requests to get or modify the state of {@link BroadcastMsg}s
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Aug 15, 2014  3432     mpduff      Initial creation
 * Oct 07, 2014  3687     bsteffen    Handle non-operational requests.
 * Oct 13, 2014  3413     rferrel     Implement User roles.
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class BroadcastMessageHandler extends
        AbstractBMHServerRequestHandler<BroadcastMsgRequest> {

    @Override
    public Object handleRequest(BroadcastMsgRequest request) throws Exception {
        BroadcastMsgResponse response = new BroadcastMsgResponse();

        switch (request.getAction()) {
        case GET_MESSAGE_BY_ID:
            response = getMessageById(request);
            break;
        case GET_MESSAGE_BY_INPUT_ID:
            response = getMessagesByInputId(request);
            break;
        default:
            throw new UnsupportedOperationException(this.getClass()
                    .getSimpleName()
                    + " cannot handle action "
                    + request.getAction());
        }

        return response;
    }

    private BroadcastMsgResponse getMessageById(BroadcastMsgRequest request) {
        BroadcastMsgResponse response = new BroadcastMsgResponse();
        BroadcastMsgDao dao = new BroadcastMsgDao(request.isOperational());
        List<BroadcastMsg> list = dao.getMessageByBroadcastId(request
                .getMessageId());
        response.setMessageList(list);

        return response;
    }

    private BroadcastMsgResponse getMessagesByInputId(
            BroadcastMsgRequest request) {
        BroadcastMsgResponse response = new BroadcastMsgResponse();
        BroadcastMsgDao dao = new BroadcastMsgDao(request.isOperational());
        List<BroadcastMsg> list = dao.getMessagesByInputMsgId(request
                .getMessageId()
                .intValue());
        response.setMessageList(list);

        return response;
    }
}
