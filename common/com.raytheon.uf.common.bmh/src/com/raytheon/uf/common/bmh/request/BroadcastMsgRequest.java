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

import java.util.Calendar;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.time.util.TimeUtil;

/**
 * Broadcast message request object.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Aug 15, 2014  3432     mpduff      Initial creation
 * Oct 07, 2014  3687     bsteffen    Extend AbstractBMHServerRequest
 * Dec 18, 2014  3865     bsteffen    Add GET_MESSAGE_BY_INPUT_ID Action
 * May 04, 2015  4449     bkowal      Added {@link BroadcastMessageAction#GET_ACTIVE_PLAYLISTS_WITH_MESSAGE},
 *                                    and {@link #time}, and {@link #transmitterGroup}.
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */
@DynamicSerialize
public class BroadcastMsgRequest extends AbstractBMHServerRequest {
    public enum BroadcastMessageAction {
        GET_MESSAGE_BY_ID, GET_MESSAGE_BY_INPUT_ID, GET_ACTIVE_PLAYLISTS_WITH_MESSAGE
    }

    @DynamicSerializeElement
    private BroadcastMessageAction action;

    @DynamicSerializeElement
    private Long messageId;

    @DynamicSerializeElement
    private Calendar time = TimeUtil.newGmtCalendar();

    @DynamicSerializeElement
    private String transmitterGroup;

    /**
     * @return the messageId
     */
    public Long getMessageId() {
        return messageId;
    }

    /**
     * @param messageId
     *            the messageId to set
     */
    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    /**
     * @return the action
     */
    public BroadcastMessageAction getAction() {
        return action;
    }

    /**
     * @param action
     *            the action to set
     */
    public void setAction(BroadcastMessageAction action) {
        this.action = action;
    }

    /**
     * @return the time
     */
    public Calendar getTime() {
        return time;
    }

    /**
     * @param time
     *            the time to set
     */
    public void setTime(Calendar time) {
        this.time = time;
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
