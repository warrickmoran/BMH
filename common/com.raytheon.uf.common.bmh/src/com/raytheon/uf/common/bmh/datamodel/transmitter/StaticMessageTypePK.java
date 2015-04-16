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
package com.raytheon.uf.common.bmh.datamodel.transmitter;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * The primary key definition for {@link StaticMessageType}s.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 9, 2015  4213       bkowal      Initial creation
 * Apr 15, 2015 4397       bkowal      Added {@link #hashCode()} and {@link #equals(Object)}.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */
@Embeddable
@DynamicSerialize
public class StaticMessageTypePK implements Serializable {

    private static final long serialVersionUID = 2029019848158417387L;

    @DynamicSerializeElement
    private TransmitterLanguagePK transmitterLanguagePK;

    @Column
    @DynamicSerializeElement
    private int msgTypeId;

    /**
     * @return the transmitterLanguagePK
     */
    public TransmitterLanguagePK getTransmitterLanguagePK() {
        return transmitterLanguagePK;
    }

    /**
     * @param transmitterLanguagePK
     *            the transmitterLanguagePK to set
     */
    public void setTransmitterLanguagePK(
            TransmitterLanguagePK transmitterLanguagePK) {
        this.transmitterLanguagePK = transmitterLanguagePK;
    }

    /**
     * @return the msgTypeId
     */
    public int getMsgTypeId() {
        return msgTypeId;
    }

    /**
     * @param msgTypeId
     *            the msgTypeId to set
     */
    public void setMsgTypeId(int msgTypeId) {
        this.msgTypeId = msgTypeId;
    }

    /**
     * @return the serialversionuid
     */
    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("StaticMessageTypePK [");
        sb.append("transmitterLanguagePK=").append(
                this.transmitterLanguagePK.toString());
        sb.append(", msgTypeId=").append(this.msgTypeId).append("]");

        return sb.toString();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + msgTypeId;
        result = prime
                * result
                + ((transmitterLanguagePK == null) ? 0 : transmitterLanguagePK
                        .hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        StaticMessageTypePK other = (StaticMessageTypePK) obj;
        if (msgTypeId != other.msgTypeId)
            return false;
        if (transmitterLanguagePK == null) {
            if (other.transmitterLanguagePK != null)
                return false;
        } else if (!transmitterLanguagePK.equals(other.transmitterLanguagePK))
            return false;
        return true;
    }
}