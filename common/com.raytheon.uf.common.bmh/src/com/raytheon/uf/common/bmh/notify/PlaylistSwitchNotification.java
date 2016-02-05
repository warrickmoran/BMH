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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Notification generated when DacTransmit application switches its current
 * playlist.
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
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

@DynamicSerialize
public class PlaylistSwitchNotification {

    @DynamicSerializeElement
    private String suiteName;

    @DynamicSerializeElement
    private String transmitterGroup;

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

    public PlaylistSwitchNotification() {
        // empty constructor for serialization support
    }

    public PlaylistSwitchNotification(String suiteName,
            String transmitterGroup, List<MessagePlaybackPrediction> messages,
            long playbackCycleTime) {
        this.suiteName = suiteName;
        this.transmitterGroup = transmitterGroup;
        this.messages = messages;
        this.playbackCycleTime = playbackCycleTime;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PlaylistSwitchNotification [suiteName=");
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
            return Collections.emptyList();
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
}
