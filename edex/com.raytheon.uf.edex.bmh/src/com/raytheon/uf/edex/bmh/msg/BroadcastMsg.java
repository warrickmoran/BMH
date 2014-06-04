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
package com.raytheon.uf.edex.bmh.msg;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * The message generated by the Message Transformer component for the TTS
 * Manager. The purpose is to ensure that TTS Manager has all of the information
 * it requires in order to perform the Text to Speech conversion, without
 * reference to the database or any other entity. Consists of a header and a
 * body, representing a single message to be sent to a transmitter.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 2, 2014  3228       bkowal     Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

@DynamicSerialize
public class BroadcastMsg {
    /*
     * The unique id associated with this Broadcast Message in the database.
     */
    // TODO: may need to adjust data type.
    @DynamicSerializeElement
    private String id;

    @DynamicSerializeElement
    private BroadcastMsgHeader header;

    @DynamicSerializeElement
    private BroadcastMsgBody body;

    /**
     * 
     */
    public BroadcastMsg(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public BroadcastMsgHeader getHeader() {
        return header;
    }

    public void setHeader(BroadcastMsgHeader header) {
        this.header = header;
    }

    public BroadcastMsgBody getBody() {
        return body;
    }

    public void setBody(BroadcastMsgBody body) {
        this.body = body;
    }
}