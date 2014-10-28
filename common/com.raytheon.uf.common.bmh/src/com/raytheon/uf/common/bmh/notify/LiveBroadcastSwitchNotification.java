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
package com.raytheon.uf.common.bmh.notify;

import java.util.Calendar;

import com.raytheon.uf.common.bmh.data.IPlaylistData;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Used to notify the Playlist State Manager and any listening Broadcast Cycle
 * dialogs that a live broadcast is now playing on a particular transmitter.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 21, 2014 3655       bkowal      Initial creation
 * Oct 27, 2014 3712       bkowal      Added broadcastState.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

@DynamicSerialize
public class LiveBroadcastSwitchNotification implements IPlaylistData {

    public static enum STATE {
        STARTED, FINISHED
    }

    private static final String TONE_SENT = "SENT";

    private static final String TONE_NONE = "NONE";

    @DynamicSerializeElement
    private STATE broadcastState;

    @DynamicSerializeElement
    private String transmitterGroup;

    @DynamicSerializeElement
    private MessageType messageType;

    @DynamicSerializeElement
    private Calendar transitTime;

    @DynamicSerializeElement
    private Calendar expirationTime;

    @DynamicSerializeElement
    private String sameTone = TONE_NONE;

    @DynamicSerializeElement
    private String alertTone = TONE_NONE;

    /**
     * 
     */
    public LiveBroadcastSwitchNotification() {
    }

    public STATE getBroadcastState() {
        return broadcastState;
    }

    public void setBroadcastState(STATE broadcastState) {
        this.broadcastState = broadcastState;
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
     * @return the messageType
     */
    public MessageType getMessageType() {
        return messageType;
    }

    /**
     * @param messageType
     *            the messageType to set
     */
    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    /**
     * @return the transitTime
     */
    public Calendar getTransitTime() {
        return transitTime;
    }

    /**
     * @param transitTime
     *            the transitTime to set
     */
    public void setTransitTime(Calendar transitTime) {
        this.transitTime = transitTime;
    }

    /**
     * @return the expirationTime
     */
    public Calendar getExpirationTime() {
        return expirationTime;
    }

    /**
     * @param expirationTime
     *            the expirationTime to set
     */
    public void setExpirationTime(Calendar expirationTime) {
        this.expirationTime = expirationTime;
    }

    /**
     * @return the sameTone
     */
    public String getSameTone() {
        return sameTone;
    }

    /**
     * @param sameTone
     *            the sameTone to set
     */
    public void setSameTone(String sameTone) {
        this.sameTone = sameTone;
    }

    /**
     * @param sameTone
     *            the sameTone to set
     */
    public void setSameTone(boolean sameTone) {
        if (sameTone) {
            this.sameTone = TONE_SENT;
        }
    }

    /**
     * @return the alertTone
     */
    public String getAlertTone() {
        return alertTone;
    }

    /**
     * @param alertTone
     *            the alertTone to set
     */
    public void setAlertTone(String alertTone) {
        this.alertTone = alertTone;
    }

    /**
     * @param alertTone
     *            the alertTone to set
     */
    public void setAlertTone(boolean alertTone) {
        if (alertTone) {
            this.alertTone = TONE_SENT;
        }
    }
}