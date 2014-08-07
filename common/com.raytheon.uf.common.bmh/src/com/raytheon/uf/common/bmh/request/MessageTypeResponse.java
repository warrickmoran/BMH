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

import java.util.List;

import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Message Type data response object
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 22, 2014    3411    mpduff      Initial creation
 * Aug 5, 2014  #3490      lvenable    Added code.
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */
@DynamicSerialize
public class MessageTypeResponse {

    @DynamicSerializeElement
    private List<MessageType> messageTypeList;

    /**
     * Get the message type list.
     * 
     * @return List of message types.
     */
    public List<MessageType> getMessageTypeList() {
        return messageTypeList;
    }

    /**
     * Set the message type list.
     * 
     * @param messageTypeList
     *            The message type list.
     */
    public void setMessageTypeList(List<MessageType> messageTypeList) {
        this.messageTypeList = messageTypeList;
    }
}
