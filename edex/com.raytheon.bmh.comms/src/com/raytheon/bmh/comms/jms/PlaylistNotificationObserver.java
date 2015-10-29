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
package com.raytheon.bmh.comms.jms;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raytheon.bmh.comms.dactransmit.DacTransmitServer;
import com.raytheon.uf.common.bmh.datamodel.playlist.PlaylistUpdateNotification;
import com.raytheon.uf.common.jms.notification.INotificationObserver;
import com.raytheon.uf.common.jms.notification.NotificationException;
import com.raytheon.uf.common.jms.notification.NotificationMessage;

/**
 * 
 * Observer for recieving {@link PlaylistUpdateNotification} and forwarding them
 * to dac transmit.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Jul 24, 2014  3399     bsteffen    Initial creation
 * Sep 23, 2014  3485     bsteffen    Send notification to the DacTransmitServer
 * Oct 17, 2014  3687     bsteffen    Add info log statement for normal playlist updates.
 * Feb 09, 2015  4071     bsteffen    Consolidate Queues.
 * Feb 16, 2015  4107     bsteffen    Notify dac transmit server when actually connected.
 * Aug 12, 2015  4424     bkowal      Eliminate Dac Transmit Key.
 * 
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class PlaylistNotificationObserver implements INotificationObserver {

    private static final Logger logger = LoggerFactory
            .getLogger(PlaylistNotificationObserver.class);

    private final DacTransmitServer server;

    private final String transmitterGroup;

    public PlaylistNotificationObserver(DacTransmitServer server,
            String transmitterGroup) {
        this.server = server;
        this.transmitterGroup = transmitterGroup;
    }

    @Override
    public void notificationArrived(NotificationMessage[] messages) {
        for (NotificationMessage message : messages) {
            try {

                Object payload = message.getMessagePayload();
                if (payload instanceof PlaylistUpdateNotification) {
                    PlaylistUpdateNotification update = (PlaylistUpdateNotification) payload;
                    Path playlistPath = Paths.get(update.getPlaylistPath());
                    String playlistGroup = playlistPath.getName(0).toString();
                    if (transmitterGroup.equals(playlistGroup)) {
                        logger.info("A new playlist is at {}",
                                update.getPlaylistPath());
                        server.playlistNotificationArrived(
                                this.transmitterGroup, update);
                    }
                } else if (payload != null) {
                    logger.error("Unexpected notification of type: "
                            + payload.getClass().getSimpleName());
                }
            } catch (NotificationException e) {
                logger.error("Error processing notification.", e);
            }

        }
    }

    public void connected() {
        server.updatePlaylistListener(this.transmitterGroup, true);

    }

    public void disconnected() {
        server.updatePlaylistListener(this.transmitterGroup, false);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime
                * result
                + ((this.transmitterGroup == null) ? 0 : this.transmitterGroup
                        .hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PlaylistNotificationObserver other = (PlaylistNotificationObserver) obj;
        if (transmitterGroup == null) {
            if (other.transmitterGroup != null)
                return false;
        } else if (!transmitterGroup.equals(other.transmitterGroup))
            return false;
        return true;
    }

}
