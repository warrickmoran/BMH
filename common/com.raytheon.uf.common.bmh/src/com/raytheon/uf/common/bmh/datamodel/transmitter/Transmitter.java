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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Transmitter information.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * May 30, 2014  3175     rjpeter     Initial creation
 * Jun 30, 2014  3283     bsteffen    Add some getter/setters.
 * Jul 17, 2014  3406     mpduff      Added id pk column, named query, removed cascade
 * 
 * </pre>
 * 
 * @author rjpeter
 * @version 1.0
 */
// @NamedQueries({
// @NamedQuery(name = "getTransmitters", query = "SELECT t FROM Transmitter"),
// @NamedQuery(name = "getTransmitterByName", query =
// "SELECT t from Transmitter t where t.mnemonic = :mnemonic"),
// @NamedQuery(name = "getTransmitterNames", query =
// "SELECT t.mnemonic FROM Transmitter") })
@Entity
@Table(name = "transmitter", schema = "bmh", uniqueConstraints = { @UniqueConstraint(columnNames = { "mnemonic" }) })
@SequenceGenerator(initialValue = 1, name = Transmitter.GEN, sequenceName = "transmitter_seq")
@DynamicSerialize
public class Transmitter {
    static final String GEN = "Transmitter Generator";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = GEN)
    @DynamicSerializeElement
    protected int id;

    @Column(length = 5)
    @DynamicSerializeElement
    private String mnemonic;

    @Column(length = 20, nullable = false)
    @DynamicSerializeElement
    private String name;

    @Column(precision = 3, nullable = false)
    @DynamicSerializeElement
    private Float frequency;

    @Column(length = 10, nullable = false)
    @DynamicSerializeElement
    private String callSign;

    @Column(length = 30, nullable = false)
    @DynamicSerializeElement
    private String location;

    @Column(length = 30, nullable = false)
    @DynamicSerializeElement
    private String serviceArea;

    @Column(length = 20, nullable = false)
    @DynamicSerializeElement
    private String transmitterGroup;

    @Column
    @DynamicSerializeElement
    private int position;

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

    public String getMnemonic() {
        return mnemonic;
    }

    public void setMnemonic(String mnemonic) {
        this.mnemonic = mnemonic;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Float getFrequency() {
        return frequency;
    }

    public void setFrequency(Float frequency) {
        this.frequency = frequency;
    }

    public String getCallSign() {
        return callSign;
    }

    public void setCallSign(String callSign) {
        this.callSign = callSign;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getServiceArea() {
        return serviceArea;
    }

    public void setServiceArea(String serviceArea) {
        this.serviceArea = serviceArea;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getTransmitterGroup() {
        return transmitterGroup;
    }

    public void setTransmitterGroup(String transmitterGroup) {
        this.transmitterGroup = transmitterGroup;
    }

}
