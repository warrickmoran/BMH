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

import com.raytheon.uf.common.bmh.datamodel.transmitter.Area;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Zone;
import com.raytheon.uf.common.bmh.request.ZoneAreaRequest;
import com.raytheon.uf.common.bmh.request.ZoneAreaResponse;
import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.edex.bmh.dao.AreaDao;
import com.raytheon.uf.edex.bmh.dao.TransmitterDao;
import com.raytheon.uf.edex.bmh.dao.ZoneDao;

/**
 * Listening Area and Listening Zone request handler.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 15, 2014    3406    mpduff      Initial creation
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class ZoneAreaHandler implements IRequestHandler<ZoneAreaRequest> {
    private final static IUFStatusHandler statusHandler = UFStatus
            .getHandler(ZoneAreaHandler.class);

    @Override
    public Object handleRequest(ZoneAreaRequest request) throws Exception {
        ZoneAreaResponse response = null;
        switch (request.getAction()) {
        case GetAreas:
            response = getAreas(request);
            break;
        case GetZones:
            response = getZones(request);
            break;
        case GetTransmitters:
            response = getTransmitters(request);
            break;
        case SaveAreas:
            response = saveAreas(request);
            break;
        case SaveZones:
            response = saveZones(request);
            break;
        case DeleteArea:
            deleteArea(request);
            break;
        case DeleteZone:
            deleteZone(request);
            break;
        default:
            break;
        }
        return response;
    }

    /**
     * Get {@link Area} objects from BMH db.
     * 
     * @param req
     *            Request object
     * @return response object
     */
    private ZoneAreaResponse getAreas(ZoneAreaRequest req) {
        AreaDao dao = new AreaDao();
        List<Area> areas = dao.getAllAreas();
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
    private ZoneAreaResponse getZones(ZoneAreaRequest req) {
        ZoneDao dao = new ZoneDao();
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
    private ZoneAreaResponse getTransmitters(ZoneAreaRequest req) {
        TransmitterDao dao = new TransmitterDao();
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
    private ZoneAreaResponse saveZones(ZoneAreaRequest req) {
        ZoneDao dao = new ZoneDao();
        for (Zone z : req.getZoneList()) {
            dao.saveOrUpdate(z);
        }

        req.getZoneList();
        ZoneAreaResponse response = new ZoneAreaResponse();
        response.setZoneList(req.getZoneList());

        return response;
    }

    /**
     * Save the {@link Area} objects to the BMH db.
     * 
     * @param req
     *            Request object
     * @return response object
     */
    private ZoneAreaResponse saveAreas(ZoneAreaRequest req) {
        AreaDao dao = new AreaDao();
        for (Area a : req.getAreaList()) {
            dao.saveOrUpdate(a);
        }

        ZoneAreaResponse response = new ZoneAreaResponse();
        response.setAreaList(req.getAreaList());

        return response;
    }

    /**
     * Delete {@link Area} objects from the BMH db.
     * 
     * @param req
     *            Request object
     * @return response object
     */
    private void deleteArea(ZoneAreaRequest req) {
        AreaDao dao = new AreaDao();
        for (Area a : req.getAreaList()) {
            dao.delete(a);
        }
    }

    /**
     * Delete {@link Zone} objects from the BMH db.
     * 
     * @param req
     *            Request object
     * @return response object
     */
    private void deleteZone(ZoneAreaRequest req) {
        ZoneDao dao = new ZoneDao();
        List<Zone> zoneList = req.getZoneList();
        for (Zone z : zoneList) {
            dao.delete(z);
        }
    }
}
