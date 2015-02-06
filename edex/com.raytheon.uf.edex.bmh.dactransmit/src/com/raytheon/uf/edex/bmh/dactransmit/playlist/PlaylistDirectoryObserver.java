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
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.JAXB;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylist;
import com.raytheon.uf.common.bmh.datamodel.playlist.PlaylistUpdateNotification;
import com.raytheon.uf.edex.bmh.dactransmit.dacsession.DacSession;
import com.raytheon.uf.edex.bmh.dactransmit.util.NamedThreadFactory;

/**
 * Watches this DacTransmit application's playlist directory for new playlist to
 * be dropped in. If a new playlist is detected, a new
 * PlaylistUpdateNotification is sent over the EventBus for the new playlist.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 17, 2014  #3286     dgilling     Initial creation
 * Jul 29, 2014  #3286     dgilling     Use NamedThreadFactory.
 * Aug 26, 2014  #3286     dgilling     Revert previous change to start().
 * Sep 08, 2014  #3286     dgilling     Make compatible with playlist changes.
 * Feb 06, 2015  #4071     bsteffen     Consolidate threading.
 * 
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

public class PlaylistDirectoryObserver {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ExecutorService threadPool;

    private final WatchService watchService;

    private final EventBus eventBus;

    private volatile boolean keepRunning;

    private final Path playlistDirectory;

    /**
     * Constructor.
     * 
     * @param playlistDirectory
     *            Directory to monitor for new playlist files.
     * @param eventBus
     *            Reference to an event bus instance to send events.
     * @throws IOException
     *             If {@code WatchService} used to monitor directory cannot be
     *             instantiated.
     */
    public PlaylistDirectoryObserver(DacSession dacSession)
            throws IOException {
        this.eventBus = dacSession.getEventBus();
        this.threadPool = Executors
                .newSingleThreadExecutor(new NamedThreadFactory(
                        "PlaylistDirectoryObserver"));
        this.playlistDirectory = dacSession.getConfig().getInputDirectory();
        this.watchService = FileSystems.getDefault().newWatchService();
        this.playlistDirectory.register(this.watchService,
                StandardWatchEventKinds.ENTRY_CREATE);
        this.keepRunning = true;
    }

    /**
     * Start monitoring the playlist directory. Must be called before new
     * playlist events will be generated.
     */
    public void start() {
        Callable<?> eventDispatchJob = createEventDispatcher();
        threadPool.submit(eventDispatchJob);
    }

    /**
     * Shutdown this instance. Takes down the threads responsible for running
     * the {@code WatchService} that're monitoring the playlist directory.
     */
    public void shutdown() {
        keepRunning = false;
        threadPool.shutdown();
    }

    private Callable<Object> createEventDispatcher() {
        Callable<Object> retVal = new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                while (keepRunning) {
                    WatchKey key = watchService.poll(1L, TimeUnit.SECONDS);
                    if (key != null) {
                        for (WatchEvent<?> event : key.pollEvents()) {
                            if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                                @SuppressWarnings("unchecked")
                                WatchEvent<Path> ev = (WatchEvent<Path>) event;
                                Path newPlaylistPath = playlistDirectory
                                        .resolve(ev.context());

                                logger.debug("Got new file notification for "
                                        + newPlaylistPath + "...");

                                try {
                                    DacPlaylist newPlaylist = JAXB.unmarshal(
                                            newPlaylistPath.toFile(),
                                            DacPlaylist.class);
                                    PlaylistUpdateNotification notification = new PlaylistUpdateNotification(
                                            newPlaylist);
                                    eventBus.post(notification);
                                } catch (Exception e) {
                                    logger.error(
                                            "Unable to process new playlist file "
                                                    + newPlaylistPath, e);
                                }
                            }
                        }

                        key.reset();
                    }
                }

                return null;
            }
        };

        return retVal;
    }
}
