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
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Abstract thread for servers that are listening for connections within the
 * comms manager.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Aug 15, 2014  3486     bsteffen    Initial Creation
 * Sep 24, 2014  3485     bsteffen    Better logging and smoother shutdown.
 * Jun 05, 2015  4482     rjpeter     Ignore IPVS port checks.
 * Nov 11, 2015  5114     rjpeter     Updated CommsManager to use a single port.
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public abstract class AbstractServerThread extends Thread {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    protected final AtomicBoolean shutdown = new AtomicBoolean(false);

    protected final SocketListener socketListener;

    private final BlockingQueue<ConnectionPair> queue;

    private class ConnectionPair {
        final Socket socket;

        final Object obj;

        final long createTime = System.currentTimeMillis();

        public ConnectionPair(Socket socket, Object obj) {
            this.socket = socket;
            this.obj = obj;
        }

        /**
         * @return the socket
         */
        public Socket getSocket() {
            return socket;
        }

        /**
         * @return the obj
         */
        public Object getObj() {
            return obj;
        }

        public long getCreateTime() {
            return createTime;
        }
    }

    /**
     * Create a server for listening to an external connection.
     * 
     * @param socketListener
     *            the SocketListener to register with
     * @param queueSize
     *            The size of the queue for waiting connections. Value of zero
     *            or less utilizing a synchronous queue.
     * @throws IOException
     */
    public AbstractServerThread(SocketListener socketListener, int queueSize)
            throws IOException {
        super();
        this.setName(getClass().getSimpleName());
        this.socketListener = socketListener;
        if (queueSize > 0) {
            queue = new ArrayBlockingQueue<>(queueSize);
        } else {
            queue = new SynchronousQueue<>();
        }

    }

    @Override
    public void run() {
        for (Class<?> clazz : getTypesHandled()) {
            socketListener.registerListener(clazz, this);
        }

        logger.info("{} is now handling connections ", this.getClass()
                .getSimpleName());
        while (shutdown.get() == false) {
            try {
                ConnectionPair pair = null;
                try {
                    pair = queue.take();
                } catch (InterruptedException e) {
                    // ignore
                }
                if (pair != null) {
                    Object obj = pair.getObj();
                    logger.info("Handling {} request.", obj.getClass()
                            .getName());

                    handleConnectionInternal(pair.getSocket(), obj);
                }
            } catch (Throwable e) {
                logger.error("Error occurred accepting connection", e);
            }
        }

        for (Class<?> clazz : getTypesHandled()) {
            socketListener.removeListener(clazz, this);
        }

        logger.info("{} is no longer handling connections ", this.getClass()
                .getSimpleName());
    }

    public void handleConnection(Socket socket, Object initialObj,
            long timeoutMillis) throws InterruptedException {
        ConnectionPair head = queue.peek();
        if ((head != null)
                && ((System.currentTimeMillis() - head.getCreateTime()) > timeoutMillis)) {
            logger.error("Connection has been waiting for longer than timeout.  Interrupting Server Thread");
            this.interrupt();
        }

        if (queue.offer(new ConnectionPair(socket, initialObj), timeoutMillis,
                TimeUnit.MILLISECONDS) == false) {
            logger.error(
                    "Timed out waiting to accept socket for {}.  Interrupting Server Thread",
                    initialObj.getClass());
            this.interrupt();
        }
    }

    protected abstract void handleConnectionInternal(Socket socket,
            Object initialObj) throws Exception;

    protected abstract Set<Class<?>> getTypesHandled();

    public synchronized void shutdown() {
        if (shutdown.compareAndSet(false, true)) {
            this.shutdownInternal();
            this.interrupt();
        }
    }

    protected abstract void shutdownInternal();
}
