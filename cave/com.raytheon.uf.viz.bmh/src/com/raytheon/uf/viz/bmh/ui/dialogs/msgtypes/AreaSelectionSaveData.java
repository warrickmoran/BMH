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
import java.util.Set;

import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Area;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
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
 * Oct 16, 2014    3657    bkowal      Added affectedTransmitters
 * Oct 17, 2014    3655    bkowal      Change affectedTransmitters from {@link String} 
 *                                     to {@link Transmitter}
 * Oct 21, 2014   #3728    lvenable    Added set of zonecodes and area codes.
 *  Jan 13, 2014   3876    lvenable    Updated method name and comments to be more correct.
 * Jan 15, 2015    4010    bkowal      Areas associated with transmitters are no longer
 *                                     included in the area/zone list.
 * Mar 16, 2015    4244    bsteffen    Add constructor which copies from message type.
 * Mar 30, 2016    5536    bkowal      Ignore areas/zones that are not present in the database.
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class AreaSelectionSaveData {

    /** All of the areas that were selected. */
    private Set<String> allSelectedZonesAreaCodes = new HashSet<String>();

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
     * The affected transmitters based on the Area, Zone, and Transmitter
     * selections. The dialog has already determined which transmitters should
     * be included in this list so there is no reason for an external component
     * to complete the same process.
     */
    private Set<Transmitter> affectedTransmitters;

    public AreaSelectionSaveData() {

    }

    public AreaSelectionSaveData(MessageType messageType) {
        Set<Transmitter> affectedTransmitters = new HashSet<Transmitter>();

        for (Area area : messageType.getDefaultAreas()) {
            addArea(area);
            affectedTransmitters.addAll(area.getTransmitters());
        }

        for (Zone zone : messageType.getDefaultZones()) {
            addZone(zone);
            if (zone.getAreas() != null) {
                for (Area area : zone.getAreas()) {
                    affectedTransmitters.addAll(area.getTransmitters());
                }
            }
        }

        for (TransmitterGroup group : messageType.getDefaultTransmitterGroups()) {
            for (Transmitter t : group.getTransmitters()) {
                addTransmitter(t);
                affectedTransmitters.add(t);
            }
        }
        this.affectedTransmitters = affectedTransmitters;
    }

    /**
     * @return the areaList
     */
    public Set<Area> getAreas() {
        if (areas == null) {
            return new HashSet<>();
        }
        return areas;
    }

    /**
     * Add Area.
     * 
     * @param a
     *            Area.
     */
    public void addArea(Area a) {
        if (a == null) {
            return;
        }
        if (areas == null) {
            areas = new HashSet<>();
        }

        areas.add(a);
        allSelectedZonesAreaCodes.add(a.getAreaCode());
    }

    /**
     * @return the zoneList
     */
    public Set<Zone> getZones() {
        if (zones == null) {
            return new HashSet<>();
        }
        return zones;
    }

    /**
     * Add zone.
     * 
     * @param z
     *            Zone.
     */
    public void addZone(Zone z) {
        if (z == null) {
            return;
        }
        if (zones == null) {
            zones = new HashSet<>();
        }

        zones.add(z);

        // Add the zone code.
        allSelectedZonesAreaCodes.add(z.getZoneCode());
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
     */
    public void addTransmitter(Transmitter t) {
        if (transmitters == null) {
            transmitters = new HashSet<>();
        }
        transmitters.add(t);
    }

    public Set<Transmitter> getAffectedTransmitters() {
        return affectedTransmitters;
    }

    public void setAffectedTransmitters(Set<Transmitter> affectedTransmitters) {
        this.affectedTransmitters = affectedTransmitters;
    }

    /**
     * Get a list of all the area and zone codes. This list is only based on
     * {@link #areas} and {@link #zones}. If the {@link Area}s included in the
     * {@link #transmitters} are ever required, this method should be updated so
     * that it could optionally include the additional information.
     * 
     * @return Set of area and zone codes.
     */
    public Set<String> getSelectedAreaZoneCodes() {
        return allSelectedZonesAreaCodes;
    }

}