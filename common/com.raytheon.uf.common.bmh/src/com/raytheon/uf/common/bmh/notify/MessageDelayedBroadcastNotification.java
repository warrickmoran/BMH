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

import com.raytheon.uf.common.bmh.datamodel.msg.InputMessage;
import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Allows non-EDEX components to send message broadcast delayed notifications to
 * AlertViz.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 19, 2015 4002       bkowal      Initial creation
 * Mar 03, 2015 4002       bkowal      Added {@link #toString()}.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */
@DynamicSerialize
public class MessageDelayedBroadcastNotification {

    /**
     * The id of the {@link BroadcastMsg}
     */
    @DynamicSerializeElement
    private long broadcastId;

    /**
     * boolean flag indicating whether or not this notification has been sent
     * for an interrupt message that has been delayed.
     */
    @DynamicSerializeElement
    private boolean interrupt;

    /**
     * the name assigned to the source {@link InputMessage} that the delayed
     * {@link BroadcastMsg} has been generated for.
     */
    @DynamicSerializeElement
    private String name;

    /**
     * the afosid associated with the message.
     */
    @DynamicSerializeElement
    private String messageType;

    /**
     * the formatted date/time that the message expires.
     */
    @DynamicSerializeElement
    private String expire;

    /**
     * The transmitter group that this broadcast message is destined for.
     */
    @DynamicSerializeElement
    private String transmitterGroup;

    /**
     * Constructor
     * 
     * Empty constructor for {@link DynamicSerialize}.
     */
    public MessageDelayedBroadcastNotification() {
    }

    public MessageDelayedBroadcastNotification(long broadcastId,
            boolean interrupt, String name, String messageType, String expire) {
        this.broadcastId = broadcastId;
        this.interrupt = interrupt;
        this.name = name;
        this.messageType = messageType;
        this.expire = expire;
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
     * @return the interrupt
     */
    public boolean isInterrupt() {
        return interrupt;
    }

    /**
     * @param interrupt
     *            the interrupt to set
     */
    public void setInterrupt(boolean interrupt) {
        this.interrupt = interrupt;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the messageType
     */
    public String getMessageType() {
        return messageType;
    }

    /**
     * @param messageType
     *            the messageType to set
     */
    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    /**
     * @return the expire
     */
    public String getExpire() {
        return expire;
    }

    /**
     * @param expire
     *            the expire to set
     */
    public void setExpire(String expire) {
        this.expire = expire;
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
        StringBuilder sb = new StringBuilder(
                "MessageDelayedBroadcastNotification [broadcastId=");
        sb.append(this.broadcastId);
        sb.append(", interrupt=");
        sb.append(this.interrupt);
        sb.append(", name=");
        sb.append(this.name);
        sb.append(", messageType=");
        sb.append(this.messageType);
        sb.append(", expire=");
        sb.append(this.expire);
        sb.append(", transmitterGroup=");
        sb.append(this.transmitterGroup);
        sb.append("]");

        return sb.toString();
    }
}