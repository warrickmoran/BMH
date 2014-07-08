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

import org.springframework.dao.DataAccessException;

import com.raytheon.uf.common.bmh.datamodel.language.Dictionary;
import com.raytheon.uf.common.bmh.request.DictionaryRequest;
import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.edex.bmh.dao.DictionaryDao;
import com.raytheon.uf.edex.database.query.DatabaseQuery;

/**
 * BMH Dictionary related request handler.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 02, 2014   3355     mpduff      Initial creation
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class DictionaryHandler implements IRequestHandler<DictionaryRequest> {
    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(DictionaryHandler.class);

    @Override
    public Object handleRequest(DictionaryRequest request) throws Exception {
        DictionaryDao dao = new DictionaryDao();

        switch (request.getAction()) {
        case ListNames:
            List<String> names = dao.getDictionaryNames();
            request.setDictionaryNames(names);
            request.setStatus(true);
            break;
        case QueryByName:
            DatabaseQuery query = new DatabaseQuery(Dictionary.class);
            query.addQueryParam("name", request.getQueryName());
            List<?> dictReturnList = dao.queryByCriteria(query);
            if (dictReturnList != null) {
                request.setDictionary((Dictionary) dictReturnList.get(0));
                request.setStatus(true);
            }
            break;
        case Save:
            try {
                dao.saveOrUpdate(request.getDictionary());
                request.setStatus(true);
            } catch (DataAccessException e) {
                statusHandler.error("Error saving dictionary", e);
                request.setStatus(false);
            }
            break;
        default:
            break;

        }

        return request;
    }
}
