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

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;

import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterLanguage;

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
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class TransmitterGroupDao extends
        AbstractBMHDao<TransmitterGroup, Integer> {
    public TransmitterGroupDao() {
        super(TransmitterGroup.class);
    }

    public TransmitterGroupDao(boolean operational) {
        super(operational, TransmitterGroup.class);
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

    @SuppressWarnings("unchecked")
    public List<TransmitterGroup> getEnabledTransmitterGroups() {
        return (List<TransmitterGroup>) findByNamedQuery(TransmitterGroup.GET_ENABLED_TRANSMITTER_GROUPS);
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
}