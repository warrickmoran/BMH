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

import java.net.Socket;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import com.raytheon.bmh.comms.broadcast.ManagedTransmitterGroup.STREAMING_STATUS;
import com.raytheon.bmh.comms.cluster.ClusterServer;
import com.raytheon.bmh.comms.dactransmit.DacTransmitServer;
import com.raytheon.uf.common.bmh.broadcast.BroadcastStatus;
import com.raytheon.uf.common.bmh.broadcast.LiveBroadcastCommand.ACTION;
import com.raytheon.uf.common.bmh.broadcast.LiveBroadcastPlayCommand;
import com.raytheon.uf.common.bmh.broadcast.LiveBroadcastStartCommand;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;

/**
 * This version of the {@link BroadcastStreamTask} is started and executed on
 * the cluster members that do not have a direct connection to Viz. It receives
 * directions and information from the {@link BroadcastStreamTask} that is
 * connected to Viz, hereafter referred to as the Primary. During the
 * initialization phase, it will also report back to the Primary using a
 * {@link BroadcastStatus} to identify which {@link TransmitterGroup}s it will
 * be managing and the streaming state of each group that it will be managing.
 * 
 * The Primary controls state transitions in member
 * {@link ClusteredBroadcastStreamTask}s by sending a
 * {@link ClusteredBroadcastTransitionTrigger} to each cluster member when it is
 * safe to transition to the next state. The Primary will also forward
 * {@link LiveBroadcastPlayCommand}s with audio information to stream to the
 * {@link TransmitterGroup}s that the {@link ClusteredBroadcastStreamTask} is
 * responsible for managing.
 * 
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 25, 2014 3797       bkowal      Initial creation
 * Jun 19, 2015 4482       rjpeter     Remove override of stopBroadcast.
 * Aug 12, 2015 4424       bkowal      Eliminate Dac Transmit Key.
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class ClusteredBroadcastStreamTask extends BroadcastStreamTask {

    private final Semaphore transitionLock = new Semaphore(1);

    private STATE nextState;

    /**
     * @param socket
     * @param command
     * @param streamingServer
     * @param clusterServer
     * @param dacServer
     */
    public ClusteredBroadcastStreamTask(LiveBroadcastStartCommand command,
            BroadcastStreamServer streamingServer, ClusterServer clusterServer,
            DacTransmitServer dacServer) {
        super(null, command, streamingServer, clusterServer, dacServer);
    }

    @Override
    protected void initializeBroadcast() {
        this.lockExecution();

        // Determine which transmitter(s) we are responsible for.
        /*
         * We should never have to worry about encountering duplicate
         * transmitters across multiple systems?
         * ..........................................
         */
        for (TransmitterGroup transmitterGrp : this.command
                .getTransmitterGroups()) {
            if (this.streamingServer.isDacConnected(transmitterGrp.getName()) == false) {
                continue;
            }
            this.tgManager.claimResponsibility(transmitterGrp);
        }

        this.notifyClusterMembersDacResponsibility();

        // wait for permission to transition to the next state.
        this.waitExecutionUnlock(STATE.READY);
    }

    @Override
    protected void broadcastLive() {
        this.lockExecution();

        this.waitExecutionUnlock(STATE.SHUTDOWN);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.bmh.comms.broadcast.BroadcastStreamTask#broadcastReady()
     */
    @Override
    protected void broadcastReady() {
        this.lockExecution();
        this.sendStartCmdToDacs();

        this.communicationWait(this.calculateWaitDuration());

        // notify available transmitters
        this.notifyClusterMembersDacStatus(STREAMING_STATUS.AVAILABLE, true);

        // notify busy transmitters
        this.notifyClusterMembersDacStatus(STREAMING_STATUS.BUSY, false);

        // unknown transmitters are ignored
        this.waitExecutionUnlock(STATE.TRIGGER);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.bmh.comms.broadcast.BroadcastStreamTask#triggerBroadcast()
     */
    @Override
    protected void triggerBroadcast() {
        this.lockExecution();
        this.sendCmdToDacs(ACTION.TRIGGER);

        this.communicationWait(this.calculateWaitDuration());

        // notify ready transmitters
        this.notifyClusterMembersDacStatus(STREAMING_STATUS.READY, true);

        // notify busy transmitters
        this.notifyClusterMembersDacStatus(STREAMING_STATUS.BUSY, false);

        // unknown transmitters are ignored
        this.waitExecutionUnlock(STATE.LIVE);
    }

    /**
     * This function is not currently used. But, if it represents a valid
     * scenario and if it is used, it will allow a clustered broadcast stream
     * task to continue interacting with Viz in the case that the broadcast
     * stream task that Viz originally connected to becomes unavailable - server
     * goes offline, comms manager is stopped, etc. However, Viz will need to be
     * updated to failover to a new connection when required to make this
     * scenario a possibility.
     * 
     * @param socket
     *            {@link Socket} used to interact with Viz
     */
    public void promoteForFailover(final Socket socket) {
        this.socket = socket;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.bmh.comms.broadcast.BroadcastStreamTask#shutdown()
     */
    @Override
    public void shutdown() {
        super.shutdown();
        this.transitionLock.release();
    }

    private void lockExecution() {
        try {
            this.transitionLock.tryAcquire(10, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            logger.warn("Clustered Broadcast Stream Task was interrupted while waiting for the Transition Lock.");
        }
    }

    private void waitExecutionUnlock(final STATE nextState) {
        if (this.state == STATE.STOP || this.state == STATE.SHUTDOWN
                || this.state == STATE.ERROR) {
            return;
        }
        this.nextState = nextState;
        logger.info("Waiting to transition to state: {} ...",
                nextState.toString());
        try {
            this.transitionLock.acquire();
        } catch (InterruptedException e) {
            logger.warn("Clustered Broadcast Stream Task was interrupted while waiting for the Transition Lock.");
        }
        this.transitionLock.release();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.bmh.comms.broadcast.BroadcastStreamTask#
     * invokeTransitionExecution()
     */
    @Override
    protected void invokeTransitionExecution() {
        if (this.state == STATE.STOP || this.state == STATE.SHUTDOWN
                || this.state == STATE.ERROR) {
            return;
        }
        this.state = this.nextState;
        this.transitionLock.release();
    }
}