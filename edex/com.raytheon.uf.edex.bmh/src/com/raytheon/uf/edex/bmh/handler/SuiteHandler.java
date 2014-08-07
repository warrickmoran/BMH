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
import com.raytheon.uf.common.bmh.request.SuiteRequest;
import com.raytheon.uf.common.bmh.request.SuiteResponse;
import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.edex.bmh.dao.SuiteDao;

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
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */

public class SuiteHandler implements IRequestHandler<SuiteRequest> {

    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(ProgramHandler.class);

    @Override
    public Object handleRequest(SuiteRequest request) throws Exception {

        SuiteResponse suiteResponse = new SuiteResponse();

        switch (request.getAction()) {
        case ListSuitesCats:
            suiteResponse = getNamesCategories();
            break;
        case AllSuites:
            suiteResponse = getSuites();
            break;
        default:
            break;
        }

        return suiteResponse;
    }

    /**
     * Get a list of suite with the name and type populated.
     * 
     * @return Suite response.
     */
    private SuiteResponse getNamesCategories() {
        SuiteDao dao = new SuiteDao();
        SuiteResponse response = new SuiteResponse();

        List<Suite> suiteList = dao.getSuiteNameCategories();

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
}
