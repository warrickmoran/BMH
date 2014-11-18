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
import java.util.List;

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
import javax.persistence.OrderColumn;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.ForeignKey;

import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg;
import com.raytheon.uf.common.bmh.datamodel.msg.Suite;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;

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

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(schema = "bmh", name = "playlist_messages", joinColumns = @JoinColumn(name = "playlist_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "message_id", referencedColumnName = "id"), uniqueConstraints = @UniqueConstraint(columnNames = {
            "playlist_id", "message_position" }))
    @OrderColumn(name = "message_position", nullable = false)
    @Fetch(FetchMode.SELECT)
    private List<BroadcastMsg> messages;

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

    public List<BroadcastMsg> getMessages() {
        return messages;
    }

    public void setMessages(List<BroadcastMsg> messages) {
        this.messages = messages;
    }

}
