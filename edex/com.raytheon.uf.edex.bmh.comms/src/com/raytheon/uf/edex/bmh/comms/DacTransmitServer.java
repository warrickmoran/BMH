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
package com.raytheon.uf.edex.bmh.comms;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raytheon.uf.common.serialization.SerializationException;
import com.raytheon.uf.common.serialization.SerializationUtil;
import com.raytheon.uf.edex.bmh.comms.config.CommsConfig;
import com.raytheon.uf.edex.bmh.dactransmit.ipc.DacTransmitRegister;

/**
 * 
 * Server listening for connections from dac transmit applications.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Jul 16, 2014  3399     bsteffen    Initial creation
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class DacTransmitServer extends Thread {

    private static final Logger logger = LoggerFactory
            .getLogger(DacTransmitServer.class);

    private final CommsManager manager;

    /**
     * Map of transmitter group names to the communicator instances that are
     * trying to communicate with the dac. Under most normal circumstances there
     * will be a single communicator in the list. For cases where the comms
     * manager has been restarted or there are troubles communicating with the
     * dac there may be more than one.
     */
    private final Map<String, List<DacTransmitCommunicator>> communicators;

    private final int port;

    /**
     * Create a server for listening to dac transmit applications.
     * 
     * @param config
     *            the config to use for this server.
     */
    public DacTransmitServer(CommsManager manager, CommsConfig config) {
        super("DacTransmitServer");
        communicators = new ConcurrentHashMap<String, List<DacTransmitCommunicator>>(
                config.getDacs().size() * 4);
        port = config.getIpcPort();
        this.manager = manager;

    }

    /**
     * 
     */
    @Override
    public void run() {
        ServerSocket server = null;
        try {
            server = new ServerSocket(port);
        } catch (IOException e) {
            logger.error("Failed to start dac transmit server", e);
            return;
        }

        while (!server.isClosed()) {
            try {
                Socket socket = server.accept();
                handleConnection(socket);
            } catch (Throwable e) {
                logger.error("Unexpected error accepting a connection", e);
            }
        }
        try {
            server.close();
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * @return All the group names for dac transmit instances connected to the
     *         server.
     */
    public Set<String> getConnectedGroups() {
        return communicators.keySet();
    }

    /**
     * Get the communicator for the given group
     * 
     * @param group
     *            name of a transmitter group
     * @return a communicator or null if nothing is connected.
     */
    public DacTransmitCommunicator getCommunicator(String group) {
        List<DacTransmitCommunicator> communicators = this.communicators
                .get(group);
        if (communicators == null) {
            return null;
        }
        /*
         * Try to find a dac transmit instance that is actually connected to the
         * dac, remove any that have died.
         */
        DacTransmitCommunicator connected = null;
        Iterator<DacTransmitCommunicator> it = communicators.iterator();
        while (it.hasNext()) {
            DacTransmitCommunicator comms = it.next();
            if (!comms.isAlive()) {
                it.remove();
            } else if (comms.isConnectedToDac()) {
                connected = comms;
                break;
            }
        }
        if (connected == null) {
            /*
             * No one is connected to the dac so if any are left return an
             * arbitrary instance, since all instances are actively trying to
             * connect and there is no way to guess which one will get it and
             * who deserves to die.
             */
            if (communicators.isEmpty()) {
                this.communicators.remove(group);
                return null;
            } else {
                return communicators.get(0);
            }
        } else if (communicators.size() > 1) {
            /*
             * There is a single communicator connected to the DAC, close any
             * other connections and rturn the connected one.
             */
            it = communicators.iterator();
            while (it.hasNext()) {
                DacTransmitCommunicator comms = it.next();
                if (comms != connected) {
                    it.remove();
                    comms.shutdown();
                }
            }

        }
        return connected;
    }

    /**
     * Shutdown any communicators for the given group.
     * 
     * @param group
     *            the name of a transmitter group.
     */
    public void shutdownGroup(String group) {
        List<DacTransmitCommunicator> communicators = this.communicators
                .remove(group);
        if (communicators == null) {
            return;
        }
        for (DacTransmitCommunicator comms : communicators) {
            comms.shutdown();
        }
    }

    protected void handleConnection(Socket socket) {
        try {
            DacTransmitRegister message = SerializationUtil
                    .transformFromThrift(DacTransmitRegister.class,
                            socket.getInputStream());
            String group = message.getTransmitterGroup();
            DacTransmitCommunicator comms = new DacTransmitCommunicator(
                    manager, group, socket);
            List<DacTransmitCommunicator> communicators = this.communicators
                    .get(group);
            if (communicators == null) {
                communicators = new ArrayList<>(1);
                this.communicators.put(group, communicators);
            }
            communicators.add(comms);
            comms.start();
        } catch (IOException | SerializationException e) {
            try {
                socket.close();
            } catch (IOException ignorable) {
                logger.error("Error closing message to dac transmit");
            }
            logger.error("Error accepting client", e);
        }
    }

}
