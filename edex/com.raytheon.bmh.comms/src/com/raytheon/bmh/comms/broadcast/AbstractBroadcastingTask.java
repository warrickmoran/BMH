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
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
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
 * Nov 21, 2014 3845       bkowal      Re-factor/cleanup
 * Dec 1, 2014  3797       bkowal      Support broadcast clustering.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public abstract class AbstractBroadcastingTask extends Thread {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final String description;

    protected Socket socket;

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
            final Exception exception,
            final List<TransmitterGroup> transmitterGroups) {
        BroadcastStatus status = new BroadcastStatus();
        status.setMsgSource(MSGSOURCE.COMMS);
        status.setBroadcastId(this.getName());
        status.setStatus(false);
        status.setTransmitterGroups(transmitterGroups);
        status.setMessage(message);
        status.setException(exception);

        return status;
    }

    public abstract void shutdown();

    /**
     * Returns a {@link List} of {@link TransmitterGroup}s that the
     * {@link AbstractBroadcastingTask} is responsible for/interacting with.
     * 
     * @return a {@link List} of {@link TransmitterGroup}s that the
     *         {@link AbstractBroadcastingTask} is responsible for/interacting
     *         with.
     */
    public abstract List<TransmitterGroup> getTransmitterGroups();

    /**
     * Returns the {@link TransmitterGroup} associated with the specified name
     * if the {@link AbstractBroadcastingTask} is responsible for managing /
     * interacting with the associated {@link TransmitterGroup}.
     * 
     * Note: if the comms manager identifier is ever changed, this function will
     * need to be updated.
     * 
     * @param name
     *            the specified name
     * @return the {@link TransmitterGroup} when it is managed by the current
     *         {@link AbstractBroadcastingTask}; NULL, otherwise
     */
    protected TransmitterGroup getTransmitterGroupByIdentifier(final String name) {
        for (TransmitterGroup tg : this.getTransmitterGroups()) {
            if (tg.getName().equals(name)) {
                return tg;
            }
        }

        return null;
    }

    /**
     * Allows a {@link AbstractBroadcastingTask} to respond to a dac transmit
     * starting on the same server. The specified {@link TransmitterGroup}
     * identifier indicates which {@link TransmitterGroup} is affected by the
     * change.
     * 
     * @param tg
     *            The specified {@link TransmitterGroup} identifier
     */
    public abstract void dacConnectedToServer(final String tg);

    /**
     * Allows a {@link AbstractBroadcastingTask} to respond to a dac transmit
     * stopping on the same server. The specified {@link TransmitterGroup}
     * identifier indicates which {@link TransmitterGroup} is affected by the
     * change.
     * 
     * @param tg
     *            The specified {@link TransmitterGroup} identifier
     */
    public abstract void dacDisconnectedFromServer(final String tg);

    /**
     * Returns text used to differentiate the different type of
     * {@link AbstractBroadcastingTask}s.
     * 
     * @return text for identification/differentiation purposes
     */
    public String getDescription() {
        return this.description;
    }
}