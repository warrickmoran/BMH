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
package com.raytheon.bmh.comms.dactransmit;

import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raytheon.bmh.comms.CommsManager;
import com.raytheon.uf.common.bmh.datamodel.playlist.PlaylistUpdateNotification;
import com.raytheon.uf.common.bmh.notify.DacHardwareStatusNotification;
import com.raytheon.uf.common.bmh.notify.MessagePlaybackStatusNotification;
import com.raytheon.uf.common.bmh.notify.PlaylistSwitchNotification;
import com.raytheon.uf.common.serialization.SerializationException;
import com.raytheon.uf.common.serialization.SerializationUtil;
import com.raytheon.uf.edex.bmh.dactransmit.ipc.ChangeTransmitters;
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
 * Jul 31, 2014  3286     dgilling    Support DacHardwareStatusNotification.
 * Aug 12, 2014  3486     bsteffen    Support ChangeTransmitters
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class DacTransmitCommunicator extends Thread {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Object sendLock = new Object();

    private final CommsManager manager;

    private final DacTransmitServer server;

    private final String groupName;

    private final Socket socket;

    private int[] radios;

    private DacTransmitStatus lastStatus;

    public DacTransmitCommunicator(CommsManager manager,
            DacTransmitServer server, String groupName,
            int[] radios, Socket socket) {
        super("DacTransmitCommunicator-" + groupName);
        this.manager = manager;
        this.server = server;
        this.groupName = groupName;
        Arrays.sort(radios);
        this.radios = radios;
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
                handleMessage(message);
            } catch (SerializationException | IOException e) {
                logger.error("Error reading message from DacTransmit: "
                        + groupName, e);
                disconnect();
            }
        }
    }

    protected void handleMessage(Object message) {
        if (message instanceof DacTransmitStatus) {
            DacTransmitStatus newStatus = (DacTransmitStatus) message;
            if (lastStatus == null || !newStatus.equals(lastStatus)) {
                if (newStatus.isConnectedToDac()) {
                    server.connectedToDac(this);
                }
                manager.dacStatusChanged(this, newStatus);
                lastStatus = newStatus;

            }
        } else if (message instanceof MessagePlaybackStatusNotification) {
            MessagePlaybackStatusNotification notification = (MessagePlaybackStatusNotification) message;
            notification.setTransmitterGroup(groupName);
            manager.messagePlaybackStatusArrived(notification);
        } else if (message instanceof PlaylistSwitchNotification) {
            PlaylistSwitchNotification notification = (PlaylistSwitchNotification) message;
            notification.setTransmitterGroup(groupName);
            manager.playlistSwitched(notification);
        } else if (message instanceof DacHardwareStatusNotification) {
            DacHardwareStatusNotification notification = (DacHardwareStatusNotification) message;
            notification.setTransmitterGroup(groupName);
            manager.hardwareStatusArrived(notification);
        } else if (message instanceof DacTransmitShutdown) {
            disconnect();
        } else {
            logger.error("Unexpected message from dac transmit of type: "
                    + message.getClass().getSimpleName());
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
        send(new DacTransmitShutdown());
    }

    public void setRadios(int[] radios) {
        Arrays.sort(radios);
        if (!Arrays.equals(radios, this.radios)) {
            send(new ChangeTransmitters(radios));
            this.radios = radios;
        }
    }

    public void sendPlaylistUpdate(PlaylistUpdateNotification notification) {
        send(notification);
    }

    private void send(Object toSend) {
        synchronized (sendLock) {
            try {
                SerializationUtil.transformToThriftUsingStream(toSend,
                        socket.getOutputStream());
            } catch (SerializationException | IOException e) {
                logger.error("Error communicating with DacTransmit: "
                        + groupName, e);
            }
        }
    }

    private void disconnect() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            logger.error("Error disconnecting from comms manager", e);
        }
        server.disconnected(this);
    }
}
