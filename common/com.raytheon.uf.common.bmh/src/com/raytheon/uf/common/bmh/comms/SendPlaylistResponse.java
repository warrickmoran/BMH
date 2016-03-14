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
package com.raytheon.uf.common.bmh.comms;

import com.raytheon.uf.common.bmh.notify.LiveBroadcastSwitchNotification;
import com.raytheon.uf.common.bmh.notify.MaintenanceMessagePlayback;
import com.raytheon.uf.common.bmh.notify.NoPlaybackMessageNotification;
import com.raytheon.uf.common.bmh.notify.PlaylistNotification;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Response to a playlist request.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 14, 2016 5472       rjpeter     Initial creation
 * 
 * </pre>
 * 
 * @author rjpeter
 * @version 1.0
 */
@DynamicSerialize
public class SendPlaylistResponse {
    public static enum ResponseType {
        PLAYLIST, LIVE_BROADCAST, MAINTENANCE, NO_PLAYLIST
    }

    @DynamicSerializeElement
    private String group;

    @DynamicSerializeElement
    private ResponseType type;

    @DynamicSerializeElement
    private PlaylistNotification playlistNotification;

    @DynamicSerializeElement
    private LiveBroadcastSwitchNotification liveBroadcastNotification;

    @DynamicSerializeElement
    private MaintenanceMessagePlayback maintenanceMessage;

    @DynamicSerializeElement
    private NoPlaybackMessageNotification noPlaylistNotification;

    public SendPlaylistResponse() {
    }

    public SendPlaylistResponse(String group, PlaylistNotification notification) {
        this.group = group;
        this.playlistNotification = notification;
        this.type = ResponseType.PLAYLIST;
    }

    public SendPlaylistResponse(String group,
            LiveBroadcastSwitchNotification broadcast) {
        this.group = group;
        this.liveBroadcastNotification = broadcast;
        this.type = ResponseType.LIVE_BROADCAST;
    }

    public SendPlaylistResponse(String group,
            MaintenanceMessagePlayback maintenance) {
        this.group = group;
        this.maintenanceMessage = maintenance;
        this.type = ResponseType.MAINTENANCE;
    }

    public SendPlaylistResponse(String group,
            NoPlaybackMessageNotification notification) {
        this.group = group;
        this.noPlaylistNotification = notification;
        this.type = ResponseType.NO_PLAYLIST;
    }

    /**
     * @return the group
     */
    public String getGroup() {
        return group;
    }

    /**
     * @param group
     *            the group to set
     */
    public void setGroup(String group) {
        this.group = group;
    }

    /**
     * @return the type
     */
    public ResponseType getType() {
        return type;
    }

    /**
     * @param type
     *            the type to set
     */
    public void setType(ResponseType type) {
        this.type = type;
    }

    /**
     * @return the playlistNotification
     */
    public PlaylistNotification getPlaylistNotification() {
        return playlistNotification;
    }

    /**
     * @param playlistNotification
     *            the playlistNotification to set
     */
    public void setPlaylistNotification(
            PlaylistNotification playlistNotification) {
        this.playlistNotification = playlistNotification;
    }

    /**
     * @return the liveBroadcastNotification
     */
    public LiveBroadcastSwitchNotification getLiveBroadcastNotification() {
        return liveBroadcastNotification;
    }

    /**
     * @param liveBroadcastNotification
     *            the liveBroadcastNotification to set
     */
    public void setLiveBroadcastNotification(
            LiveBroadcastSwitchNotification liveBroadcastNotification) {
        this.liveBroadcastNotification = liveBroadcastNotification;
    }

    /**
     * @return the maintenanceMessage
     */
    public MaintenanceMessagePlayback getMaintenanceMessage() {
        return maintenanceMessage;
    }

    /**
     * @param maintenanceMessage
     *            the maintenanceMessage to set
     */
    public void setMaintenanceMessage(
            MaintenanceMessagePlayback maintenanceMessage) {
        this.maintenanceMessage = maintenanceMessage;
    }

    /**
     * @return the noPlaylistNotification
     */
    public NoPlaybackMessageNotification getNoPlaylistNotification() {
        return noPlaylistNotification;
    }

    /**
     * @param noPlaylistNotification
     *            the noPlaylistNotification to set
     */
    public void setNoPlaylistNotification(
            NoPlaybackMessageNotification noPlaylistNotification) {
        this.noPlaylistNotification = noPlaylistNotification;
    }
}
