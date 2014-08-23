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
package com.raytheon.uf.viz.bmh.ui.dialogs.broadcastcycle;

import java.util.Calendar;

import org.eclipse.swt.graphics.Color;

import com.raytheon.uf.common.bmh.datamodel.msg.InputMessage;

/**
 * Broadcast cycle table data entry object.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 21, 2014     3432   mpduff      Initial creation
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class BroadcastCycleTableDataEntry {

    private Calendar transmitTime;

    private String messageId;

    private String messageTitle;

    private String mrd;

    private Calendar expirationTime;

    private boolean alertSent;

    private boolean sameSent;

    private int playCount;

    private long broadcastId;

    private Color transmitTimeColor;

    private InputMessage inputMsg;

    /**
     * @return the transmitTime
     */
    public Calendar getTransmitTime() {
        return transmitTime;
    }

    /**
     * @param transmitTime
     *            the transmitTime to set
     */
    public void setTransmitTime(Calendar transmitTime) {
        this.transmitTime = transmitTime;
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
     * @return the mrd
     */
    public String getMrd() {
        return mrd;
    }

    /**
     * @param mrd
     *            the mrd to set
     */
    public void setMrd(String mrd) {
        this.mrd = mrd;
    }

    /**
     * @return the expirationTime
     */
    public Calendar getExpirationTime() {
        return expirationTime;
    }

    /**
     * @param expirationTime
     *            the expirationTime to set
     */
    public void setExpirationTime(Calendar expirationTime) {
        this.expirationTime = expirationTime;
    }

    /**
     * @return the alertSent
     */
    public boolean isAlertSent() {
        return alertSent;
    }

    /**
     * @param alertSent
     *            the alertSent to set
     */
    public void setAlertSent(boolean alertSent) {
        this.alertSent = alertSent;
    }

    /**
     * @return the sameSent
     */
    public boolean isSameSent() {
        return sameSent;
    }

    /**
     * @param sameSent
     *            the sameSent to set
     */
    public void setSameSent(boolean sameSent) {
        this.sameSent = sameSent;
    }

    /**
     * @return the playCount
     */
    public int getPlayCount() {
        return playCount;
    }

    /**
     * @param playCount
     *            the playCount to set
     */
    public void setPlayCount(int playCount) {
        this.playCount = playCount;
    }

    /**
     * @return the broadcastId
     */
    public long getBroadcastId() {
        return broadcastId;
    }

    /**
     * @param broadcastId
     *            the broadcastId to set
     */
    public void setBroadcastId(long broadcastId) {
        this.broadcastId = broadcastId;
    }

    /**
     * @return the transmitTimeColor
     */
    public Color getTransmitTimeColor() {
        return transmitTimeColor;
    }

    /**
     * @param transmitTimeColor
     *            the transmitTimeColor to set
     */
    public void setTransmitTimeColor(Color transmitTimeColor) {
        this.transmitTimeColor = transmitTimeColor;
    }

    /**
     * @return the inputMsg
     */
    public InputMessage getInputMsg() {
        return inputMsg;
    }

    /**
     * @param inputMsg
     *            the inputMsg to set
     */
    public void setInputMsg(InputMessage inputMsg) {
        this.inputMsg = inputMsg;
    }
}
