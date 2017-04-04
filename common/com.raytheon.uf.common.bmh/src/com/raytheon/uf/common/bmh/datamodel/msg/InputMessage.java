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

import java.io.File;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

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
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;

import com.raytheon.uf.common.bmh.datamodel.language.Language;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Area;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.time.util.TimeUtil;

/**
 * 
 * Contains the parsed message data exactly as it was received from NWRWAVES.
 * This is the starting point of messages within BMH.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Jun 16, 2014  3283     bsteffen    Initial creation
 * Aug 14, 2014  3432     mpduff      Added isPeriodic method
 * Sep 4, 2014   3568     bkowal      Added fields to differentiate between
 *                                    generated static messages and ingested messages.
 * Sep 09, 2014  2585     bsteffen    Implement MAT
 * Sep 25, 2014  3620     bsteffen    Add seconds to periodicity.
 * Oct 15, 2014  3728     lvenable    Added name column.
 * Oct 18, 2014  3728     lvenable    Added query to retrieve the Id, Name, Afos Id, and
 *                                    creation time.
 * Nov 03, 2014  3790     lvenable    Update query to include the active field.
 * Nov 03, 2014  3728     lvenable    Made active default to true when creating a new object.
 * Nov 17, 2014  3793     bsteffen    Add same transmitters.
 * Nov 26, 2014  3613     bsteffen    Add getPurgableInputMessages
 * Dec 11, 2014  3905     lvenable    Added a method to return a set of area codes.
 * Jan 02, 2014  3833     lvenable    Added query to get unexpired messages.
 * Jan 12, 2015  3843     bsteffen    Return empty list when areas is null.
 * Jan 20, 2015  4010     bkowal      Added {@link #selectedTransmitters}.
 * Feb 09, 2015  4094     bsteffen    Update areaCodes to be length of 4096.
 * Feb 10, 2015  4104     bkowal      Trim any area codes beyond the maximum length
 * Fev 23, 2015  4140     rjpeter     Renamed foreign constraint.
 * Mar 05, 2015  4222     bkowal      Include messages that never expire when retrieving
 *                                    unexpired messages.
 * Mar 19, 2015  4282     rferrel     Added missing elements to clone constructor,
 *                                     equal, hashCode and toString methods. Clone all
 *                                     Calendar elements.
 * Mar 25, 2015  4290     bsteffen    Switch to global replacement.
 * Apr 16, 2015  4395     rferrel     Added {@link #ALL_UNEXPIRED_QUERY}.
 * Apr 21, 2015  4397     bkowal      Added {@link #lastUpdateTime}.
 * May 11, 2015  4476     bkowal      Added {@link #ALL_WITH_NAME_AND_AFOSID_QUERY}.
 * May 12, 2015  4248     rjpeter     Remove bmh schema, standardize foreign/unique keys.
 * May 19, 2015  4483     bkowal      Updated {@link #DUP_QUERY} to match effective and
 *                                    expiration times and removed mrd comparison.
 * Jul 29, 2015  4690     rjpeter     Added originalFile.
 * Nov 16, 2015  5127     rjpeter     Added insertTime and a getActiveInputMessagesWithAfosidAndAreaCodesAndNoMrd
 * Jul 29, 2016 5766      bkowal      Added {@link #cycles}.
 * Aug 04, 2016  5766     bkowal      Include {@link #cycles} in {@link #hashCode()} and {@link #equals(Object)}.
 * Jan 19, 2017  6078     bkowal      Updated {@link #GET_INPUT_MSGS_ID_NAME_AFOS_CREATION} and
 *                                    {@link #UNEXPIRED_QUERY_NAME} to retrieve the origin column.
 * Feb 24, 2017  6030     bkowal      Added {@link #MESSAGE_NAME_LENGTH}.
 * </pre>
 * 
 * @author bsteffen
 */
@NamedQueries({
        @NamedQuery(name = InputMessage.DUP_QUERY_NAME, query = InputMessage.DUP_QUERY),
        @NamedQuery(name = InputMessage.GET_INPUT_MSGS_ID_NAME_AFOS_CREATION, query = InputMessage.GET_INPUT_MSGS_ID_NAME_AFOS_CREATION_QUERY),
        @NamedQuery(name = InputMessage.PURGE_QUERY_NAME, query = InputMessage.PURGE_QUERY),
        @NamedQuery(name = InputMessage.UNEXPIRED_QUERY_NAME, query = InputMessage.UNEXPIRED_QUERY),
        @NamedQuery(name = InputMessage.ACTIVE_WITH_AFOSID_AND_AREACODES_QUERY_NAME, query = InputMessage.ACTIVE_WITH_AFOSID_AND_AREACODES_QUERY),
        @NamedQuery(name = InputMessage.ACTIVE_WITH_AFOSID_AND_AREACODES_AND_NO_MRD_QUERY_NAME, query = InputMessage.ACTIVE_WITH_AFOSID_AND_AREACODES_AND_NO_MRD_QUERY),
        @NamedQuery(name = InputMessage.ACTIVE_WITH_MRD_LIKE_QUERY_NAME, query = InputMessage.ACTIVE_WITH_MRD_LIKE_QUERY),
        @NamedQuery(name = InputMessage.ALL_UNEXPIRED_QUERY_NAME, query = InputMessage.ALL_UNEXPIRED_QUERY),
        @NamedQuery(name = InputMessage.ALL_WITH_NAME_AND_AFOSID, query = InputMessage.ALL_WITH_NAME_AND_AFOSID_QUERY) })
@Entity
@DynamicSerialize
@Table(name = "input_msg")
@SequenceGenerator(initialValue = 1, name = InputMessage.GEN, sequenceName = "input_msg_seq")
public class InputMessage {

    public static enum ReplacementType {
        MAT, MRD;
    }

    public static enum Origin {
        UNKNOWN, EXTERNAL, WXMSG, DMOMSG, EOMSG;
    }

    protected static final String GEN = "Input Messsage Id Generator";

    public static final String GET_INPUT_MSGS_ID_NAME_AFOS_CREATION = "getInputMsgIdNameAfosCreation";

    protected static final String GET_INPUT_MSGS_ID_NAME_AFOS_CREATION_QUERY = "select id, name, afosid, creationTime, active, origin FROM InputMessage im";

    /**
     * Named query to pull all messages with a matching afosid and with a valid
     * time range encompassing a specified time range.
     */
    public static final String DUP_QUERY_NAME = "getDuplicateInputMessages";

    protected static final String DUP_QUERY = "FROM InputMessage m WHERE m.id != :id AND m.afosid = :afosid AND (m.effectiveTime = :effectiveTime AND m.expirationTime = :expirationTime)";

    /**
     * Named query to delete all old messages
     */
    public static final String PURGE_QUERY_NAME = "getPurgableInputMessages";

    protected static final String PURGE_QUERY = "FROM InputMessage m WHERE m.expirationTime < :purgeTime OR (m.active=false and m.creationTime < :purgeTime))";

    /**
     * Named query to retrieve message that have not expired.
     */
    public static final String UNEXPIRED_QUERY_NAME = "getNonExpiredMessages";

    protected static final String UNEXPIRED_QUERY = "select id, name, afosid, creationTime, active, origin FROM InputMessage m WHERE m.expirationTime >= :currentTime OR (m.expirationTime is null AND m.validHeader = true)";

    /**
     * Named query to retrieve messages that have a specific afosid and
     * areacodes and are active and have not yet expired.
     */
    public static final String ACTIVE_WITH_AFOSID_AND_AREACODES_QUERY_NAME = "getActiveInputMessagesWithAfosidAndAreaCodes";

    protected static final String ACTIVE_WITH_AFOSID_AND_AREACODES_QUERY = "FROM InputMessage m WHERE m.afosid = :afosid and m.areaCodes = :areaCodes and m.expirationTime >= :expireAfter and m.active = true and m.language = :language";

    /**
     * Named query to retrieve messages that have a specific afosid and
     * areacodes and are active and have not yet expired and have no mrd.
     */
    public static final String ACTIVE_WITH_AFOSID_AND_AREACODES_AND_NO_MRD_QUERY_NAME = "getActiveInputMessagesWithAfosidAndAreaCodesAndNoMrd";

    protected static final String ACTIVE_WITH_AFOSID_AND_AREACODES_AND_NO_MRD_QUERY = "FROM InputMessage m WHERE m.afosid = :afosid and m.areaCodes = :areaCodes and m.mrd IS NULL and m.expirationTime >= :expireAfter and m.active = true and m.language = :language";

    public static final String ALL_UNEXPIRED_QUERY_NAME = "getALLNonExpiredMessages";

    protected static final String ALL_UNEXPIRED_QUERY = "FROM InputMessage m where m.expirationTime IS NULL or m.expirationTime >= :currentTime";

    /**
     * Named query to retrieve messages that have an mrd matching a like
     * expression and are active and have not yet expired.
     */
    public static final String ACTIVE_WITH_MRD_LIKE_QUERY_NAME = "getActiveInputMessagesWithMrdLike";

    protected static final String ACTIVE_WITH_MRD_LIKE_QUERY = "FROM InputMessage m WHERE m.mrd LIKE :mrdLike and m.expirationTime >= :expireAfter and m.active = true and m.language = :language";

    /**
     * Used to retrieve all {@link InputMessage}s with the specified Afos Id and
     * Name. Primarily only useful for static messages.
     */
    public static final String ALL_WITH_NAME_AND_AFOSID = "getAllWithNameAndAfosId";

    protected static final String ALL_WITH_NAME_AND_AFOSID_QUERY = "FROM InputMessage m WHERE m.afosid = :afosid AND m.name = :name";

    public static final int MESSAGE_NAME_LENGTH = 40;

    private static final int AREA_CODE_LENGTH = 4096;

    private static final int ORIGIN_LENGTH = 8;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = GEN)
    @DynamicSerializeElement
    private int id;

    /**
     * Name for the input message.
     */
    @Column(length = MESSAGE_NAME_LENGTH, nullable = false)
    @DynamicSerializeElement
    private String name = "";

    @Column(length = Language.LENGTH)
    @Enumerated(EnumType.STRING)
    @DynamicSerializeElement
    private Language language;

    /** AFOS product identifier of the form CCNNNXXX **/
    @Column(length = 9)
    @DynamicSerializeElement
    private String afosid;

    /** The Date/Time that the product was created in AFOS. **/
    @Column
    @DynamicSerializeElement
    private Calendar creationTime;

    /** The Date/Time after which the message may be output. **/
    @Column
    @DynamicSerializeElement
    private Calendar effectiveTime;

    /**
     * The periodicity of message transmission for messages to be scheduled
     * based on time. This is stored in String representation with the format
     * 'DDHHMMSS'. This field will contain null for non-time-inserted messages.
     * 
     * @see InputMessage#getPeriodicityMinutes()
     **/
    @Column(length = 8)
    @DynamicSerializeElement
    private String periodicity;

    @Column(nullable = true)
    @DynamicSerializeElement
    private Integer cycles;

    /**
     * This is an optional field containing three pieces of information. A
     * unique identifier, a list of identifiers that this message replaces, and
     * a list of identifiers this message follows. This field is stored unparsed
     * from the raw file although convenience methods are provided that can
     * parse it.
     * 
     * @see #getMrdId()
     * @see #getMrdReplacements()
     * @see #getMrdFollows()
     */
    @Column
    @DynamicSerializeElement
    private String mrd;

    /** This field is used by AFOS to direct messages into inactive storage . */
    @Column
    @DynamicSerializeElement
    private Boolean active = true;

    /**
     * This field is used to display a confirmation that this message was
     * transmitted.
     */
    @Column
    @DynamicSerializeElement
    private Boolean confirm = false;

    /**
     * This field is used to interrupt any message currently being broadcast on
     * the applicable transmitters with this message.
     */
    @Column
    @DynamicSerializeElement
    private Boolean interrupt = false;

    /**
     * This field is used to indicate an alert tone should be broadcast prior to
     * broadcast of this message for the first time.
     */
    @Column
    @DynamicSerializeElement
    private Boolean alertTone = false;

    /** Indicate an NWRSAME tone for this message. */
    @Column
    @DynamicSerializeElement
    private Boolean nwrsameTone = null;

    /**
     * The transmitters for which NWRSAME tones should be played. This field
     * should be ignored if nwrsameTone is not true. When parsing messages this
     * information is not included in the header, it is derived from the current
     * system configuration.
     */
    @Column
    @DynamicSerializeElement
    private String sameTransmitters;

    /**
     * The notion of a message's LISTENING AREA code is defined as a collection
     * of Universal Generic Codes (UGCs). These codes serve to specify
     * geographical areas to be served by the message's transmission. This field
     * consists of one or more UGCs separated by a dash.
     */
    @Column(length = AREA_CODE_LENGTH)
    @DynamicSerializeElement
    private String areaCodes;

    /**
     * Individual {@link Transmitter}s that have been selected as a broadcast
     * destination. The message will be broadcast to all {@link Area}s
     * associated with the {@link Transmitter}s. This field will primarily be
     * used for user-generated Weather Messages.
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "input_msg_selected_transmitters", joinColumns = @JoinColumn(name = "input_msg_id") , inverseJoinColumns = @JoinColumn(name = "transmitter_id") )
    @ForeignKey(name = "fk_selected_tx_to_input_msg", inverseName = "fk_selected_tx_to_tx")
    @Fetch(FetchMode.SUBSELECT)
    @DynamicSerializeElement
    private Set<Transmitter> selectedTransmitters;

    /**
     * The Date/Time after which the message is ignored for transmission and may
     * be deleted .
     */
    @Column
    @Index(name = "input_msg_expirationtime_idx")
    @DynamicSerializeElement
    private Calendar expirationTime;

    /** The text content of a message. */
    @Column(columnDefinition = "text")
    @DynamicSerializeElement
    private String content;

    /**
     * This field indicates if all other fields were parsed correctly. When
     * false some fields may not be populated and the content will be set to the
     * full file content including the invalid header.
     */
    @Column(nullable = false)
    @DynamicSerializeElement
    private boolean validHeader;

    @Column
    @Enumerated(EnumType.STRING)
    @DynamicSerializeElement
    private ReplacementType replacementType;

    /**
     * The date that this {@link InputMessage} record was last updated. This
     * field has been added just for statistics tracking. This information will
     * be included in the playlist files so that dac transmit will be able to
     * generate the BMH Delivery Time statistic.
     */
    @Column(nullable = false)
    @DynamicSerializeElement
    private Date lastUpdateTime = TimeUtil.newDate();

    /**
     * The date that this {@link InputMessage} record was inserted. This field
     * is auto populated by the database at insert time and cannot be saved back
     * to database.
     */
    @Column(nullable = false, insertable = false, updatable = false, columnDefinition = "timestamp default now()")
    @DynamicSerializeElement
    private Date insertTime;

    @Column(nullable = false, length = ORIGIN_LENGTH)
    @Enumerated(EnumType.STRING)
    @DynamicSerializeElement
    private Origin origin = Origin.UNKNOWN;

    private transient File originalFile;

    public InputMessage() {
        super();
    }

    /**
     * Clone constructor.
     * 
     * @param other
     */
    public InputMessage(InputMessage other) {
        this.id = other.id;
        this.name = other.name;
        this.language = other.language;
        this.afosid = other.afosid;
        this.creationTime = cloneCal(other.creationTime);
        this.effectiveTime = cloneCal(other.effectiveTime);
        this.periodicity = other.periodicity;
        this.cycles = other.cycles;
        this.mrd = other.mrd;
        this.active = other.active;
        this.confirm = other.confirm;
        this.interrupt = other.interrupt;
        this.alertTone = other.alertTone;
        this.nwrsameTone = other.nwrsameTone;
        this.sameTransmitters = other.sameTransmitters;
        this.areaCodes = other.areaCodes;
        this.expirationTime = cloneCal(other.expirationTime);
        this.content = other.content;
        this.validHeader = other.validHeader;
        this.replacementType = other.replacementType;
        if (other.selectedTransmitters != null) {
            this.selectedTransmitters = new HashSet<>(
                    other.selectedTransmitters);
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public String getAfosid() {
        return afosid;
    }

    public void setAfosid(String afosid) {
        this.afosid = afosid;
    }

    public Calendar getCreationTime() {
        return cloneCal(creationTime);
    }

    public void setCreationTime(Calendar creationTime) {
        this.creationTime = cloneCal(creationTime);
    }

    public Calendar getEffectiveTime() {
        return cloneCal(effectiveTime);
    }

    public void setEffectiveTime(Calendar effectiveTime) {
        this.effectiveTime = cloneCal(effectiveTime);
    }

    public String getPeriodicity() {
        return periodicity;
    }

    /**
     * @return the parsed number of seconds represented by the periodicty field
     *         of this message.
     */
    public int getPeriodicitySeconds() {
        if (this.periodicity != null) {
            int days = Integer.parseInt(this.periodicity.substring(0, 2));
            int hours = Integer.parseInt(this.periodicity.substring(2, 4));
            int minutes = Integer.parseInt(this.periodicity.substring(4, 6));
            int seconds = Integer.parseInt(this.periodicity.substring(6, 8));
            return seconds + ((60 * minutes) + (60 * (hours + (24 * days))));
        }
        return -1;
    }

    public void setPeriodicity(String periodicity) {
        this.periodicity = periodicity;
    }

    public Integer getCycles() {
        return cycles;
    }

    public void setCycles(Integer cycles) {
        this.cycles = cycles;
    }

    public String getMrd() {
        return mrd;
    }

    /**
     * @return The parsed unique id for this message(between 0 and 999) from the
     *         mrd, or -1 if no id was included
     */
    public int getMrdId() {
        if (mrd != null) {
            return Integer.parseInt(mrd.substring(0, 3));
        }
        return -1;
    }

    /**
     * @return The parsed unique ids that this message should replace, will
     *         return an empty array if no replacements were specified.
     */
    public int[] getMrdReplacements() {
        if (mrd == null) {
            return new int[0];
        }
        int start = mrd.indexOf('R') + 1;
        if (start == 0) {
            return new int[0];
        }
        int end = mrd.indexOf('F');
        if (end == -1) {
            end = mrd.length();
        }
        int[] result = new int[(end - start) / 3];
        for (int i = 0; i < result.length; i += 1) {
            int s = start + (i * 3);
            result[i] = Integer.parseInt(mrd.substring(s, s + 3));
        }
        return result;
    }

    /**
     * @return The parsed unique ids that this message should follow, will
     *         return an empty array if no follows were specified.
     */
    public int[] getMrdFollows() {
        if (mrd == null) {
            return new int[0];
        }
        int start = mrd.indexOf('F') + 1;
        if (start == 0) {
            return new int[0];
        }
        int[] result = new int[(mrd.length() - start) / 3];
        for (int i = 0; i < result.length; i += 1) {
            int s = start + (i * 3);
            result[i] = Integer.parseInt(mrd.substring(s, s + 3));
        }
        return result;
    }

    public void setMrd(String mrd) {
        this.mrd = mrd;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getConfirm() {
        return confirm;
    }

    public void setConfirm(Boolean confirm) {
        this.confirm = confirm;
    }

    public Boolean getInterrupt() {
        return interrupt;
    }

    public void setInterrupt(Boolean interrupt) {
        this.interrupt = interrupt;
    }

    public Boolean getAlertTone() {
        return alertTone;
    }

    public void setAlertTone(Boolean alertTone) {
        this.alertTone = alertTone;
    }

    public Boolean getNwrsameTone() {
        return nwrsameTone;
    }

    public void setNwrsameTone(Boolean nwrsameTone) {
        this.nwrsameTone = nwrsameTone;
    }

    public Set<String> getSameTransmitterSet() {
        if (sameTransmitters == null) {
            return Collections.emptySet();
        } else {
            return new HashSet<>(Arrays.asList(sameTransmitters.split("-")));
        }
    }

    public String getSameTransmitters() {
        return sameTransmitters;
    }

    public void setSameTransmitters(String sameTransmitters) {
        this.sameTransmitters = sameTransmitters;
    }

    public void setSameTransmitterSet(Set<String> transmitters) {
        if ((transmitters == null) || transmitters.isEmpty()) {
            sameTransmitters = null;
        } else {
            StringBuilder transmittersBuilder = new StringBuilder();
            for (String transmitter : transmitters) {
                if (transmittersBuilder.length() > 0) {
                    transmittersBuilder.append("-");
                }
                transmittersBuilder.append(transmitter);
            }
            sameTransmitters = transmittersBuilder.toString();
        }
    }

    public String getAreaCodes() {
        return areaCodes;
    }

    public List<String> getAreaCodeList() {
        if ((areaCodes == null) || areaCodes.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(areaCodes.split("-"));
    }

    /**
     * Get a set of area codes.
     * 
     * @return A set of area codes.
     */
    public Set<String> getAreaCodeSet() {
        return new TreeSet<>(getAreaCodeList());
    }

    public void setAreaCodes(String areaCodes) {
        if ((areaCodes != null) && (areaCodes.length() > AREA_CODE_LENGTH)) {
            areaCodes = this.trimAreaCodesToLength(areaCodes);
        }
        this.areaCodes = areaCodes;
    }

    /**
     * Removes area codes at the end of the {@link String} until the area code
     * {@link String} length is <= the specified maximum.
     * 
     * @param areaCodes
     *            the area code {@link String} to trim.
     * @return the trimmed area code {@link String}
     */
    private String trimAreaCodesToLength(String areaCodes) {
        while (areaCodes.length() > AREA_CODE_LENGTH) {
            int lastIndex = areaCodes.lastIndexOf('-');
            areaCodes = areaCodes.substring(0, lastIndex);
        }

        return areaCodes;
    }

    /**
     * @return the selectedTransmitters
     */
    public Set<Transmitter> getSelectedTransmitters() {
        return selectedTransmitters;
    }

    /**
     * @param selectedTransmitters
     *            the selectedTransmitters to set
     */
    public void setSelectedTransmitters(Set<Transmitter> selectedTransmitters) {
        this.selectedTransmitters = selectedTransmitters;
    }

    public Calendar getExpirationTime() {
        return cloneCal(expirationTime);
    }

    public void setExpirationTime(Calendar expirationTime) {
        this.expirationTime = cloneCal(expirationTime);
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isValidHeader() {
        return validHeader;
    }

    public void setValidHeader(boolean validHeader) {
        this.validHeader = validHeader;
    }

    /**
     * Check if this message is periodic or not.
     * 
     * @return true if periodic, false if not
     */
    public boolean isPeriodic() {
        try {
            if ((periodicity != null) && (Integer.parseInt(periodicity) > 0)) {
                return true;
            }
        } catch (NumberFormatException e) {
            return false;
        }
        return false;
    }

    public ReplacementType getReplacementType() {
        return replacementType;
    }

    public void setReplacementType(ReplacementType replacementType) {
        this.replacementType = replacementType;
    }

    /**
     * @return the lastUpdateTime
     */
    public Date getLastUpdateTime() {
        return lastUpdateTime;
    }

    /**
     * @param lastUpdateTime
     *            the lastUpdateTime to set
     */
    public void setLastUpdateTime(Date lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    /**
     * @return the originalFile
     */
    public File getOriginalFile() {
        return originalFile;
    }

    /**
     * 
     * @param originalFile
     *            the originalFile to set
     */
    public void setOriginalFile(File originalFile) {
        this.originalFile = originalFile;
    }

    /**
     * @return the insertTime
     */
    public Date getInsertTime() {
        return insertTime;
    }

    /**
     * The time this message was inserted in to the database. Setter exists only
     * for serialization purposes. Changes to this field are not persisted to
     * database.
     * 
     * @param insertTime
     *            the insertTime to set
     */
    public void setInsertTime(Date insertTime) {
        this.insertTime = insertTime;
    }

    public Origin getOrigin() {
        return origin;
    }

    public void setOrigin(Origin origin) {
        this.origin = origin;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((active == null) ? 0 : active.hashCode());
        result = (prime * result) + ((afosid == null) ? 0 : afosid.hashCode());
        result = (prime * result)
                + ((alertTone == null) ? 0 : alertTone.hashCode());
        result = (prime * result)
                + ((areaCodes == null) ? 0 : areaCodes.hashCode());
        result = (prime * result)
                + ((confirm == null) ? 0 : confirm.hashCode());
        result = (prime * result)
                + ((content == null) ? 0 : content.hashCode());
        result = (prime * result)
                + ((creationTime == null) ? 0 : creationTime.hashCode());
        result = (prime * result)
                + ((effectiveTime == null) ? 0 : effectiveTime.hashCode());
        result = (prime * result)
                + ((expirationTime == null) ? 0 : expirationTime.hashCode());
        result = (prime * result) + id;
        result = (prime * result)
                + ((interrupt == null) ? 0 : interrupt.hashCode());
        result = (prime * result)
                + ((language == null) ? 0 : language.hashCode());
        result = (prime * result) + ((mrd == null) ? 0 : mrd.hashCode());
        result = (prime * result) + ((name == null) ? 0 : name.hashCode());
        result = (prime * result)
                + ((nwrsameTone == null) ? 0 : nwrsameTone.hashCode());
        result = (prime * result)
                + ((periodicity == null) ? 0 : periodicity.hashCode());
        result = (prime * result) + ((cycles == null) ? 0 : cycles.hashCode());
        result = (prime * result) + ((sameTransmitters == null) ? 0
                : sameTransmitters.hashCode());
        result = (prime * result) + ((selectedTransmitters == null) ? 0
                : selectedTransmitters.hashCode());
        result = (prime * result) + (validHeader ? 1231 : 1237);
        result = (prime * result)
                + ((replacementType == null) ? 0 : replacementType.hashCode());
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
        InputMessage other = (InputMessage) obj;
        if (active == null) {
            if (other.active != null) {
                return false;
            }
        } else if (!active.equals(other.active)) {
            return false;
        }
        if (afosid == null) {
            if (other.afosid != null) {
                return false;
            }
        } else if (!afosid.equals(other.afosid)) {
            return false;
        }
        if (alertTone == null) {
            if (other.alertTone != null) {
                return false;
            }
        } else if (!alertTone.equals(other.alertTone)) {
            return false;
        }
        if (areaCodes == null) {
            if (other.areaCodes != null) {
                return false;
            }
        } else if (!areaCodes.equals(other.areaCodes)) {
            return false;
        }
        if (confirm == null) {
            if (other.confirm != null) {
                return false;
            }
        } else if (!confirm.equals(other.confirm)) {
            return false;
        }
        if (content == null) {
            if (other.content != null) {
                return false;
            }
        } else if (!content.equals(other.content)) {
            return false;
        }
        if (creationTime == null) {
            if (other.creationTime != null) {
                return false;
            }
        } else if (!creationTime.equals(other.creationTime)) {
            return false;
        }
        if (effectiveTime == null) {
            if (other.effectiveTime != null) {
                return false;
            }
        } else if (!effectiveTime.equals(other.effectiveTime)) {
            return false;
        }
        if (expirationTime == null) {
            if (other.expirationTime != null) {
                return false;
            }
        } else if (!expirationTime.equals(other.expirationTime)) {
            return false;
        }
        if (id != other.id) {
            return false;
        }
        if (interrupt == null) {
            if (other.interrupt != null) {
                return false;
            }
        } else if (!interrupt.equals(other.interrupt)) {
            return false;
        }
        if (language != other.language) {
            return false;
        }
        if (mrd == null) {
            if (other.mrd != null) {
                return false;
            }
        } else if (!mrd.equals(other.mrd)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (nwrsameTone == null) {
            if (other.nwrsameTone != null) {
                return false;
            }
        } else if (!nwrsameTone.equals(other.nwrsameTone)) {
            return false;
        }
        if (periodicity == null) {
            if (other.periodicity != null) {
                return false;
            }
        } else if (!periodicity.equals(other.periodicity)) {
            return false;
        }
        if (cycles == null) {
            if (other.cycles != null) {
                return false;
            }
        } else if (!cycles.equals(other.cycles)) {
            return false;
        }
        if (sameTransmitters == null) {
            if (other.sameTransmitters != null) {
                return false;
            }
        } else if (!sameTransmitters.equals(other.sameTransmitters)) {
            return false;
        }
        if (selectedTransmitters == null) {
            if (other.selectedTransmitters != null) {
                return false;
            }
        } else if (!selectedTransmitters.equals(other.selectedTransmitters)) {
            return false;
        }
        if (validHeader != other.validHeader) {
            return false;
        }
        if (replacementType != other.replacementType) {
            return false;
        }
        return true;
    }

    /**
     * Protect from any side affects by making clone of any calendar getter or
     * setter.
     * 
     * @param cal
     * @return cloneCal
     */
    private Calendar cloneCal(Calendar cal) {
        Calendar clone = null;
        if (cal != null) {
            clone = (Calendar) cal.clone();
            /*
             * Only use to the minute. Zero these elements to make consistent
             * values for equal and hasCode.
             */
            clone.set(Calendar.MILLISECOND, 0);
            clone.set(Calendar.SECOND, 0);
        }
        return clone;
    }

    @Override
    public String toString() {
        String content = this.content;
        if (content != null) {
            String[] lines = content.split("\n");
            for (String line : lines) {
                if (!line.trim().isEmpty()) {
                    content = line;
                    break;
                }
            }
            if (content.length() > 70) {
                content = content.substring(0, 70);
            }
            content = content + "...";
        }

        return "InputMessage [\n  id=" + id + "\n  language=" + language
                + "\n  name=\"" + name + "\"" + "\n  afosid=" + afosid
                + "\n  creationTime=" + creationTime.getTime()
                + "\n  effectiveTime=" + effectiveTime.getTime()
                + "\n  periodicity=" + periodicity + "\n  mrd=" + mrd
                + "\n  active=" + active + "\n  confirm=" + confirm
                + "\n  interrupt=" + interrupt + "\n  alertTone=" + alertTone
                + "\n  nwrsameTone=" + nwrsameTone + "\n  areaCodes="
                + areaCodes + "\n  expirationTime=" + expirationTime.getTime()
                + "\n  content=" + content + "\n  validHeader=" + validHeader
                + "\n  selectedTransmitters=" + selectedTransmitters
                + "\n  replacementType=" + replacementType + "\n]";
    }

}
