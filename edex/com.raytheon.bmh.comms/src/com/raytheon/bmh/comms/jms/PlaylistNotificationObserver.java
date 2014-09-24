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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raytheon.bmh.comms.DacTransmitKey;
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

    private final DacTransmitKey key;

    public PlaylistNotificationObserver(DacTransmitServer server,
            DacTransmitKey key) {
        this.server = server;
        this.key = key;
    }

    @Override
    public void notificationArrived(NotificationMessage[] messages) {
        for (NotificationMessage message : messages) {
            try {

                Object payload = message.getMessagePayload();
                if (payload instanceof PlaylistUpdateNotification) {
                    PlaylistUpdateNotification update = (PlaylistUpdateNotification) payload;
                    server.playlistNotificationArrived(key, update);
                } else if (payload != null) {
                    logger.error("Unexpected notification of type: "
                            + payload.getClass().getSimpleName());
                }
            } catch (NotificationException e) {
                // TODO it might be good to tell the dac transmit to rescan the
                // directory at this point.
                logger.error("Error processing notification.", e);
            }

        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((key == null) ? 0 : key.hashCode());
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
        if (key == null) {
            if (other.key != null)
                return false;
        } else if (!key.equals(other.key))
            return false;
        return true;
    }

}
