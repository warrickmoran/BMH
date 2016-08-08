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
 * Feb 03, 2015  4081     bkowal      Fix {@link #isTimePeriodic()}. Removed unused isStatic field.
 * Mar 05, 2015  4222     bkowal      Handle messages that never expire.
 * Mar 13, 2015  4222     bkowal      Prevent NPE for messages that do not expire.
 * Mar 25, 2015  4290     bsteffen    Switch to global replacement.
 * Apr 07, 2015  4293     bkowal      Added get/set methods for {@link #messageBroadcastNotificationSent}.
 * Apr 27, 2015  4397     bkowal      Added {@link #initialRecognitionTime} and {@link #recognized}.
 * May 11, 2015  4002     bkowal      Added {@link #initialBLDelayNotificationSent}.
 * May 13, 2015  4429     rferrel     Added traceId to {@link #toString()}.
 * May 26, 2015  4481     bkowal      Added {@link #dynamic}.
 * Feb 04, 2016  5308     bkowal      Refactored into {@link DacPlaylistMessageMetadata}.
 * Mar 08, 2016  5382     bkowal      Updated to only include information that the Dac Transmit
 *                                    would manage.
 * Aug 04, 2016  5766     bkowal      Keep track of the {@link #remainingCycles}. Added {@link #isTimePeriodic()}
 *                                    and {@link #isCyclePeriodic()}.
 * 
 * </pre>
 * 
 * @author bsteffen
 */
@XmlRootElement(name = "bmhMessage")
@XmlAccessorType(XmlAccessType.NONE)
public class DacPlaylistMessage extends DacPlaylistMessageId implements
        IMessageMetadataAccess {

    @XmlElement
    private Calendar lastTransmitTime;

    @XmlElement
    private int playCount;

    @XmlElement
    private boolean playedSameTone;

    @XmlElement
    private boolean playedAlertTone;

    /**
     * When cycle-based periodicity is in use, this quantity will keep track of
     * the remaining cycles that must be completed until this message can be
     * broadcast. The quantity will initially be set to the number of cycles
     * associated with the message. Every cycle, the quantity will be
     * decremented by 1. When the quantity reaches 0, the message will be
     * broadcast and the quantity will be reset to the number of cycles
     * associated with the message.
     */
    @XmlElement
    private Integer remainingCycles;

    /**
     * boolean flag used to mark when a {@link MessageBroadcastNotifcation} is
     * sent for this message to ensure that multiple notifications are never
     * sent. This field is theoretically transient and will only hold its state
     * for as long as this object is in memory. This flag is necessary because
     * an expired {@link DacPlaylistMessage} will only be eliminated (the
     * playlist scheduler will keep iterating over it until then) when a newer
     * version of the containing playlist is read.
     */
    private boolean messageBroadcastNotificationSent;

    /**
     * boolean used to ensure that once one delay notification is sent out for a
     * single {@link DacPlaylistMessage} that may be associated with more than
     * one delay scenario.
     */
    private transient boolean initialBLDelayNotificationSent;

    private transient volatile DacPlaylistMessageMetadata metadata;

    public DacPlaylistMessage() {

    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DACPlaylistMessage [broadcastId=").append(broadcastId);
        builder.append(", messageType=");
        if (this.metadata != null) {
            builder.append(this.metadata.getMessageType());
        }
        builder.append(", traceId=").append(traceId).append(", expire=");
        if (this.expire != null) {
            builder.append(expire.getTime().toString());
        }
        builder.append("]");
        return builder.toString();
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

    public Integer getRemainingCycles() {
        return remainingCycles;
    }

    public void setRemainingCycles(Integer remainingCycles) {
        this.remainingCycles = remainingCycles;
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
        boolean started = currentTime >= this.metadata.getStart()
                .getTimeInMillis();
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
        boolean hasTonesToPlay = ((isSAMETones() && !playedSameTone) || (this.metadata
                .isAlertTone() && !playedAlertTone));
        final boolean toneBlackoutEnabled = this.metadata
                .isToneBlackoutEnabled();
        boolean outsideBlackoutPeriod = (hasTonesToPlay && toneBlackoutEnabled) ? this.metadata
                .isOutsideBlackoutPeriod(time) : false;

        if (!toneBlackoutEnabled && hasTonesToPlay) {
            return true;
        } else if (toneBlackoutEnabled && hasTonesToPlay
                && outsideBlackoutPeriod) {
            return true;
        }

        return false;
    }

    public boolean requiresExpirationNoPlaybackNotification() {
        boolean expired = this.expire != null
                && TimeUtil.currentTimeMillis() >= this.expire
                        .getTimeInMillis();

        boolean result = expired
                && (this.messageBroadcastNotificationSent == false)
                && (this.metadata.isWarning() || this.metadata.isWatch())
                && (this.playCount == 0);
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
    public DacPlaylistMessageMetadata getMetadata() {
        return metadata;
    }

    /**
     * @param metadata
     *            the metadata to set
     */
    public void setMetadata(DacPlaylistMessageMetadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public String getName() {
        return this.metadata.getName();
    }

    @Override
    public String getMessageType() {
        return this.metadata.getMessageType();
    }

    @Override
    public String getSAMEtone() {
        return this.metadata.getSAMEtone();
    }

    @Override
    public boolean isAlertTone() {
        return this.metadata.isAlertTone();
    }

    @Override
    public boolean isSAMETones() {
        return this.metadata.isSAMETones();
    }

    @Override
    public boolean isPeriodic() {
        return (isTimePeriodic() || isCyclePeriodic());
    }

    @Override
    public boolean isTimePeriodic() {
        return this.metadata.isTimePeriodic();
    }

    @Override
    public boolean isCyclePeriodic() {
        return metadata.isCyclePeriodic();
    }

    @Override
    public long getPlaybackInterval() {
        return this.metadata.getPlaybackInterval();
    }

    @Override
    public List<String> getSoundFiles() {
        return this.metadata.getSoundFiles();
    }

    @Override
    public boolean isConfirm() {
        return this.metadata.isConfirm();
    }

    @Override
    public boolean isWarning() {
        return this.metadata.isWarning();
    }

    @Override
    public boolean isDynamic() {
        return this.metadata.isDynamic();
    }
}