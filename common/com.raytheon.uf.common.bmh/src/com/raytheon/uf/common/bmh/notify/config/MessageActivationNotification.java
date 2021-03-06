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

import java.util.ArrayList;
import java.util.List;

import com.raytheon.uf.common.bmh.datamodel.msg.InputMessage;
import com.raytheon.uf.common.bmh.trace.ITraceable;
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
 * Apr 20, 2015  4397     bkowal      Added {@link #expireRequestTime}.
 * May 28, 2015  4429     rjpeter     Update for ITraceable
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
@DynamicSerialize
public class MessageActivationNotification extends ConfigNotification {

    @DynamicSerializeElement
    private List<Integer> inputMessageIds;

    @DynamicSerializeElement
    private Long expireRequestTime;

    public MessageActivationNotification() {

    }

    public MessageActivationNotification(InputMessage inputMessage,
            Long expireRequestTime, ITraceable traceable) {
        super(getType(inputMessage.getActive()), traceable);
        this.addInputMessageId(inputMessage.getId());
        this.expireRequestTime = expireRequestTime;
    }

    public MessageActivationNotification(List<InputMessage> inputMessages,
            boolean active, ITraceable traceable) {
        super(getType(active), traceable);
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

    /**
     * @return the expireRequestTime
     */
    public Long getExpireRequestTime() {
        return expireRequestTime;
    }

    /**
     * @param expireRequestTime
     *            the expireRequestTime to set
     */
    public void setExpireRequestTime(Long expireRequestTime) {
        this.expireRequestTime = expireRequestTime;
    }

}
