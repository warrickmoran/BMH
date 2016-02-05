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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
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
 * Feb 03, 2015  4056     bsteffen    Add DEFAULT_RECEIVE_ADDRESS
 * May 12, 2015  4248     rjpeter     Remove bmh schema, standardize foreign/unique keys.
 * Jul 01, 2015 4602      rjpeter     Port order matters.
 * Nov 05, 2015  5092     bkowal      Associate a {@link Dac} with a fully configured
 *                                    {@link DacChannel} instead of just a port number.
 * Nov 09, 2015  5113     bkowal      Created named queries to validate uniqueness. Defined
 *                                    default values.
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
@NamedQueries({ @NamedQuery(name = Dac.VALIDATE_DAC_UNIQUENESS, query = Dac.VALIDATE_DAC_UNIQUENESS_QUERY) })
@Entity
@Table(name = "dac_address", uniqueConstraints = {
        @UniqueConstraint(name = "uk_dac_address_name", columnNames = { "name" }),
        @UniqueConstraint(name = "uk_dac_address_address", columnNames = { "address" }),
        @UniqueConstraint(name = "uk_dac_address_receivePort", columnNames = { "receivePort" }) })
@SequenceGenerator(initialValue = 1, allocationSize = 1, name = Dac.GEN, sequenceName = "dac_seq")
@DynamicSerialize
public class Dac {
    static final String GEN = "DAC Generator";

    public static final String VALIDATE_DAC_UNIQUENESS = "validateDacUniqueness";

    protected static final String VALIDATE_DAC_UNIQUENESS_QUERY = "FROM Dac d WHERE d.id != :dacId AND (d.name = :name OR d.address = :address OR (d.receiveAddress = :receiveAddress AND d.receivePort = :receivePort))";

    public static final int DAC_NAME_LENGTH = 40;

    public static final int IP_LENGTH = 39;

    public static final String DEFAULT_NET_MASK = "255.255.255.0";

    public static final String DEFAULT_GATEWAY = "10.2.69.254";

    public static final int DEFAULT_BROADCAST_BUFFER = 5;

    public static final int BROADCAST_BUFFER_MIN = 0;

    public static final int BROADCAST_BUFFER_MAX = 255;

    public static final String DEFAULT_RECEIVE_ADDRESS = System.getProperty(
            "DefaultDacReceiveAddress", "239.255.86.75");

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = GEN)
    @DynamicSerializeElement
    private int id;

    @Column(length = DAC_NAME_LENGTH, nullable = false)
    @DynamicSerializeElement
    private String name;

    /* 39 is long enough for IPv6 */
    @Column(length = IP_LENGTH, nullable = false)
    @DynamicSerializeElement
    private String address;

    @Column(length = IP_LENGTH, nullable = false)
    @DynamicSerializeElement
    private String netMask = DEFAULT_NET_MASK;

    @Column(length = IP_LENGTH, nullable = false)
    @DynamicSerializeElement
    private String gateway = DEFAULT_GATEWAY;

    @Column(nullable = false)
    private int broadcastBuffer = DEFAULT_BROADCAST_BUFFER;

    @Column
    @DynamicSerializeElement
    private String receiveAddress;

    @Column
    @DynamicSerializeElement
    private int receivePort;

    @OneToMany(mappedBy = "dac", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @Fetch(FetchMode.SUBSELECT)
    @OrderColumn(name = "channel")
    @DynamicSerializeElement
    private List<DacChannel> channels;

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

    /**
     * @return the netMask
     */
    public String getNetMask() {
        return netMask;
    }

    /**
     * @param netMask
     *            the netMask to set
     */
    public void setNetMask(String netMask) {
        this.netMask = netMask;
    }

    /**
     * @return the gateway
     */
    public String getGateway() {
        return gateway;
    }

    /**
     * @param gateway
     *            the gateway to set
     */
    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    /**
     * @return the broadcastBuffer
     */
    public int getBroadcastBuffer() {
        return broadcastBuffer;
    }

    /**
     * @param broadcastBuffer
     *            the broadcastBuffer to set
     */
    public void setBroadcastBuffer(int broadcastBuffer) {
        if (broadcastBuffer < BROADCAST_BUFFER_MIN
                || broadcastBuffer > BROADCAST_BUFFER_MAX) {
            throw new IllegalArgumentException(
                    "The specified buffer size must be in the range: "
                            + BROADCAST_BUFFER_MIN + " to "
                            + BROADCAST_BUFFER_MAX + "!");
        }
        this.broadcastBuffer = broadcastBuffer;
    }

    public String getReceiveAddress() {
        return receiveAddress;
    }

    public void setReceiveAddress(String receiveAddress) {
        this.receiveAddress = receiveAddress;
    }

    public int getReceivePort() {
        return receivePort;
    }

    public void setReceivePort(int receivePort) {
        this.receivePort = receivePort;
    }

    /**
     * @return the channels
     */
    public List<DacChannel> getChannels() {
        return channels;
    }

    /**
     * @param channels
     *            the channels to set
     */
    public void setChannels(List<DacChannel> channels) {
        this.channels = channels;
        if (channels == null || channels.isEmpty()) {
            return;
        }
        if (channels.size() != 4) {
            throw new IllegalArgumentException(
                    "4 channels are required for a DAC; " + channels.size()
                            + " channels were provided!");
        }
        for (int i = 0; i < channels.size(); i++) {
            DacChannel channel = this.channels.get(i);
            channel.setDac(this);
            channel.getId().setChannel(i);
        }
    }

    public List<Integer> getDataPorts() {
        List<Integer> dataPorts = new ArrayList<>(this.channels.size());
        for (DacChannel channel : this.channels) {
            dataPorts.add(channel.getPort());
        }
        return dataPorts;
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
