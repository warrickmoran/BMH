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
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link Comparator} used to sort message metadata files based on the timestamp
 * specified in the file name.
 * 
 * Note: the current usage of this {@link Comparator} assumes that all inputs
 * will match the specified message metadata file pattern passed to the
 * constructor. That is currently the case based on how it is used in:
 * com.raytheon.bmh.dactransmit.playlist.PlaylistMessageArchiverTask.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 26, 2016 5382       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class MessageMetadataPathComparator implements Comparator<Path> {

    private static final int METADATA_TIMESTAMP_GROUP = 2;

    private final Pattern messageMetadataFilePattern;

    public MessageMetadataPathComparator(
            final Pattern messageMetadataFilePattern) {
        this.messageMetadataFilePattern = messageMetadataFilePattern;
    }

    @Override
    public int compare(Path o1, Path o2) {
        Matcher matcher1 = this.messageMetadataFilePattern.matcher(o1
                .getFileName().toString());
        matcher1.matches();
        Matcher matcher2 = this.messageMetadataFilePattern.matcher(o2
                .getFileName().toString());
        matcher2.matches();

        final String metadata1TimestampStr = matcher1
                .group(METADATA_TIMESTAMP_GROUP);
        final String metadata2TimestampStr = matcher2
                .group(METADATA_TIMESTAMP_GROUP);

        final long metadata1Timestamp = Long.parseLong(metadata1TimestampStr);
        final long metadata2Timestamp = Long.parseLong(metadata2TimestampStr);

        return Long.compare(metadata1Timestamp, metadata2Timestamp);
    }
}