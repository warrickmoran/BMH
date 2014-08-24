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
package com.raytheon.uf.common.bmh.data;

import java.util.HashMap;
import java.util.Map;

import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.common.bmh.notify.MessagePlaybackPrediction;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Playlist Data Object by Transmitter Group
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 22, 2014     3432   mpduff      Initial creation
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */
@DynamicSerialize
public class PlaylistDataStructure {
    /**
     * DacPlaylistMessageId -> BroadcastMsg
     */
    @DynamicSerializeElement
    private Map<Long, BroadcastMsg> playlistMap;

    /**
     * Broadcast Message ID -> MessagePlaybackPrediction
     */
    @DynamicSerializeElement
    private Map<Long, MessagePlaybackPrediction> predictionMap;

    /**
     * Broadcast Message ID -> MessageType
     */
    @DynamicSerializeElement
    private Map<Long, MessageType> messageTypeMap;

    public Map<Long, BroadcastMsg> getPlaylistMap() {
        if (playlistMap == null) {
            playlistMap = new HashMap<>();
        }

        return playlistMap;
    }

    public void setPlaylistMap(Map<Long, BroadcastMsg> playlistMap) {
        this.playlistMap = playlistMap;
    }

    public Map<Long, MessagePlaybackPrediction> getPredictionMap() {
        if (predictionMap == null) {
            predictionMap = new HashMap<>();
        }

        return predictionMap;
    }

    public void setPredictionMap(
            Map<Long, MessagePlaybackPrediction> predictionMap) {
        this.predictionMap = predictionMap;
    }

    public Map<Long, MessageType> getMessageTypeMap() {
        if (messageTypeMap == null) {
            messageTypeMap = new HashMap<>();
        }

        return messageTypeMap;
    }

    public void setMessageTypeMap(Map<Long, MessageType> messageTypeMap) {
        this.messageTypeMap = messageTypeMap;
    }
}
