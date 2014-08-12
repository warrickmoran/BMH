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
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raytheon.uf.common.serialization.SerializationException;
import com.raytheon.uf.common.serialization.SerializationUtil;
import com.raytheon.uf.edex.bmh.comms.config.CommsConfig;
import com.raytheon.uf.edex.bmh.comms.config.DacChannelConfig;
import com.raytheon.uf.edex.bmh.comms.config.DacConfig;
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
 * Aug 04, 2014  3487     bsteffen    Rename config options.
 * Aug 12, 2014  3486     bsteffen    Support ChangeTransmitters
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
     * Map of transmitter keys to the communicator instances that are trying to
     * communicate with the dac. Under most normal circumstances there will be a
     * single communicator in the list. For cases where the comms manager has
     * been restarted or there are troubles communicating with the dac there may
     * be more than one.
     */
    private final Map<DacTransmitKey, List<DacTransmitCommunicator>> communicators;

    /**
     * Map of transmitter keys to the configuration for those channels
     */
    private Map<DacTransmitKey, DacChannelConfig> channels;

    private final int port;

    /**
     * Create a server for listening to dac transmit applications.
     * 
     * @param config
     *            the config to use for this server.
     */
    public DacTransmitServer(CommsManager manager, CommsConfig config) {
        super("DacTransmitServer");
        communicators = new ConcurrentHashMap<>(config.getDacs().size() * 4);
        port = config.getDacTransmitPort();
        this.manager = manager;
        readChannels(config);
    }

    private void readChannels(CommsConfig config) {
        Map<DacTransmitKey, DacChannelConfig> channels = new ConcurrentHashMap<>(
                config.getDacs().size() * 4);
        for (DacConfig dac : config.getDacs()) {
            for (DacChannelConfig channel : dac.getChannels()) {
                channels.put(new DacTransmitKey(dac, channel), channel);
            }
        }
        this.channels = channels;
    }


    public void reconfigure(CommsConfig config) {
        readChannels(config);
        for (Entry<DacTransmitKey, List<DacTransmitCommunicator>> entry : communicators
                .entrySet()) {
            DacChannelConfig channel = channels.get(entry.getKey());
            if (channel == null) {
                for (DacTransmitCommunicator communicator : entry.getValue()) {
                    communicator.shutdown();
                }
            } else {
                for (DacTransmitCommunicator communicator : entry.getValue()) {
                    communicator.setRadios(channel.getRadios());
                }
            }
        }
    }

    public boolean isConnected(DacTransmitKey key) {
        List<DacTransmitCommunicator> communicators = this.communicators
                .get(key);
        if (communicators == null || communicators.isEmpty()) {
            return false;
        }
        return true;
    }

    public void disconnected(DacTransmitCommunicator communicator) {
        Iterator<List<DacTransmitCommunicator>> cit = this.communicators
                .values().iterator();
        while (cit.hasNext()) {
            List<DacTransmitCommunicator> communicators = cit.next();
            if (communicators.remove(communicator)) {
                if (communicators.isEmpty()) {
                    cit.remove();
                }
                break;
            }
        }
    }

    public void connectedToDac(DacTransmitCommunicator communicator) {
        for (List<DacTransmitCommunicator> communicators : this.communicators
                .values()) {
            if (communicators.contains(communicator)) {
                for (DacTransmitCommunicator otherComm : communicators) {
                    if (communicator != otherComm) {
                        otherComm.shutdown();
                    }
                }
                break;
            }
        }
    }

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

    protected void handleConnection(Socket socket) {
        try {
            DacTransmitRegister message = SerializationUtil
                    .transformFromThrift(DacTransmitRegister.class,
                            socket.getInputStream());
            DacTransmitKey key = new DacTransmitKey(
                    message.getInputDirectory(), message.getDataPort(),
                    message.getDacAddress());
            DacChannelConfig channel = channels.get(key);
            boolean keep = true;
            String group = null;
            if (channel == null) {
                keep = false;
            } else {
                group = channel.getTransmitterGroup();
            }
            DacTransmitCommunicator comms = new DacTransmitCommunicator(
                    manager, this, group, message.getTransmitters(), socket);
            List<DacTransmitCommunicator> communicators = this.communicators
                    .get(key);
            if (communicators == null) {
                communicators = new ArrayList<>(1);
                this.communicators.put(key, communicators);
            } else {
                for (DacTransmitCommunicator communicator : communicators) {
                    if (communicator.isConnectedToDac()) {
                        keep = false;
                    }
                }
            }
            communicators.add(comms);
            comms.start();
            if (keep) {
                comms.setRadios(channel.getRadios());
            } else {
                comms.shutdown();
            }
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
