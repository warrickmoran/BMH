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

import com.raytheon.uf.common.bmh.broadcast.AbstractLiveBroadcastMessage;
import com.raytheon.uf.common.bmh.broadcast.OnDemandBroadcastConstants.MSGSOURCE;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;

/**
 * Used to notify {@link ClusteredBroadcastStreamTask}s that they can transition
 * to the next phase.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 26, 2014 3797       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */
@DynamicSerialize
public class ClusteredBroadcastTransitionTrigger extends
        AbstractLiveBroadcastMessage {

    /*
     * only exists for dynamicserialize
     */
    public ClusteredBroadcastTransitionTrigger() {
    }

    /**
     * This is the constructor that should be used.
     * 
     * @param broadcastId
     *            the id of the broadcast to transition
     */
    public ClusteredBroadcastTransitionTrigger(final String broadcastId) {
        this.setMsgSource(MSGSOURCE.COMMS);
        this.setBroadcastId(broadcastId);
    }
}