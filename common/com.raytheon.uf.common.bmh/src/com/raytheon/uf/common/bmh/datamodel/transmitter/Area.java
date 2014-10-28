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

import java.util.HashSet;
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
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.raytheon.uf.common.bmh.diff.DiffString;
import com.raytheon.uf.common.bmh.diff.DiffTitle;
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
 * Aug 14, 2014  3411     mpduff      Add areaName to unique constraint
 * Oct 21, 2014  3746     rjpeter     Hibernate upgrade.
 * Oct 24, 2014  3636     rferrel     Implement logging.
 * </pre>
 * 
 * @author rjpeter
 * @version 1.0
 */
@NamedQueries({ @NamedQuery(name = Area.GET_AREA_FOR_CODE, query = Area.GET_AREA_FOR_CODE_QUERY) })
@Entity
@Table(name = "area", schema = "bmh", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "areaCode" }),
        @UniqueConstraint(columnNames = { "areaName" }) })
@SequenceGenerator(initialValue = 1, name = Area.GEN, sequenceName = "area_seq")
@DynamicSerialize
public class Area {
    static final String GEN = "Area Generator";

    public static final String GET_AREA_FOR_CODE = "getAreaForCode";

    protected static final String GET_AREA_FOR_CODE_QUERY = "FROM Area a WHERE a.areaCode = :areaCode";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = GEN)
    @DynamicSerializeElement
    @DiffTitle(position = 2)
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
    @DiffTitle(position = 1)
    @DiffString
    private String areaCode;

    @Column(length = 30, nullable = false)
    @DynamicSerializeElement
    private String areaName;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "area_tx", schema = "bmh", joinColumns = @JoinColumn(name = "areaId"), inverseJoinColumns = @JoinColumn(name = "transmitterId"))
    @Fetch(FetchMode.SELECT)
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

    public void addTransmitter(Transmitter transmitter) {
        if (transmitter != null) {
            if (transmitters == null) {
                transmitters = new HashSet<>();
            }

            transmitters.add(transmitter);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result)
                + ((areaCode == null) ? 0 : areaCode.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Area other = (Area) obj;
        if (areaCode == null) {
            if (other.areaCode != null) {
                return false;
            }
        } else if (!areaCode.equals(other.areaCode)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Area [areaId=").append(areaId).append(", areaCode=")
                .append(areaCode).append(", areaName=").append(areaName)
                .append(", transmitters=");
        if (transmitters == null) {
            sb.append(transmitters);
        } else {
            sb.append("[");
            if (transmitters.size() > 0) {
                for (Transmitter transmitter : transmitters) {
                    sb.append(transmitter.getMnemonic()).append(", ");
                }
                sb.setLength(sb.length() - 2);
            }
            sb.append("]");
        }
        sb.append("]");
        return sb.toString();
    }

}
