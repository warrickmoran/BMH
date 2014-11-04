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
import java.util.List;
import java.util.LinkedList;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.raytheon.uf.common.bmh.broadcast.BroadcastStatus;
import com.raytheon.uf.common.bmh.broadcast.ILiveBroadcastMessage;
import com.raytheon.uf.common.bmh.broadcast.LiveBroadcastCommand;
import com.raytheon.uf.common.bmh.broadcast.LiveBroadcastCommand.ACTION;
import com.raytheon.uf.common.bmh.broadcast.LiveBroadcastPlayCommand;
import com.raytheon.uf.common.serialization.SerializationException;
import com.raytheon.uf.common.serialization.SerializationUtil;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.bmh.BMHServers;
import com.raytheon.uf.viz.bmh.ui.recordplayback.IAudioRecorderListener;

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
 * Oct 17, 2014 3687       bsteffen    Support practice servers.
 * Oct 21, 2014 3655       bkowal      Use the new message types. Improved error handling.
 * Nov 3, 2014  3655       bkowal      Cache live broadcast audio on the Viz side.
 * Nov 4, 2014  3655       bkowal      Eliminate audio echo. Decrease buffer delay.
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

    private final ILiveBroadcastMessage command;

    private IBroadcastStateListener listener;

    private volatile BROADCAST_STATE state;

    private Socket socket;

    private String broadcastId;

    private List<byte[]> bufferedAudio;

    /** Audio streaming timer. */
    private ScheduledExecutorService timer;

    /*
     * Save 0.25 seconds of audio before initial broadcast stream.
     */
    private static final int INITIAL_BUFFER_DELAY = 250;

    /*
     * Accumulate audio for every 50ms afterwards until the end of the
     * broadcast.
     */
    private static final int BUFFER_DELAY = 50;

    /**
     * 
     */
    public LiveBroadcastThread(final ILiveBroadcastMessage command) {
        super(LiveBroadcastThread.class.getName());
        this.command = command;
        this.state = BROADCAST_STATE.INITIALIZING;
        this.bufferedAudio = new LinkedList<>();
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
                                            + BMHServers.getBroadcastServer()
                                            + " unexpectedly dropped the connection during the live broadcast!");
                            this.state = BROADCAST_STATE.ERROR;
                        } else if (object instanceof BroadcastStatus) {
                            BroadcastStatus status = (BroadcastStatus) object;
                            if (status.getStatus() == false) {
                                statusHandler.error(
                                        "The live broadcast has failed and must be terminated: "
                                                + status.getMessage() + "!",
                                        status.getException());
                                this.state = BROADCAST_STATE.ERROR;
                            }
                        } else {
                            statusHandler.warn("Received unexpected message "
                                    + object.getClass().getName()
                                    + " from comms manager.");
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
                    // Do Nothing.
                    break;
                }
            } catch (Exception e) {
                statusHandler.error(
                        "Unexpected error occurred during the live broadcast!",
                        e);
                this.state = BROADCAST_STATE.ERROR;
            }
        }

        if (this.state == BROADCAST_STATE.ERROR) {
            /*
             * stop the recording which will trigger the proper shutdown of
             * everything else.
             */
            this.notifyListener();
            return;
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
        String commsLoc = BMHServers.getBroadcastServer();
        if (commsLoc == null) {
            Exception e = new IllegalStateException(
                    "No address has been specified for comms manager "
                            + BMHServers.getBroadcastServerKey() + ".");
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
                            + BMHServers.getBroadcastServerKey() + ": "
                            + commsLoc + ".", e);
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
                            + BMHServers.getBroadcastServerKey() + ": "
                            + commsLoc + ".", e);
            statusHandler.error("Failed to start live broadcast!", exc);
            this.state = BROADCAST_STATE.ERROR;
            return;
        }

        try {
            this.writeToCommsManager(this.command);
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
            if (this.state != BROADCAST_STATE.STOPPED
                    && this.state != BROADCAST_STATE.ERROR) {
                statusHandler.error("Failed to start live broadcast!", e);
                this.state = BROADCAST_STATE.ERROR;
            }
            return;
        }

        if (responseObject instanceof BroadcastStatus) {
            BroadcastStatus status = (BroadcastStatus) responseObject;
            if (status.getStatus() == true) {
                this.state = BROADCAST_STATE.LIVE;
                this.broadcastId = status.getBroadcastId();
                this.timer = Executors.newSingleThreadScheduledExecutor();
                this.timer.scheduleWithFixedDelay(new ElapsedTimerTask(),
                        INITIAL_BUFFER_DELAY, BUFFER_DELAY,
                        TimeUnit.MILLISECONDS);
            } else if (status.getStatus() == false) {
                this.state = BROADCAST_STATE.ERROR;
                this.notifyListener();
                statusHandler.error("Failed to start live broadcast! REASON = "
                        + status.getMessage(), status.getException());
            }
        } else {
            BroadcastException exc = new BroadcastException(
                    "Received unexpected message type during the initialization phase: "
                            + responseObject.getClass().getName() + ".");
            statusHandler.error("Failed to start live broadcast!", exc);
            this.state = BROADCAST_STATE.ERROR;
        }
    }

    private synchronized void writeToCommsManager(Object broadcastMsg)
            throws BroadcastException {
        if (this.socket == null || this.socket.isClosed()) {
            return;
        }
        try {
            SerializationUtil.transformToThriftUsingStream(broadcastMsg,
                    this.socket.getOutputStream());
        } catch (SerializationException | IOException e) {
            throw new BroadcastException(
                    "Failed to send data to comms manager "
                            + BMHServers.getBroadcastServer() + ".", e);
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
                            + BMHServers.getBroadcastServer() + ".", e);
        }
        return object;
    }

    public void halt() {
        if (this.timer != null) {
            this.timer.shutdown();
        }

        LiveBroadcastCommand command = new LiveBroadcastCommand();
        command.setBroadcastId(this.broadcastId);
        command.setMsgSource(ILiveBroadcastMessage.SOURCE_VIZ);
        command.setTransmitterGroups(this.command.getTransmitterGroups());
        command.setAction(ACTION.STOP);

        this.state = BROADCAST_STATE.STOPPED;
        try {
            this.writeToCommsManager(command);
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
        if (this.state == BROADCAST_STATE.ERROR
                || this.state == BROADCAST_STATE.STOPPED) {
            return;
        }

        synchronized (this.bufferedAudio) {
            this.bufferedAudio.add(audioData);
        }
    }

    private void broadcastBufferedAudio() {
        synchronized (this.bufferedAudio) {
            if (this.bufferedAudio.isEmpty()) {
                return;
            }

            LiveBroadcastPlayCommand playCommand = new LiveBroadcastPlayCommand();
            playCommand.setMsgSource(ILiveBroadcastMessage.SOURCE_VIZ);
            playCommand.setBroadcastId(this.broadcastId);
            playCommand.setTransmitterGroups(this.command
                    .getTransmitterGroups());
            playCommand.setAudio(this.bufferedAudio);
            try {
                this.writeToCommsManager(playCommand);
            } catch (BroadcastException e) {
                statusHandler.error(
                        "Failed to stream audio during live broadcast!", e);
                this.state = BROADCAST_STATE.ERROR;
            }
            this.bufferedAudio.clear();
        }
    }

    /**
     * Elapsed timer task called when the timer fires.
     */
    private class ElapsedTimerTask extends TimerTask {
        @Override
        public void run() {
            broadcastBufferedAudio();
        }
    }
}