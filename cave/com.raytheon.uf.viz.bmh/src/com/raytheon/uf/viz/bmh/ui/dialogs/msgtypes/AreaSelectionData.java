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
package com.raytheon.uf.viz.bmh.ui.dialogs.msgtypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.raytheon.uf.common.bmh.datamodel.transmitter.Area;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Zone;
import com.raytheon.uf.viz.bmh.ui.dialogs.listening.ZonesAreasDataManager;

/**
 * Class holding the data required by the Area Selection dialog.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 13, 2014    3411    mpduff      Initial creation
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class AreaSelectionData {
    private List<Area> areaList;

    private List<Zone> zoneList;

    private List<Transmitter> transmitterList;

    private final Map<Transmitter, List<Area>> transmitterToAreaMap = new HashMap<>();

    private final Map<String, Area> areaNameMap = new HashMap<>();

    private final ZonesAreasDataManager dataManager = new ZonesAreasDataManager();

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
     * @return the transmitterList
     */
    public List<Transmitter> getTransmitterList() {
        return transmitterList;
    }

    /**
     * @param transmitterList
     *            the transmitterList to set
     */
    public void setTransmitterList(List<Transmitter> transmitterList) {
        this.transmitterList = transmitterList;
    }

    /**
     * @return the transmitterToAreaMap
     */
    public Map<Transmitter, List<Area>> getTransmitterToAreaMap() {
        return transmitterToAreaMap;
    }

    /**
     * Populate the internal data structures
     * 
     * @throws Exception
     */
    public void populate() throws Exception {
        this.areaList = dataManager.getAreas();
        this.zoneList = dataManager.getZones();
        this.transmitterList = dataManager.getTransmitters();

        // Populate map
        for (Transmitter t : transmitterList) {
            List<Area> al = new ArrayList<>();
            this.transmitterToAreaMap.put(t, al);
        }

        for (Area a : areaList) {
            for (Transmitter t : a.getTransmitters()) {
                this.transmitterToAreaMap.get(t).add(a);
            }

            areaNameMap.put(a.getAreaName(), a);
        }
    }

    /**
     * Get the {@link Area}s for the given {@link Transmitter}
     * 
     * @param t
     *            The Transmitter
     * @return List of Area Objects
     */
    public List<Area> getAreasForTransmitter(Transmitter t) {
        return transmitterToAreaMap.get(t);
    }

    /**
     * Get the Area object for the given area name.
     * 
     * @param areaName
     *            The area name
     * @return The Area Object
     */
    public Area getAreaByName(String areaName) {
        return areaNameMap.get(areaName);
    }
}
