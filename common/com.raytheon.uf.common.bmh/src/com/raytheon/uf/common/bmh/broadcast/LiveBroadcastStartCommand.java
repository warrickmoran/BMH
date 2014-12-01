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
package com.raytheon.uf.common.bmh.broadcast;

import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

import java.util.Map;
import java.util.HashMap;

/**
 * Used to start / initialize a live broadcast.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 20, 2014 3655       bkowal      Initial creation
 * Nov 17, 2014 3808       bkowal      Support broadcast live.
 * Nov 21, 2014 3845       bkowal      Re-factor/cleanup
 * Dec 1, 2014  3797       bkowal      Aded get/set tonesDuration
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */
@DynamicSerialize
public class LiveBroadcastStartCommand extends LiveBroadcastCommand {

    public static enum BROADCASTTYPE {
        /* enum constant for Emergency Override */
        EO,
        /* enum constant for Broadcast Live */
        BL
    }

    @DynamicSerializeElement
    private BROADCASTTYPE type = BROADCASTTYPE.EO;

    @DynamicSerializeElement
    private Map<TransmitterGroup, BroadcastTransmitterConfiguration> transmitterGroupConfigurationMap = new HashMap<>();

    @DynamicSerializeElement
    private long tonesDuration;

    /**
     * 
     */
    public LiveBroadcastStartCommand() {
        super();
        super.setAction(ACTION.PREPARE);
    }

    public void addTransmitterConfiguration(
            BroadcastTransmitterConfiguration configuration) {
        this.transmitterGroupConfigurationMap.put(
                configuration.getTransmitterGroup(), configuration);
        super.addTransmitterGroup(configuration.getTransmitterGroup());
    }

    public BROADCASTTYPE getType() {
        return type;
    }

    public void setType(BROADCASTTYPE type) {
        this.type = type;
    }

    public Map<TransmitterGroup, BroadcastTransmitterConfiguration> getTransmitterGroupConfigurationMap() {
        return transmitterGroupConfigurationMap;
    }

    public void setTransmitterGroupConfigurationMap(
            Map<TransmitterGroup, BroadcastTransmitterConfiguration> transmitterGroupConfigurationMap) {
        this.transmitterGroupConfigurationMap = transmitterGroupConfigurationMap;
    }

    public long getTonesDuration() {
        return tonesDuration;
    }

    public void setTonesDuration(long tonesDuration) {
        this.tonesDuration = tonesDuration;
    }
}