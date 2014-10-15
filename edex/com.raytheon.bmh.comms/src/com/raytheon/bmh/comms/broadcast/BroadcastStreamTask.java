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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raytheon.bmh.comms.DacTransmitKey;
import com.raytheon.bmh.comms.cluster.ClusterServer;
import com.raytheon.bmh.comms.dactransmit.DacTransmitServer;
import com.raytheon.uf.common.bmh.comms.BroadcastAudioRequest;
import com.raytheon.uf.common.bmh.comms.BroadcastProblemMsg;
import com.raytheon.uf.common.bmh.comms.LiveBroadcastStartData;
import com.raytheon.uf.common.bmh.comms.LiveBroadcastStopRequest;
import com.raytheon.uf.common.bmh.comms.StartLiveBroadcastRequest;
import com.raytheon.uf.common.bmh.comms.LiveBroadcastClientStatus;
import com.raytheon.uf.common.bmh.comms.LiveBroadcastClientStatus.STATUS;
import com.raytheon.uf.common.serialization.SerializationUtil;
import com.raytheon.uf.edex.bmh.dactransmit.ipc.IDacLiveBroadcastMsg;
import com.raytheon.uf.edex.bmh.dactransmit.ipc.LiveBroadcastAudioRequest;
import com.raytheon.uf.edex.bmh.dactransmit.ipc.PrepareLiveBroadcastRequest;
import com.raytheon.uf.edex.bmh.dactransmit.ipc.LiveBroadcastStatus;
import com.raytheon.uf.edex.bmh.dactransmit.ipc.StopLiveBroadcastRequest;
import com.raytheon.uf.edex.bmh.dactransmit.ipc.TriggerLiveBroadcast;

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
 * Oct 15, 2014 3655       bkowal      Support live broadcasting to the DAC.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class BroadcastStreamTask extends Thread {

    private static enum STATE {
        INIT, READY, TRIGGER, LIVE, STOP, ERROR
    }

    private static final Logger logger = LoggerFactory
            .getLogger(BroadcastStreamTask.class);

    private final Socket socket;

    private final BroadcastStreamServer streamingServer;

    private final ClusterServer clusterServer;

    private final DacTransmitServer dacServer;

    private final StartLiveBroadcastRequest request;

    private List<String> managedTransmitterGroups;

    private volatile boolean live;

    private CountDownLatch responseLock;

    private List<IDacLiveBroadcastMsg> responses;

    private volatile STATE state;

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
        this.managedTransmitterGroups = new ArrayList<>(this.request
                .getRequestedTransmitters().size());
    }

    private static String determineName(final StartLiveBroadcastRequest request) {
        return request.getBroadcastId() == null ? request.getWsid() + "-"
                + UUID.randomUUID().toString().toUpperCase() : request
                .getBroadcastId();
    }

    @Override
    public void run() {
        this.state = STATE.INIT;
        this.noteStateTransition();

        /*
         * First need to notify other Comms Managers of the existence of the
         * streaming task.
         */
        this.request.setBroadcastId(this.getName());
        int count = this.clusterServer.sendDataToAll(this.request);

        // Determine which transmitter(s) we are responsible for.
        /*
         * We should never have to worry about encountering duplicate
         * transmitters across multiple systems?
         * ..........................................
         */
        for (String transmitterGroup : this.request.getRequestedTransmitters()) {
            DacTransmitKey key = this.streamingServer
                    .getLocalDacCommunicationKey(transmitterGroup);
            if (key == null) {
                continue;
            }
            this.managedTransmitterGroups.add(transmitterGroup);
        }

        if (count > 0) {
            this.responseLock = new CountDownLatch(count);

            /*
             * Wait for the ready confirmation from other comms manager(s). If
             * we do not have access to all involved dacs, verify that the
             * remaining dacs have been covered by other comms manager(s).
             * 
             * Ideally, the user would not be able to select a disabled
             * transmitter.
             * 
             * TODO: This will need to be able to handle cluster member
             * shutdowns. Note: A cluster member shutdown may increase the
             * probability, that all required transmitters will not be accounted
             * for.
             */
            try {
                /*
                 * Wait for replies from the other cluster members. They need
                 * time to start their own tasks and determine which
                 * transmitters they will be responsible for managing.
                 */
                this.responseLock.await(6000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                // Ignore until clustering.
            }
        }

        /*
         * Every specified transmitter must be a transmitter that we can access
         * or that a different cluster member can access.
         * 
         * TODO: will need to separate handled specifically by this thread and
         * ALL that can be managed by the cluster members prior to completing
         * this verification when clustering is involved.
         */
        if (this.managedTransmitterGroups.size() != this.request
                .getRequestedTransmitters().size()) {
            final String clientMsg = this.buildMissingTransmittersMsg(
                    this.managedTransmitterGroups,
                    this.request.getRequestedTransmitters());

            // failure
            this.notifyClientProblem(clientMsg.toString());
            logger.error("Failed to start broadcast {}! {}", this.getName(),
                    clientMsg.toString());
            return;
        }

        this.state = STATE.READY;
        this.noteStateTransition();
        /*
         * at this point we know all required transmitters are available (at
         * least < 1 ms ago). So, we send another notification to the dac
         * transmitters to inform them that we are preparing to start a live
         * broadcast. If an interrupt occurs before all transmitters can be
         * notified, the live broadcast will fail.
         */

        for (String transmitterGroup : this.managedTransmitterGroups) {
            DacTransmitKey key = this.streamingServer
                    .getLocalDacCommunicationKey(transmitterGroup);
            if (key == null) {
                this.dacMsgFailed(transmitterGroup);
                continue;
            }
            LiveBroadcastStartData startData = this.request
                    .getLiveBroadcastDataMap().get(transmitterGroup);
            if (startData == null) {
                continue;
            }
            // TODO: simplify messaging with an ACTION enum.
            PrepareLiveBroadcastRequest request = new PrepareLiveBroadcastRequest();
            request.setBroadcastId(this.getName());
            request.setData(startData);

            this.dacServer.sendToDac(key, request);
        }

        this.responseLock = new CountDownLatch(
                this.managedTransmitterGroups.size());
        this.responses = new ArrayList<>(this.managedTransmitterGroups.size());
        if (this.verifyTransmitterAccess(3000) == false) {
            /*
             * the broadcast has failed. cleanup what has been started.
             */
            this.shutdownDacLiveBroadcasts();
        }

        /*
         * At this point, all transmitters are waiting for the signal to
         * proceed. Any incoming interrupt messages will be delayed because an
         * interrupt cannot interrupt an interrupt. Additionally, it will not be
         * possible to start other live broadcast messages from other
         * workstations on the same transmitter(s).
         */

        this.state = STATE.TRIGGER;
        this.noteStateTransition();
        for (String transmitterGroup : this.managedTransmitterGroups) {
            DacTransmitKey key = this.streamingServer
                    .getLocalDacCommunicationKey(transmitterGroup);
            if (key == null) {
                this.dacMsgFailed(transmitterGroup);
                continue;
            }
            TriggerLiveBroadcast trigger = new TriggerLiveBroadcast();
            trigger.setBroadcastId(this.request.getBroadcastId());
            this.dacServer.sendToDac(key, trigger);
        }

        this.responseLock = new CountDownLatch(
                this.managedTransmitterGroups.size());
        synchronized (this.responses) {
            this.responses.clear();
        }

        /*
         * Proceed signal is transmitted. SAME Tone playback begins. The delay
         * on the playback indicates when the data thread should be halted for
         * each transmitter.
         */

        // wait for the current broadcast to stop and tones to be played.
        // Extra time to allow for tone playback.
        if (this.verifyTransmitterAccess(20000) == false) {
            /*
             * the broadcast has failed. cleanup what has been started.
             */
            this.shutdownDacLiveBroadcasts();
            return;
        }

        /*
         * All SAME tones were submitted successfully. Notify the client and
         * prepare to start streaming audio.
         */
        LiveBroadcastClientStatus response = new LiveBroadcastClientStatus();
        response.setBroadcastId(this.getName());
        response.setStatus(STATUS.READY);
        response.setTransmitterGroups(this.managedTransmitterGroups
                .toArray(new String[0]));

        this.state = STATE.LIVE;
        this.noteStateTransition();
        this.live = this.sendClientReplyMessage(response);
        while (this.live) {
            Object object = null;
            try {
                object = SerializationUtil.transformFromThrift(Object.class,
                        this.socket.getInputStream());
            } catch (Exception e) {
                this.notifyMembersProblem(e);
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
        this.state = STATE.STOP;
        this.noteStateTransition();
        logger.info("Broadcast {} is shutting down ...", this.getName());
        try {
            this.socket.close();
        } catch (IOException e) {
            logger.warn("Failed to close the socket connection (Broadcast "
                    + this.getName() + ")!", e);
        }
        this.streamingServer.broadcastTaskFinished(this.getName());
    }

    private void noteStateTransition() {
        logger.info("Broadcast {} state transition to: {}", this.getName(),
                this.state.toString());
    }

    private boolean verifyTransmitterAccess(long timeout) {
        boolean ready = false;
        // TODO: wait with timeout. Do not want to allow unlimited time.
        try {
            ready = this.responseLock.await(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            // Ignore.
        }

        if (ready == false) {
            logger.error("Failed to receive a response from all transmitters in a reasonable amount of time.");
            this.notifyClientProblem("Failed to receive a response from all transmitters in a reasonable amount of time.");
            this.notifyMembersProblem("Failed to receive a response from all transmitters in a reasonable amount of time.");
            return ready;
        }

        List<String> readyTransmitters = new ArrayList<String>(
                this.managedTransmitterGroups.size());
        List<String> clientResponseMsgs = new ArrayList<>(this.responses.size());
        // analyze the responses to our latest request.
        synchronized (this.responses) {
            for (IDacLiveBroadcastMsg responseMsg : this.responses) {
                LiveBroadcastStatus responseImpl = (LiveBroadcastStatus) responseMsg;

                if (responseImpl.isReady() == false) {
                    StringBuilder clientMsg = new StringBuilder("Transmitter ");
                    clientMsg.append(responseImpl.getTransmitterGroup())
                            .append(": ").append(responseImpl.getDetail())
                            .append(".");
                    clientResponseMsgs.add(clientMsg.toString());
                    this.notifyClientProblem(clientMsg.toString());
                    logger.error("Failed to start broadcast {}! {}",
                            this.getName(), clientMsg.toString());
                    continue;
                }

                // verify it is actually one of the transmitters we are
                // interested in.
                if (this.managedTransmitterGroups.contains(responseImpl
                        .getTransmitterGroup())) {
                    readyTransmitters.add(responseImpl.getTransmitterGroup());
                }
            }
        }

        if (clientResponseMsgs.isEmpty() == false) {
            StringBuilder clientMsg = new StringBuilder(
                    "Failed to start the broadcast due to the following issues with the listed transmitters:");
            for (String msg : clientResponseMsgs) {
                clientMsg.append("\n* ").append(msg);
            }
            this.notifyClientProblem(clientMsg.toString());

            return false;
        }

        // are all transmitters accounted for?
        if (readyTransmitters.size() != this.managedTransmitterGroups.size()) {
            final String clientMsg = this.buildMissingTransmittersMsg(
                    readyTransmitters, this.managedTransmitterGroups);
            this.notifyClientProblem(clientMsg);
            this.notifyMembersProblem(clientMsg);
            logger.error("Failed to start broadcast {}! {}", this.getName(),
                    clientMsg);
            return false;
        }

        return true;
    }

    private String buildMissingTransmittersMsg(final List<String> actual,
            final Collection<String> expected) {
        StringBuilder clientMsg = new StringBuilder(
                "Unable to access the following transmitters: ");
        int counter = 0;
        for (String transmitterGroup : expected) {
            if (actual.contains(transmitterGroup) == false) {
                if (counter > 0) {
                    clientMsg.append(", ");
                }
                clientMsg.append(transmitterGroup);
            }
            ++counter;
        }
        clientMsg.append(".");

        return clientMsg.toString();
    }

    /**
     * Sends a reply to the sender of the message that was responsible for the
     * creation of this task. The sender will either be: a) Viz or b) The
     * cluster member that was created in response to the original message
     * received from Viz.
     * 
     * @param msg
     *            the reply msg to send
     * @return true if the reply was successful; false, otherwise
     */
    public synchronized boolean sendClientReplyMessage(Object msg) {
        try {
            SerializationUtil.transformToThriftUsingStream(msg,
                    this.socket.getOutputStream());
        } catch (Throwable e) {
            this.notifyMembersProblem(e);
            return false;
        }

        return true;
    }

    private void streamDacAudio(byte[] audioData) {
        logger.info("Streaming {} bytes of audio to the dac (Broadcast {}).",
                audioData.length, this.getName());
        LiveBroadcastAudioRequest request = new LiveBroadcastAudioRequest();
        request.setBroadcastId(this.getName());
        request.setAudioData(audioData);
        // TODO: thread audio data transmissions to the managed dacs.
        for (String transmitterGroup : this.managedTransmitterGroups) {
            this.dacServer.sendToDac(this.streamingServer
                    .getLocalDacCommunicationKey(transmitterGroup), request);
        }
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
            this.shutdownDacLiveBroadcasts();
            this.handleStopRequest((LiveBroadcastStopRequest) object);
        }
    }

    public void handleDacBroadcastMsg(IDacLiveBroadcastMsg msg) {
        logger.info("Handling dac live broadcast {} msg (Broadcast {}).", msg
                .getClass().getName(), this.getName());
        switch (this.state) {
        case INIT:
            /*
             * We should not receive messages from the dac in this state.
             */
            logger.warn("Received unexpected {} message for broadcast {}.", msg
                    .getClass().getName(), msg.getBroadcastId());
            break;
        case READY:
        case TRIGGER:
            synchronized (this.responses) {
                this.responses.add(msg);
            }
            this.responseLock.countDown();
            break;
        case LIVE:
            /*
             * Process messages in this state immediately.
             */
            if (msg instanceof LiveBroadcastStatus) {
                /*
                 * Set to error state to avoid multiple status notifications.
                 */
                this.state = STATE.ERROR;
                LiveBroadcastStatus status = (LiveBroadcastStatus) msg;
                if (status.isReady() == false) {
                    /*
                     * presently all errors indicate that a portion of the
                     * broadcast has failed to one or multiple transmitters.
                     */
                    logger.error(
                            "A problem has been encountered during live broadcast {}. REASON = {}! Terminating broadcast ...",
                            this.getName(), status.getDetail());

                    // Notify cluster members.
                    this.notifyMembersProblem(status.getDetail());

                    // Notify the client.
                    this.notifyClientProblem(status.getDetail());

                    // Stop the current broadcasts.
                    this.shutdownDacLiveBroadcasts();

                    // Stop the broadcast.
                    this.shutdown();
                }
            } else {
                /* message unexpected. */
                logger.warn("Received unexpected {} message for broadcast {}.",
                        msg.getClass().getName(), msg.getBroadcastId());
            }
            break;
        case STOP:
        case ERROR:
            /*
             * Ignore messages when we are stopped or in the process of
             * stopping.
             */
            return;
        }
    }

    private void dacMsgFailed(final String transmitterGroup) {
        /*
         * TODO: handle. Should the entire live broadcast be stopped? Should
         * other potential comms manager(s) be checked to determine if they can
         * now interact with the desired transmitter? Or should the broadcast be
         * continued on the transmitters that are still available?
         */

        this.notifyClientProblem("Broadcast " + this.getName()
                + " is no longer able to communicate with transmitter "
                + transmitterGroup + ".");
        logger.error(
                "Broadcast {} is no longer able to communicate with transmitter {}!",
                this.getName(), transmitterGroup);
    }

    private void shutdownDacLiveBroadcasts() {
        /*
         * Shutdown the transmitter live streams.
         */
        StopLiveBroadcastRequest request = new StopLiveBroadcastRequest();
        request.setBroadcastId(this.getName());
        for (String transmitterGroup : this.managedTransmitterGroups) {
            DacTransmitKey key = this.streamingServer
                    .getLocalDacCommunicationKey(transmitterGroup);
            if (key == null) {
                this.dacMsgFailed(transmitterGroup);
                continue;
            }
            this.dacServer.sendToDac(key, request);
        }
    }

    private void notifyMembersProblem(Throwable e) {
        this.notifyMembersProblem(e.getMessage() != null ? e.getMessage() : e
                .getLocalizedMessage());
    }

    private void notifyMembersProblem(final String detail) {
        /*
         * attempt to notify other comms manager instances.
         */
        BroadcastProblemMsg msg = new BroadcastProblemMsg();
        msg.setBroadcastId(this.getName());
        msg.setDetail(detail);
        this.clusterServer.sendDataToAll(msg);

        /*
         * dropping the connection will propagate back up to the Live Broadcast
         * Thread running on the client machine.
         */
        this.live = false;
        this.state = STATE.ERROR;
    }

    private void notifyClientProblem(final String detail) {
        LiveBroadcastClientStatus status = new LiveBroadcastClientStatus();
        status.setBroadcastId(this.getName());
        status.setStatus(STATUS.FAILED);
        status.setDetail(detail);
        this.sendClientReplyMessage(status);
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

    public void shutdown() {
        /*
         * should shutdown wait until the live broadcasts have ended (max: 2
         * minutes)?
         */
        this.live = false;
    }
}