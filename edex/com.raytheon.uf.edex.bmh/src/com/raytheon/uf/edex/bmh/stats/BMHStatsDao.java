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
package com.raytheon.uf.edex.bmh.stats;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;

import com.raytheon.uf.edex.bmh.dao.AbstractBMHDao;
import com.raytheon.uf.edex.database.dao.CoreDao;
import com.raytheon.uf.edex.database.dao.DaoConfig;

/**
 * Dao for loading and interacting with the BMH message delivery statistic
 * procedure.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 29, 2015 4686       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class BMHStatsDao extends CoreDao {

    private final String SP_QUERY = "SELECT messageDeliveryStat(:begin_time, :end_time)";

    public BMHStatsDao() {
        super(DaoConfig.forDatabase(AbstractBMHDao.BMH_DATABASE_NAME));
    }

    public void importSQLProcedure(final String sql) {
        txTemplate.execute(new TransactionCallbackWithoutResult() {

            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                Session session = getCurrentSession();

                Query query = session.createSQLQuery(sql);
                query.executeUpdate();
            }
        });
    }

    public DeliveryStats executeMessageDeliveryProcedure(
            final Calendar beginTime, final Calendar endTime) {
        List<?> results = txTemplate
                .execute(new TransactionCallback<List<?>>() {
                    @Override
                    public List<?> doInTransaction(TransactionStatus status) {
                        SQLQuery sqlQuery = getCurrentSession().createSQLQuery(
                                SP_QUERY);
                        sqlQuery.setCalendar("begin_time", beginTime);
                        sqlQuery.setCalendar("end_time", endTime);
                        return sqlQuery.list();
                    }
                });

        BigDecimal expected = (BigDecimal) results.get(0);
        BigDecimal actual = (BigDecimal) results.get(1);
        BigDecimal pctSuccess = (BigDecimal) results.get(2);
        return new DeliveryStats(expected.doubleValue(), actual.doubleValue(),
                pctSuccess.doubleValue());
    }
}