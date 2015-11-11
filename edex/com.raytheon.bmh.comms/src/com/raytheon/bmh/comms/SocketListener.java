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
package com.raytheon.bmh.comms;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raytheon.uf.common.serialization.SerializationException;
import com.raytheon.uf.common.serialization.SerializationUtil;

/**
 * Listens for connections to a socket and passes off to registered
 * ServerThread.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 11, 2015 5114      rjpeter     Initial creation
 * </pre>
 * 
 * @author rjpeter
 * @version 1.0
 */
public class SocketListener extends Thread {
    private static final long ACCEPT_TIME = 10000;

    private final Logger logger = LoggerFactory.getLogger(SocketListener.class);

    private volatile ServerSocket server;

    private final SocketAcceptor acceptor;

    private final ConcurrentMap<Class<?>, AbstractServerThread> registeredTypes = new ConcurrentHashMap<>(
            8, 1, 4);

    /**
     * Create a server for listening to dac transmit applications.
     * 
     * @param config
     *            the config to use for this server.
     * @throws IOException
     */
    public SocketListener(int port) throws IOException {
        super();
        this.setName(getClass().getSimpleName());
        server = new ServerSocket(port);
        acceptor = new SocketAcceptor();
    }

    public void registerListener(Class<?> clazz, AbstractServerThread server) {
        AbstractServerThread previous = registeredTypes.putIfAbsent(clazz,
                server);
        if (previous != null) {
            throw new IllegalArgumentException("Failed to register listener "
                    + server.getClass().getName() + " for " + clazz.getName()
                    + ". " + previous.getClass().getName()
                    + " already registered.");
        }
    }

    public void removeListener(Class<?> clazz, AbstractServerThread server) {
        registeredTypes.remove(clazz, server);
    }

    public void shutdown() {
        ServerSocket server = this.server;
        this.server = null;
        if (server != null) {
            try {
                server.close();
            } catch (IOException e) {
                logger.error("Failed to close server socket for {}", getName(),
                        e);
            }
        }
    }

    public void changePort(int port) throws IOException {
        if (server.getLocalPort() != port) {
            ServerSocket server = new ServerSocket(port);
            ServerSocket old = this.server;
            this.server = server;

            if (old != null) {
                try {
                    old.close();
                } catch (IOException e) {
                    logger.error("Failed to close server socket for {}",
                            getName(), e);
                }
            }
        }
    }

    @Override
    public void run() {
        acceptor.start();
        logger.info("{} is now accepting connections ", this.getClass()
                .getSimpleName());
        while ((server != null) && !server.isClosed()) {
            ServerSocket server = this.server;
            Socket socket = null;
            try {
                socket = server.accept();
                socket.setTcpNoDelay(true);

                if (socket.getInputStream() == null) {
                    socket.close();
                    continue;
                }
            } catch (Throwable e) {
                /*
                 * do not log if this is the exact exception we are expecting
                 * for closing the connection(either from port change or
                 * shutdown)
                 */
                if ((server == this.server) || !(e instanceof SocketException)
                        || !"Socket closed".equals(e.getMessage())) {
                    logger.error("Unexpected error accepting a connection", e);
                }
                continue;
            }

            acceptor.acceptConnection(socket);
        }

        logger.info("{} is no longer accepting connections ", this.getClass()
                .getSimpleName());
        acceptor.wake();
    }

    /*
     * TODO: Should this be a pool instead of single thread?
     */
    private class SocketAcceptor extends Thread {

        private final Object lock = new Object();

        private volatile Socket currentSocket;

        private volatile long lastAcceptTime;

        public SocketAcceptor() {
            setName(getClass().getSimpleName());
        }

        /**
         * 
         * @param socket
         */
        private void acceptConnection(Socket socket) {
            synchronized (lock) {
                long sleepTime = (lastAcceptTime + ACCEPT_TIME)
                        - System.currentTimeMillis();

                while ((currentSocket != null) && (sleepTime > 0)) {
                    try {
                        lock.wait(sleepTime);
                    } catch (InterruptedException e) {
                        // ignore
                    }

                    sleepTime = (lastAcceptTime + ACCEPT_TIME)
                            - System.currentTimeMillis();
                }

                if (currentSocket != null) {
                    logger.warn(
                            "Socket {} has taken too long to be accepted.  Closing socket",
                            socket.toString());

                    try {
                        currentSocket.close();
                    } catch (IOException e) {
                        logger.error(
                                "Error occurred closing socket that timed out on accept",
                                e);
                    }
                }

                currentSocket = socket;
                lock.notify();
            }
        }

        /**
         * Wakes up sleeping thread.
         */
        private void wake() {
            synchronized (lock) {
                lock.notifyAll();
            }
        }

        @Override
        public void run() {
            while ((server != null) && !server.isClosed()) {
                try {
                    Socket socket = null;

                    synchronized (lock) {
                        while ((server != null) && !server.isClosed()
                                && (currentSocket == null)) {
                            try {
                                lock.wait();
                            } catch (InterruptedException e) {
                                // ignore
                            }
                        }

                        socket = currentSocket;
                        lastAcceptTime = System.currentTimeMillis();
                        currentSocket = null;
                    }

                    if (socket != null) {
                        try {
                            Object obj = SerializationUtil.transformFromThrift(
                                    Object.class, socket.getInputStream());
                            Class<?> objClass = obj.getClass();
                            AbstractServerThread listener = registeredTypes
                                    .get(obj.getClass());

                            if (listener == null) {
                                // check for inheritance use cases
                                for (Class<?> clazz : registeredTypes.keySet()) {
                                    if (clazz.isAssignableFrom(objClass)) {
                                        listener = registeredTypes.get(clazz);
                                        break;
                                    }
                                }

                            }

                            if (listener != null) {
                                listener.handleConnection(socket, obj,
                                        ACCEPT_TIME);
                            } else {
                                logger.error(
                                        "No listener for Object {} defined.  Rejecting connection",
                                        obj.getClass().getName());
                                socket.close();
                            }
                        } catch (SerializationException e) {
                            /*
                             * IPVS creates a connection on the port and then
                             * closes it to validate process is available.
                             * Ignore them since it will happen every second.
                             */
                            boolean printError = true;
                            if (e.getCause() instanceof TTransportException) {
                                if (((TTransportException) e.getCause())
                                        .getType() == TTransportException.END_OF_FILE) {
                                    printError = false;
                                }
                            }

                            if (printError) {
                                logger.error("Error accepting client", e);
                            }

                            try {
                                socket.close();
                            } catch (IOException e2) {
                                logger.error("Error closing socket", e2);
                            }
                        } catch (Throwable e) {
                            try {
                                socket.close();
                            } catch (IOException e2) {
                                logger.error("Error closing socket", e2);
                            }
                            logger.error("Error accepting client", e);
                        }
                    }
                } catch (Throwable e) {
                    logger.error("Error occurred accepting socket", e);
                }
            }
        }
    }
}
