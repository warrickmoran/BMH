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

import com.raytheon.uf.common.bmh.datamodel.msg.Suite;
import com.raytheon.uf.common.bmh.datamodel.msg.Suite.SuiteType;

/**
 * 
 * DAO for {@link Suite} objects.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Jul 14, 2014           rjpeter     Initial creation
 * Aug 06, 2014 #3490     lvenable    Updated to get Suite information.
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class SuiteDao extends AbstractBMHDao<Suite, String> {

    public SuiteDao() {
        super(Suite.class);
    }

    /**
     * Get all of the suites and associated data.
     * 
     * @return List of suites.
     */
    public List<Suite> getSuites() {
        List<Object> allObjects = this.loadAll();
        if (allObjects == null) {
            return Collections.emptyList();
        }

        List<Suite> suiteList = new ArrayList<Suite>(allObjects.size());
        for (Object obj : allObjects) {
            Suite suite = (Suite) obj;
            suiteList.add(suite);
        }

        return suiteList;
    }

    /**
     * Get a list of Suite objects that have the suite names and categories.
     * 
     * @return A list of Program objects.
     */
    public List<Suite> getSuiteNameCategories() {

        List<Suite> suiteList;

        List<Object[]> namesCats = txTemplate
                .execute(new TransactionCallback<List<Object[]>>() {
                    @Override
                    public List<Object[]> doInTransaction(
                            TransactionStatus status) {
                        HibernateTemplate ht = getHibernateTemplate();
                        return ht
                                .findByNamedQuery(Suite.GET_SUITE_NAMES_CATS_IDS);
                    }
                });

        if (namesCats == null) {
            suiteList = Collections.emptyList();
        } else {
            suiteList = new ArrayList<Suite>(namesCats.size());
            for (Object[] objArray : namesCats) {
                Suite p = new Suite();
                p.setName((String) objArray[0]);
                p.setType((SuiteType) objArray[1]);
                p.setId((Integer) objArray[2]);
                suiteList.add(p);
            }
        }

        return suiteList;
    }
}
