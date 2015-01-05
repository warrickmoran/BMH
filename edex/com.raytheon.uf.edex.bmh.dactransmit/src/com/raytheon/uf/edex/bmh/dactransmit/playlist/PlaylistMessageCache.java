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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.JAXB;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylist;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessage;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessageId;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.edex.bmh.dactransmit.events.CriticalErrorEvent;
import com.raytheon.uf.edex.bmh.dactransmit.exceptions.NoSoundFileException;
import com.raytheon.uf.edex.bmh.dactransmit.ipc.ChangeDecibelTarget;
import com.raytheon.uf.edex.bmh.dactransmit.util.NamedThreadFactory;

/**
 * Cache for {@code PlaylistMessage} objects. Stores the contents of each audio
 * file for each item to be played during this session.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 10, 2014  #3286     dgilling     Initial creation
 * Jul 14, 2014  #3286     dgilling     Use logback for logging, switch to
 *                                      proper playlist objects.
 * Jul 16, 2014  #3286     dgilling     Add shutdown() to take down executor.
 * Jul 29, 2014  #3286     dgilling     Add getPlaylist().
 * Aug 04, 2014  #3286     dgilling     Remove getPlaylist().
 * Aug 12, 2014  #3286     dgilling     Support tones playback.
 * Aug 19, 2014  #3532     bkowal       The raw audio data will now be attenuated or
 *                                      amplified based on the transmitter decibel range
 *                                      before it is cached. Initial implementation of
 *                                      prioritization.
 * Aug 22, 2014  #3286     dgilling     Re-factor based on PriorityBasedExecutorService.
 * Sep 5, 2014   #3532     bkowal       Use a decibel target instead of a range.
 * Sep 08, 2014  #3286     dgilling     Allow expired items to be removed from
 *                                      cache.
 * Sep 11, 2014  #3606     dgilling     Prevent exceptions when messages don't
 *                                      have an associated sound file.
 * Sep 12, 2014  #3588     bsteffen     Support audio fragments.
 * Sep 12, 2014  #3485     bsteffen     Shutdown purge job on shutdown.
 * Oct 2, 2014   #3642     bkowal       Updated to use the audio buffer abstraction
 * Oct 30, 2014  #3617     dgilling     Take tone blackout period into account
 *                                      when calculating playback time.
 * Nov 17, 2014  #3630     bkowal       Added doesMessageFileExist.
 * Jan 05, 2015  #3913     bsteffen     Handle future replacements.
 * 
 * 
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

public final class PlaylistMessageCache implements IAudioJobListener {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final int PURGE_JOB_INTERVAL = 15;

    private static final int THREAD_POOL_MAX_SIZE = 3;

    private static final int PURGE_THREADS = 1;

    /*
     * Note: the current implementation does not allow a task with a higher
     * priority to preempt a task that has already started with a lower priority
     * when all of the available threads are in use.
     */

    private static final long SECONDS_TO_BYTES_CONV_FACTOR = 8000L;

    /**
     * Estimated file size for SAME tones playback (not including alert tone):
     * based on 10 seconds of pauses plus an estimated 1 second for tones
     * playback.
     */
    private static final long SAME_TONE_ESTIMATE = 11 * SECONDS_TO_BYTES_CONV_FACTOR;

    /**
     * Estimated file size for alert tones playback: based on 9 second alert
     * tone plus 2 second pause prior to tone.
     */
    private static final long ALERT_TONE_ESTIMATE = 11 * SECONDS_TO_BYTES_CONV_FACTOR;

    private final Path messageDirectory;

    private final ExecutorService cacheThreadPool;

    private final ScheduledExecutorService purgeThreadPool;

    private final ConcurrentMap<DacPlaylistMessageId, DacPlaylistMessage> cachedMessages;

    private final ConcurrentMap<DacPlaylistMessage, IAudioFileBuffer> cachedFiles;

    private final ConcurrentMap<DacPlaylistMessageId, Future<IAudioFileBuffer>> cacheStatus;

    private final ConcurrentMap<String, CachedAudioRetrievalTask> cachedRetrievalTasks;

    private final EventBus eventBus;

    private double dbTarget;

    private TimeZone timezone;

    private final PlaylistScheduler scheduler;

    public PlaylistMessageCache(Path messageDirectory,
            PlaylistScheduler playlistScheduler, EventBus eventBus,
            double dbTarget, TimeZone timezone) {
        this.messageDirectory = messageDirectory;
        this.cacheThreadPool = new PriorityBasedExecutorService(
                THREAD_POOL_MAX_SIZE, new NamedThreadFactory(
                        "PlaylistMessageCache"));
        this.cachedMessages = new ConcurrentHashMap<>();
        this.cachedFiles = new ConcurrentHashMap<>();
        this.cacheStatus = new ConcurrentHashMap<>();
        this.cachedRetrievalTasks = new ConcurrentHashMap<>();
        this.eventBus = eventBus;
        this.dbTarget = dbTarget;
        this.timezone = timezone;
        this.scheduler = playlistScheduler;

        this.purgeThreadPool = Executors.newScheduledThreadPool(PURGE_THREADS,
                new NamedThreadFactory("MessageCachePurge"));
        Runnable purgeJob = createPurgeJob();
        this.purgeThreadPool.scheduleWithFixedDelay(purgeJob,
                PURGE_JOB_INTERVAL, PURGE_JOB_INTERVAL, TimeUnit.MINUTES);
    }

    /**
     * Stops all background threads this class was using. After this method is
     * called, this instance cannot be restarted.
     */
    public void shutdown() {
        cacheThreadPool.shutdown();
        purgeThreadPool.shutdown();
    }

    /**
     * Adds all messages in the list to the cache.
     * 
     * @param playlist
     *            List of messages to cache.
     */
    public void retrieveAudio(final List<DacPlaylistMessageId> playlist) {
        for (DacPlaylistMessageId message : playlist) {
            retrieveAudio(message);
        }
    }

    /**
     * Cache a single message.
     * 
     * @param message
     *            Message to cache.
     */
    public void retrieveAudio(final DacPlaylistMessageId id) {
        if (!cacheStatus.containsKey(id)) {
            Future<IAudioFileBuffer> jobStatus = scheduleFileRetrieval(
                    PriorityBasedExecutorService.PRIORITY_NORMAL, id);
            cacheStatus.put(id, jobStatus);
        }
    }

    /**
     * Schedules an audio file retrieval
     * 
     * @param priority
     *            indicates the importance of the retrieval
     * @param id
     *            identifies the audio file to retrieve
     * @return the results of an asynchronous computation; used to track the job
     *         status.
     */
    private Future<IAudioFileBuffer> scheduleFileRetrieval(final int priority,
            final DacPlaylistMessageId id) {
        return this.scheduleFileRetrieval(priority, id, null);
    }

    /**
     * Schedules an audio file retrieval that will be tracked.
     * 
     * @param priority
     *            indicates the importance of the retrieval
     * @param id
     *            identifies the audio file to retrieve
     * @param taskId
     *            used to track the progress of the retrieval task consisting of
     *            one or multiple audio retrievals
     * @return the results of an asynchronous computation; used to track the job
     *         status.
     */
    private Future<IAudioFileBuffer> scheduleFileRetrieval(final int priority,
            final DacPlaylistMessageId id, final String taskId) {
        Callable<IAudioFileBuffer> retrieveAudioJob = new RetrieveAudioJob(
                priority, this.dbTarget, this.getMessage(id), this, taskId);

        return cacheThreadPool.submit(retrieveAudioJob);
    }

    /**
     * Returns the {@code AudioFileBuffer} from cache that corresponds to the
     * specified {@code PlaylistMessage}. If the message hasn't been completely
     * cached yet, this method will block until it has been cached.
     * 
     * @param message
     *            The {@code PlaylistMessage} that corresponds to the audio file
     *            data to retrieve from cache.
     * @return The audio file's data, or {@code null} if the data isn't in the
     *         cache.
     */
    public IAudioFileBuffer getAudio(final DacPlaylistMessage message) {
        Future<IAudioFileBuffer> fileStatus = cacheStatus
                .get(new DacPlaylistMessageId(message.getBroadcastId()));
        if (fileStatus != null) {
            IAudioFileBuffer buffer = cachedFiles.get(message);
            if (buffer == null) {
                try {
                    /*
                     * TODO: slightly increase the priority of the task that we
                     * are waiting for?
                     */
                    buffer = fileStatus.get();
                    cachedFiles.put(message, buffer);
                } catch (InterruptedException e) {
                    logger.error(
                            "Exception thrown waiting on cache status for "
                                    + message, e);
                } catch (ExecutionException e) {
                    /*
                     * TODO: handle failed data retrieval.
                     */
                    logger.error(e.getMessage(), e.getCause());
                    CriticalErrorEvent event = new CriticalErrorEvent(
                            e.getMessage(), e.getCause());
                    this.eventBus.post(event);
                }
            }

            if (buffer != null) {
                buffer.rewind();
            }
            return buffer;
        }

        return null;
    }

    public AudioFileBuffer refreshTimeSensitiveAudio(
            DynamicTimeAudioFileBuffer dynamicBuffer,
            final DacPlaylistMessage message, Calendar transmission) {
        /*
         * Currently, the data is in GMT format. So, it will be necessary to
         * convert it to the correct timezone.
         */
        transmission.setTimeZone(this.timezone);
        try {
            return dynamicBuffer.finalizeFileBuffer(transmission);
        } catch (NotTimeCachedException e) {
            logger.error("Failed to update dynamic time audio!", e);
            CriticalErrorEvent event = new CriticalErrorEvent(
                    "Failed to update dynamic time audio!", e);
            this.eventBus.post(event);
            return null;
        }
    }

    /**
     * Determines whether a message file associated with the specified
     * {@link DacPlaylistMessageId} actually exists. Currently used at startup
     * to ensure that a playlist does not contain references to a message that
     * no longer exists (most likely due to expiration). Possibly handles a rare
     * scenario.
     * 
     * @param id
     *            the specified {@link DacPlaylistMessageId}
     * @return true if the file does exist; false, otherwise
     */
    public boolean doesMessageFileExist(DacPlaylistMessageId id) {
        Path messagePath = messageDirectory.resolve(id.getBroadcastId()
                + ".xml");
        return Files.exists(messagePath);
    }

    /**
     * Return the full message for the given id. If the message has not been
     * loaded into the cache this will access the filesystem.
     * 
     * @param id
     *            id of a playlist message
     * @return the full message.
     */
    public DacPlaylistMessage getMessage(DacPlaylistMessageId id) {
        DacPlaylistMessage message = cachedMessages.get(id);
        if (message == null) {
            Path messagePath = messageDirectory.resolve(id.getBroadcastId()
                    + ".xml");
            message = JAXB.unmarshal(messagePath.toFile(),
                    DacPlaylistMessage.class);
            message.setPath(messagePath);
            cachedMessages.put(id, message);
        }
        message.setReplaceTime(id.getReplaceTime());
        return message;
    }

    /**
     * Calculate the playback time of the specified message (including all
     * tones), based on file size.
     * 
     * @param messageId
     *            The {@code DacPlaylistMessageId} of the message to get the
     *            playback time for.
     * @return The playback time in milliseconds.
     * @throws NoSoundFileException
     *             If the message data contains no soundFile attribute.
     */
    public long getPlaybackTime(DacPlaylistMessageId messageId)
            throws NoSoundFileException {
        return getPlaybackTime(messageId, null);
    }

    /**
     * Calculate the playback time of the specified message (including all
     * tones), based on file size.
     * 
     * @param messageId
     *            The {@code DacPlaylistMessageId} of the message to get the
     *            playback time for.
     * @param startTime
     *            A {@code Calendar} containing the time the message will be
     *            played. If {@code null}, playback time of tones will be
     *            ignored.
     * @return The playback time in milliseconds.
     * @throws NoSoundFileException
     *             If the message data contains no soundFile attribute.
     */
    public long getPlaybackTime(DacPlaylistMessageId messageId,
            Calendar startTime) throws NoSoundFileException {
        DacPlaylistMessage message = cachedMessages.get(messageId);
        boolean includeTones = (startTime != null) ? getMessage(messageId)
                .shouldPlayTones(startTime) : false;

        long fileSize = 0;
        if (cachedFiles.containsKey(message)) {
            fileSize = cachedFiles.get(message).capacity(includeTones);
        } else {
            List<String> soundFiles = cachedMessages.get(messageId)
                    .getSoundFiles();
            if (soundFiles == null || soundFiles.isEmpty()) {
                throw new NoSoundFileException("Message " + messageId
                        + " contains no soundFile attributes.");
            }
            for (String pathString : soundFiles) {

                if ((pathString != null) && (!pathString.isEmpty())) {
                    Path audioFile = Paths.get(pathString);
                    try {
                        fileSize = Files.size(audioFile);
                    } catch (Exception e) {
                        logger.error("Unable to retrieve file size for file: "
                                + audioFile.toString(), e);
                    }

                } else {
                    throw new NoSoundFileException("Message " + messageId
                            + " contains an empty soundFile attribute.");
                }
            }
            if (includeTones) {
                fileSize += SAME_TONE_ESTIMATE;
                if (message.isAlertTone()) {
                    fileSize += ALERT_TONE_ESTIMATE;
                }
            }
        }

        /* For ULAW encoded files, 160 bytes = 20 ms of playback time. */
        long playbackTime = fileSize / 160L * 20L;
        return playbackTime;
    }

    /**
     * Listens for changes to the decibel target
     * 
     * @param changeEvent
     *            a notification including the updated decibel target
     */
    @Subscribe
    public void changeDecibelRange(ChangeDecibelTarget changeEvent) {
        this.dbTarget = changeEvent.getDbTarget();

        CachedAudioRetrievalTask task = new CachedAudioRetrievalTask(
                new HashSet<DacPlaylistMessage>(cachedFiles.keySet()));
        this.cachedRetrievalTasks.put(task.getIdentifier(), task);
        logger.info("Initiating cache update " + task.getIdentifier());
        for (DacPlaylistMessage message : cachedFiles.keySet()) {
            DacPlaylistMessageId messageId = new DacPlaylistMessageId(
                    message.getBroadcastId());
            Future<IAudioFileBuffer> jobStatus = scheduleFileRetrieval(
                    PriorityBasedExecutorService.PRIORITY_LOW, messageId);
            cacheStatus.replace(messageId, jobStatus);
        }

        logger.info("Updated transmitter decibel target to: " + this.dbTarget);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.edex.bmh.dactransmit.playlist.IAudioJobListener#
     * audioRetrievalFinished(java.lang.String,
     * com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessage)
     */
    @Override
    public void audioRetrievalFinished(String taskId, DacPlaylistMessage message) {
        if (this.cachedRetrievalTasks.get(taskId) != null) {
            CachedAudioRetrievalTask task = this.cachedRetrievalTasks
                    .get(taskId);
            task.retrievalFinished(message);
            if (task.isComplete()) {
                logger.info("Finished cache update " + task.getIdentifier()
                        + " in " + TimeUtil.prettyDuration(task.getDuration())
                        + ".");
                this.cachedRetrievalTasks.remove(message);
            }
        }
    }

    /**
     * Remove any expired messages referenced in this playlist from the cache.
     * After removing the messages from cache, the playlist file will be deleted
     * from disk.
     * 
     * @param playlist
     *            Expired playlist.
     */
    public void removeExpiredMessages(final DacPlaylist playlist) {
        try {
            Runnable removeFromCacheJob = createUncacheExpiredPlaylistJob(playlist);
            purgeThreadPool.submit(removeFromCacheJob);
        } catch (Exception e) {
            logger.error("Cannot schedule playlist " + playlist
                    + " for removal.", e);
        }
    }

    private Runnable createPurgeJob() {
        Runnable job = new Runnable() {

            @Override
            public void run() {
                Collection<DacPlaylist> playlists = scheduler
                        .getActivePlaylists();
                Set<DacPlaylistMessageId> activeMessageIds = new HashSet<>();
                for (DacPlaylist playlist : playlists) {
                    activeMessageIds.addAll(playlist.getMessages());
                }

                for (DacPlaylistMessageId messageId : cachedMessages.keySet()) {
                    /*
                     * we remove the cached audio buffer separate from the
                     * cached message data because we need the message data to
                     * remain cached so that any playlists that reference the
                     * expired message can know that the message has expired.
                     * But once expiration hits, we no longer need the audio
                     * data.
                     */
                    if (isExpired(messageId)) {
                        purgeAudio(messageId);
                    }

                    if (!activeMessageIds.contains(messageId)) {
                        try {
                            purgeAudio(messageId);
                            purgeMessage(messageId);
                        } catch (IOException e) {
                            logger.error("Error removing message " + messageId
                                    + " from cache.", e);
                        }
                    }
                }
            }
        };
        return job;
    }

    private Runnable createUncacheExpiredPlaylistJob(final DacPlaylist playlist) {
        Runnable job = new Runnable() {

            @Override
            public void run() {
                for (DacPlaylistMessageId messageId : playlist.getMessages()) {
                    if (isExpired(messageId)) {
                        purgeAudio(messageId);
                    }
                }

                try {
                    Files.delete(playlist.getPath());
                    logger.info("Deleted " + playlist.getPath());
                } catch (IOException e) {
                    logger.error(
                            "Error deleting playlist " + playlist.getPath()
                                    + " from disk.", e);
                }
            }
        };
        return job;
    }

    private boolean isExpired(final DacPlaylistMessageId messageId) {
        long playbackTime;
        try {
            playbackTime = getPlaybackTime(messageId);
        } catch (NoSoundFileException e) {
            playbackTime = 0;
            logger.warn("Unable to determine playback time for message "
                    + messageId);
        }
        long purgeTime = playbackTime
                + getMessage(messageId).getExpire().getTimeInMillis();

        long currentTime = TimeUtil.currentTimeMillis();

        return (currentTime >= purgeTime);
    }

    private void purgeMessage(final DacPlaylistMessageId messageId)
            throws IOException {
        logger.debug("Removing message " + messageId + " from cache.");

        DacPlaylistMessage message = cachedMessages.remove(messageId);
        if (message != null) {
            Path path = message.getPath();
            if (path != null) {
                Files.delete(path);
                logger.info("Deleted " + message.getPath());
            }
        }
    }

    private void purgeAudio(final DacPlaylistMessageId messageId) {
        logger.debug("Removing audio for message " + messageId + " from cache.");

        Future<IAudioFileBuffer> jobStatus = cacheStatus.remove(messageId);
        if (jobStatus != null) {
            jobStatus.cancel(true);
        }

        DacPlaylistMessage message = cachedMessages.get(messageId);
        if (message != null) {
            cachedFiles.remove(message);
        }
    }
}