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

import com.raytheon.uf.common.bmh.BMHLoggerUtils;
import com.raytheon.uf.common.bmh.datamodel.language.Word;
import com.raytheon.uf.common.bmh.request.WordRequest;
import com.raytheon.uf.common.bmh.request.WordResponse;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.edex.bmh.dao.WordDao;
import com.raytheon.uf.edex.database.DataAccessLayerException;
import com.raytheon.uf.edex.database.query.DatabaseQuery;

/**
 * Handles any requests to get or modify the state of {@link Word}s
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Jul 03, 2014  3355     mpduff      Initial creation
 * Jul 28, 2014  3407     mpduff      Added delete word
 * Aug 05, 2014  3175     rjpeter     Added replace word.
 * Sep 10, 2014  3407     mpduff      Added break statement to delete word
 * Oct 07, 2014  3687     bsteffen    Handle non-operational requests.
 * Oct 13, 2014  3413     rferrel     Implement User roles.
 * Oct 16, 2014  3636     rferrel     Implement logging.
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class WordHandler extends AbstractBMHServerRequestHandler<WordRequest> {

    @Override
    public Object handleRequest(WordRequest request) throws Exception {
        switch (request.getAction()) {
        case Query:
            return queryWord(request);
        case Save:
            return saveWord(request);
        case Delete:
            deleteWord(request);
            break;
        case Replace:
            return replaceWord(request);
        default:
            throw new UnsupportedOperationException(this.getClass()
                    .getSimpleName()
                    + " cannot handle action "
                    + request.getAction());
        }

        WordResponse response = new WordResponse();
        return response;
    }

    @SuppressWarnings("unchecked")
    private WordResponse queryWord(WordRequest request)
            throws DataAccessLayerException {
        WordDao dao = new WordDao(request.isOperational());
        WordResponse response = new WordResponse();
        DatabaseQuery query = new DatabaseQuery(Word.class);
        query.addOrder("word", true);
        List<?> wordReturnList = dao.queryByCriteria(query);
        response.setWordList((List<Word>) wordReturnList);

        return response;
    }

    private WordResponse saveWord(WordRequest request) {
        WordDao dao = new WordDao(request.isOperational());
        WordResponse response = new WordResponse();
        Word newWord = request.getWord();

        IUFStatusHandler logger = BMHLoggerUtils.getSrvLogger(request
                .isOperational());
        Word oldWord = null;
        if (logger.isPriorityEnabled(Priority.INFO)) {
            oldWord = dao.getByID(newWord.getId());
        }

        dao.saveOrUpdate(newWord);
        List<Word> wordList = new ArrayList<Word>(1);
        wordList.add(newWord);
        response.setWordList(wordList);

        if (logger.isPriorityEnabled(Priority.INFO)) {
            String user = BMHLoggerUtils.getUser(request);
            logWordChange(user, oldWord, newWord, logger);
        }

        return response;
    }

    private WordResponse replaceWord(WordRequest request) {
        WordDao dao = new WordDao(request.isOperational());
        Word word = request.getWord();

        IUFStatusHandler logger = BMHLoggerUtils.getSrvLogger(request
                .isOperational());
        Word oldWord = null;
        if (logger.isPriorityEnabled(Priority.INFO)) {
            oldWord = dao.getByID(word.getId());
        }

        dao.replaceWord(word);
        WordResponse response = new WordResponse();
        List<Word> wordList = new ArrayList<Word>(1);
        wordList.add(word);
        response.setWordList(wordList);

        if (logger.isPriorityEnabled(Priority.INFO)) {
            String user = BMHLoggerUtils.getUser(request);
            logWordChange(user, oldWord, word, logger);
        }

        return response;
    }

    private void deleteWord(WordRequest request) {
        WordDao dao = new WordDao(request.isOperational());
        Word word = request.getWord();
        dao.delete(word);

        IUFStatusHandler logger = BMHLoggerUtils.getSrvLogger(request
                .isOperational());
        if (logger.isPriorityEnabled(Priority.INFO)) {
            String user = BMHLoggerUtils.getUser(request);
            logger.info("User " + user + " Deleted Word " + word.toString());
        }
    }

    private void logWordChange(String user, Word oldWord, Word newWord,
            IUFStatusHandler logger) {
        String entry = newWord.logEntry(oldWord, user);
        if (entry.length() > 0) {
            logger.info(entry);
        }
    }
}
