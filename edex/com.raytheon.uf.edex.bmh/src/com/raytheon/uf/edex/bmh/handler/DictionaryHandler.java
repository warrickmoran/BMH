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

import com.raytheon.uf.common.bmh.datamodel.language.Dictionary;
import com.raytheon.uf.common.bmh.request.DictionaryRequest;
import com.raytheon.uf.common.bmh.request.DictionaryResponse;
import com.raytheon.uf.common.serialization.comm.IRequestHandler;
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
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class DictionaryHandler implements IRequestHandler<DictionaryRequest> {

    @Override
    public Object handleRequest(DictionaryRequest request) throws Exception {
        DictionaryResponse response = new DictionaryResponse();

        switch (request.getAction()) {
        case ListNames:
            response = getNames(request);
            break;
        case QueryByName:
            response = getDictionaryByName(request);
            break;
        case Save:
            response = saveDictionary(request);
            break;
        case Delete:
            deleteDictionary(request);
            break;
        default:
            throw new UnsupportedOperationException(this.getClass()
                    .getSimpleName()
                    + " cannot handle action "
                    + request.getAction());
        }

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
        dao.saveOrUpdate(request.getDictionary());
        response.setDictionary(request.getDictionary());

        return response;
    }

    private void deleteDictionary(DictionaryRequest request) {
        DictionaryResponse response = getDictionaryByName(request);
        if (response.getDictionary() != null) {
            DictionaryDao dao = new DictionaryDao(request.isOperational());
            dao.delete(response.getDictionary());
        }
    }
}
