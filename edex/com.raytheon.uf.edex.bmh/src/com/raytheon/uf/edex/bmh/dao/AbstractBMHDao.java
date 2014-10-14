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

import java.io.Serializable;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

import com.raytheon.uf.edex.database.dao.CoreDao;
import com.raytheon.uf.edex.database.dao.DaoConfig;

/**
 * Abstract BMH DAO implementation. Provides a common interface to the Core DAO.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#    Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Jun 24, 2014  3302     bkowal      Initial creation.
 * Jul 17, 2014  3175     rjpeter     Added getAll.
 * Oct 06, 2014  3687     bsteffen    Add operational flag to constructor.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class AbstractBMHDao<T, I extends Serializable> extends CoreDao {

    public static final String BMH_DATABASE_NAME = "bmh";

    public static final String BMH_PRACTICE_DATABASE_NAME = "bmh_practice";

    public static String getDatabaseName(boolean operational) {
        if (operational) {
            return BMH_DATABASE_NAME;
        } else {
            return BMH_PRACTICE_DATABASE_NAME;
        }
    }

    private final Class<T> daoClass;

    /**
     * Create a DAO for the operational database.
     * 
     * @see #AbstractBMHDao(boolean, Class)
     */
    public AbstractBMHDao(Class<T> daoClass) {
        this(true, daoClass);
    }

    /**
     * Create a new DAO for accessing BMH data.
     * 
     * @param operational
     *            true indicates the DAO should use the operational database,
     *            false will use the test/practice database.
     * @param daoClass
     *            The class that this dao is responsible for persisting.
     */
    public AbstractBMHDao(boolean operational, Class<T> daoClass) {
        super(DaoConfig.forClass(getDatabaseName(operational), daoClass));
        this.daoClass = daoClass;
    }

    /**
     * Abstract implementation of getByID for the DAO
     * 
     * @param id
     *            the id associated with the Object that should be retrieved.
     * @return the object that was retrieved; NULL if no object was found
     * @throws DataAccessException
     *             when retrieval fails
     */
    public T getByID(I id) throws DataAccessException {
        return super.getHibernateTemplate().get(this.daoClass, id);
    }

    /**
     * Temporary type safety work around until CoreDao generics fixed.
     * 
     * @return
     */
    public List<T> getAll() {
        return txTemplate.execute(new TransactionCallback<List<T>>() {
            @Override
            public List<T> doInTransaction(TransactionStatus status) {
                HibernateTemplate ht = getHibernateTemplate();
                return ht.loadAll(daoClass);
            }
        });
    }
}