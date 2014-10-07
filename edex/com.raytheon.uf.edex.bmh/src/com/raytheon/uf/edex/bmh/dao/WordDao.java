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

import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;

import com.raytheon.uf.common.bmh.datamodel.language.Word;

/**
 * BMH DAO for {@link Word}.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Jul 03, 2014  3355     mpduff      Initial creation.
 * Aug 05, 2014  3175     rjpeter     Added replaceWord.
 * Oct 06, 2014  3687     bsteffen    Add operational flag to constructor.
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class WordDao extends AbstractBMHDao<Word, String> {
    protected static final String DELETE_BY_WORD_DICT_QUERY = "delete from Word w where w.word = :word and w.dictionary = :dict";

    public WordDao() {
        super(Word.class);
    }

    public WordDao(boolean operational) {
        super(operational, Word.class);
    }

    public void replaceWord(final Word word) {
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            public void doInTransactionWithoutResult(TransactionStatus status) {
                HibernateTemplate ht = getHibernateTemplate();
                // delete can't be in a named query, use direct hql
                ht.bulkUpdate(
                        "delete from Word w where w.word = ? and w.dictionary = ?",
                        word.getWord(), word.getDictionary());
                saveOrUpdate(word);
            }
        });
    }
}
