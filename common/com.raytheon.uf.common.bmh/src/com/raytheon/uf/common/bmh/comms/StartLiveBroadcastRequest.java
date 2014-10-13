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
package com.raytheon.uf.common.bmh.comms;

import java.util.Set;
import java.util.Map;
import java.util.HashMap;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Used to request the initiation of a live broadcast session.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 8, 2014  3656       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */
@DynamicSerialize
public class StartLiveBroadcastRequest {

    /*
     * The broadcast identifier will be set by the {@link BroadcastStreamTask}
     * that is started first. The associated {@link BroadcastStreamTask}(s)
     * running on other Comms Manager cluster members will take the name of the
     * original task.
     */
    @DynamicSerializeElement
    private String broadcastId;

    @DynamicSerializeElement
    private String wsid;

    @DynamicSerializeElement
    private Map<String, LiveBroadcastStartData> liveBroadcastDataMap;

    /**
     * 
     */
    public StartLiveBroadcastRequest() {
    }

    /**
     * @return the broadcastId
     */
    public String getBroadcastId() {
        return broadcastId;
    }

    /**
     * @param broadcastId
     *            the broadcastId to set
     */
    public void setBroadcastId(String broadcastId) {
        this.broadcastId = broadcastId;
    }

    public void addLiveBroadcastStartData(LiveBroadcastStartData data) {
        if (this.liveBroadcastDataMap == null) {
            this.liveBroadcastDataMap = new HashMap<>();
        }
        /*
         * TODO: ensure that each start data that is added only maps to one
         * transmitter group.
         */
        this.liveBroadcastDataMap.put(data.getTransmitterGroup(), data);
    }

    public Set<String> getRequestedTransmitters() {
        return this.liveBroadcastDataMap.keySet();
    }

    /**
     * @return the wsid
     */
    public String getWsid() {
        return wsid;
    }

    /**
     * @param wsid
     *            the wsid to set
     */
    public void setWsid(String wsid) {
        this.wsid = wsid;
    }

    /**
     * @return the liveBroadcastDataMap
     */
    public Map<String, LiveBroadcastStartData> getLiveBroadcastDataMap() {
        return liveBroadcastDataMap;
    }

    /**
     * @param liveBroadcastDataMap
     *            the liveBroadcastDataMap to set
     */
    public void setLiveBroadcastDataMap(
            Map<String, LiveBroadcastStartData> liveBroadcastDataMap) {
        this.liveBroadcastDataMap = liveBroadcastDataMap;
    }
}