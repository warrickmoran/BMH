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
package com.raytheon.uf.edex.bmh.handler;

import java.util.List;

import com.raytheon.uf.common.bmh.datamodel.language.Word;
import com.raytheon.uf.common.bmh.request.WordRequest;
import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.edex.bmh.dao.WordDao;
import com.raytheon.uf.edex.database.query.DatabaseQuery;

/**
 * BMH Word related request handler.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 03, 2014   3355     mpduff      Initial creation
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class WordHandler implements IRequestHandler<WordRequest> {

    @Override
    public Object handleRequest(WordRequest request) throws Exception {
        WordDao dao = new WordDao();

        switch (request.getAction()) {
        case Query:
            DatabaseQuery query = new DatabaseQuery(Word.class);
            query.addOrder("word", true);
            List<?> wordReturnList = dao.queryByCriteria(query);
            request.setWordList((List<Word>) wordReturnList);
            request.setStatus(true);
            break;
        case Save:
            dao.saveOrUpdate(request.getWord());
            request.setStatus(true);
        default:
            break;

        }

        return request;
    }
}
