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
package com.raytheon.uf.edex.bmh.comms;

import java.io.IOException;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raytheon.uf.common.bmh.comms.LineTapDisconnect;
import com.raytheon.uf.common.bmh.dac.IDacListener;
import com.raytheon.uf.common.serialization.SerializationUtil;

/**
 * 
 * Communicate over a socket to restream audio packets from the dac.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Aug 04, 2014  2487     bsteffen    Initial creation
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class LineTapCommunicator extends Thread implements IDacListener {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final LineTapServer server;

    private final int channel;

    private final String groupName;

    private final Socket socket;

    public LineTapCommunicator(LineTapServer server, String group,
            int channel, Socket socket) {
        this.server = server;
        this.groupName = group;
        this.channel = channel;
        this.socket = socket;
    }

    @Override
    public void dataArrived(byte[] payload) {
        if (socket.isClosed()) {
            return;
        }
        try {
            socket.getOutputStream().write(payload);
        } catch (IOException e) {
            logger.error("Error sending line tap packets for group + "
                    + groupName, e);
        }
    }

    @Override
    public void run() {
        try {
            SerializationUtil.transformFromThrift(LineTapDisconnect.class,
                    socket.getInputStream());
        } catch (Throwable e) {
            logger.error("Unexpected error while disconnecting from + "
                    + groupName, e);
        } finally {
            server.unsubscribe(this);
            try {
                socket.close();
            } catch (IOException e) {
                logger.error("Unexpected error while closing connection for + "
                        + groupName, e);
            }
        }
    }

    @Override
    public int getChannel() {
        return channel;
    }

    public String getGroupName() {
        return groupName;
    }

}
