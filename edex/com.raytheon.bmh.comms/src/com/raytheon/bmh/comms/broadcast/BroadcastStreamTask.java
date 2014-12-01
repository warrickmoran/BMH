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
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import com.raytheon.bmh.comms.DacTransmitKey;
import com.raytheon.bmh.comms.broadcast.ManagedTransmitterGroup.STREAMING_STATUS;
import com.raytheon.bmh.comms.cluster.ClusterServer;
import com.raytheon.bmh.comms.dactransmit.DacTransmitServer;
import com.raytheon.uf.common.bmh.broadcast.BroadcastStatus;
import com.raytheon.uf.common.bmh.broadcast.BroadcastTransmitterConfiguration;
import com.raytheon.uf.common.bmh.broadcast.ILiveBroadcastMessage;
import com.raytheon.uf.common.bmh.broadcast.LiveBroadcastCommand.ACTION;
import com.raytheon.uf.common.bmh.broadcast.OnDemandBroadcastConstants.MSGSOURCE;
import com.raytheon.uf.common.bmh.broadcast.LiveBroadcastCommand;
import com.raytheon.uf.common.bmh.broadcast.LiveBroadcastPlayCommand;
import com.raytheon.uf.common.bmh.broadcast.LiveBroadcastStartCommand;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.serialization.SerializationUtil;

/**
 * Used to manage a broadcast stream. Executes the SAME tones and will continue
 * streaming live audio to the Dac until the conclusion of the broadcast after
 * successful initialization.
 * 
 * Responsible for managing the {@link TransmitterGroup}s that it is responsible
 * for and forwarding commands to other member
 * {@link ClusteredBroadcastStreamTask}s that are created on other cluster
 * members. It will also keep track of the {@link TransmitterGroup}s that the
 * {@link ClusteredBroadcastStreamTask}s are managing to ensure that all
 * requested {@link TransmitterGroup}s are accounted for.
 * 
 * The {@link ClusterServer} is used to communicate with other cluster members.
 * The {@link BroadcastStreamTask} also has a direct connection to Viz (the
 * client) via a {@link Socket}.
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
 * Nov 3, 2014  3655       bkowal      Increase timeout for same tone playback. Handle
 *                                     multiple audio packets.
 * Nov 10, 2014 3630       bkowal      Re-factor to support on-demand broadcasting.
 * Nov 15, 2014 3630       bkowal      Extend AbstractBroadcastingTask.
 * Nov 17, 2014 3808       bkowal      Support broadcast live. Initial transition to
 *                                     transmitter group.
 * Nov 21, 2014 3845       bkowal      Re-factor/cleanup
 * Dec 1, 2014  3797       bkowal      Support broadcast clustering.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class BroadcastStreamTask extends AbstractBroadcastingTask {

    protected static enum STATE {
        INIT, READY, TRIGGER, LIVE, SHUTDOWN, STOP, ERROR
    }

    private static final String DESCRIPTION = "broadcast streaming task";

    protected final BroadcastStreamServer streamingServer;

    private final ClusterServer clusterServer;

    private final DacTransmitServer dacServer;

    protected final LiveBroadcastStartCommand command;

    protected final TransmitterGroupManager tgManager;

    protected volatile STATE state;

    /**
     * Used to wait for information from all managed {@link TransmitterGroup}s
     * and all member {@link ClusteredBroadcastStreamTask}s.
     */
    protected final Semaphore communicationLock = new Semaphore(1);

    /**
     * Amount of time to wait for a response from a {@link TransmitterGroup} in
     * milliseconds.
     */
    private final long PER_TRANSMITTER_WAIT_DURATION = 10000;

    /**
     * 
     */
    public BroadcastStreamTask(final Socket socket,
            final LiveBroadcastStartCommand command,
            final BroadcastStreamServer streamingServer,
            final ClusterServer clusterServer, final DacTransmitServer dacServer) {
        super(determineName(command), DESCRIPTION, socket);
        this.streamingServer = streamingServer;
        this.clusterServer = clusterServer;
        this.dacServer = dacServer;
        this.command = command;
        this.tgManager = new TransmitterGroupManager(
                this.command.getTransmitterGroups());
    }

    private static String determineName(final LiveBroadcastStartCommand command) {
        return command.getMsgSource() == MSGSOURCE.COMMS ? command
                .getBroadcastId() : UUID.randomUUID().toString().toUpperCase();
    }

    private void handleStateTransition() {
        logger.info("Broadcast {} state transition to: {}", this.getName(),
                this.state.toString());
        logger.info("CURRENT MANAGED STATE: {}", this.tgManager.toString());

        if (this.state == STATE.READY || this.state == STATE.TRIGGER) {
            /**
             * Lock on the READY state and the TRIGGER state for both the
             * {@link BroadcastStreamTask} and the
             * {@link ClusteredBroadcastStreamTask}. This use of the locking
             * provides time for the dac transmits associated with the managed
             * {@link TransmitterGroup}s to provide responses.
             */
            try {
                /*
                 * lock should be available immediately or else it is already
                 * locked.
                 */
                if (this.communicationLock
                        .tryAcquire(10, TimeUnit.MILLISECONDS) == false) {
                    logger.warn("The Communication Lock is currently already locked. Expected State ... Continuing");
                }
            } catch (InterruptedException e) {
                logger.warn("Broadcast Stream Task was interrupted while waiting for the Communication Lock.");
            }
        }

        switch (this.state) {
        case ERROR:
        case STOP:
            // do nothing.
            break;
        case INIT:
            this.initializeBroadcast();
            break;
        case LIVE:
            this.broadcastLive();
            break;
        case READY:
            this.broadcastReady();
            break;
        case SHUTDOWN:
            this.stopBroadcast();
            break;
        case TRIGGER:
            this.triggerBroadcast();
            break;
        }
    }

    protected void initializeBroadcast() {
        /*
         * First need to notify other Comms Managers of the existence of the
         * streaming task.
         */
        this.command.setMsgSource(MSGSOURCE.COMMS);
        this.command.setBroadcastId(this.getName());
        int count = this.clusterServer.sendDataToAll(this.command);

        // Determine which transmitter(s) we are responsible for.
        /*
         * We should never have to worry about encountering duplicate
         * transmitters across multiple systems?
         * ..........................................
         */
        for (TransmitterGroup transmitterGrp : this.command
                .getTransmitterGroups()) {
            DacTransmitKey key = this.streamingServer
                    .getLocalDacCommunicationKey(transmitterGrp.getName());
            if (key == null) {
                continue;
            }
            this.tgManager.claimResponsibility(transmitterGrp);
        }

        /**
         * We will only acquire the communication lock if there are other
         * cluster members that we want to allow to respond
         */
        if (count > 0) {
            try {
                if (this.communicationLock
                        .tryAcquire(10, TimeUnit.MILLISECONDS) == false) {
                    logger.warn("The Communication Lock is currently already locked. Expected State ... Continuing");
                }
            } catch (InterruptedException e) {
                logger.warn("Broadcast Stream Task was interrupted while waiting for the Communication Lock.");
            }
        }

        /*
         * Every specified transmitter group must be a transmitter group that we
         * can access or that a different cluster member can access.
         */
        if ((count > 0 && this.communicationWait(this.calculateWaitDuration()) == false)
                || this.tgManager.allTransmittersAssigned() == false) {
            final String preText = "Failed to start broadcast "
                    + this.getName()
                    + ". Unable to access the following transmitter groups: ";
            final String clientMsg = this.buildTransmitterListMsg(preText,
                    this.tgManager.getUnassignedTransmitters());

            // failure
            this.notifyShareholdersProblem(clientMsg);

            /*
             * due to the fact that we are only in the initialization phase, we
             * will also need to notify broadcast stream tasks running on other
             * cluster members that they will need to be shutdown because Viz
             * will only initiate the shutdown if an error occurs at any point
             * after a successful initialization.
             */
            // build and submit a stop command.
            LiveBroadcastCommand command = new LiveBroadcastCommand();
            command.setAction(ACTION.STOP);
            command.setBroadcastId(this.getName());
            command.setMsgSource(MSGSOURCE.COMMS);
            this.handleStopCommand(command);

            return;
        }

        this.state = STATE.READY;
    }

    protected void broadcastLive() {
        /*
         * allow any clustered broadcast streams to transition to the next state
         * to prepare the dacs that they are responsible for managing.
         */
        this.clusterServer
                .sendDataToAll(new ClusteredBroadcastTransitionTrigger(this
                        .getName()));
        /*
         * Ready to start streaming audio. Notify the client and prepare to
         * start streaming audio.
         */
        BroadcastStatus status = new BroadcastStatus();
        status.setMsgSource(MSGSOURCE.COMMS);
        status.setStatus(true);
        status.setBroadcastId(this.getName());
        status.setTransmitterGroups(this.tgManager.getManagedTransmitters());

        if (this.sendClientReplyMessage(status)) {
            this.state = STATE.LIVE;
        } else {
            this.state = STATE.ERROR;
        }
        while (this.state == STATE.LIVE) {
            Object object = null;
            try {
                object = SerializationUtil.transformFromThrift(Object.class,
                        this.socket.getInputStream());
            } catch (Exception e) {
                this.notifyShareholdersProblem(
                        "Failed to read data from the socket connection.", e,
                        this.command.getTransmitterGroups());
                this.state = STATE.ERROR;
            }
            if (object instanceof ILiveBroadcastMessage) {
                this.handleMessageInternal((ILiveBroadcastMessage) object);
            }
        }
    }

    protected void broadcastReady() {
        /*
         * at this point we know all required transmitters are available (at
         * least < 1 ms ago). So, we send another notification to the dac
         * transmitters to inform them that we are preparing to start a live
         * broadcast. If an interrupt occurs before all transmitters can be
         * notified, the live broadcast will fail.
         */

        /*
         * allow any clustered broadcast streams to transition to the next state
         * to prepare the dacs that they are responsible for managing.
         */
        this.clusterServer
                .sendDataToAll(new ClusteredBroadcastTransitionTrigger(this
                        .getName()));
        this.sendStartCmdToDacs();

        /*
         * give dac transmits and member comms managers some amount of time to
         * respond based on the total number of transmitters required. The wait
         * will end early if we receive a response for all transmitters before
         * time expires.
         */

        /*
         * verify all transmitter groups are available for streaming.
         */
        if (this.communicationWait(this.calculateWaitDuration()) == false
                || this.tgManager
                        .doAllTransmittersHaveStreamStatus(STREAMING_STATUS.AVAILABLE) == false) {
            StringBuilder msgBuilder = new StringBuilder(
                    "Failed to start broadcast " + this.getName() + ".");
            if (this.tgManager
                    .doAnyTransmittersHaveStreamStatus(STREAMING_STATUS.UNKNOWN)) {
                msgBuilder
                        .append(this
                                .buildTransmitterListMsg(
                                        " Unable to determine the status of the following transmitter groups: ",
                                        this.tgManager
                                                .getTransmittersWithStreamStatus(STREAMING_STATUS.UNKNOWN)));
            }
            if (this.tgManager
                    .doAnyTransmittersHaveStreamStatus(STREAMING_STATUS.BUSY)) {
                msgBuilder
                        .append(this
                                .buildTransmitterListMsg(
                                        " The following transmitter groups are not currently available: ",
                                        this.tgManager
                                                .getTransmittersWithStreamStatus(STREAMING_STATUS.BUSY)));
            }

            // failure
            this.notifyShareholdersProblem(msgBuilder.toString());

            /*
             * due to the fact that we are only in the initialization phase, we
             * will also need to notify broadcast stream tasks running on other
             * cluster members that they will need to be shutdown because Viz
             * will only initiate the shutdown if an error occurs at any point
             * after a successful initialization.
             */
            // build and submit a stop command.
            LiveBroadcastCommand command = new LiveBroadcastCommand();
            command.setAction(ACTION.STOP);
            command.setBroadcastId(this.getName());
            command.setMsgSource(MSGSOURCE.COMMS);
            this.handleStopCommand(command);

            return;
        }

        this.state = STATE.TRIGGER;
    }

    protected void sendStartCmdToDacs() {
        for (TransmitterGroup transmitterGroup : this.tgManager
                .getManagedTransmitters()) {
            DacTransmitKey key = this.streamingServer
                    .getLocalDacCommunicationKey(transmitterGroup.getName());
            if (key == null) {
                /*
                 * for now assume that the dac transmit was given to another
                 * comms manager.
                 */
                this.tgManager.forfeitResponsibility(transmitterGroup);
                continue;
            }
            BroadcastTransmitterConfiguration config = this.command
                    .getTransmitterGroupConfigurationMap()
                    .get(transmitterGroup);
            if (config == null) {
                continue;
            }
            LiveBroadcastStartCommand startCommand = new LiveBroadcastStartCommand();
            startCommand.setType(this.command.getType());
            startCommand.setBroadcastId(this.getName());
            startCommand.setMsgSource(MSGSOURCE.COMMS);
            startCommand.addTransmitterConfiguration(config);

            this.dacServer.sendToDac(key, startCommand);
        }
    }

    protected void triggerBroadcast() {
        /*
         * At this point, all transmitters are waiting for the signal to
         * proceed. Any incoming interrupt messages will be delayed because an
         * interrupt cannot interrupt an interrupt. Additionally, it will not be
         * possible to start other live broadcast messages from other
         * workstations on the same transmitter(s).
         */
        /*
         * allow any clustered broadcast streams to transition to the next state
         * to prepare the dacs that they are responsible for managing.
         */
        this.clusterServer
                .sendDataToAll(new ClusteredBroadcastTransitionTrigger(this
                        .getName()));
        this.sendCmdToDacs(ACTION.TRIGGER);

        /*
         * verify all transmitter groups are now available to start streaming -
         * indicating that the required tones have been played and all existing
         * playback has been paused.
         */
        if (this.communicationWait(this.calculateWaitDuration()) == false
                || this.tgManager
                        .doAllTransmittersHaveStreamStatus(STREAMING_STATUS.READY) == false) {
            StringBuilder msgBuilder = new StringBuilder(
                    "Failed to start broadcast " + this.getName() + ".");
            if (this.tgManager
                    .doAnyTransmittersHaveStreamStatus(STREAMING_STATUS.AVAILABLE)) {
                msgBuilder
                        .append(this
                                .buildTransmitterListMsg(
                                        " Unable to determine the status of the following transmitter groups: ",
                                        this.tgManager
                                                .getTransmittersWithStreamStatus(STREAMING_STATUS.AVAILABLE)));
            }
            if (this.tgManager
                    .doAnyTransmittersHaveStreamStatus(STREAMING_STATUS.BUSY)) {
                msgBuilder
                        .append(this
                                .buildTransmitterListMsg(
                                        " The following transmitter groups are not currently available: ",
                                        this.tgManager
                                                .getTransmittersWithStreamStatus(STREAMING_STATUS.BUSY)));
            }

            // failure
            this.notifyShareholdersProblem(msgBuilder.toString());

            /*
             * due to the fact that we are only in the initialization phase, we
             * will also need to notify broadcast stream tasks running on other
             * cluster members that they will need to be shutdown because Viz
             * will only initiate the shutdown if an error occurs at any point
             * after a successful initialization.
             */
            // build and submit a stop command.
            LiveBroadcastCommand command = new LiveBroadcastCommand();
            command.setAction(ACTION.STOP);
            command.setBroadcastId(this.getName());
            command.setMsgSource(MSGSOURCE.COMMS);
            this.handleStopCommand(command);

            return;
        }

        this.state = STATE.LIVE;
    }

    protected void sendCmdToDacs(ACTION cmdAction) {
        for (TransmitterGroup transmitterGrp : this.tgManager
                .getManagedTransmitters()) {
            DacTransmitKey key = this.streamingServer
                    .getLocalDacCommunicationKey(transmitterGrp.getName());
            if (key == null) {
                /*
                 * for now assume that the dac transmit was given to another
                 * comms manager.
                 */
                this.tgManager.forfeitResponsibility(transmitterGrp);
                continue;
            }

            LiveBroadcastCommand command = new LiveBroadcastCommand();
            command.setBroadcastId(this.getName());
            command.setMsgSource(MSGSOURCE.COMMS);
            command.setAction(cmdAction);
            this.dacServer.sendToDac(key, command);
        }
    }

    protected void stopBroadcast() {
        logger.info("Broadcast {} is shutting down ...", this.getName());
        try {
            this.socket.close();
        } catch (IOException e) {
            logger.warn("Failed to close the socket connection (Broadcast "
                    + this.getName() + ")!", e);
        }
        this.streamingServer.broadcastTaskFinished(this.getName());
        this.state = STATE.STOP;
    }

    /**
     * Provides a common location to wait for communicationLock to unlock. The
     * unlock indicates that the required information has been successfully
     * received from all managed {@link TransmitterGroup}s and all member
     * {@link ClusteredBroadcastStreamTask}s.
     * 
     * @param waitDuration
     *            the maximum amount of time to wait in milliseconds for
     *            communicationLock to unlock
     * @return true, if the lock was successfully acquired; false, otherwise
     */
    protected boolean communicationWait(long waitDuration) {
        boolean acquired = false;
        try {
            acquired = this.communicationLock.tryAcquire(waitDuration,
                    TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            logger.warn("Broadcast Stream Task was interrupted while waiting for the Communication Lock.");
        }

        if (acquired) {
            this.communicationLock.release();
        }

        return acquired;
    }

    @Override
    public void run() {
        if (this.command.getMsgSource() == MSGSOURCE.VIZ) {
            logger.info("Running broadcast streaming task {}.", this.getName());
        } else if (this.command.getMsgSource() == MSGSOURCE.COMMS) {
            logger.info("Running CLUSTERED broadcast streaming task {}.",
                    this.getName());
        }

        this.state = STATE.INIT;
        while (this.state != STATE.STOP && this.state != STATE.ERROR) {
            this.handleStateTransition();
        }
    }

    private String buildTransmitterListMsg(String preText,
            List<TransmitterGroup> tgList) {
        if (preText == null) {
            preText = "";
        }
        StringBuilder clientMsg = new StringBuilder(preText);
        boolean first = true;
        for (TransmitterGroup transmitterGrp : tgList) {
            if (first == false) {
                clientMsg.append(", ");
            } else {
                first = false;
            }
            clientMsg.append(transmitterGrp.getName());
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
    @Override
    protected synchronized boolean sendClientReplyMessage(BroadcastStatus msg) {
        try {
            super.sendClientReplyMessage(msg);
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
        logger.info(
                "Streaming {} packets of audio to the dac for Broadcast {}.",
                playCommand.getAudio().size(), this.getName());

        // TODO: thread audio data transmissions to the managed dacs.
        for (TransmitterGroup transmitterGroup : this.tgManager
                .getManagedTransmitters()) {
            this.dacServer.sendToDac(this.streamingServer
                    .getLocalDacCommunicationKey(transmitterGroup.getName()),
                    playCommand);
        }
    }

    protected void handleMessageInternal(ILiveBroadcastMessage msg) {
        if (msg == null) {
            return;
        }
        if (msg instanceof LiveBroadcastCommand) {
            LiveBroadcastCommand liveCommand = (LiveBroadcastCommand) msg;
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
        } else if (msg instanceof BroadcastStatus) {
            BroadcastStatus status = (BroadcastStatus) msg;
            switch (this.state) {
            case INIT:
                if (status.getStatus() == false
                        || status.getTransmitterGroups() == null) {
                    return;
                }

                /*
                 * determine which transmitters other comms managers are
                 * managing.
                 */
                for (TransmitterGroup tg : msg.getTransmitterGroups()) {
                    this.tgManager.giveResponsibility(tg);
                }

                logger.info("UPDATED MANAGED STATE: {}",
                        this.tgManager.toString());

                // if all transmitters have now been accounted for, unlock
                if (this.tgManager.allTransmittersAssigned()) {
                    /*
                     * Note: at this point, we may not have necessarily received
                     * a response from every cluster member (granted this design
                     * does allow for more than one additional cluster member).
                     * But, if all transmitters have been accounted for, there
                     * is no reason to wait for other cluster members to
                     * respond.
                     */
                    this.communicationLock.release();
                }
                break;
            case READY:
            case TRIGGER:
                if (status.getTransmitterGroups() == null) {
                    return;
                }

                STREAMING_STATUS newStatus = null;
                if (status.getStatus()) {
                    newStatus = (this.state == STATE.READY) ? STREAMING_STATUS.AVAILABLE
                            : STREAMING_STATUS.READY;
                } else {
                    newStatus = STREAMING_STATUS.BUSY;
                }
                for (TransmitterGroup tg : msg.getTransmitterGroups()) {
                    this.tgManager.updateStreamStatus(tg, newStatus);
                }

                logger.info("UPDATED MANAGED STATE: {}",
                        this.tgManager.toString());

                STREAMING_STATUS nonDesiredStatus = (this.state == STATE.READY) ? STREAMING_STATUS.UNKNOWN
                        : STREAMING_STATUS.AVAILABLE;
                if (this.tgManager
                        .doAnyTransmittersHaveStreamStatus(nonDesiredStatus) == false) {
                    this.communicationLock.release();
                }
                break;
            default:
                if (status.getStatus() == false) {
                    logger.error(
                            "Received ERROR status message from cluster member: "
                                    + status.getMessage(),
                            status.getException());
                }
                break;
            }
        } else if (msg instanceof ClusteredBroadcastTransitionTrigger) {
            this.invokeTransitionExecution();
        }
    }

    /**
     * Handles a {@link ILiveBroadcastMessage} received from another cluster
     * member.
     * 
     * @param command
     *            the {@link ILiveBroadcastMessage} received from another
     *            cluster member.
     */
    public void handleClusteredBroadcastMsg(ILiveBroadcastMessage command) {
        this.handleMessageInternal(command);
    }

    /**
     * Handles a {@link ILiveBroadcastMessage} received from a dac transmit
     * process.
     * 
     * @param msg
     *            the {@link ILiveBroadcastMessage} received from a dac transmit
     *            process.
     */
    public void handleDacBroadcastMsg(ILiveBroadcastMessage msg) {
        logger.info("Handling dac broadcast {} msg (Broadcast {}).", msg
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
            if (msg instanceof BroadcastStatus == false) {
                return;
            }

            final STREAMING_STATUS desiredStatus = (this.state == STATE.READY) ? STREAMING_STATUS.AVAILABLE
                    : STREAMING_STATUS.READY;
            final STREAMING_STATUS notWantedStatus = (this.state == STATE.READY) ? STREAMING_STATUS.UNKNOWN
                    : STREAMING_STATUS.AVAILABLE;

            BroadcastStatus status = (BroadcastStatus) msg;
            STREAMING_STATUS streamingStatus = null;
            if (status.getStatus()) {
                /*
                 * Listed {@link TransmitterGroup}s are available and/or ready
                 * to start streaming audio.
                 */
                streamingStatus = desiredStatus;
            } else {
                /*
                 * An interrupt may have been playing on one of the requested
                 * transmitters.
                 */
                streamingStatus = STREAMING_STATUS.BUSY;
            }
            for (TransmitterGroup tg : status.getTransmitterGroups()) {
                this.tgManager.updateStreamStatus(tg, streamingStatus);
            }
            logger.info("UPDATED MANAGED STATE: {}", this.tgManager.toString());

            // have all transmitters been accounted for?
            if (this.isPrimary()
                    && this.tgManager
                            .doAnyTransmittersHaveStreamStatus(notWantedStatus) == false) {
                this.communicationLock.release();
            } else if (this.isPrimary() == false
                    && this.tgManager
                            .doManagedTransmittersHaveStreamStatus(notWantedStatus) == false) {
                this.communicationLock.release();
            }

            break;
        case LIVE:
            /*
             * Process messages in this state immediately.
             */
            if (msg instanceof BroadcastStatus) {
                /*
                 * Set to error state to avoid multiple status notifications.
                 */
                status = (BroadcastStatus) msg;
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
        case SHUTDOWN:
            /*
             * Ignore messages when we are stopped or in the process of
             * stopping.
             */
            return;
        }
    }

    /**
     * Used to submit a notification to other cluster members and the client to
     * inform them that we are no longer able to interact with a
     * {@link TransmitterGroup} that we were able to interact with previously.
     * Only invoked if the communication lapse occurs when the broadcast stream
     * is in the process of shutting down.
     * 
     * @param transmitterGroup
     *            the {@link TransmitterGroup} that can no longer be reached.
     */
    private void dacMsgFailed(final TransmitterGroup transmitterGroup) {
        this.notifyShareholdersProblem("Broadcast " + this.getName()
                + " is no longer able to communicate with transmitter group "
                + transmitterGroup.getName() + ".", null, transmitterGroup);
    }

    /**
     * Sends a shutdown {@link LiveBroadcastCommand} to all managed
     * {@link TransmitterGroup} broadcast streams.
     */
    private void shutdownDacLiveBroadcasts() {
        /*
         * Shutdown the transmitter live streams.
         */
        LiveBroadcastCommand command = new LiveBroadcastCommand();
        command.setBroadcastId(this.getName());
        command.setMsgSource(MSGSOURCE.COMMS);
        command.setAction(ACTION.STOP);
        for (TransmitterGroup transmitterGroup : this.tgManager
                .getManagedTransmitters()) {
            DacTransmitKey key = this.streamingServer
                    .getLocalDacCommunicationKey(transmitterGroup.getName());
            if (key == null) {
                this.dacMsgFailed(transmitterGroup);
                continue;
            }
            this.dacServer.sendToDac(key, command);
        }
    }

    private void notifyShareholdersProblem(final String message) {
        this.notifyShareholdersProblem(this.buildErrorStatus(message, null,
                null));
    }

    private void notifyShareholdersProblem(final String message,
            final Exception exception, TransmitterGroup transmitterGroup) {
        List<TransmitterGroup> transmitterGroups = new ArrayList<>(1);
        transmitterGroups.add(transmitterGroup);
        this.notifyShareholdersProblem(message, exception, transmitterGroups);
    }

    private void notifyShareholdersProblem(final String message,
            final Exception exception,
            final List<TransmitterGroup> transmitterGroups) {
        this.notifyShareholdersProblem(this.buildErrorStatus(message,
                exception, transmitterGroups));
    }

    private void notifyShareholdersProblem(final BroadcastStatus status) {
        this.clusterServer.sendDataToAll(status);
        if (this.isPrimary()) {
            this.sendClientReplyMessage(status);
        }
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
                || this.state != STATE.LIVE) {
            return;
        }
        playCommand.setMsgSource(MSGSOURCE.COMMS);
        if (this.isPrimary()) {
            this.clusterServer.sendDataToAll(playCommand);
        }
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
        command.setMsgSource(MSGSOURCE.COMMS);
        if (this.isPrimary()) {
            this.clusterServer.sendDataToAll(command);
        }
        this.shutdownDacLiveBroadcasts();
        this.shutdown();
    }

    public void shutdown() {
        /*
         * should shutdown wait until the live broadcasts have ended (max: 2
         * minutes)?
         */
        this.state = STATE.SHUTDOWN;
    }

    /**
     * Indicates whether or not this {@link BroadcastStreamTask} has an active
     * connection to the client. Indicates that it is the task responsible for
     * orchestrating the entire broadcast stream.
     * 
     * @return true, if primary; false, otherwise
     */
    private boolean isPrimary() {
        return this.socket != null;
    }

    /**
     * Used to trigger the transition to the next state for any tasks that do
     * not manage their own state transitions.
     */
    protected void invokeTransitionExecution() {
    }

    /**
     * Sends a {@link BroadcastStatus} to other cluster members to provide
     * updates about the streaming status of the {@link TransmitterGroup}s that
     * it is responsible for.
     * 
     * @param statusToCheck
     *            indicates which {@link TransmitterGroup}s the status should be
     *            reported for
     * @param indicatesSuccess
     *            whether or not the specified statusToCheck indicates that the
     *            {@link TransmitterGroup} is in a usable state.
     */
    protected void notifyClusterMembersDacStatus(
            final STREAMING_STATUS statusToCheck, final boolean indicatesSuccess) {
        if (this.tgManager.doAnyTransmittersHaveStreamStatus(statusToCheck) == false) {
            return;
        }

        BroadcastStatus status = new BroadcastStatus();
        status.setBroadcastId(this.getName());
        status.setMsgSource(MSGSOURCE.COMMS);
        status.setStatus(indicatesSuccess);
        status.setTransmitterGroups(this.tgManager
                .getTransmittersWithStreamStatus(statusToCheck));
        this.clusterServer.sendDataToAll(status);
    }

    /**
     * Sends a {@link BroadcastStatus} to other cluster members to provide
     * updates about which {@link TransmitterGroup}s this process will be
     * responsible for.
     */
    protected void notifyClusterMembersDacResponsibility() {
        /*
         * notify all comms managers to inform them which transmitters we will
         * be responsible for.
         */
        BroadcastStatus status = new BroadcastStatus();
        status.setBroadcastId(this.getName());
        status.setMsgSource(MSGSOURCE.COMMS);
        status.setStatus(true);
        status.setTransmitterGroups(this.tgManager.getManagedTransmitters());
        this.clusterServer.sendDataToAll(status);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.bmh.comms.broadcast.AbstractBroadcastingTask#
     * getTransmitterGroups()
     */
    @Override
    public List<TransmitterGroup> getTransmitterGroups() {
        return this.command.getTransmitterGroups();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.bmh.comms.broadcast.AbstractBroadcastingTask#
     * dacConnectedToServer(java.lang.String)
     */
    @Override
    public void dacConnectedToServer(final String tgName) {
        /*
         * Is it a {@link TransmitterGroup} we care about?
         */
        TransmitterGroup tg = this.getTransmitterGroupByIdentifier(tgName);
        if (tg == null) {
            return;
        }

        /*
         * We are now responsible for the {@link TransmitterGroup}.
         * 
         * Note: if a dac transmit switches servers during the first phase of
         * live stream initialization, the initialization will most likely fail.
         */
        this.tgManager.claimResponsibility(tg);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.bmh.comms.broadcast.AbstractBroadcastingTask#
     * dacDisconnectedFromServer(java.lang.String)
     */
    @Override
    public void dacDisconnectedFromServer(final String tgName) {
        /*
         * Is it a {@link TransmitterGroup} we care about?
         */
        TransmitterGroup tg = this.getTransmitterGroupByIdentifier(tgName);
        if (tg == null) {
            return;
        }

        /*
         * We are no longer responsible for this {@link TransmitterGroup}.
         * 
         * Note: if a dac transmit switches servers during the first phase of
         * live stream initialization, the initialization will most likely fail.
         */
        this.tgManager.forfeitResponsibility(tg);
    }

    /**
     * Calculates the amount of time in milliseconds to wait for responses from
     * other managed and member, when applicable, processes.
     * 
     * @return the amount of time to wait in milliseconds.
     */
    protected long calculateWaitDuration() {
        long duration = this.command.getTransmitterGroups().size()
                * PER_TRANSMITTER_WAIT_DURATION;

        /*
         * if this is the trigger step, we will want to increase the wait
         * duration by the duration of the longest tone to allow for the full
         * playback of tones before timing out.
         */
        if (this.state == STATE.TRIGGER) {
            duration += this.command.getTonesDuration();
        }

        return duration;
    }
}