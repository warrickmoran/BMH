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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.raytheon.uf.common.bmh.datamodel.transmitter.Area;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Zone;

/**
 * Selected data from the Area Selection Dialog.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 20, 2014    3411    mpduff      Initial creation
 * Oct 05, 2014    3411    mpduff      getTransmitters now returns an empty set rather than null
 * Oct 14, 2014   #3728    lvenable    Added a set of area codes.
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class AreaSelectionSaveData {

    /** All of the areas that were selected. */
    private Set<String> allSelectedAreaCodes = new HashSet<String>();

    /**
     * Selected Areas
     */
    private Set<Area> areas;

    /**
     * Selected Zones
     */
    private Set<Zone> zones;

    /**
     * Selected Transmitters
     */
    private Set<Transmitter> transmitters;

    /**
     * @return the areaList
     */
    public Set<Area> getAreas() {
        return areas;
    }

    /**
     * Add Area.
     * 
     * @param a
     *            Area.
     */
    public void addArea(Area a) {
        if (areas == null) {
            areas = new HashSet<>();
        }

        areas.add(a);
        allSelectedAreaCodes.add(a.getAreaCode());
    }

    /**
     * @return the zoneList
     */
    public Set<Zone> getZones() {
        return zones;
    }

    /**
     * Add zone.
     * 
     * @param z
     *            Zone.
     */
    public void addZone(Zone z) {
        if (zones == null) {
            zones = new HashSet<>();
        }

        zones.add(z);

        // Add the area codes from the selected zone.
        if (z.getAreas() != null) {
            for (Area a : z.getAreas()) {
                allSelectedAreaCodes.add(a.getAreaCode());
            }
        }
    }

    /**
     * @return the transmitterList
     */
    public Set<Transmitter> getTransmitters() {
        if (transmitters == null) {
            transmitters = new HashSet<>();
        }
        return transmitters;
    }

    /**
     * Add the transmitter to the transmitter set and save off the areas
     * associated with this transmitter.
     * 
     * @param t
     *            Transmitter.
     * @param transmitterAreaList
     *            Areas associated with the transmitter.
     */
    public void addTransmitter(Transmitter t, List<Area> transmitterAreaList) {
        if (transmitters == null) {
            transmitters = new HashSet<>();
        }
        transmitters.add(t);

        if (transmitterAreaList != null && !transmitterAreaList.isEmpty()) {
            addTransmitterAreas(transmitterAreaList);
        }
    }

    /**
     * Add the area codes from the selected transmitter to the set of area
     * codes.
     * 
     * @param transmitterAreaList
     */
    private void addTransmitterAreas(List<Area> transmitterAreaList) {
        for (Area a : transmitterAreaList) {
            allSelectedAreaCodes.add(a.getAreaCode());
        }
    }

    /**
     * Get a list of all the areas.
     * 
     * @return List of areas.
     */
    public Set<String> getSelectedAreaCodes() {
        return allSelectedAreaCodes;
    }

}
