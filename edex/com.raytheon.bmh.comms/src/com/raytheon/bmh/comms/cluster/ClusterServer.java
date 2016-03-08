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
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raytheon.bmh.comms.AbstractServer;
import com.raytheon.bmh.comms.CommsManager;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.edex.bmh.comms.CommsConfig;
import com.raytheon.uf.edex.bmh.comms.CommsHostConfig;
import com.raytheon.uf.edex.bmh.comms.DacConfig;

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
 * Aug 19, 2015  4764     bkowal      Handle the case when there are not any transmitters
 *                                    that can be load balanced due to restrictions.
 * Oct 23, 2015  5029     rjpeter     Added removeCommunicator, fix addCommunicator to not
 *                                    prefer a disconnected communicator over a new one,
 *                                    load balance failure only effects specific transmitter
 *                                    that failed to start and load balance state will clear
 *                                    if the transmitter ever stops running on the remote host.
 * Oct 28, 2015  5029     rjpeter     Allow multiple dac transmits to be requested.
 * Nov 11, 2015  5114     rjpeter     Updated CommsManager to use a single port.
 * Dec 15, 2015  5114     rjpeter     Updated SocketListener to use a ThreadPool.
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class ClusterServer extends AbstractServer {

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

    private final ConcurrentMap<String, ClusterCommunicator> communicators = new ConcurrentHashMap<>();

    /*
     * Lock used when adding or removing from communicators map. Utilized to
     * ensure two sockets not processed concurrently for the same host.
     */
    private final Object communicatorLock = new Object();

    private final Set<String> configuredAddresses = new CopyOnWriteArraySet<>();

    private final ClusterStateMessage state = new ClusterStateMessage();

    private final ConcurrentMap<String, Long> requestTimeout = new ConcurrentHashMap<String, Long>();

    private String localIp;

    private final List<String> unavailableLoadBalanceCandidates = new ArrayList<>();

    private volatile int totalDacTransmits = 0;

    /**
     * Create a server for listening to dac transmit applications.
     * 
     * @param config
     *            the config to use for this server.
     * @throws IOException
     */
    public ClusterServer(CommsManager manager, CommsConfig config)
            throws IOException {
        super(manager.getSocketListener());
        this.manager = manager;
        if (config.getClusterHosts() == null) {
            logger.warn("No cluster members in config, continuing without clustering.");
            return;
        }

        for (DacConfig dConfig : config.getDacs()) {
            totalDacTransmits += dConfig.getChannels().size();
        }
    }

    public void attempClusterConnections(CommsConfig config) {
        if (config.getClusterHosts() == null) {
            synchronized (communicatorLock) {
                logger.info("No cluster hosts define in config, disconnecting from any clustered hosts");
                for (ClusterCommunicator communicator : communicators.values()) {
                    logger.info("Cluster host: {}, disconnecting...",
                            communicator.getClusterId());
                    communicator.disconnect();
                }
                communicators.clear();
                return;
            }
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
                            "Cluster time out for cluster member: {}.  Have not received heartbeat message within threshold of {}, disconnecting...",
                            remoteAddress,
                            TimeUtil.prettyDuration(CLUSTER_TIMEOUT_INTERVAL));
                    communicator.disconnect();
                } else {
                    communicator.send(new ClusterHeartbeatMessage(localIp));
                    continue;
                }
            }

            try {
                logger.info("Initiating cluster communication with {}",
                        remoteAddress);
                Socket socket = new Socket(address, config.getPort());
                socket.setTcpNoDelay(true);
                communicator = new ClusterCommunicator(manager, this, socket,
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

        synchronized (communicatorLock) {
            Set<String> disconnectSet = new HashSet<>(communicators.keySet());
            disconnectSet.removeAll(configuredAddresses);

            for (String address : disconnectSet) {
                ClusterCommunicator communicator = communicators
                        .remove(address);
                if (communicator != null) {
                    logger.info(
                            "Cluster member {} no longer configured as part of cluster.  Disconnecting...",
                            address);
                    communicator.shutdown();
                }
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
    protected void shutdownInternal() {
        synchronized (communicatorLock) {
            for (ClusterCommunicator communicator : communicators.values()) {
                communicator.shutdown();
            }
            communicators.clear();
        }
    }

    @Override
    public boolean handleConnection(Socket socket, Object obj) {
        if (!(obj instanceof String)) {
            logger.warn("Received unexpected message with type: "
                    + obj.getClass().getName() + ". Disconnecting ...");
            return true;
        }

        // first message from socket is to be its ip from comms.xml
        String id = (String) obj;
        InetAddress address = socket.getInetAddress();

        if (!configuredAddresses.contains(id)) {
            // reject socket, not from known host
            logger.warn(
                    "Cluster request from unknown host, rejecting request from {}",
                    address.getHostName());
            return true;
        }

        logger.info("Received new cluster request from {}", id);
        ClusterCommunicator communicator = new ClusterCommunicator(manager,
                this, socket, id);
        addCommunicator(communicator);
        return false;
    }

    protected void addCommunicator(ClusterCommunicator communicator) {
        String remoteAddress = communicator.getClusterId();
        synchronized (communicatorLock) {
            ClusterCommunicator prev = communicators.get(remoteAddress);

            if (prev != null) {
                // reject new communicator?
                if (prev.remoteAccepted()
                        || ((localIp.compareTo(remoteAddress) < 0) && prev
                                .isConnected())) {
                    logger.info(
                            "Already connected to {}, closing new connection",
                            remoteAddress);
                    communicator.disconnect();
                    return;
                }

                // new communicator better, close prev
                logger.info(
                        "Received new connection for {}, closing previous connection",
                        remoteAddress);
                prev.disconnect();
            }

            communicators.put(remoteAddress, communicator);
            communicator.start();
            synchronized (state) {
                communicator.sendState(state);
            }
        }
    }

    /**
     * Removes a communicator from internal map. Should only be called by
     * ClusterServer.disconnect().
     * 
     * @param communicator
     */
    protected void removeCommunicator(ClusterCommunicator communicator) {
        synchronized (communicatorLock) {
            communicators.remove(communicator.getClusterId(), communicator);
        }
    }

    public void dacConnectedLocal(final String transmitterGroup) {
        synchronized (state) {
            state.add(transmitterGroup);
            state.removeRequestedTransmitter(transmitterGroup);
            requestTimeout.remove(transmitterGroup);
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
                requestTimeout.put(transmitterGroup, System.currentTimeMillis()
                        + REQUEST_TIMEOUT_INTERVAL);
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
            Set<String> failedRequests = new HashSet<>();
            long currentTime = System.currentTimeMillis();
            for (Map.Entry<String, Long> entry : requestTimeout.entrySet()) {
                if (currentTime > entry.getValue()) {
                    failedRequests.add(entry.getKey());
                }
            }

            if (allDacsRunning && (pendingRequest == false)
                    && (failedRequests.isEmpty())) {
                /* determine total number of transmitters */
                int requiredForBalance = totalDacTransmits
                        / (communicators.size() + 1);
                int numConnected = state.getConnectedTransmitters().size();
                int numToRequest = requiredForBalance - numConnected;

                NavigableMap<Integer, ClusterCommunicator> commMap = new TreeMap<>();
                for (ClusterCommunicator communicator : communicators.values()) {
                    ClusterStateMessage otherState = communicator
                            .getClusterState();
                    if ((otherState == null)
                            || otherState.hasRequestedTransmitter()) {
                        /*
                         * another comms manager already clustering or just
                         * connected
                         */
                        return;
                    }
                    commMap.put(otherState.getConnectedTransmitters().size(),
                            communicator);
                }

                /*
                 * double check in a large cluster that everything is balanced,
                 * say with 7 transmitters across 4 nodes (1, 1, 1, 4)
                 */
                if (numToRequest == 0) {
                    // map sorted by least number of connections
                    for (Integer key : commMap.keySet()) {
                        if (key < numConnected) {
                            /*
                             * we have more than another communicator, don't
                             * request anything
                             */
                            break;
                        } else if (key > (numConnected + 1)) {
                            /*
                             * another entry has more than 1 more than me,
                             * request 1
                             */
                            numToRequest = 1;
                            break;
                        }
                    }
                }

                if (numToRequest > 0) {
                    int numRequested = 0;

                    /*
                     * Iterate in order of most connected transmitters
                     */
                    for (ClusterCommunicator communicator : commMap
                            .descendingMap().values()) {
                        String otherId = communicator.getClusterId();
                        ClusterStateMessage otherState = communicator
                                .getClusterState();
                        Set<String> otherTrans = new HashSet<>(
                                otherState.getConnectedTransmitters());
                        if (otherTrans.size() > requiredForBalance) {
                            int numAllowedToTake = otherTrans.size()
                                    - requiredForBalance;

                            synchronized (this.unavailableLoadBalanceCandidates) {
                                /*
                                 * Remove any transmitters that cannot be load
                                 * balanced.
                                 */
                                otherTrans
                                        .removeAll(unavailableLoadBalanceCandidates);
                            }

                            if (otherTrans.isEmpty() == false) {
                                Iterator<String> iter = otherTrans.iterator();

                                while ((numRequested < numToRequest)
                                        && (numAllowedToTake > 0)
                                        && iter.hasNext()) {
                                    String group = iter.next();
                                    logger.info(
                                            "To balance the load, {} dac transmit has been requested from {}",
                                            group, otherId);
                                    state.addRequestedTransmitter(group);
                                    numRequested++;
                                    numAllowedToTake--;
                                }
                            } else {
                                logger.info(
                                        "Unable to load balance with {}, all transmitters are unavailable for balancing",
                                        otherId);
                            }
                        }
                    }

                    if (state.hasRequestedTransmitter()) {
                        sendStateToAll();
                    }
                }
            } else if (pendingRequest && (failedRequests.isEmpty() == false)) {
                for (String transmitter : failedRequests) {
                    logger.error(
                            "Load balancing for {} has been disabled due to failure to connect to DAC in {}",
                            transmitter,
                            TimeUtil.prettyDuration(REQUEST_TIMEOUT_INTERVAL));
                    lockLoadBalanceDac(transmitter);
                    requestTimeout.remove(transmitter);
                    state.removeRequestedTransmitter(transmitter);
                }
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
        totalDacTransmits = activeDacTransmits.size();

        synchronized (state) {
            if (state.hasRequestedTransmitter()) {
                for (String transmitter : state.getRequestedTransmitters()) {
                    if (activeDacTransmits.contains(transmitter) == false) {
                        /*
                         * No longer wait for the process to start locally
                         * because it is no longer enabled.
                         */
                        state.remove(transmitter);
                        requestTimeout.remove(transmitter);
                    }
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

    @Override
    protected Set<Class<?>> getTypesHandled() {
        return Collections.<Class<?>> singleton(String.class);
    }
}