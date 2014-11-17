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
package com.raytheon.uf.viz.bmh.ui.dialogs.config.transmitter;

import com.raytheon.uf.common.bmh.broadcast.BroadcastStatus;
import com.raytheon.uf.common.bmh.broadcast.TransmitterAlignmentTestCommand;
import com.raytheon.uf.viz.bmh.comms.AbstractThreadedBroadcastCommsMgrCommunicator;
import com.raytheon.uf.viz.bmh.comms.CommsCommunicationException;
import com.raytheon.uf.viz.bmh.ui.recordplayback.live.BroadcastException;

/**
 * Executes and reports the final status of a transmitter alignment test.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 13, 2014 3630       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class TransmitterAlignmentTestThread extends
        AbstractThreadedBroadcastCommsMgrCommunicator {

    public static enum TEST_STATUS {
        UNKNOWN, SUCCESS, FAIL
    }

    private TEST_STATUS status = TEST_STATUS.UNKNOWN;

    private String statusDetails;

    private final TransmitterAlignmentTestCommand command;

    /**
     * @param name
     */
    public TransmitterAlignmentTestThread(
            final TransmitterAlignmentTestCommand command) {
        super(TransmitterAlignmentTestThread.class.getName());
        this.command = command;
    }

    @Override
    public void run() {
        try {
            this.openCommsConnection();
        } catch (CommsCommunicationException e) {
            statusHandler.error("Failed to connect to the Comms Manager!", e);
            this.status = TEST_STATUS.FAIL;
            return;
        }

        try {
            this.writeToCommsManager(this.command);
        } catch (BroadcastException e) {
            statusHandler.error(
                    "Failed to start the Transmitter Alignment Test!", e);
            this.status = TEST_STATUS.FAIL;
            return;
        }

        Object object = null;
        try {
            object = this.readFromCommsManager();
        } catch (BroadcastException e) {
            statusHandler.error(
                    "Failed to receive a response from Comms Manager!", e);
        }

        if (object == null) {
            /* socket connection closed - default to status unknown */
            return;
        }

        if (object instanceof BroadcastStatus == false) {
            /*
             * Unlikely - indicates misuse of the capability.
             */
            statusHandler.warn("Received unexpected message type: "
                    + object.getClass().getName());
            return;
        }

        BroadcastStatus statusMsg = (BroadcastStatus) object;
        this.statusDetails = statusMsg.getMessage();
        if (statusMsg.getStatus()) {
            this.status = TEST_STATUS.SUCCESS;
        } else {
            this.status = TEST_STATUS.FAIL;
        }

        this.closeCommsConnection();
    }

    public void shutdown() {
        this.closeCommsConnection();
    }

    /**
     * @return the status
     */
    public TEST_STATUS getStatus() {
        return status;
    }

    /**
     * @param status
     *            the status to set
     */
    public void setStatus(TEST_STATUS status) {
        this.status = status;
    }

    /**
     * @return the statusDetails
     */
    public String getStatusDetails() {
        return statusDetails;
    }

    /**
     * @param statusDetails
     *            the statusDetails to set
     */
    public void setStatusDetails(String statusDetails) {
        this.statusDetails = statusDetails;
    }
}