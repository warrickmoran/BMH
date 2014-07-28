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

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

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
    private Calendar playbackTime;

    public MessagePlaybackPrediction() {
        // for serialization use only
    }

    public MessagePlaybackPrediction(long broadcastId, Calendar playbackTime) {
        this.broadcastId = broadcastId;
        this.playbackTime = playbackTime;
    }

    public long getBroadcastId() {
        return broadcastId;
    }

    public void setBroadcastId(long broadcastId) {
        this.broadcastId = broadcastId;
    }

    public Calendar getPlaybackTime() {
        return playbackTime;
    }

    public void setPlaybackTime(Calendar playbackTime) {
        this.playbackTime = playbackTime;
    }
}
