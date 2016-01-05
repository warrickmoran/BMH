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

import org.springframework.transaction.TransactionException;

import com.raytheon.uf.common.bmh.trace.ITraceable;
import com.raytheon.uf.edex.bmh.msg.logging.ErrorActivity.BMH_ACTIVITY;
import com.raytheon.uf.edex.bmh.msg.logging.IMessageLogger;

/**
 * Further abstraction of the {@link AbstractBMHDao} that is capable of logging
 * data persistence failures to fulfill the BMH message logging requirements.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 6, 2015  3651       bkowal      Initial creation
 * May 13, 2015 4429       rferrel     Use traceId in log messages.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public abstract class AbstractBMHPersistenceLoggingDao<T, I extends Serializable>
        extends AbstractBMHDao<T, I> {

    protected final IMessageLogger messageLogger;

    public AbstractBMHPersistenceLoggingDao(Class<T> daoClass,
            final IMessageLogger messageLogger) {
        this(true, daoClass, messageLogger);
    }

    public AbstractBMHPersistenceLoggingDao(boolean operational,
            Class<T> daoClass, final IMessageLogger messageLogger) {
        super(operational, daoClass);
        this.messageLogger = messageLogger;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.edex.database.dao.CoreDao#persist(java.lang.Object)
     */
    @Override
    public void persist(final Object obj) throws TransactionException {
        saveOrUpdate(obj);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.edex.database.dao.CoreDao#saveOrUpdate(java.lang.Object)
     */
    @Override
    public void saveOrUpdate(final Object obj) {
        try {
            super.saveOrUpdate(obj);
        } catch (Exception e) {
            ITraceable it = null;
            if (obj instanceof ITraceable) {
                it = (ITraceable) obj;
            }
            this.messageLogger.logDaoError(it, BMH_ACTIVITY.DATA_STORAGE, obj,
                    e);
            throw e;
        }
    }
}