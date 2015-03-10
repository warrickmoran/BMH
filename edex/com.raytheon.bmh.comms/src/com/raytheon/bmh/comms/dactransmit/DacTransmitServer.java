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
package com.raytheon.bmh.comms.dactransmit;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raytheon.bmh.comms.AbstractServerThread;
import com.raytheon.bmh.comms.CommsManager;
import com.raytheon.bmh.comms.DacTransmitKey;
import com.raytheon.uf.common.bmh.broadcast.ILiveBroadcastMessage;
import com.raytheon.uf.common.bmh.datamodel.playlist.PlaylistUpdateNotification;
import com.raytheon.uf.common.serialization.SerializationException;
import com.raytheon.uf.common.serialization.SerializationUtil;
import com.raytheon.uf.edex.bmh.comms.CommsConfig;
import com.raytheon.uf.edex.bmh.comms.DacChannelConfig;
import com.raytheon.uf.edex.bmh.comms.DacConfig;
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
 * Aug 18, 2014  3532     bkowal      Support ChangeDecibelRange
 * Sep 5,  2014  3532     bkowal      Replace ChangeDecibelRange with ChangeDecibelTarget.
 * Sep 23, 2014  3485     bsteffen    Bug fixes and changes to notification processing to support clustering
 * Oct 15, 2014  3655     bkowal      Support live broadcasting to the DAC.
 * Oct 21, 2014  3655     bkowal      Use the new message types.
 * Nov 03, 2014  3762     bsteffen    Add load balancing of dac transmits.
 * Nov 19, 2014  3817     bsteffen    Updates to send system status messages.
 * Jan 22, 2015  3912     bsteffen    Shutdown dac transmit when it connects after a remote dac transmit
 *                                    has already established a connection with the dac.
 * Jan 23, 2015  3912     bsteffen    More logging and detection of disconnected dac transmits.
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class DacTransmitServer extends AbstractServerThread {

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
    private volatile Map<DacTransmitKey, DacChannelConfig> channels;

    /**
     * Create a server for listening to dac transmit applications.
     * 
     * @param config
     *            the config to use for this server.
     * @throws IOException
     */
    public DacTransmitServer(CommsManager manager, CommsConfig config)
            throws IOException {
        super(config.getDacTransmitPort());
        int mapSize = 0;
        if (config.getDacs() != null) {
            mapSize = config.getDacs().size() * 4;
        }
        communicators = new ConcurrentHashMap<>(mapSize);
        this.manager = manager;
        readChannels(config);
    }

    private void readChannels(CommsConfig config) {
        Set<DacConfig> dacs = config.getDacs();
        if (dacs == null) {
            this.channels = new HashMap<DacTransmitKey, DacChannelConfig>(0);
        } else {
            Map<DacTransmitKey, DacChannelConfig> channels = new ConcurrentHashMap<>(
                    dacs.size() * 4);
            for (DacConfig dac : config.getDacs()) {
                for (DacChannelConfig channel : dac.getChannels()) {
                    channels.put(new DacTransmitKey(dac, channel), channel);
                }
            }
            this.channels = channels;
        }
    }

    /**
     * Reload the configuration file. Shutdown any dac transmit processes that
     * are no longer in the configuration and ensure that all remaining
     * processes are configured correctly.
     * 
     * @param config
     */
    public void reconfigure(CommsConfig config) {
        readChannels(config);
        for (Entry<DacTransmitKey, List<DacTransmitCommunicator>> entry : communicators
                .entrySet()) {
            DacChannelConfig channel = channels.get(entry.getKey());
            if (channel == null) {
                for (DacTransmitCommunicator communicator : entry.getValue()) {
                    logger.info(
                            "Reconfigure has triggered dac transmit shutdown for {}.",
                            entry.getKey());
                    communicator.shutdown(true);
                }
            } else {
                for (DacTransmitCommunicator communicator : entry.getValue()) {
                    communicator.setRadios(channel.getRadios());
                    communicator.setTransmitterDBTarget(channel.getDbTarget());
                }
            }
        }
    }

    /**
     * Check if the server is connected to a dac transmit process for the
     * provided key. This method does not check whether that process is
     * successfully communicating with a dac or not.
     */
    public boolean isConnectedToDacTransmit(DacTransmitKey key) {
        List<DacTransmitCommunicator> communicators = this.communicators
                .get(key);
        if (communicators != null) {
            for (DacTransmitCommunicator communicator : communicators) {
                if (communicator.isConnectedToDacTransmit()) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Check if the server is connected to a dac transmit process for the
     * provided key and if that process is currently communicating with a dac.
     */
    public boolean isConnectedToDac(DacTransmitKey key) {
        List<DacTransmitCommunicator> communicators = this.communicators
                .get(key);
        if (communicators != null && !communicators.isEmpty()) {
            for (DacTransmitCommunicator communicator : communicators) {
                if (communicator.isConnectedToDac()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Send a playlist update to any dac transmit processes which are connected
     * to this server.
     */
    public void playlistNotificationArrived(DacTransmitKey key,
            PlaylistUpdateNotification notification) {
        List<DacTransmitCommunicator> communicators = this.communicators
                .get(key);
        if (communicators != null) {
            for (DacTransmitCommunicator communicator : communicators) {
                communicator.sendPlaylistUpdate(notification);
            }
        }
    }

    /**
     * Send the specified data to the dac associated with the specified
     * {@link DacTransmitKey}.
     * 
     * @param key
     *            the specified {@link DacTransmitKey}
     * @param data
     *            the specified data
     */
    public void sendToDac(DacTransmitKey key, ILiveBroadcastMessage msg) {
        List<DacTransmitCommunicator> communicators = this.communicators
                .get(key);
        if (communicators != null) {
            for (DacTransmitCommunicator communicator : communicators) {
                communicator.sendLiveBroadcastMsg(msg);
            }
        }
    }

    /**
     * This method should be called from the comms manager whenever a successful
     * connection is made to a dac. This will clean up any dac transmit process
     * that are trying to access the same dac unsuccessfully(Normally there
     * shouldn't be much to clean up unless there was a startup or clustering
     * race and this server lost).
     */
    public void dacConnected(DacTransmitKey key) {
        List<DacTransmitCommunicator> communicators = this.communicators
                .get(key);
        if (communicators != null) {
            Iterator<DacTransmitCommunicator> it = communicators.iterator();
            while (it.hasNext()) {
                DacTransmitCommunicator communicator = it.next();
                if (!communicator.isConnectedToDac()) {
                    logger.info(
                            "Shutting down dac transmit for {} because another dac transmit has connected.",
                            key);
                    communicator.shutdown(true);
                }
            }
        }

    }

    public void dacRequested(DacTransmitKey key) {
        List<DacTransmitCommunicator> communicators = this.communicators
                .get(key);
        if (communicators != null) {
            Iterator<DacTransmitCommunicator> it = communicators.iterator();
            while (it.hasNext()) {
                DacTransmitCommunicator communicator = it.next();
                communicator.shutdown(false);
            }
        }

    }

    /**
     * This method should be called from the comms manager when the connection
     * with a dac transmit has been lost.
     */
    public void dacTransmitDisconnected(DacTransmitKey key) {
        List<DacTransmitCommunicator> communicators = this.communicators
                .get(key);
        if (communicators != null) {
            List<DacTransmitCommunicator> bad = new ArrayList<>();
            for (DacTransmitCommunicator communicator : communicators) {
                if (communicator.isDisconnectedFromDacTransmit()) {
                    bad.add(communicator);
                }
            }
            communicators.removeAll(bad);

        }
    }

    public void updatePlaylistListener(DacTransmitKey key, boolean active) {
        List<DacTransmitCommunicator> communicators = this.communicators
                .get(key);
        if (communicators != null) {
            for (DacTransmitCommunicator communicator : communicators) {
                communicator.updatePlaylistListener(active);
            }
        }
    }

    /**
     * Shutdown all dac transmit communications as well as the server. This
     * should only be called for cluster failover.
     */
    @Override
    public void shutdown() {
        super.shutdown();
        for (List<DacTransmitCommunicator> communicators : this.communicators
                .values()) {
            for (DacTransmitCommunicator communicator : communicators) {
                communicator.shutdown(true);
            }
        }
    }

    @Override
    protected void handleConnection(Socket socket)
            throws SerializationException, IOException {
        DacTransmitRegister message = SerializationUtil.transformFromThrift(
                DacTransmitRegister.class, socket.getInputStream());
        DacTransmitKey key = new DacTransmitKey(message.getInputDirectory(),
                message.getDataPort(), message.getDacAddress());
        DacChannelConfig channel = channels.get(key);
        boolean keep = true;
        String group = null;
        if (channel == null) {
            logger.info(
                    "Shutting down newly connected dac transmit for {} because it was not found in the configuration.",
                    key);
            keep = false;
        } else {
            group = channel.getTransmitterGroup();
        }
        DacTransmitCommunicator comms = new DacTransmitCommunicator(manager,
                key, group, message.getTransmitters(), socket,
                message.getDbTarget());
        List<DacTransmitCommunicator> communicators = this.communicators
                .get(key);
        if (communicators == null) {
            communicators = new CopyOnWriteArrayList<>();
            this.communicators.put(key, communicators);
        } else {
            for (DacTransmitCommunicator communicator : communicators) {
                if (communicator.isConnectedToDac()) {
                    logger.info(
                            "Shutting down newly connected dac transmit for {} because another dac transmit is already connected.",
                            group);
                    keep = false;
                }
            }
        }
        if (keep && manager.isConnectedRemote(key)) {
            logger.info(
                    "Shutting down newly connected dac transmit for {} because another cluster member is already connected.",
                    group);
            keep = false;
        }
        communicators.add(comms);
        comms.start();
        if (keep) {
            comms.setRadios(channel.getRadios());
            comms.setTransmitterDBTarget(channel.getDbTarget());
        } else {
            comms.shutdown(true);
        }

    }

}
