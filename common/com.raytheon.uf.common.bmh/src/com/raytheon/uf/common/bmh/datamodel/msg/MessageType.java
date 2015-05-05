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
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.ForeignKey;

import com.raytheon.uf.common.bmh.datamodel.language.TtsVoice;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Area;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Zone;
import com.raytheon.uf.common.bmh.diff.DiffTitle;
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
 * Nov 13, 2014  3717      bsteffen    Add staticDesignation field to Designation
 * Nov 18, 2014  3746      rjpeter     Refactored MessageTypeReplacement.
 * Feb 05, 2015  4085      bkowal      Designations are no longer static.
 * Feb 23, 2015  4140      rjpeter     Renamed foreign constraints.
 * Mar 25, 2015  4290      bsteffen    Switch to global replacement.
 * May 05, 2015  4463      bkowal      Added {@link #originator}.
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
        @NamedQuery(name = MessageType.GET_REPLACEMENT_AFOSIDS, query = MessageType.GET_REPLACEMENT_AFOSIDS_QUERY),
        @NamedQuery(name = MessageType.GET_REVERSE_REPLACEMENT_AFOSIDS, query = MessageType.GET_REVERSE_REPLACEMENT_AFOSIDS_QUERY) })
@Entity
@DynamicSerialize
@Table(name = "message_type", schema = "bmh")
@SequenceGenerator(initialValue = 1, schema = "bmh", name = MessageType.GEN, sequenceName = "message_type_seq")
@BatchSize(size = 100)
public class MessageType {
    public enum Designation {

        StationID, Forecast, Observation, Outlook, Watch, Warning, Advisory, TimeAnnouncement, Other;

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

    protected static final String GET_REPLACEMENT_AFOSIDS_QUERY = "SELECT mt.replacementMsgs FROM MessageType mt WHERE mt.afosid = :afosid";

    public static final String GET_REVERSE_REPLACEMENT_AFOSIDS = "getReverseReplacementAfosids";

    protected static final String GET_REVERSE_REPLACEMENT_AFOSIDS_QUERY = "Select m FROM MessageType m, MessageTypeSummary s WHERE s.afosid = :afosid and s in elements(m.replacementMsgs)";

    // use surrogate key
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = GEN)
    @DynamicSerializeElement
    @DiffTitle(position = 3)
    protected int id;

    @Column(length = 9, unique = true)
    @DynamicSerializeElement
    @DiffTitle(position = 1)
    private String afosid;

    @Column(length = 40, nullable = false)
    @DynamicSerializeElement
    @DiffTitle(position = 2)
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

    @Column(nullable = false, length = 3)
    @DynamicSerializeElement
    private String originator;

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
    @JoinTable(name = "message_same_tx", schema = "bmh", joinColumns = @JoinColumn(name = "msgtype_id"), inverseJoinColumns = @JoinColumn(name = "transmitter_id"))
    @ForeignKey(name = "fk_msg_same_tx_to_msg_type", inverseName = "fk_msg_same_tx_to_tx")
    @Fetch(FetchMode.SUBSELECT)
    @DynamicSerializeElement
    private Set<Transmitter> sameTransmitters;

    @ManyToMany(fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @DynamicSerializeElement
    @JoinTable(name = "message_replace", schema = "bmh", joinColumns = @JoinColumn(name = "msgtype_id"), inverseJoinColumns = @JoinColumn(name = "msgtype_replace_id"))
    @ForeignKey(name = "fk_msg_replace_to_msg_type", inverseName = "fk_msg_replace_to_replaced_msgs")
    private Set<MessageTypeSummary> replacementMsgs;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "message_default_areas", schema = "bmh", joinColumns = @JoinColumn(name = "msgtype_id"), inverseJoinColumns = @JoinColumn(name = "area_id"))
    @ForeignKey(name = "fk_msg_def_areas_to_msg_type", inverseName = "fk_msg_def_areas_to_area")
    @Fetch(FetchMode.SUBSELECT)
    @DynamicSerializeElement
    private Set<Area> defaultAreas;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "message_default_zones", schema = "bmh", joinColumns = @JoinColumn(name = "msgtype_id"), inverseJoinColumns = @JoinColumn(name = "zone_id"))
    @ForeignKey(name = "fk_msg_def_zones_to_msg_type", inverseName = "fk_msg_def_zones_to_zone")
    @Fetch(FetchMode.SUBSELECT)
    @DynamicSerializeElement
    private Set<Zone> defaultZones;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "message_default_transmitter_groups", schema = "bmh", joinColumns = @JoinColumn(name = "msgtype_id"), inverseJoinColumns = @JoinColumn(name = "transmitter_group_id"))
    @ForeignKey(name = "fk_msg_def_tx_to_msg_type", inverseName = "fk_msg_def_tx_to_tx_group")
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

    /**
     * @return the originator
     */
    public String getOriginator() {
        return originator;
    }

    /**
     * @param originator
     *            the originator to set
     */
    public void setOriginator(String originator) {
        this.originator = originator;
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

    public Set<MessageTypeSummary> getReplacementMsgs() {
        if (replacementMsgs == null) {
            replacementMsgs = new HashSet<>();
        }
        return replacementMsgs;
    }

    /**
     * @param replacementMsgs
     *            the replacementMsgs to set
     */
    public void setReplacementMsgs(Set<MessageTypeSummary> replacementMsgs) {
        this.replacementMsgs = replacementMsgs;
    }

    public void addReplacementMsg(MessageTypeSummary replaceMsg) {
        if (replaceMsg != null) {
            if (replacementMsgs == null) {
                replacementMsgs = new HashSet<>();
            }

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
        sb.append(duration);

        sb.append(", periodicity=");
        sb.append(periodicity);

        sb.append(", voice=");
        if (voice == null) {
            sb.append("None");
        } else {
            sb.append(voice.getVoiceName());
        }

        sb.append(", originator=").append(originator);
        sb.append(", toneBlackoutEnabled=").append(toneBlackoutEnabled);

        sb.append(", toneBlackOutStart=");
        sb.append(toneBlackOutStart);

        sb.append(", toneBlackOutEnd=");
        sb.append(toneBlackOutEnd);

        sb.append(", sameTransmitters=[");
        if ((sameTransmitters != null) && (sameTransmitters.size() > 0)) {
            for (Transmitter transmitter : sameTransmitters) {
                sb.append(transmitter.getMnemonic()).append(", ");
            }
            sb.setLength(sb.length() - 2);
        }

        sb.append("], replacementMsgs=[");
        if (getReplacementMsgs().size() > 0) {
            for (MessageTypeSummary mtr : getReplacementMsgs()) {
                sb.append(mtr.getAfosid()).append(", ");
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
}
