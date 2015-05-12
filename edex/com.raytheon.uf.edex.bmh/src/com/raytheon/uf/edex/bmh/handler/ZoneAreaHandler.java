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

import com.raytheon.uf.common.bmh.BMHLoggerUtils;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Area;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Zone;
import com.raytheon.uf.common.bmh.notify.config.AbstractTraceableSystemConfigNotification;
import com.raytheon.uf.common.bmh.notify.config.ConfigNotification.ConfigChangeType;
import com.raytheon.uf.common.bmh.notify.config.ZoneAreaConfigNotification;
import com.raytheon.uf.common.bmh.request.ZoneAreaRequest;
import com.raytheon.uf.common.bmh.request.ZoneAreaResponse;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.edex.bmh.BmhMessageProducer;
import com.raytheon.uf.edex.bmh.dao.AreaDao;
import com.raytheon.uf.edex.bmh.dao.TransmitterDao;
import com.raytheon.uf.edex.bmh.dao.ZoneDao;

/**
 * Handles any requests to get or modify the state of {@link Zone}s or
 * {@link Area}s.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Jul 15, 2014  3406     mpduff      Initial creation
 * Oct 07, 2014  3687     bsteffen    Handle non-operational requests.
 * Oct 13, 2014  3413     rferrel     Implement User roles.
 * Oct 24, 2014  3636     rferrel     Implement logging.
 * Nov 21, 2014  3845     bkowal      Added getAreasForTransmitter
 * Apr 22, 2015  4397     bkowal      Construct a {@link AbstractTraceableSystemConfigNotification}
 *                                    notification when database changes occur.
 * May 12, 2015  4248     rjpeter     Normalized area id field.
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class ZoneAreaHandler extends
        AbstractBMHServerRequestHandler<ZoneAreaRequest> {

    @Override
    public Object handleRequest(ZoneAreaRequest request) throws Exception {
        ZoneAreaResponse response = null;
        ZoneAreaConfigNotification notification = null;
        switch (request.getAction()) {
        case GetAreas:
            response = getAreas(request);
            break;
        case GetAreasForTransmitter:
            response = this.getAreasForTransmitter(request);
            break;
        case GetZones:
            response = getZones(request);
            break;
        case GetTransmitters:
            response = getTransmitters(request);
            break;
        case SaveAreas:
            response = saveAreas(request);
            notification = new ZoneAreaConfigNotification(
                    ConfigChangeType.Update, request);
            break;
        case SaveZones:
            response = saveZones(request);
            notification = new ZoneAreaConfigNotification(
                    ConfigChangeType.Update, request);
            break;
        case DeleteArea:
            deleteArea(request);
            notification = new ZoneAreaConfigNotification(
                    ConfigChangeType.Delete, request);
            break;
        case DeleteZone:
            deleteZone(request);
            notification = new ZoneAreaConfigNotification(
                    ConfigChangeType.Delete, request);
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
     * Get {@link Area} objects from BMH db.
     * 
     * @param req
     *            Request object
     * @return response object
     */
    private ZoneAreaResponse getAreas(ZoneAreaRequest request) {
        AreaDao dao = new AreaDao(request.isOperational());
        List<Area> areas = dao.getAllAreas();
        ZoneAreaResponse response = new ZoneAreaResponse();
        response.setAreaList(areas);

        return response;
    }

    private ZoneAreaResponse getAreasForTransmitter(ZoneAreaRequest request) {
        AreaDao dao = new AreaDao(request.isOperational());
        List<Area> areas = dao.getAreasForTransmitter(request.getTransmitter()
                .getId());
        ZoneAreaResponse response = new ZoneAreaResponse();
        response.setAreaList(areas);

        return response;
    }

    /**
     * Get {@link Zone} objects from BMH db.
     * 
     * @param req
     *            Request object
     * @return response object
     */
    private ZoneAreaResponse getZones(ZoneAreaRequest request) {
        ZoneDao dao = new ZoneDao(request.isOperational());
        List<Zone> zones = dao.getAllZones();
        ZoneAreaResponse response = new ZoneAreaResponse();
        response.setZoneList(zones);

        return response;
    }

    /**
     * Get {@link Transmitter} objects from BMH db.
     * 
     * @param req
     *            Request object
     * @return response object
     */
    private ZoneAreaResponse getTransmitters(ZoneAreaRequest request) {
        TransmitterDao dao = new TransmitterDao(request.isOperational());
        List<Transmitter> transList = dao.getAllTransmitters();
        ZoneAreaResponse response = new ZoneAreaResponse();
        response.setTransmitterList(transList);

        return response;
    }

    /**
     * Save the {@link Zone} objects to the BMH db.
     * 
     * @param req
     *            Request object
     * @return response object
     */
    private ZoneAreaResponse saveZones(ZoneAreaRequest request) {
        ZoneDao dao = new ZoneDao(request.isOperational());
        IUFStatusHandler logger = BMHLoggerUtils.getSrvLogger(request);
        if (logger.isPriorityEnabled(Priority.INFO)) {
            String user = BMHLoggerUtils.getUser(request);
            for (Zone z : request.getZoneList()) {
                Zone oldZ = dao.getByID(z.getId());
                dao.saveOrUpdate(z);
                BMHLoggerUtils.logSave(request, user, oldZ, z);
            }
        } else {
            for (Zone z : request.getZoneList()) {
                dao.saveOrUpdate(z);
            }
        }

        request.getZoneList();
        ZoneAreaResponse response = new ZoneAreaResponse();
        response.setZoneList(request.getZoneList());

        return response;
    }

    /**
     * Save the {@link Area} objects to the BMH db.
     * 
     * @param req
     *            Request object
     * @return response object
     */
    private ZoneAreaResponse saveAreas(ZoneAreaRequest request) {
        AreaDao dao = new AreaDao(request.isOperational());
        IUFStatusHandler logger = BMHLoggerUtils.getSrvLogger(request);
        if (logger.isPriorityEnabled(Priority.INFO)) {
            String user = BMHLoggerUtils.getUser(request);
            for (Area a : request.getAreaList()) {
                Area oldArea = dao.getByID(a.getId());
                dao.saveOrUpdate(a);
                BMHLoggerUtils.logSave(request, user, oldArea, a);
            }
        } else {
            for (Area a : request.getAreaList()) {
                dao.saveOrUpdate(a);
            }
        }

        ZoneAreaResponse response = new ZoneAreaResponse();
        response.setAreaList(request.getAreaList());

        return response;
    }

    /**
     * Delete {@link Area} objects from the BMH db.
     * 
     * @param req
     *            Request object
     * @return response object
     */
    private void deleteArea(ZoneAreaRequest request) {
        AreaDao dao = new AreaDao(request.isOperational());
        IUFStatusHandler logger = BMHLoggerUtils.getSrvLogger(request);
        if (logger.isPriorityEnabled(Priority.INFO)) {
            String user = BMHLoggerUtils.getUser(request);
            for (Area a : request.getAreaList()) {
                dao.delete(a);
                BMHLoggerUtils.logDelete(request, user, a);
            }
        } else {
            for (Area a : request.getAreaList()) {
                dao.delete(a);
            }
        }
    }

    /**
     * Delete {@link Zone} objects from the BMH db.
     * 
     * @param req
     *            Request object
     * @return response object
     */
    private void deleteZone(ZoneAreaRequest request) {
        ZoneDao dao = new ZoneDao(request.isOperational());
        IUFStatusHandler logger = BMHLoggerUtils.getSrvLogger(request);
        List<Zone> zoneList = request.getZoneList();

        if (logger.isPriorityEnabled(Priority.INFO)) {
            String user = BMHLoggerUtils.getUser(request);
            for (Zone z : zoneList) {
                dao.delete(z);
                BMHLoggerUtils.logDelete(request, user, z);
            }
        } else {
            for (Zone z : zoneList) {
                dao.delete(z);
            }
        }
    }
}
