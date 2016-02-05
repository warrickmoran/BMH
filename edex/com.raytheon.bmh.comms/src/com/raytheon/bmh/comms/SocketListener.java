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
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raytheon.uf.common.serialization.SerializationException;
import com.raytheon.uf.common.serialization.SerializationUtil;

/**
 * Listens for connections to a socket. Socket is then passed off to a thread
 * pool for reading initial thrift object from the socket. Based on initial
 * object, socket is then passed to registered listener. The listener can
 * complete the task inline on the thread pool for tasks only taking a few
 * seconds. Tasks more than a few seconds should be handed off to a separate
 * thread to allow the thread pool to stay open to handle new connections.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 11, 2015 5114      rjpeter     Initial creation.
 * Dec 15, 2015  5114     rjpeter     Updated to use a ThreadPool.
 * </pre>
 * 
 * @author rjpeter
 * @version 1.0
 */
public class SocketListener extends Thread {
    private static final long TIMEOUT = 10000;

    private static final int POOL_SIZE = 16;

    private final Logger logger = LoggerFactory.getLogger(SocketListener.class);

    private volatile ServerSocket server;

    private final ConcurrentMap<Class<?>, AbstractServer> registeredTypes = new ConcurrentHashMap<>(
            8, 1, 2);

    /**
     * Allow 1 task to be waiting to avoid having to coordinate 1 thread
     * finishing and submitting of a new task.
     */
    private final ThreadPoolExecutor threadPool = new ThreadPoolExecutor(1,
            POOL_SIZE, 1, TimeUnit.MINUTES, new ArrayBlockingQueue<Runnable>(1));

    private final Set<SocketAcceptor> runningAcceptors = new LinkedHashSet<>();

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
    }

    public void registerListener(Class<?> clazz, AbstractServer server) {
        AbstractServer previous = registeredTypes.putIfAbsent(clazz, server);
        if (previous != null) {
            throw new IllegalArgumentException("Failed to register listener "
                    + server.getClass().getName() + " for " + clazz.getName()
                    + ". " + previous.getClass().getName()
                    + " already registered.");
        }
    }

    public void removeListener(Class<?> clazz, AbstractServer server) {
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
        threadPool.shutdown();
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
        logger.info("{} is now accepting connections ", this.getClass()
                .getSimpleName());
        while ((server != null) && !server.isClosed()) {
            ServerSocket server = this.server;
            Socket socket = null;
            boolean closeSocket = true;

            try {
                socket = server.accept();
                socket.setTcpNoDelay(true);

                if (socket.getInputStream() == null) {
                    continue;
                }

                SocketAcceptor acceptor = new SocketAcceptor(socket);
                boolean threadWaited = false;

                synchronized (runningAcceptors) {
                    /*
                     * If all threads busy, check if a previous acceptor has
                     * timed out
                     */
                    while ((runningAcceptors.size() >= POOL_SIZE)
                            && (acceptor.hasTimedOut() == false)) {
                        long curTime = System.currentTimeMillis();
                        long sleepTime = TIMEOUT;

                        for (SocketAcceptor acceptorToCheck : runningAcceptors) {
                            if (acceptorToCheck.timeOut < curTime) {
                                sleepTime = 10;
                                if (acceptorToCheck.socket.isClosed()) {
                                    logger.warn(
                                            "SocketAcceptor has timed out and socket has been closed. Interrupting Thread {}",
                                            acceptorToCheck.thread);
                                    acceptorToCheck.thread.interrupt();
                                } else {
                                    logger.warn(
                                            "SocketAcceptor has timed out. Closing Socket {} on Thread {}",
                                            acceptorToCheck.socket,
                                            acceptorToCheck.thread);

                                    // close the socket used by the thread
                                    try {
                                        acceptorToCheck.socket.close();
                                    } catch (IOException e) {
                                        logger.error(
                                                "Error occurred closing socket",
                                                e);
                                    }
                                }
                            } else {
                                sleepTime = Math.min(sleepTime,
                                        acceptor.timeOut - curTime);
                                /*
                                 * set ordered by insert time, no need to check
                                 * any further as this will be the oldest socket
                                 * that is still valid
                                 */
                                break;
                            }
                        }

                        logger.warn(
                                "All socket acceptors in use, waiting for available thread to process {}",
                                socket);

                        try {
                            threadWaited = true;
                            // notified on SocketAcceptor completion
                            runningAcceptors.wait(sleepTime);
                        } catch (InterruptedException e2) {
                            // ignore
                        }
                    }

                }
                if (acceptor.hasTimedOut()) {
                    logger.error("SocketListener has timed out. No free threads");
                } else {
                    if (threadWaited) {
                        logger.info("Thread available to process {}", socket);
                    }

                    threadPool.submit(acceptor);
                    synchronized (runningAcceptors) {
                        runningAcceptors.add(acceptor);
                    }
                    closeSocket = false;
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
            } finally {
                if (closeSocket && (socket != null)) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        logger.error("Error occurred closing socket", e);
                    }
                }
            }
        }

        logger.info("{} is no longer accepting connections ", this.getClass()
                .getSimpleName());
    }

    private class SocketAcceptor implements Runnable {

        private final Socket socket;

        private final long timeOut = System.currentTimeMillis() + TIMEOUT;

        private volatile Thread thread = null;

        public SocketAcceptor(Socket socket) {
            this.socket = socket;
        }

        public boolean hasTimedOut() {
            return System.currentTimeMillis() >= timeOut;
        }

        @Override
        public void run() {
            thread = Thread.currentThread();
            thread.setName(this.getClass().getSimpleName());
            boolean closeSocket = true;

            try {
                Object obj = SerializationUtil.transformFromThrift(
                        Object.class, socket.getInputStream());
                Class<?> objClass = obj.getClass();
                AbstractServer server = registeredTypes.get(obj.getClass());

                if (server == null) {
                    // check for inheritance use cases
                    for (Class<?> clazz : registeredTypes.keySet()) {
                        if (clazz.isAssignableFrom(objClass)) {
                            server = registeredTypes.get(clazz);
                            break;
                        }
                    }

                }

                if (server != null) {
                    thread.setName(server.getClass().getSimpleName());
                    /*
                     * Note this needs to finish within timeout period to avoid
                     * having socket closed. Any task taking more than a few
                     * seconds should be in its own thread
                     */
                    closeSocket = server.handleConnection(socket, obj);
                } else {
                    logger.error(
                            "No listener for Object {} defined.  Rejecting connection",
                            obj.getClass().getName());
                }
            } catch (SerializationException e) {
                /*
                 * IPVS creates a connection on the port and then closes it to
                 * validate process is available. Ignore them since it will
                 * happen every second.
                 */
                boolean printError = true;
                if (e.getCause() instanceof TTransportException) {
                    if (((TTransportException) e.getCause()).getType() == TTransportException.END_OF_FILE) {
                        printError = false;
                    }
                }

                if (printError) {
                    logger.error("Error accepting client", e);
                }
            } catch (Throwable e) {
                logger.error("Error accepting client", e);
            } finally {
                if (closeSocket) {
                    try {
                        socket.close();
                    } catch (IOException e2) {
                        logger.error("Error closing socket", e2);
                    }
                }

                thread.setName(this.getClass().getSimpleName());

                /*
                 * Notify SocketListener that a thread is available in use case
                 * where all threads were busy
                 */
                synchronized (runningAcceptors) {
                    runningAcceptors.remove(this);
                    runningAcceptors.notify();
                }
            }
        }
    }
}
