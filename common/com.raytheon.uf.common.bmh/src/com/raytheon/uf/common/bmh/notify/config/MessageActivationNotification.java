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
package com.raytheon.uf.common.bmh.notify.config;

import com.raytheon.uf.common.bmh.datamodel.msg.InputMessage;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * 
 * Notification sent out when a message is activated or deactivated.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket   Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Oct 31, 2014  3778     bsteffen    Initial creation
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
@DynamicSerialize
public class MessageActivationNotification extends ConfigNotification {

    @DynamicSerializeElement
    private int inputMessageId;

    public MessageActivationNotification() {

    }

    public MessageActivationNotification(InputMessage inputMessage) {
        super(getType(inputMessage.getActive()));
        this.inputMessageId = inputMessage.getId();
    }

    public int getInputMessageId() {
        return inputMessageId;
    }

    public void setInputMessageId(int inputMessageId) {
        this.inputMessageId = inputMessageId;
    }

    private static ConfigChangeType getType(Boolean active) {
        if (Boolean.FALSE.equals(active)) {
            return ConfigChangeType.Delete;
        } else {
            return ConfigChangeType.Update;
        }
    }

}
