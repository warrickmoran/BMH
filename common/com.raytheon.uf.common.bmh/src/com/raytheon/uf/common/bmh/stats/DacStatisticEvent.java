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
 * Stat event for information about a transmitter groups DAC.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 6, 2015  3942      rjpeter     Initial creation
 * Jul 28, 2015 4686      bkowal      Moved statistics to common.
 * 
 * </pre>
 * 
 * @author rjpeter
 * @version 1.0
 */
@DynamicSerialize
public class DacStatisticEvent extends StatisticsEvent {
    private static final long serialVersionUID = 1L;

    private static final Map<String, String> FIELD_UNIT_MAP;
    static {
        Map<String, String> m = new HashMap<String, String>();
        m.put("bufferSize", "packets");
        m.put("recoverablePacketErrors", "packets");
        m.put("unrecoverablePacketErrors", "packets");
        FIELD_UNIT_MAP = Collections.unmodifiableMap(m);
    }

    @DynamicSerializeElement
    private String transmitterGroup;

    @DynamicSerializeElement
    private int bufferSize;

    @DynamicSerializeElement
    private int recoverablePacketErrors;

    @DynamicSerializeElement
    private int unrecoverablePacketErrors;

    @Override
    protected Map<String, String> getFieldUnitMap() {
        return FIELD_UNIT_MAP;
    }

    public String getTransmitterGroup() {
        return transmitterGroup;
    }

    public void setTransmitterGroup(String transmitterGroup) {
        this.transmitterGroup = transmitterGroup;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public int getRecoverablePacketErrors() {
        return recoverablePacketErrors;
    }

    public void setRecoverablePacketErrors(int recoverablePacketErrors) {
        this.recoverablePacketErrors = recoverablePacketErrors;
    }

    public int getUnrecoverablePacketErrors() {
        return unrecoverablePacketErrors;
    }

    public void setUnrecoverablePacketErrors(int unrecoverablePacketErrors) {
        this.unrecoverablePacketErrors = unrecoverablePacketErrors;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.common.stats.StatisticsEvent#finalizeEvent()
     */
    @Override
    public void finalizeEvent() {
        // not implemented
    }

}
