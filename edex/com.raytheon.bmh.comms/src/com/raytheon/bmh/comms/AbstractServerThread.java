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

import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raytheon.uf.common.serialization.SerializationException;

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
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public abstract class AbstractServerThread extends Thread {

    private static final Logger logger = LoggerFactory
            .getLogger(AbstractServerThread.class);

    private ServerSocket server;

    /**
     * Create a server for listening to dac transmit applications.
     * 
     * @param config
     *            the config to use for this server.
     * @throws IOException
     */
    public AbstractServerThread(int port) throws IOException {
        super();
        this.setName(getClass().getSimpleName());
        server = new ServerSocket(port);
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
        ServerSocket server = new ServerSocket(port);
        ServerSocket old = this.server;
        this.server = server;
        if (old != null) {
            try {
                old.close();
            } catch (IOException e) {
                logger.error("Failed to close server socket for {}", getName(),
                        e);
            }
        }
    }

    @Override
    public void run() {
        logger.info("{} is now handling connections ", this.getClass()
                .getSimpleName());
        while (server != null && !server.isClosed()) {
            ServerSocket server = this.server;
            Socket socket = null;
            try {
                socket = server.accept();
                socket.setTcpNoDelay(true);

                if (socket.getInputStream() == null) {

                    continue;
                }
            } catch (Throwable e) {
                /*
                 * do not log if this is the exact exception we are expecting
                 * for closing the connection(either from port change or
                 * shutdown)
                 */
                if (server == this.server || !(e instanceof SocketException)
                        || !"Socket closed".equals(e.getMessage())) {
                    logger.error("Unexpected error accepting a connection", e);
                }
                continue;
            }
            try {
                handleConnection(socket);
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
        logger.info("{} is no longer handling connections ", this.getClass()
                .getSimpleName());
    }

    protected abstract void handleConnection(Socket socket) throws Exception;

}
