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
package com.raytheon.uf.common.bmh.notify;

import java.util.Calendar;

import com.raytheon.uf.common.bmh.broadcast.LiveBroadcastStartCommand.BROADCASTTYPE;
import com.raytheon.uf.common.bmh.data.PlaylistDataStructure;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Used to notify the Playlist State Manager and any listening Broadcast Cycle
 * dialogs that a live broadcast is now playing on a particular transmitter.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 21, 2014 3655       bkowal      Initial creation
 * Oct 27, 2014 3712       bkowal      Added broadcastState.
 * Nov 17, 2014 3808       bkowal      Support broadcast live.
 * Nov 21, 2014 3845       bkowal      Re-factor/cleanup
 * Feb 10, 2015 4106       bkowal      Added {@link #actualPlaylist}.
 * Apr 29, 2015 4394       bkowal      Implement {@link INonStandardBroadcast}.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

@DynamicSerialize
public class LiveBroadcastSwitchNotification implements INonStandardBroadcast {

    public static enum STATE {
        STARTED, FINISHED
    }

    @DynamicSerializeElement
    private BROADCASTTYPE type;

    @DynamicSerializeElement
    private STATE broadcastState;

    @DynamicSerializeElement
    private TransmitterGroup transmitterGroup;

    @DynamicSerializeElement
    private Calendar transitTime;

    @DynamicSerializeElement
    private String messageId;

    @DynamicSerializeElement
    private String messageTitle;

    @DynamicSerializeElement
    private String messageName;

    @DynamicSerializeElement
    private String mrd = "-";

    @DynamicSerializeElement
    private String expirationTime;

    @DynamicSerializeElement
    private String sameTone;

    @DynamicSerializeElement
    private String alertTone;

    @DynamicSerializeElement
    private String playCount = "1";

    @DynamicSerializeElement
    private PlaylistDataStructure actualPlaylist;

    /**
     * Default Constructor for {@link DynamicSerialize}.
     */
    public LiveBroadcastSwitchNotification() {
    }

    /**
     * Copy Constructor.
     * 
     * @param notification
     *            the {@link LiveBroadcastSwitchNotification} to copy.
     * @param actualPlaylist
     *            the {@link PlaylistDataStructure} to copy.
     */
    public LiveBroadcastSwitchNotification(
            final LiveBroadcastSwitchNotification notification,
            final PlaylistDataStructure actualPlaylist) {
        this.type = notification.type;
        this.broadcastState = notification.broadcastState;
        this.transmitterGroup = notification.transmitterGroup;
        this.transitTime = notification.transitTime;
        this.messageId = notification.messageId;
        this.messageTitle = notification.messageTitle;
        this.messageName = notification.messageName;
        this.mrd = notification.mrd;
        this.expirationTime = notification.expirationTime;
        this.sameTone = notification.sameTone;
        this.alertTone = notification.alertTone;
        this.playCount = notification.playCount;
        this.actualPlaylist = actualPlaylist;
    }

    /**
     * @return the type
     */
    public BROADCASTTYPE getType() {
        return type;
    }

    /**
     * @param type
     *            the type to set
     */
    public void setType(BROADCASTTYPE type) {
        this.type = type;
    }

    /**
     * @return the broadcastState
     */
    public STATE getBroadcastState() {
        return broadcastState;
    }

    /**
     * @param broadcastState
     *            the broadcastState to set
     */
    public void setBroadcastState(STATE broadcastState) {
        this.broadcastState = broadcastState;
    }

    /**
     * @return the transmitterGroup
     */
    public TransmitterGroup getTransmitterGroup() {
        return transmitterGroup;
    }

    /**
     * @param transmitterGroup
     *            the transmitterGroup to set
     */
    public void setTransmitterGroup(TransmitterGroup transmitterGroup) {
        this.transmitterGroup = transmitterGroup;
    }

    /**
     * @return the transitTime
     */
    public Calendar getTransitTime() {
        return transitTime;
    }

    /**
     * @param transitTime
     *            the transitTime to set
     */
    public void setTransitTime(Calendar transitTime) {
        this.transitTime = transitTime;
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
     * @return the messageName
     */
    public String getMessageName() {
        return messageName;
    }

    /**
     * @param messageName
     *            the messageName to set
     */
    public void setMessageName(String messageName) {
        this.messageName = messageName;
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
    public String getExpirationTime() {
        return expirationTime;
    }

    /**
     * @param expirationTime
     *            the expirationTime to set
     */
    public void setExpirationTime(String expirationTime) {
        this.expirationTime = expirationTime;
    }

    /**
     * @return the sameTone
     */
    public String getSameTone() {
        return sameTone;
    }

    /**
     * @param sameTone
     *            the sameTone to set
     */
    public void setSameTone(String sameTone) {
        this.sameTone = sameTone;
    }

    /**
     * @return the alertTone
     */
    public String getAlertTone() {
        return alertTone;
    }

    /**
     * @param alertTone
     *            the alertTone to set
     */
    public void setAlertTone(String alertTone) {
        this.alertTone = alertTone;
    }

    /**
     * @return the playCount
     */
    public String getPlayCount() {
        return playCount;
    }

    /**
     * @param playCount
     *            the playCount to set
     */
    public void setPlayCount(String playCount) {
        this.playCount = playCount;
    }

    /**
     * @return the actualPlaylist
     */
    public PlaylistDataStructure getActualPlaylist() {
        return actualPlaylist;
    }

    /**
     * @param actualPlaylist
     *            the actualPlaylist to set
     */
    public void setActualPlaylist(PlaylistDataStructure actualPlaylist) {
        this.actualPlaylist = actualPlaylist;
    }
}