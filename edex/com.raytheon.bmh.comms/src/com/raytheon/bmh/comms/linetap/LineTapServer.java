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
package com.raytheon.bmh.comms.linetap;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raytheon.bmh.comms.AbstractServer;
import com.raytheon.bmh.comms.CommsManager;
import com.raytheon.uf.common.bmh.comms.LineTapRequest;
import com.raytheon.uf.common.bmh.dac.DacReceiveThread;
import com.raytheon.uf.edex.bmh.comms.CommsConfig;
import com.raytheon.uf.edex.bmh.comms.CommsHostConfig;

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
 * Sep 23, 2014  3485     bsteffen    Enable multicast
 * Apr 14, 2015  4394     bkowal      Eliminated the need to iteratively search for
 *                                    dac configuration information.
 * Nov 11, 2015  5114     rjpeter     Updated CommsManager to use a single port.
 * Dec 15, 2015  5114     rjpeter     Updated SocketListener to use a ThreadPool.
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class LineTapServer extends AbstractServer {

    private static final Logger logger = LoggerFactory
            .getLogger(LineTapServer.class);

    private final Map<ReceiveKey, DacReceiveThread> receivers = new HashMap<>();

    private volatile NetworkInterface multicastInterface;

    public LineTapServer(CommsManager manager, CommsConfig config)
            throws IOException {
        super(manager.getSocketListener());
        reconfigure(config);
    }

    public void reconfigure(CommsConfig config) {
        try {
            CommsHostConfig localHost = config.getLocalClusterHost();
            if (localHost == null) {
                multicastInterface = null;
            } else {
                multicastInterface = localHost.getDacNetworkInterface();
            }
        } catch (IOException e) {
            logger.error(
                    "Unable to determine interface for multicast line tap, default interface will be used.",
                    e);
            multicastInterface = null;
        }
    }

    @Override
    public boolean handleConnection(Socket socket, Object obj) {
        if (!(obj instanceof LineTapRequest)) {
            logger.warn("Received unexpected message with type: "
                    + obj.getClass().getName() + ". Disconnecting ...");
            return true;
        }

        LineTapRequest message = (LineTapRequest) obj;
        DacReceiveThread receiver = null;
        try {
            receiver = getReceiver(message);
        } catch (UnknownHostException | SocketException e) {
            logger.error("Error occurred looking address for request {}",
                    message, e);
        }

        if (receiver == null) {
            // no receiver found
            return true;
        }

        LineTapCommunicator comms = new LineTapCommunicator(this,
                message.getTransmitterGroup(), message.getChannel(), socket);
        receiver.subscribe(comms);
        comms.start();
        return false;
    }

    private DacReceiveThread getReceiver(LineTapRequest request)
            throws UnknownHostException, SocketException {
        synchronized (receivers) {
            int receivePort = request.getReceivePort();
            DacReceiveThread receiver = null;
            String receiverAddress = request.getDacReceiveAddress();
            if (receiverAddress != null) {
                InetAddress address = InetAddress.getByName(receiverAddress);
                if (address.isMulticastAddress()) {
                    ReceiveKey key = new ReceiveKey(receiverAddress,
                            receivePort);
                    receiver = receivers.get(key);
                    if ((receiver == null) || !receiver.isAlive()) {
                        receiver = new DacReceiveThread(multicastInterface,
                                address, receivePort);
                        receivers.put(key, receiver);
                        receiver.start();
                    }
                } else if (NetworkInterface.getByInetAddress(address) == null) {
                    logger.error(
                            "Unable open line tap because the receive address({}) is not valid for this host.",
                            receiverAddress);
                    return null;
                }
            }
            if (receiver == null) {
                ReceiveKey key = new ReceiveKey(receivePort);
                receiver = receivers.get(key);
                if ((receiver == null) || !receiver.isAlive()) {
                    receiver = new DacReceiveThread(receivePort);
                    receivers.put(key, receiver);
                    receiver.start();
                }
            }
            return receiver;
        }
    }

    public void unsubscribe(LineTapCommunicator comms) {
        synchronized (receivers) {
            Iterator<DacReceiveThread> it = receivers.values().iterator();
            while (it.hasNext()) {
                DacReceiveThread receiver = it.next();
                receiver.unsubscribe(comms);
                if (!receiver.hasSubscribers()) {
                    receiver.halt();
                    it.remove();
                }
            }
        }
    }

    @Override
    protected Set<Class<? extends Object>> getTypesHandled() {
        return Collections.<Class<?>> singleton(LineTapRequest.class);
    }

    @Override
    protected void shutdownInternal() {
        // NOOP
    }

    private static final class ReceiveKey {

        private final String address;

        private final int port;

        private final int hashCode;

        public ReceiveKey(String address, int port) {
            this.address = address;
            this.port = port;
            final int prime = 31;
            int hashCode = 1;
            hashCode = (prime * hashCode)
                    + ((address == null) ? 0 : address.hashCode());
            hashCode = (prime * hashCode) + port;
            this.hashCode = hashCode;
        }

        public ReceiveKey(int port) {
            this(null, port);
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            ReceiveKey other = (ReceiveKey) obj;
            if (address == null) {
                if (other.address != null) {
                    return false;
                }
            } else if (!address.equals(other.address)) {
                return false;
            }
            if (port != other.port) {
                return false;
            }
            return true;
        }

    }

}
