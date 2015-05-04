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

import com.raytheon.uf.common.bmh.notify.INonStandardBroadcast;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
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
 * Nov 1, 2014  3655       bkowal      Added end of message tones.
 * Nov 17, 2014 3808       bkowal      Initial support for transmitter groups.
 * Nov 21, 2014 3845       bkowal      Re-factor/cleanup
 * May 04, 2015 4394       bkowal      Relocated tone playback text to
 *                                     {@link INonStandardBroadcast}.
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */
@DynamicSerialize
public class BroadcastTransmitterConfiguration {

    @DynamicSerializeElement
    private TransmitterGroup transmitterGroup;

    /*
     * Broadcast cycle playlist text. Already pre-formatted and ready for
     * immediate display.
     */
    @DynamicSerializeElement
    private String messageId;

    @DynamicSerializeElement
    private String messageTitle;

    @DynamicSerializeElement
    private String messageName;

    @DynamicSerializeElement
    private String expirationTime;

    @DynamicSerializeElement
    private String alert;

    @DynamicSerializeElement
    private String same;

    /*
     * Tones and associated information (when applicable). Currently not worth
     * abstracting this class to provide constructs both with and without tones.
     */
    @DynamicSerializeElement
    private byte[] toneAudio;

    @DynamicSerializeElement
    private long delayMilliseconds;

    @DynamicSerializeElement
    private byte[] endToneAudio;

    /**
     * 
     */
    public BroadcastTransmitterConfiguration() {
    }

    /**
     * @return the transmitterGroup
     */
    public TransmitterGroup getTransmitterGroup() {
        return transmitterGroup;
    }

    /**
     * @param transmitterGroup
     *            the transmitterGroup to set
     */
    public void setTransmitterGroup(TransmitterGroup transmitterGroup) {
        this.transmitterGroup = transmitterGroup;
    }

    /**
     * @return the messageId
     */
    public String getMessageId() {
        return messageId;
    }

    /**
     * @param messageId
     *            the messageId to set
     */
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    /**
     * @return the messageTitle
     */
    public String getMessageTitle() {
        return messageTitle;
    }

    /**
     * @param messageTitle
     *            the messageTitle to set
     */
    public void setMessageTitle(String messageTitle) {
        this.messageTitle = messageTitle;
    }

    /**
     * @return the messageName
     */
    public String getMessageName() {
        return messageName;
    }

    /**
     * @param messageName
     *            the messageName to set
     */
    public void setMessageName(String messageName) {
        this.messageName = messageName;
    }

    /**
     * @return the expirationTime
     */
    public String getExpirationTime() {
        return expirationTime;
    }

    /**
     * @param expirationTime
     *            the expirationTime to set
     */
    public void setExpirationTime(String expirationTime) {
        this.expirationTime = expirationTime;
    }

    /**
     * @return the alert
     */
    public String getAlert() {
        return alert;
    }

    /**
     * @param alert
     *            the alert to set
     */
    public void setAlert(String alert) {
        this.alert = alert;
    }

    /**
     * @return the same
     */
    public String getSame() {
        return same;
    }

    /**
     * @param same
     *            the same to set
     */
    public void setSame(String same) {
        this.same = same;
    }

    /**
     * @return the toneAudio
     */
    public byte[] getToneAudio() {
        return toneAudio;
    }

    /**
     * @param toneAudio
     *            the toneAudio to set
     */
    public void setToneAudio(byte[] toneAudio) {
        this.toneAudio = toneAudio;
    }

    /**
     * @return the delayMilliseconds
     */
    public long getDelayMilliseconds() {
        return delayMilliseconds;
    }

    /**
     * @param delayMilliseconds
     *            the delayMilliseconds to set
     */
    public void setDelayMilliseconds(long delayMilliseconds) {
        this.delayMilliseconds = delayMilliseconds;
    }

    /**
     * @return the endToneAudio
     */
    public byte[] getEndToneAudio() {
        return endToneAudio;
    }

    /**
     * @param endToneAudio
     *            the endToneAudio to set
     */
    public void setEndToneAudio(byte[] endToneAudio) {
        this.endToneAudio = endToneAudio;
    }
}