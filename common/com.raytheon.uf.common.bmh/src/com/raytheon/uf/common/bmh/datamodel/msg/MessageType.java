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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.ForeignKey;

import com.raytheon.uf.common.bmh.BMHLoggerUtils;
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
 * Aug 06, 2014 3490       lvenable    Added fetch type eagar to fields and changed
 *                                     same transmitters to a Set.
 * Aug 12, 2014 3490       lvenable    Added wxr and enable tone blackout fields.
 * Aug 17, 2014 3490       lvenable    Added batch size, removed cascade all.
 * Aug 18, 2014 3411       mpduff      Added {@link MessageTypeReplacement}
 * Sep 2, 2014  3568       bkowal      Added the getMessageTypeForDesignation named query
 * Sep 15, 2014 3610       lvenable    Added query for getting Afos ID and Title.
 * Sep 19, 2014 3611       lvenable    Added query for getting emergency override message types.
 * Oct 13, 2014 3654       rjpeter     Added additional queries.
 * Oct 16, 2014 3636       rferrel     Add logging.
 * Oct 21, 2014 3746       rjpeter     Hibernate upgrade.
 * Oct 23, 2014  #3728     lvenable    Added query for getting AFOS IDs by designation.
 * 
 * </pre>
 * 
 * @author rjpeter
 * @version 1.0
 */
@NamedQueries({
        @NamedQuery(name = MessageType.GET_MESSAGETYPE_AFOSID_TITLE, query = MessageType.GET_MESSAGETYPE_AFOSID_TITLE_QUERY),
        @NamedQuery(name = MessageType.GET_MESSAGETYPE_FOR_AFOSID, query = MessageType.GET_MESSAGETYPE_FOR_AFOSID_QUERY),
        @NamedQuery(name = MessageType.GET_MESSAGETYPE_AFOSID_DESIGNATION, query = MessageType.GET_MESSAGETYPE_AFOSID_DESIGNATION_QUERY),
        @NamedQuery(name = MessageType.GET_MESSAGETYPE_FOR_EMERGENCYOVERRIDE, query = MessageType.GET_MESSAGETYPE_FOR_EMERGENCYOVERRIDE_QUERY),
        @NamedQuery(name = MessageType.GET_MESSAGETYPE_FOR_DESIGNATION, query = MessageType.GET_MESSAGETYPE_FOR_DESIGNATION_QUERY),
        @NamedQuery(name = MessageType.GET_REPLACEMENT_AFOSIDS, query = MessageType.GET_REPLACEMENT_AFOSIDS_QUERY) })
@Entity
@DynamicSerialize
@Table(name = "message_type", schema = "bmh")
@SequenceGenerator(initialValue = 1, schema = "bmh", name = MessageType.GEN, sequenceName = "message_type_seq")
@BatchSize(size = 100)
public class MessageType {
    public enum Designation {
        StationID, Forecast, Observation, Outlook, Watch, Warning, Advisory, TimeAnnouncement, Other
    }

    static final String GEN = "Message Type Generator";

    public static final String GET_MESSAGETYPE_AFOSID_TITLE = "getMessageTypeAfosTitle";

    protected static final String GET_MESSAGETYPE_AFOSID_TITLE_QUERY = "select afosid, title FROM MessageType m";

    public static final String GET_MESSAGETYPE_AFOSID_DESIGNATION = "getMessageTypeAfosDesignation";

    protected static final String GET_MESSAGETYPE_AFOSID_DESIGNATION_QUERY = "select afosid, designation FROM MessageType m WHERE m.designation = :designation";

    public static final String GET_MESSAGETYPE_FOR_AFOSID = "getMessageTypeForAfosId";

    protected static final String GET_MESSAGETYPE_FOR_AFOSID_QUERY = "FROM MessageType m WHERE m.afosid = :afosid";

    public static final String GET_MESSAGETYPE_FOR_DESIGNATION = "getMessageTypeForDesignation";

    protected static final String GET_MESSAGETYPE_FOR_DESIGNATION_QUERY = "FROM MessageType m WHERE m.designation = :designation";

    public static final String GET_MESSAGETYPE_FOR_EMERGENCYOVERRIDE = "getMessageTypeForEmergencyOverride";

    protected static final String GET_MESSAGETYPE_FOR_EMERGENCYOVERRIDE_QUERY = "FROM MessageType m WHERE m.emergencyOverride = :emergencyOverride";

    public static final String GET_REPLACEMENT_AFOSIDS = "getReplacementAfosids";

    protected static final String GET_REPLACEMENT_AFOSIDS_QUERY = "SELECT mr.replaceMsgType FROM MessageType mt inner join mt.replacementMsgs mr WHERE mt.afosid = ?";

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @DynamicSerializeElement
    private Designation designation;

    @Column(length = 8, nullable = false)
    @DynamicSerializeElement
    private String duration;

    @Column(length = 8, nullable = false)
    @DynamicSerializeElement
    private String periodicity;

    @ManyToOne(optional = false)
    @JoinColumn(name = "voice")
    @DynamicSerializeElement
    private TtsVoice voice;

    @Column(nullable = false)
    @DynamicSerializeElement
    private boolean wxr = true;

    @Column(nullable = false)
    @DynamicSerializeElement
    private boolean toneBlackoutEnabled;

    @Column(length = 6)
    @DynamicSerializeElement
    private String toneBlackOutStart;

    @Column(length = 6)
    @DynamicSerializeElement
    private String toneBlackOutEnd;

    @ManyToMany(fetch = FetchType.EAGER)
    @MapKey(name = "mnemonic")
    @JoinTable(name = "message_same_tx", schema = "bmh", joinColumns = @JoinColumn(name = "afosid"), inverseJoinColumns = @JoinColumn(name = "mnemonic"))
    @Fetch(FetchMode.SUBSELECT)
    @DynamicSerializeElement
    private Set<Transmitter> sameTransmitters;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @Fetch(FetchMode.SUBSELECT)
    @DynamicSerializeElement
    @ForeignKey(name = "message_type_to_replace_delete_me")
    @JoinColumn(name = "msgtype_id")
    private Set<MessageTypeReplacement> replacementMsgs;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "message_default_areas", schema = "bmh", joinColumns = @JoinColumn(name = "afosid"), inverseJoinColumns = @JoinColumn(name = "areacode"))
    @Fetch(FetchMode.SUBSELECT)
    @DynamicSerializeElement
    private Set<Area> defaultAreas;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "message_default_zones", schema = "bmh", joinColumns = @JoinColumn(name = "afosid"), inverseJoinColumns = @JoinColumn(name = "zonecode"))
    @Fetch(FetchMode.SUBSELECT)
    @DynamicSerializeElement
    private Set<Zone> defaultZones;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "message_default_transmitters", schema = "bmh", joinColumns = @JoinColumn(name = "afosid"), inverseJoinColumns = @JoinColumn(name = "mnemonic"))
    @Fetch(FetchMode.SUBSELECT)
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

    public boolean isWxr() {
        return wxr;
    }

    public void setWxr(boolean wxr) {
        this.wxr = wxr;
    }

    public boolean isToneBlackoutEnabled() {
        return toneBlackoutEnabled;
    }

    public void setToneBlackoutEnabled(boolean toneBlackoutEnabled) {
        this.toneBlackoutEnabled = toneBlackoutEnabled;
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
        // TODO return empty set and fix classes that test for null.
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

    public Set<MessageTypeReplacement> getReplacementMsgs() {
        if (replacementMsgs == null) {
            replacementMsgs = new HashSet<>();
        }
        return replacementMsgs;
    }

    /**
     * @param replacementMsgs
     *            the replacementMsgs to set
     */
    public void setReplacementMsgs(Set<MessageTypeReplacement> replacementMsgs) {
        this.replacementMsgs = replacementMsgs;

        if (replacementMsgs != null) {
            for (MessageTypeReplacement mtr : replacementMsgs) {
                mtr.setMsgType(this.getSummary());
            }
        }
    }

    public void addReplacementMsg(MessageTypeReplacement replaceMsg) {
        if (replaceMsg != null) {
            if (replacementMsgs == null) {
                replacementMsgs = new HashSet<>();
            }

            replaceMsg.setMsgType(this.getSummary());
            replacementMsgs.add(replaceMsg);
        }
    }

    public Set<Area> getDefaultAreas() {
        if (defaultAreas == null) {
            defaultAreas = new HashSet<>();
        }
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
        if (defaultZones == null) {
            defaultZones = new HashSet<>();
        }
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
        if (defaultTransmitterGroups == null) {
            defaultTransmitterGroups = new HashSet<>();
        }
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

    /**
     * Returns a summary view of this object. Note changes to the summary object
     * will be reflected in this object and vice versa.
     * 
     * @return
     */
    public MessageTypeSummary getSummary() {
        return new MessageTypeSummary(this);
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

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("MessageType [");
        sb.append("id=").append(id);
        sb.append(", afosid=").append(afosid);
        sb.append(", title=\"").append(title);
        sb.append("\", alert=").append(alert);
        sb.append(", confirm=").append(confirm);
        sb.append(", emergencyOverride=").append(emergencyOverride);
        sb.append(", interrupt=").append(interrupt);
        sb.append(", designation=");
        if (designation == null) {
            sb.append("None");
        } else {
            sb.append(designation.name());
        }

        sb.append(", duration=");
        sb.append(nullCheck(duration));

        sb.append(", periodicity=");
        sb.append(nullCheck(periodicity));

        sb.append(", voice=");
        if (voice == null) {
            sb.append("None");
        } else {
            sb.append(voice.getVoiceName());
        }

        sb.append(", wxr=").append(wxr);
        sb.append(", toneBlackoutEnabled=").append(toneBlackoutEnabled);

        sb.append(", toneBlackOutStart=");
        sb.append(nullCheck(toneBlackOutStart));

        sb.append(", toneBlackOutEnd=");
        sb.append(nullCheck(toneBlackOutEnd));

        sb.append(", sameTransmitters=[");
        if ((sameTransmitters != null) && (sameTransmitters.size() > 0)) {
            for (Transmitter transmitter : sameTransmitters) {
                sb.append(transmitter.getMnemonic()).append(", ");
            }
            sb.setLength(sb.length() - 2);
        }

        sb.append("], replacementMsgs=[");
        if (getReplacementMsgs().size() > 0) {
            for (MessageTypeReplacement mtr : getReplacementMsgs()) {
                sb.append(mtr.getMsgType().getAfosid()).append(", ");
            }
            sb.setLength(sb.length() - 2);
        }

        sb.append("], defaultAreas=[");
        if (getDefaultAreas().size() > 0) {
            for (Area area : getDefaultAreas()) {
                sb.append(area.getAreaName()).append(", ");
            }
            sb.setLength(sb.length() - 2);
        }

        sb.append("], defaultZones=[");
        if (getDefaultZones().size() > 0) {
            for (Zone zone : getDefaultZones()) {
                sb.append(zone.getZoneName()).append(", ");
            }
            sb.setLength(sb.length() - 2);
        }

        sb.append("], defaultTransmitterGroups=[");
        if (getDefaultTransmitterGroups().size() > 0) {
            for (TransmitterGroup group : getDefaultTransmitterGroups()) {
                sb.append(group.getName()).append(", ");
            }
            sb.setLength(sb.length() - 2);
        }
        sb.append("]]");

        return sb.toString();
    }

    private String nullCheck(String value) {
        return BMHLoggerUtils.nullCheck(value, "None");
    }

    /**
     * Get log entry.
     * 
     * @param oldType
     * @param user
     *            - Who is making the change
     * @return entry - empty string when no differences.
     */
    public String logEntry(MessageType oldType, String user) {
        StringBuilder sb = new StringBuilder();
        sb.append("User ").append(user);
        if (oldType == null) {
            sb.append(" New MessageType afoisId/title/id: ")
                    .append(getAfosid()).append("/").append(getTitle())
                    .append("/").append(getId()).append(" ").append(toString());
        } else {
            Object oldValue = null;
            Object newValue = null;
            boolean logChanges = false;
            sb.append(" Update fields Message Type afoisId/title/id: ")
                    .append(getAfosid()).append("/").append(getTitle())
                    .append("/").append(getId()).append(" [");
            if (!getTitle().equals(oldType.getTitle())) {
                BMHLoggerUtils.logFieldChange(sb, "title", oldType.getTitle(),
                        getTitle());
                logChanges = true;
            }
            if (!getAfosid().equals(oldType.getAfosid())) {
                BMHLoggerUtils.logFieldChange(sb, "Afosid",
                        oldType.getAfosid(), getAfosid());
                logChanges = true;
            }
            if (isAlert() != oldType.isAlert()) {
                BMHLoggerUtils.logFieldChange(sb, "alert", oldType.isAlert(),
                        isAlert());
                logChanges = true;
            }
            if (isConfirm() != oldType.isConfirm()) {
                BMHLoggerUtils.logFieldChange(sb, "confirm",
                        oldType.isConfirm(), isConfirm());
                logChanges = true;
            }
            if (isEmergencyOverride() != oldType.isEmergencyOverride()) {
                BMHLoggerUtils.logFieldChange(sb, "Emergency Override",
                        oldType.isEmergencyOverride(), isEmergencyOverride());
                logChanges = true;
            }
            if (isInterrupt() != oldType.isInterrupt()) {
                BMHLoggerUtils.logFieldChange(sb, "interrupt",
                        oldType.isInterrupt(), isInterrupt());
                logChanges = true;
            }
            if (getDesignation() != oldType.getDesignation()) {
                BMHLoggerUtils.logFieldChange(sb, "designation",
                        oldType.getDesignation(), getDesignation());
                logChanges = true;
            }
            if (!getDuration().equals(oldType.getDuration())) {
                BMHLoggerUtils.logFieldChange(sb, "duration",
                        oldType.getDuration(), getDuration());
                logChanges = true;
            }
            if (!getPeriodicity().equals(oldType.getPeriodicity())) {
                BMHLoggerUtils.logFieldChange(sb, "periodicity",
                        oldType.getPeriodicity(), getPeriodicity());
                logChanges = true;
            }

            oldValue = oldType.getVoice();
            if (oldValue == null) {
                oldValue = "None";
            } else {
                oldValue = oldType.getVoice().getVoiceName();
            }
            newValue = getVoice();
            if (newValue == null) {
                newValue = "None";
            } else {
                newValue = getVoice().getVoiceName();
            }
            if (!newValue.equals(oldValue)) {
                BMHLoggerUtils.logFieldChange(sb, "voice", oldValue, newValue);
                logChanges = true;
            }
            if (isWxr() != oldType.isWxr()) {
                BMHLoggerUtils.logFieldChange(sb, "wxr", oldType.isWxr(),
                        isWxr());
                logChanges = true;
            }
            if (isToneBlackoutEnabled() != oldType.isToneBlackoutEnabled()) {
                BMHLoggerUtils.logFieldChange(sb, "toneBlackoutEnabled",
                        oldType.isToneBlackoutEnabled(),
                        isToneBlackoutEnabled());
                logChanges = true;
            }

            oldValue = oldType.getToneBlackOutStart();
            if (oldValue == null) {
                oldValue = "None";
            }
            newValue = getToneBlackOutStart();
            if (newValue == null) {
                newValue = "None";
            }
            if (!newValue.equals(oldValue)) {
                BMHLoggerUtils.logFieldChange(sb, "toneBlackOutStart",
                        oldValue, newValue);
                logChanges = true;
            }

            oldValue = oldType.getToneBlackOutEnd();
            if (oldValue == null) {
                oldValue = "None";
            }
            newValue = getToneBlackOutEnd();
            if (newValue == null) {
                newValue = "None";
            }

            if (!newValue.equals(oldValue)) {
                BMHLoggerUtils.logFieldChange(sb, "toneBlackOutEnd", oldValue,
                        newValue);
                logChanges = true;
            }

            // Add Set differences.

            StringBuilder setSb = new StringBuilder();
            if (BMHLoggerUtils.setsDiffer(oldType.getSameTransmitters(),
                    getSameTransmitters())) {
                Set<Transmitter> oldTransmitters = oldType
                        .getSameTransmitters();
                if (oldTransmitters == null) {
                    oldTransmitters = new HashSet<>();
                }
                Set<Transmitter> newTransmitters = getSameTransmitters();
                if (newTransmitters == null) {
                    newTransmitters = new HashSet<>();
                }
                setSb.setLength(0);
                setSb.append("-[");
                if (oldTransmitters.size() > 0) {
                    for (Transmitter t : oldTransmitters) {
                        if (!newTransmitters.contains(t)) {
                            setSb.append(t.getMnemonic()).append(", ");
                        }
                    }
                    if (setSb.length() > 2) {
                        setSb.setLength(setSb.length() - 2);
                    }
                }
                setSb.append("]");
                oldValue = setSb.toString();
                setSb.setLength(0);
                setSb.append("+[");

                if (newTransmitters.size() > 0) {
                    for (Transmitter t : newTransmitters) {
                        if (!oldTransmitters.contains(t)) {
                            setSb.append(t.getMnemonic()).append(", ");
                        }
                    }
                    if (setSb.length() > 2) {
                        setSb.setLength(setSb.length() - 2);
                    }
                }
                setSb.append("]");
                newValue = setSb.toString();
                BMHLoggerUtils.logFieldChange(sb, "sameTransmitters", oldValue,
                        newValue);
                logChanges = true;
            }

            if (BMHLoggerUtils.setsDiffer(oldType.getReplacementMsgs(),
                    getReplacementMsgs())) {
                setSb.setLength(0);
                setSb.append("-[");
                Set<MessageTypeReplacement> oldMtrSet = oldType
                        .getReplacementMsgs();
                Set<MessageTypeReplacement> newMtrSet = getReplacementMsgs();
                if (oldMtrSet.size() > 0) {
                    for (MessageTypeReplacement mtr : oldMtrSet) {
                        if (!newMtrSet.contains(mtr)) {
                            setSb.append(mtr.getReplaceMsgType().getAfosid())
                                    .append(", ");
                        }
                    }
                    setSb.setLength(setSb.length() - 2);
                }
                setSb.append("]");
                oldValue = setSb.toString();
                setSb.setLength(0);
                setSb.append("+[");
                if (newMtrSet.size() > 0) {
                    for (MessageTypeReplacement mtr : newMtrSet) {
                        if (!oldMtrSet.contains(mtr)) {
                            setSb.append(mtr.getReplaceMsgType().getAfosid())
                                    .append(", ");
                        }
                    }
                    if (setSb.length() > 2) {
                        setSb.setLength(setSb.length() - 2);
                    }
                }
                setSb.append("]");
                newValue = setSb.toString();
                BMHLoggerUtils.logFieldChange(sb, "replacementMsgs", oldValue,
                        newValue);
                logChanges = true;
            }

            if (BMHLoggerUtils.setsDiffer(oldType.getDefaultAreas(),
                    getDefaultAreas())) {
                setSb.setLength(0);
                setSb.append("-[");

                Set<Area> oldAreaSet = oldType.getDefaultAreas();
                Set<Area> newAreaSet = getDefaultAreas();
                if (oldAreaSet.size() > 0) {
                    for (Area area : oldAreaSet) {
                        if (!newAreaSet.contains(area)) {
                            setSb.append(area.getAreaName()).append(", ");
                        }
                    }
                    if (setSb.length() > 2) {
                        setSb.setLength(setSb.length() - 2);
                    }
                }
                setSb.append("]");
                oldValue = setSb.toString();
                setSb.setLength(0);
                setSb.append("+[");
                if (newAreaSet.size() > 0) {
                    for (Area area : newAreaSet) {
                        if (!oldAreaSet.contains(area)) {
                            setSb.append(area.getAreaName()).append(", ");
                        }
                    }
                    if (setSb.length() > 2) {
                        setSb.setLength(setSb.length() - 2);
                    }
                }
                setSb.append("]");
                newValue = setSb.toString();
                BMHLoggerUtils.logFieldChange(sb, "defaultAreas", oldValue,
                        newValue);
                logChanges = true;
            }

            if (BMHLoggerUtils.setsDiffer(oldType.getDefaultZones(),
                    getDefaultZones())) {
                setSb.setLength(0);
                setSb.append("-[");
                Set<Zone> oldZones = oldType.getDefaultZones();
                Set<Zone> newZones = getDefaultZones();
                if (oldZones.size() > 0) {
                    for (Zone zone : oldZones) {
                        if (!newZones.contains(zone)) {
                            setSb.append(zone.getZoneName()).append(", ");
                        }
                    }
                    if (setSb.length() > 2) {
                        setSb.setLength(setSb.length() - 2);
                    }
                }
                setSb.append("]");
                oldValue = setSb.toString();
                setSb.setLength(0);
                setSb.append("+[");
                if (newZones.size() > 0) {
                    for (Zone zone : newZones) {
                        if (!oldZones.contains(zone)) {
                            setSb.append(zone.getZoneName()).append(", ");
                        }
                    }
                    if (setSb.length() > 2) {
                        setSb.setLength(setSb.length() - 2);
                    }
                }
                setSb.append("]");
                newValue = setSb.toString();
                BMHLoggerUtils.logFieldChange(sb, "defaultZones", oldValue,
                        newValue);
                logChanges = true;
            }

            if (BMHLoggerUtils.setsDiffer(
                    oldType.getDefaultTransmitterGroups(),
                    getDefaultTransmitterGroups())) {
                setSb.setLength(0);
                setSb.append("-[");
                Set<TransmitterGroup> oldGroups = oldType
                        .getDefaultTransmitterGroups();
                Set<TransmitterGroup> newGroups = getDefaultTransmitterGroups();
                if (oldGroups.size() > 0) {
                    for (TransmitterGroup group : oldGroups) {
                        if (!newGroups.contains(group)) {
                            setSb.append(group.getName()).append(", ");
                        }
                    }
                    if (setSb.length() > 2) {
                        setSb.setLength(setSb.length() - 2);
                    }
                }
                setSb.append("]");
                oldValue = setSb.toString();
                setSb.setLength(0);
                setSb.append("+[");
                if (newGroups.size() > 0) {
                    for (TransmitterGroup group : newGroups) {
                        if (!oldGroups.contains(group)) {
                            setSb.append(group.getName()).append(", ");
                        }
                    }
                    if (setSb.length() > 2) {
                        setSb.setLength(setSb.length() - 2);
                    }
                }
                setSb.append("]");
                newValue = setSb.toString();

                BMHLoggerUtils.logFieldChange(sb, "defaultTransmitterGroups",
                        oldValue, newValue);
                logChanges = true;
            }

            // No changes made
            if (!logChanges) {
                return "";
            }

            sb.setCharAt(sb.length() - 2, ']');
        }
        return sb.toString();
    }
}
