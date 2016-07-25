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
package com.raytheon.uf.common.bmh.datamodel.playlist.compatibility;

import java.nio.file.Path;
import java.util.Calendar;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.raytheon.uf.common.bmh.notify.MessageBroadcastNotifcation;
import com.raytheon.uf.common.time.util.TimeUtil;

/**
 * 
 * Xml representation of a playlist message that is sent from the playlist
 * manager to the comms manager.
 * 
 * NOTE: Do not update. Only exists to allow message file compatibility from one
 * version to the next.
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
 * Jan 08, 2015  3912     bsteffen    Add isPeriodic
 * Jan 12, 2015  3968     bkowal      Added {@link #confirm}.
 * Jan 14, 2014  3969     bkowal      Added {@link #warning}, {@link #watch},
 *                                    {@link #messageBroadcastNotificationSent},
 *                                    and {@link #requiresExpirationNoPlaybackNotification()}.
 * Feb 03, 2015  4081     bkowal      Fix {@link #isPeriodic()}. Removed unused isStatic field.
 * Mar 05, 2015  4222     bkowal      Handle messages that never expire.
 * Mar 13, 2015  4222     bkowal      Prevent NPE for messages that do not expire.
 * Mar 25, 2015  4290     bsteffen    Switch to global replacement.
 * Apr 07, 2015  4293     bkowal      Added get/set methods for {@link #messageBroadcastNotificationSent}.
 * Apr 27, 2015  4397     bkowal      Added {@link #initialRecognitionTime} and {@link #recognized}.
 * May 11, 2015  4002     bkowal      Added {@link #initialBLDelayNotificationSent}.
 * May 13, 2015  4429     rferrel     Added traceId to {@link #toString()}.
 * May 26, 2015  4481     bkowal      Added {@link #dynamic}.
 * Feb 04, 2016  5308     bkowal      Refactored into {@link DacPlaylistMessageMetadata}.
 * Mar 08, 2016  5382     bkowal      Based on 16.1.3. Maintained for message file conversion.
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
@Deprecated
@XmlRootElement(name = "bmhMessage")
@XmlAccessorType(XmlAccessType.NONE)
public class DacPlaylistMessage16_1_3 extends DacPlaylistMessageId16_1_3
        implements IMessageMetadataAccess16_1_3 {

    @XmlElement
    private String name;

    @XmlElement
    private Calendar start;

    @XmlElement
    private String messageType;

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
     * boolean indicating whether or not the confirm flag has been set on the
     * associated message.
     */
    @XmlElement
    private boolean confirm;

    /*
     * boolean indicating that this message is a watch. Requirements state that
     * BMH users must be notified when a watch/warning is not broadcast due to
     * expiration even though it had been scheduled for broadcast. Set based on
     * the message type designation.
     */
    @XmlElement
    private boolean watch;

    /*
     * boolean indicating that this message is a warning. Requirements state
     * that BMH users must be notified when a watch/warning is not broadcast due
     * to expiration even though it had been scheduled for broadcast. Set based
     * on the message type designation.
     */
    @XmlElement
    private boolean warning;

    /**
     * boolean flag used to mark when a {@link MessageBroadcastNotifcation} is
     * sent for this message to ensure that multiple notifications are never
     * sent. This field is theoretically transient and will only hold its state
     * for as long as this object is in memory. This flag is necessary because
     * an expired {@link DacPlaylistMessage16_1_3} will only be eliminated (the
     * playlist scheduler will keep iterating over it until then) when a newer
     * version of the containing playlist is read.
     */
    private boolean messageBroadcastNotificationSent;

    /**
     * boolean used to ensure that once one delay notification is sent out for a
     * single {@link DacPlaylistMessage16_1_3} that may be associated with more
     * than one delay scenario.
     */
    private transient boolean initialBLDelayNotificationSent;

    private transient volatile DacPlaylistMessage16_1_3 metadata;

    public DacPlaylistMessage16_1_3() {

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
                .append(", traceId=").append(traceId).append(", expire=");
        if (this.expire != null) {
            builder.append(expire.getTime().toString());
        }
        builder.append("]");
        return builder.toString();
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
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

    public Calendar getStart() {
        return this.start;
    }

    /**
     * @param start
     *            the start to set
     */
    public void setStart(Calendar start) {
        this.start = start;
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
        boolean started = currentTime >= this.start.getTimeInMillis();
        boolean ended = (this.expire != null && currentTime >= expire
                .getTimeInMillis());
        return started && !ended;
    }

    /**
     * @return the initialBLDelayNotificationSent
     */
    public boolean isInitialBLDelayNotificationSent() {
        return initialBLDelayNotificationSent;
    }

    /**
     * @param initialBLDelayNotificationSent
     *            the initialBLDelayNotificationSent to set
     */
    public void setInitialBLDelayNotificationSent(
            boolean initialBLDelayNotificationSent) {
        this.initialBLDelayNotificationSent = initialBLDelayNotificationSent;
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
     * @return the confirm
     */
    public boolean isConfirm() {
        return confirm;
    }

    /**
     * @param confirm
     *            the confirm to set
     */
    public void setConfirm(boolean confirm) {
        this.confirm = confirm;
    }

    /**
     * @return the watch
     */
    public boolean isWatch() {
        return watch;
    }

    /**
     * @param watch
     *            the watch to set
     */
    public void setWatch(boolean watch) {
        this.watch = watch;
    }

    /**
     * @return the warning
     */
    public boolean isWarning() {
        return warning;
    }

    /**
     * @param warning
     *            the warning to set
     */
    public void setWarning(boolean warning) {
        this.warning = warning;
    }

    public boolean requiresExpirationNoPlaybackNotification() {
        boolean expired = this.expire != null
                && TimeUtil.currentTimeMillis() >= this.expire
                        .getTimeInMillis();

        boolean result = expired
                && (this.messageBroadcastNotificationSent == false)
                && (this.warning || this.watch) && (this.playCount == 0);
        if (result) {
            /*
             * ensure that a notification is not continuously sent until a new
             * playlist is generated without this message. there are still a few
             * rare edge cases that would cause duplicate notifications to be
             * sent. however, all of them involve the dac transmit process
             * crashing and restarting.
             */
            this.messageBroadcastNotificationSent = true;
        }
        return result;
    }

    /**
     * @param messageBroadcastNotificationSent
     *            the messageBroadcastNotificationSent to set
     */
    public void setMessageBroadcastNotificationSent(
            boolean messageBroadcastNotificationSent) {
        this.messageBroadcastNotificationSent = messageBroadcastNotificationSent;
    }

    /**
     * @return the messageBroadcastNotificationSent
     */
    public boolean isMessageBroadcastNotificationSent() {
        return messageBroadcastNotificationSent;
    }

    /**
     * @return the metadata
     */
    public DacPlaylistMessage16_1_3 getMetadata() {
        return metadata;
    }

    /**
     * @param metadata
     *            the metadata to set
     */
    public void setMetadata(DacPlaylistMessage16_1_3 metadata) {
        this.metadata = metadata;
    }

    @Override
    public boolean isPeriodic() {
        if (this.metadata == null) {
            throw new IllegalStateException(
                    "Associated message metadata has not yet been loaded.");
        }
        return this.metadata.isPeriodic();
    }

    @Override
    public long getPlaybackInterval() {
        if (this.metadata == null) {
            throw new IllegalStateException(
                    "Associated message metadata has not yet been loaded.");
        }
        return this.metadata.getPlaybackInterval();
    }

    @Override
    public List<String> getSoundFiles() {
        if (this.metadata == null) {
            throw new IllegalStateException(
                    "Associated message metadata has not yet been loaded.");
        }
        return this.metadata.getSoundFiles();
    }

    @Override
    public boolean isDynamic() {
        if (this.metadata == null) {
            throw new IllegalStateException(
                    "Associated message metadata has not yet been loaded.");
        }
        return this.metadata.isDynamic();
    }
}