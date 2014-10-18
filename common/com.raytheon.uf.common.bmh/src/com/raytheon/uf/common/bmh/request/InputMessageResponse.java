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

import java.util.ArrayList;
import java.util.List;

import com.raytheon.uf.common.bmh.datamodel.msg.InputMessage;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Response object for {@link InputMessage} queries.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 16, 2014  #3728     lvenable     Initial creation
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */
@DynamicSerialize
public class InputMessageResponse {

    @DynamicSerializeElement
    private List<InputMessage> inputMessageList;

    /**
     * Get the input message list.
     * 
     * @return The input message list.
     */
    public List<InputMessage> getInputMessageList() {
        return inputMessageList;
    }

    /**
     * Set the input message list.
     * 
     * @param inputMessageList
     *            The input message list.
     */
    public void setInputMessageList(List<InputMessage> inputMessageList) {
        this.inputMessageList = inputMessageList;
    }

    /**
     * Add an input message to the input message list.
     * 
     * @param inputMessage
     *            Input message.
     */
    public void addInputMessage(InputMessage inputMessage) {
        if (inputMessageList == null) {
            inputMessageList = new ArrayList<InputMessage>(1);
        }
        inputMessageList.add(inputMessage);
    }

}
