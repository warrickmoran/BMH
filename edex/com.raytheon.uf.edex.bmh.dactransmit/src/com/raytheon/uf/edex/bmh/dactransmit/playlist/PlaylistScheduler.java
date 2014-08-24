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

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;

import javax.xml.bind.JAXB;

import org.apache.commons.lang.math.Range;
import org.apache.commons.lang.mutable.MutableLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylist;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessage;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessageId;
import com.raytheon.uf.common.bmh.datamodel.playlist.PlaylistUpdateNotification;
import com.raytheon.uf.common.bmh.notify.MessagePlaybackPrediction;
import com.raytheon.uf.common.bmh.notify.PlaylistSwitchNotification;
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
 * Jul 28, 2014  #3286     dgilling     Support sending notifications when
 *                                      changing playlists.
 * Jul 29, 2014  #3286     dgilling     Use playback time to predict which
 *                                      messages will be played from a playlist.
 * Aug 06, 2014  #3286     dgilling     Send more informational 
 *                                      PlaylistSwitchNotifications, simplify logic
 *                                      when receiving update to current playlist.
 * Aug 12, 2014  #3286     dgilling     Support tones playback.
 * Aug 18, 2014  #3540     dgilling     Support periodic messsages.
 * Aug 19, 2014  #3532     bkowal       Provide the supported transmitter decibel range
 *                                      to the audio cache.
 * Aug 22, 2014  #3286     dgilling     Update playlist selection logic to use 
 *                                      creation and trigger time.
 * Aug 24, 2014   3432     mpduff       Fixed for the case of only one playlist in the directory.
 * 
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

public final class PlaylistScheduler implements
        IPlaylistUpdateNotificationHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String PLAYLIST_EXT = "*.xml";

    private static final Comparator<DacPlaylist> PLAYBACK_ORDER = new Comparator<DacPlaylist>() {

        @Override
        public int compare(DacPlaylist o1, DacPlaylist o2) {
            int retVal = 0 - Integer
                    .compare(o1.getPriority(), o2.getPriority());
            if (retVal != 0) {
                return retVal;
            }

            retVal = 0 - o1.getLatestTrigger().compareTo(o2.getLatestTrigger());
            return retVal;
        }
    };

    private static final Comparator<DacPlaylist> QUEUE_ORDER = new Comparator<DacPlaylist>() {

        @Override
        public int compare(DacPlaylist o1, DacPlaylist o2) {
            int retVal = 0 - o1.getStart().compareTo(o2.getStart());
            if (retVal != 0) {
                return retVal;
            }

            retVal = 0 - Integer.compare(o1.getPriority(), o2.getPriority());
            return retVal;
        }
    };

    private final Path playlistDirectory;

    private final PlaylistMessageCache cache;

    private final List<DacPlaylist> activePlaylists;

    private List<DacPlaylistMessageId> currentMessages;

    /**
     * Unplayed interrupt messages. We use a Queue because the playlist
     * associated with an interrupt is played only one time and discarded.
     */
    private final Queue<DacPlaylist> interrupts;

    /**
     * New playlists that we've received that don't take effect until some time
     * in the future.
     */
    private final List<DacPlaylist> futurePlaylists;

    // TODO: Is this always going to be 0?? If so, this is probably not needed.
    private int playlistIndex;

    /**
     * Always points to the index of the next message to play from
     * currentMessages. Subtract one to get currently playing message. If this
     * value is equal to currentMessages.size(), then the next call to
     * nextMessage() will cause a playlist switch (which may just be looping
     * back to the beginning of the current playlist).
     */
    private int messageIndex;

    private final Object playlistMessgeLock;

    private final EventBus eventBus;

    /**
     * There are some cases where we can't immediately send a playlist switch
     * notification--on the startup case when the event bus isn't fully ready or
     * when receiving a new higher priority playlist. In those cases we hold
     * this notification until the next call to next().
     */
    private PlaylistSwitchNotification playlistNotify;

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
     * @param dbRange
     *            The minimum and maximum levels of audio allowed by the
     *            destination transmitter in decibels.
     * @throws IOException
     *             If any I/O errors occur attempting to get the list of
     *             playlist files from the specified directory.
     */
    public PlaylistScheduler(Path inputDirectory, EventBus eventBus,
            Range dbRange) throws IOException {
        this.playlistDirectory = inputDirectory;
        this.eventBus = eventBus;

        this.futurePlaylists = new LinkedList<>();
        this.playlistIndex = 0;
        this.messageIndex = 0;

        Map<String, DacPlaylist> uniqueActivePlaylists = new HashMap<>();
        List<Path> expiredPlaylists = new ArrayList<>();
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(
                this.playlistDirectory, PLAYLIST_EXT)) {
            for (Path entry : dirStream) {
                DacPlaylist playlist = JAXB.unmarshal(entry.toFile(),
                        DacPlaylist.class);

                if (playlist.isValid()) {
                    String key = playlist.getPriority() + ":"
                            + playlist.getSuite();
                    DacPlaylist otherPlaylist = uniqueActivePlaylists.get(key);
                    if (otherPlaylist == null) {
                        uniqueActivePlaylists.put(key, playlist);
                    } else if ((otherPlaylist != null)
                            && (playlist.getCreationTime().after(otherPlaylist
                                    .getCreationTime()))) {
                        uniqueActivePlaylists.put(key, playlist);
                        // TODO add superseded playlist to expiredPlaylists
                        // need to see how file name/path string changes before
                        // that can be done.
                    }
                } else if (!playlist.isExpired()) {
                    this.futurePlaylists.add(playlist);
                } else {
                    expiredPlaylists.add(entry);
                }
            }
        }

        this.activePlaylists = new ArrayList<>(uniqueActivePlaylists.values());
        if (activePlaylists.isEmpty()) {
            throw new IllegalArgumentException(
                    "Input directory contained no valid playlist files.");
        }

        Collections.sort(this.activePlaylists, PLAYBACK_ORDER);
        Collections.sort(this.futurePlaylists, QUEUE_ORDER);

        this.cache = new PlaylistMessageCache(
                inputDirectory.resolve("messages"), this.eventBus, dbRange);
        for (DacPlaylist playlist : this.activePlaylists) {
            this.cache.retrieveAudio(playlist.getMessages());
        }

        for (Path expired : expiredPlaylists) {
            try {
                Files.delete(expired);
            } catch (IOException e) {
                logger.warn("Unable to delete expired playlist " + expired, e);
            }
        }

        this.interrupts = new ArrayDeque<>();
        /*
         * On startup we may have some unplayed interrupts. find them and
         * transfer them to interrupt queue
         */
        for (Iterator<DacPlaylist> iter = activePlaylists.iterator(); iter
                .hasNext();) {
            DacPlaylist playlist = iter.next();
            if (playlist.isInterrupt()) {
                this.interrupts.add(playlist);
                iter.remove();
            }
        }

        DacPlaylist firstPlaylist = (!this.interrupts.isEmpty()) ? this.interrupts
                .peek() : activePlaylists.get(0);

        /*
         * We cannot send this notification to the event bus at construction
         * time because the listeners will not have all been started yet. Hence,
         * we hold on to it until the first call here.
         */
        this.playlistNotify = buildPlaylistNotification(firstPlaylist);
        if (firstPlaylist.isInterrupt()) {
            this.currentMessages = Collections.emptyList();
        } else {
            this.currentMessages = this.playlistNotify.getPlaylist();
        }
        logger.debug("Starting with playlist: " + firstPlaylist.toString());

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
        if (playlistNotify != null) {
            eventBus.post(playlistNotify);
            playlistNotify = null;
        }

        DacMessagePlaybackData nextMessageData = nextMessage();
        DacPlaylistMessage nextMessage = nextMessageData.getMessage();
        logger.debug("Switching to message: " + nextMessage.toString());
        AudioFileBuffer audioData = cache.getAudio(nextMessage);
        boolean playTones = ((nextMessage.getSAMEtone() != null) && (nextMessage
                .getPlayCount() == 0));
        audioData.setReturnTones(playTones);
        nextMessageData.setAudio(audioData);
        return nextMessageData;
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
             * the beginning of the highest priority playlist.
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

                PlaylistSwitchNotification notify = buildPlaylistNotification(nextPlaylist);
                eventBus.post(notify);
            }

            DacPlaylist currentPlaylist = activePlaylists.get(playlistIndex);
            List<DacPlaylist> expiredPlaylists = mergeFutureToActivePlaylists();
            DacPlaylist newActivePlaylist = activePlaylists.get(0);
            if (currentPlaylist == newActivePlaylist) {
                logger.debug("Continuing with playlist " + currentPlaylist);
            } else if ((currentPlaylist.getPriority() == newActivePlaylist
                    .getPriority())
                    && (currentPlaylist.getSuite().equals(newActivePlaylist
                            .getSuite()))) {
                logger.debug("Playlist " + currentPlaylist
                        + " has been updated by future queued playlist "
                        + newActivePlaylist);

                int prevMessageIndex = findLastPlayedMessage(newActivePlaylist);

                /*
                 * We pass prevMessageIndex+1 to buildPlaylistNotification()
                 * because that is the first index in the updated playlist that
                 * we want to predict new playback times for. If
                 * (prevMessageIndex+1) > newPlaylist.getMessages().size() then
                 * we have no messages to predict new times for and
                 * buildPlaylistNotification() won't attempt to.
                 * 
                 * A reminder about messageIndex: it always points to the next
                 * message to play from currentMessages and if messageIndex =
                 * currentMessages.size(), the next call to nextMessage() will
                 * cause a playlist switch because we've run out of messages to
                 * play. We do not want to wrap the value of messageIndex here
                 * because we won't generate the playlist switch that the GUIs
                 * need to know we're beginning a new cycle.
                 */
                PlaylistSwitchNotification notify = buildPlaylistNotification(
                        newActivePlaylist, prevMessageIndex + 1);
                currentMessages = notify.getPlaylist();
                messageIndex = prevMessageIndex + 1;
                eventBus.post(notify);
            } else {
                logger.debug("Playlist " + currentPlaylist
                        + " has been superseded by future queued playlist "
                        + newActivePlaylist);
                /*
                 * We're going to be swapping playlists, so let's just force the
                 * code below to start looking by jumping the index off the edge
                 * of the list.
                 */
                messageIndex = Integer.MAX_VALUE;
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

            while ((nextMessage == null) && (!activePlaylists.isEmpty())) {
                DacPlaylist nextPlaylist = activePlaylists.get(playlistIndex);
                PlaylistSwitchNotification notify = buildPlaylistNotification(nextPlaylist);
                if ((notify != null) && (!notify.getMessages().isEmpty())) {
                    logger.debug("Switching to playlist: "
                            + nextPlaylist.toString());

                    List<DacPlaylistMessageId> nextMessages = notify
                            .getPlaylist();

                    /*
                     * Since we've switched playlists, this call will return
                     * index 0 from the playlist. Set the messageIndex to 1, so
                     * the next call to this method is set to check the next
                     * message in the playlist. If this playlist so happens to
                     * only have a single message, we'll just loop back to the
                     * beginning at the next call to nextMessage() and generate
                     * an updated PlaylistSwitchNotification.
                     */
                    nextMessage = cache.getMessage(nextMessages.get(0));
                    currentMessages = nextMessages;
                    messageIndex = 1;
                    eventBus.post(notify);
                }

                if (nextMessage == null) {
                    DacPlaylist purgedPlaylist = activePlaylists
                            .remove(playlistIndex);
                    logger.info("Puring expired playlist: "
                            + purgedPlaylist.toString());
                    expiredPlaylists.add(purgedPlaylist);
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

        // TODO delete all playlist files in expiredPlaylists
        // waiting on file name/path updates...

        DacMessagePlaybackData nextMessageData = new DacMessagePlaybackData();
        nextMessageData.setMessage(nextMessage);
        nextMessageData.setInterrupt(isInterrupt);
        return nextMessageData;
    }

    private List<DacPlaylist> mergeFutureToActivePlaylists() {
        List<DacPlaylist> expiredPlaylists = new ArrayList<>();

        List<DacPlaylist> newPlaylists = new ArrayList<>();
        for (Iterator<DacPlaylist> iter = futurePlaylists.iterator(); iter
                .hasNext();) {
            DacPlaylist playlist = iter.next();
            if (playlist.isValid()) {
                iter.remove();
                newPlaylists.add(playlist);
            }

            if (playlist.isExpired()) {
                iter.remove();
                expiredPlaylists.add(playlist);
            }
        }

        for (Iterator<DacPlaylist> iter = newPlaylists.iterator(); iter
                .hasNext();) {
            DacPlaylist newPlaylist = iter.next();

            for (int i = 0; i < activePlaylists.size(); i++) {
                DacPlaylist activePlaylist = activePlaylists.get(i);
                if ((newPlaylist.getPriority() == activePlaylist.getPriority())
                        && (newPlaylist.getSuite().equals(activePlaylist
                                .getSuite()))) {
                    iter.remove();
                    if ((activePlaylist.isExpired())
                            || (newPlaylist.getCreationTime()
                                    .after(activePlaylist.getCreationTime()))) {
                        activePlaylists.set(i, newPlaylist);
                        expiredPlaylists.add(activePlaylist);
                    } else {
                        expiredPlaylists.add(newPlaylist);
                    }
                }
            }
        }
        activePlaylists.addAll(newPlaylists);
        Collections.sort(activePlaylists, PLAYBACK_ORDER);

        return expiredPlaylists;
    }

    private int findLastPlayedMessage(DacPlaylist newPlaylist) {
        List<DacPlaylistMessageId> newMessages = newPlaylist.getMessages();

        int prevMessageIndex = -1;
        for (int i = (messageIndex - 1); i >= 0; i--) {
            prevMessageIndex = newMessages.indexOf(currentMessages.get(i));
            if (prevMessageIndex >= 0) {
                break;
            }
        }

        return prevMessageIndex;
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
            cache.retrieveAudio(newPlaylist.getMessages());
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

                if (newPlaylist.isExpired()) {
                    logger.warn("Received a notification for an expired playlist: "
                            + newPlaylist);
                    // TODO delete from filesystem
                    return;
                } else if (!newPlaylist.isValid()) {
                    logger.debug("New playlist doesn't take affect immediately. Queueing...");
                    futurePlaylists.add(newPlaylist);
                    Collections.sort(futurePlaylists, QUEUE_ORDER);
                    return;
                }

                DacPlaylist currentPlaylist = activePlaylists
                        .get(playlistIndex);

                if ((newPlaylist.getPriority() == currentPlaylist.getPriority())
                        && (newPlaylist.getSuite().equals(currentPlaylist
                                .getSuite()))) {
                    if (newPlaylist.getCreationTime().before(
                            currentPlaylist.getCreationTime())) {
                        logger.warn("Received an update to playlist "
                                + currentPlaylist
                                + " that has an older creation date. Ignoring new playlist.");
                        // TODO delete from filesystem
                        return;
                    }

                    logger.debug("New playlist is an update to current playlist.");

                    int prevMessageIndex = findLastPlayedMessage(newPlaylist);

                    /*
                     * We pass prevMessageIndex+1 to buildPlaylistNotification()
                     * because that is the first index in the updated playlist
                     * that we want to predict new playback times for. If
                     * (prevMessageIndex+1) > newPlaylist.getMessages().size()
                     * then we have no messages to predict new times for and
                     * buildPlaylistNotification() won't attempt to.
                     * 
                     * A reminder about messageIndex: it always points to the
                     * next message to play from currentMessages and if
                     * messageIndex = currentMessages.size(), the next call to
                     * nextMessage() will cause a playlist switch because we've
                     * run out of messages to play. We do not want to wrap the
                     * value of messageIndex here because we won't generate the
                     * playlist switch that the GUIs need to know we're
                     * beginning a new cycle.
                     */
                    PlaylistSwitchNotification notify = buildPlaylistNotification(
                            newPlaylist, prevMessageIndex + 1);
                    activePlaylists.set(0, newPlaylist);
                    currentMessages = notify.getPlaylist();
                    messageIndex = prevMessageIndex + 1;
                    eventBus.post(notify);
                } else {
                    int replaceIdx = findMatchingPlaylist(newPlaylist,
                            activePlaylists);
                    if (replaceIdx != -1) {
                        DacPlaylist toReplace = activePlaylists.get(replaceIdx);
                        if (newPlaylist.getCreationTime().after(
                                toReplace.getCreationTime())) {
                            activePlaylists.set(replaceIdx, newPlaylist);
                        } else {
                            logger.warn("Received an update to playlist "
                                    + toReplace
                                    + " that has an older creation date. Ignoring new playlist.");
                            // TODO delete from filesystem
                            return;
                        }
                    }

                    int compareVal = PLAYBACK_ORDER.compare(newPlaylist,
                            currentPlaylist);

                    if (compareVal < 0) {
                        activePlaylists.add(0, newPlaylist);
                        /*
                         * We must hold this playlist switch until the next call
                         * to next() because we will continue playing the
                         * current message from the old playlist until then.
                         */
                        playlistNotify = buildPlaylistNotification(newPlaylist);
                        currentMessages = playlistNotify.getPlaylist();
                        playlistIndex = 0;
                        messageIndex = 0;
                    } else {
                        activePlaylists.add(newPlaylist);
                        Collections.sort(activePlaylists.subList(
                                playlistIndex + 1, activePlaylists.size()),
                                PLAYBACK_ORDER);
                    }
                }
            }

            timer.stop();
            logger.debug("Time for newPlaylistReceived() to merge in new playlist: "
                    + timer.getElapsedTime() + " ms.");
        }
    }

    private static int findMatchingPlaylist(DacPlaylist playlist,
            List<DacPlaylist> playlistList) {
        for (int i = 0; i < playlistList.size(); i++) {
            DacPlaylist compare = playlistList.get(i);
            if ((playlist.getPriority() == compare.getPriority())
                    && (playlist.getSuite().equals(compare.getSuite()))) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Build a {@code PlaylistSwitchNotification} for the specified playlist.
     * Playlist will only contain messages predicted to be valid based on
     * playback time of all previous messages in the playlist and the current
     * time.
     * 
     * @param playlist
     *            {@code DacPlaylist} which will have a list of all possible
     *            messages that could be played for this playlist.
     * @return A {@code PlaylistSwitchNotification} containing the list of valid
     *         messages or messages predicted to be valid when it comes to be
     *         their turn for playback.
     */
    private PlaylistSwitchNotification buildPlaylistNotification(
            DacPlaylist playlist) {
        return buildPlaylistNotification(playlist, 0);
    }

    /**
     * Build a {@code PlaylistSwitchNotification} for the specified playlist,
     * and only begin predicting playback times at a specific index for the
     * messages within that playlist.
     * 
     * @param playlist
     *            {@code DacPlaylist} which will have a list of all possible
     *            messages that could be played for this playlist.
     * @param predictStartIndex
     *            The index within the list to start predicting playback times.
     *            All messages prior to this index will not receive prediction
     *            times, but still have all the historical playback information
     *            (playcounts, last played time, etc.).
     * @return A {@code PlaylistSwitchNotification} containing the list of valid
     *         messages or messages predicted to be valid when it comes to be
     *         their turn for playback.
     */
    private PlaylistSwitchNotification buildPlaylistNotification(
            DacPlaylist playlist, int predictStartIndex) {
        if (!playlist.isValid()) {
            return null;
        }

        List<MessagePlaybackPrediction> predictedMessages = new ArrayList<>();
        List<DacPlaylistMessageId> allMessages = playlist.getMessages();
        long playbackStartTime = 0;
        long cycleTime = 0;

        /*
         * Anything that happened previously in the playlist will be reported
         * just as it occurred.
         */
        for (int i = 0; i < predictStartIndex; i++) {
            DacPlaylistMessageId messageId = allMessages.get(i);
            DacPlaylistMessage messageData = cache.getMessage(messageId);

            MessagePlaybackPrediction prediction = new MessagePlaybackPrediction(
                    messageId.getBroadcastId(), null, messageData);
            predictedMessages.add(prediction);

            cycleTime += cache.getPlaybackTime(messageId);

            if (i == (predictStartIndex - 1)) {
                playbackStartTime = messageData.getLastTransmitTime()
                        .getTimeInMillis() + cache.getPlaybackTime(messageId);
            }
        }

        if (playbackStartTime < TimeUtil.currentTimeMillis()) {
            playbackStartTime = TimeUtil.currentTimeMillis();
        }

        /*
         * Determine if we have any periodic messages in the playlist that need
         * to be scheduled outside of the typical playlist order.
         */
        Map<DacPlaylistMessageId, MutableLong> periodicMessages = new HashMap<>();
        for (int i = predictStartIndex; i < allMessages.size(); i++) {
            DacPlaylistMessageId messageId = allMessages.get(i);
            DacPlaylistMessage messageData = cache.getMessage(messageId);

            if ((messageData.getPeriodicity() != null)
                    && (!messageData.getPeriodicity().isEmpty())
                    && (messageData.getPlayCount() >= 1)) {
                long nextPlayTime = messageData.getLastTransmitTime()
                        .getTimeInMillis() + messageData.getPlaybackInterval();
                periodicMessages.put(messageId, new MutableLong(nextPlayTime));
            }
        }

        for (int i = predictStartIndex; i < allMessages.size(); i++) {
            DacPlaylistMessageId messageId = allMessages.get(i);
            DacPlaylistMessage messageData = cache.getMessage(messageId);

            /*
             * ignore start/expire times for interrupt playlists, we just want
             * to play the message.
             */
            if ((playlist.isInterrupt() || messageData
                    .isValid(playbackStartTime))
                    && (!periodicMessages.containsKey(messageId))) {
                MessagePlaybackPrediction prediction = new MessagePlaybackPrediction(
                        messageId.getBroadcastId(),
                        TimeUtil.newGmtCalendar(new Date(playbackStartTime)),
                        messageData);
                predictedMessages.add(prediction);

                long playbackTime = cache.getPlaybackTime(messageId);
                cycleTime += playbackTime;
                playbackStartTime += playbackTime;
            }

            for (Entry<DacPlaylistMessageId, MutableLong> entry : periodicMessages
                    .entrySet()) {
                DacPlaylistMessageId periodicMessageId = entry.getKey();
                DacPlaylistMessage periodicMessage = cache
                        .getMessage(periodicMessageId);
                MutableLong nextPlaybackTime = entry.getValue();

                if (periodicMessage.isValid(playbackStartTime)
                        && nextPlaybackTime.longValue() <= playbackStartTime) {
                    logger.debug("Scheduling periodic message ["
                            + periodicMessageId + "].");

                    MessagePlaybackPrediction prediction = new MessagePlaybackPrediction(
                            periodicMessageId.getBroadcastId(),
                            TimeUtil.newGmtCalendar(new Date(playbackStartTime)),
                            periodicMessage);
                    predictedMessages.add(prediction);

                    long playbackTime = cache
                            .getPlaybackTime(periodicMessageId);
                    cycleTime += playbackTime;
                    playbackStartTime += playbackTime;

                    /*
                     * Now that we've scheduled the message, we jump the next
                     * play time way into the future so it doesn't get scheduled
                     * again.
                     */
                    nextPlaybackTime.setValue(Long.MAX_VALUE);
                }
            }
        }

        PlaylistSwitchNotification retVal = new PlaylistSwitchNotification(
                playlist.getSuite(), playlist.getTransmitterGroup(),
                predictedMessages, cycleTime);
        return retVal;
    }
}
