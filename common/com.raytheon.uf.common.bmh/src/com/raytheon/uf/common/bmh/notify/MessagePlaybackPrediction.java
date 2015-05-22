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

import java.util.Calendar;
import java.util.Date;

import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessage;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.time.util.TimeUtil;

/**
 * Encapsulates predicted playback times for a list of
 * {@code DacPlaylistMessage} objects, which for simplicity reasons, are simply
 * referred to by their broadcast ID. Useful for informing BMH GUI what
 * DacTransmit predicts to play over the next cycle.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 28, 2014  #3286     dgilling     Initial creation
 * Aug 04, 2014  #3286     dgilling     Added additional fields for GUI.
 * Jan 08, 2015  #3912     bsteffen     Add convenience constructors
 * May 22, 2015  #4481     bkowal       Added {@link #dynamic}.
 * 
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

@DynamicSerialize
public final class MessagePlaybackPrediction {

    @DynamicSerializeElement
    private long broadcastId;

    @DynamicSerializeElement
    private Calendar nextTransmitTime;

    @DynamicSerializeElement
    private int playCount;

    @DynamicSerializeElement
    private Calendar lastTransmitTime;

    @DynamicSerializeElement
    private boolean playedAlertTone;

    @DynamicSerializeElement
    private boolean playedSameTone;

    /**
     * boolean flag indicating whether or not this prediction is associated with
     * a message with dynamic content.
     */
    @DynamicSerializeElement
    private boolean dynamic;

    public MessagePlaybackPrediction() {
        // for serialization use only
    }

    public MessagePlaybackPrediction(DacPlaylistMessage message) {
        this.broadcastId = message.getBroadcastId();
        this.playCount = message.getPlayCount();
        this.lastTransmitTime = message.getLastTransmitTime();
        this.playedAlertTone = false;
        this.playedSameTone = false;
        this.dynamic = message.isDynamic();
    }

    public MessagePlaybackPrediction(Calendar playbackTime,
            DacPlaylistMessage message) {
        this(message);
        this.nextTransmitTime = playbackTime;
    }

    public MessagePlaybackPrediction(long playbackTime,
            DacPlaylistMessage message) {
        this(TimeUtil.newGmtCalendar(new Date(playbackTime)), message);
    }

    public long getBroadcastId() {
        return broadcastId;
    }

    public void setBroadcastId(long broadcastId) {
        this.broadcastId = broadcastId;
    }

    public Calendar getNextTransmitTime() {
        return nextTransmitTime;
    }

    public void setNextTransmitTime(Calendar nextTransmitTime) {
        this.nextTransmitTime = nextTransmitTime;
    }

    public int getPlayCount() {
        return playCount;
    }

    public void setPlayCount(int playCount) {
        this.playCount = playCount;
    }

    public Calendar getLastTransmitTime() {
        return lastTransmitTime;
    }

    public void setLastTransmitTime(Calendar lastTransmitTime) {
        this.lastTransmitTime = lastTransmitTime;
    }

    public boolean isPlayedAlertTone() {
        return playedAlertTone;
    }

    public void setPlayedAlertTone(boolean playedAlertTone) {
        this.playedAlertTone = playedAlertTone;
    }

    public boolean isPlayedSameTone() {
        return playedSameTone;
    }

    public void setPlayedSameTone(boolean playedSameTone) {
        this.playedSameTone = playedSameTone;
    }

    /**
     * @return the dynamic
     */
    public boolean isDynamic() {
        return dynamic;
    }

    /**
     * @param dynamic
     *            the dynamic to set
     */
    public void setDynamic(boolean dynamic) {
        this.dynamic = dynamic;
    }
}
