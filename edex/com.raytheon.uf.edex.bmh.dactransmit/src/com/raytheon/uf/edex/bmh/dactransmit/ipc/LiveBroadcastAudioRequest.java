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
package com.raytheon.uf.edex.bmh.dactransmit.ipc;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Used to transfer audio data to DAC Transmit for transmission.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 15, 2014 3655       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */
@DynamicSerialize
public class LiveBroadcastAudioRequest implements IDacLiveBroadcastMsg {

    @DynamicSerializeElement
    private String broadcastId;

    @DynamicSerializeElement
    private byte[] audioData;

    /**
     * 
     */
    public LiveBroadcastAudioRequest() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.edex.bmh.dactransmit.ipc.IDacLiveBroadcastMsg#getBroadcastId
     * ()
     */
    @Override
    public String getBroadcastId() {
        return this.broadcastId;
    }

    /**
     * @param broadcastId
     *            the broadcastId to set
     */
    public void setBroadcastId(String broadcastId) {
        this.broadcastId = broadcastId;
    }

    /**
     * @return the audioData
     */
    public byte[] getAudioData() {
        return audioData;
    }

    /**
     * @param audioData
     *            the audioData to set
     */
    public void setAudioData(byte[] audioData) {
        this.audioData = audioData;
    }
}