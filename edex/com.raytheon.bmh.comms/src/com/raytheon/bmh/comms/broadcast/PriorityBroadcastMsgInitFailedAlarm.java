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

import com.raytheon.uf.common.bmh.BMH_CATEGORY;

/**
 * Used to construct a log message that will be transmitted to AlertViz to
 * inform the user that a specific HIGH PRIORITY message could not be
 * initialized for broadcast. HIGH PRIORITY messages currently include:
 * interrupts, watches, and warnings. This alarm differs from the
 * {@link WtchOrWrnNotBroadcastAlarm} in that this alarm indicates that the
 * message could not even be loaded for an attempted broadcast whereas the other
 * indicates that the message had been successfully loaded, but it expired
 * before it could be broadcast.
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

public class PriorityBroadcastMsgInitFailedAlarm extends
        BroadcastMsgInitFailedAlarm {

    public PriorityBroadcastMsgInitFailedAlarm() {
        super(BMH_CATEGORY.PRIORITY_MSG_RETRIEVAL_FAILED);
    }
}