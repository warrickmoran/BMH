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
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Comparator;

/**
 * A single entry within a playlist.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 09, 2014  #3286     dgilling     Initial creation
 * 
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

public final class PlaylistMessage {

    public static final Comparator<PlaylistMessage> SORT_BY_CREATION_TIME = new Comparator<PlaylistMessage>() {

        @Override
        public int compare(PlaylistMessage o1, PlaylistMessage o2) {
            try {
                FileTime o1Time = Files.readAttributes(o1.filename,
                        BasicFileAttributes.class).lastModifiedTime();
                FileTime o2Time = Files.readAttributes(o2.filename,
                        BasicFileAttributes.class).lastModifiedTime();
                return o1Time.compareTo(o2Time);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return 0;
        }
    };

    private final Path filename;

    public PlaylistMessage(Path filename) {
        this.filename = filename;
    }

    @Override
    public int hashCode() {
        return filename.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return filename.equals(obj);
    }

    @Override
    public String toString() {
        return filename.toString();
    }

    public Path getFilename() {
        return filename;
    }
}