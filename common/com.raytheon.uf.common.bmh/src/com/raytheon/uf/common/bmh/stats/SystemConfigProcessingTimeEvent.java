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
 * Stat event used to track system configuration change process time.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 22, 2015 4397       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */
@DynamicSerialize
public class SystemConfigProcessingTimeEvent extends StatisticsEvent {

    private static final long serialVersionUID = 7603086466291952024L;

    private static final Map<String, String> FIELD_UNIT_MAP;
    static {
        Map<String, String> m = new HashMap<String, String>();
        m.put("latency", "ms");
        FIELD_UNIT_MAP = Collections.unmodifiableMap(m);
    }

    private long startTime = System.currentTimeMillis();

    @DynamicSerializeElement
    private long processingTime;

    @DynamicSerializeElement
    private String configurationType;

    /**
     * Empty constructor for {@link DynamicSerialize}.
     */
    public SystemConfigProcessingTimeEvent() {
    }

    public SystemConfigProcessingTimeEvent(String configurationType) {
        this.configurationType = configurationType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.common.stats.StatisticsEvent#getFieldUnitMap()
     */
    @Override
    protected Map<String, String> getFieldUnitMap() {
        return FIELD_UNIT_MAP;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.common.stats.StatisticsEvent#finalizeEvent()
     */
    @Override
    public void finalizeEvent() {
        this.processingTime = System.currentTimeMillis() - this.startTime;
    }

    /**
     * @return the processingTime
     */
    public long getProcessingTime() {
        return processingTime;
    }

    /**
     * @param processingTime
     *            the processingTime to set
     */
    public void setProcessingTime(long processingTime) {
        this.processingTime = processingTime;
    }

    /**
     * @return the configurationType
     */
    public String getConfigurationType() {
        return configurationType;
    }

    /**
     * @param configurationType
     *            the configurationType to set
     */
    public void setConfigurationType(String configurationType) {
        this.configurationType = configurationType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.common.event.Event#toString()
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(
                "SystemConfigProcessingTimeEvent [");
        sb.append("processingTime=").append(this.processingTime);
        sb.append(", configurationType=").append(this.configurationType);
        sb.append("]");

        return sb.toString();
    }
}