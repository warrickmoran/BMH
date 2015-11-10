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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.hibernate.Session;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.util.CollectionUtils;

import com.raytheon.uf.common.bmh.datamodel.PositionUtil;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterLanguage;
import com.raytheon.uf.edex.bmh.BMHConstants;
import com.raytheon.uf.edex.database.cluster.ClusterLocker;
import com.raytheon.uf.edex.database.cluster.ClusterTask;
import com.raytheon.uf.edex.database.cluster.ClusterLockUtils.LockState;

/**
 * BMH DAO for {@link TransmitterGroup}.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Jun 24, 2014  3302     bkowal      Initial creation
 * Aug 04, 2014  3173     mpduff      Added getTransmitterGroups()
 * Aug 25, 2014  3558     rjpeter     Added getEnabledTransmitterGroups()
 * Oct 06, 2014  3687     bsteffen    Add operational flag to constructor.
 * Nov 21, 2014  3845     bkowal      Added getTransmitterGroupWithTransmitter
 * Jan 22, 2015  3995     rjpeter     Added getNextPosition()
 * Feb 09, 2015  4082     bkowal      Added {@link #createGroupAndLanguages(TransmitterGroup, Collection)}.
 * Apr 14, 2015  4390     rferrel     Added {@link #reorderTransmitterGroup(List)}.
 * Apr 14, 2015  4394     bkowal      Added {@link #getConfiguredTransmitterGroups()}.
 * Jul 17, 2015  4636     bkowal      Added {@link #getTransmitterGroupsWithIds(Set)}.
 * Aug 10, 2015  4424     bkowal      Added {@link #saveRenamedTransmitterGroup(TransmitterGroup, String)}.
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class TransmitterGroupDao extends
        AbstractBMHDao<TransmitterGroup, Integer> {

    private final Path playlistDir;

    private final ClusterLocker locker;

    public TransmitterGroupDao() {
        this(true);
    }

    public TransmitterGroupDao(boolean operational) {
        super(operational, TransmitterGroup.class);
        playlistDir = BMHConstants.getBmhDataDirectory(operational).resolve(
                BMHConstants.PLAYLIST_DIRECTORY);
        locker = new ClusterLocker(AbstractBMHDao.getDatabaseName(operational));
    }

    /**
     * Looks up the TransmitterGroup for the given name.
     * 
     * @param areaCode
     * @return
     */
    public TransmitterGroup getByGroupName(final String name) {
        @SuppressWarnings("unchecked")
        List<TransmitterGroup> types = (List<TransmitterGroup>) findByNamedQueryAndNamedParam(
                TransmitterGroup.GET_TRANSMITTER_GROUP_FOR_NAME, "name", name);

        if ((types != null) && !types.isEmpty()) {
            return types.get(0);
        }

        return null;
    }

    public List<TransmitterGroup> getTransmitterGroups() {
        List<TransmitterGroup> tGroup = this.loadAll();
        if ((tGroup == null) || tGroup.isEmpty()) {
            // No data
            return Collections.emptyList();
        }

        return tGroup;
    }

    public List<TransmitterGroup> getTransmitterGroupsWithIds(
            final Set<Integer> ids) {
        List<?> returnObjects = this.findAllByNamedInQuery(
                TransmitterGroup.GET_TRANSMITTER_GROUPS_FOR_IDS, "tgids", ids);
        if (CollectionUtils.isEmpty(returnObjects)) {
            return Collections.emptyList();
        }

        List<TransmitterGroup> transmitterGroups = new ArrayList<>(
                returnObjects.size());
        for (Object object : returnObjects) {
            transmitterGroups.add((TransmitterGroup) object);
        }

        return transmitterGroups;
    }

    @SuppressWarnings("unchecked")
    public List<TransmitterGroup> getEnabledTransmitterGroups() {
        return (List<TransmitterGroup>) findByNamedQuery(TransmitterGroup.GET_ENABLED_TRANSMITTER_GROUPS);
    }

    public List<TransmitterGroup> getConfiguredTransmitterGroups() {
        List<?> results = this
                .findByNamedQuery(TransmitterGroup.GET_CONFIGURED_TRANSMITTER_GROUPS);
        if (results == null || results.isEmpty()) {
            return Collections.emptyList();
        }

        List<TransmitterGroup> groups = new ArrayList<>(results.size());
        for (Object object : results) {
            groups.add((TransmitterGroup) object);
        }

        return groups;
    }

    public TransmitterGroup getTransmitterGroupWithTransmitter(
            final int transmitterId) {
        List<?> results = this.findByNamedQueryAndNamedParam(
                TransmitterGroup.GET_TRANSMITTER_GROUP_CONTAINS_TRANSMITTER,
                "transmitterId", transmitterId);
        if ((results == null) || results.isEmpty()) {
            return null;
        }

        if (results.get(0) instanceof TransmitterGroup) {
            return (TransmitterGroup) results.get(0);
        }

        return null;
    }

    /**
     * Returns position number of next new group.
     * 
     * @return
     */
    public int getNextPosition() {
        List<?> result = findByNamedQuery(TransmitterGroup.GET_TRANSMITTER_GROUP_MAX_POSITION);

        if ((result != null) && !result.isEmpty()) {
            Object row = result.get(0);
            if (row instanceof Number) {
                return ((Number) row).intValue() + 1;
            } else {
                logger.error("The "
                        + TransmitterGroup.GET_TRANSMITTER_GROUP_MAX_POSITION
                        + " query returned results in the wrong format. Expected a Number.");
            }
        }

        return 1;
    }

    /**
     * Used to save both a new {@link TransmitterGroup} and
     * {@link TransmitterLanguage}(s) in the same transaction.
     * 
     * @param tg
     *            the {@link TransmitterGroup} to save.
     * @param languages
     *            the {@link TransmitterLanguage}(s) to save.
     */
    public void createGroupAndLanguages(final TransmitterGroup tg,
            final Collection<TransmitterLanguage> languages) {
        if (tg.getId() != 0) {
            throw new IllegalArgumentException(
                    "createGroupAndLanguages only supports NEW Transmitter Group(s).");
        }
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                persist(tg);
                for (TransmitterLanguage tl : languages) {
                    tl.getId().setTransmitterGroup(tg);
                    persist(tl);
                }
            }
        });
    }

    /**
     * Change the order of Transmitter groups to match the order in the list. It
     * is assumed the contains all the transmitter groups.
     * 
     * @param tgList
     */
    public void reorderTransmitterGroup(final List<TransmitterGroup> tgList) {
        txTemplate.execute(new TransactionCallbackWithoutResult() {

            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                Session session = getCurrentSession();
                PositionUtil.updatePositions(tgList);
                for (TransmitterGroup tg : tgList) {
                    session.saveOrUpdate(tg);
                }
            }
        });
    }

    /**
     * Saves a {@link Transmitter} and the associated standalone
     * {@link TransmitterGroup} with a new name. Completes all file i/o
     * operations to ensure that both remain in sync.
     * 
     * @param tg
     *            the {@link TransmitterGroup} that has been renamed.
     * @param previousName
     *            the previous name of the {@link Transmitter}. Used to lookup
     *            filesystem assets that need to be relocated.
     */
    public void saveRenamedTransmitterGroup(final TransmitterGroup tg,
            final String previousName) {
        txTemplate.execute(new TransactionCallbackWithoutResult() {

            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                /*
                 * Persist {@link Transmitter} and {@link TransmitterGroup}.
                 * Ensure that the records can be persisted before attempting to
                 * relocate file system assets.
                 */
                persist(tg);

                /*
                 * Determine the {@link Path}s to the filesystem assets that we
                 * may be interacting with.
                 */
                final String newName = tg.getName();
                final Path oldPlaylistPath = playlistDir.resolve(previousName);
                final Path newPlaylistPath = playlistDir.resolve(newName);

                /*
                 * First, determine if is any data needs to be purged. Nothing
                 * should exist in the new {@link Path} yet.
                 */
                if (Files.exists(newPlaylistPath)) {
                    ClusterTask ct = null;
                    do {
                        ct = locker.lock("playlist", newName, 30000, true);
                    } while (!LockState.SUCCESSFUL.equals(ct.getLockState()));
                    try {
                        FileUtils.deleteDirectory(newPlaylistPath.toFile());
                    } catch (IOException e) {
                        throw new IllegalStateException(
                                "Failed to purge old playlist files: "
                                        + newPlaylistPath.toString()
                                        + " during the transmitter rename. Cancelling rename to prevent resources from getting out of sync.",
                                e);
                    } finally {
                        locker.deleteLock(ct.getId().getName(), ct.getId()
                                .getDetails());
                    }
                }

                /*
                 * Are there files that need to be moved to the new {@link
                 * Path}?
                 */
                if (Files.exists(oldPlaylistPath)) {
                    ClusterTask ct_old = null;
                    ClusterTask ct_new = null;
                    do {
                        ct_old = locker.lock("playlist", previousName, 30000,
                                true);
                    } while (!LockState.SUCCESSFUL.equals(ct_old.getLockState()));
                    do {
                        ct_new = locker.lock("playlist", newName, 30000, true);
                    } while (!LockState.SUCCESSFUL.equals(ct_new.getLockState()));
                    try {
                        FileUtils.moveDirectory(oldPlaylistPath.toFile(),
                                newPlaylistPath.toFile());
                    } catch (IOException e) {
                        throw new IllegalStateException(
                                "Failed to move the original playlist files: "
                                        + oldPlaylistPath.toString()
                                        + " to the new location: "
                                        + newPlaylistPath.toString() + ".", e);
                    } finally {
                        locker.deleteLock(ct_old.getId().getName(), ct_old
                                .getId().getDetails());
                        locker.deleteLock(ct_new.getId().getName(), ct_new
                                .getId().getDetails());
                    }
                }
            }
        });
    }
}