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
package com.raytheon.uf.common.bmh.notify;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Notification of the current playlist state for a DAC Transmit. This is
 * generated every time DAC Transmit switches a playlist as well as on demand.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 25, 2014  #3286     dgilling    Initial creation
 * Aug 06, 2014  #3286     dgilling    Rename getMessageIds() to getPlaylist().
 * Jan 13, 2015  #3843     bsteffen    Add periodic predictions
 * Jan 28, 2016   5300     rjpeter     Return emptyList instead of null.
 * Feb 04, 2016   5308     rjpeter     Renamed from PlaylistSwitchNotification to PlaylistNotification.
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

@DynamicSerialize
public class PlaylistNotification {

    @DynamicSerializeElement
    private String suiteName;

    @DynamicSerializeElement
    private String transmitterGroup;

    @DynamicSerializeElement
    private long timestamp;

    @DynamicSerializeElement
    private List<MessagePlaybackPrediction> messages;

    /**
     * Prediction times for periodic messages that are not in this cycle because
     * they need to be scheduled further in the future.
     */
    @DynamicSerializeElement
    private List<MessagePlaybackPrediction> periodicMessages;

    /**
     * Total time to play all messages in {@code messages}, measured in ms.
     */
    @DynamicSerializeElement
    private long playbackCycleTime;

    public PlaylistNotification() {
        // empty constructor for serialization support
    }

    public PlaylistNotification(String suiteName, String transmitterGroup,
            List<MessagePlaybackPrediction> messages, long playbackCycleTime) {
        this.suiteName = suiteName;
        this.transmitterGroup = transmitterGroup;
        this.timestamp = System.currentTimeMillis();
        this.messages = messages;
        this.playbackCycleTime = playbackCycleTime;
    }

    public PlaylistNotification(PlaylistNotification that) {
        this.suiteName = that.suiteName;
        this.transmitterGroup = that.transmitterGroup;
        this.timestamp = that.timestamp;
        this.messages = new ArrayList<>(that.getMessages());
        this.periodicMessages = new ArrayList<>(that.getPeriodicMessages());
        this.playbackCycleTime = that.playbackCycleTime;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PlaylistNotification [suiteName=");
        builder.append(suiteName);
        builder.append(", transmitterGroup=");
        builder.append(transmitterGroup);
        builder.append("]");
        return builder.toString();
    }

    public Set<Long> getBroadcastIds() {
        Set<Long> retVal = new HashSet<>(messages.size(), 1.0f);
        for (MessagePlaybackPrediction message : messages) {
            retVal.add(message.getBroadcastId());
        }
        if (periodicMessages != null) {
            for (MessagePlaybackPrediction message : periodicMessages) {
                retVal.add(message.getBroadcastId());
            }
        }
        return retVal;
    }

    public String getSuiteName() {
        return suiteName;
    }

    public void setSuiteName(String suiteName) {
        this.suiteName = suiteName;
    }

    public String getTransmitterGroup() {
        return transmitterGroup;
    }

    public void setTransmitterGroup(String transmitterGroup) {
        this.transmitterGroup = transmitterGroup;
    }

    public List<MessagePlaybackPrediction> getMessages() {
        if (messages == null) {
            messages = new ArrayList<>(0);
        }

        return messages;
    }

    public void setMessages(List<MessagePlaybackPrediction> messages) {
        this.messages = messages;
    }

    public List<MessagePlaybackPrediction> getPeriodicMessages() {
        if (periodicMessages == null) {
            return Collections.emptyList();
        }

        return periodicMessages;
    }

    public void setPeriodicMessages(
            List<MessagePlaybackPrediction> periodicMessages) {
        this.periodicMessages = periodicMessages;
    }

    public long getPlaybackCycleTime() {
        return playbackCycleTime;
    }

    public void setPlaybackCycleTime(long playbackCycleTime) {
        this.playbackCycleTime = playbackCycleTime;
    }

    /**
     * @return the timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * @param timestamp
     *            the timestamp to set
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Return a new PlaylistNotification with the details of the message in the
     * playlist updated.
     * 
     * @param notification
     */
    public PlaylistNotification merge(
            MessagePlaybackStatusNotification notification) {
        if (notification == null) {
            return this;
        }

        PlaylistNotification rval = new PlaylistNotification(this);

        long bId = notification.getBroadcastId();
        List<MessagePlaybackPrediction> messages = rval.getMessages();
        int lastPlayedIndex = 0;

        for (int i = 0; i < messages.size(); i++) {
            MessagePlaybackPrediction msg = messages.get(i);
            if (bId == msg.getBroadcastId()) {
                messages.set(i, new MessagePlaybackPrediction(notification));
                return rval;
            } else if (msg.getLastTransmitTime() != null) {
                lastPlayedIndex++;
            }
        }

        messages.add(lastPlayedIndex, new MessagePlaybackPrediction(
                notification));
        return rval;
    }
}
