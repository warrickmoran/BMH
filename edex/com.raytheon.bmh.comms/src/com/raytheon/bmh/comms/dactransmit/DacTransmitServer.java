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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raytheon.bmh.comms.AbstractServer;
import com.raytheon.bmh.comms.CommsManager;
import com.raytheon.bmh.dactransmit.ipc.DacMaintenanceRegister;
import com.raytheon.bmh.dactransmit.ipc.DacTransmitRegister;
import com.raytheon.uf.common.bmh.broadcast.ILiveBroadcastMessage;
import com.raytheon.uf.common.bmh.comms.SendPlaylistMessage;
import com.raytheon.uf.common.bmh.datamodel.playlist.PlaylistUpdateNotification;
import com.raytheon.uf.edex.bmh.comms.CommsConfig;
import com.raytheon.uf.edex.bmh.comms.DacChannelConfig;
import com.raytheon.uf.edex.bmh.comms.DacConfig;

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
 * Apr 21, 2015  4407     bkowal      {@link #isConnectedToDacTransmit(DacTransmitKey)} will now only
 *                                    verify a connection to dac transmit.
 * Apr 24, 2015  4423     rferrel     Added {@link #changeTimeZone}.
 * Apr 29, 2015  4394     bkowal      Handle {@link DacMaintenanceRegister}.
 * Jul 08, 2015  4636     bkowal      Support same and alert decibel levels.
 * Aug 04, 2015  4424     bkowal      Added {@link #getActiveDacTransmits()}.
 * Aug 11, 2015  4372     bkowal      Added {@link #lookupDacTransmitKeyByGroup(String)}.
 * Aug 12, 2015  4424     bkowal      Eliminate Dac Transmit Key.
 * Oct 28, 2015  5029     rjpeter     Allow multiple dac transmits to be requested.
 * Nov 04, 2015  5068     rjpeter     Switch audio units from dB to amplitude.
 * Nov 11, 2015  5114     rjpeter     Updated CommsManager to use a single port.
 * Dec 15, 2015  5114     rjpeter     Updated SocketListener to use a ThreadPool.
 * Jan 07, 2016  4997     bkowal      dactransmit is no longer a uf edex plugin.
 * Feb 04, 2016  5308     rjpeter     Handle SendPlaylistMessage.
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class DacTransmitServer extends AbstractServer {

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
    private final Map<String, List<DacTransmitCommunicator>> communicators;

    /**
     * Map of transmitter keys to the configuration for those channels
     */
    private volatile Map<String, DacChannelConfig> channels;

    /**
     * Create a server for listening to dac transmit applications.
     * 
     * @param config
     *            the config to use for this server.
     * @throws IOException
     */
    public DacTransmitServer(CommsManager manager, CommsConfig config)
            throws IOException {
        super(manager.getSocketListener());
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
            this.channels = new HashMap<String, DacChannelConfig>(0);
        } else {
            Map<String, DacChannelConfig> channels = new ConcurrentHashMap<>(
                    dacs.size() * 4);
            for (DacConfig dac : config.getDacs()) {
                for (DacChannelConfig channel : dac.getChannels()) {
                    channels.put(channel.getTransmitterGroup(), channel);
                }
            }
            this.channels = channels;
        }
    }

    /**
     * TODO: Update JavaDoc.
     * 
     * Returns a {@link Set} of {@link DacTransmitKey}s consisting of the
     * {@link DacTransmitKey}s that are associated with active dac transmit
     * processes that have been specified in the comms configuration.
     * 
     * @return a {@link Set} of {@link DacTransmitKey}s
     */
    public Set<String> getActiveDacTransmits() {
        return new HashSet<>(this.channels.keySet());
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
        for (Entry<String, List<DacTransmitCommunicator>> entry : communicators
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
                    communicator.setTransmitterAudioAmplitudes(
                            channel.getAudioAmplitude(),
                            channel.getSameAmplitude(),
                            channel.getAlertAmplitude());
                }
            }
        }
    }

    /**
     * Adjust time zone for communicators for the transmitter group.
     * 
     * @param timeZone
     * @param transmitterGroup
     */
    public void changeTimeZone(String timeZone, String transmitterGroup) {
        DacChannelConfig toUpdate = this.channels.get(transmitterGroup);
        if ((toUpdate == null) || timeZone.equals(toUpdate.getTimezone())) {
            return;
        }

        toUpdate.setTimezone(timeZone);
        for (DacTransmitCommunicator communicator : this.communicators
                .get(transmitterGroup)) {
            communicator.setTimeZone(timeZone);
        }
    }

    /**
     * Check if the server is connected to a dac transmit process for the
     * provided key. This method does not check whether that process is
     * successfully communicating with a dac or not.
     */
    public boolean isConnectedToDacTransmit(final String transmitterGroup) {
        List<DacTransmitCommunicator> communicators = this.communicators
                .get(transmitterGroup);
        if (communicators != null) {
            for (DacTransmitCommunicator communicator : communicators) {
                if (communicator.isDisconnectedFromDacTransmit() == false) {
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
    public boolean isConnectedToDac(final String transmitterGroup) {
        List<DacTransmitCommunicator> communicators = this.communicators
                .get(transmitterGroup);
        if ((communicators != null) && !communicators.isEmpty()) {
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
    public void playlistNotificationArrived(final String transmitterGroup,
            PlaylistUpdateNotification notification) {
        List<DacTransmitCommunicator> communicators = this.communicators
                .get(transmitterGroup);
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
    public void sendToDac(final String transmitterGroup,
            ILiveBroadcastMessage msg) {
        List<DacTransmitCommunicator> communicators = this.communicators
                .get(transmitterGroup);
        if (communicators != null) {
            for (DacTransmitCommunicator communicator : communicators) {
                communicator.sendLiveBroadcastMsg(msg);
            }
        }
    }

    /**
     * Send the playlist request to the associated dac transmit.
     * 
     * @param key
     *            the specified {@link DacTransmitKey}
     * @param data
     *            the specified data
     */
    public void sendToDac(SendPlaylistMessage msg) {
        List<DacTransmitCommunicator> communicators = this.communicators
                .get(msg.getTransmitterGroup());
        if (communicators != null) {
            for (DacTransmitCommunicator communicator : communicators) {
                communicator.sendPlaylistRequest(msg);
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
    public void dacConnected(final String transmitterGroup) {
        List<DacTransmitCommunicator> communicators = this.communicators
                .get(transmitterGroup);
        if (communicators != null) {
            Iterator<DacTransmitCommunicator> it = communicators.iterator();
            while (it.hasNext()) {
                DacTransmitCommunicator communicator = it.next();
                if (!communicator.isConnectedToDac()) {
                    logger.info(
                            "Shutting down dac transmit for {} because another dac transmit has connected.",
                            transmitterGroup);
                    communicator.shutdown(true);
                }
            }
        }

    }

    public void dacRequested(final List<String> transmitterGroups) {
        for (String transmitterGroup : transmitterGroups) {
            List<DacTransmitCommunicator> communicators = this.communicators
                    .get(transmitterGroup);
            if (communicators != null) {
                Iterator<DacTransmitCommunicator> it = communicators.iterator();
                while (it.hasNext()) {
                    DacTransmitCommunicator communicator = it.next();
                    communicator.shutdown(false);
                }
            }
        }
    }

    /**
     * This method should be called from the comms manager when the connection
     * with a dac transmit has been lost.
     */
    public void dacTransmitDisconnected(final String transmitterGroup) {
        List<DacTransmitCommunicator> communicators = this.communicators
                .get(transmitterGroup);
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

    public void updatePlaylistListener(final String transmitterGroup,
            boolean active) {
        List<DacTransmitCommunicator> communicators = this.communicators
                .get(transmitterGroup);
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
    protected void shutdownInternal() {
        for (List<DacTransmitCommunicator> communicators : this.communicators
                .values()) {
            for (DacTransmitCommunicator communicator : communicators) {
                communicator.shutdown(true);
            }
        }
    }

    @Override
    public boolean handleConnection(Socket socket, Object registrationMessage) {
        if (registrationMessage instanceof DacTransmitRegister) {
            this.handleDacTransmitConnection(
                    (DacTransmitRegister) registrationMessage, socket);
            return false;
        }

        if (registrationMessage instanceof DacMaintenanceRegister) {
            this.handleDacMaintenanceConnection(
                    (DacMaintenanceRegister) registrationMessage, socket);
            return false;
        }

        logger.warn("Received unexpected message with type: "
                + registrationMessage.getClass().getName()
                + " from an unknown entity. Disconnecting ...");
        return true;
    }

    private void handleDacTransmitConnection(DacTransmitRegister message,
            Socket socket) {
        DacChannelConfig channel = channels.get(message.getTransmitterGroup());
        boolean keep = true;
        final String group = message.getTransmitterGroup();
        if (channel == null) {
            logger.info(
                    "Shutting down newly connected dac transmit for {} because it was not found in the configuration.",
                    group);
            keep = false;
        }
        DacTransmitCommunicator comms = new DacTransmitCommunicator(manager,
                group, message.getTransmitters(), socket,
                message.getAudioAmplitude(), message.getSameAmplitude(),
                message.getAlertAmplitude());
        List<DacTransmitCommunicator> communicators = this.communicators
                .get(group);
        if (communicators == null) {
            communicators = new CopyOnWriteArrayList<>();
            this.communicators.put(group, communicators);
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
        if (keep && manager.isConnectedRemote(message.getTransmitterGroup())) {
            logger.info(
                    "Shutting down newly connected dac transmit for {} because another cluster member is already connected.",
                    group);
            keep = false;
        }
        communicators.add(comms);
        comms.start();
        if (keep) {
            comms.setRadios(channel.getRadios());
            comms.setTransmitterAudioAmplitudes(channel.getAudioAmplitude(),
                    channel.getSameAmplitude(), channel.getAlertAmplitude());
        } else {
            comms.shutdown(true);
        }
    }

    private void handleDacMaintenanceConnection(DacMaintenanceRegister message,
            Socket socket) {
        DacMaintenanceCommunicator comms = new DacMaintenanceCommunicator(
                this.manager, message.getTransmitterGroup(), socket);
        comms.start();
    }

    @Override
    protected Set<Class<?>> getTypesHandled() {
        Set<Class<?>> rval = new HashSet<>(2, 1);
        rval.add(DacTransmitRegister.class);
        rval.add(DacMaintenanceRegister.class);
        return Collections.unmodifiableSet(rval);
    }

}