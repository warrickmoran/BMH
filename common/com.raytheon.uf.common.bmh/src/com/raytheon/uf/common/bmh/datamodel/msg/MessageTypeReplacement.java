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

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

import org.hibernate.annotations.BatchSize;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Join object between a MessageType and a replacement MessageType
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 19, 2014    3411    mpduff      Initial creation
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */
@Entity
@DynamicSerialize
@Table(name = "message_replace", schema = "bmh")
@BatchSize(size = 100)
public class MessageTypeReplacement {

    @EmbeddedId
    @DynamicSerializeElement
    private MessageTypeReplacementPK id;

    @ManyToOne(optional = false)
    @MapsId("msgId")
    // No dynamic serialize due to bi-directional relationship
    private MessageType msgType;

    @ManyToOne(optional = false)
    @MapsId("replaceId")
    @DynamicSerializeElement
    private MessageType replaceMsgType;

    /**
     * @return the id
     */
    public MessageTypeReplacementPK getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(MessageTypeReplacementPK id) {
        this.id = id;
    }

    /**
     * @return the msgType
     */
    public MessageType getMsgType() {
        return msgType;
    }

    /**
     * @param msgType
     *            the msgType to set
     */
    public void setMsgType(MessageType msgType) {
        this.msgType = msgType;
        if (id == null) {
            id = new MessageTypeReplacementPK();
        }

        id.setMsgId(msgType != null ? msgType.getId() : 0);
    }

    /**
     * @return the replaceMsgType
     */
    public MessageType getReplaceMsgType() {
        return replaceMsgType;
    }

    /**
     * @param replaceMsgType
     *            the replaceMsgType to set
     */
    public void setReplaceMsgType(MessageType replaceMsgType) {
        this.replaceMsgType = replaceMsgType;
        if (id == null) {
            id = new MessageTypeReplacementPK();
        }

        id.setReplaceId(replaceMsgType != null ? replaceMsgType.getId() : 0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((msgType == null) ? 0 : msgType.hashCode());
        result = prime * result
                + ((replaceMsgType == null) ? 0 : replaceMsgType.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MessageTypeReplacement other = (MessageTypeReplacement) obj;
        if (msgType == null) {
            if (other.msgType != null) {
                return false;
            }
        } else if (!msgType.equals(other.msgType)) {
            return false;
        }
        if (replaceMsgType == null) {
            if (other.replaceMsgType != null) {
                return false;
            }
        } else if (!replaceMsgType.equals(other.replaceMsgType)) {
            return false;
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "MessageTypeReplacement [msgType=" + msgType
                + ", replaceMsgType=" + replaceMsgType + "]";
    }
}
