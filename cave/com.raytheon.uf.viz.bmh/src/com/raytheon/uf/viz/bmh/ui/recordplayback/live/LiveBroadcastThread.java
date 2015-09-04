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

import java.util.List;
import java.util.LinkedList;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.raytheon.uf.common.bmh.audio.AudioRegulationConfiguration;
import com.raytheon.uf.common.bmh.broadcast.BroadcastStatus;
import com.raytheon.uf.common.bmh.broadcast.ILiveBroadcastMessage;
import com.raytheon.uf.common.bmh.broadcast.LiveBroadcastCommand;
import com.raytheon.uf.common.bmh.broadcast.LiveBroadcastCommand.ACTION;
import com.raytheon.uf.common.bmh.broadcast.LiveBroadcastPlayCommand;
import com.raytheon.uf.common.bmh.broadcast.OnDemandBroadcastConstants.MSGSOURCE;
import com.raytheon.uf.viz.bmh.BMHServers;
import com.raytheon.uf.viz.bmh.comms.AbstractThreadedBroadcastCommsMgrCommunicator;
import com.raytheon.uf.viz.bmh.comms.CommsCommunicationException;
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
 * Nov 10, 2014 3630       bkowal      Re-factor to support on-demand broadcasting.
 * Nov 15, 2014 3630       bkowal      Extend AbstractThreadedBroadcastCommsMgrCommunicator.
 * Nov 17, 2014 3820       bkowal      Recognize a separate error state due to broadcast
 *                                     initialization failure.
 * Nov 17, 2014 3808       bkowal      Support broadcast live.
 * Nov 21, 2014 3845       bkowal      Use Transmitter Groups
 * Jul 15, 2015 4636       bkowal      Slightly increase the amount of audio that is accumulated.
 * Aug 25, 2015 4771       bkowal      Buffer delays are now configurable.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class LiveBroadcastThread extends
        AbstractThreadedBroadcastCommsMgrCommunicator implements
        IAudioRecorderListener {

    public static enum BROADCAST_STATE {
        INITIALIZING, LIVE, STOPPED, ERROR, INIT_ERROR
    }

    private final ILiveBroadcastMessage command;

    private IBroadcastStateListener listener;

    private volatile BROADCAST_STATE state;

    private String broadcastId;

    private List<byte[]> bufferedAudio;

    /** Audio streaming timer. */
    private ScheduledExecutorService timer;

    private final int initialBufferDelay;

    private final int bufferDelay;

    /**
     * 
     */
    public LiveBroadcastThread(final ILiveBroadcastMessage command,
            AudioRegulationConfiguration configuration) {
        super(LiveBroadcastThread.class.getName());
        this.command = command;
        this.state = BROADCAST_STATE.INITIALIZING;
        this.bufferedAudio = new LinkedList<>();
        this.initialBufferDelay = configuration.getInitialBufferDelay();
        this.bufferDelay = configuration.getBufferDelay();
    }

    @Override
    public void run() {
        while (this.state != BROADCAST_STATE.STOPPED
                && this.state != BROADCAST_STATE.ERROR
                && this.state != BROADCAST_STATE.INIT_ERROR) {
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
                case INIT_ERROR:
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

        if (this.state == BROADCAST_STATE.ERROR
                || this.state == BROADCAST_STATE.INIT_ERROR) {
            /*
             * stop the recording which will trigger the proper shutdown of
             * everything else.
             */
            this.notifyListener();
            return;
        }

        if (this.state != BROADCAST_STATE.INIT_ERROR) {
            this.closeCommsConnection();
        }
    }

    private void initialize() {
        try {
            this.openCommsConnection();
        } catch (CommsCommunicationException e1) {
            statusHandler.error("Failed to connect to the Comms Manager!", e1);
            this.state = BROADCAST_STATE.INIT_ERROR;
            return;
        }

        try {
            this.writeToCommsManager(this.command);
        } catch (BroadcastException e) {
            statusHandler.error("Failed to start live broadcast!", e);
            this.state = BROADCAST_STATE.INIT_ERROR;
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
                this.state = BROADCAST_STATE.INIT_ERROR;
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
                        initialBufferDelay, bufferDelay, TimeUnit.MILLISECONDS);
            } else if (status.getStatus() == false) {
                this.state = BROADCAST_STATE.INIT_ERROR;
                this.notifyListener();
                statusHandler.error("Failed to start live broadcast! REASON = "
                        + status.getMessage(), status.getException());
            }
        } else {
            BroadcastException exc = new BroadcastException(
                    "Received unexpected message type during the initialization phase: "
                            + responseObject.getClass().getName() + ".");
            statusHandler.error("Failed to start live broadcast!", exc);
            this.state = BROADCAST_STATE.INIT_ERROR;
        }
    }

    public void halt() {
        if (this.state == BROADCAST_STATE.INIT_ERROR) {
            /*
             * nothing to shutdown because nothing was ever started due to the
             * failed initialization.
             */
            return;
        }

        if (this.timer != null) {
            this.timer.shutdown();
        }

        LiveBroadcastCommand command = new LiveBroadcastCommand();
        command.setBroadcastId(this.broadcastId);
        command.setMsgSource(MSGSOURCE.VIZ);
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
            playCommand.setMsgSource(MSGSOURCE.VIZ);
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