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
package com.raytheon.uf.common.bmh.datamodel.playlist;

import java.util.Calendar;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.raytheon.uf.common.time.util.TimeUtil;

/**
 * 
 * Xml representation of a playlist message that is sent from the playlist
 * manager to the comms manager.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Jul 01, 2014  3285     bsteffen    Initial creation
 * Jul 24, 2014  3286     dgilling    Implement toString().
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
@XmlRootElement(name = "bmhMessage")
@XmlAccessorType(XmlAccessType.NONE)
public class DacPlaylistMessage extends DacPlaylistMessageId {

    @XmlElement
    private String messageType;

    @XmlElement
    private String soundFile;

    @XmlElement
    private Calendar start;

    @XmlElement
    private Calendar expire;

    /** format is HHMMSS */
    @XmlElement
    private String periodicity;

    @XmlElement
    private String messageText;

    @XmlElement
    private String SAMEtone;

    @XmlElement
    private boolean alertTone;

    @XmlElement
    private Calendar lastTransmitTime;

    @XmlElement
    private int playCount;

    public DacPlaylistMessage() {

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DACPlaylistMessage [broadcastId=").append(broadcastId)
                .append(", messageType=").append(messageType)
                .append(", start=").append(start).append(", expire=")
                .append(expire).append("]");
        return builder.toString();
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getSoundFile() {
        return soundFile;
    }

    public void setSoundFile(String soundFile) {
        this.soundFile = soundFile;
    }

    public Calendar getStart() {
        return start;
    }

    public void setStart(Calendar start) {
        this.start = start;
    }

    public Calendar getExpire() {
        return expire;
    }

    public void setExpire(Calendar expire) {
        this.expire = expire;
    }

    public String getPeriodicity() {
        return periodicity;
    }

    public void setPeriodicity(String periodicity) {
        this.periodicity = periodicity;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getSAMEtone() {
        return SAMEtone;
    }

    public void setSAMEtone(String sAMEtone) {
        SAMEtone = sAMEtone;
    }

    public boolean isAlertTone() {
        return alertTone;
    }

    public void setAlertTone(boolean alertTone) {
        this.alertTone = alertTone;
    }

    public Calendar getLastTransmitTime() {
        return lastTransmitTime;
    }

    public void setLastTransmitTime(Calendar lastTransmitTime) {
        this.lastTransmitTime = lastTransmitTime;
    }

    public int getPlayCount() {
        return playCount;
    }

    public void setPlayCount(int playCount) {
        this.playCount = playCount;
    }

    /**
     * Determine whether this message is within its valid playback period based
     * on the current time.
     * 
     * @return {@code true}, if the message start time is before the current
     *         time and the expire time is after the current time. Else,
     *         {@code false}.
     */
    public boolean isValid() {
        long currentTime = TimeUtil.currentTimeMillis();
        return ((currentTime >= start.getTimeInMillis()) && (currentTime <= expire
                .getTimeInMillis()));
    }

}