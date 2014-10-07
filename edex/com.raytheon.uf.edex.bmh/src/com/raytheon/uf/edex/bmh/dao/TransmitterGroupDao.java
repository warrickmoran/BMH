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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;

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
 * 
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
        List<TransmitterGroup> types = txTemplate
                .execute(new TransactionCallback<List<TransmitterGroup>>() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public List<TransmitterGroup> doInTransaction(
                            TransactionStatus status) {
                        HibernateTemplate ht = getHibernateTemplate();
                        return ht
                                .findByNamedQueryAndNamedParam(
                                        TransmitterGroup.GET_TRANSMITTER_GROUP_FOR_NAME,
                                        new String[] { "name" },
                                        new Object[] { name });
                    }
                });

        if ((types != null) && !types.isEmpty()) {
            return types.get(0);
        }

        return null;
    }

    public List<TransmitterGroup> getTransmitterGroups() {
        List<Object> objList = this.loadAll();
        if ((objList == null) || objList.isEmpty()) {
            // No data
            return Collections.emptyList();
        }

        List<TransmitterGroup> tGroup = new ArrayList<TransmitterGroup>();
        for (Object o : objList) {
            tGroup.add((TransmitterGroup) o);
        }

        return tGroup;
    }

    public List<TransmitterGroup> getEnabledTransmitterGroups() {
        List<TransmitterGroup> groups = txTemplate
                .execute(new TransactionCallback<List<TransmitterGroup>>() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public List<TransmitterGroup> doInTransaction(
                            TransactionStatus status) {
                        HibernateTemplate ht = getHibernateTemplate();
                        return ht
                                .findByNamedQuery(TransmitterGroup.GET_ENABLED_TRANSMITTER_GROUPS);
                    }
                });

        return groups;
    }
}