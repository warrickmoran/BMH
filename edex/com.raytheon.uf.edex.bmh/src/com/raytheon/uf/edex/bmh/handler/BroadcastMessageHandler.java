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
import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.edex.bmh.dao.BroadcastMsgDao;

/**
 * Broadcast Message request handler.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 15, 2014     3432   mpduff      Initial creation
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class BroadcastMessageHandler implements
        IRequestHandler<BroadcastMsgRequest> {

    @Override
    public Object handleRequest(BroadcastMsgRequest request) throws Exception {
        BroadcastMsgResponse response = new BroadcastMsgResponse();

        switch (request.getAction()) {
        case GET_MESSAGE_BY_ID:
            response = getMessage(request);
            break;
        default:
            break;
        }

        return response;
    }

    private BroadcastMsgResponse getMessage(BroadcastMsgRequest request) {
        BroadcastMsgResponse response = new BroadcastMsgResponse();
        BroadcastMsgDao dao = new BroadcastMsgDao();
        List<BroadcastMsg> list = dao.getMessageByBroadcastId(request
                .getBroadcastMessageId());
        response.setMessageList(list);

        return response;
    }
}
