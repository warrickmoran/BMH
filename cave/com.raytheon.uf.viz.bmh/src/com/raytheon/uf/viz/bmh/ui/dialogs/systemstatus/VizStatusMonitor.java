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
package com.raytheon.uf.viz.bmh.ui.dialogs.systemstatus;

import com.raytheon.uf.common.bmh.notify.status.DacHardwareStatusNotification;
import com.raytheon.uf.common.bmh.systemstatus.SystemStatusMonitor;
import com.raytheon.uf.common.bmh.systemstatus.SystemStatusRequest;
import com.raytheon.uf.common.jms.notification.INotificationObserver;
import com.raytheon.uf.common.jms.notification.NotificationException;
import com.raytheon.uf.common.jms.notification.NotificationMessage;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.bmh.BMHJmsDestinations;
import com.raytheon.uf.viz.bmh.data.BmhUtils;
import com.raytheon.uf.viz.core.notification.jobs.NotificationManagerJob;

/**
 * 
 * {@link SystemStatusMonitor} implementation which automatically requests
 * current status from edex on construction and monitors jms for status updates.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Nov 18, 2014  3817     bsteffen    Initial creation
 * Jan 27, 2015  4029     bkowal      Added {@link #updateDacHardwareStatus(DacHardwareStatusNotification)}
 *                                    to override the buffer size.
 * Jan 29, 2015  4029     bkowal      Limit dac voltage variation during status comparison.
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class VizStatusMonitor extends SystemStatusMonitor implements
        INotificationObserver {

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(VizStatusMonitor.class);

    public VizStatusMonitor() {
        super(requestStatus());
        NotificationManagerJob.addObserver(
                BMHJmsDestinations.getStatusDestination(), this);
    }

    @Override
    public void notificationArrived(NotificationMessage[] messages) {
        for (NotificationMessage message : messages) {
            try {
                handleStatusMessage(message.getMessagePayload());
            } catch (NotificationException e) {
                statusHandler
                        .error("Unexpected error updating bmh system status, some statuses may not be accurate",
                                e);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.common.bmh.systemstatus.SystemStatusMonitor#
     * updateDacHardwareStatus
     * (com.raytheon.uf.common.bmh.notify.status.DacHardwareStatusNotification)
     */
    @Override
    protected void updateDacHardwareStatus(
            DacHardwareStatusNotification dacStatus) {
        /*
         * Viz status does not utilize the buffer field. So, we do not want to
         * trigger status updates whenever the buffer size changes.
         */
        dacStatus.setBufferSize(0);
        /*
         * Viz status does not care about the true voltage of each dac power
         * supply, it is only interested in indicating when their is no voltage,
         * low voltage, or sufficient voltage - so, the voltage numbers will be
         * adjusted to be at a fixed value within that range.
         */
        dacStatus
                .setPsu1Voltage(this.adjustVoltage(dacStatus.getPsu1Voltage()));
        dacStatus
                .setPsu2Voltage(this.adjustVoltage(dacStatus.getPsu2Voltage()));

        super.updateDacHardwareStatus(dacStatus);
    }

    /**
     * Converts the specified voltage to one of three defined values within each
     * of the possible ranges. This conversion will ensure that minor voltage
     * fluctuations do not trigger a complete refresh of the dac portion of the
     * status dialog.
     * 
     * @param voltage
     *            the specified voltage
     * @return the adjusted voltage
     */
    private double adjustVoltage(double voltage) {
        /*
         * if voltage is NaN, leave it as it is indicating no voltage reading.
         */
        if (Double.isNaN(voltage)) {
            return voltage;
        }

        /*
         * if voltage is less than 8, return 7.0 indicating low voltage.
         */
        if (voltage < 8) {
            return 7.0;
        }

        /*
         * voltage reading is within the desired range, return 12.0.
         */
        return 12.0;
    }

    public void dispose() {
        NotificationManagerJob.removeObserver(
                BMHJmsDestinations.getStatusDestination(), this);
        listeners.clear();
    }

    private static SystemStatusMonitor requestStatus() {
        try {
            return (SystemStatusMonitor) BmhUtils
                    .sendRequest(new SystemStatusRequest());
        } catch (Exception e) {
            statusHandler
                    .error("An error has occured getting bmh system status, status will be unknown until updates are received.",
                            e);
            return null;
        }
    }

}
