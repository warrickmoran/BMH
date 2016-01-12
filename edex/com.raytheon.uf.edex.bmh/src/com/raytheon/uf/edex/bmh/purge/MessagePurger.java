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
package com.raytheon.uf.edex.bmh.purge;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastContents;
import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastFragment;
import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg;
import com.raytheon.uf.common.bmh.datamodel.msg.InputMessage;
import com.raytheon.uf.edex.bmh.BMHConstants;
import com.raytheon.uf.edex.bmh.FileManager;
import com.raytheon.uf.edex.bmh.dao.BroadcastMsgDao;
import com.raytheon.uf.edex.bmh.dao.InputMessageDao;

/**
 * 
 * Purges old messages from BMH.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Nov 26, 2014  3613     bsteffen    Initial creation
 * Jan 06, 2015  3651     bkowal      Support AbstractBMHPersistenceLoggingDao.
 * Jan 26, 2015  3928     bsteffen    Allow creation of a practice purger.
 * Feb 24, 2015  4160     bsteffen    Purge message files.
 * Apr 07, 2015  4293     bkowal      Purge all audio generated for a single
 *                                    Broadcast Message.
 * Jul 29, 2015  4690     rjpeter     Added purging of reject files.
 * Nov 16, 2015  5127     rjpeter     Added purging of archive files.
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class MessagePurger {

    private static final String DATE_GLOB = "[0-9][0-9][0-1][0-9][0-3][0-9]";

    private final Logger logger = LoggerFactory.getLogger(MessagePurger.class);

    private final int purgeDays;

    private final InputMessageDao inputMessageDao;

    private final BroadcastMsgDao broadcastMessageDao;

    private final Path audioPath;

    private final Path playlistPath;

    private final List<FileManager> fileManagers;

    public MessagePurger(int purgeDays, final InputMessageDao inputMessageDao,
            final BroadcastMsgDao broadcastMessageDao, boolean operational) {
        this(purgeDays, inputMessageDao, broadcastMessageDao, operational, null);
    }

    public MessagePurger(int purgeDays, final InputMessageDao inputMessageDao,
            final BroadcastMsgDao broadcastMessageDao, boolean operational,
            List<FileManager> fileManagers) {
        this.purgeDays = purgeDays;
        this.inputMessageDao = inputMessageDao;
        this.broadcastMessageDao = broadcastMessageDao;
        audioPath = BMHConstants.getBmhDataDirectory(operational).resolve(
                BMHConstants.AUDIO_DATA_DIRECTORY);
        playlistPath = BMHConstants.getBmhDataDirectory(operational).resolve(
                "playlist");
        if (fileManagers != null) {
            this.fileManagers = fileManagers;
        } else {
            this.fileManagers = Collections.emptyList();
        }
    }

    public void purge() {
        if (purgeDays < 0) {
            return;
        }
        Calendar purgeTime = Calendar.getInstance();
        purgeTime.add(Calendar.DAY_OF_YEAR, -purgeDays);
        purgeDatabase(purgeTime);
        purgeAudioFiles(purgeTime);
        purgeMessageFiles(purgeTime);
        purgeOldAudioContents(purgeTime);
        for (FileManager fileManager : fileManagers) {
            fileManager.purge(2, purgeDays);
        }
    }

    protected void purgeDatabase(Calendar purgeTime) {
        List<InputMessage> inputMessages = inputMessageDao
                .getPurgableMessages(purgeTime);
        logger.info("Purging {} messages older than {}", inputMessages.size(),
                purgeTime.getTime());
        List<Path> audioFilesToDelete = new ArrayList<>(inputMessages.size());
        List<Path> messageFilesToDelete = new ArrayList<>(inputMessages.size());
        for (InputMessage inputMessage : inputMessages) {
            for (BroadcastMsg broadcastMessage : broadcastMessageDao
                    .getMessagesByInputMsgId(inputMessage.getId())) {
                if (broadcastMessage.getContents() == null
                        || broadcastMessage.getContents().isEmpty()) {
                    continue;
                }
                for (BroadcastContents contents : broadcastMessage
                        .getContents()) {
                    if (contents.getFragments() == null
                            || contents.getFragments().isEmpty()) {
                        continue;
                    }
                    for (BroadcastFragment fragment : contents.getFragments()) {
                        audioFilesToDelete.add(Paths.get(fragment
                                .getOutputName()));
                    }
                }
                Path messagePath = playlistPath
                        .resolve(
                                broadcastMessage.getTransmitterGroup()
                                        .getName()).resolve("messages")
                        .resolve(broadcastMessage.getId() + ".xml");
                messageFilesToDelete.add(messagePath);
            }
        }
        inputMessageDao.deleteAll(inputMessages);
        for (Path path : audioFilesToDelete) {
            try {
                Files.delete(path);
            } catch (IOException e) {
                logger.error("Failed to delete {}", path, e);
            }
        }
        for (Path path : messageFilesToDelete) {
            try {
                Files.deleteIfExists(path);
            } catch (IOException e) {
                logger.error("Failed to delete {}", path, e);
            }
        }
    }

    protected void purgeAudioFiles(Calendar purgeTime) {
        if (Files.isDirectory(audioPath)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(
                    audioPath, DATE_GLOB)) {
                for (Path datedDir : stream) {
                    purgeAudioFilesInDatedDirectory(datedDir, purgeTime);
                }
            } catch (IOException e) {
                logger.error("Cannot clear old orphaned audio files.", e);
            }
        }
        Path wxDir = audioPath.resolve("WXMessages");
        if (Files.isDirectory(wxDir)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(wxDir,
                    DATE_GLOB)) {
                for (Path datedDir : stream) {
                    purgeAudioFilesInDatedDirectory(datedDir, purgeTime);
                }
            } catch (IOException e) {
                logger.error("Cannot clear old orphaned audio files.", e);
            }
        }
    }

    protected void purgeAudioFilesInDatedDirectory(Path datedDir,
            Calendar purgeTime) {
        boolean empty = true;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(datedDir)) {
            for (Path audioFile : stream) {
                if (Files.getLastModifiedTime(audioFile).toMillis() < purgeTime
                        .getTimeInMillis()) {
                    BroadcastMsg parent = broadcastMessageDao
                            .getMessageByFragmentPath(audioFile);
                    if (parent == null) {
                        logger.info("Deleting orphaned audio file: {}",
                                audioFile);
                        Files.delete(audioFile);
                    } else {
                        empty = false;
                    }
                } else {
                    empty = false;
                }
            }
        } catch (IOException e) {
            logger.error("Cannot clear old orphaned audio files.", e);
            empty = false;
        }
        if (empty) {
            try {
                Files.delete(datedDir);
            } catch (IOException e) {
                logger.error("Failed to delete empty directroy: ", datedDir, e);
            }
        }
    }

    protected void purgeMessageFiles(Calendar purgeTime) {
        if (Files.isDirectory(playlistPath)) {
            try (DirectoryStream<Path> stream = Files
                    .newDirectoryStream(playlistPath)) {
                for (Path playlistDir : stream) {
                    Path messageDir = playlistDir.resolve("messages");
                    if (Files.exists(messageDir)) {
                        purgeMessageFilesForTransmitter(messageDir, purgeTime);
                    }
                }
            } catch (IOException e) {
                logger.error("Cannot clear old orphaned audio files.", e);
            }
        }
    }

    protected void purgeMessageFilesForTransmitter(Path messageDir,
            Calendar purgeTime) {
        boolean empty = true;
        try (DirectoryStream<Path> stream = Files
                .newDirectoryStream(messageDir)) {
            for (Path messageFile : stream) {
                if (Files.getLastModifiedTime(messageFile).toMillis() < purgeTime
                        .getTimeInMillis()) {
                    String fileName = messageFile.getFileName().toString();
                    long id = Long.parseLong(fileName.substring(0,
                            fileName.indexOf('_')));
                    BroadcastMsg parent = broadcastMessageDao.getByID(id);
                    if (parent == null) {
                        logger.info("Deleting orphaned message file: {}",
                                messageFile);
                        Files.delete(messageFile);
                    } else {
                        empty = false;
                    }
                } else {
                    empty = false;
                }
            }
        } catch (IOException e) {
            logger.error("Cannot clear old orphaned message files.", e);
            empty = false;
        }
        if (empty) {
            try {
                Files.delete(messageDir);
            } catch (IOException e) {
                logger.error("Failed to delete empty directroy: ", messageDir,
                        e);
            }
        }
    }

    protected void purgeOldAudioContents(Calendar purgeTime) {
        long purgeMillis = purgeTime.getTimeInMillis();
        List<BroadcastMsg> broadcastMessages = this.broadcastMessageDao
                .getMessagesWithOldContents(purgeMillis);
        if (broadcastMessages.isEmpty()) {
            return;
        }

        for (BroadcastMsg msg : broadcastMessages) {
            List<Path> filesToDelete = new ArrayList<>();
            BroadcastContents contents = msg.getAndRemovePreviousContents();
            while (contents != null) {
                for (BroadcastFragment fragment : contents
                        .getOrderedFragments()) {
                    final Path outputPath = Paths.get(fragment.getOutputName());
                    if (Files.isDirectory(outputPath)) {
                        /*
                         * indicates a time fragment directory.
                         */
                        continue;
                    }

                    if (Files.exists(outputPath)) {
                        filesToDelete.add(outputPath);
                    }
                }

                contents = msg.getAndRemovePreviousContents();
            }
            this.broadcastMessageDao.saveOrUpdate(msg);

            for (Path fileToDelete : filesToDelete) {
                try {
                    Files.delete(fileToDelete);
                } catch (IOException e) {
                    logger.error("Failed to delete audio file: {}.",
                            fileToDelete, e);
                }
            }
        }
    }

    protected int purgeRejectFilesRecursive(Path dir, Calendar purgeTime,
            boolean deleteDir) {
        int filesDeleted = 0;

        if (Files.isDirectory(dir)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
                boolean empty = true;

                for (Path entry : stream) {
                    if (Files.isDirectory(entry)) {
                        filesDeleted += purgeRejectFilesRecursive(entry,
                                purgeTime, true);

                        if (Files.exists(entry)) {
                            empty = false;
                        }
                    } else {
                        if (Files.getLastModifiedTime(entry).toMillis() < purgeTime
                                .getTimeInMillis()) {
                            try {
                                Files.delete(entry);
                                filesDeleted++;
                            } catch (IOException e) {
                                logger.error("Failed to purge reject file: "
                                        + entry.toString(), e);
                                empty = false;
                            }
                        } else {
                            empty = false;
                        }
                    }
                }

                if (empty && deleteDir) {
                    try {
                        Files.delete(dir);
                    } catch (IOException e) {
                        logger.error(
                                "Failed to delete empty reject directory: "
                                        + dir.toString(), e);
                    }
                }
            } catch (IOException e) {
                logger.error("Failed to purge reject files.", e);
            }
        }

        return filesDeleted;
    }
}