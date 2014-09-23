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

import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.notify.config.ConfigNotification.ConfigChangeType;
import com.raytheon.uf.common.bmh.notify.config.TransmitterGroupConfigNotification;
import com.raytheon.uf.common.bmh.request.TransmitterRequest;
import com.raytheon.uf.common.bmh.request.TransmitterResponse;
import com.raytheon.uf.common.serialization.SerializationUtil;
import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.edex.bmh.dao.TransmitterDao;
import com.raytheon.uf.edex.bmh.dao.TransmitterGroupDao;
import com.raytheon.uf.edex.core.EDEXUtil;

/**
 * Thrift handler for {@link Transmitter} and {@link TransmitterGroup} objects
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 30, 2014   3173     mpduff      Initial creation
 * Aug 19, 2014   3486     bsteffen    Send change notification over jms.
 * Aug 24, 2014 3432       mpduff      Added getEnabledTransmitterGroups()
 * Sep 05, 2014 3554       bsteffen    Send more specific config change notification.
 * Sep 23, 2014 3649       rferrel     DeleteTransmitterGroup notfication no longer
 *                                      causes null pointer exception.
 * 
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class TransmitterHandler implements IRequestHandler<TransmitterRequest> {

    @Override
    public Object handleRequest(TransmitterRequest request) throws Exception {
        TransmitterGroupConfigNotification notification = null;
        TransmitterResponse response = new TransmitterResponse();
        switch (request.getAction()) {
        case GetTransmitterGroups:
            response = getTransmitterGroups();
            break;
        case GetTransmitters:
            response = getTransmitters();
            break;
        case GetEnabledTransmitterGroups:
            response = getEnabledTransmitterGroups();
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
            EDEXUtil.getMessageProducer().sendAsyncUri(
                    "jms-durable:topic:BMH.Config",
                    SerializationUtil.transformToThrift(notification));
            notification = new TransmitterGroupConfigNotification(
                    ConfigChangeType.Delete, request.getTransmitterGroup());
            break;
        default:
            break;
        }
        if (notification != null) {
            EDEXUtil.getMessageProducer().sendAsyncUri(
                    "jms-durable:topic:BMH.Config",
                    SerializationUtil.transformToThrift(notification));
        }
        return response;
    }

    private TransmitterResponse getTransmitterGroups() {
        TransmitterGroupDao dao = new TransmitterGroupDao();
        List<TransmitterGroup> tGroups = dao.getTransmitterGroups();
        TransmitterResponse resp = new TransmitterResponse();
        resp.setTransmitterGroupList(tGroups);

        return resp;
    }

    private TransmitterResponse saveTransmitter(TransmitterRequest request) {
        TransmitterResponse response = new TransmitterResponse();
        Transmitter t = request.getTransmitter();
        TransmitterDao dao = new TransmitterDao();
        dao.saveOrUpdate(t);
        response.setTransmitter(t);

        return response;
    }

    private TransmitterResponse saveTransmitterGroup(TransmitterRequest request) {
        TransmitterResponse response = new TransmitterResponse();
        TransmitterDao dao = new TransmitterDao();
        TransmitterGroup group = request.getTransmitterGroup();
        dao.saveOrUpdate(group);
        List<TransmitterGroup> list = new ArrayList<TransmitterGroup>();
        list.add(group);
        response.setTransmitterGroupList(list);

        return response;
    }

    private void deleteTransmitter(TransmitterRequest request) {
        TransmitterDao dao = new TransmitterDao();
        Transmitter transmitter = request.getTransmitter();
        dao.delete(transmitter);
    }

    private void deleteTransmitterGroup(TransmitterRequest request) {
        TransmitterDao dao = new TransmitterDao();
        TransmitterGroup group = request.getTransmitterGroup();
        dao.delete(group);
    }

    private TransmitterResponse getTransmitters() {
        TransmitterResponse response = new TransmitterResponse();
        TransmitterDao dao = new TransmitterDao();
        List<Transmitter> transmitters = dao.getAllTransmitters();
        response.setTransmitterList(transmitters);

        return response;
    }

    private TransmitterResponse getEnabledTransmitterGroups() {
        TransmitterResponse response = new TransmitterResponse();
        TransmitterGroupDao dao = new TransmitterGroupDao();
        List<TransmitterGroup> groups = dao.getEnabledTransmitterGroups();
        response.setTransmitterGroupList(groups);

        return response;
    }

    private TransmitterResponse saveTransmitterGroups(TransmitterRequest request) {
        TransmitterResponse response = new TransmitterResponse();
        TransmitterGroupDao dao = new TransmitterGroupDao();
        List<TransmitterGroup> groupList = request.getTransmitterGroupList();
        dao.persistAll(groupList);
        response.setTransmitterGroupList(groupList);

        return response;
    }

    private TransmitterResponse saveTransmitterDeleteGroup(
            TransmitterRequest request) {
        TransmitterResponse response = new TransmitterResponse();
        TransmitterDao dao = new TransmitterDao();
        Transmitter transmitter = dao.saveTransmitterDeleteGroup(
                request.getTransmitter(), request.getTransmitterGroup());
        response.setTransmitter(transmitter);

        return response;
    }
}
