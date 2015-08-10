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
import com.raytheon.bmh.comms.DacTransmitKey;
import com.raytheon.bmh.comms.cluster.ClusterStateMessage.ClusterDacTransmitKey;
import com.raytheon.uf.common.bmh.broadcast.ILiveBroadcastMessage;
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

    private ClusterStateMessage state;

    private final String remoteHost;

    private volatile long timeLastMessageReceived = System.currentTimeMillis();

    public ClusterCommunicator(CommsManager manager, Socket socket,
            String remoteHost) {
        super("ClusterCommunicator-" + remoteHost);
        this.socket = socket;
        this.manager = manager;
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
                    logger.error("Lost connection to cluster member: {}",
                            remoteHost);
                } else {
                    logger.error(
                            "Error reading message from cluster member: {}",
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
                    remoteHost, newState.getKeys().size());
            if (oldState == null) {
                for (ClusterDacTransmitKey key : newState.getKeys()) {
                    manager.dacConnectedRemote(key.toKey());
                }
                if (newState.hasRequestedKey()) {
                    logger.info(
                            "Clustered manager {} has requested a dac transmit.",
                            remoteHost, newState.getKeys().size());
                    manager.dacRequestedRemote(newState.getRequestedKey()
                            .toKey());
                }
            } else {
                for (ClusterDacTransmitKey key : newState.getKeys()) {
                    if (!oldState.contains(key)) {
                        manager.dacConnectedRemote(key.toKey());
                    }
                }
                for (ClusterDacTransmitKey key : oldState.getKeys()) {
                    if (!newState.contains(key)) {
                        manager.dacDisconnectedRemote(key.toKey());
                    }
                }
                if (newState.hasRequestedKey()
                        && !oldState.isRequestedKey(newState.getRequestedKey())) {
                    logger.info(
                            "Clustered manager {} has requested a dac transmit.",
                            remoteHost, newState.getKeys().size());
                    manager.dacRequestedRemote(newState.getRequestedKey()
                            .toKey());
                }
            }
        } else if (message instanceof ClusterShutdownMessage) {
            if (((ClusterShutdownMessage) message).isAcknowledged()) {
                disconnect();
            } else {
                logger.info("Clustered manager {} is shutting down.",
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
                        "Received wrong host for heartbeat.  Expected {} received {}.  Closing connection to {}",
                        remoteHost, heartbeatIp, remoteHost);
                disconnect();
            }
        } else {
            logger.error("Unexpected message from cluster member of type: {}",
                    message.getClass().getSimpleName());
        }
    }

    public boolean send(Object toSend) {
        synchronized (sendLock) {
            if (this.isConnected() == false) {
                // No usable connection. Initiate cleanup.
                this.disconnect();
                logger.error("No active connection(s) to cluster member: {}.",
                        this.remoteHost);
                return false;
            }
            try {
                SerializationUtil.transformToThriftUsingStream(toSend,
                        socket.getOutputStream());
            } catch (Throwable e) {
                logger.error("Error communicating with cluster member: {}",
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

    private boolean isConnected() {
        return (this.socket != null) && (this.socket.isClosed() == false)
                && (this.socket.isOutputShutdown() == false);
    }

    /**
     * True if the remote server has accepted the connection and the connection
     * is still valid.
     * 
     * @return
     */
    public boolean remoteAccepted() {
        return state != null && isConnected();
    }

    public void disconnect() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            logger.error("Error disconnecting from cluster member: {}",
                    remoteHost, e);
        }
        if (state != null) {
            for (ClusterDacTransmitKey key : state.getKeys()) {
                manager.dacDisconnectedRemote(key.toKey());
            }
        }
    }

    public boolean isConnected(DacTransmitKey key) {
        if ((state == null) || socket.isClosed()) {
            return false;
        }
        return state.contains(key);
    }

    public boolean isRequested(DacTransmitKey key) {
        if ((state == null) || socket.isClosed()) {
            return false;
        }
        return state.isRequestedKey(key);
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