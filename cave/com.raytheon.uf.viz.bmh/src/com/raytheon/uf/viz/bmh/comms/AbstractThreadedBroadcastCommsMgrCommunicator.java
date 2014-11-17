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
package com.raytheon.uf.viz.bmh.comms;

import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;

import com.raytheon.uf.common.serialization.SerializationException;
import com.raytheon.uf.common.serialization.SerializationUtil;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.bmh.BMHServers;
import com.raytheon.uf.viz.bmh.ui.recordplayback.live.BroadcastException;

/**
 * Threaded abstraction for interacting with the comms manager for on-demand
 * broadcasts.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 12, 2014 3630       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public abstract class AbstractThreadedBroadcastCommsMgrCommunicator extends
        Thread {

    protected final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(getClass());

    private Socket socket;

    /**
     * 
     */
    public AbstractThreadedBroadcastCommsMgrCommunicator(final String name) {
        super(name);
    }

    protected void openCommsConnection() throws CommsCommunicationException {
        String commsLoc = BMHServers.getBroadcastServer();
        if (commsLoc == null) {
            throw new CommsCommunicationException(
                    "No address has been specified for comms manager "
                            + BMHServers.getBroadcastServerKey() + ".");
        }

        URI commsURI = null;
        try {
            commsURI = new URI(commsLoc);
        } catch (URISyntaxException e) {
            throw new CommsCommunicationException(
                    "Invalid address specified for comms manager "
                            + BMHServers.getBroadcastServerKey() + ": "
                            + commsLoc + ".", e);
        }

        try {
            this.socket = new Socket(commsURI.getHost(), commsURI.getPort());
            this.socket.setTcpNoDelay(true);
        } catch (IOException e) {
            throw new CommsCommunicationException(
                    "Failed to connect to comms manager "
                            + BMHServers.getBroadcastServerKey() + ": "
                            + commsLoc + ".", e);
        }
    }

    protected void closeCommsConnection() {
        if (this.socket != null && this.socket.isClosed() == false) {
            try {
                this.socket.close();
            } catch (IOException e) {
                statusHandler.error("Failed to close socket connection!", e);
            }
        }
    }

    protected synchronized void writeToCommsManager(Object msg)
            throws BroadcastException {
        if (this.socket == null || this.socket.isClosed()) {
            return;
        }
        try {
            SerializationUtil.transformToThriftUsingStream(msg,
                    this.socket.getOutputStream());
        } catch (SerializationException | IOException e) {
            throw new BroadcastException(
                    "Failed to send data to comms manager "
                            + BMHServers.getBroadcastServer() + ".", e);
        }
    }

    protected Object readFromCommsManager() throws BroadcastException {
        Object object = null;
        try {
            object = SerializationUtil.transformFromThrift(Object.class,
                    this.socket.getInputStream());
        } catch (SerializationException | IOException e) {
            throw new BroadcastException(
                    "Failed to receive data from comms manager "
                            + BMHServers.getBroadcastServer() + ".", e);
        }
        return object;
    }
}