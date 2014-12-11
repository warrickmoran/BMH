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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * 
 * A single message in a {@link Playlist}. The primary purpose of this
 * class(rather than just a join table) is to track replacement type.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Dec 08, 2014  3864     bsteffen    Initial creation
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
@Entity
@Table(name = "playlist_msg", schema = "bmh")
@SequenceGenerator(initialValue = 1, name = PlaylistMessage.GEN, sequenceName = "playlist_msg_seq")
public class PlaylistMessage {

    public static final String GEN = "Playlist Msg Generator";

    public static enum ReplacementType {
        MAT, MRD;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = GEN)
    @DynamicSerializeElement
    private long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "broadcast_msg_id")
    private BroadcastMsg broadcastMsg;

    @ManyToOne(optional = false)
    private Playlist playlist;

    @Enumerated(EnumType.STRING)
    @Column
    private ReplacementType replacementType;

    public PlaylistMessage() {

    }

    public PlaylistMessage(BroadcastMsg broadcastMsg, Playlist playlist) {
        this.broadcastMsg = broadcastMsg;
        this.playlist = playlist;
    }

    public BroadcastMsg getBroadcastMsg() {
        return broadcastMsg;
    }

    public void setBroadcastMsg(BroadcastMsg broadcastMsg) {
        this.broadcastMsg = broadcastMsg;
    }

    public Playlist getPlaylist() {
        return playlist;
    }

    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
    }

    public ReplacementType getReplacementType() {
        return replacementType;
    }

    public void setReplacementType(ReplacementType replacementType) {
        this.replacementType = replacementType;
    }

    /**
     * convenience method, equivalent to getBroadcastMsg().getAfosid()
     */
    public String getAfosid() {
        return broadcastMsg.getAfosid();
    }

    /**
     * convenience method, equivalent to
     * getBroadcastMsg().getInputMessage().getAreaCodes()
     */
    public String getAreaCodes() {
        return broadcastMsg.getInputMessage().getAreaCodes();
    }

    /**
     * convenience method, equivalent to
     * getBroadcastMsg().getInputMessage().getEffectiveTime()
     */
    public Calendar getEffectiveTime() {
        return broadcastMsg.getInputMessage().getEffectiveTime();
    }

    /**
     * convenience method, equivalent to
     * getBroadcastMsg().getInputMessage().getExpirationTime()
     */
    public Calendar getExpirationTime() {
        return broadcastMsg.getInputMessage().getExpirationTime();
    }

    /**
     * convenience method, equivalent to
     * getBroadcastMsg().getInputMessage().getActive()
     */
    public boolean isActive() {
        return !Boolean.FALSE
                .equals(broadcastMsg.getInputMessage().getActive());
    }

    /**
     * convenience method, equivalent to
     * getBroadcastMsg().getInputMessage().getMrdId()
     */
    public int getMrdId() {
        return broadcastMsg.getInputMessage().getMrdId();
    }

    /**
     * {@link #id} and {@link #replacementType} are not considered in equality.
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((broadcastMsg == null) ? 0 : broadcastMsg.hashCode());
        result = prime * result
                + ((playlist == null) ? 0 : playlist.hashCode());
        return result;
    }

    /**
     * {@link #id} and {@link #replacementType} are not considered in equality.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PlaylistMessage other = (PlaylistMessage) obj;
        if (broadcastMsg == null) {
            if (other.broadcastMsg != null)
                return false;
        } else if (!broadcastMsg.equals(other.broadcastMsg))
            return false;
        if (playlist == null) {
            if (other.playlist != null)
                return false;
        } else if (!playlist.equals(other.playlist))
            return false;
        return true;
    }

}