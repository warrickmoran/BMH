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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raytheon.bmh.comms.AbstractServer;
import com.raytheon.bmh.comms.CommsManager;
import com.raytheon.bmh.comms.cluster.ClusterServer;
import com.raytheon.bmh.comms.dactransmit.DacTransmitCommunicator;
import com.raytheon.bmh.comms.dactransmit.DacTransmitServer;
import com.raytheon.uf.common.bmh.broadcast.AudioRegulationSettingsCommand;
import com.raytheon.uf.common.bmh.broadcast.BroadcastStatus;
import com.raytheon.uf.common.bmh.broadcast.ILiveBroadcastMessage;
import com.raytheon.uf.common.bmh.broadcast.IOnDemandBroadcastMsg;
import com.raytheon.uf.common.bmh.broadcast.LiveBroadcastGroupsMessage;
import com.raytheon.uf.common.bmh.broadcast.LiveBroadcastListGroupsCommand;
import com.raytheon.uf.common.bmh.broadcast.LiveBroadcastStartCommand;
import com.raytheon.uf.common.bmh.broadcast.OnDemandBroadcastConstants.MSGSOURCE;
import com.raytheon.uf.common.bmh.broadcast.SAMEPaddingSettingsCommand;
import com.raytheon.uf.common.bmh.broadcast.TransmitterMaintenanceCommand;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.serialization.SerializationException;
import com.raytheon.uf.common.serialization.SerializationUtil;
import com.raytheon.uf.edex.bmh.LoadedSAMEPaddingConfiguration;
import com.raytheon.uf.edex.bmh.audio.LoadedAudioRegulationConfiguration;

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
 * Oct 22, 2014 3687       bsteffen    Fix NPE in edge case.
 * Nov 15, 2014 3630       bkowal      Support alignment tests.
 * Dec 1, 2014  3797       bkowal      Support broadcast clustering.
 * Dec 12, 2014 3603       bsteffen    Reuse alignment task for transfer tones.
 * Feb 05, 2015 3743       bsteffen    Ability to return groups.
 * Aug 12, 2015 4424       bkowal      Eliminate Dac Transmit Key.
 * Aug 24, 2015 4770       bkowal      Handle {@link AudioRegulationSettingsCommand}.
 * Aug 25, 2015 4775       bkowal      Added {@link #remoteDacConnections} to track remote
 *                                     dac transmit connections.
 * Nov 11, 2015 5114       rjpeter     Updated CommsManager to use a single port.
 * Dec 15, 2015 5114       rjpeter     Updated SocketListener to use a ThreadPool.
 * Sep 30, 2016 5912       bkowal      Handle {@link SAMEPaddingSettingsCommand}.
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class BroadcastStreamServer extends AbstractServer {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ClusterServer clusterServer;

    private final DacTransmitServer transmitServer;

    private final CommsManager commsManager;

    private final ConcurrentMap<String, AbstractBroadcastingTask> broadcastStreamingTasksMap = new ConcurrentHashMap<>();

    private final List<String> availableDacConnections = new ArrayList<>();

    /*
     * Keep track of Dac Transmits connected to other Comms Managers so that
     * they can be included in the {@link List} of Dac Transmits that are
     * returned to CAVE when they are requested directly from us.
     */
    private final Set<String> remoteDacConnections = new HashSet<>();

    /**
     * @param port
     * @throws IOException
     */
    public BroadcastStreamServer(ClusterServer clusterServer,
            final DacTransmitServer transmitServer,
            final CommsManager commsManager) throws IOException {
        super(commsManager.getSocketListener());
        this.clusterServer = clusterServer;
        this.transmitServer = transmitServer;
        this.commsManager = commsManager;
    }

    @Override
    public boolean handleConnection(Socket socket, Object obj) {
        if (obj instanceof LiveBroadcastListGroupsCommand) {
            List<String> availableDacs = new ArrayList<>();
            synchronized (this.availableDacConnections) {
                availableDacs.addAll(this.availableDacConnections);
            }
            synchronized (this.remoteDacConnections) {
                availableDacs.addAll(this.remoteDacConnections);
            }

            try {
                SerializationUtil.transformToThriftUsingStream(
                        new LiveBroadcastGroupsMessage(commsManager
                                .getOperational(), availableDacs), socket
                                .getOutputStream());
            } catch (SerializationException | IOException e) {
                logger.error("Error occurred sending response to "
                        + LiveBroadcastListGroupsCommand.class.getSimpleName()
                        + " request", e);
            }
            return true;
        }

        if (obj instanceof AudioRegulationSettingsCommand) {
            try {
                SerializationUtil.transformToThriftUsingStream(
                        LoadedAudioRegulationConfiguration.getConfiguration(),
                        socket.getOutputStream());
            } catch (Exception e) {
                logger.error("Error occurred sending response to "
                        + AudioRegulationSettingsCommand.class.getSimpleName()
                        + " request", e);
            }

            return true;
        }
        
        if (obj instanceof SAMEPaddingSettingsCommand) {
            try {
                SerializationUtil.transformToThriftUsingStream(
                        LoadedSAMEPaddingConfiguration.getConfiguration(),
                        socket.getOutputStream());
            } catch (Exception e) {
                logger.error("Error occurred sending response to "
                        + SAMEPaddingSettingsCommand.class.getSimpleName()
                        + " request", e);
            }

            return true;            
        }

        if (!(obj instanceof IOnDemandBroadcastMsg)) {
            logger.warn("Received unexpected message with type: "
                    + obj.getClass().getName() + ". Disconnecting ...");
            return true;
        }

        IOnDemandBroadcastMsg command = (IOnDemandBroadcastMsg) obj;

        if (this.areTransmitterGroupsAvailable(command, socket) == false) {
            /*
             * An existing {@link AbstractBroadcastingTask} is already using one
             * of the requested {@link TransmitterGroup}.
             */
            return true;
        }

        AbstractBroadcastingTask task = null;
        if (command instanceof LiveBroadcastStartCommand) {
            task = this.handleLiveBroadcastCommand(
                    (LiveBroadcastStartCommand) command, socket);
        } else if (command instanceof TransmitterMaintenanceCommand) {
            task = this.handleTransmitterAlignmentCommand(
                    (TransmitterMaintenanceCommand) command, socket);
        } else {
            logger.error(
                    "On Demand Broadcast Command {} is not currently supported!",
                    command.getClass().getName());
            return true;
        }

        this.runDelegate(task);
        return false;
    }

    /**
     * Verifies that no other {@link AbstractBroadcastTask} is currently using
     * one of the requested {@link TransmitterGroup}s specified in the
     * {@link IOnDemandBroadcastMsg}.
     * 
     * @param command
     *            the specified {@link IOnDemandBroadcastMsg}
     * @return true, if the requested {@link TransmitterGroup}s are available;
     *         false, otherwise.
     */
    private synchronized boolean areTransmitterGroupsAvailable(
            final IOnDemandBroadcastMsg command, final Socket socket) {
        AbstractBroadcastingTask conflictingTask = null;

        for (AbstractBroadcastingTask task : this.broadcastStreamingTasksMap
                .values()) {
            /*
             * {@link List} does not natively support any type of intersection
             * operation ...
             */
            for (TransmitterGroup tg : command.getTransmitterGroups()) {
                if (task.getTransmitterGroups().contains(tg)) {
                    conflictingTask = task;
                    break;
                }
            }
        }

        if (conflictingTask != null) {
            /*
             * Notify the client that the requested {@link TransmitterGroup}s
             * are not available.
             */
            StringBuilder clientMsg = new StringBuilder(
                    "One or multiple of the requested Transmitter Group(s) are already being used by ");
            clientMsg.append(conflictingTask.getDescription());
            clientMsg.append(" ");
            clientMsg.append(conflictingTask.getName());
            clientMsg.append(".");

            BroadcastStatus status = new BroadcastStatus();
            status.setMsgSource(MSGSOURCE.COMMS);
            status.setBroadcastId(BroadcastStreamServer.class.getSimpleName());
            status.setStatus(false);
            status.setTransmitterGroups(command.getTransmitterGroups());
            status.setMessage(clientMsg.toString());

            try {
                SerializationUtil.transformToThriftUsingStream(status,
                        socket.getOutputStream());
            } catch (SerializationException | IOException e) {
                logger.error(
                        "Failed to send an unavailability status to the client.",
                        e);
                /*
                 * Failure will be indicated by the unexpected comms manager
                 * disconnect that will occur when this function exits.
                 * Although, the source of the failure will not be clear in that
                 * case. But, options are limited.
                 */
            }
        }

        return (conflictingTask == null);
    }

    private void runDelegate(AbstractBroadcastingTask task) {
        if (task == null) {
            return;
        }
        task.start();
        logger.info("Started {} {}.", task.getDescription(), task.getName());
        this.broadcastStreamingTasksMap.put(task.getName(), task);
    }

    /*
     * Presently there are only two possible options both of which are related
     * to special cases. So, at this point, we see no need to abstract and
     * generalize how the tasks are created.
     */
    private AbstractBroadcastingTask handleLiveBroadcastCommand(
            final LiveBroadcastStartCommand command, final Socket socket) {
        return new BroadcastStreamTask(socket, command, this,
                this.clusterServer, this.transmitServer);
    }

    private AbstractBroadcastingTask handleTransmitterAlignmentCommand(
            final TransmitterMaintenanceCommand command, final Socket socket) {
        return new MaintenanceTask(socket, command,
                this.commsManager.getCurrentConfigState(), this);
    }

    @Override
    protected void shutdownInternal() {
        for (AbstractBroadcastingTask task : this.broadcastStreamingTasksMap
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
    public void dacConnected(final String group) {
        synchronized (this.availableDacConnections) {
            if (this.availableDacConnections.contains(group)) {
                return;
            }
            logger.info("Adding dac connection to transmitter {}.", group);
            this.availableDacConnections.add(group);
        }
        for (AbstractBroadcastingTask task : this.broadcastStreamingTasksMap
                .values()) {
            task.dacConnectedToServer(group);
        }
    }

    public void dacConnectedRemote(final String group) {
        if (group == null) {
            return;
        }
        synchronized (this.remoteDacConnections) {
            if (this.remoteDacConnections.add(group)) {
                logger.info("Adding remote dac connection to transmitter {}.",
                        group);
            }
        }
    }

    /**
     * {@link BroadcastStreamServer#dacConnected()} and
     * {@link BroadcastStreamServer#dacDisconnected()} are only interested in
     * local activity because the {@link DacTransmitCommunicator} currently only
     * allows for local connections.
     */
    public void dacDisconnected(final String group) {
        if (group == null) {
            /*
             * Can occur if the config is changed while comms manager is
             * rebooting and dac transmits are running.
             */
            return;
        }
        synchronized (this.availableDacConnections) {
            this.availableDacConnections.remove(group);
        }
        for (AbstractBroadcastingTask task : this.broadcastStreamingTasksMap
                .values()) {
            task.dacDisconnectedFromServer(group);
        }
    }

    public void dacDisconnectedRemote(final String group) {
        if (group == null) {
            return;
        }
        synchronized (this.remoteDacConnections) {
            if (this.remoteDacConnections.remove(group)) {
                logger.info(
                        "Removing remote dac connection to transmitter {}.",
                        group);
            }
        }
    }

    public boolean isDacConnected(final String group) {
        synchronized (this.availableDacConnections) {
            return this.availableDacConnections.contains(group);
        }
    }

    public void broadcastTaskFinished(final String broadcastId) {
        this.broadcastStreamingTasksMap.remove(broadcastId);
    }

    public void handleBroadcastMsgExternal(ILiveBroadcastMessage msg) {
        if (msg.getMsgSource() == MSGSOURCE.DAC) {
            this.handleDacBroadcastMsg(msg);
        } else if (msg.getMsgSource() == MSGSOURCE.COMMS) {
            if (msg instanceof LiveBroadcastStartCommand) {
                AbstractBroadcastingTask task = new ClusteredBroadcastStreamTask(
                        (LiveBroadcastStartCommand) msg, this,
                        this.clusterServer, this.transmitServer);
                this.runDelegate(task);
            } else {
                BroadcastStreamTask task = this.getDelegateTask(msg);
                if (task == null) {
                    return;
                }
                task.handleClusteredBroadcastMsg(msg);
            }
        }
    }

    public void handleDacBroadcastMsg(ILiveBroadcastMessage msg) {
        if (msg == null) {
            return;
        }
        BroadcastStreamTask task = this.getDelegateTask(msg);
        if (task == null) {
            return;
        }
        task.handleDacBroadcastMsg(msg);
    }

    private BroadcastStreamTask getDelegateTask(ILiveBroadcastMessage msg) {
        AbstractBroadcastingTask task = this.broadcastStreamingTasksMap.get(msg
                .getBroadcastId());
        if (task == null) {
            // unlikely scenario
            logger.warn(
                    "Ignoring dac live broadcast {} msg. Broadcast {} does not exist or is no longer active.",
                    msg.getClass().getName(), msg.getBroadcastId());

            return null;
        }
        if ((task instanceof BroadcastStreamTask) == false) {
            // unlikely scenario
            logger.warn("Ignoring dac live broadcast {} msg. Broadcast {} is not a live streamed broadcast.");
            return null;
        }

        return (BroadcastStreamTask) task;
    }

    @Override
    protected Set<Class<?>> getTypesHandled() {
        Set<Class<?>> rval = new HashSet<>(4, 1);
        rval.add(LiveBroadcastListGroupsCommand.class);
        rval.add(AudioRegulationSettingsCommand.class);
        rval.add(SAMEPaddingSettingsCommand.class);
        rval.add(IOnDemandBroadcastMsg.class);
        return Collections.unmodifiableSet(rval);
    }
}