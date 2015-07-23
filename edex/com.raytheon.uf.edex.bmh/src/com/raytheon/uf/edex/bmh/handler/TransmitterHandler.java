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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.util.CollectionUtils;

import com.raytheon.uf.common.bmh.BMHLoggerUtils;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterLanguage;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TxStatus;
import com.raytheon.uf.common.bmh.notify.config.ChangeTimeZoneConfigNotification;
import com.raytheon.uf.common.bmh.notify.config.ConfigNotification.ConfigChangeType;
import com.raytheon.uf.common.bmh.notify.config.TransmitterGroupConfigNotification;
import com.raytheon.uf.common.bmh.request.AbstractBMHServerRequest;
import com.raytheon.uf.common.bmh.request.TransmitterRequest;
import com.raytheon.uf.common.bmh.request.TransmitterResponse;
import com.raytheon.uf.common.serialization.SerializationException;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.edex.bmh.BmhMessageProducer;
import com.raytheon.uf.edex.bmh.dao.TransmitterDao;
import com.raytheon.uf.edex.bmh.dao.TransmitterGroupDao;
import com.raytheon.uf.edex.core.EdexException;

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
 * Jan 21, 2015  4017     bkowal      Only create the 
 *                                    {@link TransmitterGroupConfigNotification} when
 *                                    all required information is available.
 * Jan 22, 2015  3995     rjpeter     Added setting position of a new TransmitterGroup.
 * Feb 09, 2015  4082     bkowal      Added support for creating languages with a new
 *                                    Transmitter Group.
 * Mar 25, 2015   4305    rferrel     Added GetTransmittersByFips.
 * Apr 14, 2015   4390    rferrel     {@link #saveTransmitterGroups(TransmitterRequest)} checks for reorder.
 * Apr 14, 2015  4394     bkowal      Added {@link #getConfiguredTransmitterGroups(TransmitterRequest)}.
 * Apr 24, 2015  4423     rferrel     Issue {@link ChangeTimeZoneConfigNotification}.
 * May 06, 2015  4470     bkowal      Added {@link #disableTransmitterGroup(TransmitterRequest)} and
 *                                    {@link #saveTransmitters(Collection, AbstractBMHServerRequest)}.
 * May 08, 2015  4470     bkowal      Added {@link #enableTransmitterGroup(TransmitterRequest)}.
 * May 28, 2015  4429     rjpeter     Add ITraceable
 * Jul 17, 2015  4636     bkowal      Added {@link #getTransmitterGroupsWithIds(TransmitterRequest)}.
 * Jul 21, 2015  4424     bkowal      Added {@link #getTransmitterGroupByName(TransmitterRequest)} and
 *                                    {@link #getTransmitterByMnemonic(TransmitterRequest)}
 * Jul 22, 2015  4424     bkowal      Added missing break statement.
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
        case GetTransmitterGroupsWithIds:
            response = getTransmitterGroupsWithIds(request);
            break;
        case GetTransmitterGroupByName:
            response = getTransmitterGroupByName(request);
            break;
        case GetTransmitters:
            response = getTransmitters(request);
            break;
        case GetEnabledTransmitterGroups:
            response = getEnabledTransmitterGroups(request);
            break;
        case GetConfiguredTransmitterGroups:
            response = getConfiguredTransmitterGroups(request);
            break;
        case GetTransmitterGroupWithTransmitter:
            response = this.getTransmitterGroupWithTransmitter(request);
            break;
        case SaveTransmitter:
            response = saveTransmitter(request);
            notification = new TransmitterGroupConfigNotification(
                    ConfigChangeType.Update, request.getTransmitter()
                            .getTransmitterGroup(), request);
            break;
        case SaveGroup:
            response = saveTransmitterGroup(request);
            notification = new TransmitterGroupConfigNotification(
                    ConfigChangeType.Update, request.getTransmitterGroup(),
                    request);
            break;
        case DeleteTransmitter:
            notification = new TransmitterGroupConfigNotification(
                    ConfigChangeType.Delete, request.getTransmitter()
                            .getTransmitterGroup(), request);
            deleteTransmitter(request);
            break;
        case DeleteTransmitterGroup:
            notification = new TransmitterGroupConfigNotification(
                    ConfigChangeType.Delete, request.getTransmitterGroup(),
                    request);
            deleteTransmitterGroup(request);
            break;
        case DisableTransmitterGroup:
            this.disableTransmitterGroup(request);
            notification = new TransmitterGroupConfigNotification(
                    ConfigChangeType.Update, request.getTransmitterGroup(),
                    request);
            break;
        case EnableTransmitterGroup:
            this.enableTransmitterGroup(request);
            notification = new TransmitterGroupConfigNotification(
                    ConfigChangeType.Update, request.getTransmitterGroup(),
                    request);
            break;
        case SaveGroupList:
            response = saveTransmitterGroups(request);
            notification = new TransmitterGroupConfigNotification(
                    ConfigChangeType.Update, request.getTransmitterGroupList(),
                    request);
            break;
        case SaveTransmitterDeleteGroup:
            response = saveTransmitterDeleteGroup(request);
            notification = new TransmitterGroupConfigNotification(
                    ConfigChangeType.Update, request.getTransmitter()
                            .getTransmitterGroup(), request);
            BmhMessageProducer.sendConfigMessage(notification,
                    request.isOperational());
            notification = new TransmitterGroupConfigNotification(
                    ConfigChangeType.Delete, request.getTransmitterGroup(),
                    request);
            break;
        case GetTransmittersByFips:
            response = getTransmittersByFips(request);
            break;
        case GetTransmitterByMnemonic:
            response = getTransmitterByMnemonic(request);
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

    private TransmitterResponse getTransmitterGroupsWithIds(
            TransmitterRequest request) {
        TransmitterResponse response = new TransmitterResponse();
        TransmitterGroupDao dao = new TransmitterGroupDao(
                request.isOperational());
        response.setTransmitterGroupList(dao
                .getTransmitterGroupsWithIds(request.getIds()));

        return response;
    }

    private TransmitterResponse getTransmitterGroupByName(
            TransmitterRequest request) {
        TransmitterResponse resp = new TransmitterResponse();
        TransmitterGroupDao dao = new TransmitterGroupDao(
                request.isOperational());
        TransmitterGroup tg = dao.getByGroupName(request.getArgument());
        if (tg != null) {
            List<TransmitterGroup> transmitterGroupList = new ArrayList<>(1);
            transmitterGroupList.add(tg);
            resp.setTransmitterGroupList(transmitterGroupList);
        }

        return resp;
    }

    private TransmitterResponse saveTransmitter(TransmitterRequest request) {
        IUFStatusHandler logger = BMHLoggerUtils.getSrvLogger(request);
        TransmitterResponse response = new TransmitterResponse();
        Transmitter newTrans = request.getTransmitter();
        TransmitterDao dao = new TransmitterDao(request.isOperational());
        Transmitter oldTrans = null;
        if (logger.isPriorityEnabled(Priority.INFO) && (newTrans.getId() != 0)) {
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

    /**
     * Saves all of the specified {@link Transmitter}s in a single transaction.
     * 
     * @param transmitters
     *            the specified {@link Transmitter}s
     * @param request
     *            {@link AbstractBMHServerRequest} used to determine which dao
     *            and logger to use
     * @throws Exception
     */
    private void saveTransmitters(Collection<Transmitter> transmitters,
            AbstractBMHServerRequest request) throws Exception {
        if (CollectionUtils.isEmpty(transmitters)) {
            return;
        }

        Map<Transmitter, Transmitter> updatedToOldTransmitterMap = Collections
                .emptyMap();
        IUFStatusHandler logger = BMHLoggerUtils.getSrvLogger(request);
        final TransmitterDao dao = new TransmitterDao(request.isOperational());
        if (logger.isPriorityEnabled(Priority.INFO)) {
            updatedToOldTransmitterMap = new HashMap<>(transmitters.size(),
                    1.0f);
            for (Transmitter transmitter : transmitters) {
                if (transmitter.getId() == 0) {
                    updatedToOldTransmitterMap.put(transmitter, null);
                } else {
                    updatedToOldTransmitterMap.put(transmitter,
                            dao.getByID(transmitter.getId()));
                }
            }
        }

        dao.persistAll(transmitters);
        if (CollectionUtils.isEmpty(updatedToOldTransmitterMap)) {
            return;
        }

        final String user = BMHLoggerUtils.getUser(request);
        for (Transmitter transmitter : updatedToOldTransmitterMap.keySet()) {
            Transmitter old = updatedToOldTransmitterMap.get(transmitter);
            BMHLoggerUtils.logSave(request, user, old, transmitter);
        }
    }

    private TransmitterResponse saveTransmitterGroup(TransmitterRequest request)
            throws EdexException, SerializationException {
        IUFStatusHandler logger = BMHLoggerUtils.getSrvLogger(request);
        TransmitterResponse response = new TransmitterResponse();
        TransmitterGroup group = request.getTransmitterGroup();

        TransmitterGroup oldGroup = null;
        TransmitterGroupDao tgDao = new TransmitterGroupDao(
                request.isOperational());

        if (logger.isPriorityEnabled(Priority.INFO) && (group.getId() != 0)) {
            oldGroup = tgDao.getByID(group.getId());
        }

        if ((group.getId() == 0) && (group.getPosition() == 0)) {
            // position the new group at the end of the lists if position is not
            // set
            group.setPosition(tgDao.getNextPosition());
        }

        if (request.getLanguages() != null
                && request.getLanguages().isEmpty() == false) {
            tgDao.createGroupAndLanguages(group, request.getLanguages());

            /*
             * Determine if new need to log the new Transmitter Language(s) that
             * were just created.
             */
            if (logger.isPriorityEnabled(Priority.INFO)) {
                String user = BMHLoggerUtils.getUser(request);
                for (TransmitterLanguage tl : request.getLanguages()) {
                    BMHLoggerUtils.logSave(request, user, null, tl);
                }
            }
        } else {
            tgDao.saveOrUpdate(group);
            if ((oldGroup != null)
                    && !oldGroup.getTimeZone().equals(group.getTimeZone())
                    && (group.getDac() != null)
                    && !CollectionUtils.isEmpty(group.getTransmitterList())) {
                ChangeTimeZoneConfigNotification notification = new ChangeTimeZoneConfigNotification(
                        group.getTimeZone(), group.getName(), request);
                BmhMessageProducer.sendConfigMessage(notification,
                        request.isOperational());
            }
        }
        List<TransmitterGroup> list = new ArrayList<TransmitterGroup>(1);
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

    private void disableTransmitterGroup(TransmitterRequest request)
            throws Exception {
        TransmitterGroup group = request.getTransmitterGroup();
        Set<Transmitter> transmittersToDisable = group.getEnabledTransmitters();
        for (Transmitter transmitter : transmittersToDisable) {
            transmitter.setTxStatus(TxStatus.DISABLED);
        }

        this.saveTransmitters(transmittersToDisable, request);
    }

    private void enableTransmitterGroup(TransmitterRequest request)
            throws Exception {
        TransmitterGroup group = request.getTransmitterGroup();
        List<Transmitter> transmittersToEnable = group
                .getOrderedConfiguredTransmittersList();
        for (Transmitter transmitter : transmittersToEnable) {
            transmitter.setTxStatus(TxStatus.ENABLED);
        }

        this.saveTransmitters(transmittersToEnable, request);
    }

    private TransmitterResponse getTransmitters(TransmitterRequest request) {
        TransmitterResponse response = new TransmitterResponse();
        TransmitterDao dao = new TransmitterDao(request.isOperational());
        List<Transmitter> transmitters = dao.getAllTransmitters();
        response.setTransmitterList(transmitters);

        return response;
    }

    private TransmitterResponse getTransmittersByFips(TransmitterRequest request) {
        TransmitterResponse response = new TransmitterResponse();
        TransmitterDao dao = new TransmitterDao(request.isOperational());
        List<Transmitter> transmitters = dao.getTransmitterByFips(request
                .getArgument());
        response.setTransmitterList(transmitters);
        return response;
    }

    private TransmitterResponse getTransmitterByMnemonic(
            TransmitterRequest request) {
        TransmitterResponse response = new TransmitterResponse();
        TransmitterDao dao = new TransmitterDao(request.isOperational());
        response.setTransmitter(dao.getTransmitterByMnemonic(request
                .getArgument()));
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

    private TransmitterResponse getConfiguredTransmitterGroups(
            TransmitterRequest request) {
        TransmitterResponse response = new TransmitterResponse();
        TransmitterGroupDao dao = new TransmitterGroupDao(
                request.isOperational());
        List<TransmitterGroup> groups = dao.getConfiguredTransmitterGroups();
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
        if (request.isReorder()) {
            dao.reorderTransmitterGroup(groupList);
        } else {
            dao.persistAll(groupList);
        }
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
