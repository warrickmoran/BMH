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

import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.common.bmh.request.MessageTypeRequest;
import com.raytheon.uf.common.bmh.request.MessageTypeResponse;
import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.edex.bmh.dao.MessageTypeDao;

/**
 * Message Type Server Request Handler
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 22, 2014    3411    mpduff      Initial creation
 * Aug 5, 2014  #3490      lvenable    Updated to get Message types.
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class MessageTypeHandler implements IRequestHandler<MessageTypeRequest> {

    @Override
    public Object handleRequest(MessageTypeRequest request) throws Exception {
        MessageTypeResponse response = new MessageTypeResponse();

        switch (request.getAction()) {
        case AllMessageTypes:
            response = getMessageTypes();
            break;
        default:
            break;
        }

        return response;
    }

    private MessageTypeResponse getMessageTypes() {
        MessageTypeDao dao = new MessageTypeDao();
        MessageTypeResponse response = new MessageTypeResponse();

        List<MessageType> messageTypeList = dao.getMessgeTypes();
        response.setMessageTypeList(messageTypeList);

        return response;
    }
}
