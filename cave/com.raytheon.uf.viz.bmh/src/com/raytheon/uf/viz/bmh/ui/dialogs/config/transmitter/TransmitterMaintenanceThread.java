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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.common.bmh.broadcast.BroadcastStatus;
import com.raytheon.uf.common.bmh.broadcast.TransmitterMaintenanceCommand;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.viz.bmh.comms.AbstractThreadedBroadcastCommsMgrCommunicator;
import com.raytheon.uf.viz.bmh.comms.CommsCommunicationException;
import com.raytheon.uf.viz.bmh.ui.common.utility.DialogUtility;
import com.raytheon.uf.viz.bmh.ui.recordplayback.live.BroadcastException;

/**
 * Executes and reports the final status of a transmitter maintenance operation.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 13, 2014 3630       bkowal      Initial creation
 * Dec 12, 2014 3603       bsteffen    Rename and add runAndReportResult
 * 
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class TransmitterMaintenanceThread extends
        AbstractThreadedBroadcastCommsMgrCommunicator {

    public static enum MAINTENANCE_STATUS {
        UNKNOWN, SUCCESS, FAIL
    }

    private static final int MAINTENANCE_TIMEOUT = 45000;

    private MAINTENANCE_STATUS status = MAINTENANCE_STATUS.UNKNOWN;

    private String statusDetails;

    private final TransmitterMaintenanceCommand command;

    /**
     * @param name
     */
    public TransmitterMaintenanceThread(
            final TransmitterMaintenanceCommand command) {
        super(TransmitterMaintenanceThread.class.getName());
        this.command = command;
    }

    @Override
    public void run() {
        try {
            this.openCommsConnection();
        } catch (CommsCommunicationException e) {
            statusHandler.error("Failed to connect to the Comms Manager!", e);
            this.status = MAINTENANCE_STATUS.FAIL;
            return;
        }

        try {
            this.writeToCommsManager(this.command);
        } catch (BroadcastException e) {
            statusHandler.error(
                    "Failed to start the " + command.getMaintenanceDetails()
                            + "!", e);
            this.status = MAINTENANCE_STATUS.FAIL;
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
            this.status = MAINTENANCE_STATUS.SUCCESS;
        } else {
            this.status = MAINTENANCE_STATUS.FAIL;
        }

        this.closeCommsConnection();
    }

    public void shutdown() {
        this.closeCommsConnection();
    }

    /**
     * @return the status
     */
    public MAINTENANCE_STATUS getStatus() {
        return status;
    }

    /**
     * @param status
     *            the status to set
     */
    public void setStatus(MAINTENANCE_STATUS status) {
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

    /**
     * Run this task, reporting status to the user in the form of a progress
     * dialog and a status message box.
     * 
     * @param statusHandler
     *            for reporting errors
     * @param shell
     *            to use for creating dialogs.
     * @param command
     *            the command to run.
     */
    public static void runAndReportResult(final IUFStatusHandler statusHandler,
            Shell shell, final TransmitterMaintenanceCommand command) {
        final TransmitterMaintenanceThread alignmentTestThread = new TransmitterMaintenanceThread(
                command);
        alignmentTestThread.start();

        /*
         * Block all interaction. Will only last a maximum of 45 seconds
         * worst-case scenario.
         */
        ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
        try {
            dialog.run(true, false, new IRunnableWithProgress() {

                @Override
                public void run(IProgressMonitor monitor) {
                    monitor.beginTask(
                            "Running " + command.getMaintenanceDetails(),
                            IProgressMonitor.UNKNOWN);
                    try {
                        alignmentTestThread.join(MAINTENANCE_TIMEOUT);
                    } catch (InterruptedException e) {
                        statusHandler.error(
                                "Interrupted while waiting for the "
                                        + command.getMaintenanceDetails()
                                        + " to finish.", e);
                    }
                }
            });
        } catch (InvocationTargetException | InterruptedException e) {
            statusHandler.error(
                    "Failed to run the " + command.getMaintenanceDetails()
                            + ".", e);
        }

        // check the status of the task.
        int icon = 0;
        String message = null;
        switch (alignmentTestThread.getStatus()) {
        case FAIL:
            icon = SWT.ICON_ERROR;
            message = alignmentTestThread.getStatusDetails();
            break;
        case SUCCESS:
            icon = SWT.ICON_INFORMATION;
            message = alignmentTestThread.getStatusDetails();
            break;
        case UNKNOWN:
            icon = SWT.ICON_WARNING;
            message = "The final status of the "
                    + command.getMaintenanceDetails()
                    + " is unknown. Please check the server logs.";
            break;
        }
        DialogUtility.showMessageBox(shell, icon | SWT.OK,
                command.getMaintenanceDetails() + " Result", message);
    }
}