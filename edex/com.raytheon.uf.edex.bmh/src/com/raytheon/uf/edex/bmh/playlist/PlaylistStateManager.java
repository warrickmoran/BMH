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
import com.raytheon.uf.common.bmh.notify.DacTransmitShutdownNotification;
import com.raytheon.uf.common.bmh.notify.INonStandardBroadcast;
import com.raytheon.uf.common.bmh.notify.LiveBroadcastSwitchNotification;
import com.raytheon.uf.common.bmh.notify.LiveBroadcastSwitchNotification.STATE;
import com.raytheon.uf.common.bmh.notify.MaintenanceMessagePlayback;
import com.raytheon.uf.common.bmh.notify.MessagePlaybackPrediction;
import com.raytheon.uf.common.bmh.notify.MessagePlaybackStatusNotification;
import com.raytheon.uf.common.bmh.notify.NoPlaybackMessageNotification;
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
 * Apr 20, 2015   4394     bkowal      Ensure that playlist information does not remain in the
 *                                     cache for disabled transmitters that are no longer transmitting.
 * Apr 29, 2015   4394     bkowal      Support {@link INonStandardBroadcast}.
 * May 05, 2015   4456     bkowal      Update the interrupt flag associated with a {@link BroadcastMsg}
 *                                     after it has played as an interrupt.
 * May 08, 2015   4478     bkowal      Prevent NPE in {@link #setToneFlags(PlaylistDataStructure, long)}.
 * May 21, 2015   4397     bkowal      Update the broadcast flag on {@link BroadcastMsg}.
 * May 22, 2015   4481     bkowal      Set the dynamic flag on the {@link MessagePlaybackPrediction}.
 * Jun 02, 2016   4369     rferrel     Added method {@link #processNoPlaybackMessageNotification(NoPlaybackMessageNotification)}.
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

    private final ConcurrentMap<String, INonStandardBroadcast> broadcastOverrideMap = new ConcurrentHashMap<>();

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
            this.broadcastOverrideMap.put(notification.getTransmitterGroup()
                    .getName(), notification);
        } else {
            if (this.broadcastOverrideMap.remove(notification
                    .getTransmitterGroup().getName()) != null) {
                statusHandler
                        .info("Evicting Live Broadcast information for Transmitter "
                                + notification.getTransmitterGroup() + ".");
            }
        }
    }

    public synchronized void processMaintenanceMessagePlayback(
            MaintenanceMessagePlayback playback) {
        statusHandler
                .info("Received a Maintenance Message Playback notification for Transmitter "
                        + playback.getTransmitterGroup() + ".");
        this.broadcastOverrideMap.put(playback.getTransmitterGroup(), playback);
    }

    public synchronized void processNoPlaybackMessageNotification(
            NoPlaybackMessageNotification notification) {
        String group = notification.getGroupName();
        statusHandler
                .info("Received a No Playback Message notification for Transmitter "
                        + group);
        if (this.broadcastOverrideMap.remove(group) != null) {
            statusHandler
                    .info("Evicting Live Braodcast information for No Playback on Transmitter "
                            + group);
        }
        if (this.playlistDataMap.remove(group) != null) {
            statusHandler
                    .info("Evicting Playlist information for No Playback on Transmitter "
                            + group);
        }
    }

    public synchronized void handleDacTransmitShutdown(
            DacTransmitShutdownNotification notification) {
        statusHandler
                .info("Received a Dac Transmit Shutdown Notification for Transmitter "
                        + notification.getTransmitterGroup() + ".");

        /*
         * Determine if the Transmitter Group has actually been disabled or if a
         * load-balancing may have occurred.
         */
        final String name = notification.getTransmitterGroup();
        if (this.broadcastOverrideMap.remove(name) != null) {
            statusHandler
                    .info("Evicting Live Broadcast information for Disabled Transmitter "
                            + name + ".");
        }

        /*
         * Ensure that all requests for playlist information accurately reflect
         * that the 'Off the Air' message should be playing.
         */
        if (this.playlistDataMap.remove(name) != null) {
            statusHandler
                    .info("Evicting Playlist information for Disabled Transmitter "
                            + name + ".");
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
        if (this.broadcastOverrideMap.remove(tg) != null) {
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
        if (this.broadcastOverrideMap
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
        pred.setDynamic(notification.isDynamic());
        setToneFlags(playlistData, id);
    }

    private void setToneFlags(PlaylistDataStructure playlistData,
            long broadcastId) {
        final boolean interrupt = (playlistData.getSuiteName() == null) ? false
                : playlistData.getSuiteName().startsWith("Interrupt");
        MessagePlaybackPrediction prediction = playlistData.getPredictionMap()
                .get(broadcastId);
        BroadcastMsg broadcastMessage = playlistData.getPlaylistMap().get(
                broadcastId);
        if (prediction.isPlayedAlertTone() || prediction.isPlayedSameTone()
                || interrupt) {
            if (broadcastMessage != null) {
                if (tonesMatch(prediction, broadcastMessage)
                        && interrupt == false && broadcastMessage.isBroadcast()) {
                    return;
                }
            }
            broadcastMessage = broadcastMsgDao.getByID(broadcastId);
            if (broadcastMessage != null) {
                boolean updated = false;

                if (!tonesMatch(prediction, broadcastMessage)) {
                    broadcastMessage.setPlayedAlertTone(prediction
                            .isPlayedAlertTone());
                    broadcastMessage.setPlayedSameTone(prediction
                            .isPlayedSameTone());
                    updated = true;
                }
                if (interrupt
                        && interrupt != broadcastMessage.isPlayedInterrupt()) {
                    broadcastMessage.setPlayedInterrupt(interrupt);
                    updated = true;
                }
                if (broadcastMessage.isBroadcast() == false) {
                    broadcastMessage.setBroadcast(true);
                    updated = true;
                }
                if (updated) {
                    broadcastMsgDao.persist(broadcastMessage);
                }
            }
        } else {
            if (broadcastMessage != null && broadcastMessage.isBroadcast()) {
                return;
            }
            broadcastMessage = broadcastMsgDao.getByID(broadcastId);
            if (broadcastMessage != null) {
                broadcastMessage.setBroadcast(true);
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
                && this.broadcastOverrideMap.containsKey(transmitterGrpName)) {
            return this.getBroadcastOverride(transmitterGrpName, playlistData);
        }

        return playlistData;
    }

    private synchronized INonStandardBroadcast getBroadcastOverride(
            String transmitterGrpName, PlaylistDataStructure playlistData) {
        INonStandardBroadcast broadcast = this.broadcastOverrideMap
                .get(transmitterGrpName);
        if (broadcast instanceof LiveBroadcastSwitchNotification) {
            return new LiveBroadcastSwitchNotification(
                    (LiveBroadcastSwitchNotification) broadcast, playlistData);
        } else if (broadcast instanceof MaintenanceMessagePlayback) {
            return new MaintenanceMessagePlayback(
                    (MaintenanceMessagePlayback) broadcast);
        }

        return null;
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
