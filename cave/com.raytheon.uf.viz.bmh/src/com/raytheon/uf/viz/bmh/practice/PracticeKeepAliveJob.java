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
package com.raytheon.uf.viz.bmh.practice;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.raytheon.uf.common.bmh.request.PracticeModeRequest;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.viz.bmh.data.BmhUtils;

/**
 * 
 * Job that should be run while BMH is in practice mode to tell the server that
 * practice mode is still active.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Oct 22, 2014  2687     bsteffen     Initial creation
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class PracticeKeepAliveJob extends Job {

    private final long RUN_INTERVAL = 30 * TimeUtil.MILLIS_PER_MINUTE;

    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(PracticeKeepAliveJob.class);

    private volatile boolean shutdown = false;

    public PracticeKeepAliveJob() {
        super("Updating BMH practice mode");
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        if (shutdown) {
            /*
             * Cancel appears to the UI so the user is less confused that this
             * is running after they exited bmh..
             */
            cancel();
        }
        try {
            BmhUtils.sendRequest(new PracticeModeRequest(!shutdown));
        } catch (Exception e) {
            statusHandler.error("Unable to start BMH practice mode.", e);
        }
        if (!shutdown) {
            schedule(RUN_INTERVAL);
        }
        return Status.OK_STATUS;
    }

    /**
     * Tell the server to stop running practice mode.
     */
    public void shutdown() {
        shutdown = true;
        /* Must cancel before it is possible to reschedule sooner. */
        cancel();
        schedule();
    }


}
