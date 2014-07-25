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
import java.util.Iterator;
import java.util.List;
import java.util.Queue;

import javax.xml.bind.JAXB;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylist;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessage;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessageId;
import com.raytheon.uf.common.bmh.datamodel.playlist.PlaylistUpdateNotification;
import com.raytheon.uf.common.time.util.ITimer;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.edex.bmh.dactransmit.events.InterruptMessageReceivedEvent;
import com.raytheon.uf.edex.bmh.dactransmit.events.handlers.IPlaylistUpdateNotificationHandler;

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
 * Jul 16, 2014  #3286     dgilling     Fix nextMessage() logic, add shutdown(),
 *                                      wire to event bus for playlist 
 *                                      notifications.
 * Jul 24, 2014  #3286     dgilling     Add support for interrupt messages.
 * 
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

public final class PlaylistScheduler implements
        IPlaylistUpdateNotificationHandler {

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
                 * parser requires to figure out the DacPlaylist components.
                 */
                StringBuilder fileString = new StringBuilder(entry.getParent()
                        .getFileName().toString()).append(File.separatorChar)
                        .append(entry.getFileName().toString());
                DacPlaylist temp = PlaylistUpdateNotification
                        .parseFilePath(fileString);
                if (temp != null) {
                    long currentTime = TimeUtil.currentTimeMillis();
                    return ((currentTime >= temp.getStart().getTimeInMillis()) && (currentTime < temp
                            .getExpired().getTimeInMillis()));
                }
            }

            return false;
        }
    };

    private static final Comparator<DacPlaylist> PLAYBACK_ORDER = new Comparator<DacPlaylist>() {

        @Override
        public int compare(DacPlaylist o1, DacPlaylist o2) {
            int retVal = 0 - Integer
                    .compare(o1.getPriority(), o2.getPriority());
            if (retVal != 0) {
                return retVal;
            }

            retVal = 0 - o1.getStart().compareTo(o2.getStart());
            return retVal;
        }
    };

    private final Path playlistDirectory;

    private final PlaylistMessageCache cache;

    private List<DacPlaylist> currentPlaylists;

    private List<DacPlaylistMessageId> currentMessages;

    /**
     * Unplayed interrupt messages. We use a Queue because the playlist
     * associated with an interrupt is played only one time and discarded.
     */
    private final Queue<DacPlaylist> interrupts;

    // TODO: Is this always going to be 0?? If so, this is probably not needed.
    private int playlistIndex;

    /**
     * Always points to the index of the next message to play from
     * currentMessages. Subtract one to get currently playing message.
     */
    private int messageIndex;

    private final Object playlistMessgeLock;

    private final EventBus eventBus;

    /**
     * Reads the specified directory for valid playlist files (ones that have
     * not already passed their expiration time) and sorts them into playback
     * order for DAC transmission. Asynchronously queues each message from each
     * playlist into the {@code PlaylistMessageCache}.
     * 
     * @param inputDirectory
     *            Directory containing playlists.
     * @param eventBus
     *            Reference back to the application-wide {@code EventBus}
     *            instance for posting and receiving necessary status events.
     * @throws IOException
     *             If any I/O errors occur attempting to get the list of
     *             playlist files from the specified directory.
     */
    public PlaylistScheduler(Path inputDirectory, EventBus eventBus)
            throws IOException {
        this.playlistDirectory = inputDirectory;
        this.eventBus = eventBus;

        this.currentPlaylists = new ArrayList<>();
        this.playlistIndex = 0;
        this.messageIndex = 0;

        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(
                this.playlistDirectory, PLAYLIST_FILTER)) {
            for (Path entry : dirStream) {
                DacPlaylist playlist = JAXB.unmarshal(entry.toFile(),
                        DacPlaylist.class);
                currentPlaylists.add(playlist);
            }
        }

        if (currentPlaylists.isEmpty()) {
            throw new IllegalArgumentException(
                    "Input directory contained no valid playlist files.");
        }

        Collections.sort(currentPlaylists, PLAYBACK_ORDER);

        cache = new PlaylistMessageCache(inputDirectory.resolve("messages"));
        for (DacPlaylist playlist : currentPlaylists) {
            cache.addToCache(playlist.getMessages());
        }

        this.interrupts = new ArrayDeque<>();
        /*
         * On startup we may have some unplayed interrupts. find them and
         * transfer them to interrupt queue
         */
        for (Iterator<DacPlaylist> iter = currentPlaylists.iterator(); iter
                .hasNext();) {
            DacPlaylist playlist = iter.next();
            if (playlist.isInterrupt()) {
                this.interrupts.add(playlist);
                iter.remove();
            }
        }

        DacPlaylist firstPlaylist = currentPlaylists.get(0);
        logger.debug("Starting with playlist: " + firstPlaylist.toString());
        this.currentMessages = firstPlaylist.getMessages();

        this.playlistMessgeLock = new Object();
    }

    /**
     * Shuts down any background threads this class uses for processing.
     */
    public void shutdown() {
        cache.shutdown();
    }

    /**
     * Retrieves the next audio file to play.
     * 
     * @return {@code DacMessagePlaybackData} containing the data about the next
     *         file to play.
     */
    public DacMessagePlaybackData next() {
        DacMessagePlaybackData nextMessage = nextMessage();
        logger.debug("Switching to message: "
                + nextMessage.getMessage().toString());
        AudioFileBuffer audioData = cache.getAudio(nextMessage.getMessage());
        nextMessage.setAudio(audioData);
        return nextMessage;
    }

    private DacMessagePlaybackData nextMessage() {
        DacPlaylistMessage nextMessage = null;
        boolean isInterrupt = false;

        ITimer timer = TimeUtil.getTimer();
        timer.start();
        synchronized (playlistMessgeLock) {
            timer.stop();
            logger.debug("Time for nextMessage() to acquire lock: "
                    + timer.getElapsedTime() + " ms.");

            timer.reset();
            timer.start();

            /*
             * if we have unplayed INTERRUPT priority messages always play those
             * first. After completing the INTERRUPT, playback will resume at
             * the beginning of the highest priority playist.
             */
            if (!interrupts.isEmpty()) {
                DacPlaylist nextPlaylist = interrupts.poll();
                logger.debug("Switching to playlist: "
                        + nextPlaylist.toString());

                /*
                 * By design all interrupt playlists contain a single message to
                 * play.
                 */
                DacPlaylistMessageId messageId = nextPlaylist.getMessages()
                        .get(0);
                nextMessage = cache.getMessage(messageId);
                isInterrupt = true;
                messageIndex = 0;
                playlistIndex = 0;
            }

            while ((nextMessage == null)
                    && (messageIndex < currentMessages.size())) {
                DacPlaylistMessage possibleNext = cache
                        .getMessage(currentMessages.get(messageIndex));
                if (possibleNext.isValid()) {
                    nextMessage = possibleNext;
                }
                messageIndex++;
            }

            while ((nextMessage == null) && (!currentPlaylists.isEmpty())) {
                DacPlaylist nextPlaylist = currentPlaylists.get(playlistIndex);
                logger.debug("Switching to playlist: "
                        + nextPlaylist.toString());
                List<DacPlaylistMessageId> nextMessages = nextPlaylist
                        .getMessages();

                int newMessageIdx = 0;
                while ((nextMessage == null)
                        && (newMessageIdx < nextMessages.size())) {
                    DacPlaylistMessage possibleNext = cache
                            .getMessage(nextMessages.get(newMessageIdx));
                    if (possibleNext.isValid()) {
                        nextMessage = possibleNext;
                        currentMessages = nextMessages;
                        messageIndex = newMessageIdx + 1;
                        if (messageIndex > currentMessages.size()) {
                            messageIndex = 0;
                        }
                    } else {
                        newMessageIdx++;
                    }
                }

                if (nextMessage == null) {
                    DacPlaylist purgedPlaylist = currentPlaylists
                            .remove(playlistIndex);
                    logger.info("Puring expired playlist: "
                            + purgedPlaylist.toString());
                }
            }
        }

        timer.stop();
        logger.debug("Time for nextMessage() to determine next message: "
                + timer.getElapsedTime() + " ms.");

        if (nextMessage == null) {
            // TODO what happens if we search all our playlists and find nothing
            // valid?
            logger.error("Couldn't find any valid playlists or messages to play!!!");
        }

        DacMessagePlaybackData nextMessageData = new DacMessagePlaybackData();
        nextMessageData.setMessage(nextMessage);
        nextMessageData.setInterrupt(isInterrupt);
        return nextMessageData;
    }

    @Override
    @Subscribe
    public void newPlaylistReceived(PlaylistUpdateNotification e) {
        DacPlaylist newPlaylist = e.parseFilepath();
        if (newPlaylist != null) {
            Path playlistPath = playlistDirectory.resolveSibling(e
                    .getPlaylistPath());
            newPlaylist = JAXB.unmarshal(playlistPath.toFile(),
                    DacPlaylist.class);
            cache.addToCache(newPlaylist.getMessages());
            logger.debug("Received new playlist: " + newPlaylist.toString());

            ITimer timer = TimeUtil.getTimer();
            timer.start();
            synchronized (playlistMessgeLock) {
                timer.stop();
                logger.debug("Time for newPlaylistReceived() to acquire lock: "
                        + timer.getElapsedTime() + " ms.");

                timer.reset();
                timer.start();

                if (newPlaylist.isInterrupt()) {
                    interrupts.add(newPlaylist);
                    eventBus.post(new InterruptMessageReceivedEvent(newPlaylist));
                    return;
                }

                DacPlaylist currentPlaylist = currentPlaylists
                        .get(playlistIndex);

                if ((newPlaylist.getPriority() == currentPlaylist.getPriority())
                        && (newPlaylist.getSuite().equals(currentPlaylist
                                .getSuite()))) {
                    logger.debug("New playlist is an update to current playlist.");

                    List<DacPlaylistMessageId> newMessages = newPlaylist
                            .getMessages();

                    int newMessageIdx = -1;
                    if (messageIndex == 0) {
                        newMessageIdx = 0;
                    } else {
                        int[] relativeIdxsToCheck = calcSearchOffsets();
                        for (int index : relativeIdxsToCheck) {
                            long messageId = currentMessages.get(
                                    messageIndex + index).getBroadcastId();
                            for (int i = 0; i < newMessages.size(); i++) {
                                if (messageId == newMessages.get(i)
                                        .getBroadcastId()) {
                                    newMessageIdx = i;
                                    break;
                                }
                            }

                            if (newMessageIdx >= 0) {
                                /*
                                 * if the matching message is one we previously
                                 * played, jump forward to the next message to
                                 * play the next message we haven't previously
                                 * played
                                 */
                                if (index < 0) {
                                    newMessageIdx++;
                                    if (newMessageIdx > newMessages.size()) {
                                        newMessageIdx = 0;
                                    }
                                }
                                break;
                            }
                        }

                        /*
                         * we searched the entire list and none of the messages
                         * matched, so let's just give up and start at the
                         * beginning of this "updated" playlist...
                         */
                        if (newMessageIdx < 0) {
                            newMessageIdx = 0;
                        }
                    }

                    currentPlaylists.set(playlistIndex, newPlaylist);
                    currentMessages = newMessages;
                    messageIndex = newMessageIdx;

                } else {
                    int compareVal = PLAYBACK_ORDER.compare(newPlaylist,
                            currentPlaylist);

                    if (compareVal < 0) {
                        currentPlaylists.add(0, newPlaylist);
                        currentMessages = newPlaylist.getMessages();
                        playlistIndex = 0;
                        messageIndex = 0;
                    } else {
                        currentPlaylists.add(newPlaylist);
                        Collections.sort(
                                currentPlaylists.subList(playlistIndex + 1,
                                        currentPlaylists.size() - 1),
                                PLAYBACK_ORDER);
                    }
                }
            }

            timer.stop();
            logger.debug("Time for newPlaylistReceived() to merge in new playlist: "
                    + timer.getElapsedTime() + " ms.");
        }
    }

    /**
     * Calculates the relative search indices relative to the current position
     * to use to find a matching message between 2 playlists. Starts at
     * currently playing message, then checks the next message to play, then the
     * previous message to play, and so on until all messages in the playlist
     * have been checked.
     * 
     * @return An array containing the search indices, relative to the current
     *         position in the playlist.
     */
    private int[] calcSearchOffsets() {
        int size = currentMessages.size();
        int[] retVal = new int[size];
        // always start search with message currently playing
        retVal[0] = -1;

        int retValIndex = 1;
        int posFromCurrent = 1;

        while (retValIndex < size) {
            if ((messageIndex + (posFromCurrent - 1)) < size) {
                retVal[retValIndex] = (posFromCurrent - 1);
                retValIndex++;
            }
            if ((messageIndex - (posFromCurrent + 1)) >= 0) {
                retVal[retValIndex] = 0 - (posFromCurrent + 1);
                retValIndex++;
            }
        }

        return retVal;
    }
}
