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
import com.raytheon.bmh.comms.cluster.ClusterServer;
import com.raytheon.bmh.comms.cluster.IClusterMessageListener;
import com.raytheon.bmh.comms.dactransmit.DacTransmitServer;
import com.raytheon.uf.common.bmh.comms.StartLiveBroadcastRequest;
import com.raytheon.uf.common.bmh.comms.StartLiveBroadcastResponse;
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

public class BroadcastStreamServer extends AbstractServerThread implements
        IClusterMessageListener {

    private static final Logger logger = LoggerFactory
            .getLogger(BroadcastStreamServer.class);

    private final ClusterServer clusterServer;

    private final DacTransmitServer transmitServer;

    private ConcurrentMap<String, BroadcastStreamTask> broadcastStreamingTasks;

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
        this.broadcastStreamingTasks = new ConcurrentHashMap<>();
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
        StartLiveBroadcastRequest request = SerializationUtil
                .transformFromThrift(StartLiveBroadcastRequest.class,
                        socket.getInputStream());

        logger.info("Handling {} request from {}.", request.getClass()
                .getName(), request.getWsid());

        this.startBroadcastTask(socket, request);
    }

    @Override
    public void shutdown() {
        super.shutdown();
        for (BroadcastStreamTask task : this.broadcastStreamingTasks.values()) {
            task.shutdown();
            try {
                task.join();
            } catch (InterruptedException e) {
                // Ignore.
            }
        }
    }

    private void startBroadcastTask(Socket socket,
            StartLiveBroadcastRequest request) {
        BroadcastStreamTask task = new BroadcastStreamTask(socket, request,
                this, this.clusterServer, this.transmitServer);
        task.start();

        logger.info("Started broadcast streaming task {} for {}.",
                task.getName(), request.getWsid());
        this.broadcastStreamingTasks.put(task.getName(), task);
    }

    public void broadcastTaskFinished(final String broadcastId) {
        this.broadcastStreamingTasks.remove(broadcastId);
    }

    private void handleRemoteBroadcastTaskReady(
            final StartLiveBroadcastResponse response) {
        if (this.broadcastStreamingTasks.containsKey(response.getBroadcastId()) == false) {
            return;
        }

        this.broadcastStreamingTasks.get(response.getBroadcastId())
                .handleRemoteReadyNotification(response);
    }

    @Override
    public void clusterMessageReceived(Socket socket, Object object) {
        if (object instanceof StartLiveBroadcastRequest) {
            this.startBroadcastTask(socket, (StartLiveBroadcastRequest) object);
        } else if (object instanceof StartLiveBroadcastResponse) {
            this.handleRemoteBroadcastTaskReady((StartLiveBroadcastResponse) object);
        }
    }
}