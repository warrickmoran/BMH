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
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
@XmlAccessorType(XmlAccessType.NONE)
public class DACPlaylistMessage {

    @XmlElement
    private long broadcastId;

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

    public DACPlaylistMessage() {

    }

    public long getBroadcastId() {
        return broadcastId;
    }

    public void setBroadcastId(long broadcastId) {
        this.broadcastId = broadcastId;
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

}
