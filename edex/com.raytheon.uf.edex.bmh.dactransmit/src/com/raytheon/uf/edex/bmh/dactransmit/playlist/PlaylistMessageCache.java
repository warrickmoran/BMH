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
import java.util.HashSet;
import java.util.List;
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
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessage;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessageId;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.edex.bmh.dactransmit.events.CriticalErrorEvent;
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
 * 
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

public final class PlaylistMessageCache implements IAudioJobListener {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final int THREAD_POOL_MAX_SIZE = 3;

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

    private final ConcurrentMap<DacPlaylistMessageId, DacPlaylistMessage> cachedMessages;

    private final ConcurrentMap<DacPlaylistMessage, AudioFileBuffer> cachedFiles;

    private final ConcurrentMap<DacPlaylistMessageId, Future<AudioFileBuffer>> cacheStatus;

    private final ConcurrentMap<String, CachedAudioRetrievalTask> cachedRetrievalTasks;

    private final EventBus eventBus;

    private double dbTarget;

    public PlaylistMessageCache(Path messageDirectory, EventBus eventBus,
            double dbTarget) {
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
    }

    /**
     * Stops all background threads this class was using. After this method is
     * called, this instance cannot be restarted.
     */
    public void shutdown() {
        cacheThreadPool.shutdown();
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
            Future<AudioFileBuffer> jobStatus = scheduleFileRetrieval(
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
    private Future<AudioFileBuffer> scheduleFileRetrieval(final int priority,
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
    private Future<AudioFileBuffer> scheduleFileRetrieval(final int priority,
            final DacPlaylistMessageId id, final String taskId) {
        Callable<AudioFileBuffer> retrieveAudioJob = new RetrieveAudioJob(
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
    public AudioFileBuffer getAudio(final DacPlaylistMessage message) {
        Future<AudioFileBuffer> fileStatus = cacheStatus
                .get(new DacPlaylistMessageId(message.getBroadcastId()));
        if (fileStatus != null) {
            AudioFileBuffer buffer = cachedFiles.get(message);
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
            cachedMessages.put(id, message);
        }
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
     */
    public long getPlaybackTime(DacPlaylistMessageId messageId) {
        DacPlaylistMessage message = cachedMessages.get(messageId);
        boolean includeTones = ((message.getSAMEtone() != null) && (message
                .getPlayCount() == 0));

        long fileSize = 0;
        if (cachedFiles.containsKey(message)) {
            fileSize = cachedFiles.get(message).capcity(includeTones);
        } else {
            Path audioFile = Paths.get(cachedMessages.get(messageId)
                    .getSoundFile());
            try {
                fileSize = Files.size(audioFile);
            } catch (Exception e) {
                logger.error("Unable to retrieve file size for file: "
                        + audioFile.toString(), e);
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
            Future<AudioFileBuffer> jobStatus = scheduleFileRetrieval(
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
                        + " in " + TimeUtil.prettyDuration(task.getDuration()) + ".");
                this.cachedRetrievalTasks.remove(message);
            }
        }
    }
}