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
package com.raytheon.uf.common.bmh.broadcast;

import java.util.List;

import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg;
import com.raytheon.uf.common.bmh.request.AbstractBMHServerRequest;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Request used to request the expiration of a Broadcast Message on a specific
 * Transmitter.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 15, 2015 4293       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */
@DynamicSerialize
public class ExpireBroadcastMsgRequest extends AbstractBMHServerRequest {

    /*
     * The Broadcast Messages to forcefully expire.
     */
    @DynamicSerializeElement
    private List<BroadcastMsg> expiredBroadcastMsgs;

    /**
     * 
     */
    public ExpireBroadcastMsgRequest() {
    }

    public List<BroadcastMsg> getExpiredBroadcastMsgs() {
        return expiredBroadcastMsgs;
    }

    public void setExpiredBroadcastMsgs(List<BroadcastMsg> expiredBroadcastMsgs) {
        this.expiredBroadcastMsgs = expiredBroadcastMsgs;
    }
}