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
package com.raytheon.uf.edex.bmh.legacy.handler;

import com.raytheon.uf.common.bmh.datamodel.language.Language;
import com.raytheon.uf.common.bmh.legacy.ImportLegacyDictionaryRequest;
import com.raytheon.uf.common.bmh.legacy.ImportLegacyDictionaryResponse;
import com.raytheon.uf.edex.bmh.dao.DictionaryDao;
import com.raytheon.uf.edex.bmh.handler.AbstractBMHServerRequestHandler;
import com.raytheon.uf.edex.bmh.legacy.NeospeechCorrectWordsReader;

import java.util.Map;
import java.util.HashMap;

/**
 * Handles requests to retrieve information required to filter out legacy
 * dictionary words that will not need to be converted.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 9, 2015  5112       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class ImportLegacyDictionaryHandler extends
        AbstractBMHServerRequestHandler<ImportLegacyDictionaryRequest> {

    private final Map<Language, NeospeechCorrectWordsReader> correctWordsLanguageMap = new HashMap<>(
            Language.values().length, 1.0f);

    @Override
    public Object handleRequest(ImportLegacyDictionaryRequest request)
            throws Exception {

        ImportLegacyDictionaryResponse response = new ImportLegacyDictionaryResponse();
        /*
         * Retrieve the latest list of correct words.
         */
        synchronized (this.correctWordsLanguageMap) {
            if (correctWordsLanguageMap.containsKey(request.getLanguage()) == false) {
                correctWordsLanguageMap.put(request.getLanguage(),
                        new NeospeechCorrectWordsReader(request.getLanguage()));
            }
            response.setCorrectWords(correctWordsLanguageMap.get(
                    request.getLanguage()).getCorrectWords());
        }

        /*
         * Retrieve the latest national {@link Dictionary}.
         */
        final DictionaryDao dictionaryDao = new DictionaryDao(
                request.isOperational());
        response.setNationalDictionary(dictionaryDao
                .getNationalDictionaryForLanguage(request.getLanguage()));

        return response;
    }
}