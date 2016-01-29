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
package com.raytheon.uf.common.bmh.request;

import java.util.Set;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Playlist request object.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Aug 15, 2014  3432     mpduff      Initial creation
 * Oct 07, 2014  3687     bsteffen    Extend AbstractBMHServerRequest
 * Jan 28, 2016  5300     rjpeter     Added GET_PLAYLIST_DATA_FOR_IDS
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */
@DynamicSerialize
public class PlaylistRequest extends AbstractBMHServerRequest {
    public enum PlaylistAction {
        GET_PLAYLIST_DATA_FOR_TRANSMITTER, GET_PLAYLIST_DATA_FOR_IDS
    }

    @DynamicSerializeElement
    private PlaylistAction action;

    @DynamicSerializeElement
    private String transmitterName;

    @DynamicSerializeElement
    private Set<Long> broadcastIds;

    /**
     * @return the action
     */
    public PlaylistAction getAction() {
        return action;
    }

    /**
     * @param action
     *            the action to set
     */
    public void setAction(PlaylistAction action) {
        this.action = action;
    }

    /**
     * @return the transmitterName
     */
    public String getTransmitterName() {
        return transmitterName;
    }

    /**
     * @param transmitterName
     *            the transmitterName to set
     */
    public void setTransmitterName(String transmitterName) {
        this.transmitterName = transmitterName;
    }

    /**
     * @return the broadcastIds
     */
    public Set<Long> getBroadcastIds() {
        return broadcastIds;
    }

    /**
     * @param broadcastIds
     *            the broadcastIds to set
     */
    public void setBroadcastIds(Set<Long> broadcastIds) {
        this.broadcastIds = broadcastIds;
    }

}
