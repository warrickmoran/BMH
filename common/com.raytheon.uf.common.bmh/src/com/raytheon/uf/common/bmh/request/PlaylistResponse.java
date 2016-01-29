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

import java.util.Map;

import com.raytheon.uf.common.bmh.data.IPlaylistData;
import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Playlist response object
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 15, 2014    3432    mpduff      Initial creation
 * Oct 21, 2014    3655    bkowal      Updated to use {@link IPlaylistData}.
 * Jan 28, 2016 5300       rjpeter     Added broadcastData.
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */
@DynamicSerialize
public class PlaylistResponse {

    @DynamicSerializeElement
    private IPlaylistData playlistData;

    @DynamicSerializeElement
    private Map<BroadcastMsg, MessageType> broadcastData;

    /**
     * @return the playlistData
     */
    public IPlaylistData getPlaylistData() {
        return playlistData;
    }

    /**
     * @param playlistData
     *            the playlistData to set
     */
    public void setPlaylistData(IPlaylistData playlistData) {
        this.playlistData = playlistData;
    }

    /**
     * @return the broadcastData
     */
    public Map<BroadcastMsg, MessageType> getBroadcastData() {
        return broadcastData;
    }

    /**
     * @param broadcastData
     *            the broadcastData to set
     */
    public void setBroadcastData(Map<BroadcastMsg, MessageType> broadcastData) {
        this.broadcastData = broadcastData;
    }

}
