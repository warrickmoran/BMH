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

import java.util.Calendar;

import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Used to store information that will be used to prepare a transmitter for a
 * live broadcast.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 20, 2014 3655       bkowal      Initial creation
 * Oct 21, 2014 3655       bkowal      Added additional information about
 *                                     the broadcast.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */
@DynamicSerialize
public class BroadcastTransmitterConfiguration {

    @DynamicSerializeElement
    private MessageType selectedMessageType;
    
    @DynamicSerializeElement
    private Transmitter transmitter;

    @DynamicSerializeElement
    private byte[] toneAudio;

    @DynamicSerializeElement
    private long delayMilliseconds;
    
    @DynamicSerializeElement
    private Calendar effectiveTime;

    @DynamicSerializeElement
    private Calendar expireTime;
    
    @DynamicSerializeElement
    private boolean playAlertTones;

    /**
     * 
     */
    public BroadcastTransmitterConfiguration() {
    }

    public Transmitter getTransmitter() {
        return transmitter;
    }

    public void setTransmitter(Transmitter transmitter) {
        this.transmitter = transmitter;
    }

    public byte[] getToneAudio() {
        return toneAudio;
    }

    public void setToneAudio(byte[] toneAudio) {
        this.toneAudio = toneAudio;
    }

    public long getDelayMilliseconds() {
        return delayMilliseconds;
    }

    public void setDelayMilliseconds(long delayMilliseconds) {
        this.delayMilliseconds = delayMilliseconds;
    }

    /**
     * @return the selectedMessageType
     */
    public MessageType getSelectedMessageType() {
        return selectedMessageType;
    }

    /**
     * @param selectedMessageType the selectedMessageType to set
     */
    public void setSelectedMessageType(MessageType selectedMessageType) {
        this.selectedMessageType = selectedMessageType;
    }

    /**
     * @return the effectiveTime
     */
    public Calendar getEffectiveTime() {
        return effectiveTime;
    }

    /**
     * @param effectiveTime the effectiveTime to set
     */
    public void setEffectiveTime(Calendar effectiveTime) {
        this.effectiveTime = effectiveTime;
    }

    /**
     * @return the expireTime
     */
    public Calendar getExpireTime() {
        return expireTime;
    }

    /**
     * @param expireTime the expireTime to set
     */
    public void setExpireTime(Calendar expireTime) {
        this.expireTime = expireTime;
    }

    /**
     * @return the playAlertTones
     */
    public boolean isPlayAlertTones() {
        return playAlertTones;
    }

    /**
     * @param playAlertTones the playAlertTones to set
     */
    public void setPlayAlertTones(boolean playAlertTones) {
        this.playAlertTones = playAlertTones;
    }
}