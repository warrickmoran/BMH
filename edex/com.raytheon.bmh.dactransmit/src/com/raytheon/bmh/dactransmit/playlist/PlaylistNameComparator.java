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

import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Used to order playlist names by priority, trigger, and/or creation in
 * descending order to ensure that the most recent versions of the playlists
 * with the highest priorities are listed before all other playlists in the
 * list.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 18, 2016 5382       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class PlaylistNameComparator implements Comparator<String> {

    public static final int PRIORITY_GROUP = 1;

    public static final int CREATION_GROUP = 2;

    public static final int TRIGGER_GROUP = 3;

    private final Pattern playlistNamePattern;

    public PlaylistNameComparator(Pattern playlistNamePattern) {
        this.playlistNamePattern = playlistNamePattern;
    }

    @Override
    public int compare(String o1, String o2) {
        final Matcher match1 = this.playlistNamePattern.matcher(o1);
        match1.matches();
        final Matcher match2 = this.playlistNamePattern.matcher(o2);
        match2.matches();

        long o1Pr = getMatcherNumericValue(match1, PRIORITY_GROUP);
        long o2Pr = getMatcherNumericValue(match2, PRIORITY_GROUP);

        int comparison = Long.compare(o2Pr, o1Pr);
        if (comparison != 0) {
            return comparison;
        }

        long o1Time = getMatcherNumericValue(match1, TRIGGER_GROUP);
        long o2Time = getMatcherNumericValue(match2, TRIGGER_GROUP);

        comparison = Long.compare(o2Time, o1Time);
        if (comparison != 0) {
            return comparison;
        }

        long o1Create = getMatcherNumericValue(match1, CREATION_GROUP);
        long o2Create = getMatcherNumericValue(match2, CREATION_GROUP);
        return Long.compare(o2Create, o1Create);
    }

    public static long getMatcherNumericValue(final Matcher matcher,
            final int groupNum) {
        String matcherStr = matcher.group(groupNum);
        return Long.parseLong(matcherStr);
    }
}