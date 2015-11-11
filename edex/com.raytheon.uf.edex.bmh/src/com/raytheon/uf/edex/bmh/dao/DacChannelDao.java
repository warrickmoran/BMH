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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.util.CollectionUtils;

import com.raytheon.uf.common.bmh.datamodel.dac.Dac;
import com.raytheon.uf.common.bmh.datamodel.dac.DacChannel;
import com.raytheon.uf.common.bmh.datamodel.dac.DacChannelPK;

/**
 * Data Access Object for {@link DacChannel}s.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 6, 2015  5113       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class DacChannelDao extends AbstractBMHDao<DacChannel, DacChannelPK> {

    public DacChannelDao() {
        super(DacChannel.class);
    }

    public DacChannelDao(boolean operational) {
        super(operational, DacChannel.class);
    }

    public Set<Dac> validateChannelsUniqueness(final int dacId,
            final Set<Integer> ports) {
        if (CollectionUtils.isEmpty(ports)) {
            return Collections.emptySet();
        }

        List<?> objects = txTemplate
                .execute(new TransactionCallback<List<?>>() {
                    @Override
                    public List<?> doInTransaction(TransactionStatus status) {
                        Session session = getCurrentSession();
                        Query query = session
                                .getNamedQuery(DacChannel.VALIDATE_CHANNEL_UNIQUENESS);
                        query.setParameterList("ports", ports);
                        return query.setParameter("dacId", dacId).list();
                    }
                });

        if (CollectionUtils.isEmpty(objects)) {
            return Collections.emptySet();
        }
        Set<Dac> dacs = new HashSet<>(objects.size(), 1.0f);
        for (Object object : objects) {
            dacs.add((Dac) object);
        }

        return dacs;
    }
}