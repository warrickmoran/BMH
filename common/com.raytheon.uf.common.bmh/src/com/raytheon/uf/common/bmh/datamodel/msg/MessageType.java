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
package com.raytheon.uf.common.bmh.datamodel.msg;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.raytheon.uf.common.bmh.datamodel.language.TtsVoice;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * MessageType record object. Represents the various type of data flowing
 * through BMH. Loosely tied to the concept of an AFOSID and in some cases is an
 * AFOSID, but that is not guaranteed.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 30, 2014 3175       rjpeter     Initial creation
 * 
 * </pre>
 * 
 * @author rjpeter
 * @version 1.0
 */
@Entity
@DynamicSerialize
@Table(name = "message_type", schema = "bmh")
public class MessageType {
    public enum Designation {
        StationID, Forecast, Observation, Outlook, Watch, Warning, Advisory, TimeAnnouncement, Other
    }

    @Id
    @Column(length = 9)
    @DynamicSerializeElement
    private String afosid;

    @Column(length = 20, nullable = false)
    @DynamicSerializeElement
    private String title;

    @Column(nullable = false)
    @DynamicSerializeElement
    private boolean alert;

    @Column(nullable = false)
    @DynamicSerializeElement
    private boolean confirm;

    @Column(nullable = false)
    @DynamicSerializeElement
    private boolean emergencyOverride;

    @Column(nullable = false)
    @DynamicSerializeElement
    private boolean interrupt;

    @Column(nullable = false)
    @DynamicSerializeElement
    private Designation designation;

    @Column(length = 6, nullable = false)
    @DynamicSerializeElement
    private String duration;

    @Column(length = 6, nullable = false)
    @DynamicSerializeElement
    private String periodicity;

    @ManyToOne(optional = false)
    @JoinColumn(name = "voice")
    @DynamicSerializeElement
    private TtsVoice voice;

    @Column(length = 6)
    @DynamicSerializeElement
    private String toneBlackOutStart;

    @Column(length = 6)
    @DynamicSerializeElement
    private String toneBlackOutEnd;

    public String getAfosid() {
        return afosid;
    }

    public void setAfosid(String afosid) {
        this.afosid = afosid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isAlert() {
        return alert;
    }

    public void setAlert(boolean alert) {
        this.alert = alert;
    }

    public boolean isConfirm() {
        return confirm;
    }

    public void setConfirm(boolean confirm) {
        this.confirm = confirm;
    }

    public boolean isEmergencyOverride() {
        return emergencyOverride;
    }

    public void setEmergencyOverride(boolean emergencyOverride) {
        this.emergencyOverride = emergencyOverride;
    }

    public boolean isInterrupt() {
        return interrupt;
    }

    public void setInterrupt(boolean interrupt) {
        this.interrupt = interrupt;
    }

    public Designation getDesignation() {
        return designation;
    }

    public void setDesignation(Designation designation) {
        this.designation = designation;
    }

    public String getToneBlackOutStart() {
        return toneBlackOutStart;
    }

    public void setToneBlackOutStart(String toneBlackOutStart) {
        this.toneBlackOutStart = toneBlackOutStart;
    }

    public String getToneBlackOutEnd() {
        return toneBlackOutEnd;
    }

    public void setToneBlackOutEnd(String toneBlackOutEnd) {
        this.toneBlackOutEnd = toneBlackOutEnd;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getPeriodicity() {
        return periodicity;
    }

    public void setPeriodicity(String periodicity) {
        this.periodicity = periodicity;
    }

    public TtsVoice getVoice() {
        return voice;
    }

    public void setVoice(TtsVoice voice) {
        this.voice = voice;
    }

}
