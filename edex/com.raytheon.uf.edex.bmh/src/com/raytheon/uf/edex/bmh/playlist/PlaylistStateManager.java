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

import com.raytheon.uf.common.bmh.BMH_CATEGORY;
import com.raytheon.uf.common.bmh.data.IPlaylistData;
import com.raytheon.uf.common.bmh.data.PlaylistDataStructure;
import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.common.bmh.datamodel.playlist.Playlist;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.notify.LiveBroadcastSwitchNotification;
import com.raytheon.uf.common.bmh.notify.LiveBroadcastSwitchNotification.STATE;
import com.raytheon.uf.common.bmh.notify.MessagePlaybackPrediction;
import com.raytheon.uf.common.bmh.notify.MessagePlaybackStatusNotification;
import com.raytheon.uf.common.bmh.notify.PlaylistSwitchNotification;
import com.raytheon.uf.edex.bmh.dao.BroadcastMsgDao;
import com.raytheon.uf.edex.bmh.dao.MessageTypeDao;
import com.raytheon.uf.edex.bmh.dao.PlaylistDao;
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
 * Nov 30, 2014    3752    mpduff      Store Suite name and playlist cycle duration time.
 * Jan 13, 2015    3843    bsteffen    Track periodic predictions
 * Jan 13, 2015    3844    bsteffen    Include PlaylistMessages in PlaylistDataStructure
 * Feb 05, 2015   4088     bkowal      Handle interrupt playlists that are not saved to the
 *                                     database.
 * Feb 10, 2015    4106    bkowal      Include the playlist that would be playing in a live broadcast
 *                                     notification.
 * Mar 17, 2015   4160     bsteffen    Persist state of tones.
 * Mar 25, 2015   4290     bsteffen    Switch to global replacement.
 * Apr 01, 2015   4349     rferrel     Checks to prevent exceptions when no enabled transmitters.
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

    private PlaylistDao playlistDao;

    private MessageTypeDao messageTypeDao;

    private BroadcastMsgDao broadcastMsgDao;

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
        if (playlistDao == null) {
            statusHandler
                    .info("Unable to process PlaylistSwitchNotification because the PlaylistStateManager has no PlaylistDao.");
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

        List<MessagePlaybackPrediction> messageList = notification
                .getMessages();
        List<MessagePlaybackPrediction> periodicMessageList = notification
                .getPeriodicMessages();

        PlaylistDataStructure playlistData = playlistDataMap.get(tg);
        if (playlistData == null) {
            playlistData = new PlaylistDataStructure();
            playlistDataMap.put(tg, playlistData);
        }
        Map<Long, MessagePlaybackPrediction> predictionMap = playlistData
                .getPredictionMap();

        playlistData.setSuiteName(notification.getSuiteName());
        playlistData.setPlaybackCycleTime(notification.getPlaybackCycleTime());
        predictionMap.clear();

        for (MessagePlaybackPrediction mpp : messageList) {
            predictionMap.put(mpp.getBroadcastId(), mpp);
        }

        Map<Long, MessagePlaybackPrediction> periodicPredictionMap = playlistData
                .getPeriodicPredictionMap();
        periodicPredictionMap.clear();
        if (periodicMessageList != null) {
            for (MessagePlaybackPrediction mpp : periodicMessageList) {
                periodicPredictionMap.put(mpp.getBroadcastId(), mpp);
            }
        }

        Map<Long, BroadcastMsg> playlistMap = playlistData.getPlaylistMap();
        // remove unused messages
        playlistMap.clear();

        Map<Long, MessageType> messageTypeMap = playlistData
                .getMessageTypeMap();

        Playlist playlist = playlistDao
                .getBySuiteAndGroupName(notification.getSuiteName(),
                        notification.getTransmitterGroup());
        /**
         * Warning: the following if statement is almost an exact replication of
         * the if statement in the handlePlaylistSwitchNotification method of
         * PlaylistData.
         */
        if (playlist != null) {
            for (BroadcastMsg message : playlist.getMessages()) {
                long id = message.getId();
                MessageType messageType = messageTypeDao.getByAfosId(message
                        .getAfosid());
                playlistMap.put(id, message);
                messageTypeMap.put(id, messageType);
            }
        } else {
            if (notification.getMessages().size() == 1) {
                /*
                 * A single message without a saved playlist would indicate that
                 * the notification may be for an interrupt.
                 */
                try {
                    // retrieve the associated broadcast message.
                    long id = notification.getMessages().get(0)
                            .getBroadcastId();
                    BroadcastMsg broadcastMsg = this.broadcastMsgDao
                            .getByID(id);
                    if (broadcastMsg == null) {
                        statusHandler.error(
                                BMH_CATEGORY.PLAYLIST_MANAGER_ERROR,
                                "Failed to find the broadcast msg for id: "
                                        + id
                                        + " associated with notification: "
                                        + notification.toString() + ".");
                        return;
                    }
                    MessageType messageType = messageTypeDao
                            .getByAfosId(broadcastMsg.getAfosid());
                    playlistMap.put(id, broadcastMsg);
                    messageTypeMap.put(id, messageType);
                } catch (Exception e) {
                    statusHandler.error(BMH_CATEGORY.PLAYLIST_MANAGER_ERROR,
                            "Error accessing BMH database", e);
                }
            }
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
        setToneFlags(playlistData, id);
    }

    private void setToneFlags(PlaylistDataStructure playlistData,
            long broadcastId) {
        MessagePlaybackPrediction prediction = playlistData.getPredictionMap()
                .get(broadcastId);
        if (prediction.isPlayedAlertTone() || prediction.isPlayedSameTone()) {
            BroadcastMsg broadcastMessage = playlistData.getPlaylistMap().get(
                    broadcastId);
            if (broadcastMessage != null) {
                if (tonesMatch(prediction, broadcastMessage)) {
                    return;
                }
            }
            broadcastMessage = broadcastMsgDao.getByID(broadcastId);
            if (broadcastMessage != null
                    && !tonesMatch(prediction, broadcastMessage)) {
                broadcastMessage.setPlayedAlertTone(prediction
                        .isPlayedAlertTone());
                broadcastMessage.setPlayedSameTone(prediction
                        .isPlayedSameTone());
                broadcastMsgDao.persist(broadcastMessage);
            }
        }
    }

    private static boolean tonesMatch(MessagePlaybackPrediction prediction,
            BroadcastMsg broadcastMessage) {
        return broadcastMessage.isPlayedAlertTone() == prediction
                .isPlayedAlertTone()
                && broadcastMessage.isPlayedSameTone() == prediction
                        .isPlayedSameTone();
    }

    public synchronized IPlaylistData getPlaylistDataStructure(
            String transmitterGrpName) {
        // return a copy of the map to avoid concurrent issues during
        // serialization
        PlaylistDataStructure playlistData = null;
        if (playlistDataMap.containsKey(transmitterGrpName)) {
            playlistData = new PlaylistDataStructure(
                    playlistDataMap.get(transmitterGrpName));
        } else {
            playlistData = new PlaylistDataStructure();
        }

        if ((transmitterGrpName != null)
                && this.liveBroadcastDataMap.containsKey(transmitterGrpName)) {
            return new LiveBroadcastSwitchNotification(
                    this.liveBroadcastDataMap.get(transmitterGrpName),
                    playlistData);
        }

        return playlistData;
    }

    public void setPlaylistDao(PlaylistDao playlistDao) {
        this.playlistDao = playlistDao;
    }

    public void setMessageTypeDao(MessageTypeDao messageTypeDao) {
        this.messageTypeDao = messageTypeDao;
    }

    /**
     * @return the broadcastMsgDao
     */
    public BroadcastMsgDao getBroadcastMsgDao() {
        return broadcastMsgDao;
    }

    /**
     * @param broadcastMsgDao
     *            the broadcastMsgDao to set
     */
    public void setBroadcastMsgDao(BroadcastMsgDao broadcastMsgDao) {
        this.broadcastMsgDao = broadcastMsgDao;
    }

}
