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

import com.raytheon.uf.common.bmh.notify.config.ConfigNotification;
import com.raytheon.uf.common.serialization.SerializationException;
import com.raytheon.uf.common.serialization.SerializationUtil;
import com.raytheon.uf.edex.core.EDEXUtil;
import com.raytheon.uf.edex.core.EdexException;

/**
 * 
 * Provides static methods for broadcasting messages from bmh.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#  Engineer    Description
 * ------------ -------- ----------- --------------------------
 * Oct 8, 2014  3687     bsteffen    Initial creation
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class BmhMessageProducer {

    private static final String OPERATIONAL_CONFIG_URI = "jms-generic:topic:BMH.Config";

    private static final String PRACTICE_CONFIG_URI = "jms-generic:topic:BMH.Practice.Config";

    public static void sendConfigMessage(ConfigNotification notification,
            boolean operational)
            throws EdexException, SerializationException {
        if (notification == null) {
            return;
        }
        String uri = operational ? OPERATIONAL_CONFIG_URI : PRACTICE_CONFIG_URI;
        EDEXUtil.getMessageProducer().sendAsyncUri(uri,
                SerializationUtil.transformToThrift(notification));
    }
}
