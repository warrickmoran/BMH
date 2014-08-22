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

import java.io.Serializable;

import javax.persistence.Embeddable;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Id field for a MessageTypeReplacement
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
@Embeddable
@DynamicSerialize
public class MessageTypeReplacementPK implements Serializable {
    private static final long serialVersionUID = 1L;

    @DynamicSerializeElement
    private int msgId;

    @DynamicSerializeElement
    private int replaceId;

    /**
     * @return the msgId
     */
    public int getMsgId() {
        return msgId;
    }

    /**
     * @param msgId
     *            the msgId to set
     */
    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }

    /**
     * @return the replaceId
     */
    public int getReplaceId() {
        return replaceId;
    }

    /**
     * @param replaceId
     *            the replaceId to set
     */
    public void setReplaceId(int replaceId) {
        this.replaceId = replaceId;
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
        result = prime * result + msgId;
        result = prime * result + replaceId;
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
        MessageTypeReplacementPK other = (MessageTypeReplacementPK) obj;
        if (msgId != other.msgId) {
            return false;
        }
        if (replaceId != other.replaceId) {
            return false;
        }
        return true;
    }
}
