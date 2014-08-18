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
 * 
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
        this.setName(getClass().getName());
        server = new ServerSocket(port);
    }

    public void shutdown() {
        try {
            server.close();
        } catch (IOException e) {
            logger.error("Failed to close server socket for " + getName(), e);
        }
    }

    public void changePort(int port) throws IOException {
        ServerSocket server = new ServerSocket(port);
        ServerSocket old = this.server;
        this.server = server;
        try {
            old.close();
        } catch (IOException e) {
            logger.error("Failed to close server socket for " + getName(), e);
        }
    }

    @Override
    public void run() {
        while (!server.isClosed()) {
            ServerSocket server = this.server;
            Socket socket = null;
            try {
                socket = server.accept();
                socket.setTcpNoDelay(true);
            } catch (Throwable e) {
                if (server != this.server) {
                    logger.debug("Expected error accepting a connection", e);
                } else {
                    logger.error("Unexpected error accepting a connection", e);
                }
                continue;
            }
            try {
                handleConnection(socket);
            } catch (Throwable e) {
                try {
                    socket.close();
                } catch (IOException e2) {
                    logger.error("Error closing socket", e2);
                }
                logger.error("Error accepting client", e);
            }
        }
    }

    protected abstract void handleConnection(Socket socket) throws Exception;

}
