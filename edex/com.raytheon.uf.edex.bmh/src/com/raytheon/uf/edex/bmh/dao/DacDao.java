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

import org.hibernate.Session;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.util.CollectionUtils;

import com.raytheon.uf.common.bmh.datamodel.dac.Dac;
import com.raytheon.uf.common.bmh.datamodel.dac.DacChannel;

/**
 * 
 * Data access Object for {@link Dac}s.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Aug 04, 2014  3486     bsteffen    Initial creation
 * Oct 06, 2014  3687     bsteffen    Add operational flag to constructor.
 * Nov 06, 2015  5092     bkowal      Update dac updates / delete for {@link DacChannel}.
 * Nov 09, 2015  5113     bkowal      Added {@link #validateDacUniqueness(int, String, String, String, int)}.
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class DacDao extends AbstractBMHDao<Dac, String> {

    public DacDao() {
        super(Dac.class);
    }

    public DacDao(boolean operational) {
        super(operational, Dac.class);
    }

    @Override
    public void saveOrUpdate(final Object object) {
        if (object instanceof Dac) {
            txTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(
                        TransactionStatus status) {
                    removeDacChannels(getCurrentSession(), (Dac) object);
                    getCurrentSession().saveOrUpdate(object);
                }
            });
        } else {
            super.saveOrUpdate(object);
        }
    }

    @Override
    public void delete(final Object object) {
        if (object instanceof Dac) {
            txTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(
                        TransactionStatus status) {
                    removeDacChannels(getCurrentSession(), (Dac) object);
                    getCurrentSession().delete(object);
                }
            });
        } else {
            super.delete(object);
        }
    }

    private void removeDacChannels(final Session session, final Dac dac) {
        if (dac.getId() == 0) {
            return;
        }

        session.createQuery("DELETE FROM DacChannel WHERE dac_id = ?")
                .setParameter(0, dac.getId()).executeUpdate();
    }

    public Dac validateDacUniqueness(final int dacId, final String name,
            final String address, final String receiveAddress,
            final int receivePort) {
        List<?> objects = this.findByNamedQueryAndNamedParam(
                Dac.VALIDATE_DAC_UNIQUENESS, new String[] { "dacId", "name",
                        "address", "receiveAddress", "receivePort" },
                new Object[] { dacId, name, address, receiveAddress,
                        receivePort });
        if (CollectionUtils.isEmpty(objects)) {
            return null;
        }

        return (Dac) objects.get(0);
    }
}