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
import java.util.concurrent.Future;

import javax.xml.bind.JAXB;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylist;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessage;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessageId;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.edex.bmh.dactransmit.dacsession.DacSession;
import com.raytheon.uf.edex.bmh.dactransmit.dacsession.DacSessionConfig;
import com.raytheon.uf.edex.bmh.dactransmit.events.CriticalErrorEvent;
import com.raytheon.uf.edex.bmh.dactransmit.exceptions.NoSoundFileException;
import com.raytheon.uf.edex.bmh.dactransmit.ipc.ChangeDecibelTarget;
import com.raytheon.uf.edex.bmh.msg.logging.DefaultMessageLogger;
import com.raytheon.uf.edex.bmh.msg.logging.ErrorActivity.BMH_ACTIVITY;
import com.raytheon.uf.edex.bmh.msg.logging.ErrorActivity.BMH_COMPONENT;

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
 * Jan 05, 2015  #3651     bkowal       Use {@link DefaultMessageLogger} to log msg errors.
 * Jan 16, 2015  #3928     bsteffen     Fix purging of old playlists on dac startup.
 * Feb 06, 2015  #4071     bsteffen     Consolidate threading.
 * Feb 24, 2015  #4160     bsteffen     Do not purge message files.
 * Mar 05, 2015  #4222     bkowal       Handle messages that never expire.
 * 
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

public final class PlaylistMessageCache implements IAudioJobListener {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final long PURGE_JOB_INTERVAL = 15 * TimeUtil.MILLIS_PER_MINUTE;

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

    private final ExecutorService executorService;

    private final ConcurrentMap<DacPlaylistMessageId, DacPlaylistMessage> cachedMessages;

    private final ConcurrentMap<DacPlaylistMessage, IAudioFileBuffer> cachedFiles;

    private final ConcurrentMap<DacPlaylistMessageId, Future<IAudioFileBuffer>> cacheStatus;

    private final ConcurrentMap<String, CachedAudioRetrievalTask> cachedRetrievalTasks;

    private final EventBus eventBus;

    private double dbTarget;

    private TimeZone timezone;

    private final PlaylistScheduler scheduler;

    private long lastPurgeTime;

    public PlaylistMessageCache(DacSession dacSession,
            PlaylistScheduler playlistScheduler) {
        DacSessionConfig config = dacSession.getConfig();
        this.messageDirectory = config.getInputDirectory().resolve("messages");
        this.cachedMessages = new ConcurrentHashMap<>();
        this.cachedFiles = new ConcurrentHashMap<>();
        this.cacheStatus = new ConcurrentHashMap<>();
        this.cachedRetrievalTasks = new ConcurrentHashMap<>();
        this.eventBus = dacSession.getEventBus();
        this.dbTarget = config.getDbTarget();
        this.timezone = config.getTimezone();
        this.scheduler = playlistScheduler;
        this.executorService = dacSession.getAsyncExecutor();
    }

    /**
     * Adds all messages in the list to the cache.
     * 
     * @param playlist
     *            List of messages to cache.
     */
    public void retrieveAudio(DacPlaylist playlist) {
        int index = 0;
        for (DacPlaylistMessageId message : playlist.getMessages()) {
            /*
             * This is intended to cause higher priority lists to get scheduled
             * before lower priority lists and also to get messages at the
             * beginning of the list scheduled before messages at the end of the
             * list.
             */
            int priority = PriorityBasedExecutorService.PRIORITY_NORMAL
                    + playlist.getPriority() * 100 + index;
            retrieveAudio(message, priority);
            index += 1;
        }
    }

    /**
     * Cache a single message.
     * 
     * @param message
     *            Message to cache.
     */
    private void retrieveAudio(final DacPlaylistMessageId id, int priority) {
        if (!cacheStatus.containsKey(id)) {
            Future<IAudioFileBuffer> jobStatus = scheduleFileRetrieval(
                    priority, id);
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
        return executorService.submit(retrieveAudioJob);
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
        schedulePurge();
        Future<IAudioFileBuffer> fileStatus = cacheStatus
                .get(new DacPlaylistMessageId(message.getBroadcastId()));
        if (fileStatus != null) {
            IAudioFileBuffer buffer = cachedFiles.get(message);
            if (buffer == null) {
                try {
                    long startTime = System.currentTimeMillis();
                    buffer = fileStatus.get();
                    long time = System.currentTimeMillis() - startTime;
                    if(time > 1){
                        logger.info(
                                "Spent {}ms waiting for audio for message with id={}.",
                                time, message.getBroadcastId());
                    }
                    cachedFiles.put(message, buffer);
                } catch (InterruptedException e) {
                    logger.error(
                            "Exception thrown waiting on cache status for "
                                    + message, e);
                    DefaultMessageLogger.getInstance().logError(
                            BMH_COMPONENT.DAC_TRANSMIT,
                            BMH_ACTIVITY.AUDIO_READ, message, e);
                } catch (ExecutionException e) {
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
            DefaultMessageLogger.getInstance().logError(
                    BMH_COMPONENT.DAC_TRANSMIT, BMH_ACTIVITY.AUDIO_READ,
                    message, e);
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
        DacPlaylistMessage message = getMessage(messageId);
        boolean includeTones = (startTime != null) ? message
                .shouldPlayTones(startTime) : false;

        IAudioFileBuffer buffer = cachedFiles.get(message);
        if (buffer == null) {
            Future<IAudioFileBuffer> status = cacheStatus.get(messageId);
            if (status != null && status.isDone()) {
                try {
                    buffer = status.get();
                } catch (InterruptedException | ExecutionException e) {
                    /*
                     * Ignore for now, when the message is played the same
                     * exception will be thrown and logged in getAudio
                     */
                }
            }
        }

        long fileSize = 0;
        if (buffer != null) {
            fileSize = buffer.capacity(includeTones);
        } else {
            List<String> soundFiles = message.getSoundFiles();
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
        executorService.submit(new PurgePlaylistTask(playlist));
    }

    private boolean isExpired(final DacPlaylistMessageId messageId) {
        if (getMessage(messageId).getExpire() == null) {
            return false;
        }
        
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

    private void schedulePurge() {
        if (lastPurgeTime + PURGE_JOB_INTERVAL > System.currentTimeMillis()) {
            lastPurgeTime = System.currentTimeMillis();
            executorService.submit(new PurgeTask());
        }
    }

    private void purgeMessage(final DacPlaylistMessageId messageId) {
        logger.debug("Removing message " + messageId + " from cache.");

        cachedMessages.remove(messageId);

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

    private class PurgeTask implements PrioritizableCallable<Object> {

        @Override
        public Object call() {
            Collection<DacPlaylist> playlists = scheduler.getActivePlaylists();
            Set<DacPlaylistMessageId> activeMessageIds = new HashSet<>();
            for (DacPlaylist playlist : playlists) {
                activeMessageIds.addAll(playlist.getMessages());
            }

            for (DacPlaylistMessageId messageId : cachedMessages.keySet()) {
                if (!activeMessageIds.contains(messageId)) {
                    purgeAudio(messageId);
                    purgeMessage(messageId);
                } else if (isExpired(messageId)) {
                    /*
                     * we remove the cached audio buffer separate from the
                     * cached message data because we need the message data to
                     * remain cached so that any playlists that reference the
                     * expired message can know that the message has expired.
                     * But once expiration hits, we no longer need the audio
                     * data.
                     */
                    purgeAudio(messageId);
                }
            }
            return null;
        }

        @Override
        public Integer getPriority() {
            return PriorityBasedExecutorService.PRIORITY_LOW;
        }
    }
    
    private class PurgePlaylistTask implements PrioritizableCallable<Object> {

        private final DacPlaylist playlist;

        public PurgePlaylistTask(DacPlaylist playlist) {
            this.playlist = playlist;
        }

        @Override
        public Object call() {
            try {
                for (DacPlaylistMessageId messageId : playlist
                        .getMessages()) {
                    if (!doesMessageFileExist(messageId)
                            || isExpired(messageId)) {
                        purgeAudio(messageId);
                    }
                }

                Files.delete(playlist.getPath());
                logger.info("Deleted " + playlist.getPath());
            } catch (Throwable e) {
                logger.error(
                        "Error deleting playlist " + playlist.getPath()
                                + " from disk.", e);
            }
            return null;
        }

        @Override
        public Integer getPriority() {
            return PriorityBasedExecutorService.PRIORITY_LOW;
        }
    }

}