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
import java.util.List;

import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

import com.raytheon.uf.common.bmh.datamodel.language.Dictionary;

/**
 * BMH DAO for {@link Dictionary}.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 24, 2014 3302       bkowal      Initial creation
 * Jul 08, 2014 3355       mpduff      Added getDictionaryNames()
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class DictionaryDao extends AbstractBMHDao<Dictionary, String> {
    public DictionaryDao() {
        super(Dictionary.class);
    }

    /**
     * Get a list of all dictionaries in the BMH database.
     * 
     * @return dictList list of dictionaries
     */
    public List<String> getDictionaryNames() {
        List<String> names = txTemplate
                .execute(new TransactionCallback<List<String>>() {
                    @Override
                    public List<String> doInTransaction(TransactionStatus status) {
                        HibernateTemplate ht = getHibernateTemplate();
                        return ht
                                .findByNamedQuery(Dictionary.GET_DICTIONARY_NAMES_QUERY);
                    }
                });
        if (names == null) {
            names = Collections.emptyList();
        }

        return names;
    }
}