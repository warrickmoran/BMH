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

import org.apache.commons.lang3.StringUtils;

import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg;
import com.raytheon.uf.common.bmh.datamodel.msg.InputMessage;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessage;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Abstract representation identifying a {@link DacPlaylistMessage} that
 * triggered an alarmable notification.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 1, 2015  4490       bkowal      Initial creation
 * Feb 09, 2016 5082       bkowal      Updates for Apache commons lang 3.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

@DynamicSerialize
public abstract class AbstractAlarmableMessageNotification {

    /**
     * The id of the {@link BroadcastMsg}
     */
    @DynamicSerializeElement
    private long broadcastId;

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
     * Empty constructor for {@link DynamicSerialize}.
     */
    public AbstractAlarmableMessageNotification() {
    }

    /**
     * Constructor.
     * 
     * @param message
     *            the {@link DacPlaylistMessage} this notification is being
     *            created for.
     */
    public AbstractAlarmableMessageNotification(DacPlaylistMessage message) {
        this.broadcastId = message.getBroadcastId();
        this.name = message.getName();
        this.messageType = message.getMessageType();
        this.expire = (message.getExpire() == null) ? StringUtils.EMPTY
                : message.getExpire().getTime().toString();
    }

    public String getMessageIdentification() {
        StringBuilder sb = new StringBuilder("[broadcast id=");
        sb.append(this.broadcastId);
        sb.append(", name=").append(this.name);
        sb.append(", messageType=").append(this.messageType).append("]");

        return sb.toString();
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
}