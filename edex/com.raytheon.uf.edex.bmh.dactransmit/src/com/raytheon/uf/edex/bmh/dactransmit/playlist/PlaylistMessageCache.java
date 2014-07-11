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
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


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
 * 
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

public final class PlaylistMessageCache {

    private static final int THREAD_POOL_MAX_SIZE = 3;

    private final ExecutorService cacheThreadPool;

    private final ConcurrentMap<PlaylistMessage, AudioFileBuffer> cachedFiles;

    private final ConcurrentMap<PlaylistMessage, Future<?>> cacheStatus;

    public PlaylistMessageCache() {
        cacheThreadPool = Executors.newFixedThreadPool(THREAD_POOL_MAX_SIZE);
        cachedFiles = new ConcurrentHashMap<>();
        cacheStatus = new ConcurrentHashMap<>();
    }

    public void addToCache(final AudioFileDirectoryPlaylist playlist) {
        Collection<PlaylistMessage> playlistFiles = playlist.getUniqueFiles();
        for (PlaylistMessage file : playlistFiles) {
            addToCache(file);
        }
    }

    public void addToCache(final PlaylistMessage message) {
        Runnable cacheFileJob = new Runnable() {

            @Override
            public void run() {
                Path filePath = message.getFilename();
                try {
                    byte[] rawData = Files.readAllBytes(filePath);
                    AudioFileBuffer buffer = new AudioFileBuffer(rawData);
                    cachedFiles.put(message, buffer);
                } catch (IOException e) {
                    e.printStackTrace();
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
    public AudioFileBuffer get(final PlaylistMessage message) {
        Future<?> fileStatus = cacheStatus.get(message);
        if (fileStatus != null) {
            try {
                fileStatus.get();
                AudioFileBuffer buffer = cachedFiles.get(message);
                buffer.rewind();
                return buffer;
            } catch (InterruptedException | ExecutionException e) {
                System.out
                        .println("ERROR [PlaylistMessageCache] : Exception thrown waiting on cache status for "
                                + message);
                e.printStackTrace();
            }
        }

        return null;
    }
}
