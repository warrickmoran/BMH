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
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.raytheon.uf.common.bmh.datamodel.msg.ProgramSummary;
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
 * Aug 18, 2014  3532      bkowal      Added adjustAudioMinDB and adjustAudioMaxDB
 * Aug 21, 2014  3486      lvenable    Initialized silence alram to false.
 * Aug 25, 2014  3558      rjpeter     Added query for enabled transmitter groups.
 * Sep 4, 2014   3532      bkowal      Use a decibel target instead of a range.
 * Oct 07, 2014  3649      rferrel     addTrasmitter now replaces old entry with new.
 * Oct 13, 2014 3654       rjpeter     Updated to use ProgramSummary.
 * </pre>
 * 
 * @author rjpeter
 * @version 1.0
 */
@NamedQueries({
        @NamedQuery(name = TransmitterGroup.GET_TRANSMITTER_GROUP_FOR_NAME, query = TransmitterGroup.GET_TRANSMITTER_GROUP_FOR_NAME_QUERY),
        @NamedQuery(name = TransmitterGroup.GET_ENABLED_TRANSMITTER_GROUPS, query = TransmitterGroup.GET_ENABLED_TRANSMITTER_GROUPS_QUERY) })
@Entity
@Table(name = "transmitter_group", schema = "bmh", uniqueConstraints = { @UniqueConstraint(columnNames = { "name" }) })
@SequenceGenerator(initialValue = 1, name = TransmitterGroup.GEN, sequenceName = "zone_seq")
@DynamicSerialize
@DynamicSerializeTypeAdapter(factory = TransmitterGroupAdapter.class)
public class TransmitterGroup {

    static final String GEN = "Transmitter Group Generator";

    public static final String GET_TRANSMITTER_GROUP_FOR_NAME = "getTransmitterGroupForName";

    protected static final String GET_TRANSMITTER_GROUP_FOR_NAME_QUERY = "FROM TransmitterGroup tg WHERE tg.name = :name";

    public static final String GET_ENABLED_TRANSMITTER_GROUPS = "getEnabledTransmitterGroups";

    protected static final String GET_ENABLED_TRANSMITTER_GROUPS_QUERY = "select tg FROM TransmitterGroup tg inner join tg.transmitters t WHERE t.txStatus = 'ENABLED'";

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
    private Boolean silenceAlarm = false;

    @Column
    private Boolean daylightSaving;

    @Column
    private int position;

    /*
     * TODO: defaults? we may at least need defaults for the import of legacy
     * information?
     */
    @Column(nullable = false)
    private double audioDBTarget = -10.0;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "transmitterGroup")
    private Set<Transmitter> transmitters;

    @ManyToOne
    private ProgramSummary program;

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

    /**
     * @return the audioDBTarget
     */
    public double getAudioDBTarget() {
        return audioDBTarget;
    }

    /**
     * @param audioDBTarget
     *            the audioDBTarget to set
     */
    public void setAudioDBTarget(double audioDBTarget) {
        this.audioDBTarget = audioDBTarget;
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

            // Assume trans should replace old entry.
            transmitters.remove(trans);
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
        if ((transmitters != null) && (transmitters.size() == 1)) {
            if (getTransmitterList().get(0).getMnemonic().equals(this.name)) {
                return true;
            }
        }

        return false;
    }

    public ProgramSummary getProgram() {
        return program;
    }

    public void setProgram(ProgramSummary program) {
        this.program = program;
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
        if (id != other.id) {
            return false;
        }
        // Comparing new groups not in database.
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

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("TransmitterGroup [id=");
        stringBuilder.append(this.id);
        stringBuilder.append(", name=");
        stringBuilder.append(this.name);
        stringBuilder.append(", tone=");
        stringBuilder.append(this.tone);
        stringBuilder.append(", dac=");
        stringBuilder.append(this.dac);
        stringBuilder.append(", timeZone=");
        stringBuilder.append(this.timeZone);
        stringBuilder.append(", silenceAlarm=");
        stringBuilder.append(this.silenceAlarm);
        stringBuilder.append(", daylightSaving=");
        stringBuilder.append(this.daylightSaving);
        stringBuilder.append(", position=");
        stringBuilder.append(this.position);
        stringBuilder.append(", audioDBTarget=");
        stringBuilder.append(this.audioDBTarget);
        stringBuilder.append(", program=");
        stringBuilder.append(this.program);
        stringBuilder.append(", transmitters=");
        stringBuilder.append(this.transmitters);
        stringBuilder.append("]");

        return stringBuilder.toString();
    }

}
