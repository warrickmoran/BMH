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
package com.raytheon.uf.common.bmh.stats;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.stats.StatisticsEvent;

/**
 * Stat event used to track live broadcast latency.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 14, 2015 4397       bkowal      Initial creation
 * Jul 28, 2015 4686       bkowal      Moved statistics to common.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */
@DynamicSerialize
public class LiveBroadcastLatencyEvent extends StatisticsEvent {

    private static final long serialVersionUID = -4544862921691732574L;

    private static final Map<String, String> FIELD_UNIT_MAP;
    static {
        Map<String, String> m = new HashMap<String, String>();
        m.put("latency", "ms");
        FIELD_UNIT_MAP = Collections.unmodifiableMap(m);
    }

    @DynamicSerializeElement
    private String transmitterGroup;

    @DynamicSerializeElement
    private String broadcastIdentifier;

    @DynamicSerializeElement
    private long latency;

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.common.stats.StatisticsEvent#getFieldUnitMap()
     */
    @Override
    protected Map<String, String> getFieldUnitMap() {
        return FIELD_UNIT_MAP;
    }

    /**
     * @return the transmitterGroup
     */
    public String getTransmitterGroup() {
        return transmitterGroup;
    }

    /**
     * @param transmitterGroup
     *            the transmitterGroup to set
     */
    public void setTransmitterGroup(String transmitterGroup) {
        this.transmitterGroup = transmitterGroup;
    }

    /**
     * @return the broadcastIdentifier
     */
    public String getBroadcastIdentifier() {
        return broadcastIdentifier;
    }

    /**
     * @param broadcastIdentifier
     *            the broadcastIdentifier to set
     */
    public void setBroadcastIdentifier(String broadcastIdentifier) {
        this.broadcastIdentifier = broadcastIdentifier;
    }

    /**
     * @return the latency (in milliseconds)
     */
    public long getLatency() {
        return latency;
    }

    /**
     * @param latency
     *            the latency to set (in milliseconds)
     */
    public void setLatency(long latency) {
        this.latency = latency;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.common.stats.StatisticsEvent#finalizeEvent()
     */
    @Override
    public void finalizeEvent() {
        // Do Nothing.
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(
                "LiveBroadcastLatencyEvent [transmitterGroup=");
        sb.append(this.transmitterGroup);
        sb.append(", broadcastIdentifier=");
        sb.append(this.broadcastIdentifier);
        sb.append(", latency=");
        sb.append(this.latency);
        sb.append("]");

        return sb.toString();
    }
}