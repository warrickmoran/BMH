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
package com.raytheon.uf.edex.bmh.handler;

import java.util.Map;
import java.util.Set;

import com.raytheon.uf.common.bmh.data.IPlaylistData;
import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.common.bmh.datamodel.playlist.Playlist;
import com.raytheon.uf.common.bmh.request.PlaylistRequest;
import com.raytheon.uf.common.bmh.request.PlaylistResponse;
import com.raytheon.uf.edex.bmh.msg.logging.IMessageLogger;
import com.raytheon.uf.edex.bmh.playlist.PlaylistStateManager;

/**
 * Handles any requests to get or modify the state of {@link Playlist}s
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Aug 15, 2014  3432     mpduff      Initial creation
 * Oct 07, 2014  3687     bsteffen    Handle non-operational requests.
 * Oct 13, 2014  3413     rferrel     Implement User roles.
 * Oct 21, 2014  3655     bkowal      Updated to use {@link IPlaylistData}.
 * Jan 06, 2015  3651     bkowal      Support AbstractBMHPersistenceLoggingDao.
 * Jan 28, 2016  5300     rjpeter     Handle GET_PLAYLIST_DATA_FOR_IDS.
 * Feb 04, 2016  5308     rjpeter     Removed GET_PLAYLIST_DATA_FOR_TRANSMITTER.
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class PlaylistHandler extends
        AbstractBMHLoggingServerRequestHandler<PlaylistRequest> {

    private PlaylistStateManager playlistStateManager;

    private PlaylistStateManager practicePlaylistStateManager;

    public PlaylistHandler(IMessageLogger opMessageLogger,
            IMessageLogger pracMessageLogger) {
        super(opMessageLogger, pracMessageLogger);
    }

    @Override
    public Object handleRequest(PlaylistRequest request) {
        PlaylistResponse response = null;
        switch (request.getAction()) {
        case GET_PLAYLIST_DATA_FOR_IDS:
            response = getPlaylistDataForIds(request);
            break;
        default:
            throw new UnsupportedOperationException(this.getClass()
                    .getSimpleName()
                    + " cannot handle action "
                    + request.getAction());
        }
        return response;
    }

    private PlaylistResponse getPlaylistDataForIds(PlaylistRequest request) {
        PlaylistResponse response = new PlaylistResponse();
        Set<Long> ids = request.getBroadcastIds();
        PlaylistStateManager playlistState = getPlaylistState(request
                .isOperational());
        Map<BroadcastMsg, MessageType> broadcastData = playlistState
                .getBroadcastData(ids);
        response.setBroadcastData(broadcastData);
        return response;
    }

    private PlaylistStateManager getPlaylistState(boolean operational) {
        PlaylistStateManager playlistState = null;
        if (operational) {
            playlistState = playlistStateManager;
        } else {
            playlistState = practicePlaylistStateManager;
        }
        if (playlistState == null) {
            if (operational) {
                throw new IllegalStateException(
                        "No operational playlist state manager is available for handling playlist requests. ");
            } else {
                throw new IllegalStateException(
                        "No non-operational playlist state manager is available for handling playlist requests. ");
            }

        }

        return playlistState;
    }

    public void setPlaylistStateManager(
            PlaylistStateManager playlistStateManager) {
        this.playlistStateManager = playlistStateManager;
    }

    public PlaylistStateManager setPracticePlaylistStateManager(
            PlaylistStateManager practicePlaylistStateManager) {
        this.practicePlaylistStateManager = practicePlaylistStateManager;
        return practicePlaylistStateManager;
    }

}
