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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Maintains the ordered list of files to be transmitted to the DAC for
 * playback.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 2, 2014   #3286    dgilling     Initial creation
 * 
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

public final class AudioFileDirectoryPlaylist implements
        Iterable<PlaylistMessage> {

    private static final String ULAW_FILE_PATTERN = "*.ulaw";

    private final List<PlaylistMessage> playlist;

    /**
     * Builds a playlist based on the files matching the extension .ULAW in the
     * specified input directory. Files will be sorted in order of creation
     * time.
     * 
     * @param dirOfFiles
     *            Directory of files to read and build the playlist from.
     * @throws IOException
     *             If the directory cannot be read or one of the files in the
     *             directory cannot be read.
     */
    public AudioFileDirectoryPlaylist(final Path dirOfFiles) throws IOException {
        playlist = new ArrayList<>();

        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(
                dirOfFiles, ULAW_FILE_PATTERN)) {
            for (Path entry : dirStream) {
                playlist.add(new PlaylistMessage(entry));
            }
        }

        if (playlist.isEmpty()) {
            throw new IllegalStateException("Can not play an empty playlist.");
        }

        Collections.sort(playlist, PlaylistMessage.SORT_BY_CREATION_TIME);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<PlaylistMessage> iterator() {
        Iterator<PlaylistMessage> iterator = new Iterator<PlaylistMessage>() {

            private int position = 0;

            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public PlaylistMessage next() {
                if (position >= playlist.size()) {
                    position = 0;
                }

                PlaylistMessage nextFile = playlist.get(position);
                position++;
                return nextFile;
            }

            @Override
            public void remove() {
                // unsupported operation
            }
        };

        return iterator;
    }

    public Collection<PlaylistMessage> getUniqueFiles() {
        return playlist;
    }
}
