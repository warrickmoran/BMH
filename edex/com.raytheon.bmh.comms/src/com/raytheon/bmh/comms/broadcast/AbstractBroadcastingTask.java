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
package com.raytheon.bmh.comms.broadcast;

import java.net.Socket;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raytheon.uf.common.bmh.broadcast.BroadcastStatus;
import com.raytheon.uf.common.bmh.broadcast.OnDemandBroadcastConstants.MSGSOURCE;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.serialization.SerializationUtil;

/**
 * Abstraction of an implementation that completes some type of on-demand
 * streaming / broadcasting.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 11, 2014 3630       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public abstract class AbstractBroadcastingTask extends Thread {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final String description;

    protected final Socket socket;

    /**
     * @param name
     */
    public AbstractBroadcastingTask(String name, final String description,
            Socket socket) {
        super(name);
        this.description = description;
        this.socket = socket;
    }

    /**
     * Sends a reply to the sender of the message that was responsible for the
     * creation of this task. The sender will either be: a) Viz or b) The
     * cluster member that was created in response to the original message
     * received from Viz.
     * 
     * @param msg
     *            the reply msg to send
     * @return true if the reply was successful; false, otherwise
     */
    protected synchronized boolean sendClientReplyMessage(BroadcastStatus msg)
            throws Exception {
        SerializationUtil.transformToThriftUsingStream(msg,
                this.socket.getOutputStream());

        return true;
    }

    protected BroadcastStatus buildErrorStatus(final String message,
            final Exception exception, final List<Transmitter> transmitters) {
        BroadcastStatus status = new BroadcastStatus();
        status.setMsgSource(MSGSOURCE.COMMS);
        status.setBroadcastId(this.getName());
        status.setStatus(false);
        status.setTransmitterGroups(transmitters);
        status.setMessage(message);
        status.setException(exception);

        return status;
    }

    public abstract void shutdown();

    public String getDescription() {
        return this.description;
    }
}