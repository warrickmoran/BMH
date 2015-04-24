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

import com.raytheon.uf.common.bmh.BMHLoggerUtils;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.common.bmh.notify.config.ConfigNotification.ConfigChangeType;
import com.raytheon.uf.common.bmh.notify.config.AbstractTraceableSystemConfigNotification;
import com.raytheon.uf.common.bmh.notify.config.MessageTypeConfigNotification;
import com.raytheon.uf.common.bmh.request.MessageTypeRequest;
import com.raytheon.uf.common.bmh.request.MessageTypeResponse;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.edex.bmh.BmhMessageProducer;
import com.raytheon.uf.edex.bmh.dao.MessageTypeDao;

/**
 * Handles any requests to get or modify the state of {@link MessageType}s
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Jul 22, 2014  3411     mpduff      Initial creation
 * Aug 05, 2014  3490     lvenable    Updated to get Message types.
 * Aug 17, 2014  3490     lvenable    Updated for deleting.
 * Aug 18, 2014  3411     mpduff      Added SaveMessageType
 * Aug 20, 2014  3432     mpduff      Added get by afosid and pk id
 * Sep 05, 2014  3554     bsteffen    Send config change notification.
 * Sep 15, 2014  3610     lvenable    Added GetAfosIdTitle capability.
 * Sep 19, 2014  3611     lvenable    Added emergency override capability..
 * Oct 07, 2014  3687     bsteffen    Handle non-operational requests.
 * Oct 13, 2014  3413     rferrel     Implement User roles.
 * Oct 20, 2014  3636     rferrel     Implement Logging.
 * Oct 23, 2014  3728     lvenable    Added method to get AFOS IDs by designation.
 * Mar 13, 2015  4213     bkowal      Added {@link #getByDesignationAndLanguage(MessageTypeRequest)}.
 * Apr 22, 2015  4397     bkowal      Construct a {@link AbstractTraceableSystemConfigNotification}
 *                                    notification when database changes occur.
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class MessageTypeHandler extends
        AbstractBMHServerRequestHandler<MessageTypeRequest> {

    @Override
    public Object handleRequest(MessageTypeRequest request) throws Exception {
        MessageTypeConfigNotification notification = null;
        MessageTypeResponse response = new MessageTypeResponse();
        switch (request.getAction()) {
        case AllMessageTypes:
            response = getMessageTypes(request);
            break;
        case Delete:
            deleteMessageType(request);
            notification = new MessageTypeConfigNotification(
                    ConfigChangeType.Delete, request, request.getMessageType());
            break;
        case GetAfosIdTitle:
            response = getMessageTypeAfosIdTitle(request);
            break;
        case Save:
            response = saveMessageType(request);
            notification = new MessageTypeConfigNotification(
                    ConfigChangeType.Update, request, request.getMessageType());
            break;
        case GetByAfosId:
            response = getByAfosId(request);
            break;
        case GetByPkId:
            response = getByPkId(request);
            break;
        case GetEmergencyOverrideMsgTypes:
            response = getEmergencyOverrideMsgTypes(request);
            break;
        case GetAfosDesignation:
            response = getAfosDesignation(request);
            break;
        case GetByDesignationAndLanguage:
            response = getByDesignationAndLanguage(request);
            break;
        default:
            throw new UnsupportedOperationException(this.getClass()
                    .getSimpleName()
                    + " cannot handle action "
                    + request.getAction());
        }

        BmhMessageProducer.sendConfigMessage(notification,
                request.isOperational());
        return response;
    }

    /**
     * Get a list of message types that have a specific designation.
     * 
     * @param request
     *            Message Type Request.
     * @return Message Type Response.
     */
    private MessageTypeResponse getAfosDesignation(MessageTypeRequest request) {
        MessageTypeDao dao = new MessageTypeDao(request.isOperational());
        MessageTypeResponse response = new MessageTypeResponse();

        List<MessageType> messageTypeList = dao.getAfosIdDesignation(request
                .getDesignation());
        response.setMessageTypeList(messageTypeList);

        return response;
    }

    private MessageTypeResponse getMessageTypeAfosIdTitle(
            MessageTypeRequest request) {
        MessageTypeDao dao = new MessageTypeDao(request.isOperational());
        MessageTypeResponse response = new MessageTypeResponse();

        List<MessageType> messageTypeList = dao.getMessgeTypeAfosIdTitle();
        response.setMessageTypeList(messageTypeList);

        return response;
    }

    /**
     * Get all of the message types fully populated.
     * 
     * @return Message Type Response.
     */
    private MessageTypeResponse getMessageTypes(MessageTypeRequest request) {
        MessageTypeDao dao = new MessageTypeDao(request.isOperational());
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
        MessageTypeDao dao = new MessageTypeDao(request.isOperational());
        MessageType messageType = request.getMessageType();
        dao.delete(messageType);

        IUFStatusHandler logger = BMHLoggerUtils.getSrvLogger(request);
        if (logger.isPriorityEnabled(Priority.INFO)) {
            String user = BMHLoggerUtils.getUser(request);
            BMHLoggerUtils.logDelete(request, user, messageType);
        }
    }

    private MessageTypeResponse saveMessageType(MessageTypeRequest request) {
        MessageTypeDao dao = new MessageTypeDao(request.isOperational());
        MessageType messageType = request.getMessageType();

        IUFStatusHandler logger = BMHLoggerUtils.getSrvLogger(request);

        MessageType oldType = null;
        if (logger.isPriorityEnabled(Priority.INFO)) {
            oldType = dao.getByID(messageType.getId());
        }

        dao.saveOrUpdate(messageType);

        MessageTypeResponse response = new MessageTypeResponse();
        List<MessageType> list = new ArrayList<>(1);
        list.add(request.getMessageType());
        response.setMessageTypeList(list);

        if (logger.isPriorityEnabled(Priority.INFO)) {
            String user = BMHLoggerUtils.getUser(request);
            BMHLoggerUtils.logSave(request, user, oldType, messageType);
        }

        return response;
    }

    private MessageTypeResponse getByAfosId(MessageTypeRequest request) {
        MessageTypeResponse response = new MessageTypeResponse();
        MessageTypeDao dao = new MessageTypeDao(request.isOperational());
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
        MessageTypeDao dao = new MessageTypeDao(request.isOperational());
        MessageType m = dao.getByID((int) request.getPkId());
        response.addMessageType(m);

        return response;
    }

    /**
     * Get the list of emergency override message types.
     * 
     * @param request
     *            Message type request.
     * @return Message type response.
     */
    private MessageTypeResponse getEmergencyOverrideMsgTypes(
            MessageTypeRequest request) {
        MessageTypeResponse response = new MessageTypeResponse();
        MessageTypeDao dao = new MessageTypeDao(request.isOperational());

        List<MessageType> mTypes = dao.getEmergencyOverride(request
                .isEmergencyOverride());
        response.setMessageTypeList(mTypes);

        return response;
    }

    private MessageTypeResponse getByDesignationAndLanguage(
            MessageTypeRequest request) {
        MessageTypeResponse response = new MessageTypeResponse();
        MessageTypeDao dao = new MessageTypeDao(request.isOperational());

        List<MessageType> mTypes = dao.getMessageTypeForDesignationAndLanguage(
                request.getDesignation(), request.getLanguage());
        response.setMessageTypeList(mTypes);

        return response;
    }
}
