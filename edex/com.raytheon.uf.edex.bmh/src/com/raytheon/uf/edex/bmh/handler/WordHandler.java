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

import java.util.ArrayList;
import java.util.List;

import com.raytheon.uf.common.bmh.datamodel.language.Dictionary;
import com.raytheon.uf.common.bmh.datamodel.language.Word;
import com.raytheon.uf.common.bmh.request.WordRequest;
import com.raytheon.uf.common.bmh.request.WordResponse;
import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.edex.bmh.dao.WordDao;
import com.raytheon.uf.edex.database.DataAccessLayerException;
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
 * Jul 28, 2014   3407     mpduff      Added delete word
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class WordHandler implements IRequestHandler<WordRequest> {

    @Override
    public Object handleRequest(WordRequest request) throws Exception {
        switch (request.getAction()) {
        case Query:
            return queryWord(request);
        case Save:
            return saveWord(request);
        case Delete:
            deleteWord(request);
        default:
            break;

        }

        WordResponse response = new WordResponse();
        return response;
    }

    private WordResponse queryWord(WordRequest request)
            throws DataAccessLayerException {
        WordDao dao = new WordDao();
        WordResponse response = new WordResponse();
        DatabaseQuery query = new DatabaseQuery(Word.class);
        query.addOrder("word", true);
        List<?> wordReturnList = dao.queryByCriteria(query);
        response.setWordList((List<Word>) wordReturnList);

        return response;
    }

    private WordResponse saveWord(WordRequest request) {
        WordDao dao = new WordDao();
        WordResponse response = new WordResponse();
        Word word = request.getWord();
        if (word.getDictionary() == null) {
            Dictionary d = new Dictionary();
            d.setName(request.getDictionaryName());
            word.setDictionary(d);
        }
        dao.saveOrUpdate(request.getWord());
        List<Word> wordList = new ArrayList<Word>();
        wordList.add(request.getWord());
        response.setWordList(wordList);
        return response;
    }

    private void deleteWord(WordRequest request) {
        WordDao dao = new WordDao();
        Word word = request.getWord();
        if (word.getDictionary() == null) {
            Dictionary d = new Dictionary();
            d.setName(request.getDictionaryName());
            word.setDictionary(d);
        }

        dao.delete(word);
    }
}
