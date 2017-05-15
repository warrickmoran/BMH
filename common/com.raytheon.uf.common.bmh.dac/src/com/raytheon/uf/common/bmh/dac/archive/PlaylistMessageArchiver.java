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
package com.raytheon.uf.common.bmh.dac.archive;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raytheon.uf.common.bmh.FilePermissionUtils;
import com.raytheon.uf.common.bmh.lock.InMemoryFileLockManager;

/**
 * Used to archive playlist message files to a Transmitter Group specific
 * archive. Also provides the means to restore an archive file to handle the
 * cases in which a message is reactivated and/or the expiration date/time is
 * moved out even further.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 26, 2016 5382       bkowal      Initial creation
 * May 08, 2017 6259       bkowal      Updated to use {@link com.raytheon.uf.common.util.file.Files}.
 * 
 * </pre>
 * 
 * @author bkowal
 */

public class PlaylistMessageArchiver {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final int LOCK_TIMEOUT = 1000;

    public static final String ARCHIVE_DIR = "archive";

    private final Path playlistArchivePath;

    public PlaylistMessageArchiver(final Path playlistDirectoryPath)
            throws Exception {
        this.playlistArchivePath = playlistDirectoryPath.resolve(ARCHIVE_DIR);
        if (Files.exists(playlistArchivePath)) {
            return;
        }

        try {
            com.raytheon.uf.common.util.file.Files.createDirectories(
                    playlistArchivePath,
                    FilePermissionUtils.DIRECTORY_PERMISSIONS_ATTR);
        } catch (IOException e) {
            throw new Exception("Failed to create playlist archive: "
                    + playlistArchivePath + ".", e);
        }
    }

    public void archivePlaylistMessage(final Path messageFilePath)
            throws Exception {
        logger.info("Archiving message file: {} ...",
                messageFilePath.toString());
        final Path archiveDestinationPath = this.playlistArchivePath
                .resolve(messageFilePath.getFileName());
        ReentrantLock fileLock = null;
        try {
            fileLock = InMemoryFileLockManager.getInstance()
                    .requestResourceLock(messageFilePath, LOCK_TIMEOUT);
            if (fileLock == null) {
                logger.warn(
                        "Unable to archive file: {}. Failed to lock the file.",
                        messageFilePath.toString());
                return;
            }
            Files.move(messageFilePath, archiveDestinationPath,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } finally {
            if (fileLock != null) {
                fileLock.unlock();
            }
        }
        logger.info("Successfully archived message file: {} => {}.",
                messageFilePath.toString(), archiveDestinationPath.toString());
    }

    public boolean restorePlaylistMessageIfArchived(
            final Path playlistDirectoryPath, final Path messageFile)
            throws Exception {
        final Path expectedMessageFilePath = this.playlistArchivePath
                .resolve(messageFile);
        if (!Files.exists(expectedMessageFilePath)) {
            return false;
        }

        logger.info("Restoring message file: {} ...", messageFile.toString());
        final Path restorationDestinationPath = playlistDirectoryPath
                .resolve(messageFile);
        ReentrantLock fileLock = null;
        try {
            fileLock = InMemoryFileLockManager.getInstance()
                    .requestResourceLock(restorationDestinationPath,
                            LOCK_TIMEOUT);
            if (fileLock == null) {
                logger.warn(
                        "Unable to restore file: {}. Failed to lock the file.",
                        restorationDestinationPath.toString());
                return false;
            }
            Files.move(expectedMessageFilePath, restorationDestinationPath,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } finally {
            if (fileLock != null) {
                fileLock.unlock();
            }
        }
        logger.info("Successfully restored message file: {} => {}.",
                expectedMessageFilePath.toString(),
                restorationDestinationPath.toString());

        return true;
    }
}