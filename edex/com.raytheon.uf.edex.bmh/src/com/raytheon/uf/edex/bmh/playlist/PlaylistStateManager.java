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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.raytheon.uf.common.bmh.data.PlaylistDataStructure;
import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.notify.INonStandardBroadcast;
import com.raytheon.uf.common.bmh.notify.MessagePlaybackPrediction;
import com.raytheon.uf.common.bmh.notify.MessagePlaybackStatusNotification;
import com.raytheon.uf.common.bmh.notify.NoPlaybackMessageNotification;
import com.raytheon.uf.common.util.CollectionUtil;
import com.raytheon.uf.edex.bmh.dao.BroadcastMsgDao;
import com.raytheon.uf.edex.bmh.dao.MessageTypeDao;

/**
 * Updates broadcast flags in the database.
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
 * Jun 02, 2015   4369     rferrel     Added method {@link #processNoPlaybackMessageNotification(NoPlaybackMessageNotification)}.
 * Jul 29, 2015   4686     bkowal      Removed setting of the broadcast flag on {@link BroadcastMsg}.
 * Jan 28, 2016   5300     rjpeter     Fix PlaylistDataStructure memory leak.
 * Feb 04, 2016   5308     rjpeter     Removed Playlist caching.
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class PlaylistStateManager {
    private MessageTypeDao messageTypeDao;

    private BroadcastMsgDao broadcastMsgDao;

    public PlaylistStateManager() {

    }

    public Map<BroadcastMsg, MessageType> getBroadcastData(Set<Long> ids) {
        if (CollectionUtil.isNullOrEmpty(ids)) {
            return Collections.emptyMap();
        }

        Map<BroadcastMsg, MessageType> broadcastData = new HashMap<>(
                ids.size(), 1);

        for (Long id : ids) {
            BroadcastMsg bMsg = broadcastMsgDao.getByID(id);

            if (bMsg != null) {
                broadcastData.put(bMsg,
                        messageTypeDao.getByAfosId(bMsg.getAfosid()));
            }
        }

        return broadcastData;
    }

    public synchronized void processMessagePlaybackStatusNotification(
            MessagePlaybackStatusNotification notification) {
        /* Update any tone flags in the database */
        if (notification.isPlayedAlertTone() || notification.isPlayedSameTone()) {
            BroadcastMsg broadcastMessage = broadcastMsgDao
                    .getByID(notification.getBroadcastId());
            if (broadcastMessage != null) {
                boolean updated = false;

                if (!tonesMatch(notification, broadcastMessage)) {
                    broadcastMessage.setPlayedAlertTone(notification
                            .isPlayedAlertTone());
                    broadcastMessage.setPlayedSameTone(notification
                            .isPlayedSameTone());
                    updated = true;
                }

                if (broadcastMessage.getInputMessage().getInterrupt()
                        && !broadcastMessage.isPlayedInterrupt()) {
                    broadcastMessage.setPlayedInterrupt(true);
                    updated = true;
                }

                if (updated) {
                    broadcastMsgDao.persist(broadcastMessage);
                }
            }

        }
    }

    private static boolean tonesMatch(
            MessagePlaybackStatusNotification notification,
            BroadcastMsg broadcastMessage) {
        return (broadcastMessage.isPlayedAlertTone() == notification
                .isPlayedAlertTone())
                && (broadcastMessage.isPlayedSameTone() == notification
                        .isPlayedSameTone());
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
