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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.raytheon.uf.common.bmh.datamodel.PositionComparator;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Area;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterLocationComparator;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Zone;
import com.raytheon.uf.viz.bmh.ui.dialogs.config.transmitter.TransmitterDataManager;
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
 * Aug 18, 2014    3432    mpduff      More implementation.
 * Oct 14, 2014   #3728    lvenable    Added a map of area codes and areas.
 * Oct 21, 2014   #3728    lvenable    Added zone map.
 * Jan 04, 2016   #4997    bkowal      Added {@link #transmitterGroupList} and
 *                                     {@link #getAreasForTransmitterGroup(TransmitterGroup)}.
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class AreaSelectionData {

    private List<Zone> zoneList;

    private List<Transmitter> transmitterList;

    private List<TransmitterGroup> transmitterGroupList;

    private final Map<Transmitter, List<Area>> transmitterToAreaMap = new HashMap<>();

    private final Map<String, Area> areaNameMap = new HashMap<>();

    /** Map of areas codes and related areas. */
    private final Map<String, Area> areaCodeMap = new HashMap<>();

    /** Map of areas codes and related areas. */
    private final Map<String, Zone> zoneCodeMap = new HashMap<>();

    private final ZonesAreasDataManager dataManager = new ZonesAreasDataManager();

    private final TransmitterDataManager transmitterDataManager = new TransmitterDataManager();

    /**
     * @return the areaList
     */
    public List<Area> getAreaList() {
        return new ArrayList<Area>(areaNameMap.values());
    }

    /**
     * @return the zoneList
     */
    public List<Zone> getZoneList() {
        return zoneList;
    }

    /**
     * 
     * @return Map of zone codes and zones.
     */
    public Map<String, Zone> getZonesMap() {
        return zoneCodeMap;
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

    public List<TransmitterGroup> getTransmitterGroupList() {
        return transmitterGroupList;
    }

    public void setTransmitterGroupList(
            List<TransmitterGroup> transmitterGroupList) {
        this.transmitterGroupList = transmitterGroupList;
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
        List<Area> allAreas = dataManager.getAreas();
        this.zoneList = dataManager.getZones();
        this.transmitterGroupList = transmitterDataManager
                .getTransmitterGroups(new PositionComparator());
        if (this.transmitterGroupList == null
                || this.transmitterGroupList.isEmpty()) {
            this.transmitterList = Collections.emptyList();
        } else {
            Set<Transmitter> allTransmitters = new HashSet<>();
            for (TransmitterGroup tg : this.transmitterGroupList) {
                allTransmitters.addAll(tg.getTransmitters());
            }
            this.transmitterList = new ArrayList<>(allTransmitters);
            Collections.sort(transmitterList,
                    new TransmitterLocationComparator());
        }

        // Populate map
        for (Transmitter t : transmitterList) {
            List<Area> al = new ArrayList<>();
            this.transmitterToAreaMap.put(t, al);
        }

        for (Area a : allAreas) {
            for (Transmitter t : a.getTransmitters()) {
                this.transmitterToAreaMap.get(t).add(a);
            }

            areaNameMap.put(a.getAreaName(), a);
            areaCodeMap.put(a.getAreaCode(), a);
        }

        for (Zone z : zoneList) {
            zoneCodeMap.put(z.getZoneCode(), z);
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
     * Retrieves the union of all {@link Area}s associated with all
     * {@link Transmitter}s that belong to the specified
     * {@link TransmitterGroup}.
     * 
     * @param tg
     *            the specified {@link TransmitterGroup}.
     * @return a {@link List} of all associated {@link Area}s.
     */
    public List<Area> getAreasForTransmitterGroup(TransmitterGroup tg) {
        if (tg.getTransmitters().isEmpty()) {
            return Collections.emptyList();
        }
        Set<Area> areasForGroupSet = new HashSet<>();
        for (Transmitter t : tg.getTransmitters()) {
            areasForGroupSet.addAll(this.getAreasForTransmitter(t));
        }
        return new ArrayList<>(areasForGroupSet);
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

    /**
     * Get the map of area codes and areas.
     * 
     * @return Map of all the area codes and areas.
     */
    public Map<String, Area> getAllAreaCodes() {
        return areaCodeMap;
    }
}
