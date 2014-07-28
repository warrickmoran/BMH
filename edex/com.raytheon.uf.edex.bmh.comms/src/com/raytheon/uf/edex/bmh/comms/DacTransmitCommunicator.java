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
package com.raytheon.uf.edex.bmh.comms;

import java.io.IOException;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raytheon.uf.common.bmh.datamodel.playlist.PlaylistUpdateNotification;
import com.raytheon.uf.common.bmh.notify.MessagePlaybackStatusNotification;
import com.raytheon.uf.common.bmh.notify.PlaylistSwitchNotification;
import com.raytheon.uf.common.serialization.SerializationException;
import com.raytheon.uf.common.serialization.SerializationUtil;
import com.raytheon.uf.edex.bmh.dactransmit.ipc.DacTransmitShutdown;
import com.raytheon.uf.edex.bmh.dactransmit.ipc.DacTransmitStatus;

/**
 * 
 * Manages communication with a single instance of the dac transmit application.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Jul 16, 2014  3399     bsteffen    Initial creation
 * Jul 25, 2014  3286     dgilling    Support MessagePlaybackStatusNotification
 *                                    and PlaylistSwitchNotification messages 
 *                                    from DacTransmit.
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class DacTransmitCommunicator extends Thread {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final CommsManager manager;

    private final String groupName;

    private final Socket socket;

    private DacTransmitStatus lastStatus;

    public DacTransmitCommunicator(CommsManager manager, String groupName,
            Socket socket) {
        super("DacTransmitCommunicator-" + groupName);
        this.manager = manager;
        this.groupName = groupName;
        this.socket = socket;
    }

    public String getGroupName() {
        return groupName;
    }

    @Override
    public void run() {
        while (!socket.isClosed()) {
            try {
                Object message = SerializationUtil.transformFromThrift(
                        Object.class, socket.getInputStream());
                if (message instanceof DacTransmitStatus) {
                    DacTransmitStatus newStatus = (DacTransmitStatus) message;
                    if (lastStatus == null || !newStatus.equals(lastStatus)) {
                        manager.dacStatusChanged(this, newStatus);
                        lastStatus = newStatus;
                    }
                } else if (message instanceof MessagePlaybackStatusNotification) {
                    // TODO post status back to JMS for GUI's
                    logger.debug("Received playback status message from DacTransmit: "
                            + message.toString());
                } else if (message instanceof PlaylistSwitchNotification) {
                    // TODO post status back to JMS for GUI's
                    logger.debug("Received playlist switch message from DacTransmit: "
                            + message.toString());
                } else if (message instanceof DacTransmitShutdown) {
                    disconnect();
                } else {
                    logger.error("Unexpected message from dac transmit of type: "
                            + message.getClass().getSimpleName());
                }
            } catch (SerializationException | IOException e) {
                logger.error("Error reading message from DacTransmit: "
                        + groupName, e);
                disconnect();
            }
        }
    }

    public boolean isConnectedToDac() {
        if (lastStatus == null) {
            return false;
        } else {
            return lastStatus.isConnectedToDac();
        }
    }

    public void shutdown() {
        try {
            SerializationUtil.transformToThriftUsingStream(
                    new DacTransmitShutdown(), socket.getOutputStream());
        } catch (SerializationException | IOException e) {
            logger.error("Error communicating with DacTransmit: " + groupName,
                    e);
        }
    }

    public void sendPlaylistUpdate(PlaylistUpdateNotification notification) {
        try {
            SerializationUtil.transformToThriftUsingStream(notification,
                    socket.getOutputStream());
        } catch (SerializationException | IOException e) {
            logger.error("Error communicating with DacTransmit: " + groupName,
                    e);
        }
    }

    private void disconnect() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException ignorable) {
            logger.error("Error disconnecting from comms manager");
        }
    }
}
