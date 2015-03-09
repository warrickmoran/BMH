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
package com.raytheon.uf.viz.bmh.ui.dialogs.listening;

import java.util.List;

import com.raytheon.uf.common.bmh.datamodel.transmitter.Area;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Zone;
import com.raytheon.uf.common.bmh.request.ZoneAreaRequest;
import com.raytheon.uf.common.bmh.request.ZoneAreaRequest.ZoneAreaAction;
import com.raytheon.uf.common.bmh.request.ZoneAreaResponse;
import com.raytheon.uf.viz.bmh.data.BmhUtils;
import com.raytheon.uf.viz.core.exception.VizException;

/**
 * Data access class for the Listening Area and Listening Zone dialogs
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 15, 2014    3406    mpduff      Initial creation
 * Aug 05, 2014 3414       rjpeter     Added BMH Thrift interface.
 * Nov 21, 2014  3845      bkowal      Added getAreasForTransmitters.
 * Mar 09, 2015  4247      rferrel     No longer use to validate state abbreviations.
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class ZonesAreasDataManager {

    /**
     * Get the {@link Zone}s defined in the bmh db.
     * 
     * @return List of defined zones
     * @throws VizException
     */
    public List<Zone> getZones() throws Exception {
        ZoneAreaRequest req = new ZoneAreaRequest();
        req.setAction(ZoneAreaAction.GetZones);

        ZoneAreaResponse response = (ZoneAreaResponse) BmhUtils
                .sendRequest(req);
        return response.getZoneList();
    }

    /**
     * Get the {@link Area}s defined in the bmh db.
     * 
     * @return List of defined areas
     * @throws VizException
     */
    public List<Area> getAreas() throws Exception {
        ZoneAreaRequest req = new ZoneAreaRequest();
        req.setAction(ZoneAreaAction.GetAreas);

        ZoneAreaResponse response = (ZoneAreaResponse) BmhUtils
                .sendRequest(req);
        return response.getAreaList();
    }

    /**
     * Get the {@link Area}s that have been assigned to the specified
     * {@link Transmitter}.
     * 
     * @param transmitter
     *            the specified {@link Transmitter}
     * @return {@link Area}s that have been assigned to the {@link Transmitter}
     * @throws Exception
     */
    public List<Area> getAreasForTransmitter(final Transmitter transmitter)
            throws Exception {
        ZoneAreaRequest request = new ZoneAreaRequest();
        request.setAction(ZoneAreaAction.GetAreasForTransmitter);
        request.setTransmitter(transmitter);

        return ((ZoneAreaResponse) BmhUtils.sendRequest(request)).getAreaList();
    }

    /**
     * Get the {@link Transmitter}s defined in the bmh db.
     * 
     * @return List of defined transmitters
     * @throws VizException
     */
    public List<Transmitter> getTransmitters() throws Exception {
        ZoneAreaRequest req = new ZoneAreaRequest();
        req.setAction(ZoneAreaAction.GetTransmitters);

        ZoneAreaResponse response = (ZoneAreaResponse) BmhUtils
                .sendRequest(req);
        return response.getTransmitterList();
    }

    /**
     * Persist the {@link Zone} to the bmh db.
     * 
     * @throws VizException
     */
    public ZoneAreaResponse saveZone(Zone z) throws Exception {
        ZoneAreaRequest req = new ZoneAreaRequest();
        req.setAction(ZoneAreaAction.SaveZones);
        req.addZone(z);
        ZoneAreaResponse response = (ZoneAreaResponse) BmhUtils
                .sendRequest(req);

        return response;
    }

    /**
     * Persist the {@link Area} to the bmh db.
     * 
     * @throws VizException
     */
    public ZoneAreaResponse saveArea(Area a) throws Exception {
        ZoneAreaRequest req = new ZoneAreaRequest();
        req.setAction(ZoneAreaAction.SaveAreas);
        req.addArea(a);
        ZoneAreaResponse response = (ZoneAreaResponse) BmhUtils
                .sendRequest(req);

        return response;
    }

    /**
     * Delete the {@link Area} from the bmh db.
     * 
     * @throws VizException
     */
    public void deleteArea(Area a) throws Exception {
        ZoneAreaRequest req = new ZoneAreaRequest();
        req.setAction(ZoneAreaAction.DeleteArea);
        req.addArea(a);
        BmhUtils.sendRequest(req);
    }

    /**
     * Delete the {@link Zoine} from the bmh db.
     * 
     * @throws VizException
     */
    public void deleteZone(Zone z) throws Exception {
        ZoneAreaRequest req = new ZoneAreaRequest();
        req.setAction(ZoneAreaAction.DeleteZone);
        req.addZone(z);
        BmhUtils.sendRequest(req);
    }
}
