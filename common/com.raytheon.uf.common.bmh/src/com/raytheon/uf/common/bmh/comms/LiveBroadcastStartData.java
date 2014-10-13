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

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Used to specify a transmitter and tone information for a (@link
 * StartLiveBroadcastRequest}. There will always be one
 * {@link LiveBroadcastStartData} per transmitter. So, multiple @{link
 * LiveBroadcastStartData} may be associated with a single
 * {@link StartLiveBroadcastRequest}.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 9, 2014  3656       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */
@DynamicSerialize
public class LiveBroadcastStartData {

    @DynamicSerializeElement
    private String transmitterGroup;

    @DynamicSerializeElement
    private boolean playAlertTone;

    @DynamicSerializeElement
    private byte[] tonesData;

    /*
     * The amount of time to wait before playing the tones in milliseconds. The
     * tones delay time is calculated in relation to the tone with the longest
     * duration out of all live broadcasts that will be initiated by the {@link
     * StartLiveBroadcastRequest} that this {@link LiveBroadcastStartData} is a
     * part of.
     * 
     * TODO: evaluate the implications of using this method. It is true that we
     * would prevent awkward pauses. However, we run the risk of a tone
     * associated with the current playlist that is playing playing just as the
     * delay expires. In this case, the transmitter would no longer be available
     * to us even though the Broadcast Live tones had already started playing
     * for other transmitters that the message is being transmitted to so
     * aborting the broadcast live playback becomes complicated.
     */
    @DynamicSerializeElement
    private int tonesDelay;

    /**
     * 
     */
    public LiveBroadcastStartData() {
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
     * @return the playAlertTone
     */
    public boolean isPlayAlertTone() {
        return playAlertTone;
    }

    /**
     * @param playAlertTone
     *            the playAlertTone to set
     */
    public void setPlayAlertTone(boolean playAlertTone) {
        this.playAlertTone = playAlertTone;
    }

    /**
     * @return the tonesData
     */
    public byte[] getTonesData() {
        return tonesData;
    }

    /**
     * @param tonesData
     *            the tonesData to set
     */
    public void setTonesData(byte[] tonesData) {
        this.tonesData = tonesData;
    }

    /**
     * @return the tonesDelay
     */
    public int getTonesDelay() {
        return tonesDelay;
    }

    /**
     * @param tonesDelay
     *            the tonesDelay to set
     */
    public void setTonesDelay(int tonesDelay) {
        this.tonesDelay = tonesDelay;
    }
}