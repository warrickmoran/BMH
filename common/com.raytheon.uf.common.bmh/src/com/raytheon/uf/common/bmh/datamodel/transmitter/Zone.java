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

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

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
 * Jul 17, 2014  3406      mpduff      Added id pk column, named query, removed cascade
 * 
 * </pre>
 * 
 * @author rjpeter
 * @version 1.0
 */
@Entity
@Table(name = "zone", schema = "bmh", uniqueConstraints = { @UniqueConstraint(columnNames = { "zoneCode" }) })
@SequenceGenerator(initialValue = 1, name = Zone.GEN, sequenceName = "zone_seq")
@DynamicSerialize
public class Zone {
    static final String GEN = "Zone Generator";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = GEN)
    @DynamicSerializeElement
    protected int id;

    /**
     * SSZNNN - 6 digit UGC Zone code
     * 
     * <pre>
     * SS - State
     * Z - Always Z for zone
     * NNN - zone code number
     * </pre>
     */
    @Column(length = 6)
    @DynamicSerializeElement
    private String zoneCode;

    @Column(length = 20)
    @DynamicSerializeElement
    private String zoneName;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "zone_area", schema = "bmh", joinColumns = { @JoinColumn(name = "zoneId") }, inverseJoinColumns = { @JoinColumn(name = "areaId") })
    @DynamicSerializeElement
    private Set<Area> areas;

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

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

    /**
     * @return the areas
     */
    public Set<Area> getAreas() {
        return areas;
    }

    /**
     * @param areas
     *            the areas to set
     */
    public void setAreas(Set<Area> areas) {
        this.areas = areas;
    }
}
