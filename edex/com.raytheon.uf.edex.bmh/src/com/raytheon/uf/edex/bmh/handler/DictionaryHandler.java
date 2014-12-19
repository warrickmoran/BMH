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

import com.raytheon.uf.common.bmh.BMHLoggerUtils;
import com.raytheon.uf.common.bmh.datamodel.language.Dictionary;
import com.raytheon.uf.common.bmh.notify.config.NationalDictionaryConfigNotification;
import com.raytheon.uf.common.bmh.notify.config.ConfigNotification.ConfigChangeType;
import com.raytheon.uf.common.bmh.request.DictionaryRequest;
import com.raytheon.uf.common.bmh.request.DictionaryResponse;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.edex.bmh.BmhMessageProducer;
import com.raytheon.uf.edex.bmh.dao.DictionaryDao;

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
 * Dec 11, 2014  3618     bkowal      Disseminate a {@link NationalDictionaryConfigNotification}
 *                                    whenever the National {@link Dictionary} is updated.
 * Dec 15, 2014  3618     bkowal      Include the {@link Language} in the
 *                                    {@link NationalDictionaryConfigNotification}.
 * Dec 16, 2014  3618     bkowal      Added {@link #getNationalDictionaryForLanguage(DictionaryRequest)} and
 *                                    {@link #getNonNationalDictionariesForLanguage(DictionaryRequest)}.
 * 
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
        NationalDictionaryConfigNotification notification = null;

        switch (request.getAction()) {
        case ListNames:
            response = getNames(request);
            break;
        case QueryByName:
            response = getDictionaryByName(request);
            break;
        case Save:
            response = saveDictionary(request);
            if (request.getDictionary().isNational()) {
                notification = new NationalDictionaryConfigNotification(
                        ConfigChangeType.Update);
                notification.setLanguage(request.getDictionary().getLanguage());
            }
            break;
        case Delete:
            if (request.getDictionary().isNational()) {
                notification = new NationalDictionaryConfigNotification(
                        ConfigChangeType.Delete);
                notification.setLanguage(request.getDictionary().getLanguage());
            }
            deleteDictionary(request);
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
        DictionaryResponse response = getDictionaryByName(request);
        Dictionary dictionary = response.getDictionary();
        if (dictionary != null) {
            DictionaryDao dao = new DictionaryDao(request.isOperational());
            dao.delete(response.getDictionary());

            IUFStatusHandler logger = BMHLoggerUtils.getSrvLogger(request);
            if (logger.isPriorityEnabled(Priority.INFO)) {
                String user = BMHLoggerUtils.getUser(request);
                BMHLoggerUtils.logDelete(request, user, dictionary);
            }
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