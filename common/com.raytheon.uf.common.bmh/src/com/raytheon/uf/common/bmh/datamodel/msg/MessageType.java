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
package com.raytheon.uf.common.bmh.datamodel.msg;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.raytheon.uf.common.bmh.datamodel.language.TtsVoice;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Area;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Zone;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * MessageType record object. Represents the various type of data flowing
 * through BMH. Loosely tied to the concept of an AFOSID and in some cases is an
 * AFOSID, but that is not guaranteed.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 30, 2014 3175       rjpeter     Initial creation
 * Aug 06, 2014 #3490      lvenable    Added fetch type eagar to fields and changed
 *                                     same transmitters to a Set.
 * 
 * </pre>
 * 
 * @author rjpeter
 * @version 1.0
 */
@NamedQueries({ @NamedQuery(name = MessageType.GET_MESSAGETYPE_FOR_AFOSID, query = MessageType.GET_MESSAGETYPE_FOR_AFOSID_QUERY) })
@Entity
@DynamicSerialize
@Table(name = "message_type", schema = "bmh")
@SequenceGenerator(initialValue = 1, schema = "bmh", name = MessageType.GEN, sequenceName = "message_type_seq")
public class MessageType {
    public enum Designation {
        StationID, Forecast, Observation, Outlook, Watch, Warning, Advisory, TimeAnnouncement, Other
    }

    static final String GEN = "Message Type Generator";

    public static final String GET_MESSAGETYPE_FOR_AFOSID = "getMessageTypeForAfosId";

    protected static final String GET_MESSAGETYPE_FOR_AFOSID_QUERY = "FROM MessageType m WHERE m.afosid = :afosid";

    // use surrogate key
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = GEN)
    @DynamicSerializeElement
    protected int id;

    @Column(length = 9, unique = true)
    @DynamicSerializeElement
    private String afosid;

    @Column(length = 40, nullable = false)
    @DynamicSerializeElement
    private String title;

    @Column(nullable = false)
    @DynamicSerializeElement
    private boolean alert;

    @Column(nullable = false)
    @DynamicSerializeElement
    private boolean confirm;

    @Column(nullable = false)
    @DynamicSerializeElement
    private boolean emergencyOverride;

    @Column(nullable = false)
    @DynamicSerializeElement
    private boolean interrupt;

    @Column(nullable = false)
    @DynamicSerializeElement
    private Designation designation;

    @Column(length = 6, nullable = false)
    @DynamicSerializeElement
    private String duration;

    @Column(length = 6, nullable = false)
    @DynamicSerializeElement
    private String periodicity;

    @ManyToOne(optional = false)
    @JoinColumn(name = "voice")
    @DynamicSerializeElement
    private TtsVoice voice;

    @Column(length = 6)
    @DynamicSerializeElement
    private String toneBlackOutStart;

    @Column(length = 6)
    @DynamicSerializeElement
    private String toneBlackOutEnd;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @MapKey(name = "mnemonic")
    @JoinTable(name = "message_same_tx", schema = "bmh", joinColumns = @JoinColumn(name = "afosid"), inverseJoinColumns = @JoinColumn(name = "mnemonic"))
    @DynamicSerializeElement
    private Set<Transmitter> sameTransmitters;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(name = "message_replace", schema = "bmh", joinColumns = @JoinColumn(name = "id", nullable = false, unique = false), inverseJoinColumns = @JoinColumn(name = "replaces_id", nullable = false, unique = false))
    @DynamicSerializeElement
    private Set<MessageType> replacesMsgs;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(name = "message_default_areas", schema = "bmh", joinColumns = @JoinColumn(name = "afosid"), inverseJoinColumns = @JoinColumn(name = "areacode"))
    @DynamicSerializeElement
    private Set<Area> defaultAreas;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(name = "message_default_zones", schema = "bmh", joinColumns = @JoinColumn(name = "afosid"), inverseJoinColumns = @JoinColumn(name = "zonecode"))
    @DynamicSerializeElement
    private Set<Zone> defaultZones;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(name = "message_default_transmitters", schema = "bmh", joinColumns = @JoinColumn(name = "afosid"), inverseJoinColumns = @JoinColumn(name = "mnemonic"))
    @DynamicSerializeElement
    private Set<TransmitterGroup> defaultTransmitterGroups;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAfosid() {
        return afosid;
    }

    public void setAfosid(String afosid) {
        this.afosid = afosid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isAlert() {
        return alert;
    }

    public void setAlert(boolean alert) {
        this.alert = alert;
    }

    public boolean isConfirm() {
        return confirm;
    }

    public void setConfirm(boolean confirm) {
        this.confirm = confirm;
    }

    public boolean isEmergencyOverride() {
        return emergencyOverride;
    }

    public void setEmergencyOverride(boolean emergencyOverride) {
        this.emergencyOverride = emergencyOverride;
    }

    public boolean isInterrupt() {
        return interrupt;
    }

    public void setInterrupt(boolean interrupt) {
        this.interrupt = interrupt;
    }

    public Designation getDesignation() {
        return designation;
    }

    public void setDesignation(Designation designation) {
        this.designation = designation;
    }

    public String getToneBlackOutStart() {
        return toneBlackOutStart;
    }

    public void setToneBlackOutStart(String toneBlackOutStart) {
        this.toneBlackOutStart = toneBlackOutStart;
    }

    public String getToneBlackOutEnd() {
        return toneBlackOutEnd;
    }

    public void setToneBlackOutEnd(String toneBlackOutEnd) {
        this.toneBlackOutEnd = toneBlackOutEnd;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getPeriodicity() {
        return periodicity;
    }

    public void setPeriodicity(String periodicity) {
        this.periodicity = periodicity;
    }

    public TtsVoice getVoice() {
        return voice;
    }

    public void setVoice(TtsVoice voice) {
        this.voice = voice;
    }

    public Set<Transmitter> getSameTransmitters() {
        return sameTransmitters;
    }

    public void setSameTransmitters(Set<Transmitter> sameTransmitters) {
        this.sameTransmitters = sameTransmitters;
    }

    public void addSameTransmitter(Transmitter trans) {
        if (trans != null) {
            if (sameTransmitters == null) {
                sameTransmitters = new HashSet<>();
            }

            sameTransmitters.add(trans);
        }
    }

    public Set<MessageType> getReplacesMsgs() {
        return replacesMsgs;
    }

    public void setReplacesMsgs(Set<MessageType> replacesMsgs) {
        this.replacesMsgs = replacesMsgs;
    }

    public void addReplaceMsg(MessageType replaceMsg) {
        if (replaceMsg != null) {
            if (replacesMsgs == null) {
                replacesMsgs = new HashSet<>();
            }

            replacesMsgs.add(replaceMsg);
        }
    }

    public Set<Area> getDefaultAreas() {
        return defaultAreas;
    }

    public void setDefaultAreas(Set<Area> defaultAreas) {
        this.defaultAreas = defaultAreas;
    }

    public void addDefaultArea(Area area) {
        if (area != null) {
            if (defaultAreas == null) {
                defaultAreas = new HashSet<>();
            }

            defaultAreas.add(area);
        }
    }

    public Set<Zone> getDefaultZones() {
        return defaultZones;
    }

    public void setDefaultZones(Set<Zone> defaultZones) {
        this.defaultZones = defaultZones;
    }

    public void addDefaultZone(Zone zone) {
        if (zone != null) {
            if (defaultZones == null) {
                defaultZones = new HashSet<>();
            }

            defaultZones.add(zone);
        }
    }

    public Set<TransmitterGroup> getDefaultTransmitterGroups() {
        return defaultTransmitterGroups;
    }

    public void setDefaultTransmitterGroups(
            Set<TransmitterGroup> defaultTransmitterGroups) {
        this.defaultTransmitterGroups = defaultTransmitterGroups;
    }

    public void addDefaultTransmitterGroup(TransmitterGroup transmitterGroup) {
        if (transmitterGroup != null) {
            if (defaultTransmitterGroups == null) {
                defaultTransmitterGroups = new HashSet<>();
            }

            defaultTransmitterGroups.add(transmitterGroup);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((afosid == null) ? 0 : afosid.hashCode());
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
        MessageType other = (MessageType) obj;
        if (afosid == null) {
            if (other.afosid != null) {
                return false;
            }
        } else if (!afosid.equals(other.afosid)) {
            return false;
        }
        return true;
    }

}
