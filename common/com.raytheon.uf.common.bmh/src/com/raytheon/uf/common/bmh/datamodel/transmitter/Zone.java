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
package com.raytheon.uf.common.bmh.datamodel.transmitter;

import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.MapKey;
import javax.persistence.Table;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * UGC Zone code record
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 30, 2014 3175       rjpeter     Initial creation
 * 
 * </pre>
 * 
 * @author rjpeter
 * @version 1.0
 */
@Entity
@Table(name = "zone", schema = "bmh")
@DynamicSerialize
public class Zone {
    /**
     * SSZNNN - 6 digit UGC Zone code
     * 
     * <pre>
     * SS - State
     * Z - Always Z for zone
     * NNN - zone code number
     * </pre>
     */
    @Id
    @Column(length = 6)
    @DynamicSerializeElement
    private String zoneCode;

    @Column(length = 20)
    @DynamicSerializeElement
    private String zoneName;

    @ManyToMany(cascade = CascadeType.ALL)
    @MapKey(name = "areaCode")
    @JoinTable(name = "zone_area", schema = "bmh", joinColumns = { @JoinColumn(name = "zoneCode") }, inverseJoinColumns = { @JoinColumn(name = "areaCode") })
    @DynamicSerializeElement
    private final Map<String, Area> areas = null;

    public String getZoneCode() {
        return zoneCode;
    }

    public void setZoneCode(String zoneCode) {
        this.zoneCode = zoneCode;
    }

    public String getZoneName() {
        return zoneName;
    }

    public void setZoneName(String zoneName) {
        this.zoneName = zoneName;
    }

    public Map<String, Area> getAreas() {
        return areas;
    }

}
