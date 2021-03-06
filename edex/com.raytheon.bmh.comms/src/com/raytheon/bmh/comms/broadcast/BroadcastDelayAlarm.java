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
import com.raytheon.uf.common.bmh.notify.MessageDelayedBroadcastNotification;

/**
 * Used to build a log message using a {@link MessageBroadcastNotifcation} that
 * will be sent to AlertViz.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 19, 2015 4002       bkowal      Initial creation
 * Jun 01, 2015 4490       bkowal      Extend {@link AbstractJmsAlarm}.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class BroadcastDelayAlarm extends AbstractJmsAlarm {

    /**
     * Constructor.
     */
    public BroadcastDelayAlarm() {
        super(BMH_CATEGORY.DAC_TRANSMIT_BROADCAST_DELAY);
    }

    private static final String CLASSIFY_INTERRUPT = "Interrupt";

    private static final String CLASSIFY_WARNING = "Warning";

    public void notify(final MessageDelayedBroadcastNotification notification) {
        final String msgClassification = notification.isInterrupt() ? CLASSIFY_INTERRUPT
                : CLASSIFY_WARNING;

        /*
         * Submit the notification message.
         */
        logger.error(
                "The broadcast of {} message {} on Transmitter {} will be delayed due to an active Broadcast Live Session (Message Expires: {}).",
                msgClassification, notification.getMessageIdentification(),
                notification.getTransmitterGroup(), notification.getExpire());
    }
}