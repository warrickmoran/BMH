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
import java.util.concurrent.atomic.AtomicBoolean;

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
 * Dec 15, 2015  5114     rjpeter     Refactored to not be a dedicated thread.
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public abstract class AbstractServer {

    protected final AtomicBoolean shutdown = new AtomicBoolean(false);

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
    public AbstractServer(SocketListener socketListener) throws IOException {
        super();
        for (Class<?> clazz : getTypesHandled()) {
            socketListener.registerListener(clazz, this);
        }
    }

    /**
     * Called by {@link SocketListener} to handle a new connection. As long as
     * task can complete in a few seconds, work may be done on the calling
     * thread as its backed by a threadpool. Any work taking more than a few
     * seconds should be accomplished on a different thread to avoid having the
     * socket closed due to timeout. Returning true states that the task is done
     * and socket will be closed by SocketListener. If socket was passed of to
     * another thread for additional work, this method should return false.
     * 
     * @param socket
     * @param initialObj
     *            First object read from socket.
     * 
     * @return True if the socket should be closed, false otherwise.
     */
    public abstract boolean handleConnection(Socket socket, Object initialObj);

    protected abstract Set<Class<?>> getTypesHandled();

    /**
     * Shutdown any threads / resources that are being hold on to. Specifically
     * threads launched from handleConnection.
     */
    public synchronized void shutdown() {
        if (shutdown.compareAndSet(false, true)) {
            this.shutdownInternal();
        }
    }

    protected abstract void shutdownInternal();
}
