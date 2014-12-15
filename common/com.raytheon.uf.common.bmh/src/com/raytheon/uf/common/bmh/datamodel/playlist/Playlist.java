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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

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

import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageTypeSummary;
import com.raytheon.uf.common.bmh.datamodel.msg.Suite;
import com.raytheon.uf.common.bmh.datamodel.msg.Suite.SuiteType;
import com.raytheon.uf.common.bmh.datamodel.msg.SuiteMessage;
import com.raytheon.uf.common.bmh.datamodel.playlist.PlaylistMessage.ReplacementType;
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
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
@NamedQueries({
        @NamedQuery(name = Playlist.QUERY_BY_SUITE_GROUP_NAMES, query = Playlist.QUERY_BY_SUITE_GROUP_NAMES_HQL),
        @NamedQuery(name = Playlist.QUERY_BY_GROUP_NAME, query = Playlist.QUERY_BY_GROUP_NAME_HQL) })
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

    @OneToMany(mappedBy = "playlist", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @DynamicSerializeElement
    private List<PlaylistMessage> messages = new ArrayList<>();

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

    public void setEndTime(Calendar endTime) {
        this.endTime = endTime;
    }

    /**
     * @return a {@link SortedSet} of {@link PlaylistMessage}s in the order they
     *         should be played according to the {@link Suite}
     */
    public SortedSet<PlaylistMessage> getSortedMessages() {
        SortedSet<PlaylistMessage> sorted = new TreeSet<>(
                new PlaylistMessageSuiteOrderComparator(suite));
        sorted.addAll(messages);
        return sorted;
    }

    /**
     * Intended for things like serialization and persistence which don't
     * actually play messages. Most of the time {@link #getSortedMessages()}
     * should be used instead.
     */
    public List<PlaylistMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<PlaylistMessage> messages) {
        this.messages = messages;
    }

    /**
     * Update the modTime to be the currentTime and expire all messages who's
     * expiration is before that time. Also rechecks that all messages are
     * active and in the suite and removes any that are not.
     */
    public void refresh() {
        modTime = TimeUtil.newGmtCalendar();
        Iterator<PlaylistMessage> it = messages.iterator();
        while (it.hasNext()) {
            PlaylistMessage existing = it.next();
            if (!suite.containsSuiteMessage(existing.getAfosid())) {
                it.remove();
            } else if (modTime.after(existing.getExpirationTime())) {
                it.remove();
            } else if (!existing.isActive()) {
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
        }
        Calendar startTime = this.startTime;
        List<Calendar> triggerTimes = new LinkedList<>();
        Calendar endTime = this.endTime;
        for (PlaylistMessage message : messages) {
            if (forced || triggerAfosids.contains(message.getAfosid())) {
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
            this.startTime = this.modTime;
            this.endTime = this.modTime;
            return Collections.emptyList();
        }
        this.startTime = startTime;
        this.endTime = endTime;
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
    public void addBroadcastMessage(BroadcastMsg message,
            Set<String> matReplacements, List<BroadcastMsg> removedMessages) {
        PlaylistMessage playlistMessage = new PlaylistMessage(message, this);
        if (playlistMessage.isActive() && !messages.contains(playlistMessage)
                && suite.containsSuiteMessage(message.getAfosid())) {
            removeMrdReplacements(playlistMessage, removedMessages);
            removeMatReplacements(playlistMessage, matReplacements,
                    removedMessages);
            removeIdentityReplacements(playlistMessage, removedMessages);
            messages.add(playlistMessage);
        }
    }

    /**
     * Remove any messages with the same type, and areas.
     */
    private void removeIdentityReplacements(PlaylistMessage message,
            List<BroadcastMsg> removedMessages) {
        String afosid = message.getAfosid();
        Iterator<PlaylistMessage> it = messages.iterator();
        while (it.hasNext()) {
            PlaylistMessage existing = it.next();
            if (afosid.equals(existing.getAfosid())) {
                String areaCodes = message.getAreaCodes();
                String existingAreaCodes = existing.getAreaCodes();
                int mrd = message.getMrdId();
                int existingMrd = existing.getMrdId();
                boolean areaCodesEqual = areaCodes == existingAreaCodes
                        || (areaCodes != null && areaCodes
                                .equals(existingAreaCodes));
                boolean mrdEqual = mrd == existingMrd;
                if (areaCodesEqual && mrdEqual) {
                    removedMessages.add(existing.getBroadcastMsg());
                    it.remove();
                }
            }
        }
    }

    /**
     * Remove any messages whose type is in matRepklacements.
     */
    private void removeMatReplacements(PlaylistMessage message,
            Set<String> matReplacements, List<BroadcastMsg> removedMessages) {
        if (matReplacements == null || message.getMrdId() != -1) {
            return;
        }
        Iterator<PlaylistMessage> it = messages.iterator();
        while (it.hasNext()) {
            PlaylistMessage existing = it.next();
            if (matReplacements.contains(existing.getAfosid())) {
                String areaCodes = message.getAreaCodes();
                String existingAreaCodes = existing.getAreaCodes();
                if (areaCodes == existingAreaCodes
                        || (areaCodes != null && areaCodes
                                .equals(existingAreaCodes))) {
                    removedMessages.add(existing.getBroadcastMsg());
                    it.remove();
                    if (message.getReplacementType() == null) {
                        message.setReplacementType(ReplacementType.MAT);
                    }
                }
            }
        }
    }

    /**
     * Remove any messages whose mrd id is in the set of ids that this messages
     * should replace.
     */
    private void removeMrdReplacements(PlaylistMessage message,
            List<BroadcastMsg> removedMessages) {
        int[] mrdReplacements = message.getBroadcastMsg().getInputMessage()
                .getMrdReplacements();
        if (mrdReplacements != null && mrdReplacements.length > 0) {
            Set<Integer> mrdReplacementSet = new HashSet<>(
                    mrdReplacements.length, 1.0f);
            for (int mrdReplacement : mrdReplacements) {
                mrdReplacementSet.add(mrdReplacement);
            }
            Iterator<PlaylistMessage> it = messages.iterator();
            while (it.hasNext()) {
                PlaylistMessage existing = it.next();
                int existingMrdId = existing.getBroadcastMsg()
                        .getInputMessage().getMrdId();
                if (mrdReplacementSet.contains(existingMrdId)) {
                    removedMessages.add(existing.getBroadcastMsg());
                    it.remove();
                    message.setReplacementType(ReplacementType.MRD);
                }
            }
        }
    }

}
