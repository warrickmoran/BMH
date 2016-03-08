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

import java.util.Collections;
import java.util.List;

import com.raytheon.uf.common.bmh.BMHLoggerUtils;
import com.raytheon.uf.common.bmh.datamodel.language.Dictionary;
import com.raytheon.uf.common.bmh.notify.config.ConfigNotification.ConfigChangeType;
import com.raytheon.uf.common.bmh.notify.config.LanguageDictionaryConfigNotification;
import com.raytheon.uf.common.bmh.notify.config.TransmitterLanguageConfigNotification;
import com.raytheon.uf.common.bmh.request.DictionaryRequest;
import com.raytheon.uf.common.bmh.request.DictionaryResponse;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.edex.bmh.BmhMessageProducer;
import com.raytheon.uf.edex.bmh.dao.DictionaryDao;
import com.raytheon.uf.edex.bmh.dao.TransmitterLanguageDao;
import com.raytheon.uf.edex.bmh.dao.TtsVoiceDao;

/**
 * Handles any requests to get or modify the state of {@link Dictionary}s
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Jul 02, 2014  3355     mpduff      Initial creation
 * Jul 21, 2014  3407     mpduff      Added delete dictionary action
 * Oct 07, 2014  3687     bsteffen    Handle non-operational requests.
 * Oct 13, 2014  3413     rferrel     Implement User roles.
 * Oct 16, 2014  3636     rferrel     Implement logging.
 * Dec 11, 2014  3618     bkowal      Disseminate a {@link LanguageDictionaryConfigNotification}
 *                                    whenever the National {@link Dictionary} is updated.
 * Dec 15, 2014  3618     bkowal      Include the {@link Language} in the
 *                                    {@link LanguageDictionaryConfigNotification}.
 * Dec 16, 2014  3618     bkowal      Added {@link #getNationalDictionaryForLanguage(DictionaryRequest)} and
 *                                    {@link #getNonNationalDictionariesForLanguage(DictionaryRequest)}.
 * Jan 05, 2015  3618     bkowal      The {@link Dictionary} is now specified for delete operations.
 * May 28, 2015  4429     rjpeter     Add ITraceable
 * Dec 03, 2015  5159     bkowal      Potentially trigger message re-generation even when a non-national
 *                                    {@link Dictionary} is altered.
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class DictionaryHandler extends
        AbstractBMHServerRequestHandler<DictionaryRequest> {

    @Override
    public Object handleRequest(DictionaryRequest request) throws Exception {
        DictionaryResponse response = new DictionaryResponse();
        LanguageDictionaryConfigNotification notification = null;

        final TransmitterLanguageDao transmitterLanguageDao = new TransmitterLanguageDao(
                request.isOperational());
        final TtsVoiceDao ttsVoiceDao = new TtsVoiceDao(request.isOperational());

        switch (request.getAction()) {
        case ListNames:
            response = getNames(request);
            break;
        case QueryByName:
            response = getDictionaryByName(request);
            break;
        case Save:
            response = saveDictionary(request);
            if (ttsVoiceDao.isVoiceDictionary(request.getDictionary())
                    || request.getDictionary().isNational()) {
                notification = new LanguageDictionaryConfigNotification(
                        ConfigChangeType.Update, request, request
                                .getDictionary().isNational());
                notification.setLanguage(request.getDictionary().getLanguage());
                notification.setUpdatedWords(request.getDictionary()
                        .getAllWordsAsStrings());
            } else {
                /*
                 * Determine if the {@link Dictionary} is associated with a
                 * {@link TransmitterLanguage}.
                 */
                List<TransmitterLanguageConfigNotification> notifications = transmitterLanguageDao
                        .determineAffectedTransmitterLanguages(
                                request.getDictionary(),
                                ConfigChangeType.Update, request, null);
                if (notifications.isEmpty() == false) {
                    for (TransmitterLanguageConfigNotification tlNotification : notifications) {
                        BmhMessageProducer.sendConfigMessage(tlNotification,
                                request.isOperational());
                    }
                }
            }
            break;
        case Delete:
            List<TransmitterLanguageConfigNotification> notifications = Collections
                    .emptyList();
            if (ttsVoiceDao.isVoiceDictionary(request.getDictionary())
                    || request.getDictionary().isNational()) {
                notification = new LanguageDictionaryConfigNotification(
                        ConfigChangeType.Delete, request, request
                                .getDictionary().isNational());
                notification.setLanguage(request.getDictionary().getLanguage());
                notification.setUpdatedWords(request.getDictionary()
                        .getAllWordsAsStrings());
            } else {
                /*
                 * Determine if the {@link Dictionary} is associated with a
                 * {@link TransmitterLanguage}. Even though the {@link
                 * Dictionary} has been removed, {@link ConfigChangeType#Update}
                 * is still used in this case because the {@link
                 * TransmitterLanguage}s still remain, so we want to ensure that
                 * the messages are only generated without the associated {@link
                 * Dictionary} substitutions instead of being removed all
                 * together.
                 */
                notifications = transmitterLanguageDao
                        .determineAffectedTransmitterLanguages(
                                request.getDictionary(),
                                ConfigChangeType.Update, request, null);
            }
            deleteDictionary(request);
            if (notifications.isEmpty() == false) {
                for (TransmitterLanguageConfigNotification tlNotification : notifications) {
                    BmhMessageProducer.sendConfigMessage(tlNotification,
                            request.isOperational());
                }
            }
            break;
        case GetNationalForLanguage:
            response = this.getNationalDictionaryForLanguage(request);
            break;
        case GetNonNationalForLanguage:
            response = this.getNonNationalDictionariesForLanguage(request);
            break;
        default:
            throw new UnsupportedOperationException(this.getClass()
                    .getSimpleName()
                    + " cannot handle action "
                    + request.getAction());
        }

        BmhMessageProducer.sendConfigMessage(notification,
                request.isOperational());

        return response;
    }

    private DictionaryResponse getNames(DictionaryRequest request) {
        DictionaryDao dao = new DictionaryDao(request.isOperational());
        List<String> names = dao.getDictionaryNames();
        DictionaryResponse response = new DictionaryResponse();
        response.setDictionaryNames(names);

        return response;
    }

    private DictionaryResponse getDictionaryByName(DictionaryRequest request) {
        DictionaryDao dao = new DictionaryDao(request.isOperational());
        Dictionary dictionary = dao.getByID(request.getDictionaryName());
        DictionaryResponse response = new DictionaryResponse();
        response.setDictionary(dictionary);

        return response;
    }

    private DictionaryResponse saveDictionary(DictionaryRequest request) {
        DictionaryResponse response = new DictionaryResponse();
        DictionaryDao dao = new DictionaryDao(request.isOperational());
        Dictionary newDic = request.getDictionary();

        IUFStatusHandler logger = BMHLoggerUtils.getSrvLogger(request);
        Dictionary oldDic = null;
        if (logger.isPriorityEnabled(Priority.INFO)) {
            oldDic = dao.getByID(newDic.getName());
        }

        dao.saveOrUpdate(request.getDictionary());

        if (logger.isPriorityEnabled(Priority.INFO)) {
            String user = BMHLoggerUtils.getUser(request);
            BMHLoggerUtils.logSave(request, user, oldDic, newDic);
        }
        response.setDictionary(request.getDictionary());

        return response;
    }

    private void deleteDictionary(DictionaryRequest request) {
        DictionaryDao dao = new DictionaryDao(request.isOperational());
        dao.delete(request.getDictionary());

        IUFStatusHandler logger = BMHLoggerUtils.getSrvLogger(request);
        if (logger.isPriorityEnabled(Priority.INFO)) {
            String user = BMHLoggerUtils.getUser(request);
            BMHLoggerUtils.logDelete(request, user, request.getDictionary());
        }
    }

    private DictionaryResponse getNationalDictionaryForLanguage(
            DictionaryRequest request) {
        DictionaryDao dao = new DictionaryDao(request.isOperational());
        DictionaryResponse response = new DictionaryResponse();
        response.setDictionary(dao.getNationalDictionaryForLanguage(request
                .getLanguage()));

        return response;
    }

    private DictionaryResponse getNonNationalDictionariesForLanguage(
            DictionaryRequest request) {
        DictionaryDao dao = new DictionaryDao(request.isOperational());
        DictionaryResponse response = new DictionaryResponse();
        response.setDictionaries(dao
                .getNonNationalDictionariesForLanguage(request.getLanguage()));

        return response;
    }
}