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
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.xml.bind.JAXB;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessage;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessageId;

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
 * 
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

public final class PlaylistMessageCache {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final int THREAD_POOL_MAX_SIZE = 3;

    private final Path messageDirectory;

    private final ExecutorService cacheThreadPool;

    private final ConcurrentMap<DacPlaylistMessageId, DacPlaylistMessage> cachedMessages;

    private final ConcurrentMap<DacPlaylistMessage, AudioFileBuffer> cachedFiles;

    private final ConcurrentMap<DacPlaylistMessageId, Future<?>> cacheStatus;

    public PlaylistMessageCache(Path messageDirectory) {
        this.messageDirectory = messageDirectory;
        cacheThreadPool = Executors.newFixedThreadPool(THREAD_POOL_MAX_SIZE);
        cachedMessages = new ConcurrentHashMap<>();
        cachedFiles = new ConcurrentHashMap<>();
        cacheStatus = new ConcurrentHashMap<>();
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
    public void addToCache(final List<DacPlaylistMessageId> playlist) {
        for (DacPlaylistMessageId message : playlist) {
            addToCache(message);
        }
    }

    /**
     * Cache a single message.
     * 
     * @param message
     *            Message to cache.
     */
    public void addToCache(final DacPlaylistMessageId id) {
        if (!cacheStatus.containsKey(id)) {
            Runnable cacheFileJob = new Runnable() {

                @Override
                public void run() {
                    DacPlaylistMessage message = getMessage(id);
                    Path filePath = FileSystems.getDefault().getPath(
                            message.getSoundFile());
                    try {
                        byte[] rawData = Files.readAllBytes(filePath);
                        AudioFileBuffer buffer = new AudioFileBuffer(rawData);
                        cachedFiles.put(message, buffer);
                    } catch (IOException e) {
                        logger.error(
                                "Failed to buffer file: " + filePath.toString(),
                                e);
                    }
                }
            };

            Future<?> jobStatus = cacheThreadPool.submit(cacheFileJob);
            cacheStatus.put(id, jobStatus);
        }
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
        Future<?> fileStatus = cacheStatus.get(new DacPlaylistMessageId(message
                .getBroadcastId()));
        if (fileStatus != null) {
            try {
                fileStatus.get();
                AudioFileBuffer buffer = cachedFiles.get(message);
                buffer.rewind();
                return buffer;
            } catch (InterruptedException | ExecutionException e) {
                logger.error("Exception thrown waiting on cache status for "
                        + message, e);
            }
        }

        return null;
    }

    /**
     * Return the full message fior the given id. If the message has not been
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
}
