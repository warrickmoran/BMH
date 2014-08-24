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
package com.raytheon.uf.edex.bmh;

import com.raytheon.uf.common.bmh.BMH_CATEGORY;
import com.raytheon.uf.common.bmh.notify.MessagePlaybackStatusNotification;
import com.raytheon.uf.common.bmh.notify.PlaylistSwitchNotification;
import com.raytheon.uf.edex.bmh.playlist.PlaylistStateManager;
import com.raytheon.uf.edex.bmh.status.BMHStatusHandler;

/**
 * 
 * Object for listening to notification from the dac transmit/comms manager
 * about transmitter groups and storing the status so it can be queried.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Jul 28, 2014  3399     bsteffen    Initial creation
 * Aug 23, 2014  3432     mpduff      Implement the notification handling
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class TransmitterStatusTracker {

    protected static final BMHStatusHandler logger = BMHStatusHandler
            .getInstance(TransmitterStatusTracker.class);

    private final PlaylistStateManager stateManager = PlaylistStateManager
            .getInstance();

    public void statusArrived(Object status) {
        if (status instanceof PlaylistSwitchNotification) {
            playlistSwitched((PlaylistSwitchNotification) status);
        } else if (status instanceof MessagePlaybackStatusNotification) {
            messagePlaybabackStatus((MessagePlaybackStatusNotification) status);
        } else {
            logger.error(BMH_CATEGORY.TRANSMITTER_STATUS_ERROR,
                    "Unrecognized status of type:"
                            + status.getClass().getSimpleName());
        }
    }

    protected void playlistSwitched(PlaylistSwitchNotification notification) {
        logger.info("Playlist switched for "
                + notification.getTransmitterGroup());
        stateManager.processPlaylistSwitchNotification(notification);
    }

    protected void messagePlaybabackStatus(
            MessagePlaybackStatusNotification notification) {
        logger.info("message playback status for "
                + notification.getTransmitterGroup());
        stateManager.processMessagePlaybackStatusNotification(notification);
    }
}
