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
import com.raytheon.uf.common.bmh.datamodel.msg.InputMessage;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;

/**
 * SPECIAL CASE: Used to notify the users when a watch or warning fails message
 * validation because it has expired indicating that it will never be broadcast
 * to the desired transmitters. The message never makes it into a playlist so,
 * the {@link MessageBroadcastNotifcation} cannot be used for this scenario
 * because a broadcast message is never generated.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 14, 2015 3969       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */
@DynamicSerialize
public class MessageExpiredNotification {

    /**
     * The id of the associated {@link InputMessage}.
     */
    @DynamicSerializeElement
    private int id;

    /**
     * The afosid of the associated {@link InputMessage}.
     */
    @DynamicSerializeElement
    private String afosid;

    /**
     * The name of the associated {@link InputMessage}.
     */
    @DynamicSerializeElement
    private String name;

    /**
     * The {@link String} representation of the designation associated with the
     * {@link InputMessage} {@link MessageType}.
     */
    @DynamicSerializeElement
    private String designation;

    /**
     * Constructor
     * 
     * Empty constructor for {@link DynamicSerialize} compatibility.
     */
    public MessageExpiredNotification() {
    }

    /**
     * Constructor
     * 
     * @param msg
     *            the {@link InputMessage} that this notification is being
     *            generated for.
     */
    public MessageExpiredNotification(final InputMessage msg,
            final String designation) {
        this.id = msg.getId();
        this.afosid = msg.getAfosid();
        this.name = msg.getName();
        this.designation = designation;
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the afosid
     */
    public String getAfosid() {
        return afosid;
    }

    /**
     * @param afosid
     *            the afosid to set
     */
    public void setAfosid(String afosid) {
        this.afosid = afosid;
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
        StringBuilder sb = new StringBuilder("MessageExpiredNotification [");
        sb.append("id=").append(this.id);
        sb.append(", afosid=").append(this.afosid);
        sb.append(", name=").append(this.name);
        sb.append(", designation=").append(this.designation).append("]");

        return sb.toString();
    }
}