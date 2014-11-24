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
package com.raytheon.uf.edex.bmh.playlist;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.raytheon.uf.common.bmh.data.IPlaylistData;
import com.raytheon.uf.common.bmh.data.PlaylistDataStructure;
import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessageId;
import com.raytheon.uf.common.bmh.notify.LiveBroadcastSwitchNotification;
import com.raytheon.uf.common.bmh.notify.LiveBroadcastSwitchNotification.STATE;
import com.raytheon.uf.common.bmh.notify.MessagePlaybackPrediction;
import com.raytheon.uf.common.bmh.notify.MessagePlaybackStatusNotification;
import com.raytheon.uf.common.bmh.notify.PlaylistSwitchNotification;
import com.raytheon.uf.common.util.CollectionUtil;
import com.raytheon.uf.edex.bmh.dao.BroadcastMsgDao;
import com.raytheon.uf.edex.bmh.dao.MessageTypeDao;
import com.raytheon.uf.edex.bmh.status.BMHStatusHandler;

/**
 * Playlist state tracking manager to track state of the playlists for the GUIs
 * 
 * 
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 23, 2014    3432    mpduff      Initial creation.
 * Aug 24, 2014    3432    mpduff      Added thread safety.
 * Aug 24, 2014    3558    rjpeter     Fixed population of MessagePlaybackPrediction.
 * Oct 07, 2014    3687    bsteffen    Remove singleton and inject daos to allow practice mode.
 * Oct 21, 2014    3655    bkowal      Support LiveBroadcastSwitchNotification.
 * Oct 27, 2014    3712    bkowal      Support LiveBroadcastSwitchNotification#broadcastState.
 * Nov 21, 2014    3845    bkowal      LiveBroadcastSwitchNotification now references a
 *                                     {@link TransmitterGroup}.
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class PlaylistStateManager {
    private static final BMHStatusHandler statusHandler = BMHStatusHandler
            .getInstance(PlaylistStateManager.class);

    /** Map of Transmitter -> PlaylistData for that transmitter */
    private final Map<String, PlaylistDataStructure> playlistDataMap = new HashMap<>();

    private final ConcurrentMap<String, LiveBroadcastSwitchNotification> liveBroadcastDataMap = new ConcurrentHashMap<>();

    private BroadcastMsgDao broadcastMsgDao;

    private MessageTypeDao messageTypeDao;

    public PlaylistStateManager() {

    }

    public synchronized void processLiveBroadcastSwitchNotification(
            LiveBroadcastSwitchNotification notification) {
        statusHandler
                .info("Received a Live Broadcast Switch Notification for Transmitter "
                        + notification.getTransmitterGroup() + ".");
        if (notification.getBroadcastState() == STATE.STARTED) {
            this.liveBroadcastDataMap.put(notification.getTransmitterGroup()
                    .getName(), notification);
        } else {
            if (this.liveBroadcastDataMap.remove(notification
                    .getTransmitterGroup().getName()) != null) {
                statusHandler
                        .info("Evicting Live Broadcast information for Transmitter "
                                + notification.getTransmitterGroup() + ".");
            }
        }
    }

    public synchronized void processPlaylistSwitchNotification(
            PlaylistSwitchNotification notification) {
        String tg = notification.getTransmitterGroup();
        statusHandler.info("PlaylistSwitchNotification arrived for " + tg);
        if (messageTypeDao == null) {
            statusHandler
                    .info("Unable to process PlaylistSwitchNotification because the PlaylistStateManager has no MessageTypeDao.");
            return;
        }
        if (broadcastMsgDao == null) {
            statusHandler
                    .info("Unable to process PlaylistSwitchNotification because the PlaylistStateManager has no BroadcastMsgDao.");
            return;
        }

        /*
         * any playlist notifications received for transmitters that were
         * previously in a live broadcast state eliminates the live broadcast
         * state because all playlist / message switching is paused during a
         * live broadcast.
         */
        if (this.liveBroadcastDataMap.remove(tg) != null) {
            statusHandler
                    .info("Evicting Live Broadcast information for Transmitter "
                            + tg + ".");
        }

        List<DacPlaylistMessageId> playlist = notification.getPlaylist();
        List<MessagePlaybackPrediction> messageList = notification
                .getMessages();

        PlaylistDataStructure playlistData = playlistDataMap.get(tg);
        if (playlistData == null) {
            playlistData = new PlaylistDataStructure();
            playlistDataMap.put(tg, playlistData);
        }
        Map<Long, MessagePlaybackPrediction> predictionMap = playlistData
                .getPredictionMap();

        predictionMap.clear();

        for (MessagePlaybackPrediction mpp : messageList) {
            predictionMap.put(mpp.getBroadcastId(), mpp);
        }

        Map<Long, BroadcastMsg> playlistMap = playlistData.getPlaylistMap();
        // remove unused messages
        playlistMap.keySet().retainAll(predictionMap.keySet());

        Map<Long, MessageType> messageTypeMap = playlistData
                .getMessageTypeMap();

        for (DacPlaylistMessageId id : playlist) {
            List<BroadcastMsg> broadcastMessageList = broadcastMsgDao
                    .getMessageByBroadcastId(id.getBroadcastId());
            if (CollectionUtil.isNullOrEmpty(broadcastMessageList)) {
                continue;
            }

            BroadcastMsg broadcastMessage = broadcastMessageList.get(0);
            MessageType messageType = messageTypeDao
                    .getByAfosId(broadcastMessage.getAfosid());
            playlistMap.put(id.getBroadcastId(), broadcastMessage);
            messageTypeMap.put(id.getBroadcastId(), messageType);
        }
    }

    public synchronized void processMessagePlaybackStatusNotification(
            MessagePlaybackStatusNotification notification) {
        statusHandler.info("MessagePlaybackStatusNotification arrived for "
                + notification.getTransmitterGroup());

        /*
         * any playlist notifications received for transmitters that were
         * previously in a live broadcast state eliminates the live broadcast
         * state because all playlist / message switching is paused during a
         * live broadcast.
         */
        if (this.liveBroadcastDataMap
                .remove(notification.getTransmitterGroup()) != null) {
            statusHandler
                    .info("Evicting Live Broadcast information for Transmitter "
                            + notification.getTransmitterGroup() + ".");
        }

        PlaylistDataStructure playlistData = playlistDataMap.get(notification
                .getTransmitterGroup());
        if (playlistData == null) {
            playlistData = new PlaylistDataStructure();
            playlistDataMap.put(notification.getTransmitterGroup(),
                    playlistData);
        }
        Map<Long, MessagePlaybackPrediction> predictionMap = playlistData
                .getPredictionMap();

        long id = notification.getBroadcastId();
        MessagePlaybackPrediction pred = predictionMap.get(id);
        if (pred == null) {
            pred = new MessagePlaybackPrediction();
            pred.setBroadcastId(id);
            predictionMap.put(id, pred);
        }

        pred.setPlayCount(notification.getPlayCount());
        pred.setLastTransmitTime(notification.getTransmitTime());
        pred.setNextTransmitTime(null);
        pred.setPlayedAlertTone(notification.isPlayedAlertTone());
        pred.setPlayedSameTone(notification.isPlayedSameTone());
    }

    public synchronized IPlaylistData getPlaylistDataStructure(
            String transmitterGrpName) {
        if (this.liveBroadcastDataMap.containsKey(transmitterGrpName)) {
            return this.liveBroadcastDataMap.get(transmitterGrpName);
        }
        // return a copy of the map to avoid concurrent issues during
        // serialization
        if (playlistDataMap.containsKey(transmitterGrpName)) {
            return new PlaylistDataStructure(
                    playlistDataMap.get(transmitterGrpName));
        } else {
            return new PlaylistDataStructure();
        }
    }

    public void setBroadcastMsgDao(BroadcastMsgDao broadcastMsgDao) {
        this.broadcastMsgDao = broadcastMsgDao;
    }

    public void setMessageTypeDao(MessageTypeDao messageTypeDao) {
        this.messageTypeDao = messageTypeDao;
    }

}
