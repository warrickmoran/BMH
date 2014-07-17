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
 * Represents a UGC Area code.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * May 30, 2014  3175     rjpeter     Initial creation
 * Jul 10, 2014  3283     bsteffen    Change transmitters from map to set.
 * Jul 17, 2014  3406     mpduff      Added id pk column, named query, removed cascade
 * 
 * </pre>
 * 
 * @author rjpeter
 * @version 1.0
 */
@Entity
@Table(name = "area", schema = "bmh", uniqueConstraints = { @UniqueConstraint(columnNames = { "areaCode" }) })
@SequenceGenerator(initialValue = 1, name = Area.GEN, sequenceName = "area_seq")
@DynamicSerialize
public class Area {
    static final String GEN = "Area Generator";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = GEN)
    @DynamicSerializeElement
    protected int areaId;

    /**
     * SSXNNN - 6 digit UGC area code
     * 
     * <pre>
     * SS - State
     * X - C for county code, and a numeral (i.e., 1 through 9) for a partial area code
     * NNN - county code number
     * </pre>
     */
    @Column(length = 6)
    @DynamicSerializeElement
    private String areaCode;

    @Column(length = 20, nullable = false)
    @DynamicSerializeElement
    private String areaName;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "area_tx", schema = "bmh", joinColumns = @JoinColumn(name = "areaId"), inverseJoinColumns = @JoinColumn(name = "transmitterId"))
    @DynamicSerializeElement
    private Set<Transmitter> transmitters;

    /**
     * @return the areaId
     */
    public int getAreaId() {
        return areaId;
    }

    /**
     * @param areaId
     *            the areaId to set
     */
    public void setAreaId(int areaId) {
        this.areaId = areaId;
    }

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public Set<Transmitter> getTransmitters() {
        return transmitters;
    }

    public void setTransmitters(Set<Transmitter> transmitters) {
        this.transmitters = transmitters;
    }
}
