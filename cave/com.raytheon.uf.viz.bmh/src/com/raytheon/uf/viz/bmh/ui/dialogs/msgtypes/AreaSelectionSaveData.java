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
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class AreaSelectionSaveData {
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
     * @param areaList
     *            the areaList to set
     */
    public void setAreas(Set<Area> areas) {
        this.areas = areas;
    }

    public void addArea(Area a) {
        if (areas == null) {
            areas = new HashSet<>();
        }
        areas.add(a);
    }

    /**
     * @return the zoneList
     */
    public Set<Zone> getZones() {
        return zones;
    }

    /**
     * @param zoneList
     *            the zoneList to set
     */
    public void setZones(Set<Zone> zones) {
        this.zones = zones;
    }

    public void addZone(Zone z) {
        if (zones == null) {
            zones = new HashSet<>();
        }

        zones.add(z);
    }

    /**
     * @return the transmitterList
     */
    public Set<Transmitter> getTransmitters() {
        return transmitters;
    }

    /**
     * @param transmitters
     *            the transmitterList to set
     */
    public void setTransmitters(Set<Transmitter> transmitters) {
        this.transmitters = transmitters;
    }

    public void addTransmitter(Transmitter t) {
        if (transmitters == null) {
            transmitters = new HashSet<>();
        }
        transmitters.add(t);
    }
}
