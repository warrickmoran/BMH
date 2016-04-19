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
package com.raytheon.bmh.comms.playlist;

import java.io.IOException;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raytheon.bmh.comms.AbstractServer;
import com.raytheon.bmh.comms.CommsManager;
import com.raytheon.bmh.comms.cluster.ClusterServer;
import com.raytheon.bmh.comms.dactransmit.DacTransmitServer;
import com.raytheon.uf.common.bmh.comms.SendPlaylistMessage;
import com.raytheon.uf.common.bmh.comms.SendPlaylistResponse;
import com.raytheon.uf.common.bmh.notify.NoPlaybackMessageNotification;
import com.raytheon.uf.common.serialization.SerializationUtil;
import com.raytheon.uf.common.time.util.TimeUtil;

/**
 * Listens for and forwards on @{link SendPlaylistMessage}.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 01, 2016 5308       rjpeter     Initial creation
 * Mar 14, 2016 5472       rjpeter     Wait for response
 * </pre>
 * 
 * @author rjpeter
 * @version 1.0
 */

public class PlaylistServer extends AbstractServer {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ClusterServer clusterServer;

    private final DacTransmitServer transmitServer;

    private final Map<String, AwaitingResponse> awaitingResponseMap = new HashMap<>();

    private final long RESPONSE_TIMEOUT = 15 * TimeUtil.MILLIS_PER_SECOND;

    private class AwaitingResponse {
        private final String group;

        private SendPlaylistResponse response = null;

        private AwaitingResponse(String group) {
            this.group = group;
        }

        public synchronized SendPlaylistResponse waitForResponse() {
            SendPlaylistResponse rval = response;

            /*
             * Check if response was already populated by another thread before
             * waitForResponse was called
             */
            if (rval == null) {
                try {
                    this.wait(RESPONSE_TIMEOUT);
                } catch (InterruptedException e) {
                    // ignore
                }

                // update rval
                rval = response;

                if (rval == null) {
                    rval = new SendPlaylistResponse(group,
                            new NoPlaybackMessageNotification(group,
                                    "Timed out waiting for playlist for "
                                            + group));
                }
            }

            return rval;
        }

        public synchronized void sendResponse(SendPlaylistResponse response) {
            this.response = response;
            this.notifyAll();
        }

    }

    /**
     * @param port
     * @throws IOException
     */
    public PlaylistServer(ClusterServer clusterServer,
            final DacTransmitServer transmitServer,
            final CommsManager commsManager) throws IOException {
        super(commsManager.getSocketListener());
        this.clusterServer = clusterServer;
        this.transmitServer = transmitServer;
    }

    @Override
    public boolean handleConnection(Socket socket, Object obj) {
        if (obj instanceof SendPlaylistMessage) {
            long startTime = System.currentTimeMillis();
            SendPlaylistMessage msg = (SendPlaylistMessage) obj;
            String group = msg.getTransmitterGroup();
            SendPlaylistResponse response = null;
            AwaitingResponse responseWaiter = null;

            /*
             * technically only one of the below would need to handle the
             * message. Sending to both for edge cases such as active fail over.
             */
            if (transmitServer.isConnectedToDacTransmit(group)
                    || clusterServer.isConnected(group)) {
                responseWaiter = getResponseWaiter(group);
                transmitServer.sendToDac(msg);
                clusterServer.sendDataToAll(msg);
                response = responseWaiter.waitForResponse();
            } else {
                response = new SendPlaylistResponse(group,
                        new NoPlaybackMessageNotification(group,
                                "Unable to connect to DAC for transmitter "
                                        + group));
            }

            try {
                // Send response back to client
                SerializationUtil.transformToThriftUsingStream(response,
                        socket.getOutputStream());
                logger.info(
                        "Handled playlist request for {} in {}",
                        group,
                        TimeUtil.prettyDuration(System.currentTimeMillis()
                                - startTime));
            } catch (Exception e) {
                logger.error(
                        "Error occurred responding to Playlist request for transmitter group {}",
                        group, e);
            }
        } else {
            logger.warn("Received unexpected message with type: "
                    + obj.getClass().getName() + ". Disconnecting ...");
        }

        return true;
    }

    @Override
    protected Set<Class<?>> getTypesHandled() {
        Set<Class<?>> rval = new HashSet<>(1, 1);
        rval.add(SendPlaylistMessage.class);
        return Collections.unmodifiableSet(rval);
    }

    @Override
    protected void shutdownInternal() {
    }

    protected AwaitingResponse getResponseWaiter(String group) {
        AwaitingResponse responseWaiter = null;
        synchronized (awaitingResponseMap) {
            responseWaiter = awaitingResponseMap.get(group);
            if (responseWaiter == null) {
                responseWaiter = new AwaitingResponse(group);
                awaitingResponseMap.put(group, responseWaiter);
            }
        }

        return responseWaiter;
    }

    public void handleResponse(SendPlaylistResponse response) {
        AwaitingResponse responseWaiter = null;
        synchronized (awaitingResponseMap) {
            responseWaiter = awaitingResponseMap.remove(response.getGroup());
        }

        if (responseWaiter != null) {
            responseWaiter.sendResponse(response);
        }
    }
}