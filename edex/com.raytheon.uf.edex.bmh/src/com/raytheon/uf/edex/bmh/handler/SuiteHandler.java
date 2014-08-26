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

import com.raytheon.uf.common.bmh.datamodel.msg.Suite;
import com.raytheon.uf.common.bmh.notify.ConfigurationNotification;
import com.raytheon.uf.common.bmh.request.SuiteRequest;
import com.raytheon.uf.common.bmh.request.SuiteResponse;
import com.raytheon.uf.common.serialization.SerializationUtil;
import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.edex.bmh.dao.SuiteDao;
import com.raytheon.uf.edex.core.EDEXUtil;

/**
 * BMH Suite related request handler.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 5, 2014  #3490      lvenable     Initial creation
 * Aug 12, 2014 #3490     lvenable     Refactored to make a query convenience method.
 * Aug 12, 2014 #3490     lvenable     Added delete.
 * Aug 18, 2014 #3490     lvenable     Added save.
 * Sep 03, 2014  3554     bsteffen     Post notification of updates.
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */

public class SuiteHandler implements IRequestHandler<SuiteRequest> {

    @Override
    public Object handleRequest(SuiteRequest request) throws Exception {
        boolean update = false;
        SuiteResponse suiteResponse = new SuiteResponse();

        switch (request.getAction()) {
        case ListSuitesCats:
            suiteResponse = getSuitesNameCatIDs();
            break;
        case SuitesMsgTypes:
            suiteResponse = getSuitesMessageTypes();
            break;
        case AllSuites:
            suiteResponse = getSuites();
            break;
        case Delete:
            deleteSuite(request);
            update = true;
            break;
        case Save:
            suiteResponse = saveSuite(request);
            update = true;
            break;
        default:
            break;
        }

        if (update) {
            EDEXUtil.getMessageProducer()
                    .sendAsyncUri(
                            "jms-durable:topic:BMH.Config",
                            SerializationUtil
                                    .transformToThrift(new ConfigurationNotification()));
        }

        return suiteResponse;
    }

    /**
     * Get suites with name, type, and IDs.
     * 
     * @return List of suites with name, type, and IDs.
     */
    private SuiteResponse getSuitesNameCatIDs() {
        SuiteDao dao = new SuiteDao();
        SuiteResponse response = new SuiteResponse();

        List<Suite> suiteList = dao.getSuiteNamesCatIds();

        response.setSuiteList(suiteList);

        return response;
    }

    /**
     * Get Suite with message types.
     * 
     * @return List of suites with message types.
     */
    private SuiteResponse getSuitesMessageTypes() {
        SuiteDao dao = new SuiteDao();
        SuiteResponse response = new SuiteResponse();

        List<Suite> suiteList = dao.getSuiteMsgTypes();

        response.setSuiteList(suiteList);

        return response;
    }

    /**
     * Get a list of Suites that has everything populated.
     * 
     * @return List of populated Suites.
     */
    private SuiteResponse getSuites() {
        SuiteDao dao = new SuiteDao();
        SuiteResponse response = new SuiteResponse();

        List<Suite> suiteList = dao.getSuites();
        response.setSuiteList(suiteList);

        return response;
    }

    /**
     * Delete the specified suite.
     * 
     * @param suiteRequest
     *            Suite request.
     */
    private void deleteSuite(SuiteRequest suiteRequest) {
        SuiteDao dao = new SuiteDao();
        if (suiteRequest != null) {
            dao.delete(suiteRequest.getSuite());
        }
    }

    /**
     * Save suite.
     * 
     * @param request
     *            Suite request.
     * @return Suite response.
     */
    private SuiteResponse saveSuite(SuiteRequest request) {
        SuiteDao dao = new SuiteDao();
        SuiteResponse suiteResponse = new SuiteResponse();
        if (request.getSuite() != null) {
            dao.saveOrUpdate(request.getSuite());
            suiteResponse.addSuite(request.getSuite());
        }

        return suiteResponse;
    }
}
