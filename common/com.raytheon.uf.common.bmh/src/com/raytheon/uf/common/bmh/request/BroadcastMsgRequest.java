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
package com.raytheon.uf.common.bmh.request;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

/**
 * Broadcast message request object.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 15, 2014    3432    mpduff      Initial creation
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */
@DynamicSerialize
public class BroadcastMsgRequest implements IServerRequest {
    public enum BroadcastMessageAction {
        GET_MESSAGE_BY_ID
    }

    @DynamicSerializeElement
    private BroadcastMessageAction action;

    @DynamicSerializeElement
    private Long broadcastMessageId;

    /**
     * @return the broadcastMessageId
     */
    public Long getBroadcastMessageId() {
        return broadcastMessageId;
    }

    /**
     * @param broadcastMessageId
     *            the broadcastMessageId to set
     */
    public void setBroadcastMessageId(Long broadcastMessageId) {
        this.broadcastMessageId = broadcastMessageId;
    }

    /**
     * @return the action
     */
    public BroadcastMessageAction getAction() {
        return action;
    }

    /**
     * @param action
     *            the action to set
     */
    public void setAction(BroadcastMessageAction action) {
        this.action = action;
    }
}
