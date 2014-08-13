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
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

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
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
@Entity
@Table(name = "dac_address", schema = "bmh", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "id" }),
        @UniqueConstraint(columnNames = { "address" }),
        @UniqueConstraint(columnNames = { "receivePort" }) })
public class Dac {

    @Id
    private int id;
    
    /* 39 is long enough for IPv6 */
    @Column(length = 39)
    private String address;
    
    @Column
    private int receivePort;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "dac_ports", schema = "bmh")
    @Column(name = "dataPort")
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

    public Set<Integer> getDataPorts() {
        return dataPorts;
    }

    public void setDataPorts(Set<Integer> dataPorts) {
        this.dataPorts = dataPorts;
    }
    
}
