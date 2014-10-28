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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.ForeignKey;

import com.raytheon.uf.common.bmh.BMHLoggerUtils;
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
 * Jul 17, 2014 3406       mpduff      Added id pk column
 * Aug 04, 2014 3173       mpduff      Changed rcs to dac, added position and convenience methods, using serialization adapter
 * Aug 13, 2014 3486       bsteffen    Add getEnabledTransmitters
 * Aug 18, 2014 3532       bkowal      Added adjustAudioMinDB and adjustAudioMaxDB
 * Aug 21, 2014 3486       lvenable    Initialized silence alram to false.
 * Aug 25, 2014 3558       rjpeter     Added query for enabled transmitter groups.
 * Sep 4, 2014  3532       bkowal      Use a decibel target instead of a range.
 * Oct 07, 2014 3649       rferrel     addTrasmitter now replaces old entry with new.
 * Oct 11, 2014  3630      mpduff      Add enable/disable group
 * Oct 13, 2014 3654       rjpeter     Updated to use ProgramSummary.
 * Oct 13, 2014 3636       rferrel     For logging modified toString to show transmitters' mnemonic add LogEntry.
 * Oct 13, 2014  3636      rferrel     For logging modified toString to show transmitters' mnemonic add LogEntry.
 * Oct 21, 2014 3746       rjpeter     Hibernate upgrade.
 * Oct 27, 2014 3630       mpduff      Add annotation for hibernate.
 * 
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
    @Fetch(FetchMode.SUBSELECT)
    private Set<Transmitter> transmitters;

    @ManyToOne
    @ForeignKey(name = "transmitter_group_to_program")
    @JoinColumn(name = "program_id")
    private ProgramSummary programSummary;

    /**
     * Set of transmitters enabled when the whole group is disabled.
     */
    @Transient
    private final Set<Integer> prevEnabledTransmitters = new HashSet<>();

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
     * Determine if the group is enabled, one or more enabled transmitters means
     * the group is enabled.
     * 
     * @return true if one or more transmitters is enabled
     */
    public boolean isEnabled() {
        for (Transmitter t : getTransmitters()) {
            if (t.getTxStatus() == TxStatus.ENABLED) {
                return true;
            }
        }

        return false;
    }

    /**
     * Disables all transmitters in the group. Keeps track of which transmitters
     * were enabled before this call so that the same transmitters can be
     * re-enabled.
     */
    public void disableGroup() {
        if (!transmitters.isEmpty()) {
            for (Transmitter t : transmitters) {
                if (t.getTxStatus() == TxStatus.ENABLED) {
                    prevEnabledTransmitters.add(t.id);
                    t.setTxStatus(TxStatus.DISABLED);
                }
            }
        }
    }

    /**
     * Enables transmitters in the group.
     * 
     * If enableAll is true then all transmitters are enabled, otherwise only
     * those that were previously enabled are re-enabled.
     * 
     * @param enableAll
     *            true to enable all transmitters, false to enable those
     *            previously enabled
     */
    public void enableGroup(boolean enableAll) {
        if (!transmitters.isEmpty()) {
            for (Transmitter t : transmitters) {
                if (enableAll) {
                    t.setTxStatus(TxStatus.ENABLED);
                } else {
                    if (prevEnabledTransmitters.contains(t.getId())) {
                        t.setTxStatus(TxStatus.ENABLED);
                    }
                }
            }

            prevEnabledTransmitters.clear();
        }
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

    public ProgramSummary getProgramSummary() {
        return programSummary;
    }

    public void setProgramSummary(ProgramSummary programSummary) {
        this.programSummary = programSummary;
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
        stringBuilder.append(this.programSummary);
        stringBuilder.append(", transmitters=[");
        if ((transmitters != null) && (transmitters.size() > 0)) {
            for (Transmitter t : transmitters) {
                stringBuilder.append(t.getMnemonic()).append(", ");
            }
            stringBuilder.setLength(stringBuilder.length() - 2);
        }
        stringBuilder.append("]]");

        return stringBuilder.toString();
    }

    /**
     * Get log entry.
     * 
     * @param oldGroup
     *            - When null assume this is a new transmitter.
     * @param user
     *            - Who is making the change
     * @return entry - empty string when no differences.
     */
    public String logEntry(TransmitterGroup oldGroup, String user) {
        boolean logChanges = false;
        StringBuilder sb = new StringBuilder();
        sb.append("User ").append(user);
        if (oldGroup == null) {
            sb.append(" New ").append(toString());
        } else {
            sb.append(" Update Transmitter Group name/id: \"")
                    .append(getName()).append("\"/").append(getId())
                    .append(" [");
            if (!oldGroup.getName().equals(getName())) {
                BMHLoggerUtils.logFieldChange(sb, "name", oldGroup.getName(),
                        getName());
                logChanges = true;
            }

            Object oldValue = oldGroup.getTone();
            if (oldValue == null) {
                oldValue = "";
            }
            Object newValue = getTone();
            if (newValue == null) {
                newValue = "";
            }
            if (!oldValue.equals(newValue)) {
                BMHLoggerUtils.logFieldChange(sb, "tone", oldValue, newValue);
                logChanges = true;
            }
            oldValue = oldGroup.getDac();
            if (oldValue == null) {
                oldValue = "None";
            }
            newValue = getDac();
            if (newValue == null) {
                newValue = "None";
            }
            if (!oldValue.equals(newValue)) {
                BMHLoggerUtils.logFieldChange(sb, "dac", oldValue, newValue);
                logChanges = true;
            }

            if (!oldGroup.getTimeZone().equals(getTimeZone())) {
                BMHLoggerUtils.logFieldChange(sb, "timeZone",
                        oldGroup.getTimeZone(), getTimeZone());
                logChanges = true;
            }
            if (!oldGroup.getSilenceAlarm().equals(getSilenceAlarm())) {
                BMHLoggerUtils.logFieldChange(sb, "silenceAlarm",
                        oldGroup.getSilenceAlarm(), getSilenceAlarm());
                logChanges = true;
            }
            if (!oldGroup.getDaylightSaving().equals(getDaylightSaving())) {
                BMHLoggerUtils.logFieldChange(sb, "daylightSaving",
                        oldGroup.getDaylightSaving(), getDaylightSaving());
                logChanges = true;
            }
            if (oldGroup.getPosition() != getPosition()) {
                BMHLoggerUtils.logFieldChange(sb, "position", new Integer(
                        oldGroup.getPosition()), new Integer(getPosition()));
                logChanges = true;
            }

            if (oldGroup.getAudioDBTarget() != getAudioDBTarget()) {
                BMHLoggerUtils.logFieldChange(sb, "audioDBTarget", new Double(
                        oldGroup.getAudioDBTarget()), new Double(
                        getAudioDBTarget()));
                logChanges = true;
            }

            List<Transmitter> oldTransmitters = oldGroup.getTransmitterList();
            List<Transmitter> newTransmitters = getTransmitterList();
            if (!transmittersEqual(oldTransmitters, newTransmitters)) {
                oldValue = transmittersToString(oldTransmitters);
                newValue = transmittersToString(newTransmitters);
                BMHLoggerUtils.logFieldChange(sb, "transmitters", oldValue,
                        newValue);
                logChanges = true;
            }

            // No changes made.
            if (!logChanges) {
                return "";
            }
            sb.setCharAt(sb.length() - 2, ']');
        }
        return sb.toString();
    }

    private String transmittersToString(List<Transmitter> transmitters) {
        StringBuilder sb = new StringBuilder("[");
        if (transmitters.size() > 0) {
            for (Transmitter t : transmitters) {
                sb.append(t.getMnemonic()).append(", ");
            }
            sb.setLength(sb.length() - 2);
        }
        sb.append("]");
        return sb.toString();
    }

    private boolean transmittersEqual(List<Transmitter> l1, List<Transmitter> l2) {
        if (l1.size() != l2.size()) {
            return false;
        }

        for (Transmitter t1 : l1) {
            if (!l2.contains(t1)) {
                return false;
            }
        }
        return true;
    }
}
