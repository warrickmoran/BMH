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
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.raytheon.uf.common.bmh.datamodel.msg.InputMessage;
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
 * Feb 3, 2016  5308       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */
@XmlRootElement(name = "bmhMessageMetadata")
@XmlAccessorType(XmlAccessType.NONE)
public class DacPlaylistMessageMetadata extends DacPlaylistMessageId {

    /*
     * indicates a message does not have a set periodicity.
     */
    private static final String NO_PERIODICTY = "00000000";

    /** format is DDHHMMSS */
    @XmlElement
    private String periodicity;

    @XmlElement(name = "soundFile")
    private List<String> soundFiles;

    @XmlElement
    private String messageText;

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
    private long initialRecognitionTime;

    @XmlElement
    private boolean recognized = false;

    private transient boolean dynamic;

    public DacPlaylistMessageMetadata() {
    }

    public DacPlaylistMessageMetadata(final DacPlaylistMessage message) {
        super(message.getBroadcastId());
    }

    public boolean isPeriodic() {
        return periodicity != null && !periodicity.isEmpty()
                && NO_PERIODICTY.equals(periodicity) == false;
    }

    public long getPlaybackInterval() {
        if (isPeriodic()) {
            int days = Integer.parseInt(periodicity.substring(0, 2));
            int hours = Integer.parseInt(periodicity.substring(2, 4));
            int minutes = Integer.parseInt(periodicity.substring(4, 6));
            int seconds = Integer.parseInt(periodicity.substring(6, 8));
            return (seconds + (60 * (minutes + (60 * (hours + (24 * days))))))
                    * TimeUtil.MILLIS_PER_SECOND;
        }
        return -1;
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
}