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
import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.notify.config.ConfigNotification.ConfigChangeType;
import com.raytheon.uf.common.bmh.notify.config.TransmitterGroupConfigNotification;
import com.raytheon.uf.common.bmh.request.TransmitterRequest;
import com.raytheon.uf.common.bmh.request.TransmitterResponse;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.edex.bmh.BmhMessageProducer;
import com.raytheon.uf.edex.bmh.dao.TransmitterDao;
import com.raytheon.uf.edex.bmh.dao.TransmitterGroupDao;

/**
 * Thrift handler for {@link Transmitter} and {@link TransmitterGroup} objects
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Jul 30, 2014  3173     mpduff      Initial creation
 * Aug 19, 2014  3486     bsteffen    Send change notification over jms.
 * Aug 24, 2014  3432     mpduff      Added getEnabledTransmitterGroups()
 * Sep 05, 2014  3554     bsteffen    Send more specific config change notification.
 * Sep 23, 2014  3649     rferrel     DeleteTransmitterGroup notification no longer
 *                                    causes null pointer exception.
 * Oct 07, 2014  3687     bsteffen    Handle non-operational requests.
 * Oct 13, 2014  3413     rferrel     Implement User roles.
 * Oct 15, 2014  3636     rferrel     Implement logging of changes.
 * Nov 21, 2014  3845     bkowal      Added getTransmitterGroupWithTransmitter
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class TransmitterHandler extends
        AbstractBMHServerRequestHandler<TransmitterRequest> {

    @Override
    public Object handleRequest(TransmitterRequest request) throws Exception {
        TransmitterGroupConfigNotification notification = null;
        TransmitterResponse response = new TransmitterResponse();
        switch (request.getAction()) {
        case GetTransmitterGroups:
            response = getTransmitterGroups(request);
            break;
        case GetTransmitters:
            response = getTransmitters(request);
            break;
        case GetEnabledTransmitterGroups:
            response = getEnabledTransmitterGroups(request);
            break;
        case GetTransmitterGroupWithTransmitter:
            response = this.getTransmitterGroupWithTransmitter(request);
            break;
        case SaveTransmitter:
            response = saveTransmitter(request);
            notification = new TransmitterGroupConfigNotification(
                    ConfigChangeType.Update, request.getTransmitter()
                            .getTransmitterGroup());
            break;
        case SaveGroup:
            notification = new TransmitterGroupConfigNotification(
                    ConfigChangeType.Update, request.getTransmitterGroup());
            response = saveTransmitterGroup(request);
            break;
        case DeleteTransmitter:
            deleteTransmitter(request);
            notification = new TransmitterGroupConfigNotification(
                    ConfigChangeType.Update, request.getTransmitter()
                            .getTransmitterGroup());
            break;
        case DeleteTransmitterGroup:
            deleteTransmitterGroup(request);
            notification = new TransmitterGroupConfigNotification(
                    ConfigChangeType.Update, request.getTransmitterGroup());
            break;
        case SaveGroupList:
            response = saveTransmitterGroups(request);
            notification = new TransmitterGroupConfigNotification(
                    ConfigChangeType.Update, request.getTransmitterGroupList());
            break;
        case SaveTransmitterDeleteGroup:
            response = saveTransmitterDeleteGroup(request);
            notification = new TransmitterGroupConfigNotification(
                    ConfigChangeType.Update, request.getTransmitter()
                            .getTransmitterGroup());
            BmhMessageProducer.sendConfigMessage(notification,
                    request.isOperational());
            notification = new TransmitterGroupConfigNotification(
                    ConfigChangeType.Delete, request.getTransmitterGroup());
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

    private TransmitterResponse getTransmitterGroups(TransmitterRequest request) {
        TransmitterGroupDao dao = new TransmitterGroupDao(
                request.isOperational());
        List<TransmitterGroup> tGroups = dao.getTransmitterGroups();
        TransmitterResponse resp = new TransmitterResponse();
        resp.setTransmitterGroupList(tGroups);

        return resp;
    }

    private TransmitterResponse saveTransmitter(TransmitterRequest request) {
        IUFStatusHandler logger = BMHLoggerUtils.getSrvLogger(request);
        TransmitterResponse response = new TransmitterResponse();
        Transmitter newTrans = request.getTransmitter();
        TransmitterDao dao = new TransmitterDao(request.isOperational());
        Transmitter oldTrans = null;
        if (logger.isPriorityEnabled(Priority.INFO)) {
            oldTrans = dao.getByID(newTrans.getId());
        }

        dao.saveOrUpdate(newTrans);

        if (logger.isPriorityEnabled(Priority.INFO)) {
            String user = BMHLoggerUtils.getUser(request);
            BMHLoggerUtils.logSave(request, user, oldTrans, newTrans);
        }

        response.setTransmitter(newTrans);

        return response;
    }

    private TransmitterResponse saveTransmitterGroup(TransmitterRequest request) {
        IUFStatusHandler logger = BMHLoggerUtils.getSrvLogger(request);
        TransmitterResponse response = new TransmitterResponse();
        TransmitterDao dao = new TransmitterDao(request.isOperational());
        TransmitterGroup group = request.getTransmitterGroup();

        TransmitterGroup oldGroup = null;
        TransmitterGroupDao tgDao = null;
        if (logger.isPriorityEnabled(Priority.INFO)) {
            tgDao = new TransmitterGroupDao(request.isOperational());
            oldGroup = tgDao.getByID(group.getId());
        }

        dao.saveOrUpdate(group);
        List<TransmitterGroup> list = new ArrayList<TransmitterGroup>();
        list.add(group);
        response.setTransmitterGroupList(list);

        if (logger.isPriorityEnabled(Priority.INFO)) {
            String user = BMHLoggerUtils.getUser(request);
            if (group.isStandalone() && (oldGroup == null)) {
                // New stand alone transmitter
                Transmitter newTrans = tgDao.getByGroupName(group.getName())
                        .getTransmitterList().get(0);
                BMHLoggerUtils.logSave(request, user, null, newTrans);
            } else {
                BMHLoggerUtils.logSave(request, user, oldGroup, group);
            }
        }

        return response;
    }

    private void deleteTransmitter(TransmitterRequest request) {
        IUFStatusHandler logger = BMHLoggerUtils.getSrvLogger(request);
        Transmitter transmitter = request.getTransmitter();
        TransmitterDao dao = new TransmitterDao(request.isOperational());

        TransmitterGroupDao gtdao = null;
        int groupId = -1;
        TransmitterGroup oldGroup = null;
        if (logger.isPriorityEnabled(Priority.INFO)) {
            gtdao = new TransmitterGroupDao(request.isOperational());
            groupId = transmitter.getTransmitterGroup().getId();
            oldGroup = gtdao.getByID(groupId);
        }

        dao.delete(transmitter);

        if (logger.isPriorityEnabled(Priority.INFO)) {
            String user = BMHLoggerUtils.getUser(request);
            BMHLoggerUtils.logDelete(request, user, transmitter);
            TransmitterGroup newGroup = gtdao.getByID(groupId);
            BMHLoggerUtils.logSave(request, user, oldGroup, newGroup);
        }
    }

    private void deleteTransmitterGroup(TransmitterRequest request) {
        IUFStatusHandler logger = BMHLoggerUtils.getSrvLogger(request);
        TransmitterDao dao = new TransmitterDao(request.isOperational());
        TransmitterGroup group = request.getTransmitterGroup();
        dao.delete(group);
        if (logger.isPriorityEnabled(Priority.INFO)) {
            String user = BMHLoggerUtils.getUser(request);
            BMHLoggerUtils.logDelete(request, user, group);
        }
    }

    private TransmitterResponse getTransmitters(TransmitterRequest request) {
        TransmitterResponse response = new TransmitterResponse();
        TransmitterDao dao = new TransmitterDao(request.isOperational());
        List<Transmitter> transmitters = dao.getAllTransmitters();
        response.setTransmitterList(transmitters);

        return response;
    }

    private TransmitterResponse getEnabledTransmitterGroups(
            TransmitterRequest request) {
        TransmitterResponse response = new TransmitterResponse();
        TransmitterGroupDao dao = new TransmitterGroupDao(
                request.isOperational());
        List<TransmitterGroup> groups = dao.getEnabledTransmitterGroups();
        response.setTransmitterGroupList(groups);

        return response;
    }

    private TransmitterResponse getTransmitterGroupWithTransmitter(
            TransmitterRequest request) {
        TransmitterResponse response = new TransmitterResponse();
        TransmitterGroupDao dao = new TransmitterGroupDao(
                request.isOperational());
        TransmitterGroup transmitterGroup = dao
                .getTransmitterGroupWithTransmitter(request.getTransmitter()
                        .getId());
        if (transmitterGroup == null) {
            return response;
        }

        List<TransmitterGroup> transmitterGroups = new ArrayList<>(1);
        transmitterGroups.add(transmitterGroup);
        response.setTransmitterGroupList(transmitterGroups);

        return response;
    }

    private TransmitterResponse saveTransmitterGroups(TransmitterRequest request) {
        TransmitterResponse response = new TransmitterResponse();
        TransmitterGroupDao dao = new TransmitterGroupDao(
                request.isOperational());
        List<TransmitterGroup> groupList = request.getTransmitterGroupList();
        dao.persistAll(groupList);
        response.setTransmitterGroupList(groupList);

        return response;
    }

    private TransmitterResponse saveTransmitterDeleteGroup(
            TransmitterRequest request) {
        IUFStatusHandler logger = BMHLoggerUtils.getSrvLogger(request);
        TransmitterResponse response = new TransmitterResponse();
        TransmitterDao dao = new TransmitterDao(request.isOperational());
        Transmitter transmitter = request.getTransmitter();

        TransmitterGroupDao tgdao = null;
        TransmitterGroup delGroup = null;
        Transmitter oldTrans = null;
        TransmitterGroup oldGroup = null;
        if (logger.isPriorityEnabled(Priority.INFO)) {
            tgdao = new TransmitterGroupDao(request.isOperational());
            delGroup = request.getTransmitterGroup();
            oldTrans = dao.getByID(transmitter.getId());
            oldGroup = tgdao.getByID(transmitter.getTransmitterGroup().getId());
        }

        transmitter = dao.saveTransmitterDeleteGroup(request.getTransmitter(),
                request.getTransmitterGroup());
        response.setTransmitter(transmitter);

        if (logger.isPriorityEnabled(Priority.INFO)) {
            TransmitterGroup newGroup = tgdao.getByID(transmitter
                    .getTransmitterGroup().getId());
            String user = BMHLoggerUtils.getUser(request);
            BMHLoggerUtils.logDelete(request, user, delGroup);
            BMHLoggerUtils.logSave(request, user, oldGroup, newGroup);
            BMHLoggerUtils.logSave(request, user, oldTrans, transmitter);
        }

        return response;
    }
}
