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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raytheon.uf.common.bmh.datamodel.playlist.DACPlaylistMessage;

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
 * 
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

public final class PlaylistMessageCache {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final int THREAD_POOL_MAX_SIZE = 3;

    private final ExecutorService cacheThreadPool;

    private final ConcurrentMap<DACPlaylistMessage, AudioFileBuffer> cachedFiles;

    private final ConcurrentMap<DACPlaylistMessage, Future<?>> cacheStatus;

    public PlaylistMessageCache() {
        cacheThreadPool = Executors.newFixedThreadPool(THREAD_POOL_MAX_SIZE);
        cachedFiles = new ConcurrentHashMap<>();
        cacheStatus = new ConcurrentHashMap<>();
    }

    public void addToCache(final List<DACPlaylistMessage> playlist) {
        for (DACPlaylistMessage message : playlist) {
            addToCache(message);
        }
    }

    public void addToCache(final DACPlaylistMessage message) {
        Runnable cacheFileJob = new Runnable() {

            @Override
            public void run() {
                Path filePath = FileSystems.getDefault().getPath(
                        message.getSoundFile());
                try {
                    byte[] rawData = Files.readAllBytes(filePath);
                    AudioFileBuffer buffer = new AudioFileBuffer(rawData);
                    cachedFiles.put(message, buffer);
                } catch (IOException e) {
                    logger.error(
                            "Failed to buffer file: " + filePath.toString(), e);
                }
            }
        };

        Future<?> jobStatus = cacheThreadPool.submit(cacheFileJob);
        cacheStatus.put(message, jobStatus);
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
    public AudioFileBuffer get(final DACPlaylistMessage message) {
        Future<?> fileStatus = cacheStatus.get(message);
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
}
