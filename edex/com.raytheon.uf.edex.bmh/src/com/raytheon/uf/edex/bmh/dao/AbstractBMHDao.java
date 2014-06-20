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

import org.springframework.dao.DataAccessException;

import com.raytheon.uf.edex.database.dao.CoreDao;
import com.raytheon.uf.edex.database.dao.DaoConfig;

/**
 * Abstract BMH DAO implementation. Provides a common interface to the Core DAO.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 24, 2014 3302       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class AbstractBMHDao<T, I extends Serializable> extends CoreDao {

    private Class<T> daoClass;

    public AbstractBMHDao(Class<T> daoClass) {
        super(DaoConfig.forClass(DatabaseConstants.BMH_DATABASE_NAME, daoClass));
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
}