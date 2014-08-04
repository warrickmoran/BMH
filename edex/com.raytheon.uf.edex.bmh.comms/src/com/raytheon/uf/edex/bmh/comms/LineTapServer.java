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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raytheon.uf.common.bmh.comms.LineTapRequest;
import com.raytheon.uf.common.bmh.dac.DacReceiveThread;
import com.raytheon.uf.common.serialization.SerializationException;
import com.raytheon.uf.common.serialization.SerializationUtil;
import com.raytheon.uf.edex.bmh.comms.config.CommsConfig;
import com.raytheon.uf.edex.bmh.comms.config.DacChannelConfig;
import com.raytheon.uf.edex.bmh.comms.config.DacConfig;

/**
 * 
 * Listens for connections to restream audio from a DAC.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Aug 04, 2014  2487     bsteffen    Initial creation
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class LineTapServer extends Thread {

    private static final Logger logger = LoggerFactory
            .getLogger(LineTapServer.class);

    private final int port;

    private final List<DacConfig> dacs;

    private Map<Integer, DacReceiveThread> receivers = new HashMap<>();

    public LineTapServer(CommsConfig config) {
        super("DacLineTapServer");
        this.port = config.getLineTapPort();
        this.dacs = config.getDacs();
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
            socket.setTcpNoDelay(true);
            LineTapRequest message = SerializationUtil.transformFromThrift(
                    LineTapRequest.class, socket.getInputStream());
            String group = message.getTransmitterGroup();
            for (DacConfig dac : dacs) {
                for (DacChannelConfig channel : dac.getChannels()) {
                    if (channel.getTransmitterGroup().equals(group)) {
                        synchronized (receivers) {
                            DacReceiveThread receiver = receivers.get(dac
                                    .getReceivePort());
                            if (receiver == null || !receiver.isAlive()) {
                                receiver = new DacReceiveThread(
                                        dac.getReceivePort());
                                receivers.put(dac.getReceivePort(), receiver);
                                receiver.start();
                            }
                            LineTapCommunicator comms = new LineTapCommunicator(
                                    this, group, channel.getRadios()[0], socket);
                            receiver.subscribe(comms);
                            comms.start();
                        }
                        return;
                    }
                }
            }
            logger.error("Unable to tap line of " + group
                    + " because it is not configured.");
            socket.close();
        } catch (IOException | SerializationException e) {
            try {
                socket.close();
            } catch (IOException ignorable) {
                logger.error("Error closing message to dac transmit");
            }
            logger.error("Error accepting client", e);
        }
    }

    public void unsubscribe(LineTapCommunicator comms) {
        for (DacConfig dac : dacs) {
            for (DacChannelConfig channel : dac.getChannels()) {
                if (channel.getTransmitterGroup().equals(comms.getGroupName())) {
                    synchronized (receivers) {
                        DacReceiveThread receiver = receivers.get(dac
                                .getReceivePort());
                        if (receiver != null) {
                            receiver.unsubscribe(comms);
                            if (!receiver.hasSubscribers()) {
                                receiver.halt();
                                receivers.remove(dac.getReceivePort());
                            }
                        }
                    }
                    return;
                }
            }
        }
    }
}
