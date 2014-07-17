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
package com.raytheon.uf.common.bmh.request;

import java.util.ArrayList;
import java.util.List;

import com.raytheon.uf.common.bmh.datamodel.transmitter.Area;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Zone;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

/**
 * Request object for Listening Zone and Listening Area requests.
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
@DynamicSerialize
public class ZoneAreaRequest implements IServerRequest {

    public enum ZoneAreaAction {
        GetZones, GetAreas, GetTransmitters, SaveZones, SaveAreas, DeleteArea, DeleteZone;
    }

    /**
     * Action to perform
     */
    @DynamicSerializeElement
    private ZoneAreaAction action;

    /**
     * List of Zones
     */
    @DynamicSerializeElement
    private List<Zone> zoneList;

    /**
     * List of Areas
     */
    @DynamicSerializeElement
    private List<Area> areaList;

    /**
     * @return the action
     */
    public ZoneAreaAction getAction() {
        return action;
    }

    /**
     * @param action
     *            the action to set
     */
    public void setAction(ZoneAreaAction action) {
        this.action = action;
    }

    /**
     * @return the zoneList
     */
    public List<Zone> getZoneList() {
        return zoneList;
    }

    /**
     * @param zoneList
     *            the zoneList to set
     */
    public void setZoneList(List<Zone> zoneList) {
        this.zoneList = zoneList;
    }

    /**
     * @return the areaList
     */
    public List<Area> getAreaList() {
        return areaList;
    }

    /**
     * @param areaList
     *            the areaList to set
     */
    public void setAreaList(List<Area> areaList) {
        this.areaList = areaList;
    }

    /**
     * Add an area to the list
     * 
     * @param area
     *            The area to add
     */
    public void addArea(Area area) {
        if (this.areaList == null) {
            areaList = new ArrayList<Area>(1);
        }

        areaList.add(area);
    }

    public void addZone(Zone zone) {
        if (this.zoneList == null) {
            zoneList = new ArrayList<Zone>(1);
        }

        zoneList.add(zone);
    }
}
