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
package com.raytheon.uf.edex.bmh.dao;

import java.util.List;

import com.raytheon.uf.common.bmh.datamodel.playlist.Playlist;
import com.raytheon.uf.edex.bmh.msg.logging.IMessageLogger;

/**
 * 
 * DAO for {@link Playlist} objects.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Jul 07, 2014  3285     bsteffen    Initial creation
 * Sep 09, 2014  3554     bsteffen    Add getByGroupName
 * Oct 06, 2014  3687     bsteffen    Add operational flag to constructor.
 * Jan 06, 2015  3651     bkowal      Support AbstractBMHPersistenceLoggingDao.
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class PlaylistDao extends
        AbstractBMHPersistenceLoggingDao<Playlist, Integer> {

    public PlaylistDao(final IMessageLogger messageLogger) {
        super(Playlist.class, messageLogger);
    }

    public PlaylistDao(boolean operational, final IMessageLogger messageLogger) {
        super(operational, Playlist.class, messageLogger);
    }

    @SuppressWarnings("unchecked")
    public List<Playlist> getByGroupName(final String transmitterGroupName) {
        return (List<Playlist>) findByNamedQueryAndNamedParam(
                Playlist.QUERY_BY_GROUP_NAME, "groupName", transmitterGroupName);
    }

    public Playlist getBySuiteAndGroupName(final String suiteName,
            final String transmitterGroupName) {
        List<?> playlists = findByNamedQueryAndNamedParam(
                Playlist.QUERY_BY_SUITE_GROUP_NAMES, new String[] {
                        "suiteName", "groupName" }, new String[] { suiteName,
                        transmitterGroupName });
        if (playlists.isEmpty()) {
            return null;
        } else {
            return (Playlist) playlists.get(0);
        }
    }

}
