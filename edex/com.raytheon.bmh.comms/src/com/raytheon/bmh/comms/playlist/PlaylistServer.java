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
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raytheon.bmh.comms.AbstractServer;
import com.raytheon.bmh.comms.CommsManager;
import com.raytheon.bmh.comms.cluster.ClusterServer;
import com.raytheon.bmh.comms.dactransmit.DacTransmitServer;
import com.raytheon.uf.common.bmh.comms.SendPlaylistMessage;

/**
 * Listens for and forwards on @{link SendPlaylistMessage}.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 1, 2014             rjpeter     Initial creation
 * </pre>
 * 
 * @author rjpeter
 * @version 1.0
 */

public class PlaylistServer extends AbstractServer {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ClusterServer clusterServer;

    private final DacTransmitServer transmitServer;

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
            SendPlaylistMessage msg = (SendPlaylistMessage) obj;
            /*
             * technically only one of the below would need to handle the
             * message. Sending to both for edge cases such as failover
             */
            transmitServer.sendToDac(msg);
            clusterServer.sendDataToAll(msg);
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
}