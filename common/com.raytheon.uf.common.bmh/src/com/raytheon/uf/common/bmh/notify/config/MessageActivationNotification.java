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

import java.util.List;
import java.util.ArrayList;

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
 * Jan 19, 2015  4011     bkowal      Support de-activating/activating
 *                                    multiple messages at once.
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
@DynamicSerialize
public class MessageActivationNotification extends ConfigNotification {

    @DynamicSerializeElement
    private List<Integer> inputMessageIds;

    public MessageActivationNotification() {

    }

    public MessageActivationNotification(InputMessage inputMessage) {
        super(getType(inputMessage.getActive()));
        this.addInputMessageId(inputMessage.getId());
    }

    public MessageActivationNotification(List<InputMessage> inputMessages,
            boolean active) {
        super(getType(active));
        for (InputMessage im : inputMessages) {
            this.addInputMessageId(im.getId());
        }
    }

    public void addInputMessageId(int id) {
        if (this.inputMessageIds == null) {
            this.inputMessageIds = new ArrayList<>();
        }
        this.inputMessageIds.add(id);
    }

    private static ConfigChangeType getType(Boolean active) {
        if (Boolean.FALSE.equals(active)) {
            return ConfigChangeType.Delete;
        } else {
            return ConfigChangeType.Update;
        }
    }

    /**
     * @return the inputMessageIds
     */
    public List<Integer> getInputMessageIds() {
        return inputMessageIds;
    }

    /**
     * @param inputMessageIds
     *            the inputMessageIds to set
     */
    public void setInputMessageIds(List<Integer> inputMessageIds) {
        this.inputMessageIds = inputMessageIds;
    }

}
