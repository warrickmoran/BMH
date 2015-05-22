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
package com.raytheon.uf.common.bmh.datamodel.msg;

import java.util.List;

import com.raytheon.uf.common.bmh.trace.ITraceable;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * 
 * A grouping of broadcast messages with the same input message. Created for use
 * in camel routing so that all broadcast messages for a specific input messages
 * are scheduled at the same time. The serialized form(sent over JMS) of a group
 * contains only ids and loading the messages is the responsibility of whoever
 * deserializes it.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Mar 25, 2015  4290     bsteffen    Switch to global replacement.
 * May 08, 2015  4429     rferrel     Implement {@link ITraceable}.
 * May 21, 2015  4429     rjpeter     Updated constructor.
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
@DynamicSerialize
public class BroadcastMsgGroup implements ITraceable {

    private List<BroadcastMsg> messages;

    @DynamicSerializeElement
    private List<Long> ids;

    @DynamicSerializeElement
    private String traceId;

    public BroadcastMsgGroup() {

    }

    public BroadcastMsgGroup(String traceId, List<BroadcastMsg> messages) {
        this.traceId = traceId;
        this.messages = messages;
    }

    public List<BroadcastMsg> getMessages() {
        return messages;
    }

    public void setMessages(List<BroadcastMsg> messages) {
        this.messages = messages;
    }

    public List<Long> getIds() {
        return ids;
    }

    public void setIds(List<Long> ids) {
        this.ids = ids;
    }

    public boolean isSuccess() {
        for (BroadcastMsg message : messages) {
            if (!message.isSuccess()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String getTraceId() {
        return traceId;
    }

    @Override
    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }
}
