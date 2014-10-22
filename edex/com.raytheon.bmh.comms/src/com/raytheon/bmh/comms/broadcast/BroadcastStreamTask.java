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
import com.raytheon.uf.common.bmh.broadcast.BroadcastStatus;
import com.raytheon.uf.common.bmh.broadcast.BroadcastTransmitterConfiguration;
import com.raytheon.uf.common.bmh.broadcast.ILiveBroadcastMessage;
import com.raytheon.uf.common.bmh.broadcast.LiveBroadcastCommand.ACTION;
import com.raytheon.uf.common.bmh.broadcast.LiveBroadcastCommand;
import com.raytheon.uf.common.bmh.broadcast.LiveBroadcastPlayCommand;
import com.raytheon.uf.common.bmh.broadcast.LiveBroadcastStartCommand;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
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
 * Oct 15, 2014 3655       bkowal      Support live broadcasting to the DAC.
 * Oct 21, 2014 3655       bkowal      Use the new message types. Improved
 *                                     error handling.
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

    private final LiveBroadcastStartCommand command;

    private List<Transmitter> managedTransmitterGroups;

    private volatile boolean live;

    private CountDownLatch responseLock;

    private List<ILiveBroadcastMessage> responses;

    private volatile STATE state;

    /**
     * 
     */
    public BroadcastStreamTask(final Socket socket,
            final LiveBroadcastStartCommand command,
            final BroadcastStreamServer streamingServer,
            final ClusterServer clusterServer, final DacTransmitServer dacServer) {
        super(determineName(command));
        this.socket = socket;
        this.streamingServer = streamingServer;
        this.clusterServer = clusterServer;
        this.dacServer = dacServer;
        this.command = command;
        this.managedTransmitterGroups = new ArrayList<>(this.command
                .getRequestedTransmitters().size());
    }

    private static String determineName(final LiveBroadcastStartCommand command) {
        return command.getMsgSource().equals(
                ILiveBroadcastMessage.SOURCE_COMMS_MANAGER) ? command
                .getBroadcastId() : UUID.randomUUID().toString().toUpperCase();
    }

    @Override
    public void run() {
        this.state = STATE.INIT;
        this.noteStateTransition();

        /*
         * First need to notify other Comms Managers of the existence of the
         * streaming task.
         */
        this.command.setMsgSource(ILiveBroadcastMessage.SOURCE_COMMS_MANAGER);
        this.command.setBroadcastId(this.getName());
        int count = this.clusterServer.sendDataToAll(this.command);

        // Determine which transmitter(s) we are responsible for.
        /*
         * We should never have to worry about encountering duplicate
         * transmitters across multiple systems?
         * ..........................................
         */
        for (Transmitter transmitterGroup : this.command
                .getRequestedTransmitters()) {
            DacTransmitKey key = this.streamingServer
                    .getLocalDacCommunicationKey(transmitterGroup.getMnemonic());
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
        if (this.managedTransmitterGroups.size() != this.command
                .getRequestedTransmitters().size()) {
            final String clientMsg = this.buildMissingTransmittersMsg(
                    this.managedTransmitterGroups,
                    this.command.getRequestedTransmitters());

            // failure
            this.notifyShareholdersProblem(
                    "Failed to start broadcast " + this.getName() + ". "
                            + clientMsg.toString(), null,
                    this.command.getTransmitterGroups());
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

        for (Transmitter transmitterGroup : this.managedTransmitterGroups) {
            DacTransmitKey key = this.streamingServer
                    .getLocalDacCommunicationKey(transmitterGroup.getMnemonic());
            if (key == null) {
                this.dacMsgFailed(transmitterGroup);
                continue;
            }
            BroadcastTransmitterConfiguration config = this.command
                    .getTransmitterConfigurationMap().get(transmitterGroup);
            if (config == null) {
                continue;
            }
            LiveBroadcastStartCommand startCommand = new LiveBroadcastStartCommand();
            startCommand.setBroadcastId(this.getName());
            startCommand
                    .setMsgSource(ILiveBroadcastMessage.SOURCE_COMMS_MANAGER);
            startCommand.addTransmitterConfiguration(config);
            startCommand.addTransmitter(transmitterGroup);

            this.dacServer.sendToDac(key, startCommand);
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
        for (Transmitter transmitterGroup : this.managedTransmitterGroups) {
            DacTransmitKey key = this.streamingServer
                    .getLocalDacCommunicationKey(transmitterGroup.getMnemonic());
            if (key == null) {
                this.dacMsgFailed(transmitterGroup);
                continue;
            }
            LiveBroadcastCommand command = new LiveBroadcastCommand();
            command.setBroadcastId(this.getName());
            command.setMsgSource(ILiveBroadcastMessage.SOURCE_COMMS_MANAGER);
            command.setAction(ACTION.TRIGGER);
            this.dacServer.sendToDac(key, command);
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
        BroadcastStatus status = new BroadcastStatus();
        status.setMsgSource(ILiveBroadcastMessage.SOURCE_COMMS_MANAGER);
        status.setStatus(true);
        status.setBroadcastId(this.getName());
        status.setTransmitterGroups(this.managedTransmitterGroups);

        this.state = STATE.LIVE;
        this.noteStateTransition();
        this.live = this.sendClientReplyMessage(status);
        while (this.live) {
            Object object = null;
            try {
                object = SerializationUtil.transformFromThrift(Object.class,
                        this.socket.getInputStream());
            } catch (Exception e) {
                this.notifyShareholdersProblem(
                        "Failed to read data from the socket connection.", e,
                        this.command.getTransmitterGroups());
                this.live = false;
            }
            if (object instanceof ILiveBroadcastMessage) {
                this.handleMessageInternal((ILiveBroadcastMessage) object);
            }
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
            this.notifyShareholdersProblem(
                    "Failed to receive a response from all transmitters in a reasonable amount of time.",
                    null, this.command.getTransmitterGroups());
            return ready;
        }

        List<Transmitter> readyTransmitters = new ArrayList<Transmitter>(
                this.managedTransmitterGroups.size());
        // analyze the responses to our latest request.
        synchronized (this.responses) {
            for (ILiveBroadcastMessage responseMsg : this.responses) {
                BroadcastStatus status = (BroadcastStatus) responseMsg;

                if (status.getStatus() == false) {
                    this.notifyShareholdersProblem(status);
                    continue;
                }

                // verify it is actually one of the transmitters we are
                // interested in.
                for (Transmitter transmitter : status.getTransmitterGroups()) {
                    if (this.managedTransmitterGroups.contains(transmitter)) {
                        readyTransmitters.add(transmitter);
                    }
                }
            }
        }

        if (ready == false) {
            return false;
        }

        // are all transmitters accounted for?
        if (readyTransmitters.size() != this.managedTransmitterGroups.size()) {
            final String clientMsg = this.buildMissingTransmittersMsg(
                    readyTransmitters, this.managedTransmitterGroups);
            this.notifyShareholdersProblem(
                    "Failed to start broadcast " + this.getName() + "! "
                            + clientMsg, null,
                    this.command.getTransmitterGroups());
            return false;
        }

        return true;
    }

    private String buildMissingTransmittersMsg(final List<Transmitter> actual,
            final Collection<Transmitter> expected) {
        StringBuilder clientMsg = new StringBuilder(
                "Unable to access the following transmitters: ");
        int counter = 0;
        for (Transmitter transmitterGroup : expected) {
            if (actual.contains(transmitterGroup) == false) {
                if (counter > 0) {
                    clientMsg.append(", ");
                }
                clientMsg.append(transmitterGroup.getMnemonic());
                ++counter;
            }
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
    private synchronized boolean sendClientReplyMessage(BroadcastStatus msg) {
        try {
            SerializationUtil.transformToThriftUsingStream(msg,
                    this.socket.getOutputStream());
        } catch (Exception e) {
            final String errorText = "Failed to send reply message "
                    + msg.getClass().getName() + " to the client.";

            logger.error(errorText, e);
            BroadcastStatus errorStatus = this.buildErrorStatus(errorText, e,
                    msg.getTransmitterGroups());
            this.clusterServer.sendDataToAll(errorStatus);
            return false;
        }

        return true;
    }

    private void streamDacAudio(LiveBroadcastPlayCommand playCommand) {
        logger.info("Streaming {} bytes of audio to the dac for Broadcast {}.",
                playCommand.getAudio().length, this.getName());

        // TODO: thread audio data transmissions to the managed dacs.
        for (Transmitter transmitterGroup : this.managedTransmitterGroups) {
            this.dacServer.sendToDac(
                    this.streamingServer
                            .getLocalDacCommunicationKey(transmitterGroup
                                    .getMnemonic()), playCommand);
        }
    }

    private void handleMessageInternal(ILiveBroadcastMessage command) {
        if (command == null) {
            return;
        }
        if (command instanceof LiveBroadcastCommand) {
            LiveBroadcastCommand liveCommand = (LiveBroadcastCommand) command;
            logger.info("Handling {} command for Broadcast {}.", liveCommand
                    .getAction().toString(), this.getName());
            switch (liveCommand.getAction()) {
            case STOP:
                this.handleStopCommand(liveCommand);
                break;
            case PLAY:
                this.handlePlayCommand((LiveBroadcastPlayCommand) liveCommand);
                break;
            case PREPARE:
            case TRIGGER:
                // Do Nothing.
                break;
            }
        }
    }

    public void handleDacBroadcastMsg(ILiveBroadcastMessage msg) {
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
            if (msg instanceof BroadcastStatus) {
                /*
                 * Set to error state to avoid multiple status notifications.
                 */
                final BroadcastStatus status = (BroadcastStatus) msg;
                if (status.getStatus() == false) {
                    this.state = STATE.ERROR;

                    this.notifyShareholdersProblem(status);

                    this.shutdownDacLiveBroadcasts();
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

    private void dacMsgFailed(final Transmitter transmitterGroup) {
        /*
         * TODO: handle. Should the entire live broadcast be stopped? Should
         * other potential comms manager(s) be checked to determine if they can
         * now interact with the desired transmitter? Or should the broadcast be
         * continued on the transmitters that are still available?
         */

        this.notifyShareholdersProblem("Broadcast " + this.getName()
                + " is no longer able to communicate with transmitter "
                + transmitterGroup.getMnemonic() + ".", null, transmitterGroup);
    }

    private void shutdownDacLiveBroadcasts() {
        /*
         * Shutdown the transmitter live streams.
         */
        LiveBroadcastCommand command = new LiveBroadcastCommand();
        command.setBroadcastId(this.getName());
        command.setMsgSource(ILiveBroadcastMessage.SOURCE_COMMS_MANAGER);
        command.setAction(ACTION.STOP);

        for (Transmitter transmitterGroup : this.managedTransmitterGroups) {
            DacTransmitKey key = this.streamingServer
                    .getLocalDacCommunicationKey(transmitterGroup.getMnemonic());
            if (key == null) {
                this.dacMsgFailed(transmitterGroup);
                continue;
            }
            this.dacServer.sendToDac(key, command);
        }
    }

    private BroadcastStatus buildErrorStatus(final String message,
            final Exception exception, final List<Transmitter> transmitters) {
        BroadcastStatus status = new BroadcastStatus();
        status.setMsgSource(ILiveBroadcastMessage.SOURCE_COMMS_MANAGER);
        status.setBroadcastId(this.getName());
        status.setStatus(false);
        status.setTransmitterGroups(transmitters);
        status.setMessage(message);
        status.setException(exception);

        return status;
    }

    private void notifyShareholdersProblem(final String message,
            final Exception exception, Transmitter transmitter) {
        List<Transmitter> transmitters = new ArrayList<>(1);
        transmitters.add(transmitter);
        this.notifyShareholdersProblem(message, exception, transmitters);
    }

    private void notifyShareholdersProblem(final String message,
            final Exception exception, final List<Transmitter> transmitters) {
        this.notifyShareholdersProblem(this.buildErrorStatus(message,
                exception, transmitters));
    }

    private void notifyShareholdersProblem(final BroadcastStatus status) {
        this.clusterServer.sendDataToAll(status);
        this.sendClientReplyMessage(status);
        logger.error(status.getMessage(), status.getException());

        /*
         * dropping the connection will propagate back up to the Live Broadcast
         * Thread running on the client machine.
         */
        this.state = STATE.ERROR;
    }

    public void handlePlayCommand(LiveBroadcastPlayCommand playCommand) {
        /*
         * Verify that the request is actually for us.
         */
        if (playCommand.getBroadcastId().equals(this.getName()) == false
                || this.live == false) {
            return;
        }
        playCommand.setMsgSource(ILiveBroadcastMessage.SOURCE_COMMS_MANAGER);
        this.clusterServer.sendDataToAll(playCommand);
        this.streamDacAudio(playCommand);
    }

    public void handleStopCommand(LiveBroadcastCommand command) {
        if (command.getBroadcastId() == null) {
            /*
             * In the rare case that the user immediately closes the dialog
             * after starting a broadcast. Close method is final so it cannot be
             * prevented.
             */
            command.setBroadcastId(this.getName());
        }
        this.clusterServer.sendDataToAll(command);
        this.shutdownDacLiveBroadcasts();
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