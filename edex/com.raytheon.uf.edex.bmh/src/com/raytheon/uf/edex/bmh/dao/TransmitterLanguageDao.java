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
 * Apr 08, 2015  4248     bkowal      Override {@link #saveOrUpdate(Object)} to handle
 *                                    re-numbering static msg type positions.
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

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.edex.database.dao.CoreDao#saveOrUpdate(java.lang.Object)
     */
    @Override
    public void saveOrUpdate(final Object obj) {
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            public void doInTransactionWithoutResult(TransactionStatus status) {
                Session session = getCurrentSession();
                if (obj instanceof TransmitterLanguage) {
                    saveOrUpdateTransmitterLanguage(session,
                            (TransmitterLanguage) obj);
                } else {
                    session.saveOrUpdate(obj);
                }
            }
        });
    }

    private void saveOrUpdateTransmitterLanguage(Session session,
            TransmitterLanguage language) {
        session.createQuery(
                "delete from StaticMessageType where transmittergroup_id = ? and language = ?")
                .setParameter(0, language.getTransmitterGroup().getId())
                .setParameter(1, language.getLanguage().name()).executeUpdate();

        session.saveOrUpdate(language);
    }

    @SuppressWarnings("unchecked")
    public List<TransmitterLanguage> getLanguagesForTransmitterGroup(
            final TransmitterGroup group) {
        return (List<TransmitterLanguage>) findByNamedQueryAndNamedParam(
                TransmitterLanguage.GET_LANGUAGES_FOR_GROUP, "group", group);
    }
}