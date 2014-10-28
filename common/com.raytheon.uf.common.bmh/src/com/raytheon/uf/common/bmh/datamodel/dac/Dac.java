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
package com.raytheon.uf.common.bmh.datamodel.dac;

import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * 
 * Dac information
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Aug 04, 2014  3486     bsteffen    Initial creation
 * Aug 27, 2014  3432     mpduff      Added Serialization annotation.
 * Sep 22, 2014  #3652    lvenable    Added name column and a sequence generator for the ID.
 * Sep 25, 2014  3485     bsteffen    Add receiveAddress
 * Oct 21, 2014  3746     rjpeter     Hibernate upgrade.
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
@Entity
@Table(name = "dac_address", schema = "bmh", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "id" }),
        @UniqueConstraint(columnNames = { "name" }),
        @UniqueConstraint(columnNames = { "address" }),
        @UniqueConstraint(columnNames = { "receivePort" }) })
@SequenceGenerator(initialValue = 1, name = Dac.GEN, sequenceName = "dac_seq", allocationSize = 1)
@DynamicSerialize
public class Dac {
    static final String GEN = "DAC Generator";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = GEN)
    @DynamicSerializeElement
    private int id;

    @Column(length = 40, nullable = false)
    @DynamicSerializeElement
    private String name;

    /* 39 is long enough for IPv6 */
    @Column(length = 39)
    @DynamicSerializeElement
    private String address;

    @Column
    @DynamicSerializeElement
    private int receivePort;

    @Column
    @DynamicSerializeElement
    private String receiveAddress;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "dac_ports", schema = "bmh")
    @Column(name = "dataPort")
    @Fetch(FetchMode.SELECT)
    @DynamicSerializeElement
    private Set<Integer> dataPorts;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getReceivePort() {
        return receivePort;
    }

    public void setReceivePort(int receivePort) {
        this.receivePort = receivePort;
    }

    public String getReceiveAddress() {
        return receiveAddress;
    }

    public void setReceiveAddress(String receiveAddress) {
        this.receiveAddress = receiveAddress;
    }

    public Set<Integer> getDataPorts() {
        return dataPorts;
    }

    public void setDataPorts(Set<Integer> dataPorts) {
        this.dataPorts = dataPorts;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name != null) {
            this.name = name;
        } else {
            this.name = "";
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        if (id == 0) {
            result = prime * result + ((name == null) ? 0 : name.hashCode());
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
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
        Dac other = (Dac) obj;
        if (id != other.id) {
            return false;
        }
        if (id == 0) {
            if (name == null) {
                if (other.name != null) {
                    return false;
                }
            } else if (!name.equals(other.name)) {
                return false;
            }
        }
        return true;
    }

}
