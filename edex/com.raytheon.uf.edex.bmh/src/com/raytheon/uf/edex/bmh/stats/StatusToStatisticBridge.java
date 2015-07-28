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
package com.raytheon.uf.edex.bmh.stats;

import com.raytheon.uf.common.bmh.notify.status.DacHardwareStatusNotification;
import com.raytheon.uf.common.bmh.stats.DacStatisticEvent;
import com.raytheon.uf.common.event.EventBus;

/**
 * BMH Statistic Bridge for status messages.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 6, 2015  3942      rjpeter     Initial creation
 * Jul 28, 2015 4686      bkowal      Moved statistics to common.
 * 
 * </pre>
 * 
 * @author rjpeter
 * @version 1.0
 */

public class StatusToStatisticBridge {
    public void handleStatusMessage(Object statusMessage) {
        if (statusMessage instanceof DacHardwareStatusNotification) {
            handleDacHardwareStatus((DacHardwareStatusNotification) statusMessage);
        }
    }

    protected void handleDacHardwareStatus(DacHardwareStatusNotification status) {
        DacStatisticEvent stat = new DacStatisticEvent();
        stat.setTransmitterGroup(status.getTransmitterGroup());
        stat.setBufferSize(status.getBufferSize());
        stat.setRecoverablePacketErrors(status.getRecoverablePacketErrors());
        stat.setUnrecoverablePacketErrors(status.getUnrecoverablePacketErrors());
        EventBus.publish(stat);
    }
}
