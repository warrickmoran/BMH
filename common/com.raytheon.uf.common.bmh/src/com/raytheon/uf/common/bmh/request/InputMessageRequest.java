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

import com.raytheon.uf.common.bmh.datamodel.msg.InputMessage;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.time.util.TimeUtil;

/**
 * Request object for {@link InputMessage} queries.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 16, 2014  3728      lvenable     Initial creation
 * Nov 03, 2014  3790      lvenable     Changed enum name.
 * Jan 02, 2014  3833      lvenable     Added unexpired action and time variable.
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */
@DynamicSerialize
public class InputMessageRequest extends AbstractBMHServerRequest {

    public enum InputMessageAction {
        AllInputMessages, ListIdNameAfosCreationActive, UnexpiredMessages, GetByPkId;
    }

    @DynamicSerializeElement
    private InputMessageAction action;

    @DynamicSerializeElement
    private InputMessage inputMessage;

    @DynamicSerializeElement
    private int pkId;

    @DynamicSerializeElement
    private Calendar time = TimeUtil.newGmtCalendar();

    /**
     * Get the input message action.
     * 
     * @return Input message action.
     */
    public InputMessageAction getAction() {
        return action;
    }

    /**
     * Set the input message action.
     * 
     * @param action
     *            Input message action.
     */
    public void setAction(InputMessageAction action) {
        this.action = action;
    }

    /**
     * Get the input message
     * 
     * @return The input message.
     */
    public InputMessage getInputMessage() {
        return inputMessage;
    }

    /**
     * Set the input message.
     * 
     * @param inputMessage
     *            The input message.
     */
    public void setInputMessage(InputMessage inputMessage) {
        this.inputMessage = inputMessage;
    }

    /**
     * Get the primary key ID.
     * 
     * @return the pkId
     */
    public int getPkId() {
        return pkId;
    }

    /**
     * Set the primary key ID.
     * 
     * @param pkId
     *            the pkId to set
     */
    public void setPkId(int pkId) {
        this.pkId = pkId;
    }

    public Calendar getTime() {
        return time;
    }

    public void setTime(Calendar time) {
        this.time = time;
    }
}
