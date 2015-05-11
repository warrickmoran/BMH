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
package com.raytheon.uf.common.bmh.datamodel.playlist;

import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

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
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.ForeignKey;

import com.raytheon.uf.common.bmh.StaticMessageIdentifier;
import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType.Designation;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageTypeSummary;
import com.raytheon.uf.common.bmh.datamodel.msg.Suite;
import com.raytheon.uf.common.bmh.datamodel.msg.Suite.SuiteType;
import com.raytheon.uf.common.bmh.datamodel.msg.SuiteMessage;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.time.util.TimeUtil;

/**
 * 
 * An ordered list of {@link BroadcastMsg}s that is the realization of a
 * specific suite from the messages that are available to play. A playlist is
 * valid only for a specific {@link TransmitterGroup} and {@link Suite}.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Jun 30, 2014  3285     bsteffen    Initial creation
 * Aug 15, 2014  3515     rjpeter     Add eager fetch.
 * Sep 09, 2014  3554     bsteffen    Add QUERY_BY_GROUP_NAME
 * Oct 21, 2014  3746     rjpeter     Hibernate upgrade.
 * Nov 18, 2014  3746     rjpeter     Labeled ForeignKeys.
 * Dec 08, 2014  3864     bsteffen    Add a PlaylistMsg class.
 * Dec 10, 2014  3917     bsteffen    Avoid null end time.
 * Dec 11, 2014  3651     bkowal      Track and propagate messages that are replaced.
 * Dec 13, 2014  3843     mpduff      Add DynamicSerialize and default constructor
 * Dec 16, 2014  3753     bsteffen    Don't trigger forced suites containing only static messages.
 * Jan 05, 2015  3913     bsteffen    Handle future replacements.
 * Jan 20, 2015  4010     bkowal      Compare selected transmitters when analyzing
 *                                    replacements.
 * Feb 05, 2015  4085     bkowal      Designations are no longer static.
 * Mar 12, 2015  4207     bsteffen    Do not preserve start/end time when triggers are present.
 * Mar 12, 2015  4193     bsteffen    Always keep replacements in the list.
 * Mar 25, 2015  4290     bsteffen    Switch to global replacement.
 * Apr 15, 2015  4293     bkowal      Handle the case when a single broadcast message has been
 *                                    expired.
 * May 04, 2015  4449     bkowal      Added {@link #QUERY_BY_UNEXPIRED_PLAYLIST_MSG_ON_TRANSMITTER}.
 * May 11, 2015  4002     bkowal      Added {@link #triggerBroadcastId}.
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
@NamedQueries({
        @NamedQuery(name = Playlist.QUERY_BY_SUITE_GROUP_NAMES, query = Playlist.QUERY_BY_SUITE_GROUP_NAMES_HQL),
        @NamedQuery(name = Playlist.QUERY_BY_GROUP_NAME, query = Playlist.QUERY_BY_GROUP_NAME_HQL),
        @NamedQuery(name = Playlist.QUERY_BY_UNEXPIRED_PLAYLIST_MSG_ON_TRANSMITTER, query = Playlist.QUERY_BY_UNEXPIRED_PLAYLIST_MSG_ON_TRANSMITTER_HQL) })
@Entity
@Table(name = "playlist", schema = "bmh", uniqueConstraints = { @UniqueConstraint(columnNames = {
        "transmitter_group_id", "suite_id" }) })
@SequenceGenerator(initialValue = 1, name = Playlist.GEN, sequenceName = "playlist_seq")
@DynamicSerialize
public class Playlist {

    protected static final String GEN = "Playlist Id Generator";

    /**
     * Named query to pull all messages with a matching afosid and with a valid
     * time range encompassing a specified time range.
     */
    public static final String QUERY_BY_SUITE_GROUP_NAMES = "getPlaylistBySuiteAndGroupNames";

    protected static final String QUERY_BY_SUITE_GROUP_NAMES_HQL = "select p FROM Playlist p inner join p.suite s inner join p.transmitterGroup tg WHERE s.name = :suiteName AND tg.name = :groupName";

    public static final String QUERY_BY_GROUP_NAME = "getPlaylistByGroupName";

    protected static final String QUERY_BY_GROUP_NAME_HQL = "select p FROM Playlist p inner join p.transmitterGroup tg WHERE tg.name = :groupName";

    public static final String QUERY_BY_UNEXPIRED_PLAYLIST_MSG_ON_TRANSMITTER = "getUnexpiredPlaylistsWithMessageOnTransmitter";

    protected static final String QUERY_BY_UNEXPIRED_PLAYLIST_MSG_ON_TRANSMITTER_HQL = "select p FROM Playlist p inner join p.transmitterGroup tg inner join p.messages m WHERE (p.endTime is null OR p.endTime >= :currentTime) AND tg.name = :groupName and m.id = :msgId";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = GEN)
    private int id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "transmitter_group_id")
    @ForeignKey(name = "playlist_to_tx_group")
    private TransmitterGroup transmitterGroup;

    @ManyToOne(optional = false)
    @JoinColumn(name = "suite_id")
    @ForeignKey(name = "playlist_to_suite")
    private Suite suite;

    @Column
    private Calendar modTime;

    @Column
    private Calendar startTime;

    @Column
    private Calendar endTime;

    @Transient
    private Long triggerBroadcastId = null;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(schema = "bmh", name = "playlist_msg", joinColumns = @JoinColumn(name = "playlist_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "message_id", referencedColumnName = "id"))
    @Fetch(FetchMode.SUBSELECT)
    @DynamicSerializeElement
    private Set<BroadcastMsg> messages = new HashSet<>();

    public Playlist() {
        // serialization requires
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public TransmitterGroup getTransmitterGroup() {
        return transmitterGroup;
    }

    public void setTransmitterGroup(TransmitterGroup transmitterGroup) {
        this.transmitterGroup = transmitterGroup;
    }

    public Suite getSuite() {
        return suite;
    }

    public void setSuite(Suite suite) {
        this.suite = suite;
    }

    public Calendar getModTime() {
        return modTime;
    }

    public void setModTime(Calendar modTime) {
        this.modTime = modTime;
    }

    public Calendar getStartTime() {
        return startTime;
    }

    public void setStartTime(Calendar startTime) {
        this.startTime = startTime;
    }

    public Calendar getEndTime() {
        return endTime;
    }

    public Long getTriggerBroadcastId() {
        return triggerBroadcastId;
    }

    public void setTriggerBroadcastId(Long triggerBroadcastId) {
        this.triggerBroadcastId = triggerBroadcastId;
    }

    public void setEndTime(Calendar endTime) {
        this.endTime = endTime;
    }

    /**
     * @return a {@link SortedSet} of {@link PlaylistMessage}s in the order they
     *         should be played according to the {@link Suite}
     */
    public SortedSet<BroadcastMsg> getSortedMessages() {
        SortedSet<BroadcastMsg> sorted = new TreeSet<>(
                new BroadcastMsgSuiteOrderComparator(suite));
        sorted.addAll(messages);
        return sorted;
    }

    /**
     * Intended for things like serialization and persistence which don't
     * actually play messages. Most of the time {@link #getSortedMessages()}
     * should be used instead.
     */
    public Set<BroadcastMsg> getMessages() {
        return messages;
    }

    public void setMessages(Set<BroadcastMsg> messages) {
        this.messages = messages;
    }

    /**
     * Update the modTime to be the currentTime and expire all messages who's
     * expiration is before that time. Also rechecks that all messages are
     * active and in the suite and removes any that are not. If a trigger is
     * removed from the playlist because it has become inactive then the start
     * and end times are also cleared. {@link #setTimes(Set, boolean) should be
     * called at some point after this method before persisting the list to
     * ensure times are set properly.
     * 
     * @param triggers the triggers for the program suite for this list.
     */
    public void refresh(Set<MessageTypeSummary> triggers) {
        modTime = TimeUtil.newGmtCalendar();
        Iterator<BroadcastMsg> it = messages.iterator();
        while (it.hasNext()) {
            BroadcastMsg existing = it.next();
            if (!suite.containsSuiteMessage(existing.getAfosid())) {
                it.remove();
            } else if (modTime.after(existing.getExpirationTime())) {
                it.remove();
            } else if (!existing.isActive() || existing.getForcedExpiration()) {
                for (MessageTypeSummary summary : triggers) {
                    if (summary.getAfosid().equals(existing.getAfosid())) {
                        /*
                         * When a trigger is made inactive the times must be
                         * reset so the playlist stops if there are not other
                         * triggers. If there are other triggers than the times
                         * will be recalulated and the list will continue to
                         * play anyway.
                         */
                        this.startTime = null;
                        this.endTime = null;
                    }
                }
                it.remove();
            }
        }
    }

    /**
     * Calculate the start end, and trigger times of this playlist. The start
     * and end times are stored in their respective fields and the trigger times
     * are returned.
     * 
     * @param triggers
     *            all the message types that are considered triggers for this
     *            playlist. This can be omitted for general suites or when
     *            forced is true because all messages are treated as triggers.
     * @param forced
     *            true if the playlist should be forced, this will ignore
     *            triggers and treat all messages as triggers.
     * @return the applicable trigger times for this playlist, essentially every
     *         start time of a trigger type message. Only the most recent past
     *         trigger time is included, along with all future trigger times.
     */
    public List<Calendar> setTimes(Set<MessageTypeSummary> triggers,
            boolean forced) {
        Set<String> triggerAfosids = new HashSet<>(triggers == null ? 0
                : triggers.size(), 1.0f);
        if (!forced) {
            if (suite.getType() == SuiteType.GENERAL) {
                for (SuiteMessage message : suite.getSuiteMessages()) {
                    triggerAfosids.add(message.getAfosid());
                }
            } else if (triggers != null) {
                for (MessageTypeSummary trigger : triggers) {
                    triggerAfosids.add(trigger.getAfosid());
                }
            }
        } else {
            for (SuiteMessage message : suite.getSuiteMessages()) {
                /*
                 * special case: all messages with a station id designation are
                 * considered a static message.
                 */
                boolean isStatic = (message.getMsgTypeSummary()
                        .getDesignation() == StaticMessageIdentifier.timeDesignation)
                        || (message.getMsgTypeSummary().getDesignation() == Designation.StationID);
                if (isStatic == false) {
                    triggerAfosids.add(message.getAfosid());
                }
            }
        }
        Calendar startTime = null;
        List<Calendar> triggerTimes = new LinkedList<>();
        Calendar endTime = null;
        for (BroadcastMsg message : messages) {
            if (triggerAfosids.contains(message.getAfosid())) {
                Calendar messageStart = message.getEffectiveTime();
                Calendar messageEnd = message.getExpirationTime();
                if ((startTime == null) || startTime.after(messageStart)) {
                    startTime = messageStart;
                }
                triggerTimes.add(messageStart);
                if (endTime == null || endTime.before(messageEnd)) {
                    endTime = messageEnd;
                }
            }
        }
        if (startTime == null) {
            /*
             * If this.startTime is not null then this playlist may have been
             * forced in which case it should continue playing using previously
             * assigned times.
             */
            if (this.startTime == null) {
                this.startTime = this.modTime;
                this.endTime = this.modTime;
                return Collections.emptyList();
            }
        } else {
            this.startTime = startTime;
            this.endTime = endTime;
        }
        if (forced || suite.getType() == SuiteType.GENERAL) {
            return Collections.singletonList(modTime);
        }
        Collections.sort(triggerTimes);
        Iterator<Calendar> it = triggerTimes.iterator();
        Calendar mostRecentPastTrigger = null;
        while (it.hasNext()) {
            Calendar next = it.next();
            if (modTime.after(next)) {
                it.remove();
                mostRecentPastTrigger = next;
            } else {
                break;
            }
        }
        if (mostRecentPastTrigger != null) {
            triggerTimes.add(0, mostRecentPastTrigger);
        }
        return triggerTimes;
    }

    /**
     * Attempt to add a message to the playlist. The message is not added if it
     * is inactive or if the message type is not part of the suite. MAT, MRD,
     * and Identity replacement are all processed and messages that should be
     * replaced will be removed from this list.
     * 
     * @param message
     *            the new message.
     * @param matReplacements
     *            the afosids of messages this message should replace.
     */
    public void addBroadcastMessage(BroadcastMsg message) {
        if (message.isActive() && !messages.contains(message)
                && suite.containsSuiteMessage(message.getAfosid())) {
            messages.add(message);
        }
    }
}
