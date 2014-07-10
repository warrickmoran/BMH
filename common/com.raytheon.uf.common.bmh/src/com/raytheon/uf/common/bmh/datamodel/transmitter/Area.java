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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

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
 * 
 * </pre>
 * 
 * @author rjpeter
 * @version 1.0
 */
@Entity
@Table(name = "area", schema = "bmh")
@DynamicSerialize
public class Area {
    /**
     * SSXNNN - 6 digit UGC area code
     * 
     * <pre>
     * SS - State
     * X - C for county code, and a numeral (i.e., 1 through 9) for a partial area code
     * NNN - county code number
     * </pre>
     */
    @Id
    @Column(length = 6)
    @DynamicSerializeElement
    private String areaCode;

    @Column(length = 20, nullable = false)
    @DynamicSerializeElement
    private String areaName;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(name = "area_tx", schema = "bmh", joinColumns = @JoinColumn(name = "areaCode"), inverseJoinColumns = @JoinColumn(name = "mnemonic"))
    @DynamicSerializeElement
    private Set<Transmitter> transmitters;

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
