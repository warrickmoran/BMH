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
package com.raytheon.uf.common.bmh.datamodel.playlist;

import java.io.File;
import java.util.Calendar;
import java.util.Formatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.time.util.TimeUtil;

/**
 * 
 * Notification object that is sent out every time a new playlist file is
 * written.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Jul 02, 2014  3285     bsteffen    Initial creation
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
@DynamicSerialize
public class PlaylistUpdateNotification {

    private static final Pattern PATH_PARSE_PATTERN = Pattern
            .compile("^([^"
                    + Pattern.quote(File.separator)
                    + "]+)"
                    + Pattern.quote(File.separator)
                    + "P(\\d+)_([^_]*)_(\\d{4})(\\d{2})(\\d{2})(\\d{2})(\\d{2})_S(\\d{2})(\\d{2})(\\d{2})_E(\\d{2})(\\d{2})(\\d{2}).xml$");

    @DynamicSerializeElement
    private String playlistPath;

    public PlaylistUpdateNotification() {
    }

    public PlaylistUpdateNotification(DACPlaylist playlist) {
        this.playlistPath = getFilePath(playlist).toString();
    }

    public String getPlaylistPath() {
        return playlistPath;
    }

    public void setPlaylistPath(String playlistPath) {
        this.playlistPath = playlistPath;
    }

    /**
     * Shortcut for parseFilePath(this.getPlaylistPath()).
     * 
     * @see #parseFilePath(CharSequence)
     */
    public DACPlaylist parseFilepath() {
        return parseFilePath(playlistPath);
    }

    /**
     * Encode the DACPlaylist as a filename in a specific format.
     * 
     * @param playlist
     *            the playlist to encode
     * @return the path and filename where the playlist should be stored.
     */
    public static CharSequence getFilePath(DACPlaylist playlist) {
        StringBuilder result = new StringBuilder();
        result.append(playlist.getTransmitterGroup());
        result.append(File.separatorChar);
        result.append("P");
        result.append(playlist.getPriority());
        result.append("_");
        result.append(playlist.getSuite());
        result.append("_");
        try (Formatter formatter = new Formatter(result)) {
            formatter.format("%1$tY%1$tm%1$td%1$tH%1$tM",
                    playlist.getCreationTime());
            result.append("_S");
            formatter.format("%1$td%1$tH%1$tM", playlist.getStart());
            result.append("_E");
            formatter.format("%1$td%1$tH%1$tM", playlist.getExpired());
            result.append(".xml");
        }
        return result;
    }

    /**
     * Creates a {@link DACPlaylist} and populates all fields that can be parsed
     * from the file path.
     * 
     * @param filename
     *            a filename of the format returned from
     *            {@link #getFilePath(DACPlaylist)}.
     * @return a partially populated {@link DACPlaylist}.
     * @see #getFilePath(DACPlaylist)
     */
    public static DACPlaylist parseFilePath(CharSequence filename) {
        DACPlaylist playlist = null;
        Matcher m = PATH_PARSE_PATTERN.matcher(filename);
        if (m.find()) {
            playlist = new DACPlaylist();
            playlist.setTransmitterGroup(m.group(1));
            playlist.setPriority(Integer.parseInt(m.group(2)));
            playlist.setSuite(m.group(3));
            int year = Integer.parseInt(m.group(4));
            int month = Integer.parseInt(m.group(5));
            int day = Integer.parseInt(m.group(6));
            int hour = Integer.parseInt(m.group(7));
            int minute = Integer.parseInt(m.group(8));
            Calendar c = TimeUtil.newGmtCalendar(year, month, day);
            c.set(Calendar.HOUR_OF_DAY, hour);
            c.set(Calendar.MINUTE, minute);
            playlist.setCreationTime(c);
            day = Integer.parseInt(m.group(9));
            hour = Integer.parseInt(m.group(10));
            minute = Integer.parseInt(m.group(11));
            c = TimeUtil.newGmtCalendar(year, month, day);
            c.set(Calendar.HOUR_OF_DAY, hour);
            c.set(Calendar.MINUTE, minute);
            playlist.setStart(c);
            day = Integer.parseInt(m.group(12));
            hour = Integer.parseInt(m.group(13));
            minute = Integer.parseInt(m.group(14));
            c = TimeUtil.newGmtCalendar(year, month, day);
            c.set(Calendar.HOUR_OF_DAY, hour);
            c.set(Calendar.MINUTE, minute);
            playlist.setExpired(c);
        }
        return playlist;
    }

}
