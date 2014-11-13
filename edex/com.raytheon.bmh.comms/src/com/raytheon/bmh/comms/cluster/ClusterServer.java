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

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final CommsManager manager;

    private final Map<InetAddress, ClusterCommunicator> communicators = new ConcurrentHashMap<>();

    private Set<InetAddress> configuredAddresses;

    private ClusterStateMessage state = new ClusterStateMessage();

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
        Set<InetAddress> configuredAddresses = new CopyOnWriteArraySet<>();
        boolean localFound = false;
        for (CommsHostConfig host : config.getClusterHosts()) {
            try {
                if (host.isLocalHost()) {
                    localFound = true;
                    continue;
                }
            } catch (IOException e) {
                logger.error("Unable to check location of cluster member: {}",
                        host.getIpAddress(), e);
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
            try {
                Socket socket = new Socket(address, config.getClusterPort());
                socket.setTcpNoDelay(true);
                ClusterCommunicator communicator = new ClusterCommunicator(
                        manager, socket);
                communicator.start();
                communicator.sendState(state);
                ClusterCommunicator prev = communicators.put(address,
                        communicator);
                if (prev != null) {
                    prev.disconnect();
                }
            } catch (IOException e) {
                logger.warn("Unable to connect to cluster member: {}",
                        host.getIpAddress());
            }
        }
        this.configuredAddresses = configuredAddresses;
        Set<InetAddress> disconnectSet = new HashSet<>(communicators.keySet());
        disconnectSet.removeAll(configuredAddresses);
        for (InetAddress address : disconnectSet) {
            ClusterCommunicator communicator = communicators.remove(address);
            if (communicator != null) {
                communicator.shutdown();
            }
        }
        if (!localFound) {
            logger.error("Local host was not found in the cluster configuration, shutting down.");
            manager.shutdown();
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
            ClusterCommunicator communicator = new ClusterCommunicator(manager,
                    socket);
            communicators.put(address, communicator);
            communicator.start();
            communicator.sendState(state);
        }
    }

    public void dacConnectedLocal(DacTransmitKey key) {
        state.add(key);
        if (state.removeRequest(key)) {
            requestTimeout = Long.MAX_VALUE;
        }
        sendStateToAll();
    }

    public void dacDisconnectedLocal(DacTransmitKey key) {
        state.remove(key);
        sendStateToAll();
    }

    public void dacDisconnectedRemote(DacTransmitKey key) {
        if (state.containsRequest(key)) {
            requestTimeout = System.currentTimeMillis()
                    + REQUEST_TIMEOUT_INTERVAL;
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
        boolean pendingRequests = state.getRequestedKeys().isEmpty() == false;
        boolean requestFailed = System.currentTimeMillis() > requestTimeout;
        if (allDacsRunning && pendingRequests == false
                && requestFailed == false) {
            String overloadId = null;
            ClusterStateMessage overloaded = state;
            for (ClusterCommunicator communicator : communicators.values()) {
                ClusterStateMessage other = communicator.getClusterState();
                if (other == null || !other.getRequestedKeys().isEmpty()) {
                    return;
                }
                if (other.getKeys().size() > overloaded.getKeys().size()) {
                    overloaded = other;
                    overloadId = communicator.getClusterId();
                }
            }
            if (overloaded.getKeys().size() - 1 > state.getKeys().size()) {
                logger.info(
                        "To balance the load 1 dac transmit has been requested from {}",
                        overloadId);
                /*
                 * TODO its entirely possible for 2 cluster members to be here
                 * at the same time and both request the same key, to minimize
                 * conflict it would be better if each requested a different key
                 * using a better key selection mechanism.
                 */
                ClusterDacTransmitKey request = overloaded.getKeys().get(0);
                state.addRequest(request.toKey());
                sendStateToAll();
            }
        } else if (pendingRequests && requestFailed) {
            logger.error(
                    "Load balancing has been disabled due to failure to start a requested dac: {}.",
                    state.getRequestedKeys().get(0));
            state.getRequestedKeys().clear();
            sendStateToAll();
        }
    }
}