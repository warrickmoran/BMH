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
package com.raytheon.uf.edex.bmh.dactransmit.playlist;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Queue;

import javax.xml.bind.JAXB;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raytheon.uf.common.bmh.datamodel.playlist.DACPlaylist;
import com.raytheon.uf.common.bmh.datamodel.playlist.DACPlaylistMessage;
import com.raytheon.uf.common.bmh.datamodel.playlist.PlaylistUpdateNotification;
import com.raytheon.uf.common.time.util.TimeUtil;

/**
 * Manages playback order of playlist and playlist messages for the current
 * {@code DacSession}.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 14, 2014  #3286     dgilling     Initial creation
 * 
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

public final class PlaylistScheduler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final PathMatcher XML_MATCHER = FileSystems.getDefault()
            .getPathMatcher("glob:**.xml");

    private static final DirectoryStream.Filter<Path> PLAYLIST_FILTER = new DirectoryStream.Filter<Path>() {

        @Override
        public boolean accept(Path entry) throws IOException {
            if (XML_MATCHER.matches(entry)) {
                /*
                 * we're building a "path" string containing
                 * <transmitter_group>/<playlist_filename>, which is what the
                 * parser requires to figure out the DACPlaylist components.
                 */
                StringBuilder fileString = new StringBuilder(entry.getParent()
                        .getFileName().toString()).append(File.separatorChar)
                        .append(entry.getFileName().toString());
                DACPlaylist temp = PlaylistUpdateNotification
                        .parseFilePath(fileString);
                long currentTime = TimeUtil.currentTimeMillis();
                return ((currentTime >= temp.getStart().getTimeInMillis()) && (currentTime < temp
                        .getExpired().getTimeInMillis()));
            }

            return false;
        }
    };

    private static final Comparator<DACPlaylist> PLAYBACK_ORDER = new Comparator<DACPlaylist>() {

        @Override
        public int compare(DACPlaylist o1, DACPlaylist o2) {
            int retVal = 0 - Integer
                    .compare(o1.getPriority(), o2.getPriority());
            if (retVal != 0) {
                return retVal;
            }

            retVal = 0 - o1.getCreationTime().compareTo(o2.getCreationTime());
            return retVal;
        }
    };

    private final PlaylistMessageCache cache;

    private List<DACPlaylist> currentPlaylists;

    private Queue<DACPlaylistMessage> currentMessages;

    private int position;

    /**
     * Reads the specified directory for valid playlist files (ones that have
     * not already passed their expiration time) and sorts them into playback
     * order for DAC transmission. Asynchronously queues each message from each
     * playlist into the {@code PlaylistMessageCache}.
     * 
     * @param inputDirectory
     *            Directory containing playlists.
     * @throws IOException
     *             If any I/O errors occur attempting to get the list of
     *             playlist files from the specified directory.
     */
    public PlaylistScheduler(Path inputDirectory) throws IOException {
        currentPlaylists = new ArrayList<>();
        position = 0;

        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(
                inputDirectory, PLAYLIST_FILTER)) {
            for (Path entry : dirStream) {
                DACPlaylist playlist = JAXB.unmarshal(entry.toFile(),
                        DACPlaylist.class);
                currentPlaylists.add(playlist);
            }
        }

        if (currentPlaylists.isEmpty()) {
            throw new IllegalArgumentException(
                    "Input directory contained no valid playlist files.");
        }

        Collections.sort(currentPlaylists, PLAYBACK_ORDER);

        cache = new PlaylistMessageCache();
        for (DACPlaylist playlist : currentPlaylists) {
            cache.addToCache(playlist.getMessages());
        }

        DACPlaylist firstPlaylist = currentPlaylists.get(0);
        logger.debug("Starting with playlist: "
                + PlaylistUpdateNotification.getFilePath(firstPlaylist));
        currentMessages = new ArrayDeque<>(firstPlaylist.getMessages());
    }

    /**
     * Retrieves the next audio file to play.
     * 
     * @return {@code AudioFileBuffer} containing the contents of the next audio
     *         file to play.
     */
    public AudioFileBuffer next() {
        DACPlaylistMessage nextMessage = nextMessage();
        logger.debug("Switching to message: " + nextMessage.getBroadcastId()
                + ", " + nextMessage.getMessageType());
        return cache.get(nextMessage);
    }

    private DACPlaylistMessage nextMessage() {
        if (!currentMessages.isEmpty()) {
            return currentMessages.remove();
        }

        if ((position + 1) < currentPlaylists.size()) {
            position++;
        } else {
            position = 0;
        }
        DACPlaylist newPlaylist = currentPlaylists.get(position);
        logger.debug("Switching to playlist: "
                + PlaylistUpdateNotification.getFilePath(newPlaylist));
        currentMessages = new ArrayDeque<>(newPlaylist.getMessages());
        return currentMessages.remove();
    }
}
