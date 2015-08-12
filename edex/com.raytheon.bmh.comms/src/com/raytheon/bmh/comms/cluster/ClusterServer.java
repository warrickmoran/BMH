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
package com.raytheon.bmh.comms.cluster;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raytheon.bmh.comms.AbstractServerThread;
import com.raytheon.bmh.comms.CommsManager;
import com.raytheon.uf.common.serialization.SerializationException;
import com.raytheon.uf.common.serialization.SerializationUtil;
import com.raytheon.uf.edex.bmh.comms.CommsConfig;
import com.raytheon.uf.edex.bmh.comms.CommsHostConfig;

/**
 * 
 * Server listening for connections from comms managers on different hosts
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Sep 17, 2014  3399     bsteffen    Initial creation
 * Oct 10, 2014  3656     bkowal      Updates to allow sending other message
 *                                    types to cluster members.
 * Nov 11, 2014  3762     bsteffen    Add load balancing of dac transmits.
 * Feb 10, 2015  4071     bsteffen    Synchronize State.
 * Apr 07, 2015  4370     rjpeter     Added sendConfigCheck.
 * Apr 08, 2015  4368     rjpeter     Fix ClusterCommunicator race condition.
 * Aug 04, 2015  4424     bkowal      Added {@link #reconfigure(Set)}.
 * Aug 10, 2015  4711     rjpeter     Add cluster heartbeat.
 * Aug 11, 2015  4372     bkowal      Added locking/unlocking of dacs participating in a
 *                                    live broadcast to delay load balancing.
 * Aug 12, 2015  4424     bkowal      Eliminate Dac Transmit Key.
 *                                    
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class ClusterServer extends AbstractServerThread {

    /**
     * When load balancing, after a remote comms manager has disconnect from a
     * dac this is how long(in ms) to try to connect before it is considered a
     * failed connection and the dac is returned to the cluster.
     */
    private static final int REQUEST_TIMEOUT_INTERVAL = 10 * 1000;

    /**
     * When connected to another comms manager. This is the maximum amount of
     * time allowed between messages.
     */
    private static final int CLUSTER_TIMEOUT_INTERVAL = 60 * 1000;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final CommsManager manager;

    private final Map<String, ClusterCommunicator> communicators = new ConcurrentHashMap<>();

    private final Object communicatorLock = new Object();

    private final Set<String> configuredAddresses = new CopyOnWriteArraySet<>();

    private final ClusterStateMessage state = new ClusterStateMessage();

    private long requestTimeout = Long.MAX_VALUE;

    private String localIp;

    private final List<String> unavailableLoadBalanceCandidates = new ArrayList<>();

    /**
     * Create a server for listening to dac transmit applications.
     * 
     * @param config
     *            the config to use for this server.
     * @throws IOException
     */
    public ClusterServer(CommsManager manager, CommsConfig config)
            throws IOException {
        super(config.getClusterPort());
        this.manager = manager;
        if (config.getClusterHosts() == null) {
            logger.warn("No cluster members in config, continuing without clustering.");
            return;
        }
    }

    public void attempClusterConnections(CommsConfig config) {
        if (config.getClusterHosts() == null) {
            for (ClusterCommunicator communicator : communicators.values()) {
                communicator.disconnect();
            }
            communicators.clear();
            return;
        }
        Set<String> configuredAddresses = new HashSet<>();
        for (CommsHostConfig host : config.getClusterHosts()) {
            try {
                if (host.isLocalHost()) {
                    localIp = host.getIpAddress();
                    break;
                }
            } catch (IOException e) {
                logger.error("Unable to check location of cluster member: {}",
                        host.getIpAddress(), e);
                continue;
            }
        }

        if (localIp == null) {
            logger.error("Local host was not found in the cluster configuration, shutting down.");
            manager.shutdown();
            return;
        }

        long timeoutThreshold = System.currentTimeMillis()
                - CLUSTER_TIMEOUT_INTERVAL;
        for (CommsHostConfig host : config.getClusterHosts()) {
            if (localIp.equals(host.getIpAddress())) {
                continue;
            }

            String remoteAddress = host.getIpAddress();
            InetAddress address = null;
            try {
                address = InetAddress.getByName(remoteAddress);
            } catch (UnknownHostException e) {
                logger.error("Unable to resolve host of cluster member: {}",
                        host.getIpAddress(), e);
                continue;
            }

            configuredAddresses.add(remoteAddress);

            ClusterCommunicator communicator = communicators.get(remoteAddress);

            if (communicator != null) {
                if (communicator.getTimeLastMessageReceived() < timeoutThreshold) {
                    logger.error(
                            "Cluster time out for cluster member: {}.  Have not received heartbeat message within threshold",
                            remoteAddress);
                    communicator.disconnect();
                } else {
                    communicator.send(new ClusterHeartbeatMessage(localIp));
                    continue;
                }
            }

            try {
                logger.info("Initiating cluster communication with {}",
                        remoteAddress);
                Socket socket = new Socket(address, config.getClusterPort());
                socket.setTcpNoDelay(true);
                communicator = new ClusterCommunicator(manager, socket,
                        host.getIpAddress());
                if (communicator.send(localIp)) {
                    addCommunicator(communicator);
                }
            } catch (Exception e) {
                logger.warn("Unable to connect to cluster member: {}",
                        remoteAddress);
                if (communicator != null) {
                    communicator.disconnect();
                }
            }
        }

        this.configuredAddresses.addAll(configuredAddresses);
        this.configuredAddresses.retainAll(configuredAddresses);

        Set<String> disconnectSet = new HashSet<>(communicators.keySet());
        disconnectSet.removeAll(configuredAddresses);
        for (String address : disconnectSet) {
            ClusterCommunicator communicator = communicators.remove(address);
            if (communicator != null) {
                communicator.shutdown();
            }
        }
    }

    public boolean isConnected(final String transmitterGroup) {
        for (ClusterCommunicator communicator : this.communicators.values()) {
            if (communicator.isConnected(transmitterGroup)) {
                return true;
            }
        }
        return false;
    }

    public boolean isRequested(final String transmitterGroup) {
        for (ClusterCommunicator communicator : this.communicators.values()) {
            if (communicator.isRequested(transmitterGroup)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Shutdown all dac transmit communications as well as the server. This
     * should only be called for cluster failover.
     */
    @Override
    public void shutdown() {
        super.shutdown();
        for (ClusterCommunicator communicator : communicators.values()) {
            communicator.shutdown();
        }
        communicators.clear();
    }

    @Override
    protected void handleConnection(Socket socket)
            throws SerializationException, IOException {
        InetAddress address = socket.getInetAddress();
        String id = getRemoteServerHost(socket);
        if (configuredAddresses.contains(id)) {
            if (id != null) {
                logger.info("Received new cluster request from {}", id);
                ClusterCommunicator communicator = new ClusterCommunicator(
                        manager, socket, id);
                addCommunicator(communicator);
            } else {
                logger.warn(
                        "Cluster request did not send server id as first message, rejecting request from {}",
                        address.getHostName());
                socket.close();
            }
        } else {
            // reject socket, not from known host
            logger.warn(
                    "Cluster request from unknown host, rejecting request from {}",
                    address.getHostName());
            socket.close();
        }
    }

    protected String getRemoteServerHost(Socket socket) {
        try {
            // first message from socket is to be its ip from comms.xml
            return SerializationUtil.transformFromThrift(String.class,
                    socket.getInputStream());
        } catch (Throwable e) {
            logger.error(
                    "Error getting server id from remote server: {}.  Rejecting cluster request",
                    socket.getInetAddress().getHostAddress(), e);
            return null;
        }
    }

    protected void addCommunicator(ClusterCommunicator communicator) {
        String remoteAddress = communicator.getClusterId();
        synchronized (communicatorLock) {
            ClusterCommunicator prev = communicators.get(remoteAddress);

            if (prev != null) {
                // reject new communicator
                if (prev.remoteAccepted()
                        || localIp.compareTo(remoteAddress) < 0) {
                    communicator.disconnect();
                    return;
                }

                // new communicator better, close prev
                prev.disconnect();
            }

            communicators.put(remoteAddress, communicator);
            communicator.start();
            synchronized (state) {
                communicator.sendState(state);
            }
        }
    }

    public void dacConnectedLocal(final String transmitterGroup) {
        synchronized (state) {
            state.add(transmitterGroup);
            if (state.isRequestedTransmitter(transmitterGroup)) {
                state.setRequestedTransmitter(null);
                requestTimeout = Long.MAX_VALUE;
            }
            sendStateToAll();
            this.unlockLoadBalanceDac(transmitterGroup);
        }
    }

    public void dacDisconnectedLocal(final String transmitterGroup) {
        synchronized (state) {
            state.remove(transmitterGroup);
            sendStateToAll();
            this.unlockLoadBalanceDac(transmitterGroup);
        }
    }

    public void dacDisconnectedRemote(final String transmitterGroup) {
        synchronized (state) {
            if (state.isRequestedTransmitter(transmitterGroup)) {
                requestTimeout = System.currentTimeMillis()
                        + REQUEST_TIMEOUT_INTERVAL;
            }
            this.unlockLoadBalanceDac(transmitterGroup);
        }
    }

    private void sendStateToAll() {
        Iterator<ClusterCommunicator> it = communicators.values().iterator();
        while (it.hasNext()) {
            ClusterCommunicator communicator = it.next();
            if (communicator.isAlive()) {
                if (communicator.sendState(state) == false) {
                    it.remove();
                }
            } else {
                it.remove();
            }
        }
    }

    /**
     * Used to share the specified data with all cluster members
     * 
     * @param data
     *            the data to shared
     * @return the number of cluster members that the data has been successfully
     *         shared with
     */
    public int sendDataToAll(Object data) {
        Iterator<ClusterCommunicator> it = communicators.values().iterator();
        int recipients = 0;
        while (it.hasNext()) {
            ClusterCommunicator communicator = it.next();
            if (communicator.isAlive()) {
                if (communicator.send(data)) {
                    ++recipients;
                } else {
                    it.remove();
                }
            } else {
                it.remove();
            }
        }

        return recipients;
    }

    public void balanceDacTransmits(boolean allDacsRunning) {
        synchronized (state) {
            boolean pendingRequest = state.hasRequestedTransmitter();
            boolean requestFailed = System.currentTimeMillis() > requestTimeout;
            if (allDacsRunning && (pendingRequest == false)
                    && (requestFailed == false)) {
                String overloadId = null;
                ClusterStateMessage overloaded = state;
                for (ClusterCommunicator communicator : communicators.values()) {
                    ClusterStateMessage other = communicator.getClusterState();
                    if ((other == null) || other.hasRequestedTransmitter()) {
                        return;
                    }
                    if (other.getConnectedTransmitters().size() > overloaded
                            .getConnectedTransmitters().size()) {
                        overloaded = other;
                        overloadId = communicator.getClusterId();
                    }
                }
                if ((overloaded.getConnectedTransmitters().size() - 1) > state
                        .getConnectedTransmitters().size()) {
                    /*
                     * Remove any transmitters that cannot be load balanced from
                     * the overloaded keys list.
                     */
                    synchronized (this.unavailableLoadBalanceCandidates) {
                        Iterator<String> iterator = overloaded
                                .getConnectedTransmitters().iterator();
                        while (iterator.hasNext()) {
                            if (this.unavailableLoadBalanceCandidates
                                    .contains(iterator.next())) {
                                iterator.remove();
                            }
                        }
                    }

                    logger.info(
                            "To balance the load 1 dac transmit has been requested from {}",
                            overloadId);
                    /*
                     * TODO its entirely possible for 2 cluster members to be
                     * here at the same time and both request the same key, to
                     * minimize conflict it would be better if each requested a
                     * different key using a better key selection mechanism.
                     */
                    String requestedGroup = overloaded
                            .getConnectedTransmitters().get(0);
                    state.setRequestedTransmitter(requestedGroup);
                    sendStateToAll();
                }
            } else if (pendingRequest && requestFailed) {
                logger.error(
                        "Load balancing has been disabled due to failure to start a requested dac: {}.",
                        state.getRequestedTransmitter());
                state.setRequestedTransmitter(null);
                sendStateToAll();
            }
        }
    }

    /**
     * Tells the cluster members that a new config was read.
     */
    public void sendConfigCheck() {
        sendDataToAll(new ClusterConfigMessage());
    }

    /**
     * Reconfiguration method that verifies that the dac transmit that the
     * cluster server may have requested is still defined in the configuration.
     * If not defined, the requested key is removed.
     * 
     * @param activeDacTransmits
     *            a {@link Set} of the {@link DacTransmitKey}s associated with
     *            dac transmits defined in the latest version of the
     *            configuration.
     */
    public void reconfigure(Set<String> activeDacTransmits) {
        synchronized (state) {
            if (state.hasRequestedTransmitter()) {
                final String requestedTransmitter = state
                        .getRequestedTransmitter();
                if (activeDacTransmits.contains(requestedTransmitter) == false) {
                    /*
                     * No longer wait for the process to start locally because
                     * it is no longer even enabled.
                     */
                    state.setRequestedTransmitter(null);
                    requestTimeout = Long.MAX_VALUE;
                }
            }
        }

        synchronized (this.unavailableLoadBalanceCandidates) {
            Iterator<String> iterator = this.unavailableLoadBalanceCandidates
                    .iterator();
            while (iterator.hasNext()) {
                if (activeDacTransmits.contains(iterator.next()) == false) {
                    iterator.remove();
                }
            }
        }
    }

    public void lockLoadBalanceDac(final String transmitterGroup) {
        synchronized (this.unavailableLoadBalanceCandidates) {
            logger.info(
                    "Preventing load balancing from occuring for transmitter: {}.",
                    transmitterGroup);
            this.unavailableLoadBalanceCandidates.add(transmitterGroup);
        }
    }

    public void unlockLoadBalanceDac(final String transmitterGroup) {
        synchronized (this.unavailableLoadBalanceCandidates) {
            if (this.unavailableLoadBalanceCandidates.remove(transmitterGroup)) {
                logger.info("Allowing the load balancing of transmitter: {}.",
                        transmitterGroup);
            }
        }
    }
}