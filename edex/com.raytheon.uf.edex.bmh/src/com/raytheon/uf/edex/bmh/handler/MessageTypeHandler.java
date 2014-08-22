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
 * Aug 17, 2014 #3490      lvenable    Updated for deleting.
 * Aug 18, 2014  3411      mpduff      Added SaveMessageType
 * Aug 20, 2014  3432      mpduff      Added get by afosid and pk id
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
        case Delete:
            deleteMessageType(request);
            break;
        case Save:
            response = saveMessageType(request);
            break;
        case GetByAfosId:
            response = getByAfosId(request);
            break;
        case GetByPkId:
            response = getByPkId(request);
            break;
        default:
            break;
        }

        return response;
    }

    /**
     * Get all of the message types fully populated.
     * 
     * @return Message Type Response.
     */
    private MessageTypeResponse getMessageTypes() {
        MessageTypeDao dao = new MessageTypeDao();
        MessageTypeResponse response = new MessageTypeResponse();

        List<MessageType> messageTypeList = dao.getMessgeTypes();
        response.setMessageTypeList(messageTypeList);

        return response;
    }

    /**
     * Delete the specified message type.
     * 
     * @param request
     *            Message type request.
     */
    private void deleteMessageType(MessageTypeRequest request) {
        MessageTypeDao dao = new MessageTypeDao();
        if (request != null) {
            dao.delete(request.getMessageType());
        }
    }

    private MessageTypeResponse saveMessageType(MessageTypeRequest request) {
        MessageTypeDao dao = new MessageTypeDao();
        if (request != null) {
            dao.saveOrUpdate(request.getMessageType());
        }

        MessageTypeResponse response = new MessageTypeResponse();
        List<MessageType> list = new ArrayList<>(1);
        list.add(request.getMessageType());
        response.setMessageTypeList(list);

        return response;
    }

    private MessageTypeResponse getByAfosId(MessageTypeRequest request) {
        MessageTypeResponse response = new MessageTypeResponse();
        MessageTypeDao dao = new MessageTypeDao();
        MessageType mt = dao.getByAfosId(request.getAfosId());
        List<MessageType> list = new ArrayList<>(1);
        if (mt != null) {
            list.add(mt);
        }
        response.setMessageTypeList(list);

        return response;
    }

    private MessageTypeResponse getByPkId(MessageTypeRequest request) {
        MessageTypeResponse response = new MessageTypeResponse();
        MessageTypeDao dao = new MessageTypeDao();
        MessageType m = dao.getByID((int) request.getPkId());
        response.addMessageType(m);

        return response;
    }
}
