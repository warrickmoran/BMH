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
package com.raytheon.uf.viz.bmh.ui.recordplayback.live;

import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;

import com.raytheon.uf.common.bmh.comms.BroadcastAudioRequest;
import com.raytheon.uf.common.bmh.comms.LiveBroadcastStopRequest;
import com.raytheon.uf.common.bmh.comms.StartLiveBroadcastRequest;
import com.raytheon.uf.common.bmh.comms.LiveBroadcastClientStatus;
import com.raytheon.uf.common.bmh.comms.LiveBroadcastClientStatus.STATUS;
import com.raytheon.uf.common.serialization.SerializationException;
import com.raytheon.uf.common.serialization.SerializationUtil;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.bmh.BMHServers;
import com.raytheon.uf.viz.bmh.ui.recordplayback.IAudioRecorderListener;
import com.raytheon.uf.viz.core.VizServers;

/**
 * Attempts to start the live broadcast and streams the audio to the comms
 * manager after a successful start to the live broadcast.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 9, 2014  3656       bkowal      Initial creation
 * Oct 15, 2014 3655       bkowal      Update for msg type renaming.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class LiveBroadcastThread extends Thread implements
        IAudioRecorderListener {

    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(LiveBroadcastThread.class);

    public static enum BROADCAST_STATE {
        INITIALIZING, LIVE, STOPPED, ERROR
    }

    private final StartLiveBroadcastRequest request;

    private IBroadcastStateListener listener;

    private volatile BROADCAST_STATE state;

    private Socket socket;

    private String broadcastId;

    /**
     * 
     */
    public LiveBroadcastThread(final StartLiveBroadcastRequest request) {
        super(LiveBroadcastThread.class.getName());
        this.request = request;
        this.state = BROADCAST_STATE.INITIALIZING;
    }

    @Override
    public void run() {
        while (this.state != BROADCAST_STATE.STOPPED
                && this.state != BROADCAST_STATE.ERROR) {
            try {
                this.notifyListener();
                switch (this.state) {
                case INITIALIZING:
                    this.initialize();
                    break;
                case LIVE:
                    try {
                        /*
                         * will eventually consist of status updates as well as
                         * notifications of any problems that occur from: comms
                         * manager <-> dac transmit
                         */
                        Object object = this.readFromCommsManager();
                        if (object == null
                                && this.state != BROADCAST_STATE.ERROR
                                && this.state != BROADCAST_STATE.STOPPED) {
                            statusHandler
                                    .error("comms manager "
                                            + BMHServers.BROADCAST_SERVER
                                            + " unexpectedly dropped the connection during the live broadcast!");
                            this.state = BROADCAST_STATE.ERROR;
                        }
                    } catch (BroadcastException e) {
                        if (this.state != BROADCAST_STATE.ERROR
                                && this.state != BROADCAST_STATE.STOPPED) {
                            statusHandler
                                    .error("Unexpected error occurred during the live broadcast!",
                                            e);
                            this.state = BROADCAST_STATE.ERROR;
                        }
                    }
                    break;
                case STOPPED:
                case ERROR:
                    break;
                }
            } catch (Exception e) {
                statusHandler.error(
                        "Unexpected error occurred during the live broadcast!",
                        e);
                this.state = BROADCAST_STATE.ERROR;
            }
        }

        if (this.socket != null && this.socket.isClosed() == false) {
            try {
                this.socket.close();
            } catch (IOException e) {
                statusHandler.error("Failed to close socket connection!", e);
            }
        }
    }

    private void initialize() {
        String commsLoc = VizServers.getInstance().getServerLocation(
                BMHServers.BROADCAST_SERVER);
        if (commsLoc == null) {
            Exception e = new IllegalStateException(
                    "No address has been specified for comms manager "
                            + BMHServers.BROADCAST_SERVER + ".");
            statusHandler.error("Failed to start live broadcast!", e);
            this.state = BROADCAST_STATE.ERROR;
            return;
        }

        URI commsURI = null;
        try {
            commsURI = new URI(commsLoc);
        } catch (URISyntaxException e) {
            Exception exc = new IllegalStateException(
                    "Invalid address specified for comms manager "
                            + BMHServers.BROADCAST_SERVER + ": " + commsLoc
                            + ".", e);
            statusHandler.error("Failed to start live broadcast!", exc);
            this.state = BROADCAST_STATE.ERROR;
            return;
        }

        try {
            this.socket = new Socket(commsURI.getHost(), commsURI.getPort());
            this.socket.setTcpNoDelay(true);
        } catch (IOException e) {
            Exception exc = new BroadcastException(
                    "Failed to connect to comms manager "
                            + BMHServers.BROADCAST_SERVER + ": " + commsLoc
                            + ".", e);
            statusHandler.error("Failed to start live broadcast!", exc);
            this.state = BROADCAST_STATE.ERROR;
            return;
        }

        try {
            this.writeToCommsManager(this.request);
        } catch (BroadcastException e) {
            statusHandler.error("Failed to start live broadcast!", e);
            this.state = BROADCAST_STATE.ERROR;
            return;
        }

        // Wait for live broadcast initialization success or failure ...
        /*
         * A maximum of a 10 second delay is predicted when there are same tones
         * that need to be played.
         */
        Object responseObject;
        try {
            responseObject = this.readFromCommsManager();
        } catch (BroadcastException e) {
            statusHandler.error("Failed to start live broadcast!", e);
            this.state = BROADCAST_STATE.ERROR;
            return;
        }

        if (responseObject instanceof LiveBroadcastClientStatus) {
            LiveBroadcastClientStatus response = (LiveBroadcastClientStatus) responseObject;
            if (response.getStatus() == STATUS.READY) {
                this.state = BROADCAST_STATE.LIVE;
                this.broadcastId = response.getBroadcastId();
            } else if (response.getStatus() == STATUS.FAILED) {
                this.state = BROADCAST_STATE.ERROR;
                this.notifyListener();
                statusHandler.error("Failed to start live broadcast! REASON = "
                        + response.getDetail());
            }
        } else {
            BroadcastException exc = new BroadcastException(
                    "Received unexpected message type during the initialization phase: "
                            + responseObject.getClass().getName() + ".");
            statusHandler.error("Failed to start live broadcast!", exc);
            this.state = BROADCAST_STATE.ERROR;
        }
    }

    private synchronized void writeToCommsManager(Object object)
            throws BroadcastException {
        try {
            SerializationUtil.transformToThriftUsingStream(object,
                    this.socket.getOutputStream());
        } catch (SerializationException | IOException e) {
            throw new BroadcastException(
                    "Failed to send data to comms manager "
                            + BMHServers.BROADCAST_SERVER + ".", e);
        }
    }

    private Object readFromCommsManager() throws BroadcastException {
        Object object = null;
        try {
            object = SerializationUtil.transformFromThrift(Object.class,
                    this.socket.getInputStream());
        } catch (SerializationException | IOException e) {
            throw new BroadcastException(
                    "Failed to receive data from comms manager "
                            + BMHServers.BROADCAST_SERVER + ".", e);
        }
        return object;
    }

    public void halt() {
        LiveBroadcastStopRequest request = new LiveBroadcastStopRequest();
        request.setBroadcastId(this.broadcastId);

        this.state = BROADCAST_STATE.STOPPED;
        try {
            this.writeToCommsManager(request);
        } catch (BroadcastException e) {
            statusHandler.error("Failed to stop live broadcast!", e);
            this.state = BROADCAST_STATE.ERROR;
            return;
        }
    }

    private void notifyListener() {
        if (this.listener != null) {
            this.listener.stateChanged(this.state);
        }
    }

    /**
     * @param listener
     *            the listener to set
     */
    public void setListener(IBroadcastStateListener listener) {
        this.listener = listener;
    }

    @Override
    public void audioReady(byte[] audioData) {
        BroadcastAudioRequest request = new BroadcastAudioRequest();
        request.setBroadcastId(this.broadcastId);
        request.setAudioData(audioData);

        try {
            this.writeToCommsManager(request);
        } catch (BroadcastException e) {
            statusHandler.error(
                    "Failed to stream audio during live broadcast!", e);
            this.state = BROADCAST_STATE.ERROR;
        }
    }
}