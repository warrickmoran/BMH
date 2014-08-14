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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeTypeAdapter;

/**
 * Transmitter Group information. A transmitter group of one is usually named
 * after the mnemonic of the transmitter.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 30, 2014 3175       rjpeter     Initial creation
 * Jun 26, 2014 3302       bkowal      Added a getter/setter for the
 *                                     Languages map.
 * Jul 8, 2014  3302       bkowal      Use eager fetching to eliminate session closed
 *                                     errors with lazy loading.
 * Jul 17, 2014  3406      mpduff      Added id pk column
 * Aug 04, 2014  3173      mpduff      Changed rcs to dac, added position and convenience methods, using serialization adapter
 * Aug 13, 2014  3486      bsteffen    Add getEnabledTransmitters
 * 
 * </pre>
 * 
 * @author rjpeter
 * @version 1.0
 */
@NamedQueries({ @NamedQuery(name = TransmitterGroup.GET_TRANSMITTER_GROUP_FOR_NAME, query = TransmitterGroup.GET_TRANSMITTER_GROUP_FOR_NAME_QUERY) })
@Entity
@Table(name = "transmitter_group", schema = "bmh", uniqueConstraints = { @UniqueConstraint(columnNames = { "name" }) })
@SequenceGenerator(initialValue = 1, name = TransmitterGroup.GEN, sequenceName = "zone_seq")
@DynamicSerialize
@DynamicSerializeTypeAdapter(factory = TransmitterGroupAdapter.class)
public class TransmitterGroup {

    static final String GEN = "Transmitter Group Generator";

    public static final String GET_TRANSMITTER_GROUP_FOR_NAME = "getTransmitterGroupForName";

    protected static final String GET_TRANSMITTER_GROUP_FOR_NAME_QUERY = "FROM TransmitterGroup tg WHERE tg.name = :name";

    public static final int NAME_LENGTH = 40;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = GEN)
    protected int id;

    /**
     * Alternate key, id only used to allow for ease of rename function.
     */
    @Column(length = NAME_LENGTH)
    private String name;

    @Embedded
    private Tone tone;

    @Column
    private Integer dac;

    @Column(length = 15)
    private String timeZone;

    @Column
    private Boolean silenceAlarm;

    @Column
    private Boolean daylightSaving;

    @Column
    private int position;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "transmitterGroup")
    private Set<Transmitter> transmitters;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Tone getTone() {
        return tone;
    }

    public void setTone(Tone tone) {
        this.tone = tone;
    }

    public Integer getDac() {
        return dac;
    }

    public void setDac(Integer dac) {
        this.dac = dac;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public Boolean getSilenceAlarm() {
        return silenceAlarm;
    }

    public void setSilenceAlarm(Boolean silenceAlarm) {
        this.silenceAlarm = silenceAlarm;
    }

    public Boolean getDaylightSaving() {
        return daylightSaving;
    }

    public void setDaylightSaving(Boolean daylightSaving) {
        this.daylightSaving = daylightSaving;
    }

    /**
     * @return the position
     */
    public int getPosition() {
        return position;
    }

    /**
     * @param position
     *            the position to set
     */
    public void setPosition(int position) {
        this.position = position;
    }

    public Set<Transmitter> getTransmitters() {
        if (transmitters == null) {
            transmitters = new HashSet<Transmitter>();
        }
        return transmitters;
    }

    public Set<Transmitter> getEnabledTransmitters() {
        Set<Transmitter> transmitters = getTransmitters();
        transmitters = new HashSet<>(transmitters);
        Iterator<Transmitter> it = transmitters.iterator();
        while (it.hasNext()) {
            if (it.next().getTxStatus() == TxStatus.DISABLED) {
                it.remove();
            }
        }
        return transmitters;
    }

    public void setTransmitters(Set<Transmitter> transmitters) {
        this.transmitters = transmitters;

        if (transmitters != null) {
            for (Transmitter trans : transmitters) {
                trans.setTransmitterGroup(this);
            }
        }
    }

    public void addTransmitter(Transmitter trans) {
        if (trans != null) {
            trans.setTransmitterGroup(this);

            if (transmitters == null) {
                transmitters = new LinkedHashSet<>();
            }

            transmitters.add(trans);
        }
    }

    public List<Transmitter> getTransmitterList() {
        List<Transmitter> transList;
        if (transmitters == null) {
            transList = new ArrayList<Transmitter>();
        } else {
            transList = new ArrayList<Transmitter>(transmitters.size());
            for (Transmitter t : transmitters) {
                transList.add(t);
            }
        }

        return transList;
    }

    /**
     * Is this a standalone group
     * 
     * @return true if is standalone, false otherwise
     */
    public boolean isStandalone() {
        if (transmitters != null && transmitters.size() == 1) {
            if (getTransmitterList().get(0).getMnemonic().equals(this.name)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((name == null) ? 0 : name.hashCode());
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
        TransmitterGroup other = (TransmitterGroup) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "TransmitterGroup [id=" + id + ", name=" + name + ", tone="
                + tone + ", dac=" + dac + ", timeZone=" + timeZone
                + ", silenceAlarm=" + silenceAlarm + ", daylightSaving="
                + daylightSaving + ", position=" + position + ", transmitters="
                + transmitters + "]";
    }

}
