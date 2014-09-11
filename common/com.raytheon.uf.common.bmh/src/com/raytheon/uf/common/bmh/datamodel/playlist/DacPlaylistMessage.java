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

import java.nio.file.Path;
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
 * Aug 13, 2014  3286     dgilling    Add fields for tone playback.
 * Aug 18, 2014  3540     dgilling    Add getPlaybackInterval().
 * Sep 08, 2014  3286     dgilling    Add getPath() and setPath().
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

    @XmlElement
    private boolean playedSameTone;

    @XmlElement
    private boolean playedAlertTone;

    private transient Path path;

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

    public boolean isPlayedSameTone() {
        return playedSameTone;
    }

    public void setPlayedSameTone(boolean playedSameTone) {
        this.playedSameTone = playedSameTone;
    }

    public boolean isPlayedAlertTone() {
        return playedAlertTone;
    }

    public void setPlayedAlertTone(boolean playedAlertTone) {
        this.playedAlertTone = playedAlertTone;
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
        return isValid(TimeUtil.currentTimeMillis());
    }

    /**
     * Determines whether this is within is valid playback period based on the
     * specified epoch timestamp.
     * 
     * @param currentTime
     *            Time to use in epoch milliseconds.
     * @return {@code true}, if the message start time is before the specified
     *         time and the expire time is after the specified time. Else,
     *         {@code false}.
     */
    public boolean isValid(long currentTime) {
        return ((currentTime >= start.getTimeInMillis()) && (currentTime < expire
                .getTimeInMillis()));
    }

    /**
     * If this message has a valid "periodicity" setting, this method calculates
     * the time (in ms) that should ellapse between plays of this message based
     * on the periodicity setting (format is DDHHmm).
     * 
     * @return Number of milliseconds between plays, or, -1 if this message does
     *         not have a valid periodicity setting.
     */
    public long getPlaybackInterval() {
        if ((periodicity != null) && (!periodicity.isEmpty())) {
            int days = Integer.parseInt(this.periodicity.substring(0, 2));
            int hours = Integer.parseInt(this.periodicity.substring(2, 4));
            int minutes = Integer.parseInt(this.periodicity.substring(4, 6));
            return (minutes + (60 * (hours + (24 * days))))
                    * TimeUtil.MILLIS_PER_MINUTE;
        }
        return -1;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }
}
