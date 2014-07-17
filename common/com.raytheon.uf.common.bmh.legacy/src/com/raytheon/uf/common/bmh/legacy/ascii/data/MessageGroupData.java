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
package com.raytheon.uf.common.bmh.legacy.ascii.data;

import java.util.ArrayList;
import java.util.List;

import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.util.StringUtil;

/**
 * Container for message group data from legacy system. No correlation in BMH
 * and data will have to be expanded in to individual messages each time the
 * group is referenced.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 17, 2014 3175       rjpeter     Initial creation
 * 
 * </pre>
 * 
 * @author rjpeter
 * @version 1.0
 */
public class MessageGroupData {
    // BLOCK 14
    private String name;

    private List<MessageType> messageTypes = new ArrayList<>();

    // unused??
    private List<Transmitter> sameTransmitters;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<MessageType> getMessageTypes() {
        return messageTypes;
    }

    public void setMessageTypes(List<MessageType> messageTypes) {
        this.messageTypes = messageTypes;
    }

    public void addMessageType(MessageType msgType) {
        if (msgType != null) {
            messageTypes.add(msgType);
        }
    }

    public List<Transmitter> getSameTransmitterMnemonics() {
        if (sameTransmitters == null) {
            sameTransmitters = new ArrayList<>();
        }

        return sameTransmitters;
    }

    public void setSameTransmitterMnemonics(
            List<Transmitter> sameTransmitterMnemonics) {
        this.sameTransmitters = sameTransmitterMnemonics;
    }

    public void addSameTransmitter(Transmitter transmitter) {
        if (transmitter != null) {
            if (sameTransmitters == null) {
                sameTransmitters = new ArrayList<>();
            }

            sameTransmitters.add(transmitter);
        }
    }

    @Override
    public String toString() {
        return "MessageGroupData [name=" + name + ", messageTypes="
                + StringUtil.join(messageTypes, ',')
                + ", sameTransmitterMnemonics="
                + StringUtil.join(sameTransmitters, ',') + "]";
    }
}
