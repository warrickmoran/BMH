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
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastFragment;
import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg;
import com.raytheon.uf.common.bmh.datamodel.msg.InputMessage;
import com.raytheon.uf.edex.bmh.BMHConstants;
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
 * 
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

    public MessagePurger(int purgeDays, final InputMessageDao inputMessageDao,
            final BroadcastMsgDao broadcastMessageDao) {
        this.purgeDays = purgeDays;
        this.inputMessageDao = inputMessageDao;
        this.broadcastMessageDao = broadcastMessageDao;
    }

    public void purge() {
        if (purgeDays < 0) {
            return;
        }
        Calendar purgeTime = Calendar.getInstance();
        purgeTime.add(Calendar.DAY_OF_YEAR, -purgeDays);
        purgeAudioFiles(purgeTime);
        purgeDatabase(purgeTime);
    }

    protected void purgeDatabase(Calendar purgeTime) {
        List<InputMessage> inputMessages = inputMessageDao
                .getPurgableMessages(purgeTime);
        logger.info("Purging {} messages older than {}", inputMessages.size(),
                purgeTime.getTime());
        List<Path> audioFilesToDelete = new ArrayList<>(inputMessages.size());
        for (InputMessage inputMessage : inputMessages) {
            for (BroadcastMsg broadcastMessage : broadcastMessageDao
                    .getMessagesByInputMsgId(inputMessage.getId())) {
                for (BroadcastFragment fragment : broadcastMessage
                        .getFragments()) {
                    audioFilesToDelete.add(Paths.get(fragment.getOutputName()));
                }
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
    }

    protected void purgeAudioFiles(Calendar purgeTime) {
        Path dataDir = BMHConstants.getBmhDataDirectory(true).resolve(
                BMHConstants.AUDIO_DATA_DIRECTORY);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dataDir,
                DATE_GLOB)) {
            for (Path datedDir : stream) {
                purgeAudioFilesInDatedDirectory(datedDir, purgeTime);
            }
        } catch (IOException e) {
            logger.error("Cannot clear old orphaned audio files.", e);
        }
        Path wxDir = dataDir.resolve("WXMessages");
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(wxDir,
                DATE_GLOB)) {
            for (Path datedDir : stream) {
                purgeAudioFilesInDatedDirectory(datedDir, purgeTime);
            }
        } catch (IOException e) {
            logger.error("Cannot clear old orphaned audio files.", e);
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

}
