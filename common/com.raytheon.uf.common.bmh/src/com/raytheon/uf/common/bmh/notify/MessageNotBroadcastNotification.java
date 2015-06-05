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

import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessage;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Currently used to notify a user that a watch or warning that was added to a
 * playlist expired before it could actually be broadcast for the first time.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 14, 2015 3969       bkowal      Initial creation
 * Jun 01, 2015 4490       bkowal      Extend {@link AbstractAlarmableMessageNotification}.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */
@DynamicSerialize
public class MessageNotBroadcastNotification extends
        AbstractAlarmableMessageNotification {

    @DynamicSerializeElement
    private String designation;

    /**
     * Constructor
     * 
     * Empty constructor for {@link DynamicSerialize}.
     */
    public MessageNotBroadcastNotification() {
        super();
    }

    public MessageNotBroadcastNotification(DacPlaylistMessage message) {
        super(message);
        this.designation = message.isWarning() ? MessageType.Designation.Warning
                .name() : MessageType.Designation.Watch.name();
    }

    /**
     * @return the designation
     */
    public String getDesignation() {
        return designation;
    }

    /**
     * @param designation
     *            the designation to set
     */
    public void setDesignation(String designation) {
        this.designation = designation;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(
                "MessageNotBroadcastNotification [");
        sb.append("broadcastId=").append(super.getBroadcastId());
        sb.append(", designation=").append(this.designation);
        sb.append(", transmitterGroup=").append(super.getTransmitterGroup())
                .append("]");

        return sb.toString();
    }
}