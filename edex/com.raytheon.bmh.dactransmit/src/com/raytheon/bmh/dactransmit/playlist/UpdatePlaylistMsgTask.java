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

import java.nio.file.Path;
import java.util.concurrent.locks.ReentrantLock;
import java.io.OutputStream;

import javax.xml.bind.JAXB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raytheon.uf.common.bmh.FilePermissionUtils;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessage;
import com.raytheon.uf.common.bmh.lock.InMemoryFileLockManager;
import com.raytheon.uf.common.util.file.IOPermissionsHelper;

/**
 * {@link Runnable} created to asynchronously write an updated version of a
 * {@link DacPlaylistMessage}.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 3, 2016  5766       bkowal      Initial creation
 * May 02, 2017 6259       bkowal      Updated to use {@link IOPermissionsHelper}.
 * 
 * </pre>
 * 
 * @author bkowal
 */

public class UpdatePlaylistMsgTask implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    protected final DacPlaylistMessage messageToWrite;

    public UpdatePlaylistMsgTask(final DacPlaylistMessage messageToWrite) {
        if (messageToWrite == null) {
            throw new IllegalArgumentException(
                    "Required argument 'messageToWrite' cannot be NULL.");
        }
        this.messageToWrite = messageToWrite;
    }

    @Override
    public void run() {
        writePlaylistMsgState(messageToWrite);
    }

    protected void writePlaylistMsgState(final DacPlaylistMessage message) {
        Path msgPath = message.getPath();
        ReentrantLock fileLock = null;
        try {
            fileLock = InMemoryFileLockManager.getInstance()
                    .requestResourceLock(msgPath, 1000L);
            if (fileLock == null) {
                logger.error("Unable to write updated message file: "
                        + msgPath.toString() + ". Failed to lock the file.");
                return;
            }
            try (OutputStream os = IOPermissionsHelper.getOutputStream(msgPath,
                    FilePermissionUtils.FILE_PERMISSIONS_SET)) {
                JAXB.marshal(message, os);
            }
        } catch (Throwable e) {
            logger.error("Unable to persist message state.", e);
        } finally {
            if (fileLock != null) {
                fileLock.unlock();
            }
        }
    }
}