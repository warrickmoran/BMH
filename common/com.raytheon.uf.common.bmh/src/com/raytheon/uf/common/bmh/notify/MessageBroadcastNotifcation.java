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
package com.raytheon.uf.common.bmh.notify;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Allows non-edex components to send message broadcast notifications to
 * AlertViz.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 12, 2015 3968       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

@DynamicSerialize
public class MessageBroadcastNotifcation {

    public static enum TYPE {
        /* confirms the broadcast of a message. */
        CONFIRM,
        /*
         * used to notify a user that a scheduled broadcast message has not been
         * broadcast.
         */
        NOTPLAYED
    }

    @DynamicSerializeElement
    private TYPE type;

    @DynamicSerializeElement
    private long broadcastId;

    @DynamicSerializeElement
    private String transmitterGroup;

    /**
     * Empty constructor for dynamicserialize.
     */
    public MessageBroadcastNotifcation() {
    }

    /**
     * Constructor
     * 
     * @param type
     *            the type of broadcast notification
     * @param broadcastId
     *            id of the broadcast this notification is associated with
     */
    public MessageBroadcastNotifcation(TYPE type, long broadcastId) {
        this.type = type;
        this.broadcastId = broadcastId;
    }

    /**
     * @return the type
     */
    public TYPE getType() {
        return type;
    }

    /**
     * @param type
     *            the type to set
     */
    public void setType(TYPE type) {
        this.type = type;
    }

    /**
     * @return the broadcastId
     */
    public long getBroadcastId() {
        return broadcastId;
    }

    /**
     * @param broadcastId
     *            the broadcastId to set
     */
    public void setBroadcastId(long broadcastId) {
        this.broadcastId = broadcastId;
    }

    /**
     * @return the transmitterGroup
     */
    public String getTransmitterGroup() {
        return transmitterGroup;
    }

    /**
     * @param transmitterGroup
     *            the transmitterGroup to set
     */
    public void setTransmitterGroup(String transmitterGroup) {
        this.transmitterGroup = transmitterGroup;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("MessageBroadcastNotifcation [");
        sb.append("type=").append(this.type.toString());
        sb.append(", broadcastId=").append(this.broadcastId);
        sb.append(", transmitterGroup=").append(this.transmitterGroup)
                .append("]");

        return sb.toString();
    }
}