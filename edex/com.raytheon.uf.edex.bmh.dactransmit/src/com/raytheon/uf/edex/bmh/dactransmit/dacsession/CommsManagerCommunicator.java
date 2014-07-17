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
package com.raytheon.uf.edex.bmh.dactransmit.dacsession;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.raytheon.uf.common.serialization.SerializationException;
import com.raytheon.uf.common.serialization.SerializationUtil;
import com.raytheon.uf.edex.bmh.dactransmit.events.ShutdownRequestedEvent;
import com.raytheon.uf.edex.bmh.dactransmit.ipc.DacTransmitRegister;
import com.raytheon.uf.edex.bmh.dactransmit.ipc.DacTransmitShutdown;
import com.raytheon.uf.edex.bmh.dactransmit.ipc.DacTransmitStatus;

/**
 * 
 * Implements a thread for reading messages from the comms manager and also
 * provides methods for sending messages to the comms manager.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Jul 16, 2014  3399     bsteffen    Initial creation
 * Jul 22, 2014  3286     dgilling    Connect to event bus.
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public final class CommsManagerCommunicator extends Thread {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final String transmitterGroup;

    private final InetAddress commsHost = InetAddress.getLoopbackAddress();

    private final int port;

    private final Object sendLock = new Object();

    private Socket socket;

    private DacTransmitStatus statusToSend = new DacTransmitStatus(false);

    private final EventBus eventBus;

    public CommsManagerCommunicator(int port, String transmitterGroup,
            EventBus eventBus) {
        super("CommsManagerReaderThread");
        this.port = port;
        this.transmitterGroup = transmitterGroup;
        this.eventBus = eventBus;
    }

    @Override
    public void run() {
        InputStream inputStream = null;
        while (true) {
            if (socket == null) {
                OutputStream outputStream = null;
                synchronized (sendLock) {
                    try {
                        // TODO is it ALWAYS localhost.
                        socket = new Socket(commsHost, port);
                        inputStream = socket.getInputStream();
                        outputStream = socket.getOutputStream();
                    } catch (IOException e) {
                        logger.error("Unable to connect to comms manager", e);
                        disconnect();
                    }
                    if (socket != null) {
                        try {
                            SerializationUtil.transformToThriftUsingStream(
                                    new DacTransmitRegister(transmitterGroup),
                                    outputStream);
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
            }
            if (socket == null) {
                // TODO make sleep configurable
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
                } catch (SerializationException e) {
                    logger.error("Error reading message from comms manager", e);
                    disconnect();
                }
            }
        }
    }

    private void disconnect() {
        synchronized (sendLock) {
            try {
                if (socket != null) {
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
                }
            } catch (IOException ignorable) {
                logger.error("Error closing message to comms manager");
            }
        }
    }

    private void handleMessage(Object message) {
        if (message instanceof DacTransmitShutdown) {
            // TODO graceful shutdown.
            // Maybe make this a direct call or something to the DacSession
            // shutdown method?
            /*
             * The problem with doing this here is you won't have proper control
             * of shutdown. Shutdown should be an ordered sequence. Here you are
             * disconnecting form CommsManager, even though on a shutdown you
             * are going to wait for the message to finish playing. Shutdown
             * doesn't fit well with the event bus paradigm--shutdown is
             * specifically dependent on killing the event bus's thread. What
             * happens when killing the event bus thread happens on that same
             * thread??
             */
            eventBus.post(new ShutdownRequestedEvent());
            disconnect();
        } else {
            logger.error("Unrecognized message from comms manager of type "
                    + message.getClass().getSimpleName());
        }
    }

    public void sendConnectionStatus(boolean connected) {
        statusToSend = new DacTransmitStatus(connected);
        if (socket != null) {
            synchronized (sendLock) {
                try {
                    SerializationUtil.transformToThriftUsingStream(
                            statusToSend, socket.getOutputStream());
                } catch (SerializationException | IOException e) {
                    logger.error("Error communicating with comms manager", e);
                }
            }
        }
    }

}
