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

import java.util.List;

import com.raytheon.uf.common.bmh.datamodel.transmitter.Area;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Zone;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Response object for Listening Zone and Listening Area requests.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 16, 2014   3406     mpduff      Initial creation
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */
@DynamicSerialize
public class ZoneAreaResponse {

    @DynamicSerializeElement
    private List<Zone> zoneList;

    @DynamicSerializeElement
    private List<Area> areaList;

    @DynamicSerializeElement
    private List<Transmitter> transmitterList;

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
}
