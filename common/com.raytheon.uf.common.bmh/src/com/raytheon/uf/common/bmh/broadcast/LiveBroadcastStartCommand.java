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

import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;

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
    @Deprecated
    private Map<Transmitter, BroadcastTransmitterConfiguration> transmitterConfigurationMap = new HashMap<>();

    @DynamicSerializeElement
    private Map<TransmitterGroup, BroadcastTransmitterConfiguration> transmitterGroupConfigurationMap = new HashMap<>();

    /**
     * 
     */
    public LiveBroadcastStartCommand() {
        super();
        super.setAction(ACTION.PREPARE);
    }

    public void addTransmitterConfiguration(
            BroadcastTransmitterConfiguration configuration) {
        if (configuration.getTransmitter() != null) {
            this.transmitterConfigurationMap.put(
                    configuration.getTransmitter(), configuration);
        } else if (configuration.getTransmitterGroup() != null) {
            this.transmitterGroupConfigurationMap.put(
                    configuration.getTransmitterGroup(), configuration);
        }
    }

    public Set<Transmitter> getRequestedTransmitters() {
        return this.transmitterConfigurationMap.keySet();
    }

    public BROADCASTTYPE getType() {
        return type;
    }

    public void setType(BROADCASTTYPE type) {
        this.type = type;
    }

    public Map<Transmitter, BroadcastTransmitterConfiguration> getTransmitterConfigurationMap() {
        return transmitterConfigurationMap;
    }

    public void setTransmitterConfigurationMap(
            Map<Transmitter, BroadcastTransmitterConfiguration> transmitterConfigurationMap) {
        this.transmitterConfigurationMap = transmitterConfigurationMap;
    }

    public Map<TransmitterGroup, BroadcastTransmitterConfiguration> getTransmitterGroupConfigurationMap() {
        return transmitterGroupConfigurationMap;
    }

    public void setTransmitterGroupConfigurationMap(
            Map<TransmitterGroup, BroadcastTransmitterConfiguration> transmitterGroupConfigurationMap) {
        this.transmitterGroupConfigurationMap = transmitterGroupConfigurationMap;
    }
}