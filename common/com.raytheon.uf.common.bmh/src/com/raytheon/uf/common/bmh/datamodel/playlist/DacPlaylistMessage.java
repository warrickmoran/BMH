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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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
 * Sep 12, 2014  3588     bsteffen    Support audio fragments.
 * Sep 25, 2014  3620     bsteffen    Add seconds to periodicity.
 * Oct 01, 2014  3485     bsteffen    Add method for getting path of position file.
 * Oct 23, 2014  3617     dgilling    Add support for tone blackout period.
 * Nov 03, 2014  3781     dgilling    Add isSAMETones().
 * Dec 08, 2014  3878     bkowal      Added isStatic to indicate whether or not
 *                                    the message is associated with a static msg type.
 * Dec 11, 2014  3651     bkowal      Added {@link #name} for logging purposes.
 * Jan 05, 2015  3913     bsteffen    Handle future replacements.
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
    private String name;
    
    @XmlElement
    private String messageType;

    @XmlElement(name = "soundFile")
    private List<String> soundFiles;

    @XmlElement
    private Calendar start;

    @XmlElement
    private Calendar expire;

    /** format is DDHHMMSS */
    @XmlElement
    private String periodicity;

    @XmlElement
    private String messageText;

    @XmlElement
    private String SAMEtone;

    @XmlElement
    private boolean alertTone;

    @XmlElement
    private boolean toneBlackoutEnabled;

    @XmlElement
    private String toneBlackoutStart;

    @XmlElement
    private String toneBlackoutEnd;

    @XmlElement
    private Calendar lastTransmitTime;

    @XmlElement
    private int playCount;

    @XmlElement
    private boolean playedSameTone;

    @XmlElement
    private boolean playedAlertTone;

    /*
     * boolean indicating whether or not the message is associated with a static
     * message type.
     */
    @XmlElement
    private boolean isStatic;

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

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public List<String> getSoundFiles() {
        return soundFiles;
    }

    public void setSoundFiles(List<String> soundFiles) {
        this.soundFiles = soundFiles;
    }

    public void addSoundFile(String soundFile) {
        if (this.soundFiles == null) {
            this.soundFiles = new ArrayList<>();
        }
        this.soundFiles.add(soundFile);
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
        boolean started = currentTime >= start.getTimeInMillis();
        boolean ended = currentTime >= expire.getTimeInMillis();
        boolean replaced = replaceTime == null ? false
                : currentTime >= replaceTime.getTimeInMillis();
        return started && !ended && !replaced;
    }

    /**
     * If this message has a valid "periodicity" setting, this method calculates
     * the time (in ms) that should elapse between plays of this message based
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
            int seconds = Integer.parseInt(this.periodicity.substring(6, 8));
            return (seconds + (60 * (minutes + (60 * (hours + (24 * days))))))
                    * TimeUtil.MILLIS_PER_SECOND;
        }
        return -1;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    /**
     * @return the path of a file that should be used for tracking the position
     *         in the stream of the current playback.
     */
    public Path getPositionPath() {
        if (path == null) {
            return null;
        }
        return path.resolveSibling(path.getFileName().toString()
                .replace(".xml", ".position"));
    }

    public boolean isToneBlackoutEnabled() {
        return toneBlackoutEnabled;
    }

    public void setToneBlackoutEnabled(boolean toneBlackoutEnabled) {
        this.toneBlackoutEnabled = toneBlackoutEnabled;
    }

    public String getToneBlackoutStart() {
        return toneBlackoutStart;
    }

    public void setToneBlackoutStart(String toneBlackoutStart) {
        this.toneBlackoutStart = toneBlackoutStart;
    }

    public String getToneBlackoutEnd() {
        return toneBlackoutEnd;
    }

    public void setToneBlackoutEnd(String toneBlackoutEnd) {
        this.toneBlackoutEnd = toneBlackoutEnd;
    }

    /**
     * Determine whether or not to play tones (taking into account any possibly
     * configured tone blackout period) for this message given the specified
     * play time
     * 
     * @param time
     *            A {@code Calendar} instance specifying the time this message
     *            will be played.
     * @return Whether or not tones should be played for this message.
     */
    public boolean shouldPlayTones(Calendar time) {
        boolean hasTonesToPlay = ((isSAMETones() && !playedSameTone) || (alertTone && !playedAlertTone));
        boolean outsideBlackoutPeriod = (hasTonesToPlay && toneBlackoutEnabled) ? isOutsideBlackoutPeriod(time)
                : false;

        if (!toneBlackoutEnabled && hasTonesToPlay) {
            return true;
        } else if (toneBlackoutEnabled && hasTonesToPlay
                && outsideBlackoutPeriod) {
            return true;
        }

        return false;
    }

    private boolean isOutsideBlackoutPeriod(Calendar time) {
        if (toneBlackoutEnabled) {
            int startTime = Integer.parseInt(toneBlackoutStart);
            int endTime = Integer.parseInt(toneBlackoutEnd);

            boolean periodCrossesDayLine = (endTime <= startTime);

            int currentTime = (time.get(Calendar.HOUR_OF_DAY) * 100)
                    + time.get(Calendar.MINUTE);

            if (periodCrossesDayLine) {
                /*
                 * If the blackout period crosses the day line, then the period
                 * during which a tone should play is a contiguous time range
                 * that begins after the blackout end time and ends at the start
                 * of the blackout period.
                 */
                return ((currentTime >= endTime) && (currentTime < startTime));
            } else {
                /*
                 * If the blackout period does not cross the day line, then the
                 * period is 2 disjoint time ranges: (1) a time range that
                 * begins after the end of the blackout period and lasts until
                 * the end of the day and (2) a time range that begins at the
                 * beginning of the day and lasts until the beginning of the
                 * blackout period.
                 */
                return (((currentTime >= endTime) && (currentTime < 2400)) || ((currentTime >= 0) && (currentTime < startTime)));
            }
        }

        return true;
    }

    /**
     * Returns whether this message has a valid SAME tone header.
     * 
     * @return Whether or not this message has a valid SAME tone header.
     */
    public boolean isSAMETones() {
        return ((SAMEtone != null) && (!SAMEtone.isEmpty()));
    }

    /**
     * @return the isStatic
     */
    public boolean isStatic() {
        return isStatic;
    }

    /**
     * @param isStatic
     *            the isStatic to set
     */
    public void setStatic(boolean isStatic) {
        this.isStatic = isStatic;
    }
}
