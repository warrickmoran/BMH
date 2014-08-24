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

import com.raytheon.uf.common.bmh.data.PlaylistDataStructure;
import com.raytheon.uf.common.bmh.datamodel.playlist.Playlist;
import com.raytheon.uf.common.bmh.request.PlaylistRequest;
import com.raytheon.uf.common.bmh.request.PlaylistResponse;
import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.edex.bmh.dao.PlaylistDao;
import com.raytheon.uf.edex.bmh.playlist.PlaylistStateManager;
import com.raytheon.uf.edex.bmh.status.BMHStatusHandler;

/**
 * Playlist request handler.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 15, 2014   3432     mpduff      Initial creation
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class PlaylistHandler implements IRequestHandler<PlaylistRequest> {
    private static final BMHStatusHandler statusHandler = BMHStatusHandler
            .getInstance(PlaylistHandler.class);

    @Override
    public Object handleRequest(PlaylistRequest request) throws Exception {
        PlaylistResponse response = new PlaylistResponse();
        switch (request.getAction()) {
        case GET_PLAYLIST_BY_SUITE_GROUP:
            response = getPlaylistBySuiteAndGroup(request);
            break;
        case GET_PLAYLIST_DATA_FOR_TRANSMITTER:
            statusHandler.info("Playlist data request for "
                    + request.getTransmitterName() + " arrived...");
            PlaylistDataStructure data = PlaylistStateManager.getInstance()
                    .getPlaylistDataStructure(request.getTransmitterName());
            response.setPlaylistData(data);
            break;
        default:
            break;
        }

        return response;
    }

    private PlaylistResponse getPlaylistBySuiteAndGroup(PlaylistRequest request) {
        PlaylistResponse response = new PlaylistResponse();
        PlaylistDao dao = new PlaylistDao();
        Playlist playlist = dao.getBySuiteAndGroupName(request.getSuiteName(),
                request.getGroupName());
        response.setPlaylist(playlist);

        return response;
    }
}
