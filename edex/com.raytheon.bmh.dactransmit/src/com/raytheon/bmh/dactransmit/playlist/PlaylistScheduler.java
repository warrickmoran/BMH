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
package com.raytheon.bmh.dactransmit.playlist;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.regex.Pattern;

import javax.xml.bind.JAXB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.raytheon.bmh.dactransmit.dacsession.DacSession;
import com.raytheon.bmh.dactransmit.events.handlers.IPlaylistUpdateNotificationHandler;
import com.raytheon.bmh.dactransmit.exceptions.NoSoundFileException;
import com.raytheon.uf.common.bmh.broadcast.LiveBroadcastStartCommand.BROADCASTTYPE;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylist;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessage;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessageId;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistStartTimeComparator;
import com.raytheon.uf.common.bmh.datamodel.playlist.PlaylistUpdateNotification;
import com.raytheon.uf.common.bmh.notify.BroadcastMsgInitFailedNotification;
import com.raytheon.uf.common.bmh.notify.MessageDelayedBroadcastNotification;
import com.raytheon.uf.common.bmh.notify.MessageNotBroadcastNotification;
import com.raytheon.uf.common.bmh.notify.MessagePlaybackPrediction;
import com.raytheon.uf.common.bmh.notify.NoPlaybackMessageNotification;
import com.raytheon.uf.common.bmh.notify.PlaylistNotification;
import com.raytheon.uf.common.bmh.notify.SAMEMessageTruncatedNotification;
import com.raytheon.uf.common.time.util.ITimer;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.edex.bmh.msg.logging.DefaultMessageLogger;
import com.raytheon.uf.edex.bmh.msg.logging.ErrorActivity.BMH_ACTIVITY;
import com.raytheon.uf.edex.bmh.msg.logging.ErrorActivity.BMH_COMPONENT;

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
 * Aug 24, 2014   3558     rjpeter      Switched activePlaylists to SortedSet, added deletion of old playlists.
 * Aug 25, 2014  #3286     dgilling     Fix NPE when calling 
 *                                      buildPlaylistNotification() against
 *                                      interrupt playlists.
 * Aug 26, 2014  #3286     dgilling     Allow playlists directory to be 
 *                                      initially empty.
 * Aug 26, 2014  #3486     bsteffen     Make deletion of playlists safer and more verbose.
 * Sep 5, 2014   #3532     bkowal       Use a decibel target instead of a range.
 * Sep 08, 2014  #3286     dgilling     Move playlist deletion off main thread.
 * Sep 11, 2014  #3606     dgilling     Prevent exceptions when messages don't
 *                                      have an associated sound file and when
 *                                      all playlists expire.
 * Sep 30, 2014  #3589     dgilling     Better handle forced expiration of 
 *                                      playlists from CommsManager.
 * Oct 01, 2014  #3485     bsteffen     Allow playlist to resume on startup.
 * Oct 2, 2014   #3642     bkowal       Updated to use the transmitter timezone and the
 *                                      audio buffer abstraction.
 * Oct 21, 2014  #3655     bkowal       Support delaying interrupts.
 * Oct 30, 2014  #3617     dgilling     Support tone blackout period.
 * Nov 17, 2014  #3630     bkowal       Ensure that all messages in the playlist exist
 *                                      before caching. Handles a condition that is only
 *                                      encountered on startup.
 * Dec 08, 2014  #3878     bkowal       Forcefully schedule all non-static periodic messages
 *                                      when there are only periodic messages in the playlist.
 * Jan 05, 2015  #3651     bkowal       Use {@link DefaultMessageLogger} to log msg errors.
 * Jan 08, 2015  #3912     bsteffen     Change the way playlist refresh works to do periodic better
 * Jan 13, 2015  #3843     bsteffen     Add periodic predictions to playlist switch notification.
 * Jan 14, 2015  #3969     bkowal       Publish {@link MessageNotBroadcastNotification}s when
 *                                      certain watching/warnings expire before they can be played.
 * Jan 19, 2015  #4002     bkowal       Publish {@link MessageDelayedBroadcastNotification}s when
 *                                      an active Broadcast Live is delaying the broadcast of
 *                                      a warning or interrupt.
 * Jan 28, 2015  #4036     bsteffen     Actually expire playlists with no playable messages.
 * Feb 06, 2015  #4071     bsteffen     Consolidate threading.
 * Feb 12, 2015  #4114     bsteffen     Fix playlist expiration.
 * Mar 03, 2015  #4002     bkowal       Log that the dac transmit has disseminated a
 *                                      {@link MessageNotBroadcastNotification}.
 * Mar 06, 2015  #4188     bsteffen     Handle start time of interrupts.
 * Mar 09, 2015  #4170     bsteffen     Remove messages when audio failed to load.
 * Mar 12, 2015  #4193     bsteffen     Do not insert invalid messages into the past.
 * Mar 13, 2015  #4222     bkowal       Prevent Unsupported Operation Exception when
 *                                      an Interrupt arrives.
 * Mar 25, 2015  #4290     bsteffen     Switch to global replacement.
 * Apr 03, 2015  #4222     rjpeter      Fix discarding of bad messages.
 * May 06, 2015  #4466     bkowal       No longer crash when invalid / incomplete playlists are
 *                                      read during startup.
 * May 11, 2015  #4002     bkowal       Handle all Broadcast Live message delay scenarios.
 * May 19, 2015  4508      rjpeter      Add exception handling to newPlaylistReceived.
 * May 21, 2015  4429      rjpeter      Update message logging.
 * May 22, 2015  4481      bkowal       Specify if the audio is dynamic when setting the audio in
 *                                      {@link DacMessagePlaybackData}.
 * May 28, 2015  4429      rjpeter      Updated log statements.
 * Jun 01, 2015  4490      bkowal       Use new {@link SAMEMessageTruncatedNotification} constructor.
 * Jun 02, 2016  #4369     rferrel      Added method {@link #sendNoPlaybackNotification()}.
 * Jun 04, 2015  4482      rjpeter      Create playlist directory on start.
 * Sep 22, 2015  4904      bkowal       Write replaced messages to the playlist as an
 *                                      expired message.
 * Jan 21, 2015  5278      bkowal       Determine if future playlists can be replaced
 *                                      by new incoming future playlists.
 * Feb 23, 2016  5382      bkowal       Pre-order the playlists based on name. Load remaining
 *                                      playlists as a high priority background task.
 * Mar 01, 2016  5382      bkowal       Enable the purge and archive jobs after all initial
 *                                      playlist processing has concluded. Check for null message data.
 * Apr 26, 2016  5561      bkowal       Indicate whether or not the associated playlist is an interrupt
 *                                      playlist when refreshing time-sensitive audio.
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

public final class PlaylistScheduler implements
        IPlaylistUpdateNotificationHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * The amount of time (in hours) before an invalid playlist will be deleted.
     * If the system encounters an invalid playlist, it will only be purged it
     * the difference between its last modification time and the current time is
     * greater than the specified grace period. The grace period is used to
     * ensure that we do not purge a playlist that is currently being written
     * (specifically during startup).
     */
    private static final long INVALID_PLAYLIST_GRACE_PERIOD = (Integer
            .getInteger("invalidPlaylistGracePeriod", 1) * TimeUtil.MILLIS_PER_HOUR);

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

    private final SortedSet<DacPlaylist> activePlaylists;

    private List<DacPlaylistMessageId> currentMessages = Collections
            .emptyList();

    /**
     * Unplayed interrupt messages. We use a Queue because the playlist
     * associated with an interrupt is played only one time and discarded.
     */
    private final ConcurrentSkipListSet<DacPlaylist> interrupts;

    /**
     * New playlists that we've received that don't take effect until some time
     * in the future.
     */
    private final List<DacPlaylist> futurePlaylists;

    private DacPlaylist currentPlaylist;

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

    private final ExecutorService executorService;

    private volatile boolean warnNoMessages;

    /**
     * Flag indicating the system is starting up. Will be true until the first
     * time next() is called.
     */
    private volatile boolean startup = true;

    /**
     * Flag indicating that any interrupts should be queued until further
     * notice. This flag is used to ensure that interrupts will not interfere
     * with the initialization of a live broadcast.
     */
    private volatile boolean delayInterrupts;

    private volatile BROADCASTTYPE type;

    private static final String PLAYLIST_NAME_REGEX = "^P([0-3])_.*_(\\d{17})_T(\\d{6})\\.xml$";

    private static final Pattern PLAYLIST_NAME_PATTERN = Pattern
            .compile(PLAYLIST_NAME_REGEX);

    /**
     * Reads the directory specified in DacSession for valid playlist files
     * (ones that have not already passed their expiration time) and sorts them
     * into playback order for DAC transmission. Asynchronously queues each
     * message from each playlist into the {@code PlaylistMessageCache}.
     * 
     * @param dacSession
     *            Reference back to the DacSession
     */
    public PlaylistScheduler(DacSession dacSession) {
        this.playlistDirectory = dacSession.getConfig().getInputDirectory();
        this.eventBus = dacSession.getEventBus();
        this.executorService = dacSession.getAsyncExecutor();

        this.futurePlaylists = new LinkedList<>();
        this.messageIndex = 0;

        try {
            Files.createDirectories(this.playlistDirectory);
        } catch (Exception e) {
            logger.error("Failed to create playlist directory: "
                    + this.playlistDirectory, e);
        }

        final String[] playlistNames = this.playlistDirectory.toFile().list(
                new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return PLAYLIST_NAME_PATTERN.matcher(name).matches();
                    }
                });

        List<String> playlists = new LinkedList<String>(
                Arrays.asList(playlistNames));
        Collections.sort(playlists, new PlaylistNameComparator(
                PLAYLIST_NAME_PATTERN));

        this.activePlaylists = new TreeSet<>(PLAYBACK_ORDER);
        this.cache = new PlaylistMessageCache(dacSession, this);
        this.interrupts = new ConcurrentSkipListSet<>(
                new DacPlaylistStartTimeComparator());
        this.playlistMessgeLock = new Object();
        if (playlists.isEmpty()) {
            // no pre-existing playlists.
            this.cache.enableCleanup();
        } else {
            /*
             * Read the very first playlist during startup. Based on the
             * pre-ordering of the playlists by priority and trigger, the
             * playlist will contain the first set of messages that are eligible
             * for initial playback.
             */
            final String playlist = playlists.remove(0);
            this.processPlaylist(playlist);
            logger.info("Loaded initial playlist: {}.", playlist);
            this.schedulePlaylistLoad(playlists);
        }
        this.warnNoMessages = true;
    }

    private void processPlaylist(final String playlistName) {
        Path playlistPath = this.playlistDirectory.resolve(playlistName);
        DacPlaylist playlist = null;
        try {
            playlist = JAXB.unmarshal(playlistPath.toFile(), DacPlaylist.class);
        } catch (Exception e) {
            logger.error(
                    "Unable to parse playlist file: " + playlistPath.toString()
                            + ".", e);
            DefaultMessageLogger.getInstance().logPlaylistError(
                    BMH_COMPONENT.PLAYLIST_MANAGER, BMH_ACTIVITY.PLAYLIST_READ,
                    playlistPath.toString(), e);
            this.checkPurgeUnusablePlaylist(playlistPath);
        }
        playlist.setPath(playlistPath);

        DacPlaylist retrieveAudioPlaylist = null;
        synchronized (playlistMessgeLock) {
            if (playlist.isValid()) {
                if (playlist.isInterrupt()) {
                    this.interrupts.add(playlist);
                    retrieveAudioPlaylist = playlist;
                } else if (this.checkForReplacementPlaylist(playlist,
                        activePlaylists)) {
                    retrieveAudioPlaylist = playlist;
                }
            } else if (!playlist.isExpired()) {
                if (this.checkForReplacementPlaylist(playlist, futurePlaylists)) {
                    Collections.sort(this.futurePlaylists, QUEUE_ORDER);
                    retrieveAudioPlaylist = playlist;
                }
            } else {
                expirePlaylist(playlist);
            }
        }

        if (retrieveAudioPlaylist != null) {
            Iterator<DacPlaylistMessageId> messageIterator = retrieveAudioPlaylist
                    .getMessages().iterator();
            while (messageIterator.hasNext()) {
                DacPlaylistMessageId id = messageIterator.next();
                if (this.cache.doesMessageFileExist(id) == false) {
                    logger.warn(id.toString() + " referenced by playlist "
                            + retrieveAudioPlaylist.toString()
                            + " no longer exists. Ignoring message ...");
                    messageIterator.remove();
                }
            }
            this.cache.retrieveAudio(retrieveAudioPlaylist);
        }
    }

    private void checkPurgeUnusablePlaylist(final Path playlistPath) {
        if (Files.exists(playlistPath) == false) {
            return;
        }
        final long lastModifiedTime;
        try {
            lastModifiedTime = Files.getLastModifiedTime(playlistPath)
                    .toMillis();
        } catch (IOException e) {
            logger.error(
                    "Failed to determine the last modification time of playlist: "
                            + playlistPath.toString() + ".", e);
            return;
        }
        if ((System.currentTimeMillis() - lastModifiedTime) > INVALID_PLAYLIST_GRACE_PERIOD) {
            /*
             * Grace period exceeded - purge the playlist file.
             */
            logger.info("Attempting to purge unusable playlist: {} ...",
                    playlistPath.toString());
            try {
                Files.deleteIfExists(playlistPath);
            } catch (IOException e) {
                logger.error(
                        "Failed to purge playlist file: "
                                + playlistPath.toString() + ".", e);
            }
        }
    }

    public boolean hasInterrupt() {
        if (delayInterrupts || interrupts.isEmpty()) {
            return false;
        }
        long start = interrupts.first().getStart().getTimeInMillis();
        return start <= System.currentTimeMillis();
    }

    /**
     * Retrieves the next audio file to play.
     * 
     * @return {@code DacMessagePlaybackData} containing the data about the next
     *         file to play.
     */
    public DacMessagePlaybackData next() {
        DacMessagePlaybackData nextMessageData = nextMessage();

        if (nextMessageData != null) {
            warnNoMessages = true;
            DacPlaylistMessage nextMessage = nextMessageData.getMessage();
            logger.debug("Switching to message: " + nextMessage.toString());
            IAudioFileBuffer audioDataBuffer = null;
            try {
                audioDataBuffer = cache.getAudio(nextMessage);
            } catch (Throwable e) {
                logger.error(e.getMessage(), e);
                this.eventBus.post(new BroadcastMsgInitFailedNotification(
                        nextMessage, currentPlaylist.isInterrupt()));
                synchronized (playlistMessgeLock) {
                    /*
                     * It is possible that the DacPlaylistMessageId that we want
                     * to remove may be NULL or not exist when an interrupt is
                     * triggered.
                     */
                    if (this.currentMessages.size() > 0) {
                        messageIndex -= 1;
                        DacPlaylistMessageId messageIdToRemove = this.currentMessages
                                .remove(messageIndex);
                        if (messageIdToRemove != null) {
                            this.currentPlaylist.getMessages().remove(
                                    messageIdToRemove);
                        }
                    }
                }
            }
            AudioFileBuffer audioData = null;
            if (audioDataBuffer != null) {
                if (audioDataBuffer.isDynamic()) {
                    audioData = this.cache.refreshTimeSensitiveAudio(
                            (DynamicTimeAudioFileBuffer) audioDataBuffer,
                            nextMessage, TimeUtil.newGmtCalendar(),
                            currentPlaylist.isInterrupt());
                } else {
                    audioData = (AudioFileBuffer) audioDataBuffer;
                }
                audioData.setReturnTones(nextMessage.shouldPlayTones(TimeUtil
                        .newGmtCalendar()));
                nextMessageData
                        .setAudio(audioData, audioDataBuffer.isDynamic());
            }
        } else {
            if (warnNoMessages) {
                logger.warn("There are currently no valid playlists or messages to play.");
                warnNoMessages = false;
            }
        }

        return nextMessageData;
    }

    private DacMessagePlaybackData nextMessage() {
        // TODO: Too much logic here. This was supposed to be fast just
        // switching to next message
        DacPlaylistMessage nextMessage = null;
        /*
         * Make sure even if we introduce more early returns, we never forget to
         * reset startup.
         */
        boolean startup = this.startup;
        this.startup = false;
        DacPlaylist nextPlaylist = null;
        List<DacPlaylist> expiredPlaylists = new LinkedList<>();

        if ((currentPlaylist != null) && currentPlaylist.isInterrupt()) {
            expiredPlaylists.add(currentPlaylist);
        }

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
            DacPlaylist interrupt = null;
            if (!this.delayInterrupts && !this.interrupts.isEmpty()) {
                interrupt = this.interrupts.first();
                long start = interrupts.first().getStart().getTimeInMillis();
                if (start > System.currentTimeMillis()) {
                    interrupt = null;
                } else {
                    interrupts.remove(interrupt);
                }
            }
            if (interrupt != null) {
                nextPlaylist = interrupt;
                logger.debug("Switching to playlist: "
                        + nextPlaylist.toString());

                /*
                 * By design all interrupt playlists contain a single message to
                 * play.
                 */
                DacPlaylistMessageId messageId = nextPlaylist.getMessages()
                        .get(0);
                nextMessage = cache.getMessage(messageId);
                messageIndex = 0;

                setCurrentPlaylist(nextPlaylist, false);
                currentMessages = Collections.emptyList();
            } else {
                mergeFutureToActivePlaylists(expiredPlaylists);

                if ((currentPlaylist != null) & (!activePlaylists.isEmpty())) {
                    nextPlaylist = activePlaylists.first();
                    boolean expired = currentPlaylist.isExpired();

                    boolean sameSuite = currentPlaylist.getSuite().equals(
                            nextPlaylist.getSuite());

                    if (!expired && sameSuite) {
                        if (currentPlaylist == nextPlaylist) {
                            logger.debug("Continuing with playlist "
                                    + currentPlaylist);
                        } else {
                            logger.debug("Playlist "
                                    + currentPlaylist
                                    + " has been updated by future queued playlist "
                                    + nextPlaylist);
                            setCurrentPlaylist(nextPlaylist, true);
                        }
                    } else {
                        /*
                         * We're going to be swapping playlists, so let's just
                         * force the code below to start looking by jumping the
                         * index off the edge of the list.
                         */
                        messageIndex = currentMessages.size();
                    }
                } else if (!activePlaylists.isEmpty()) {
                    nextPlaylist = activePlaylists.first();
                    setCurrentPlaylist(nextPlaylist, false);
                } else {
                    return null;
                }

                if (startup) {
                    Calendar latestLastTime = null;
                    for (int i = 0; i < currentMessages.size(); i += 1) {
                        DacPlaylistMessage message = cache
                                .getMessage(currentMessages.get(i));
                        Calendar lastTime = message.getLastTransmitTime();
                        if ((lastTime != null)
                                && ((latestLastTime == null) || lastTime
                                        .after(latestLastTime))) {
                            messageIndex = i + 1;
                            latestLastTime = lastTime;
                        }

                    }
                }

                while ((nextMessage == null)
                        && (messageIndex < currentMessages.size())) {
                    DacPlaylistMessage possibleNext = cache
                            .getMessage(currentMessages.get(messageIndex));
                    if (possibleNext.isValid()) {
                        nextMessage = possibleNext;
                    } else {
                        this.sendNotBroadcastNotification(possibleNext);
                    }
                    messageIndex++;
                }

                while ((nextMessage == null) && (!activePlaylists.isEmpty())) {
                    nextPlaylist = activePlaylists.first();
                    if (!nextPlaylist.isExpired()) {
                        setCurrentPlaylist(nextPlaylist, false);
                        if (!currentMessages.isEmpty()) {
                            logger.debug("Switching to playlist: "
                                    + nextPlaylist.toString());
                            /*
                             * Since we've switched playlists, this call will
                             * return index 0 from the playlist. Set the
                             * messageIndex to 1, so the next call to this
                             * method is set to check the next message in the
                             * playlist. If this playlist so happens to only
                             * have a single message, we'll just loop back to
                             * the beginning at the next call to nextMessage()
                             * and generate an updated PlaylistNotification.
                             */
                            nextMessage = cache.getMessage(currentMessages
                                    .get(0));
                            messageIndex += 1;
                        }
                    }
                    if (nextMessage == null) {
                        /*
                         * Handle the case when a playlist containing watches
                         * and/or warnings expires before the messages can even
                         * be scanned once. Scenario: a warning is submitted
                         * during an active broadcast live session; the
                         * broadcast live ends after the message has expired.
                         */
                        for (DacPlaylistMessageId expiredMessageId : nextPlaylist
                                .getMessages()) {
                            DacPlaylistMessage expiredMessage = this.cache
                                    .getMessage(expiredMessageId);
                            if (expiredMessage.getPlayCount() == 0) {
                                this.sendNotBroadcastNotification(expiredMessage);
                            }
                        }
                        expiredPlaylists.add(nextPlaylist);
                        activePlaylists.remove(nextPlaylist);
                        logger.info("Purging playlist because no messages are playable: "
                                + nextPlaylist);
                    }
                }
            }

            currentPlaylist = nextPlaylist;
        }
        timer.stop();
        logger.debug("Time for nextMessage() to determine next message: "
                + timer.getElapsedTime() + " ms.");

        expirePlaylists(expiredPlaylists);

        if (nextMessage == null) {
            return null;
        }

        DacMessagePlaybackData nextMessageData = new DacMessagePlaybackData();
        nextMessageData.setMessage(nextMessage);
        nextMessageData.setInterrupt(currentPlaylist.isInterrupt());
        if (startup) {
            nextMessageData.allowResume();
        }
        return nextMessageData;
    }

    private void mergeFutureToActivePlaylists(List<DacPlaylist> expiredPlaylists) {
        List<DacPlaylist> newPlaylists = new ArrayList<>();
        for (Iterator<DacPlaylist> iter = futurePlaylists.iterator(); iter
                .hasNext();) {
            DacPlaylist playlist = iter.next();
            if (playlist.isValid()) {
                iter.remove();
                newPlaylists.add(playlist);
            } else if (playlist.isExpired()) {
                iter.remove();
                expiredPlaylists.add(playlist);
            }
        }

        /*
         * Determine if any active playlists need to be moved back into the
         * future list due to the fact that playlists for higher level suites
         * can include multiple playback segments based on distribution of
         * trigger times.
         */
        for (Iterator<DacPlaylist> activeIter = activePlaylists.iterator(); activeIter
                .hasNext();) {
            DacPlaylist activePlaylist = activeIter.next();
            if (activePlaylist.isValid() == false
                    && activePlaylist.isExpired() == false) {
                activeIter.remove();
                /*
                 * Reclassify this playlist as a future playlist. The current
                 * playthrough has concluded.
                 */
                this.futurePlaylists.add(activePlaylist);
                continue;
            }
        }

        for (Iterator<DacPlaylist> newIter = newPlaylists.iterator(); newIter
                .hasNext();) {
            DacPlaylist newPlaylist = newIter.next();

            for (Iterator<DacPlaylist> activeIter = activePlaylists.iterator(); activeIter
                    .hasNext();) {
                DacPlaylist activePlaylist = activeIter.next();
                if ((newPlaylist.getPriority() == activePlaylist.getPriority())
                        && (newPlaylist.getSuite().equals(activePlaylist
                                .getSuite()))) {
                    if ((activePlaylist.isExpired())
                            || (newPlaylist.getCreationTime()
                                    .after(activePlaylist.getCreationTime()))) {
                        activeIter.remove();
                        expiredPlaylists.add(activePlaylist);
                    } else {
                        newIter.remove();
                        expiredPlaylists.add(newPlaylist);
                    }
                    break;
                }
            }
        }

        activePlaylists.addAll(newPlaylists);
    }

    @Override
    @Subscribe
    public void newPlaylistReceived(
            final PlaylistUpdateNotification notification) {
        executorService.submit(new Runnable() {

            @Override
            public void run() {
                try {
                    processNewPlaylist(notification);
                } catch (Throwable t) {
                    logger.error(
                            "Error processing playlist update ["
                                    + (notification != null ? notification
                                            .getPlaylistPath() : notification)
                                    + "]", t);
                }
            }
        });
    }

    private void processNewPlaylist(PlaylistUpdateNotification notification) {

        Path playlistPath = playlistDirectory.resolveSibling(notification
                .getPlaylistPath());
        if (!Files.exists(playlistPath)) {
            logger.warn(
                    "Ignoring an update for a new playlist that no longer exists: {}",
                    notification.getPlaylistPath());
            return;
        }
        DacPlaylist newPlaylist = notification.parseFilepath();
        if (newPlaylist != null) {
            try {
                newPlaylist = JAXB.unmarshal(playlistPath.toFile(),
                        DacPlaylist.class);
            } catch (Throwable e) {
                logger.error(
                        "Unable to parse playlist file: "
                                + playlistPath.toString() + ".", e);
                DefaultMessageLogger.getInstance().logPlaylistError(
                        BMH_COMPONENT.PLAYLIST_MANAGER,
                        BMH_ACTIVITY.PLAYLIST_READ, playlistPath.toString(), e);
                this.checkPurgeUnusablePlaylist(playlistPath);
                return;
            }
            DefaultMessageLogger.getInstance().logPlaylistActivity(newPlaylist,
                    newPlaylist);

            newPlaylist.setPath(playlistPath);
            cache.retrieveAudio(newPlaylist);
            if (newPlaylist.getReplacedMessage() != null) {
                this.cache.expirePlaylistMessage(newPlaylist
                        .getReplacedMessage());
            }

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
                    if (this.delayInterrupts && (this.type == BROADCASTTYPE.BL)) {
                        final DacPlaylistMessage messageData = this.cache
                                .getMessage(newPlaylist.getMessages().get(0));
                        this.publishDelayedNotification(messageData, true);
                    }
                    return;
                }

                if (newPlaylist.isExpired()) {
                    logger.info("Received a notification to expire playlists for suite: "
                            + newPlaylist.getSuite());
                    Collection<DacPlaylist> expired = new ArrayList<>();
                    expired.add(newPlaylist);

                    DacPlaylist expiredActive = findMatchingPlaylist(
                            newPlaylist, activePlaylists);
                    if (expiredActive != null) {
                        expired.add(expiredActive);

                        if (expiredActive == currentPlaylist) {
                            currentPlaylist = null;
                            currentMessages = Collections.emptyList();
                        }
                        activePlaylists.remove(expiredActive);
                    } else {
                        expiredActive = findMatchingPlaylist(newPlaylist,
                                this.futurePlaylists);
                        if (expiredActive != null) {
                            logger.info(
                                    "Expiring future playlist: {} for suite: {}.",
                                    expiredActive, newPlaylist.getSuite());
                            expired.add(expiredActive);
                            futurePlaylists.remove(expiredActive);
                        }
                    }
                    expirePlaylists(expired);
                    return;
                } else if (!newPlaylist.isValid()) {
                    if (this.checkForReplacementPlaylist(newPlaylist,
                            this.futurePlaylists)) {
                        logger.debug(
                                "New playlist: {} doesn't take affect immediately. Queueing...",
                                newPlaylist);
                        Collections.sort(futurePlaylists, QUEUE_ORDER);
                    }
                    return;
                }

                if ((currentPlaylist != null)
                        && (newPlaylist.getPriority() == currentPlaylist
                                .getPriority())
                        && (newPlaylist.getSuite().equals(currentPlaylist
                                .getSuite()))) {
                    if (newPlaylist.getCreationTime().before(
                            currentPlaylist.getCreationTime())) {
                        logger.warn("Received an update to playlist "
                                + currentPlaylist
                                + " that has an older creation date. Ignoring new playlist.");
                        Path old = newPlaylist.getPath();
                        if (!newPlaylist.getPath().equals(old)) {
                            logger.debug("Deleting old playlist " + newPlaylist);
                            expirePlaylist(newPlaylist);
                        }
                        return;
                    }

                    logger.debug("New playlist is an update to current playlist.");

                    setCurrentPlaylist(newPlaylist, true);
                    activePlaylists.remove(currentPlaylist);
                    activePlaylists.add(newPlaylist);
                    Path old = currentPlaylist.getPath();
                    if (!newPlaylist.getPath().equals(old)) {
                        logger.debug("Deleting old playlist " + old);
                        expirePlaylist(currentPlaylist);
                    }

                    currentPlaylist = newPlaylist;
                } else {
                    this.checkForReplacementPlaylist(newPlaylist,
                            this.activePlaylists);
                }
            }

            timer.stop();
            logger.debug("Time for newPlaylistReceived() to merge in new playlist: "
                    + timer.getElapsedTime() + " ms.");
        } else {
            logger.warn("Could not parse file path for playlist update notification: "
                    + notification.getPlaylistPath());
        }
    }

    private boolean checkForReplacementPlaylist(DacPlaylist newPlaylist,
            Collection<DacPlaylist> playlistCollection) {
        boolean added = false;
        DacPlaylist toReplace = findMatchingPlaylist(newPlaylist,
                playlistCollection);
        if (toReplace != null) {
            DacPlaylist expired = null;
            if (newPlaylist.getCreationTime()
                    .after(toReplace.getCreationTime())) {
                playlistCollection.remove(toReplace);
                playlistCollection.add(newPlaylist);
                if (newPlaylist.getTriggerBroadcastId() != null
                        && newPlaylist.getTriggerBroadcastId().equals(
                                toReplace.getTriggerBroadcastId()) == false) {
                    /*
                     * The playlist is not currently playing; but, it will be
                     * triggered soon.
                     */
                    this.handlePotentialDelayedTriggerNotification(newPlaylist);
                }
                expired = toReplace;
                added = true;
            } else {
                logger.warn("Received an update to playlist "
                        + toReplace
                        + " that has an older creation date. Ignoring new playlist.");
                expired = newPlaylist;
            }

            if (!newPlaylist.getPath().equals(toReplace.getPath())) {
                logger.debug("Deleting "
                        + expired
                        + " because it is not active or not able to replace active.");
                expirePlaylist(expired);
            } else {
                logger.debug("Did not delete " + expired
                        + " because it was the same as active");
            }
        } else {
            playlistCollection.add(newPlaylist);
            added = true;
            if (newPlaylist.getTriggerBroadcastId() != null) {
                /*
                 * The playlist is not currently playing; but, it will be
                 * triggered soon.
                 */
                this.handlePotentialDelayedTriggerNotification(newPlaylist);
            }
        }

        return added;
    }

    private static DacPlaylist findMatchingPlaylist(DacPlaylist playlist,
            Collection<DacPlaylist> playlistCollection) {
        for (DacPlaylist compare : playlistCollection) {
            if ((playlist.getPriority() == compare.getPriority())
                    && (playlist.getSuite().equals(compare.getSuite()))) {
                return compare;
            }
        }

        return null;
    }

    /**
     * Set currentMessages based off a playlist. Handles things like periodic
     * scheduling, expiration, and merging. This method differs from
     * {@link #setCurrentPlaylist(DacPlaylist, boolean, boolean)} because it
     * will attempt to schedule without forcing periodic messages and if that
     * fails it will force.
     * 
     * @param playlist
     *            The playlsit that should be set.
     * @param update
     *            When true the new playlist will be merged with the existing
     *            playlist to maintain the position in the list.
     */
    private void setCurrentPlaylist(DacPlaylist playlist, boolean update) {
        boolean result = setCurrentPlaylist(playlist, update, false);
        if (result == false) {
            /*
             * If there are only periodic messages. All periodic messages will
             * be forcefully scheduled regardless of their periodicity.
             */
            setCurrentPlaylist(playlist, update, true);
        }
    }

    /**
     * This method will determine whether or not a
     * {@link MessageDelayedBroadcastNotification} should be published for the
     * specified {@link DacPlaylist}. A
     * {@link MessageDelayedBroadcastNotification} will be published when the
     * {@link DacPlaylist} includes a trigger message that is a Warning (and
     * there is an active broadcast live session). This scenario is completely
     * different from when a new playlist arrives for a warning message that is
     * in the currently playing suite because in the case of the trigger message
     * the switch over to the new suite has not already occurred; so, the active
     * live broadcast is preventing the new suite from playing.
     * 
     * @param playlist
     *            the specified {@link DacPlaylist}
     */
    private void handlePotentialDelayedTriggerNotification(
            final DacPlaylist playlist) {
        if (this.delayInterrupts == false || this.type != BROADCASTTYPE.BL
                || playlist.isInterrupt()) {
            return;
        }

        DacPlaylistMessageId id = null;
        for (DacPlaylistMessageId dacId : playlist.getMessages()) {
            if (dacId.getBroadcastId() == playlist.getTriggerBroadcastId()) {
                id = dacId;
                break;
            }
        }
        if (id == null) {
            return;
        }
        DacPlaylistMessage messageData = this.cache.getMessage(id);
        if (messageData == null) {
            logger.warn(
                    "Failed to find a cached playlist message for playlist message id {}.",
                    id.toString());
            return;
        }
        if (messageData.isWarning() == false || messageData.getPlayCount() > 0
                || messageData.isInitialBLDelayNotificationSent()
                || messageData.isValid() == false) {
            /*
             * Not a Warning or already played once - no notification required.
             */
            return;
        }

        this.publishDelayedNotification(messageData, playlist.isInterrupt());
    }

    private void publishDelayedNotification(DacPlaylistMessage messageData,
            final boolean interrupt) {
        MessageDelayedBroadcastNotification notification = new MessageDelayedBroadcastNotification(
                messageData, interrupt);
        this.eventBus.post(notification);
    }

    /**
     * Set currentMessages based off a playlist. Handles things like periodic
     * scheduling, expiration, and merging.
     * 
     * @param playlist
     *            The playlsit that should be set.
     * @param update
     *            When true the new playlist will be merged with the existing
     *            playlist to maintain the position in the list. If an update
     *            fails to produce new messages then a full refresh will be
     *            attempted.
     * @param forceSchedulePeriodic
     * @return true if this method was able to determine a valid playlist,
     *         otherwise false. If false is returned the existing playlist will
     *         continue to play.
     */
    private boolean setCurrentPlaylist(DacPlaylist playlist, boolean update,
            boolean forceSchedulePeriodic) {
        if (!playlist.isValid() && !playlist.isInterrupt()) {
            return false;
        }
        List<DacPlaylistMessageId> playlistMessages = playlist.getMessages();

        /*
         * When doing an update then the past will contain messages that played
         * already in the current list and also any new messages whose position
         * is before the currently playing message.
         */
        List<DacPlaylistMessageId> past = Collections.emptyList();
        if (update && !currentMessages.isEmpty()) {
            past = new ArrayList<>(messageIndex);
            List<DacPlaylistMessageId> oldPast = currentMessages.subList(0,
                    messageIndex);
            for (DacPlaylistMessageId id : oldPast) {
                if (playlistMessages.contains(id)) {
                    past.add(id);
                }
            }

            List<DacPlaylistMessageId> newUnperiodicMessages = new ArrayList<>();
            for (DacPlaylistMessageId messageId : playlistMessages) {
                int index = past.indexOf(messageId);
                if (index >= 0) {
                    past.addAll(index, newUnperiodicMessages);
                    newUnperiodicMessages.clear();
                } else {
                    DacPlaylistMessage messageData = cache
                            .getMessage(messageId);
                    if (messageData != null
                            && messageData.isValid()
                            && (!messageData.isPeriodic()
                                    || (messageData.getPlayCount() == 0) || forceSchedulePeriodic)) {
                        newUnperiodicMessages.add(messageId);
                    }
                }
            }
        }
        /* Contains messages that should be scheduled periodically. */
        SortedMap<Long, DacPlaylistMessageId> periodicMessages = new TreeMap<>();
        /*
         * Contains messages that should be scheduled in order(including
         * periodic messages playing for the first time or forced periodic
         * message).
         */
        List<DacPlaylistMessageId> unperiodicMessages = new ArrayList<>();
        /*
         * 
         */
        final boolean checkAllForDelay = this.delayInterrupts
                && (this.type == BROADCASTTYPE.BL);
        for (DacPlaylistMessageId id : playlistMessages) {
            if (checkAllForDelay) {
                DacPlaylistMessage messageData = cache.getMessage(id);
                if (messageData != null
                        && (playlist.isInterrupt() == false && messageData
                                .isWarning())
                        && messageData.getPlayCount() == 0
                        && messageData.isInitialBLDelayNotificationSent() == false
                        && messageData.isValid()) {
                    /*
                     * Rqmt BMH0094: a user needs to be notified when an active
                     * live broadcast is preventing the broadcast of another
                     * interrupt or a warning.
                     */
                    this.publishDelayedNotification(messageData,
                            playlist.isInterrupt());
                    messageData.setInitialBLDelayNotificationSent(true);
                }
            }
            if (!past.contains(id)) {
                DacPlaylistMessage messageData = cache.getMessage(id);
                if (messageData != null && messageData.isPeriodic()
                        && (messageData.getPlayCount() > 0)
                        && !forceSchedulePeriodic) {
                    long lastTransmitMillis = System.currentTimeMillis();
                    if (messageData.getLastTransmitTime() != null) {
                        lastTransmitMillis = messageData.getLastTransmitTime()
                                .getTimeInMillis();
                    }
                    /*
                     * Playback interval is based on periodicity which must be
                     * non-null to reach this point. So, there is no need to
                     * verify that it has been set as has been done with the
                     * last transmit time.
                     */
                    long nextPlayTime = lastTransmitMillis
                            + messageData.getPlaybackInterval();
                    while (periodicMessages.containsKey(nextPlayTime)) {
                        nextPlayTime += 1;
                    }
                    periodicMessages.put(nextPlayTime, id);
                } else {
                    unperiodicMessages.add(id);
                }
            }
        }

        List<DacPlaylistMessageId> predictedMessages = new ArrayList<>();
        List<MessagePlaybackPrediction> predictions = new ArrayList<>();
        long cycleTime = 0;
        long nextMessageTime = TimeUtil.currentTimeMillis();
        /* Any messages in past are included in the current list. */
        for (DacPlaylistMessageId messageId : past) {
            predictedMessages.add(messageId);
            DacPlaylistMessage messageData = cache.getMessage(messageId);
            if (messageData == null) {
                continue;
            }
            predictions.add(new MessagePlaybackPrediction(messageData));

            long playbackTime;
            try {
                playbackTime = cache.getPlaybackTime(messageId);
            } catch (NoSoundFileException e) {
                playbackTime = 0;
                logger.warn("Message " + messageId
                        + " has no soundFile attribute.");
            }

            cycleTime += playbackTime;
            Calendar lastTransmitTime = messageData.getLastTransmitTime();
            /*
             * If a message is playing now move the nextMessageTime to after
             * that message.
             */
            if ((lastTransmitTime != null)
                    && ((lastTransmitTime.getTimeInMillis() + playbackTime) > nextMessageTime)) {
                nextMessageTime = lastTransmitTime.getTimeInMillis()
                        + playbackTime;
            }
        }
        for (DacPlaylistMessageId messageId : unperiodicMessages) {
            while (!periodicMessages.isEmpty()
                    && (periodicMessages.firstKey() <= nextMessageTime)) {
                DacPlaylistMessageId periodicId = periodicMessages
                        .remove(periodicMessages.firstKey());
                DacPlaylistMessage messageData = cache.getMessage(periodicId);
                if (messageData != null && messageData.isValid(nextMessageTime)) {
                    logger.debug("Scheduling periodic message [" + periodicId
                            + "].");
                    try {
                        MessagePlaybackPrediction prediction = new MessagePlaybackPrediction(
                                nextMessageTime, messageData);
                        long playbackTime = cache.getPlaybackTime(periodicId,
                                prediction.getNextTransmitTime());

                        predictions.add(prediction);
                        predictedMessages.add(periodicId);

                        cycleTime += playbackTime;
                        nextMessageTime += playbackTime;
                    } catch (NoSoundFileException e) {
                        logger.error("Message " + messageId
                                + " has no soundFile attribute. Skipping.");
                    }
                }
            }

            DacPlaylistMessage messageData = cache.getMessage(messageId);

            /*
             * ignore start/expire times for interrupt playlists, we just want
             * to play the message.
             */
            if (messageData != null
                    && (playlist.isInterrupt() || messageData
                            .isValid(nextMessageTime))) {
                try {
                    MessagePlaybackPrediction prediction = new MessagePlaybackPrediction(
                            nextMessageTime, messageData);
                    long playbackTime = cache.getPlaybackTime(messageId,
                            prediction.getNextTransmitTime());
                    predictions.add(prediction);
                    predictedMessages.add(messageId);
                    cycleTime += playbackTime;
                    nextMessageTime += playbackTime;
                } catch (NoSoundFileException e) {
                    logger.error("Message " + messageId
                            + " has no soundFile attribute. Skipping.");
                }
            } else {
                this.sendNotBroadcastNotification(messageData);
            }
        }

        if (predictions.size() == past.size()) {
            if (predictions.isEmpty()) {
                return false;
            } else if ((currentMessages.size() != messageIndex)
                    && (messageIndex != predictions.size())) {
                /*
                 * If there are no new messages and the last message isn't
                 * playing right now then attempt to generate a fresh list
                 * without recycling.
                 */
                return setCurrentPlaylist(playlist, false,
                        forceSchedulePeriodic);
            }
        }

        PlaylistNotification notification = new PlaylistNotification(
                playlist.getSuite(), playlist.getTransmitterGroup(),
                predictions, cycleTime);
        if (!periodicMessages.isEmpty()) {
            List<MessagePlaybackPrediction> periodicPredictions = new ArrayList<>(
                    periodicMessages.size());
            for (Entry<Long, DacPlaylistMessageId> entry : periodicMessages
                    .entrySet()) {
                DacPlaylistMessage messageData = cache.getMessage(entry
                        .getValue());
                if (messageData != null) {
                    periodicPredictions.add(new MessagePlaybackPrediction(entry
                            .getKey(), messageData));
                }
            }
            notification.setPeriodicMessages(periodicPredictions);
        }
        eventBus.post(notification);
        currentMessages = predictedMessages;
        messageIndex = past.size();
        return true;
    }

    private void expirePlaylists(Collection<DacPlaylist> expiredPlaylists) {
        for (DacPlaylist playlist : expiredPlaylists) {
            expirePlaylist(playlist);
        }
    }

    private void expirePlaylist(DacPlaylist expiredPlaylist) {
        cache.removeExpiredMessages(expiredPlaylist);
    }

    /**
     * Returns all currently active playlists as well as any playlists that may
     * be active in the future.
     * 
     * @return a {@link Collection} of {@link Playlist}s.
     */
    Collection<DacPlaylist> getActivePlaylists() {
        Collection<DacPlaylist> playlists = new HashSet<>();
        synchronized (playlistMessgeLock) {
            playlists.addAll(activePlaylists);
            /*
             * The future playlists are not actually active. However, they have
             * the potential to become active in the future. So, we want to
             * ensure that the associated messages are retained.
             */
            playlists.addAll(futurePlaylists);
            playlists.addAll(interrupts);
        }
        return playlists;
    }

    public void lockInterrupts(final BROADCASTTYPE type) {
        logger.info("Delaying interrupt playback. No interrupts will be played until further notice.");
        this.delayInterrupts = true;
        this.type = type;
    }

    public void resumeInterrupts() {
        logger.info("Resuming interrupt playback.");
        this.delayInterrupts = false;
        this.type = null;
    }

    /**
     * Determines if we need to notify BMH users via a
     * {@link MessageNotBroadcastNotification} that the specified
     * {@link DacPlaylistMessage} expired before it could be broadcast even
     * though it had been added to the playlist.
     * 
     * @param message
     *            the specified {@link DacPlaylistMessage}
     */
    private void sendNotBroadcastNotification(DacPlaylistMessage message) {
        if (message == null
                || message.requiresExpirationNoPlaybackNotification() == false) {
            // no notification is required.
            return;
        }

        this.eventBus.post(new MessageNotBroadcastNotification(message));
    }

    /**
     * Post a No Playback Message Notification.
     */
    public void sendNoPlaybackNotification() {
        this.eventBus.post(new NoPlaybackMessageNotification());
    }

    private void schedulePlaylistLoad(final List<String> playlistsToLoad) {
        if (playlistsToLoad.isEmpty()) {
            // only one playlist existed.
            this.cache.enableCleanup();
            return;
        }
        this.executorService.submit(new LoadPlaylistTask(playlistsToLoad));
    }

    private class LoadPlaylistTask implements PrioritizableCallable<Object> {

        private final List<String> playlistsToLoad;

        public LoadPlaylistTask(final List<String> playlistsToLoad) {
            if (playlistsToLoad == null || playlistsToLoad.isEmpty()) {
                throw new IllegalArgumentException(
                        "Required argument playlistsToLoad must include at least one playlist.");
            }
            this.playlistsToLoad = playlistsToLoad;
        }

        @Override
        public Object call() throws Exception {
            try {
                for (String playlist : playlistsToLoad) {
                    logger.info("LoadPlaylistTask processing playlist: {} ...",
                            playlist);
                    processPlaylist(playlist);
                }
                logger.info("LoadPlaylistTask has finished loading additional playlists.");
            } catch (Exception e) {
                throw e;
            } finally {
                cache.enableCleanup();
            }

            return null;
        }

        @Override
        public Integer getPriority() {
            return PriorityBasedExecutorService.PRIORITY_HIGH;
        }
    }
}