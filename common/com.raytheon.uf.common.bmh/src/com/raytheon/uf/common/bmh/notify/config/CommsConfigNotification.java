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
package com.raytheon.uf.common.bmh.notify.config;

import com.raytheon.uf.common.bmh.trace.ITraceable;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;

/**
 * Notification sent out when edex has written a new comms.xml.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Apr 06, 2015  4370     rjpeter     Initial creation
 * May 28, 2015  4429     rjpeter     Update for ITraceable
 * </pre>
 * 
 * @author rjpeter
 * @version 1.0
 */
@DynamicSerialize
public class CommsConfigNotification extends ConfigNotification {
    public CommsConfigNotification() {

    }

    public CommsConfigNotification(ITraceable traceable) {
        super(ConfigChangeType.Update, traceable);
    }

    public static String getTopicName(boolean operational) {
        if (operational) {
            return "BMH.Config";
        } else {
            return "BMH.Practice.Config";
        }
    }
}
