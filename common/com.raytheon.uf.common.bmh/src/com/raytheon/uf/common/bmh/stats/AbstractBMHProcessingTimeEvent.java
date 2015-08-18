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
 * Abstraction of a {@link StatisticsEvent} used to store the processing time
 * for an action associated with a Transmitter Group.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 20, 2015 4397       bkowal      Initial creation
 * Jun 24, 2015 4397       bkowal      Added an empty constructor.
 * Jul 28, 2015 4686       bkowal      Moved statistics to common.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */
@DynamicSerialize
public abstract class AbstractBMHProcessingTimeEvent extends StatisticsEvent {

    private static final long serialVersionUID = -629245417527840995L;

    @DynamicSerializeElement
    private String transmitterGroup;

    @DynamicSerializeElement
    private long processingTime;

    private long requestTime;

    private static final Map<String, String> FIELD_UNIT_MAP;
    static {
        Map<String, String> m = new HashMap<String, String>();
        m.put("processingTime", "ms");
        FIELD_UNIT_MAP = Collections.unmodifiableMap(m);
    }

    /**
     * Constructor.
     * 
     * Empty constructor for {@link DynamicSerialize}.
     */
    protected AbstractBMHProcessingTimeEvent() {
    }

    protected AbstractBMHProcessingTimeEvent(final long requestTime) {
        this.requestTime = requestTime;
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
        this.processingTime = System.currentTimeMillis() - this.requestTime;
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

    public long getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(long requestTime) {
        this.requestTime = requestTime;
    }
}