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
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raytheon.bmh.comms.cluster.ClusterServer;
import com.raytheon.bmh.comms.dactransmit.DacTransmitServer;
import com.raytheon.uf.common.bmh.comms.BroadcastAudioRequest;
import com.raytheon.uf.common.bmh.comms.BroadcastProblemMsg;
import com.raytheon.uf.common.bmh.comms.LiveBroadcastStopRequest;
import com.raytheon.uf.common.bmh.comms.StartLiveBroadcastRequest;
import com.raytheon.uf.common.bmh.comms.StartLiveBroadcastResponse;
import com.raytheon.uf.common.bmh.comms.StartLiveBroadcastResponse.STATUS;
import com.raytheon.uf.common.serialization.SerializationUtil;

/**
 * Used to manage a live broadcast. Executes the SAME tones and will continue
 * streaming live audio to the Dac until the conclusion of the broadcast after
 * successful initialization.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 9, 2014  3656       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class BroadcastStreamTask extends Thread {

    private static final Logger logger = LoggerFactory
            .getLogger(BroadcastStreamTask.class);

    private final Socket socket;

    // TO BE CONTINUED ... DR #3655
    private final BroadcastStreamServer streamingServer;

    private final ClusterServer clusterServer;

    private final DacTransmitServer dacServer;

    private final StartLiveBroadcastRequest request;

    private volatile boolean live;

    /**
     * 
     */
    public BroadcastStreamTask(final Socket socket,
            final StartLiveBroadcastRequest request,
            final BroadcastStreamServer streamingServer,
            final ClusterServer clusterServer, final DacTransmitServer dacServer) {
        super(determineName(request));
        this.socket = socket;
        this.streamingServer = streamingServer;
        this.clusterServer = clusterServer;
        this.dacServer = dacServer;
        this.request = request;
    }

    private static String determineName(final StartLiveBroadcastRequest request) {
        return request.getBroadcastId() == null ? request.getWsid() + "-"
                + UUID.randomUUID().toString().toUpperCase() : request
                .getBroadcastId();
    }

    @Override
    public void run() {
        /*
         * First need to notify other Comms Managers of the existence of the
         * streaming task.
         */
        this.request.setBroadcastId(this.getName());
        this.clusterServer.sendDataToAll(this.request);

        /*
         * Wait for the ready confirmation from other comms manager(s). If we do
         * not have access to all involved dacs, verify that the remaining dacs
         * have been covered by other comms manager(s).
         * 
         * If recipient count from sendDataToAll is 0, then we have nothing else
         * to wait on. However, if we cannot support all specified transmitters,
         * then we enter an error condition.
         * 
         * Ideally, the user would not be able to select a disabled transmitter.
         */

        StartLiveBroadcastResponse response = new StartLiveBroadcastResponse();

        try {
            // TO BE CONTINUED ... SAME TONES -> DR #3655
            Thread.sleep(40000);
        } catch (InterruptedException e1) {
            // Do Nothing.
        }

        response.setBroadcastId(this.getName());
        response.setStatus(STATUS.READY);
        this.live = true;

        try {
            SerializationUtil.transformToThriftUsingStream(response,
                    this.socket.getOutputStream());
        } catch (Throwable e) {
            this.notifyProblem(e);
        }

        while (this.live) {
            Object object = null;
            try {
                object = SerializationUtil.transformFromThrift(Object.class,
                        this.socket.getInputStream());
            } catch (Exception e) {
                this.notifyProblem(e);
            }
            this.handleMessageInternal(object);
        }

        /*
         * TODO: may need to handle the socket connection differently when we
         * are spawn rather than the original. May end up abstracting cluster
         * aspects to allow for multiple components to utilize the connections
         * that are formed between clustered components without potentially
         * interfering with each other.
         */
        logger.info("Broadcast {} is shutting down ...", this.getName());
        try {
            this.socket.close();
        } catch (IOException e) {
            logger.warn("Failed to close the socket connection (Broadcast "
                    + this.getName() + ")!", e);
        }
        this.streamingServer.broadcastTaskFinished(this.getName());
    }

    private void notifyProblem(Throwable e) {
        /*
         * attempt to notify other comms manager instances.
         */
        BroadcastProblemMsg msg = new BroadcastProblemMsg();
        msg.setBroadcastId(this.getName());
        if (e.getMessage() == null) {
            msg.setDetail(e.getLocalizedMessage());
        } else {
            msg.setDetail(e.getMessage());
        }
        this.clusterServer.sendDataToAll(msg);

        /*
         * dropping the connection will propagate back up to the Live Broadcast
         * Thread running on the client machine.
         */
        this.live = false;
    }

    private void streamDacAudio(byte[] audioData) {
        logger.info("Streaming {} bytes of audio to the dac (Broadcast {}).",
                audioData.length, this.getName());

        // TO BE CONTINUED ... -> DR #3655
    }

    private void handleMessageInternal(Object object) {
        if (object == null) {
            return;
        }
        logger.info("Handling {} request (Broadcast {}).", object.getClass()
                .getName(), this.getName());
        if (object instanceof BroadcastAudioRequest) {
            this.clusterServer.sendDataToAll(object);
            this.handleAudioRequest((BroadcastAudioRequest) object);
        } else if (object instanceof LiveBroadcastStopRequest) {
            this.clusterServer.sendDataToAll(object);
            this.handleStopRequest((LiveBroadcastStopRequest) object);
        }
    }

    public void handleAudioRequest(BroadcastAudioRequest request) {
        /*
         * Verify that the request is actually for us.
         */
        if (request.getBroadcastId().equals(this.getName()) == false) {
            return;
        }
        this.clusterServer.sendDataToAll(request);
        this.streamDacAudio(request.getAudioData());
    }

    public void handleStopRequest(LiveBroadcastStopRequest request) {
        /*
         * Verify that the request is actually for us.
         */
        if (request.getBroadcastId().equals(this.getName()) == false) {
            return;
        }
        this.clusterServer.sendDataToAll(request);
        this.live = false;
    }

    public void handleRemoteReadyNotification(
            final StartLiveBroadcastResponse response) {
        // TODO: part of full cluster support implementation.
        // Could potentially be eliminated.
    }

    public void shutdown() {
        /*
         * should shutdown wait until the live broadcasts have ended (max: 2
         * minutes)?
         */
        this.live = false;
    }
}