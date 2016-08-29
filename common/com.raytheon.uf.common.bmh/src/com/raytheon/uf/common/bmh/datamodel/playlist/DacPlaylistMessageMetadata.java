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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.raytheon.uf.common.bmh.datamodel.msg.InputMessage;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.time.util.TimeUtil;

/**
 * POJO containing the metadata for a message. The metadata consists of the
 * message contents as well as fields that can be changed from one revision of
 * the message to the next. Also includes fields that only exist to fulfill
 * statistics requirements.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 03, 2016  5308      bkowal      Initial creation
 * Mar 08, 2016  5382      bkowal      Updated to only include information that BMH EDEX
 *                                     would manage.
 * Mar 24, 2016  5515      bkowal      Added {@link #lastReadTime}.
 * Apr 26, 2016  5561      bkowal      Make {@link DynamicSerialize}able.
 * Jul 29, 2016  5766      bkowal      Centralized the default/no periodicity constant.
 * Aug 04, 2016  5766      bkowal      Added {@link #cycles}.
 * 
 * </pre>
 * 
 * @author bkowal
 */
@XmlRootElement(name = "bmhMessageMetadata")
@XmlAccessorType(XmlAccessType.NONE)
@DynamicSerialize
public class DacPlaylistMessageMetadata extends DacPlaylistMessageId {

    @XmlElement
    @DynamicSerializeElement
    private String name;

    @XmlElement
    @DynamicSerializeElement
    private Calendar start;

    @XmlElement
    @DynamicSerializeElement
    private String messageType;

    @XmlElement
    @DynamicSerializeElement
    private String SAMEtone;

    @XmlElement
    @DynamicSerializeElement
    private boolean alertTone;

    @XmlElement
    @DynamicSerializeElement
    private boolean toneBlackoutEnabled;

    @XmlElement
    @DynamicSerializeElement
    private String toneBlackoutStart;

    @XmlElement
    @DynamicSerializeElement
    private String toneBlackoutEnd;

    /** format is DDHHMMSS */
    @XmlElement
    @DynamicSerializeElement
    private String periodicity;

    @XmlElement
    @DynamicSerializeElement
    private Integer cycles;

    @XmlElement(name = "soundFile")
    @DynamicSerializeElement
    private List<String> soundFiles;

    @XmlElement
    @DynamicSerializeElement
    private String messageText;

    /*
     * boolean indicating whether or not the confirm flag has been set on the
     * associated message.
     */
    @XmlElement
    @DynamicSerializeElement
    private boolean confirm;

    /*
     * boolean indicating that this message is a watch. Requirements state that
     * BMH users must be notified when a watch/warning is not broadcast due to
     * expiration even though it had been scheduled for broadcast. Set based on
     * the message type designation.
     */
    @XmlElement
    @DynamicSerializeElement
    private boolean watch;

    /*
     * boolean indicating that this message is a warning. Requirements state
     * that BMH users must be notified when a watch/warning is not broadcast due
     * to expiration even though it had been scheduled for broadcast. Set based
     * on the message type designation.
     */
    @XmlElement
    @DynamicSerializeElement
    private boolean warning;

    /**
     * {@link #initialRecognitionTime} and {@link #recognized} only exist to
     * fulfill the statistics requirements. The {@link #initialRecognitionTime}
     * represents the time that the {@link InputMessage} was last recognized for
     * processing. The word last is used because a message can be processed more
     * than once as a result of the in-place edit. The {@link #recognized}
     * exists to ensure that a version of the message will only be recognized
     * once.
     */
    @XmlElement
    @DynamicSerializeElement
    private long initialRecognitionTime;

    @XmlElement
    @DynamicSerializeElement
    private boolean recognized = false;

    private transient boolean dynamic;

    /*
     * Used to keep track of when this message was last read during message
     * retrieval and cache population.
     */
    private transient long lastReadTime;

    public DacPlaylistMessageMetadata() {
    }

    public DacPlaylistMessageMetadata(final DacPlaylistMessage message) {
        super(message.getBroadcastId());
    }

    /**
     * Returns whether this message has a valid SAME tone header.
     * 
     * @return Whether or not this message has a valid SAME tone header.
     */
    public boolean isSAMETones() {
        return ((SAMEtone != null) && (!SAMEtone.isEmpty()));
    }

    protected boolean isOutsideBlackoutPeriod(Calendar time) {
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

    public boolean isTimePeriodic() {
        return periodicity != null
                && !periodicity.isEmpty()
                && MessageType.DEFAULT_NO_PERIODICITY.equals(periodicity) == false;
    }

    public boolean isCyclePeriodic() {
        return !isTimePeriodic() && cycles != null;
    }

    public long getPlaybackInterval() {
        if (isTimePeriodic()) {
            int days = Integer.parseInt(periodicity.substring(0, 2));
            int hours = Integer.parseInt(periodicity.substring(2, 4));
            int minutes = Integer.parseInt(periodicity.substring(4, 6));
            int seconds = Integer.parseInt(periodicity.substring(6, 8));
            return (seconds + (60 * (minutes + (60 * (hours + (24 * days))))))
                    * TimeUtil.MILLIS_PER_SECOND;
        }
        return -1;
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

    public Calendar getStart() {
        return this.start;
    }

    public void setStart(Calendar start) {
        this.start = start;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
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

    public String getPeriodicity() {
        return this.periodicity;
    }

    /**
     * @param periodicity
     *            the periodicity to set
     */
    public void setPeriodicity(String periodicity) {
        this.periodicity = periodicity;
    }

    public Integer getCycles() {
        return cycles;
    }

    public void setCycles(Integer cycles) {
        this.cycles = cycles;
    }

    public List<String> getSoundFiles() {
        return this.soundFiles;
    }

    public void addSoundFile(String soundFile) {
        if (this.soundFiles == null) {
            this.soundFiles = new ArrayList<>(1);
        }
        this.soundFiles.add(soundFile);
    }

    /**
     * @param soundFiles
     *            the soundFiles to set
     */
    public void setSoundFiles(List<String> soundFiles) {
        this.soundFiles = soundFiles;
    }

    public String getMessageText() {
        return this.messageText;
    }

    /**
     * @param messageText
     *            the messageText to set
     */
    public void setMessageText(String messageText) {
        this.messageText = messageText;
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

    /**
     * @return the initialRecognitionTime
     */
    public long getInitialRecognitionTime() {
        return initialRecognitionTime;
    }

    /**
     * @param initialRecognitionTime
     *            the initialRecognitionTime to set
     */
    public void setInitialRecognitionTime(long initialRecognitionTime) {
        this.initialRecognitionTime = initialRecognitionTime;
    }

    /**
     * @return the recognized
     */
    public boolean isRecognized() {
        return recognized;
    }

    /**
     * @param recognized
     *            the recognized to set
     */
    public void setRecognized(boolean recognized) {
        this.recognized = recognized;
    }

    /**
     * @return the dynamic
     */
    public boolean isDynamic() {
        return dynamic;
    }

    /**
     * @param dynamic
     *            the dynamic to set
     */
    public void setDynamic(boolean dynamic) {
        this.dynamic = dynamic;
    }

    public long getLastReadTime() {
        return lastReadTime;
    }

    public void setLastReadTime(long lastReadTime) {
        this.lastReadTime = lastReadTime;
    }
}