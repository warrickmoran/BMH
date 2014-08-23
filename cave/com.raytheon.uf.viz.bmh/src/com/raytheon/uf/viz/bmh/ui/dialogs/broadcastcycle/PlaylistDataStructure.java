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
package com.raytheon.uf.viz.bmh.ui.dialogs.broadcastcycle;

import java.util.HashMap;
import java.util.Map;

import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.common.bmh.notify.MessagePlaybackPrediction;

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

public class PlaylistDataStructure {
    /**
     * DacPlaylistMessageId -> BroadcastMsg
     */
    private final Map<Long, BroadcastMsg> playlistMap = new HashMap<>();

    /**
     * Broadcast Message ID -> MessagePlaybackPrediction
     */
    private final Map<Long, MessagePlaybackPrediction> predictionMap = new HashMap<>();

    /**
     * Broadcast Message ID -> MessageType
     */
    private final Map<Long, MessageType> messageTypeMap = new HashMap<>();

    /**
     * @return the playlistMap
     */
    public Map<Long, BroadcastMsg> getPlaylistMap() {
        return playlistMap;
    }

    /**
     * @return the predictionMap
     */
    public Map<Long, MessagePlaybackPrediction> getPredictionMap() {
        return predictionMap;
    }

    /**
     * @return the messageTypeMap
     */
    public Map<Long, MessageType> getMessageTypeMap() {
        return messageTypeMap;
    }
}
