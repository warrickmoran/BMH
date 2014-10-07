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
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.raytheon.uf.common.bmh.TIME_MSG_TOKENS;

/**
 * Used to cache time audio data associated with a {@link TIME_MSG_TOKENS} and a
 * unique identifier. Created and associated with a
 * {@link DynamicTimeAudioFileBuffer} when dynamic audio is discovered during
 * file retrieval by the {@link RetrieveAudioJob}.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 6, 2014  3642       bkowal     Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class TimeMsgCache extends SimpleFileVisitor<Path> {

    private Table<String, String, byte[]> timeCache;

    /**
     * 
     */
    public TimeMsgCache() {
        this.timeCache = HashBasedTable.create();
    }

    public TIME_MSG_TOKENS loadCache(final Path timeDirectory)
            throws IOException {
        Files.walkFileTree(timeDirectory, this);
        return TIME_MSG_TOKENS.lookupToken(timeDirectory.getFileName()
                .toString());
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.nio.file.SimpleFileVisitor#visitFile(java.lang.Object,
     * java.nio.file.attribute.BasicFileAttributes)
     */
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
            throws IOException {
        final String entityIdentifier = file.getFileName().toString();
        final String typeIdentifier = file.getParent().getFileName().toString();

        this.timeCache.put(typeIdentifier, entityIdentifier,
                Files.readAllBytes(file));

        return FileVisitResult.CONTINUE;
    }

    public Table<String, String, byte[]> getTimeCache() {
        return timeCache;
    }

    public byte[] lookupTimeCache(final TIME_MSG_TOKENS token,
            final String entityIdentifier) {
        return this.timeCache.get(token.getIdentifier(), entityIdentifier);
    }
}