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

import javax.persistence.Column;
import javax.persistence.Embeddable;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Primary Key definition for {@link BroadcastContents}.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 2, 2015  4293       bkowal      Initial creation
 * Apr 15, 2015 4397       bkowal      Added {@link #hashCode()} and {@link #equals(Object)}.
 * Jul 28, 2016 5722       rjpeter     Handled null in compare.
 * </pre>
 * 
 * @author bkowal
 */
@Embeddable
@DynamicSerialize
public class BroadcastContentsPK implements Serializable,
        Comparable<BroadcastContentsPK> {

    private static final long serialVersionUID = -7230870958237668145L;

    @Column(nullable = false)
    @DynamicSerializeElement
    private long timestamp = System.currentTimeMillis();

    @Column(nullable = false)
    @DynamicSerializeElement
    private long broadcastId;

    /**
     * @return the timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * @param timestamp
     *            the timestamp to set
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * @return the broadcastId
     */
    public long getBroadcastId() {
        return broadcastId;
    }

    /**
     * @param broadcastId
     *            the broadcastId to set
     */
    public void setBroadcastId(long broadcastId) {
        this.broadcastId = broadcastId;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("BroadcastContentsPK [timestamp=");
        sb.append(this.timestamp).append(", broadcastId=")
                .append(this.broadcastId).append("]");

        return sb.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(BroadcastContentsPK o) {
        if (o == null) {
            return 1;
        }

        /*
         * Note: we want the most recent time to be first in the list.
         */
        return Long.compare(o.getTimestamp(), this.timestamp);
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
        result = prime * result + (int) (broadcastId ^ (broadcastId >>> 32));
        result = prime * result + (int) (timestamp ^ (timestamp >>> 32));
        return result;
    }

    /*
     * (non-Javadoc)
     * 
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
        BroadcastContentsPK other = (BroadcastContentsPK) obj;
        if (broadcastId != other.broadcastId)
            return false;
        if (timestamp != other.timestamp)
            return false;
        return true;
    }
}
