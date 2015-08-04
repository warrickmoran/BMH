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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raytheon.bmh.comms.AbstractServerThread;
import com.raytheon.bmh.comms.CommsManager;
import com.raytheon.bmh.comms.DacTransmitKey;
import com.raytheon.bmh.comms.cluster.ClusterStateMessage.ClusterDacTransmitKey;
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

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final CommsManager manager;

    private final Map<InetAddress, ClusterCommunicator> communicators = new ConcurrentHashMap<>();

    private final Object communicatorLock = new Object();

    private final Set<InetAddress> configuredAddresses = new CopyOnWriteArraySet<>();

    private final ClusterStateMessage state = new ClusterStateMessage();

    private long requestTimeout = Long.MAX_VALUE;

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
        Set<InetAddress> configuredAddresses = new HashSet<>();
        String localIp = null;
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

        for (CommsHostConfig host : config.getClusterHosts()) {
            if (localIp.equals(host.getIpAddress())) {
                continue;
            }

            InetAddress address = null;
            try {
                address = InetAddress.getByName(host.getIpAddress());
            } catch (UnknownHostException e) {
                logger.error("Unable to resolve host of cluster member: {}",
                        host.getIpAddress(), e);
                continue;
            }

            configuredAddresses.add(address);

            if (communicators.containsKey(address)) {
                continue;
            }

            ClusterCommunicator communicator = null;
            try {
                logger.info("Initiating cluster communication with {}",
                        host.getIpAddress());
                Socket socket = new Socket(address, config.getClusterPort());
                socket.setTcpNoDelay(true);
                communicator = new ClusterCommunicator(manager, socket, localIp);
                if (communicator.send(localIp)) {
                    addCommunicator(communicator, address);
                }
            } catch (Exception e) {
                logger.warn("Unable to connect to cluster member: {}",
                        host.getIpAddress());
                if (communicator != null) {
                    communicator.disconnect();
                }
            }
        }

        this.configuredAddresses.addAll(configuredAddresses);
        this.configuredAddresses.retainAll(configuredAddresses);

        Set<InetAddress> disconnectSet = new HashSet<>(communicators.keySet());
        disconnectSet.removeAll(configuredAddresses);
        for (InetAddress address : disconnectSet) {
            ClusterCommunicator communicator = communicators.remove(address);
            if (communicator != null) {
                communicator.shutdown();
            }
        }
    }

    public boolean isConnected(DacTransmitKey key) {
        for (ClusterCommunicator communicator : this.communicators.values()) {
            if (communicator.isConnected(key)) {
                return true;
            }
        }
        return false;
    }

    public boolean isRequested(DacTransmitKey key) {
        for (ClusterCommunicator communicator : this.communicators.values()) {
            if (communicator.isRequested(key)) {
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
        if (configuredAddresses.contains(address)) {
            String id = getRemoteServerUniqueId(socket);
            if (id != null) {
                logger.info("Received new cluster request from {}", id);
                ClusterCommunicator communicator = new ClusterCommunicator(
                        manager, socket, id);
                addCommunicator(communicator, address);
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

    protected String getRemoteServerUniqueId(Socket socket) {
        try {
            // first message from socket is to be its id
            return SerializationUtil.transformFromThrift(String.class,
                    socket.getInputStream());
        } catch (Throwable e) {
            logger.error(
                    "Error getting server id from remote server: {}.  Rejecting cluster request",
                    socket.getInetAddress().getHostAddress(), e);
            return null;
        }
    }

    protected void addCommunicator(ClusterCommunicator communicator,
            InetAddress address) {
        synchronized (communicatorLock) {
            ClusterCommunicator prev = communicators.get(address);

            if (prev != null) {
                // reject new communicator
                if (prev.remoteAccepted()
                        || prev.getUniqueId().compareTo(
                                communicator.getUniqueId()) < 0) {
                    communicator.disconnect();
                    return;
                }

                // new communicator better, close prev
                prev.disconnect();
            }

            communicators.put(address, communicator);
            communicator.start();
            synchronized (state) {
                communicator.sendState(state);
            }
        }
    }

    public void dacConnectedLocal(DacTransmitKey key) {
        synchronized (state) {
            state.add(key);
            if (state.isRequestedKey(key)) {
                state.setRequestedKey(null);
                requestTimeout = Long.MAX_VALUE;
            }
            sendStateToAll();
        }
    }

    public void dacDisconnectedLocal(DacTransmitKey key) {
        synchronized (state) {
            state.remove(key);
            sendStateToAll();
        }
    }

    public void dacDisconnectedRemote(DacTransmitKey key) {
        synchronized (state) {
            if (state.isRequestedKey(key)) {
                requestTimeout = System.currentTimeMillis()
                        + REQUEST_TIMEOUT_INTERVAL;
            }
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
            boolean pendingRequest = state.hasRequestedKey();
            boolean requestFailed = System.currentTimeMillis() > requestTimeout;
            if (allDacsRunning && (pendingRequest == false)
                    && (requestFailed == false)) {
                String overloadId = null;
                ClusterStateMessage overloaded = state;
                for (ClusterCommunicator communicator : communicators.values()) {
                    ClusterStateMessage other = communicator.getClusterState();
                    if ((other == null) || other.hasRequestedKey()) {
                        return;
                    }
                    if (other.getKeys().size() > overloaded.getKeys().size()) {
                        overloaded = other;
                        overloadId = communicator.getClusterId();
                    }
                }
                if ((overloaded.getKeys().size() - 1) > state.getKeys().size()) {
                    logger.info(
                            "To balance the load 1 dac transmit has been requested from {}",
                            overloadId);
                    /*
                     * TODO its entirely possible for 2 cluster members to be
                     * here at the same time and both request the same key, to
                     * minimize conflict it would be better if each requested a
                     * different key using a better key selection mechanism.
                     */
                    ClusterDacTransmitKey request = overloaded.getKeys().get(0);
                    state.setRequestedKey(request);
                    sendStateToAll();
                }
            } else if (pendingRequest && requestFailed) {
                logger.error(
                        "Load balancing has been disabled due to failure to start a requested dac: {}.",
                        state.getRequestedKey());
                state.setRequestedKey(null);
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
    public void reconfigure(Set<DacTransmitKey> activeDacTransmits) {
        synchronized (state) {
            if (state.hasRequestedKey()) {
                final DacTransmitKey requestedKey = state.getRequestedKey()
                        .toKey();
                if (activeDacTransmits.contains(requestedKey) == false) {
                    /*
                     * No longer wait for the process to start locally because
                     * it is no longer even enabled.
                     */
                    state.setRequestedKey(null);
                    requestTimeout = Long.MAX_VALUE;
                }
            }
        }
    }
}