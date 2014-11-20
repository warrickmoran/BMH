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
package com.raytheon.uf.edex.bmh;

import com.raytheon.uf.common.bmh.jms.AbstractBMHJMSDestinations;
import com.raytheon.uf.common.bmh.request.AbstractBMHServerRequest;

/**
 * JMS destination provider for BMH EDEX components.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 18, 2014 3807       bkowal      Initial creation
 * Nov 19, 2014 3817       bsteffen    Use status queue for more than just dacs.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class BMHJmsDestinations extends AbstractBMHJMSDestinations {

    private static final String JMS_DURABLE_TOPIC = "jms-durable:topic:";

    private static final String JMS_GENERIC_TOPIC = "jms-generic:topic:";

    private static final String JMS_DURABLE_QUEUE = "jms-durable:queue:";

    private static final String JMS_GENERIC_QUEUE = "jms-generic:queue:";

    private static final String jmsFormatStr = "%s%s";

    protected static final BMHJmsDestinations instance = new BMHJmsDestinations();

    /**
     * 
     */
    public BMHJmsDestinations() {
    }

    private static String getJmsTopic(final boolean operational) {
        return (operational) ? JMS_DURABLE_TOPIC : JMS_GENERIC_TOPIC;
    }

    private static String getJmsQueue(final boolean operational) {
        return (operational) ? JMS_DURABLE_QUEUE : JMS_GENERIC_QUEUE;
    }

    public static String getStatusDestination(final boolean operational) {
        return String.format(jmsFormatStr, getJmsTopic(operational),
                instance.getStatusURI(operational));
    }

    public static String getStatusDestination(
            final AbstractBMHServerRequest request) {
        return getStatusDestination(request.isOperational());
    }

    public static String getBMHConfigDestination(final boolean operational) {
        return String.format(jmsFormatStr, getJmsTopic(operational),
                instance.getBMHConfigURI(operational));
    }

    public static String getBMHConfigDestination(
            final AbstractBMHServerRequest request) {
        return getBMHConfigDestination(request.isOperational());
    }

    public static String getBMHTransformDestination(final boolean operational) {
        return String.format(jmsFormatStr, getJmsQueue(operational),
                instance.getBMHTransformURI(operational));
    }

    public static String getBMHTransformDestination(
            final AbstractBMHServerRequest request) {
        return getBMHTransformDestination(request.isOperational());
    }

    public static String getBMHTTSDestination(final boolean operational) {
        return String.format(jmsFormatStr, getJmsQueue(operational),
                instance.getBMHTTSURI(operational));
    }

    public static String getBMHTTSDestination(
            final AbstractBMHServerRequest request) {
        return getBMHTTSDestination(request.isOperational());
    }

    public static String getBMHScheduleDestination(final boolean operational) {
        return String.format(jmsFormatStr, getJmsQueue(operational),
                instance.getBMHScheduleURI(operational));
    }

    public static String getBMHScheduleDestination(
            final AbstractBMHServerRequest request) {
        return getBMHScheduleDestination(request.isOperational());
    }
}