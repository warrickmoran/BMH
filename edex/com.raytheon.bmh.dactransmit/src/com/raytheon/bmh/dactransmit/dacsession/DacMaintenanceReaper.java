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
package com.raytheon.bmh.dactransmit.dacsession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raytheon.uf.common.time.util.TimeUtil;

/**
 * Will reap a dac maintenance session after a set amount of time. Will also
 * allow for a broadcast that has started late to finish in certain
 * circumstances.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 9, 2015  4364       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class DacMaintenanceReaper implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /*
     * The amount of time the transmit process will have to show that it is
     * successfully actively broadcasting audio before the reaper kills it. If
     * this interval ever passes without the broadcast of any audio, the reaper
     * will complete its tasking.
     */
    private static final long GRACEFUL_ALLOWANCE = 5 * TimeUtil.MILLIS_PER_SECOND;

    private final DacMaintenanceSession session;

    private final MaintenanceBroadcastTransmitThread broadcast;

    private final ControlStatusThread control;

    /**
     * 
     */
    public DacMaintenanceReaper(final DacMaintenanceSession session,
            final MaintenanceBroadcastTransmitThread broadcast,
            final ControlStatusThread control) {
        this.session = session;
        this.broadcast = broadcast;
        this.control = control;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        logger.info("The {} has been activated ...", getClass().getName());
        if (this.isSyncNotComplete()) {
            logger.info("The maintenance session has not yet completed an initial sync with the dac.");
            /*
             * If the initial sync with the dac still has not finished at this
             * point, there is no point in allowing the session time to finish
             * broadcasting audio that needs to be broadcast.
             */
            this.kill();
        }

        boolean gracefulAllowed = true;
        int currentRemainingPacketCount = this.broadcast
                .getRemainingPacketCount();
        while (gracefulAllowed) {
            try {
                Thread.sleep(GRACEFUL_ALLOWANCE);
            } catch (InterruptedException e) {
                logger.warn(
                        "Interrupted while determining if a graceful session shutdown is a possibility.",
                        e);
            }

            int nextRemainingPacketCount = this.broadcast
                    .getRemainingPacketCount();
            if (nextRemainingPacketCount == 0) {
                /*
                 * all packets have been successfully broadcast. the session
                 * will stop naturally.
                 */
                return;
            } else if (nextRemainingPacketCount == currentRemainingPacketCount) {
                /*
                 * no packets have been broadcast since the last check. the
                 * probability of the broadcast finishing naturally is low,
                 * initiate the early kill.
                 */
                gracefulAllowed = false;
            }
            currentRemainingPacketCount = nextRemainingPacketCount;
        }

        this.kill();
    }

    private boolean isSyncNotComplete() {
        return this.session.isInitialSyncCompleted() == false;
    }

    private void kill() {
        logger.info(
                "Reaping the DacMaintenanceSession ... with {} audio packets left to broadcast.",
                this.broadcast.getRemainingPacketCount());
        this.control.shutdown();
        this.broadcast.forceShutdown();
    }
}