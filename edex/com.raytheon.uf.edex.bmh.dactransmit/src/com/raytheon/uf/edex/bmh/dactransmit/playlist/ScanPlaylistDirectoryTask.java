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
import java.nio.file.Paths;
import java.nio.file.WatchService;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.raytheon.uf.common.bmh.datamodel.playlist.PlaylistUpdateNotification;

/**
 * A task which scan the playlist directory for changes and sends out
 * notification. A {@link WatchService} cannot be used to watch the directory
 * because it does not consistently report updates over NFS.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Feb 16, 2015  4107     bsteffen    Initial Creation
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class ScanPlaylistDirectoryTask implements
        PrioritizableCallable<Object> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final EventBus eventBus;

    /**
     * root of the playlist directory for this specific dac transmit
     * process(something like ${BMH_DATA}/palylists/${trnamitter}/
     */
    private final Path root;

    /**
     * This should only be null until after the first call(), and during that
     * invocation it will not send out notifications.
     */
    private Set<Path> playlistFiles = null;

    public ScanPlaylistDirectoryTask(EventBus eventBus, Path root) {
        this.eventBus = eventBus;
        this.root = root;
        eventBus.register(this);
    }

    @Override
    public Object call() {
        Set<Path> newPlaylistFiles = new HashSet<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(root,
                "*.xml")) {
            for (Path file : stream) {
                file = file.getFileName();
                if (playlistFiles != null && !playlistFiles.contains(file)) {
                    PlaylistUpdateNotification notify = new PlaylistUpdateNotification();
                    notify.setPlaylistPath(root.getFileName().resolve(file)
                            .toString());
                    eventBus.post(notify);
                }
                newPlaylistFiles.add(file);
            }
        } catch (IOException e) {
            logger.error("Error checking for new playlists", e);
        }
        return playlistFiles = Collections.synchronizedSet(newPlaylistFiles);
    }

    @Override
    public Integer getPriority() {
        return PriorityBasedExecutorService.PRIORITY_LOW;
    }

    @Subscribe
    public void newPlaylistReceived(PlaylistUpdateNotification notification) {
        /* Its only null during the initial population. */
        if (playlistFiles != null) {
            playlistFiles.add(Paths.get(notification.getPlaylistPath())
                    .getFileName());
        }
    }

}
