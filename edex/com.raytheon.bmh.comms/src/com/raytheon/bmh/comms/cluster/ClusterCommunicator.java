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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raytheon.bmh.comms.CommsManager;
import com.raytheon.bmh.comms.DacTransmitKey;
import com.raytheon.bmh.comms.cluster.ClusterStateMessage.ClusterDacTransmitKey;
import com.raytheon.uf.common.serialization.SerializationException;
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
 * 
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

    public ClusterCommunicator(CommsManager manager, Socket socket) {
        super("ClusterCommunicator-" + socket.getRemoteSocketAddress());
        this.socket = socket;
        this.manager = manager;
    }

    @Override
    public void run() {
        while (!socket.isClosed()) {
            try {
                Object message = SerializationUtil.transformFromThrift(
                        Object.class, socket.getInputStream());
                handleMessage(message);
            } catch (SerializationException | IOException e) {
                logger.error("Error reading message from cluster member: {}",
                        getClusterId(), e);
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
                    getClusterId(), newState.getKeys().size());
            if (oldState == null) {
                for (ClusterDacTransmitKey key : newState.getKeys()) {
                    manager.dacConnectedRemote(key.toKey());
                }
            } else {
                for (ClusterDacTransmitKey key : newState.getKeys()) {
                    if (!oldState.contains(key)) {
                        manager.dacConnectedRemote(key.toKey());
                    }
                }
                for (ClusterDacTransmitKey key : oldState.getKeys()) {
                    if (!newState.contains(key)) {
                        manager.dacDisconnectedRemote();
                        break;
                    }
                }
            }
        } else if (message instanceof ClusterShutdownMessage) {
            if (((ClusterShutdownMessage) message).isAcknowledged()) {
                disconnect();
            } else {
                logger.info("Clustered manager {} is shutting down.",
                        getClusterId());
                send(new ClusterShutdownMessage(true));
                disconnect();
            }
        } else {
            logger.error("Unexpected message from cluster member of type: {}",
                    message.getClass().getSimpleName());
        }
    }

    private void send(Object toSend) {
        synchronized (sendLock) {
            try {
                SerializationUtil.transformToThriftUsingStream(toSend,
                        socket.getOutputStream());
            } catch (SerializationException | IOException e) {
                logger.error("Error communicating with cluster member: {}",
                        getClusterId(), e);
            }
        }
    }

    public void sendState(ClusterStateMessage state) {
        send(state);
    }

    public void disconnect() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            logger.error("Error disconnecting from cluster member: {}",
                    getClusterId(), e);
        }
        if (state != null) {
            if (!state.getKeys().isEmpty()) {
                manager.dacDisconnectedRemote();
            }
        }
    }

    public boolean isConnected(DacTransmitKey key) {
        if (state == null || socket.isClosed()) {
            return false;
        }
        return state.contains(key);
    }

    public void shutdown() {
        send(new ClusterShutdownMessage(false));
    }

    /**
     * Convenience method, this is used in logging
     */
    private String getClusterId() {
        return socket.getInetAddress().getHostAddress();
    }
}