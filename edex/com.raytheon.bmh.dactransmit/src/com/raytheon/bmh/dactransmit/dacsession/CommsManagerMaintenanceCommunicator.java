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
package com.raytheon.bmh.dactransmit.dacsession;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.primitives.Ints;
import com.raytheon.bmh.dactransmit.ipc.DacMaintenanceRegister;
import com.raytheon.bmh.dactransmit.ipc.DacTransmitShutdown;
import com.raytheon.bmh.dactransmit.ipc.DacTransmitStatus;
import com.raytheon.uf.common.bmh.comms.SendPlaylistMessage;
import com.raytheon.uf.common.bmh.notify.MaintenanceMessagePlayback;
import com.raytheon.uf.common.serialization.SerializationException;
import com.raytheon.uf.common.serialization.SerializationUtil;

/**
 * Allows a Dac Transmit running in maintenance mode to communicate with the
 * Comms Manager.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 28, 2015 4394       bkowal      Initial creation
 * Feb 04, 2016 5308       rjpeter     Handle SendPlaylistMessage.
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class CommsManagerMaintenanceCommunicator extends Thread {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final InetAddress commsHost = InetAddress.getLoopbackAddress();

    private final DacMaintenanceConfig config;

    private final String transmitterGroup;

    private final Object sendLock = new Object();

    private Socket socket;

    private DacTransmitStatus statusToSend = new DacTransmitStatus(false);

    private transient boolean running = true;

    private volatile MaintenanceMessagePlayback lastPlayback;

    public CommsManagerMaintenanceCommunicator(
            DacMaintenanceSession dacMaintenanceSession, String transmitterGroup) {
        super("CommsManagerMaintenanceReaderThread");
        this.config = dacMaintenanceSession.getConfig();
        this.transmitterGroup = transmitterGroup;
    }

    @Override
    public void run() {
        InputStream inputStream = null;
        while (running) {
            if (socket == null) {
                OutputStream outputStream = null;
                synchronized (sendLock) {
                    try {
                        socket = new Socket(commsHost, config.getManagerPort());
                        if (socket.getLocalPort() == config.getManagerPort()) {
                            /*
                             * If the manager port is within the ephemeral range
                             * then on some OSs it is possible that the socket
                             * is connected to itself using TCP simultaneous
                             * open. This is only possible if the comms manager
                             * is not running and it causes the comms manager to
                             * fail to start because the port is in use so it is
                             * important to abort the connection ASAP.
                             */
                            logger.error(
                                    "Port reuse has been detected on port {}, connection to comms manager will be aborted.",
                                    config.getManagerPort());
                            disconnect();
                        } else {
                            socket.setTcpNoDelay(true);
                            inputStream = socket.getInputStream();
                            outputStream = socket.getOutputStream();
                        }
                    } catch (IOException e) {
                        logger.error(
                                "Unable to communicate with comms manager", e);
                        disconnect();
                    }
                    if (socket != null) {
                        try {
                            DacMaintenanceRegister registration = new DacMaintenanceRegister(
                                    config.getDataPort(),
                                    config.getDacHostname(),
                                    Ints.toArray(config.getTransmitters()),
                                    config.getMessageFilePath().toString(),
                                    this.transmitterGroup);
                            SerializationUtil.transformToThriftUsingStream(
                                    registration, outputStream);
                            if (statusToSend.isConnectedToDac()) {
                                SerializationUtil.transformToThriftUsingStream(
                                        statusToSend, outputStream);
                            }
                        } catch (SerializationException e) {
                            logger.error(
                                    "Unable to communicate with comms manager",
                                    e);
                            disconnect();
                        }
                    }
                }
                if (socket == null) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e1) {
                        /* just try again sooner */
                    }
                    continue;
                } else {
                    try {
                        handleMessage(SerializationUtil.transformFromThrift(
                                Object.class, inputStream));
                    } catch (Throwable e) {
                        logger.error(
                                "Error reading message from comms manager", e);
                        disconnect();
                    }
                }
            }
        }
    }

    public void shutdown() {
        this.running = false;
    }

    public void sendConnectionStatus(boolean connected) {
        statusToSend = new DacTransmitStatus(connected);
        sendMessageToCommsManager(statusToSend);
    }

    public void forwardPlaybackNotification(MaintenanceMessagePlayback playback) {
        lastPlayback = playback;

        this.sendMessageToCommsManager(playback);
    }

    private void disconnect() {
        synchronized (sendLock) {
            if ((socket != null) && (socket.isClosed() == false)) {
                try {
                    try {
                        SerializationUtil.transformToThriftUsingStream(
                                new DacTransmitShutdown(),
                                socket.getOutputStream());
                    } catch (SerializationException e) {
                        logger.error(
                                "Unable to send shutdown message to comms manager",
                                e);
                    }
                    socket.close();
                    socket = null;
                } catch (IOException ignorable) {
                    logger.error("Error closing message to comms manager");
                }
            }
        }
    }

    private void sendMessageToCommsManager(final Object message) {
        synchronized (sendLock) {
            if ((socket != null) && (socket.isClosed() == false)) {
                try {
                    SerializationUtil.transformToThriftUsingStream(message,
                            socket.getOutputStream());
                } catch (SerializationException | IOException e) {
                    logger.error("Error communicating with comms manager", e);
                }
            } else {
                logger.warn("Received notification that could not be sent to CommsManager: "
                        + message.toString());
            }
        }
    }

    private void handleMessage(Object message) {
        if (message instanceof SendPlaylistMessage) {
            if (lastPlayback != null) {
                sendMessageToCommsManager(lastPlayback);
            }
        }
    }
}