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
package com.raytheon.uf.common.bmh.notify.config;

import java.util.List;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Used to notify the Playlist Manager that specific broadcast messages have
 * been expired.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 15, 2015 4293       bkowal      Initial creation
 * Apr 20, 2015 4397       bkowal      Added {@link #requestTime}.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */
@DynamicSerialize
public class MessageForcedExpirationNotification extends ConfigNotification {

    @DynamicSerializeElement
    private long requestTime;

    @DynamicSerializeElement
    private List<Long> broadcastIds;

    /*
     * No argument constructor for DynamicSerialize
     */
    public MessageForcedExpirationNotification() {
        super(ConfigChangeType.Delete);
    }

    /**
     * Constructor.
     */
    public MessageForcedExpirationNotification(long requestTime) {
        super(ConfigChangeType.Delete);
        this.requestTime = requestTime;
    }

    /**
     * @return the requestTime
     */
    public long getRequestTime() {
        return requestTime;
    }

    /**
     * @param requestTime the requestTime to set
     */
    public void setRequestTime(long requestTime) {
        this.requestTime = requestTime;
    }

    /**
     * @return the broadcastIds
     */
    public List<Long> getBroadcastIds() {
        return broadcastIds;
    }

    /**
     * @param broadcastIds
     *            the broadcastIds to set
     */
    public void setBroadcastIds(List<Long> broadcastIds) {
        this.broadcastIds = broadcastIds;
    }
}
