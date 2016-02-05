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

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
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
import com.raytheon.bmh.dactransmit.dacsession.DacSession;
import com.raytheon.bmh.dactransmit.dacsession.DacSessionConfig;
import com.raytheon.bmh.dactransmit.events.CriticalErrorEvent;
import com.raytheon.bmh.dactransmit.exceptions.NoSoundFileException;
import com.raytheon.bmh.dactransmit.ipc.ChangeAmplitudeTarget;
import com.raytheon.bmh.dactransmit.ipc.ChangeTimeZone;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylist;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessage;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessageId;
import com.raytheon.uf.common.bmh.stats.DeliveryTimeEvent;
import com.raytheon.uf.common.bmh.trace.TraceableUtil;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.common.util.CollectionUtil;
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
 * Mar 09, 2015  #4170     bsteffen     Throw exceptions from getAudio.
 * Mar 13, 2015  4251      bkowal       Limit messages accompanied by tones to 2 minutes.
 * Mar 25, 2015  4290      bsteffen     Switch to global replacement.
 * Apr 07, 2015  4293      bkowal       Updated to allow reading in new message files based on
 *                                      timestamp. Will also check for new audio based on file name.
 * Apr 24, 2015  4423      rferrel      Added {@link #changeTimezone(ChangeTimeZone)}.
 * Apr 27, 2015  4397      bkowal       Create a {@link DeliveryTimeEvent} for newly processed
 *                                      messages.
 * May 14, 2015  4460      bkowal       Only update {@link DacPlaylistMessage} metadata when metadata
 *                                      is actually read.
 * May 13, 2015 4429       rferrel      Changes to {@link DefaultMessageLogger} for traceId.
 * May 26, 2015 4481       bkowal       Set dynamic on the {@link DacPlaylistMessage}.
 * Jun 25, 2015 4508       bkowal       Validate message metadata in {@link #retrieveAudio(DacPlaylist)}
 *                                      in every case.
 * Jun 29, 2015 4602       bkowal       Fix updates in response to decibel changes.
 * Jul 08, 2015 4636       bkowal       Support same and alert decibel levels.
 * Jul 28, 2015 4686       bkowal       Moved statistics to common.
 * Jul 29, 2015 4686       bkowal       Set the broadcast id on the {@link DeliveryTimeEvent}.
 * Sep 22, 2015 4904       bkowal       Ensure that message metadata for replaced messages
 *                                      matches the db. Add additional logging for purging.
 * Oct 06, 2015 4904       bkowal       Handle the case when the last purge time is not set.
 * Nov 04, 2015 5068       rjpeter      Switch audio units from dB to amplitude.
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

    private short audioAmplitude;

    private short sameAmplitude;

    private short alertAmplitude;

    private volatile TimeZone timezone;

    private final PlaylistScheduler scheduler;

    private final DacSession dacSession;

    private long lastPurgeTime;

    private final Object purgeLock = new Object();

    public PlaylistMessageCache(DacSession dacSession,
            PlaylistScheduler playlistScheduler) {
        this.dacSession = dacSession;
        DacSessionConfig config = dacSession.getConfig();
        this.messageDirectory = config.getInputDirectory().resolve("messages");
        this.cachedMessages = new ConcurrentHashMap<>();
        this.cachedFiles = new ConcurrentHashMap<>();
        this.cacheStatus = new ConcurrentHashMap<>();
        this.cachedRetrievalTasks = new ConcurrentHashMap<>();
        this.eventBus = dacSession.getEventBus();
        this.audioAmplitude = config.getAudioAmplitude();
        this.sameAmplitude = config.getSameAmplitude();
        this.alertAmplitude = config.getAlertAmplitude();
        this.timezone = config.getTimezone();
        this.scheduler = playlistScheduler;
        this.executorService = dacSession.getAsyncExecutor();
        /*
         * Assume this Last for duration of the spring container. No need to
         * unregister.
         */
        this.eventBus.register(this);
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
            if (!cacheStatus.containsKey(message)) {
                /*
                 * Always retrieve the audio when it has not been previously
                 * retrieved.
                 */
                logger.info("Completing initial load of {}.",
                        message.toString());
                this.checkMessageMetadata(playlist, message, index, false);

                /*
                 * This is intended to cause higher priority lists to get
                 * scheduled before lower priority lists and also to get
                 * messages at the beginning of the list scheduled before
                 * messages at the end of the list.
                 */
                int priority = PriorityBasedExecutorService.PRIORITY_NORMAL
                        + playlist.getPriority() * 100 + index;
                retrieveAudio(message, priority);
                index += 1;
            } else {
                /*
                 * If the audio has been previously retrieved. Determine if any
                 * metadata updates are required.
                 */
                if (this.checkMessageMetadata(playlist, message, index, true)) {
                    ++index;
                }
            }
        }
        schedulePurge();
    }

    /**
     * Updates the expiration time of a message that is no longer in the
     * playlist thus it is not eligible for a full metadata update. This will
     * ensure that a message that will no longer be played will not remain in
     * the cache (specifically the associated audio) until its natural
     * expiration time (which could easily be a day or more away).
     * 
     * @param message
     *            the id of the message to update the expiration time for.
     */
    public void expirePlaylistMessage(DacPlaylistMessageId message) {
        /*
         * This method does not explicitly mention expired messages because the
         * same action may be extended to messages that are deactivated as well.
         */
        Future<IAudioFileBuffer> jobStatus = cacheStatus.remove(message);
        if (jobStatus != null) {
            jobStatus.cancel(true);
        }

        if (this.cachedMessages.containsKey(message) == false) {
            return;
        }

        logger.info("Expiring message: {}.", message.toString());

        /*
         * We only update the expiration time instead of actually removing the
         * message because we want to allow the purge mechanism to perform all
         * necessary cleanup for a message removal.
         */
        this.cachedMessages.get(message).setExpire(message.getExpire());
    }

    private boolean checkMessageMetadata(DacPlaylist playlist,
            DacPlaylistMessageId message, int index, final boolean checkAudio) {
        DacPlaylistMessage dacMessage = this.cachedMessages.get(message);
        if (dacMessage == null) {
            return false;
        }
        if (message.getTimestamp() != dacMessage.getTimestamp()) {
            logger.info("Updating metadata for {}.", message.toString());

            /*
             * a metadata update is required.
             */
            DacPlaylistMessage updatedDacMessage = this.readMessageMetadata(
                    message, false);
            /*
             * Override all information that is not persisted across metadata
             * writes.
             */
            updatedDacMessage.setPlayCount(dacMessage.getPlayCount());
            updatedDacMessage.setLastTransmitTime(dacMessage
                    .getLastTransmitTime());
            updatedDacMessage.setMessageBroadcastNotificationSent(dacMessage
                    .isMessageBroadcastNotificationSent());
            updatedDacMessage
                    .setPlayedAlertTone(dacMessage.isPlayedAlertTone());
            updatedDacMessage.setPlayedSameTone(dacMessage.isPlayedSameTone());

            try {
                this.persistMergedMessageMetadata(message, updatedDacMessage);
            } catch (IOException e) {
                logger.error("Failed to write updated metadata for {}.",
                        message.toString(), e);
            }

            this.cachedMessages.replace(message, updatedDacMessage);

            /*
             * Determine if an audio re-retrieval is necessary.
             */
            if (checkAudio
                    && dacMessage.getSoundFiles().equals(
                            updatedDacMessage.getSoundFiles()) == false) {
                logger.info("Updating audio for {}.", message.toString());

                Future<IAudioFileBuffer> jobStatus = scheduleFileRetrieval(
                        PriorityBasedExecutorService.PRIORITY_NORMAL
                                + playlist.getPriority() * 100 + index, message);
                this.cacheStatus.replace(message, jobStatus);
                IAudioFileBuffer removedBuffer = this.cachedFiles
                        .remove(dacMessage);
                if (removedBuffer == null) {
                    logger.warn(
                            "No cached files have been removed for broadcast: {}.",
                            message.toString());
                } else {
                    logger.warn(
                            "Successfully removed cache file for broadcast: {}.",
                            message.toString());
                }

                return true;
            }
        }

        return false;
    }

    /**
     * Cache a single message.
     * 
     * @param message
     *            Message to cache.
     */
    private void retrieveAudio(final DacPlaylistMessageId id, int priority) {
        Future<IAudioFileBuffer> jobStatus = scheduleFileRetrieval(priority, id);
        cacheStatus.put(id, jobStatus);
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
                priority, this.audioAmplitude, this.sameAmplitude,
                this.alertAmplitude, this.getMessage(id), this, taskId);
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
    public IAudioFileBuffer getAudio(final DacPlaylistMessage message)
            throws Throwable {
        schedulePurge();
        Future<IAudioFileBuffer> fileStatus = cacheStatus
                .get(new DacPlaylistMessageId(message.getBroadcastId()));
        if (fileStatus != null) {
            IAudioFileBuffer buffer = cachedFiles.get(message);
            if (buffer == null) {
                long startTime = System.currentTimeMillis();
                try {
                    buffer = fileStatus.get();
                } catch (ExecutionException e) {
                    throw e.getCause();
                }
                long time = System.currentTimeMillis() - startTime;
                if (time > 1) {
                    logger.info(
                            TraceableUtil.createTraceMsgHeader(message)
                                    + "Spent {}ms waiting for audio for message with id={}.",
                            time, message.getBroadcastId());
                }
                cachedFiles.put(message, buffer);
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
            DefaultMessageLogger.getInstance().logError(message,
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
        Path messagePath = messageDirectory.resolve(id.getBroadcastId() + "_"
                + id.getTimestamp() + ".xml");
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
            message = this.readMessageMetadata(id, true);

            cachedMessages.put(id, message);
            message.setExpire(id.getExpire());
            message.setTimestamp(id.getTimestamp());
        }
        return message;
    }

    private DacPlaylistMessage readMessageMetadata(DacPlaylistMessageId id,
            final boolean persistUpdates) {
        Path messagePath = messageDirectory.resolve(id.getBroadcastId() + "_"
                + id.getTimestamp() + ".xml");
        DacPlaylistMessage message = JAXB.unmarshal(messagePath.toFile(),
                DacPlaylistMessage.class);
        message.setPath(messagePath);
        if (CollectionUtil.isNullOrEmpty(message.getSoundFiles()) == false) {
            for (String soundFile : message.getSoundFiles()) {
                if (Files.isDirectory(Paths.get(soundFile))) {
                    message.setDynamic(true);
                    break;
                }
            }
        }

        /*
         * Fulfill the statistics requirement.
         */
        if (message.isRecognized()) {
            /*
             * Statistics have already been generated for this version of the
             * message.
             */
            return message;
        }

        /*
         * Generate a Delivery Time statistic.
         */
        DeliveryTimeEvent event = new DeliveryTimeEvent();
        event.setBroadcastId(message.getBroadcastId());
        event.setDeliveryTime(TimeUtil.newGmtCalendar().getTimeInMillis()
                - message.getInitialRecognitionTime());

        this.dacSession.handleDeliveryTimeStat(event);

        message.setRecognized(true);
        if (persistUpdates) {
            try {
                this.persistMergedMessageMetadata(id, message);
            } catch (IOException e) {
                logger.error("Failed to write updated metadata for {}.",
                        id.toString(), e);
            }
        }

        return message;
    }

    private void persistMergedMessageMetadata(DacPlaylistMessageId id,
            DacPlaylistMessage message) throws IOException {
        Path messagePath = messageDirectory.resolve(id.getBroadcastId() + "_"
                + id.getTimestamp() + ".xml");
        try (Writer writer = Files.newBufferedWriter(messagePath,
                Charset.defaultCharset())) {
            JAXB.marshal(message, writer);
        }
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
                } catch (InterruptedException e) {
                    /* Should never happen, just check file size. */
                } catch (ExecutionException e) {
                    /*
                     * Ignore for now, when the message is played the same
                     * exception will be thrown and logged in getAudio
                     */
                    return 0;
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
                    } catch (NoSuchFileException e) {
                        logger.error(
                                "Unable to retrieve file size for file: {} because it does not exist.",
                                audioFile);
                    } catch (Exception e) {
                        logger.error(
                                "Unable to retrieve file size for file: {}",
                                audioFile, e);
                    }

                } else {
                    throw new NoSoundFileException("Message " + messageId
                            + " contains an empty soundFile attribute.");
                }
            }
            if (includeTones) {
                /*
                 * audio playback is truncated to two (2) minutes when tones are
                 * also played.
                 */
                fileSize = Math.min(fileSize,
                        AudioFileBuffer.MAX_TONES_MSG_AUDIO_BYTE_COUNT);
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
     * Listens for changes to the amplitude target
     * 
     * @param changeEvent
     *            a notification including the updated amplitude target
     */
    @Subscribe
    public void changeAmplitude(ChangeAmplitudeTarget changeEvent) {
        this.audioAmplitude = changeEvent.getAudioAmplitude();
        this.sameAmplitude = changeEvent.getSameAmplitude();
        this.alertAmplitude = changeEvent.getAlertAmplitude();

        CachedAudioRetrievalTask task = new CachedAudioRetrievalTask(
                new HashSet<DacPlaylistMessage>(cachedFiles.keySet()));
        this.cachedRetrievalTasks.put(task.getIdentifier(), task);
        logger.info("Initiating cache update " + task.getIdentifier());
        for (DacPlaylistMessage message : cachedFiles.keySet()) {
            DacPlaylistMessageId messageId = new DacPlaylistMessageId(
                    message.getBroadcastId());
            messageId.setTraceId(message.getTraceId());
            Future<IAudioFileBuffer> jobStatus = scheduleFileRetrieval(
                    PriorityBasedExecutorService.PRIORITY_LOW, messageId,
                    task.getIdentifier());
            cacheStatus.replace(messageId, jobStatus);
        }

        logger.info(
                "Updated transmitter amplitude targets to: audio={}, same={}, alert={}.",
                this.audioAmplitude, this.sameAmplitude, this.alertAmplitude);
    }

    /**
     * Set the new time zone value and log the event.
     * 
     * @param changeEvent
     */
    @Subscribe
    public void changeTimezone(ChangeTimeZone changeEvent) {
        this.timezone = TimeZone.getTimeZone(changeEvent.getTimeZone());
        logger.info("Updated transmitter time zone to: "
                + this.timezone.getID());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.bmh.dactransmit.playlist.IAudioJobListener#
     * audioRetrievalFinished(java.lang.String,
     * com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessage)
     */
    @Override
    public void audioRetrievalFinished(String taskId,
            DacPlaylistMessage message, IAudioFileBuffer buffer) {
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
        if (buffer != null) {
            this.cachedFiles.replace(message, buffer);
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
        synchronized (purgeLock) {
            if (lastPurgeTime + PURGE_JOB_INTERVAL < System.currentTimeMillis()) {
                lastPurgeTime = System.currentTimeMillis();
                logger.info("Scheduling a cache purge ... {}",
                        this.lastPurgeTime);
                executorService.submit(new PurgeTask());
            }
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

            int messagesPurged = 0;
            int audioFilesPurged = 0;
            for (DacPlaylistMessageId messageId : cachedMessages.keySet()) {
                if (!activeMessageIds.contains(messageId)) {
                    purgeAudio(messageId);
                    ++audioFilesPurged;
                    purgeMessage(messageId);
                    ++messagesPurged;
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
                    ++audioFilesPurged;
                }
            }

            logger.info(
                    "Purge Summary: messages purged = {}; cached audio purged = {}.",
                    messagesPurged, audioFilesPurged);

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
                for (DacPlaylistMessageId messageId : playlist.getMessages()) {
                    if (!doesMessageFileExist(messageId)
                            || isExpired(messageId)) {
                        purgeAudio(messageId);
                    }
                }

                /*
                 * Playlist may be deleted before the purge runs if a newer
                 * version of the playlist is received.
                 */
                if (Files.exists(playlist.getPath())) {
                    Files.delete(playlist.getPath());
                    logger.info("Deleted " + playlist.getPath());
                }
            } catch (Throwable e) {
                logger.error("Error deleting playlist " + playlist.getPath()
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