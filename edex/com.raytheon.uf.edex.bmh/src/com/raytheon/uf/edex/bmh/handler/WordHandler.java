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
import java.util.Collections;
import java.util.List;

import com.raytheon.uf.common.bmh.BMHLoggerUtils;
import com.raytheon.uf.common.bmh.datamodel.language.Dictionary;
import com.raytheon.uf.common.bmh.datamodel.language.Word;
import com.raytheon.uf.common.bmh.notify.config.ConfigNotification.ConfigChangeType;
import com.raytheon.uf.common.bmh.notify.config.LanguageDictionaryConfigNotification;
import com.raytheon.uf.common.bmh.notify.config.TransmitterLanguageConfigNotification;
import com.raytheon.uf.common.bmh.request.WordRequest;
import com.raytheon.uf.common.bmh.request.WordResponse;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.edex.bmh.BmhMessageProducer;
import com.raytheon.uf.edex.bmh.dao.TransmitterLanguageDao;
import com.raytheon.uf.edex.bmh.dao.TtsVoiceDao;
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
 * Jan 05, 2015  3618     bkowal      Notify other components of dictionary
 *                                    modifications using a 
 *                                    {@link LanguageDictionaryConfigNotification}.
 * May 28, 2015  4429     rjpeter     Add ITraceable
 * Dec 03, 2015  5159     bkowal      Potentially trigger message re-generation even when a non-national
 *                                    {@link Dictionary} is altered.
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class WordHandler extends AbstractBMHServerRequestHandler<WordRequest> {

    @Override
    public Object handleRequest(WordRequest request) throws Exception {
        WordResponse response = new WordResponse();
        LanguageDictionaryConfigNotification notification = null;
        List<TransmitterLanguageConfigNotification> notifications = Collections
                .emptyList();
        final TransmitterLanguageDao transmitterLanguageDao = new TransmitterLanguageDao(
                request.isOperational());
        final TtsVoiceDao ttsVoiceDao = new TtsVoiceDao(request.isOperational());

        switch (request.getAction()) {
        case Query:
            response = queryWord(request);
            break;
        case Save:
            response = saveWord(request);
            if (request.getWord().getDictionary() != null
                    && (ttsVoiceDao.isVoiceDictionary(request.getWord()
                            .getDictionary()) || request.getWord()
                            .getDictionary().isNational())) {
                notification = new LanguageDictionaryConfigNotification(
                        ConfigChangeType.Update, request, request.getWord()
                                .getDictionary().isNational());
                notification.setLanguage(request.getWord().getDictionary()
                        .getLanguage());
                notification.setUpdatedWord(request.getWord().getWord());
            } else {
                /*
                 * Determine if the {@link Dictionary} is associated with a
                 * {@link TransmitterLanguage}.
                 */
                notifications = transmitterLanguageDao
                        .determineAffectedTransmitterLanguages(request
                                .getWord().getDictionary(),
                                ConfigChangeType.Update, request, request
                                        .getWord().getWord());
            }
            break;
        case Delete:
            if (request.getWord().getDictionary() != null
                    && (ttsVoiceDao.isVoiceDictionary(request.getWord()
                            .getDictionary()) || request.getWord()
                            .getDictionary().isNational())) {
                notification = new LanguageDictionaryConfigNotification(
                        ConfigChangeType.Delete, request, request.getWord()
                                .getDictionary().isNational());
                notification.setLanguage(request.getWord().getDictionary()
                        .getLanguage());
                /*
                 * If a word is removed from a {@link Dictionary}, any messages
                 * that contain the word should be updated because the
                 * substitution associated with the word should no longer be
                 * applied to the message.
                 */
                notification.setUpdatedWord(request.getWord().getWord());
            } else {
                /*
                 * Determine if the {@link Dictionary} is associated with a
                 * {@link TransmitterLanguage}.
                 */
                notifications = transmitterLanguageDao
                        .determineAffectedTransmitterLanguages(request
                                .getWord().getDictionary(),
                                ConfigChangeType.Update, request, request
                                        .getWord().getWord());
            }
            deleteWord(request);
            break;
        case Replace:
            response = replaceWord(request);
            if (request.getWord().getDictionary() != null
                    && (ttsVoiceDao.isVoiceDictionary(request.getWord()
                            .getDictionary()) || request.getWord()
                            .getDictionary().isNational())) {
                notification = new LanguageDictionaryConfigNotification(
                        ConfigChangeType.Update, request, request.getWord()
                                .getDictionary().isNational());
                notification.setLanguage(request.getWord().getDictionary()
                        .getLanguage());
            } else {
                /*
                 * Determine if the {@link Dictionary} is associated with a
                 * {@link TransmitterLanguage}.
                 */
                notifications = transmitterLanguageDao
                        .determineAffectedTransmitterLanguages(request
                                .getWord().getDictionary(),
                                ConfigChangeType.Update, request, request
                                        .getWord().getWord());
            }
            break;
        default:
            throw new UnsupportedOperationException(this.getClass()
                    .getSimpleName()
                    + " cannot handle action "
                    + request.getAction());
        }

        BmhMessageProducer.sendConfigMessage(notification,
                request.isOperational());
        if (notifications.isEmpty() == false) {
            for (TransmitterLanguageConfigNotification tlNotification : notifications) {
                BmhMessageProducer.sendConfigMessage(tlNotification,
                        request.isOperational());
            }
        }

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

        IUFStatusHandler logger = BMHLoggerUtils.getSrvLogger(request);
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
            BMHLoggerUtils.logSave(request, user, oldWord, newWord);
        }

        return response;
    }

    private WordResponse replaceWord(WordRequest request) {
        WordDao dao = new WordDao(request.isOperational());
        Word word = request.getWord();

        IUFStatusHandler logger = BMHLoggerUtils.getSrvLogger(request);
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
            BMHLoggerUtils.logSave(request, user, oldWord, word);
        }

        return response;
    }

    private void deleteWord(WordRequest request) {
        WordDao dao = new WordDao(request.isOperational());
        Word word = request.getWord();
        dao.delete(word);

        IUFStatusHandler logger = BMHLoggerUtils.getSrvLogger(request);
        if (logger.isPriorityEnabled(Priority.INFO)) {
            String user = BMHLoggerUtils.getUser(request);
            BMHLoggerUtils.logDelete(request, user, word);
        }
    }
}
