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
package com.raytheon.uf.common.bmh.broadcast;

import java.util.List;

import com.raytheon.uf.common.bmh.datamodel.msg.InputMessage;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.bmh.request.AbstractBMHServerRequest;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Used to submit user-generated weather messages for broadcast.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 23, 2014  #3748     bkowal      Initial creation
 * Oct 31, 2014  #3778     bsteffen    Do not clear the id when editing messages.
 * Nov 21, 2014  #3385     bkowal      Transmitter selection is valid in the case of
 *                                     Weather Msgs and Emergency Override
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */
@DynamicSerialize
public class NewBroadcastMsgRequest extends AbstractBMHServerRequest {

    @DynamicSerializeElement
    private InputMessage inputMessage;

    @DynamicSerializeElement
    private byte[] messageAudio;

    @DynamicSerializeElement
    private List<Transmitter> selectedTransmitters;

    /**
	 * 
	 */
    public NewBroadcastMsgRequest() {
    }

    /**
     * @return the inputMessage
     */
    public InputMessage getInputMessage() {
        return inputMessage;
    }

    /**
     * @param inputMessage
     *            the inputMessage to set
     */
    public void setInputMessage(InputMessage inputMessage) {
        this.inputMessage = inputMessage;
    }

    /**
     * @return the messageAudio
     */
    public byte[] getMessageAudio() {
        return messageAudio;
    }

    /**
     * @param messageAudio
     *            the messageAudio to set
     */
    public void setMessageAudio(byte[] messageAudio) {
        this.messageAudio = messageAudio;
    }

    /**
     * @return the selectedTransmitters
     */
    public List<Transmitter> getSelectedTransmitters() {
        return selectedTransmitters;
    }

    /**
     * @param selectedTransmitters
     *            the selectedTransmitters to set
     */
    public void setSelectedTransmitters(List<Transmitter> selectedTransmitters) {
        this.selectedTransmitters = selectedTransmitters;
    }
}