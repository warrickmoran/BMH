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
import com.raytheon.uf.common.bmh.notify.MessageNotBroadcastNotification;

/**
 * Alarm for a {@link MessageNotBroadcastNotification}.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 1, 2015  4490       bkowal     Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class WtchOrWrnNotBroadcastAlarm extends AbstractJmsAlarm {

    /**
     * Constructor.
     */
    public WtchOrWrnNotBroadcastAlarm() {
        super(BMH_CATEGORY.WTCH_OR_WRN_NOT_BROADCAST);
    }

    /**
     * Triggers an alarm for the specified
     * {@link MessageNotBroadcastNotification}.
     * 
     * @param notification
     *            the specified {@link MessageNotBroadcastNotification}
     */
    public void notify(final MessageNotBroadcastNotification notification) {
        logger.error(
                "{} message {} expired before it could be broadcast on Transmitter {} (Message Expires: {}).",
                notification.getDesignation(),
                notification.getMessageIdentification(),
                notification.getTransmitterGroup(), notification.getExpire());
    }
}