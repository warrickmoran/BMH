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

import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessage;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Allows non-EDEX components to send message broadcast delayed notifications to
 * AlertViz. This notification will be used in three scenarios:
 * 
 * 1) An interrupt associated with any message type is delayed. 2) A trigger
 * message that is associated with a warning message type is delayed. 3) A
 * non-trigger message that is part of the currently playing suite and it
 * associated with a warning message type is delayed.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 19, 2015 4002       bkowal      Initial creation
 * Mar 03, 2015 4002       bkowal      Added {@link #toString()}.
 * May 11, 2015 4002       bkowal      Improved JavaDoc.
 * Jun 01, 2015 4490       bkowal      Extend {@link AbstractAlarmableMessageNotification}.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */
@DynamicSerialize
public class MessageDelayedBroadcastNotification extends
        AbstractAlarmableMessageNotification {

    /**
     * boolean flag indicating whether or not this notification has been sent
     * for an interrupt message that has been delayed.
     */
    @DynamicSerializeElement
    private boolean interrupt;

    /**
     * Constructor
     * 
     * Empty constructor for {@link DynamicSerialize}.
     */
    public MessageDelayedBroadcastNotification() {
        super();
    }

    public MessageDelayedBroadcastNotification(DacPlaylistMessage message,
            boolean interrupt) {
        super(message);
        this.interrupt = interrupt;
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(
                "MessageDelayedBroadcastNotification [broadcastId=");
        sb.append(super.getBroadcastId());
        sb.append(", interrupt=");
        sb.append(this.interrupt);
        sb.append(", name=");
        sb.append(super.getName());
        sb.append(", messageType=");
        sb.append(super.getMessageType());
        if (super.getExpire() != null) {
            sb.append(", expire=");
            sb.append(super.getExpire());
        }
        sb.append(", transmitterGroup=");
        sb.append(super.getTransmitterGroup());
        sb.append("]");

        return sb.toString();
    }
}