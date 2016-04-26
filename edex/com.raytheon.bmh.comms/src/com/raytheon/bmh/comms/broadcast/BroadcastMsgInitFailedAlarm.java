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

import com.raytheon.bmh.comms.AbstractJmsAlarm;
import com.raytheon.uf.common.bmh.BMH_CATEGORY;
import com.raytheon.uf.common.bmh.notify.BroadcastMsgInitFailedNotification;

/**
 * Used to construct a log message that will be transmitted to AlertViz to
 * inform the user that a specific message could not be initialized for
 * broadcast.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 26, 2016 5561       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class BroadcastMsgInitFailedAlarm extends AbstractJmsAlarm {

    public BroadcastMsgInitFailedAlarm() {
        this(BMH_CATEGORY.MSG_RETRIEVAL_FAILED);
    }

    protected BroadcastMsgInitFailedAlarm(final BMH_CATEGORY bmhCategory) {
        super(bmhCategory);
    }

    public void notify(final BroadcastMsgInitFailedNotification notification) {
        String label = "message";
        if (notification.isInterrupt()) {
            label = "interrupt";
        } else if (notification.getMetadata().isWatch()) {
            label = "watch";
        } else if (notification.getMetadata().isWarning()) {
            label = "warning";
        }

        logger.error(
                "Failed to load {} {} for Transmitter {}. (Message Starts: {}; Message Expires: {}). The message currently cannot be broadcast. Recommendation: please resubmit the message.",
                label, notification.getMessageIdentification(),
                notification.getTransmitterGroup(), notification.getMetadata()
                        .getStart().getTime().toString(),
                notification.getExpire());
    }
}