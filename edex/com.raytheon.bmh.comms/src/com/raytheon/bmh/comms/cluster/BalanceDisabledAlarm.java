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
package com.raytheon.bmh.comms.cluster;

import java.util.Collection;

import com.raytheon.bmh.comms.AbstractJmsAlarm;
import com.raytheon.uf.common.bmh.BMH_CATEGORY;
import com.raytheon.uf.common.time.util.TimeUtil;

/**
 * {@link AbstractJmsAlarm} used to alert users that load balancing has been
 * disabled on a comms manager.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 30, 2016 5419       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class BalanceDisabledAlarm extends AbstractJmsAlarm {

    private final String host;

    public BalanceDisabledAlarm(final String host) {
        super(BMH_CATEGORY.COMMS_BALANCE_DISABLED);
        this.host = host;
    }

    public void alarm(final Collection<String> failedDacTransmits,
            final long resumeInterval) {
        if (failedDacTransmits == null || failedDacTransmits.isEmpty()) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String failed : failedDacTransmits) {
            if (first) {
                first = false;
            } else {
                sb.append(", ");
            }
            sb.append(failed);
        }
        logger.error(
                "Comms Manager load balancing has been disabled on: {}. {} was unable to connect to transmitter(s): {}. Please verify DACs and network connectivity. Load balancing will resume in {}.",
                this.host, this.host, sb.toString(),
                TimeUtil.prettyDuration(resumeInterval));
    }
}