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

import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterLanguage;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterLanguagePK;

/**
 * BMH DAO for {@link TransmitterLanguage}.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#    Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Jul 28, 2014  3175     rjpeter     Initial creation
 * Aug 29, 2014  3568     bkowal      Added getLanguagesForTransmitterGroup
 * Oct 06, 2014  3687     bsteffen    Add operational flag to constructor.
 * 
 * </pre>
 * 
 * @author rjpeter
 * @version 1.0
 */
public class TransmitterLanguageDao extends
        AbstractBMHDao<TransmitterLanguage, TransmitterLanguagePK> {
    public TransmitterLanguageDao() {
        super(TransmitterLanguage.class);
    }

    public TransmitterLanguageDao(boolean operational) {
        super(operational, TransmitterLanguage.class);
    }

    public List<TransmitterLanguage> getLanguagesForTransmitterGroup(
            final TransmitterGroup group) {
        List<TransmitterLanguage> languages = txTemplate
                .execute(new TransactionCallback<List<TransmitterLanguage>>() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public List<TransmitterLanguage> doInTransaction(
                            TransactionStatus status) {
                        HibernateTemplate ht = getHibernateTemplate();

                        return ht.findByNamedQueryAndNamedParam(
                                TransmitterLanguage.GET_LANGUAGES_FOR_GROUP,
                                "group", group);
                    }
                });

        return languages;
    }
}