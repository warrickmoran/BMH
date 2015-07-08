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
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
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
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;

import com.raytheon.uf.common.bmh.datamodel.PositionComparator;
import com.raytheon.uf.common.bmh.datamodel.PositionOrdered;
import com.raytheon.uf.common.bmh.datamodel.msg.ProgramSummary;
import com.raytheon.uf.common.bmh.diff.DiffString;
import com.raytheon.uf.common.bmh.diff.DiffTitle;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeTypeAdapter;
import com.raytheon.uf.common.util.CollectionUtil;

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
 * Oct 21, 2014 3746       rjpeter     Hibernate upgrade.
 * Oct 27, 2014 3630       mpduff      Add annotation for hibernate.
 * 
 * Oct 28, 2014 3617       dgilling    Encapsulate time zone/DST flag into single
 *                                     time zone field using Java TimeZone id string.
 * Nov 21, 2014 3845       bkowal      Added getTransmitterGroupContainsTransmitter
 * Jan 08, 2015 3821       bsteffen    Rename silenceAlarm to deadAirAlarm
 * Jan 14, 2015 3994       rjpeter     Added distinct to {@link #GET_ENABLED_TRANSMITTER_GROUPS}.
 * Jan 22, 2015 3995       rjpeter     Added {@link #GET_TRANSMITTER_GROUP_MAX_POSITION}, removed Tone.
 * Feb 02, 2015 4080       bkowal      Only include transmitters with a TxStatus of enabled in the
 *                                     {@link Set} produced by {@link #getEnabledTransmitters()}.
 * Mar 03, 2015 3962       rferrel     Added logic for MAINT status.
 * Mar 18, 2015 4298       bkowal      Added {@link #getTransmitterWithStatus(TxStatus)}.
 * Apr 02, 2015 4248       rjpeter     Implement PositionOrdered.
 * Apr 14, 2015 4390       rferrel     Removed constraint on position to allow reordering using PositionOrdered.
 * Apr 14, 2015 4394       bkowal      Added {@link #GET_CONFIGURED_TRANSMITTER_GROUPS}.
 * May 08, 2015 4470       bkowal      Configured transmitters must have both an associated dac and port.
 * May 12, 2015 4248       rjpeter     Remove bmh schema, standardize foreign/unique keys.
 * Jul 08, 2015 4636       bkowal      Support multiple decibel target levels.
 * </pre>
 * 
 * @author rjpeter
 * @version 1.0
 */
@NamedQueries({
        @NamedQuery(name = TransmitterGroup.GET_TRANSMITTER_GROUP_FOR_NAME, query = TransmitterGroup.GET_TRANSMITTER_GROUP_FOR_NAME_QUERY),
        @NamedQuery(name = TransmitterGroup.GET_ENABLED_TRANSMITTER_GROUPS, query = TransmitterGroup.GET_ENABLED_TRANSMITTER_GROUPS_QUERY),
        @NamedQuery(name = TransmitterGroup.GET_TRANSMITTER_GROUP_CONTAINS_TRANSMITTER, query = TransmitterGroup.GET_TRANSMITTER_GROUP_CONTAINS_TRANSMITTER_QUERY),
        @NamedQuery(name = TransmitterGroup.GET_CONFIGURED_TRANSMITTER_GROUPS, query = TransmitterGroup.GET_CONFIGURED_TRANSMITTER_GROUPS_QUERY),
        @NamedQuery(name = TransmitterGroup.GET_TRANSMITTER_GROUP_MAX_POSITION, query = TransmitterGroup.GET_TRANSMITTER_GROUP_MAX_POSITION_QUERY) })
@Entity
@Table(name = "transmitter_group", uniqueConstraints = { @UniqueConstraint(name = "uk_tx_group_name", columnNames = { "name" }) })
@SequenceGenerator(initialValue = 1, allocationSize = 1, name = TransmitterGroup.GEN, sequenceName = "transmitter_group_seq")
@DynamicSerialize
@DynamicSerializeTypeAdapter(factory = TransmitterGroupAdapter.class)
public class TransmitterGroup implements PositionOrdered {

    static final String GEN = "Transmitter Group Generator";

    public static final String GET_TRANSMITTER_GROUP_FOR_NAME = "getTransmitterGroupForName";

    protected static final String GET_TRANSMITTER_GROUP_FOR_NAME_QUERY = "FROM TransmitterGroup tg WHERE tg.name = :name";

    public static final String GET_ENABLED_TRANSMITTER_GROUPS = "getEnabledTransmitterGroups";

    protected static final String GET_ENABLED_TRANSMITTER_GROUPS_QUERY = "select distinct tg FROM TransmitterGroup tg inner join tg.transmitters t WHERE t.txStatus = 'ENABLED'";

    public static final String GET_CONFIGURED_TRANSMITTER_GROUPS = "getConfiguredTransmitterGroups";

    protected static final String GET_CONFIGURED_TRANSMITTER_GROUPS_QUERY = "select distinct tg FROM TransmitterGroup tg inner join tg.transmitters t WHERE tg.dac is not null AND t.dacPort is not null";

    public static final String GET_TRANSMITTER_GROUP_CONTAINS_TRANSMITTER = "getTransmitterGroupContainsTransmitter";

    protected static final String GET_TRANSMITTER_GROUP_CONTAINS_TRANSMITTER_QUERY = "SELECT tg FROM TransmitterGroup tg INNER JOIN tg.transmitters t WHERE t.id = :transmitterId";

    public static final String GET_TRANSMITTER_GROUP_MAX_POSITION = "getTransmitterGroupMaxPosition";

    protected static final String GET_TRANSMITTER_GROUP_MAX_POSITION_QUERY = "SELECT MAX(position) FROM TransmitterGroup";

    public static final int NAME_LENGTH = 40;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = GEN)
    @DiffTitle(position = 2)
    protected int id;

    /**
     * Alternate key, id only used to allow for ease of rename function.
     */
    @Column(length = NAME_LENGTH, nullable = false)
    @DiffTitle(position = 1)
    @DiffString
    private String name;

    @Column
    private Integer dac;

    @Column(length = 25, nullable = false)
    private String timeZone;

    @Column(nullable = false)
    private boolean deadAirAlarm = true;

    @Column(nullable = false)
    private int position;

    @Column(nullable = false, columnDefinition = "Decimal (3,1)")
    private double audioDBTarget = 0.0;

    @Column(nullable = false, columnDefinition = "Decimal (3,1)")
    private double sameDBTarget = 0.0;

    @Column(nullable = false, columnDefinition = "Decimal (3,1)")
    private double alertDBTarget = 0.0;

    @Column(nullable = false, columnDefinition = "Decimal (3,1)")
    private double transferDBTarget = 0.0;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "transmitterGroup")
    @Fetch(FetchMode.SUBSELECT)
    private Set<Transmitter> transmitters;

    @ManyToOne
    @ForeignKey(name = "fk_tx_group_to_program")
    @JoinColumn(name = "program_id")
    @Index(name = "tx_group_program_idx")
    private ProgramSummary programSummary;

    public TransmitterGroup() {
    }

    public TransmitterGroup(TransmitterGroup tg) {
        id = tg.id;
        name = tg.name;
        dac = tg.dac;
        timeZone = tg.timeZone;
        deadAirAlarm = tg.deadAirAlarm;
        position = tg.position;
        audioDBTarget = tg.audioDBTarget;
        if (tg.transmitters != null) {
            transmitters = new HashSet<>(tg.transmitters);
        }
        programSummary = tg.programSummary;
    }

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

    public boolean getDeadAirAlarm() {
        return deadAirAlarm;
    }

    public void setDeadAirAlarm(boolean deadAirAlarm) {
        this.deadAirAlarm = deadAirAlarm;
    }

    /**
     * @return the position
     */
    @Override
    public int getPosition() {
        return position;
    }

    /**
     * @param position
     *            the position to set
     */
    @Override
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

    /**
     * @return the sameDBTarget
     */
    public double getSameDBTarget() {
        return sameDBTarget;
    }

    /**
     * @param sameDBTarget the sameDBTarget to set
     */
    public void setSameDBTarget(double sameDBTarget) {
        this.sameDBTarget = sameDBTarget;
    }

    /**
     * @return the alertDBTarget
     */
    public double getAlertDBTarget() {
        return alertDBTarget;
    }

    /**
     * @param alertDBTarget the alertDBTarget to set
     */
    public void setAlertDBTarget(double alertDBTarget) {
        this.alertDBTarget = alertDBTarget;
    }

    /**
     * @return the transferDBTarget
     */
    public double getTransferDBTarget() {
        return transferDBTarget;
    }

    /**
     * @param transferDBTarget the transferDBTarget to set
     */
    public void setTransferDBTarget(double transferDBTarget) {
        this.transferDBTarget = transferDBTarget;
    }

    public Set<Transmitter> getTransmitters() {
        if (transmitters == null) {
            transmitters = new HashSet<>();
        }
        return transmitters;
    }

    /**
     * Returns a {@link List} of {@link Transmitter}s that have been configured
     * sorted by position.
     * 
     * @return a {@link List} of {@link Transmitter}s sorted by position.
     */
    public List<Transmitter> getOrderedConfiguredTransmittersList() {
        if (this.dac == null || CollectionUtil.isNullOrEmpty(this.transmitters)) {
            return Collections.emptyList();
        }

        List<Transmitter> configuredTransmitters = new ArrayList<>(
                this.transmitters.size());
        for (Transmitter transmitter : this.transmitters) {
            if (transmitter.getDacPort() == null) {
                continue;
            }
            configuredTransmitters.add(transmitter);
        }

        if (configuredTransmitters.size() > 1) {
            Collections.sort(configuredTransmitters, new PositionComparator());
        }

        return configuredTransmitters;
    }

    public Set<Transmitter> getTransmitterWithStatus(final TxStatus status) {
        Set<Transmitter> transmitters = getTransmitters();
        transmitters = new HashSet<>(transmitters);
        Iterator<Transmitter> it = transmitters.iterator();
        while (it.hasNext()) {
            if (it.next().getTxStatus() != status) {
                it.remove();
            }
        }
        return transmitters;
    }

    public Set<Transmitter> getEnabledTransmitters() {
        return this.getTransmitterWithStatus(TxStatus.ENABLED);
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
     * Determine if the group is in maintenance.
     * 
     * @return true if one or more transmitters' status is maintenance
     */
    public boolean isMaint() {
        for (Transmitter t : getTransmitters()) {
            if (t.getTxStatus() == TxStatus.MAINT) {
                return true;
            }
        }

        return false;
    }

    public boolean isDisabled() {
        for (Transmitter t : getTransmitters()) {
            if (t.getTxStatus() == TxStatus.DISABLED) {
                return true;
            }
        }

        return false;
    }

    /**
     * Group's enabled transmitters change to maintenance.
     */
    public void maintenanceGroup() {
        if (!transmitters.isEmpty()) {
            for (Transmitter t : transmitters) {
                if (t.getTxStatus() == TxStatus.ENABLED) {
                    t.setTxStatus(TxStatus.MAINT);
                }
            }
        }
    }

    /**
     * Group's maintenance transmitters change to enabled.
     * 
     */
    public void enableGroup() {
        if (!transmitters.isEmpty()) {
            for (Transmitter t : transmitters) {
                if (t.getTxStatus() == TxStatus.MAINT) {
                    t.setTxStatus(TxStatus.ENABLED);
                }
            }
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
        stringBuilder.append(", dac=");
        stringBuilder.append(this.dac);
        stringBuilder.append(", timeZone=");
        stringBuilder.append(this.timeZone);
        stringBuilder.append(", deadAirAlarm=");
        stringBuilder.append(this.deadAirAlarm);
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
}
