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
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.raytheon.uf.common.time.util.TimeUtil;

/**
 * 
 * Xml representation of a playlist that is sent from the playlist manager to
 * the comms manager.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Jun 30, 2014  3285     bsteffen    Initial creation
 * Jul 22, 2014  3286     dgilling    Added toString(), isValid().
 * Aug 22, 2014  3286     dgilling    Added isExpired().
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
@XmlRootElement(name = "bmhPlaylist")
@XmlAccessorType(XmlAccessType.NONE)
public class DacPlaylist {

    @XmlAttribute
    private String transmitterGroup;

    @XmlAttribute
    private int priority;

    @XmlAttribute
    private String suite;

    @XmlAttribute(name = "created")
    private Calendar creationTime;

    @XmlAttribute(name = "start")
    private Calendar start;

    @XmlAttribute(name = "expired")
    private Calendar expired;

    @XmlAttribute
    private Calendar latestTrigger;

    @XmlAttribute(name = "interrupt")
    private boolean interrupt;

    @XmlElement(name = "message")
    private List<DacPlaylistMessageId> messages;

    public DacPlaylist() {

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return PlaylistUpdateNotification.getFilePath(this).toString();
    }

    /**
     * Determine whether this playlist is within its valid playback period based
     * on the current time.
     * 
     * @return {@code true}, if the playlist's start time is before the current
     *         time and the expire time is after the current time. Else,
     *         {@code false}.
     */
    public boolean isValid() {
        long currentTime = TimeUtil.currentTimeMillis();
        return ((currentTime >= start.getTimeInMillis()) && (currentTime <= expired
                .getTimeInMillis()));
    }

    /**
     * Determine whether this playlist has passed its expiration time based on
     * the current time.
     * 
     * @return {@code true}, if the playlist's expire time is before the current
     *         time. Else, {@code false}.
     */
    public boolean isExpired() {
        return (TimeUtil.currentTimeMillis() <= expired.getTimeInMillis());
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getTransmitterGroup() {
        return transmitterGroup;
    }

    public void setTransmitterGroup(String transmitterGroup) {
        this.transmitterGroup = transmitterGroup;
    }

    public String getSuite() {
        return suite;
    }

    public void setSuite(String suite) {
        this.suite = suite;
    }

    public Calendar getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Calendar creationTime) {
        this.creationTime = creationTime;
    }

    public Calendar getStart() {
        return start;
    }

    public void setStart(Calendar start) {
        this.start = start;
    }

    public Calendar getExpired() {
        return expired;
    }

    public void setExpired(Calendar expired) {
        this.expired = expired;
    }

    public Calendar getLatestTrigger() {
        return latestTrigger;
    }

    public void setLatestTrigger(Calendar latestTrigger) {
        this.latestTrigger = latestTrigger;
    }

    public void addMessage(DacPlaylistMessageId message) {
        if (messages == null) {
            messages = new ArrayList<>();
        }
        messages.add(message);
    }

    public List<DacPlaylistMessageId> getMessages() {
        return messages;
    }

    public void setMessages(List<DacPlaylistMessageId> messages) {
        this.messages = messages;
    }

    public boolean isInterrupt() {
        return interrupt;
    }

    public void setInterrupt(boolean interrupt) {
        this.interrupt = interrupt;
    }

}
