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
package com.raytheon.bmh.comms.cluster;

import java.io.IOException;
import java.net.Socket;

import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raytheon.bmh.comms.CommsManager;
import com.raytheon.uf.common.bmh.broadcast.ILiveBroadcastMessage;
import com.raytheon.uf.common.bmh.comms.SendPlaylistMessage;
import com.raytheon.uf.common.bmh.notify.LiveBroadcastSwitchNotification;
import com.raytheon.uf.common.serialization.SerializationUtil;

/**
 * 
 * Manages communication with another instance of a comms manager.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Sep 25, 2014  3399     bsteffen    Initial creation
 * Oct 10, 2014  3656     bkowal      Updates to allow sending other message
 *                                    types to cluster members.
 * Nov 11, 2014  3762     bsteffen    Add load balancing of dac transmits.
 * Dec 1, 2014   3797     bkowal      Support broadcast clustering.
 * Jan 14, 2015  3869     bsteffen    Log a shorter message for disconnect.
 * Mar 20, 2015  4296     bsteffen    Catch all throwables from SerializationUtil.
 * Apr 07, 2015  4370     rjpeter     Add handling of ClusterConfigMessage.
 * Apr 08, 2015  4368     rjpeter     Add uniqueId and remoteAccepted.
 * Aug 10, 2015  4711     rjpeter     Add cluster heartbeat.
 * Aug 11, 2015  4372     bkowal      Handle {@link LiveBroadcastSwitchNotification} from other
 * Aug 12, 2015  4424     bkowal      Eliminate Dac Transmit Key.
 *                                    cluster members.
 * Oct 23, 2015  5029     rjpeter     Make isConnected public, have disconnect call removeCommunicator.
 * Oct 28, 2015  5029     rjpeter     Allow multiple dac transmits to be requested.
 * Feb 11, 2016  5308     rjpeter     Forward on SendPlaylistMessage.
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class ClusterCommunicator extends Thread {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Socket socket;

    private final Object sendLock = new Object();

    private final CommsManager manager;

    private final ClusterServer server;

    private ClusterStateMessage state;

    private final String remoteHost;

    private volatile long timeLastMessageReceived = System.currentTimeMillis();

    public ClusterCommunicator(CommsManager manager, ClusterServer server,
            Socket socket, String remoteHost) {
        super("ClusterCommunicator-" + remoteHost);

        if (socket == null) {
            // not currently possible, but better safe than sorry
            throw new IllegalArgumentException("Socket must not be null");
        }

        this.socket = socket;
        this.manager = manager;
        this.server = server;
        this.remoteHost = remoteHost;
    }

    @Override
    public void run() {
        while (!socket.isClosed()) {
            try {
                Object message = SerializationUtil.transformFromThrift(
                        Object.class, socket.getInputStream());
                timeLastMessageReceived = System.currentTimeMillis();
                handleMessage(message);
            } catch (Throwable e) {
                boolean lostConnection = false;
                if (e.getCause() instanceof TTransportException) {
                    TTransportException te = (TTransportException) e.getCause();
                    if (te.getType() == TTransportException.END_OF_FILE) {
                        lostConnection = true;
                    }
                }
                if (lostConnection) {
                    logger.error(
                            "Lost connection to cluster member: {}, disconnecting...",
                            remoteHost);
                } else {
                    logger.error(
                            "Error reading message from cluster member: {}, disconnecting...",
                            remoteHost, e);
                }
                disconnect();
            }
        }
    }

    protected void handleMessage(Object message) {
        if (message instanceof ClusterStateMessage) {
            ClusterStateMessage newState = (ClusterStateMessage) message;
            ClusterStateMessage oldState = state;
            state = newState;
            logger.info("Clustered manager {} is connected to {} dac(s)",
                    remoteHost, newState.getConnectedTransmitters().size());
            if (oldState == null) {
                for (String transmitterGroup : newState
                        .getConnectedTransmitters()) {
                    manager.dacConnectedRemote(transmitterGroup);
                }
                if (newState.hasRequestedTransmitter()) {
                    logger.info(
                            "Clustered manager {} has requested a dac transmit.",
                            remoteHost);
                    manager.dacRequestedRemote(newState
                            .getRequestedTransmitters());
                }
            } else {
                for (String transmitterGroup : newState
                        .getConnectedTransmitters()) {
                    if (oldState.contains(transmitterGroup) == false) {
                        manager.dacConnectedRemote(transmitterGroup);
                    }
                }
                for (String transmitterGroup : oldState
                        .getConnectedTransmitters()) {
                    if (newState.contains(transmitterGroup) == false) {
                        manager.dacDisconnectedRemote(transmitterGroup);
                    }
                }
                if (newState.hasRequestedTransmitter()
                        && !newState.getRequestedTransmitters().equals(
                                oldState.getRequestedTransmitters())) {
                    logger.info(
                            "Clustered manager {} has requested a dac transmit.",
                            remoteHost);
                    manager.dacRequestedRemote(newState
                            .getRequestedTransmitters());
                }
            }
        } else if (message instanceof ClusterShutdownMessage) {
            if (((ClusterShutdownMessage) message).isAcknowledged()) {
                logger.info("{} has acknowledged shutdown, disconnecting...",
                        remoteHost);
                disconnect();
            } else {
                logger.info(
                        "Clustered manager {} is shutting down, disconnecting...",
                        remoteHost);
                send(new ClusterShutdownMessage(true));
                disconnect();
            }
        } else if (message instanceof ClusterConfigMessage) {
            this.manager.reloadConfig(false);
        } else if (message instanceof ILiveBroadcastMessage) {
            this.manager
                    .forwardDacBroadcastMsg((ILiveBroadcastMessage) message);
        } else if (message instanceof ClusterHeartbeatMessage) {
            String heartbeatIp = ((ClusterHeartbeatMessage) message).getHost();
            if (!remoteHost.equals(heartbeatIp)) {
                logger.error(
                        "Received wrong heartbeat host for {}.  Expected {} received {}, disconnecting...",
                        remoteHost, remoteHost, heartbeatIp);
                disconnect();
            }
        } else if (message instanceof LiveBroadcastSwitchNotification) {
            LiveBroadcastSwitchNotification notification = (LiveBroadcastSwitchNotification) message;
            this.manager.checkLoadBalanceLockdown(notification);
        } else if (message instanceof SendPlaylistMessage) {
            this.manager
                    .forwardPlaylistRequestMessage((SendPlaylistMessage) message);
        } else {
            logger.error("Unexpected message from cluster member of type: {}",
                    message.getClass().getSimpleName());
        }
    }

    public boolean send(Object toSend) {
        synchronized (sendLock) {
            if (this.isConnected() == false) {
                logger.error(
                        "No active connection(s) to cluster member: {}, disconnecting...",
                        this.remoteHost);

                // No usable connection. Initiate cleanup.
                this.disconnect();
                return false;
            }
            try {
                SerializationUtil.transformToThriftUsingStream(toSend,
                        socket.getOutputStream());
            } catch (Throwable e) {
                logger.error(
                        "Error communicating with cluster member: {}, disconnecting...",
                        remoteHost, e);
                this.disconnect();
                return false;
            }

            return true;
        }
    }

    public boolean sendState(ClusterStateMessage state) {
        return send(state);
    }

    public boolean isConnected() {
        return (this.socket.isClosed() == false)
                && (this.socket.isOutputShutdown() == false)
                && (this.socket.isInputShutdown() == false);
    }

    /**
     * True if the remote server has accepted the connection and the connection
     * is still valid.
     * 
     * @return
     */
    public boolean remoteAccepted() {
        return (state != null) && isConnected();
    }

    public void disconnect() {
        try {
            socket.close();
        } catch (IOException e) {
            logger.error("Error disconnecting from cluster member: {}",
                    remoteHost, e);
        }

        if (state != null) {
            for (String transmitterGroup : state.getConnectedTransmitters()) {
                manager.dacDisconnectedRemote(transmitterGroup);
            }
        }

        server.removeCommunicator(this);
    }

    public boolean isConnected(String transmitterGroup) {
        return remoteAccepted() && state.contains(transmitterGroup);
    }

    public boolean isRequested(String transmitterGroup) {
        if ((state == null) || socket.isClosed()) {
            return false;
        }
        return state.isRequestedTransmitter(transmitterGroup);
    }

    public void shutdown() {
        send(new ClusterShutdownMessage(false));
    }

    public ClusterStateMessage getClusterState() {
        return state;
    }

    /**
     * Convenience method, this is used in logging
     */
    public String getClusterId() {
        return remoteHost;
    }

    /**
     * @return the timeLastMessageReceived
     */
    public long getTimeLastMessageReceived() {
        return timeLastMessageReceived;
    }
}