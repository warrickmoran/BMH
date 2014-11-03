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
import java.util.Collections;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.dao.DataAccessException;
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
 * Oct 22, 2014  3747     bkowal      Wrap all database actions in txTemplate with
 *                                    getCurrentSession.
 * Nov 02, 2014  3746     rjpeter     Updated loadAll to return empty list on null.
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
    @SuppressWarnings("unchecked")
    public T getByID(final I id) throws DataAccessException {
        return txTemplate.execute(new TransactionCallback<T>() {
            @Override
            public T doInTransaction(TransactionStatus status) {
                return (T) getCurrentSession().get(daoClass, id);
            }
        });
    }

    /**
     * Temporary type safety work around until CoreDao generics fixed.
     * 
     * @return
     */
    public List<T> getAll() {
        return loadAll();
    }

    /**
     * Loads all persistent instances of the daoClass
     * 
     * @return All persistent instances of the daoClass
     */
    @SuppressWarnings("unchecked")
    public List<T> loadAll() {
        List<T> rval = (List<T>) loadAll(daoClass);

        if (rval == null) {
            rval = Collections.emptyList();
        }

        return rval;
    }

    public List<?> loadAll(final Class<?> clazz) {
        return txTemplate.execute(new TransactionCallback<List<?>>() {
            @Override
            public List<?> doInTransaction(TransactionStatus status) {
                return getCurrentSession().createCriteria(clazz).list();
            }
        });
    }

    public List<?> findByNamedQuery(final String queryName) {
        return txTemplate.execute(new TransactionCallback<List<?>>() {
            @Override
            public List<?> doInTransaction(TransactionStatus status) {
                return getCurrentSession().getNamedQuery(queryName).list();
            }
        });
    }

    public List<?> findByNamedQueryAndNamedParam(final String queryName,
            final String name, final Object parameter) {
        return txTemplate.execute(new TransactionCallback<List<?>>() {
            @Override
            public List<?> doInTransaction(TransactionStatus status) {
                Session session = getCurrentSession();
                Query query = session.getNamedQuery(queryName);
                return query.setParameter(name, parameter).list();
            }
        });
    }

    public List<?> findByNamedQueryAndNamedParam(final String queryName,
            final String[] names, final Object[] values) {
        if ((names == null) || (values == null)
                || (names.length != values.length)) {
            throw new IllegalArgumentException(
                    "Length of parameter names and parameter value arrays must match!");

        }
        return txTemplate.execute(new TransactionCallback<List<?>>() {
            @Override
            public List<?> doInTransaction(TransactionStatus status) {
                Session session = getCurrentSession();
                Query query = session.getNamedQuery(queryName);
                for (int i = 0; i < names.length; i++) {
                    query.setParameter(names[i], values[i]);
                }
                return query.list();
            }
        });
    }

}