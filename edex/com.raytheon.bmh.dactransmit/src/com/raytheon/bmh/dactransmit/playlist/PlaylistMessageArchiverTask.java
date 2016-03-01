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

import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXB;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raytheon.uf.common.bmh.dac.archive.PlaylistMessageArchiver;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessageId;
import com.raytheon.uf.common.time.util.TimeUtil;

/**
 * {@link PrioritizableCallable} task that cleans old message files out of the
 * messages directory for a particular playlist. This task will relocate
 * unnecessary message files to the playlist archive and any remaining position
 * and/or metadata files associated with the message will be purged. Messages
 * files that are moved to the archive will later be purged by the message
 * purger or there is a possibility that they may be temporarily restored under
 * one of the following conditions: 1) a message that was previously made
 * inactive is made active again 2) the expiration time of a message that had
 * previously expired is extended. The objective of this task is to ensure that
 * only the files that are actually needed are present in the playlist messages
 * directory.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 25, 2016 5382       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class PlaylistMessageArchiverTask implements
        PrioritizableCallable<Object> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Path messageDirectory;

    private final PlaylistMessageArchiver playlistMessageArchiver;

    private static final int BROADCAST_ID_GROUP = 1;

    private static final int METADATA_KEEP_MAX = 2;

    /*
     * Only messages that have remained expired for the specified number of
     * milliseconds will be archived. It is inadvisable to set this duration to
     * less than the purge duration in the playlist message cache. If a message
     * is archived before it is purged from cache, the message in memory will be
     * used if a user updates the expiration time of a message that was
     * previously archived. As a result, a copy of the message file will exist
     * in the archive and an active copy of the message that was previously
     * cached will exist in the playlist messages directory.
     */
    private static final long EXPIRE_MS_BUFFER = 15 * TimeUtil.MILLIS_PER_MINUTE;

    private static final String MESSAGE_FILE_REGEX = "^(\\d+)\\.xml$";

    private static final String MESSAGE_METADATA_FILE_REGEX = "^(\\d+)_(\\d+)\\.xml$";

    private static final String POSITION_FILE_REGEX = "^(\\d+)\\.position$";

    private static final Pattern messageFilePattern = Pattern
            .compile(MESSAGE_FILE_REGEX);

    private static final Pattern messageMetadataFilePattern = Pattern
            .compile(MESSAGE_METADATA_FILE_REGEX);

    private static final Pattern positionFilePattern = Pattern
            .compile(POSITION_FILE_REGEX);

    public PlaylistMessageArchiverTask(final Path messageDirectory,
            final PlaylistMessageArchiver playlistMessageArchiver) {
        this.messageDirectory = messageDirectory;
        this.playlistMessageArchiver = playlistMessageArchiver;
    }

    @Override
    public Object call() throws Exception {
        /*
         * runTime will be used as a basis for determining whether or not a
         * message file has expired.
         */
        final Calendar runTime = TimeUtil.newGmtCalendar();
        logger.info("Run {} commencing ...", runTime.getTime().toString());
        try {
            this.executeArchive(runTime);
        } catch (Exception e) {
            logger.error("Run " + runTime.getTime().toString()
                    + " has failed to finish.", e);
            return null;
        }
        logger.info("Run {} has concluded.", runTime.getTime().toString());
        return null;
    }

    private void executeArchive(final Calendar runTime) throws Exception {
        Map<String, MessageFiles> messageFilesMap = new HashMap<>();

        try (DirectoryStream<Path> dirStream = Files
                .newDirectoryStream(this.messageDirectory)) {
            for (Path entry : dirStream) {
                final String fileName = entry.getFileName().toString();

                Matcher matcher = messageFilePattern.matcher(fileName);
                if (matcher.matches()) {
                    final String broadcastId = matcher
                            .group(BROADCAST_ID_GROUP);
                    MessageFiles messageFiles = this
                            .lookupMessageFilesStructure(messageFilesMap,
                                    broadcastId);
                    messageFiles.setMessageFilePath(entry);
                    continue;
                }

                /*
                 * Only the files that match the messageMetadataFilePattern will
                 * be sorted using the {@link MessageMetadataPathComparator}.
                 */
                matcher = messageMetadataFilePattern.matcher(fileName);
                if (matcher.matches()) {
                    final String broadcastId = matcher
                            .group(BROADCAST_ID_GROUP);
                    MessageFiles messageFiles = this
                            .lookupMessageFilesStructure(messageFilesMap,
                                    broadcastId);
                    messageFiles.addMessageMetadataPath(entry);
                    continue;
                }

                matcher = positionFilePattern.matcher(fileName);
                if (matcher.matches()) {
                    final String broadcastId = matcher
                            .group(BROADCAST_ID_GROUP);
                    MessageFiles messageFiles = this
                            .lookupMessageFilesStructure(messageFilesMap,
                                    broadcastId);
                    messageFiles.setPositionFilePath(entry);
                    continue;
                }

                logger.warn(
                        "Unexpected file: {} found in playlist messages directory: {}.",
                        fileName, this.messageDirectory.toString());
            }
        }

        if (messageFilesMap.isEmpty()) {
            return;
        }

        final long expireTimeMillis = runTime.getTimeInMillis();

        /*
         * Possible scenarios that will be examined:
         * 
         * 1) There are no metadata files. RESPONSE: read {@link
         * DacPlaylistMessageId} in message file and archive if expired; purge
         * position file if it exists.
         * 
         * 2) Position file is old and can no longer be used to continue the
         * broadcast of a message. RESPONSE: purge position file.
         * 
         * 3) Three (3) or more metadata files exist for the message. RESPONSE:
         * purge the older files until two remain. Two are allowed to remain to
         * handle the case in which a message was recently updated.
         * 
         * 4) There is no message file. RESPONSE: purge any existing position
         * and metadata files.
         */
        final Set<Path> filesToPurge = new HashSet<>();
        final Set<Path> filesToArchive = new HashSet<>();
        for (MessageFiles messageFiles : messageFilesMap.values()) {
            boolean metadataArchived = false;
            boolean positionArchived = false;
            if (messageFiles.messageFileExists()) {
                final Path messageFilePath = messageFiles.getMessageFilePath();

                DacPlaylistMessageId messageId = null;
                try (InputStream is = Files.newInputStream(messageFilePath)) {
                    messageId = JAXB.unmarshal(is, DacPlaylistMessageId.class);
                } catch (IOException e) {
                    logger.error(
                            "Failed to read message file: "
                                    + messageFilePath
                                    + ". Message file: "
                                    + messageFilePath
                                    + " and all associated information will not be analyzed during this run.",
                            e);
                    continue;
                }

                if (messageId != null
                        && messageId.getExpire() != null
                        && (expireTimeMillis >= messageId.getExpire()
                                .getTimeInMillis() + EXPIRE_MS_BUFFER)) {
                    /*
                     * The message has expired, archive it.
                     */
                    filesToArchive.add(messageFilePath);

                    metadataArchived = messageFiles.messageMetadataFilesExist();
                    if (metadataArchived) {
                        filesToPurge.addAll(messageFiles
                                .getMessageMetadataFilePaths());
                    }
                    positionArchived = messageFiles.positionFileExists();
                    if (positionArchived) {
                        filesToPurge.add(messageFiles.getPositionFilePath());
                    }
                }
            }

            /*
             * If a position file exists, is it still usable?
             */
            if (positionArchived == false && messageFiles.positionFileExists()) {
                final Path filePath = messageFiles.getPositionFilePath();
                if (DacMessagePlaybackData.positionFileExpired(filePath, null)) {
                    filesToPurge.add(filePath);
                }
            }

            if (metadataArchived == false
                    && messageFiles.messageMetadataFilesExist()) {
                /*
                 * Sort the list of metadata files based on timestamp.
                 */
                List<Path> sortedMetadataPathList = new LinkedList<>(
                        messageFiles.getMessageMetadataFilePaths());
                Collections.sort(sortedMetadataPathList,
                        new MessageMetadataPathComparator(
                                messageMetadataFilePattern));
                if (messageFiles.messageFileExists() == false) {
                    /*
                     * Determine if the latest metadata file indicates
                     * expiration in which case all metadata files can be
                     * removed.
                     */
                    DacPlaylistMessageId messageId = null;
                    Path lastMetadataFilePath = sortedMetadataPathList
                            .get(sortedMetadataPathList.size() - 1);
                    try (InputStream is = Files
                            .newInputStream(lastMetadataFilePath)) {
                        messageId = JAXB.unmarshal(is,
                                DacPlaylistMessageId.class);
                    } catch (IOException e) {
                        logger.error(
                                "Failed to read message metadata file: "
                                        + lastMetadataFilePath
                                        + ". Message metadata file: "
                                        + lastMetadataFilePath
                                        + " and all associated information will not be analyzed during this run.",
                                e);
                        continue;
                    }
                    if (messageId != null
                            && messageId.getExpire() != null
                            && (expireTimeMillis >= messageId.getExpire()
                                    .getTimeInMillis() + EXPIRE_MS_BUFFER)) {
                        filesToPurge.addAll(sortedMetadataPathList);
                        sortedMetadataPathList.clear();
                    }
                }

                if (sortedMetadataPathList.size() > METADATA_KEEP_MAX) {
                    final int toDelete = sortedMetadataPathList.size() - 2;
                    filesToPurge.addAll(sortedMetadataPathList.subList(0,
                            toDelete));
                }
            }
        }

        if (filesToPurge.isEmpty() == false) {
            /*
             * Delete all of the files in this {@link Set}.
             */
            for (Path purgePath : filesToPurge) {
                logger.info("Purging file: {} ...", purgePath);
                try {
                    Files.deleteIfExists(purgePath);
                } catch (Exception e) {
                    logger.error(
                            "Failed to purge file: " + purgePath.toString()
                                    + ".", e);
                }
            }
        }
        if (filesToArchive.isEmpty() == false) {
            /*
             * Archive all of the files in this {@link Set}.
             */
            for (Path archivePath : filesToArchive) {
                try {
                    this.playlistMessageArchiver
                            .archivePlaylistMessage(archivePath);
                } catch (Exception e) {
                    logger.error("Failed to archive message file: "
                            + archivePath.toString() + ".", e);
                }
            }
        }
    }

    public MessageFiles lookupMessageFilesStructure(
            final Map<String, MessageFiles> messageFilesMap,
            final String broadcastId) {
        MessageFiles messageFilesStruct = messageFilesMap.get(broadcastId);
        if (messageFilesStruct == null) {
            messageFilesStruct = new MessageFiles();
            messageFilesMap.put(broadcastId, messageFilesStruct);
        }

        return messageFilesStruct;
    }

    @Override
    public Integer getPriority() {
        return PriorityBasedExecutorService.PRIORITY_LOW;
    }

    private class MessageFiles {

        private Path messageFilePath;

        private final List<Path> messageMetadataFilePaths = new ArrayList<>();

        private Path positionFilePath;

        public void addMessageMetadataPath(Path messageMetadataFilePath) {
            this.messageMetadataFilePaths.add(messageMetadataFilePath);
        }

        public boolean messageFileExists() {
            return (this.messageFilePath != null);
        }

        public boolean messageMetadataFilesExist() {
            return (this.messageMetadataFilePaths.isEmpty() == false);
        }

        public boolean positionFileExists() {
            return (this.positionFilePath != null);
        }

        /**
         * @return the messageFileName
         */
        public Path getMessageFilePath() {
            return messageFilePath;
        }

        /**
         * @param messageFileName
         *            the messageFileName to set
         */
        public void setMessageFilePath(Path messageFilePath) {
            this.messageFilePath = messageFilePath;
        }

        /**
         * @return the positionFileName
         */
        public Path getPositionFilePath() {
            return positionFilePath;
        }

        /**
         * @param positionFileName
         *            the positionFileName to set
         */
        public void setPositionFilePath(Path positionFilePath) {
            this.positionFilePath = positionFilePath;
        }

        /**
         * @return the messageMetadataFileNames
         */
        public List<Path> getMessageMetadataFilePaths() {
            return messageMetadataFilePaths;
        }
    }
}