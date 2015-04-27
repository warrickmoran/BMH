/**
 * 
 */
package com.raytheon.uf.edex.bmh.stats;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.stats.StatisticsEvent;

/**
 * Stat event used to track message delivery time.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 24, 2015 4397       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */
@DynamicSerialize
public class DeliveryTimeEvent extends StatisticsEvent {

    private static final long serialVersionUID = -3037976216199253059L;

    private static final Map<String, String> FIELD_UNIT_MAP;
    static {
        Map<String, String> m = new HashMap<String, String>();
        m.put("deliveryTime", "ms");
        FIELD_UNIT_MAP = Collections.unmodifiableMap(m);
    }

    @DynamicSerializeElement
    private String transmitterGroup;

    @DynamicSerializeElement
    private long deliveryTime;

    /**
     * Constructor.
     */
    public DeliveryTimeEvent() {
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
        /* Do Nothing. */
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
     * @return the deliveryTime
     */
    public long getDeliveryTime() {
        return deliveryTime;
    }

    /**
     * @param deliveryTime
     *            the deliveryTime to set
     */
    public void setDeliveryTime(long deliveryTime) {
        this.deliveryTime = deliveryTime;
    }
}