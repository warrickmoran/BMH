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
package com.raytheon.bmh.comms.broadcast;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raytheon.bmh.comms.AbstractServerThread;
import com.raytheon.bmh.comms.DacTransmitKey;
import com.raytheon.bmh.comms.cluster.ClusterServer;
import com.raytheon.bmh.comms.dactransmit.DacTransmitServer;
import com.raytheon.uf.common.bmh.broadcast.ILiveBroadcastMessage;
import com.raytheon.uf.common.bmh.broadcast.LiveBroadcastStartCommand;
import com.raytheon.uf.common.serialization.SerializationUtil;
import com.raytheon.uf.edex.bmh.comms.CommsConfig;

/**
 * Listens for and handles @{link StartLiveBroadcastRequest}. Creates a
 * {@link BroadcastStreamTask} to actually manage a live broadcast.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 8, 2014  3656       bkowal      Initial creation
 * Oct 15, 2014 3655       bkowal      Support live broadcasting to the DAC.
 * Oct 21, 2014 3655       bkowal      Use the new message types. Improved
 *                                     error handling.
 * 
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

/*
 * TODO: still need to finalize clustering implementation. Need access to
 * additional hardware to validate, verify and optimize implementation.
 */

public class BroadcastStreamServer extends AbstractServerThread {

    private static final Logger logger = LoggerFactory
            .getLogger(BroadcastStreamServer.class);

    private final ClusterServer clusterServer;

    private final DacTransmitServer transmitServer;

    private ConcurrentMap<String, BroadcastStreamTask> broadcastStreamingTasksMap;

    /*
     * TODO: will need to update the implementation if the message directory is
     * ever updated to be keyed by id.
     */

    private ConcurrentMap<String, DacTransmitKey> availableDacConnectionsMap;

    /**
     * @param port
     * @throws IOException
     */
    public BroadcastStreamServer(ClusterServer clusterServer,
            final DacTransmitServer transmitServer, CommsConfig config)
            throws IOException {
        super(config.getBroadcastLivePort());
        this.clusterServer = clusterServer;
        this.transmitServer = transmitServer;
        this.broadcastStreamingTasksMap = new ConcurrentHashMap<>();
        this.availableDacConnectionsMap = new ConcurrentHashMap<>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.bmh.comms.AbstractServerThread#handleConnection(java.net
     * .Socket)
     */
    @Override
    protected void handleConnection(Socket socket) throws Exception {
        LiveBroadcastStartCommand command = SerializationUtil
                .transformFromThrift(LiveBroadcastStartCommand.class,
                        socket.getInputStream());

        logger.info("Handling {} request.", command.getClass().getName());

        this.startBroadcastTask(socket, command);
    }

    @Override
    public void shutdown() {
        super.shutdown();
        for (BroadcastStreamTask task : this.broadcastStreamingTasksMap
                .values()) {
            task.shutdown();
            try {
                task.join();
            } catch (InterruptedException e) {
                logger.warn(
                        "Interrupted while waiting for broadcast task {} to shutdown.",
                        task.getName());
            }
        }
    }

    /**
     * {@link BroadcastStreamServer#dacConnected()} and
     * {@link BroadcastStreamServer#dacDisconnected()} are only interested in
     * local activity because the {@link DacTransmitCommunicator} currently only
     * allows for local connections.
     */
    public void dacConnected(final DacTransmitKey key, final String group) {
        if (this.availableDacConnectionsMap.containsKey(group)) {
            logger.info(
                    "Dac connection to transmitter {} has been replaced with connection: {}.",
                    group, key.toString());
            this.availableDacConnectionsMap.replace(group, key);
        } else {
            logger.info("Adding transmitter {} connection: {}.", group,
                    key.toString());
            this.availableDacConnectionsMap.put(group, key);
        }
    }

    public void dacDisconnected(final DacTransmitKey key, final String group) {
        this.availableDacConnectionsMap.remove(group, key);
    }

    public DacTransmitKey getLocalDacCommunicationKey(final String group) {
        return this.availableDacConnectionsMap.get(group);
    }

    private void startBroadcastTask(Socket socket,
            LiveBroadcastStartCommand command) {
        BroadcastStreamTask task = new BroadcastStreamTask(socket, command,
                this, this.clusterServer, this.transmitServer);
        task.start();

        logger.info("Started broadcast streaming task {}.", task.getName());
        this.broadcastStreamingTasksMap.put(task.getName(), task);
    }

    public void broadcastTaskFinished(final String broadcastId) {
        this.broadcastStreamingTasksMap.remove(broadcastId);
    }

    public void handleDacBroadcastMsg(ILiveBroadcastMessage msg) {
        BroadcastStreamTask task = this.broadcastStreamingTasksMap.get(msg
                .getBroadcastId());
        if (task == null) {
            logger.warn(
                    "Ignoring dac live broadcast {} msg. Broadcast {} does not exist or is no longer active.",
                    msg.getClass().getName(), msg.getBroadcastId());

            return;
        }
        task.handleDacBroadcastMsg(msg);
    }
}