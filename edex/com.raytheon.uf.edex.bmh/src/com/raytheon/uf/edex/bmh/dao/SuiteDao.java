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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;

import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.common.bmh.datamodel.msg.Suite;
import com.raytheon.uf.common.bmh.datamodel.msg.Suite.SuiteType;
import com.raytheon.uf.common.bmh.datamodel.msg.SuiteMessage;

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
 * Aug 12, 2014 #3490     lvenable    Refactored to make a getSuiteByQuery() method that
 *                                    will used the query passed it to retrieve the data.
 * Aug 17, 2014 #3490     lvenable    Fixed empty list error in createSuiteMsgTypes().
 * Aug 21, 2014 #3490     lvenable    Added code from Richard to fix a hibernate issue for save/update.
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
     * Get a list of suites containing the names, types, and IDs.
     * 
     * @return List of suites.
     */
    public List<Suite> getSuiteNamesCatIds() {
        List<Object[]> objectList = getSuiteByQuery(Suite.GET_SUITE_NAMES_CATS_IDS);

        if (objectList == null) {
            return Collections.emptyList();
        }

        List<Suite> suiteList = createSuiteNamesCatIds(objectList);
        return suiteList;
    }

    /**
     * Get a list of suites and message types
     * 
     * @return List of suites.
     */
    public List<Suite> getSuiteMsgTypes() {
        List<Object[]> objectList = getSuiteByQuery(Suite.GET_SUITE_MSG_TYPES);

        if (objectList == null) {
            return Collections.emptyList();
        }

        List<Suite> suiteList = createSuiteMsgTypes(objectList);
        return suiteList;
    }

    /**
     * Get a list of objects associated with the query passed in.
     * 
     * @return A list of objects.
     */
    private List<Object[]> getSuiteByQuery(final String suiteQuery) {

        List<Object[]> objectList = txTemplate
                .execute(new TransactionCallback<List<Object[]>>() {
                    @Override
                    public List<Object[]> doInTransaction(
                            TransactionStatus status) {
                        HibernateTemplate ht = getHibernateTemplate();
                        return ht.findByNamedQuery(suiteQuery);
                    }
                });

        return objectList;
    }

    /**
     * Get a list of suites containing the name, type, and id.
     * 
     * @param objectList
     *            Object list.
     * @return List of suites.
     */
    private List<Suite> createSuiteNamesCatIds(List<Object[]> objectList) {
        List<Suite> suiteList = new ArrayList<Suite>(objectList.size());
        for (Object[] objArray : objectList) {
            Suite p = new Suite();
            p.setName((String) objArray[0]);
            p.setType((SuiteType) objArray[1]);
            p.setId((Integer) objArray[2]);
            suiteList.add(p);
        }
        return suiteList;
    }

    /**
     * Get a list of suites containing the name, type, and id and a list of
     * message types.
     * 
     * @param objectList
     *            Object list.
     * @return List of suites.
     */
    private List<Suite> createSuiteMsgTypes(List<Object[]> objectList) {

        Map<Integer, Suite> existingSuites = new TreeMap<Integer, Suite>();
        Suite suite = null;

        for (Object[] objArray : objectList) {

            int suiteId = (Integer) objArray[0];
            String suiteName = (String) objArray[1];
            SuiteType suiteType = (SuiteType) objArray[2];
            String msgTypeAfosId = (String) objArray[3];

            suite = existingSuites.get(suiteId);

            if (suite == null) {
                suite = new Suite();
                suite.setId(suiteId);
                suite.setName(suiteName);
                suite.setType(suiteType);
                existingSuites.put(suiteId, suite);
            }

            SuiteMessage sm = new SuiteMessage();
            MessageType mt = new MessageType();
            mt.setAfosid(msgTypeAfosId);
            sm.setMsgType(mt);
            suite.addSuiteMessage(sm);
        }

        List<Suite> suiteList = new ArrayList<Suite>(existingSuites.values());

        return suiteList;
    }

    /**
     * Overrides persist so that calls for Suite are properly handled.
     */
    @Override
    public void persist(final Object obj) {
        if (obj instanceof Suite) {
            saveOrUpdate((Suite) obj);
        } else {
            super.persist(obj);
        }
    }

    /**
     * Overrides persistAll so that calls for Suite are properly handled.
     */
    @Override
    public void persistAll(final Collection<? extends Object> objs) {
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            public void doInTransactionWithoutResult(TransactionStatus status) {
                HibernateTemplate ht = getHibernateTemplate();
                for (Object obj : objs) {
                    if (obj instanceof Suite) {
                        saveOrUpdateSuite(ht, (Suite) obj);
                    } else {
                        ht.saveOrUpdate(obj);
                    }
                }
            }
        });
    }

    /**
     * Handle saveOrUpdate specifically for a suite.
     * 
     * @param suite
     */
    public void saveOrUpdate(final Suite suite) {
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            public void doInTransactionWithoutResult(TransactionStatus status) {
                HibernateTemplate ht = getHibernateTemplate();
                saveOrUpdateSuite(ht, suite);
            }
        });
    }

    private void saveOrUpdateSuite(HibernateTemplate ht, final Suite suite) {
        // work around to orphanRemoval not working correctly in
        // bidirectional relationship
        if (suite.getId() != 0) {
            ht.bulkUpdate("delete from SuiteMessage where suite_id = ?",
                            suite.getId());
            ht.update(suite);
        } else {
            ht.save(suite);
        }

        if (suite.getSuiteMessages() != null) {
            for (SuiteMessage sm : suite.getSuiteMessages()) {
                ht.save(sm);
            }
        }
    }
}
