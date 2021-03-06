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
import com.raytheon.uf.common.bmh.datamodel.msg.Suite;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.notify.config.ConfigNotification.ConfigChangeType;
import com.raytheon.uf.common.bmh.notify.config.AbstractTraceableSystemConfigNotification;
import com.raytheon.uf.common.bmh.notify.config.SuiteConfigNotification;
import com.raytheon.uf.common.bmh.request.SuiteRequest;
import com.raytheon.uf.common.bmh.request.SuiteResponse;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.edex.bmh.BmhMessageProducer;
import com.raytheon.uf.edex.bmh.dao.ProgramDao;
import com.raytheon.uf.edex.bmh.dao.SuiteDao;

/**
 * Handles any requests to get or modify the state of {@link Suite}s
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Aug 05, 2014  3490     lvenable    Initial creation
 * Aug 12, 2014  3490     lvenable    Refactored to make a query convenience method.
 * Aug 12, 2014  3490     lvenable    Added delete.
 * Aug 18, 2014  3490     lvenable    Added save.
 * Sep 03, 2014  3554     bsteffen    Post notification of updates.
 * Sep 05, 2014  3554     bsteffen    Send more specific config change notification.
 * Oct 07, 2014  3687     bsteffen    Handle non-operational requests.
 * Oct 13, 2014  3413     rferrel     Implement User roles.
 * Oct 21, 2014  3715     bkowal      Updates due to hibernate upgrade.
 * Oct 29, 2014  3636     rferrel     Implement logging,
 * Dec 07, 2014  3752     mpduff      Add getSuiteByName
 * Mar 25, 2015  4213     bkowal      Added affected Transmitter Group(s) to the 
 *                                    {@link SuiteConfigNotification} for deletes.
 * Apr 22, 2015  4397     bkowal      Construct a {@link AbstractTraceableSystemConfigNotification}
 *                                    notification when database changes occur.
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */

public class SuiteHandler extends AbstractBMHServerRequestHandler<SuiteRequest> {

    @Override
    public Object handleRequest(SuiteRequest request) throws Exception {
        SuiteConfigNotification notification = null;
        SuiteResponse suiteResponse = new SuiteResponse();

        switch (request.getAction()) {
        case ListSuitesCats:
            suiteResponse = getSuitesNameCatIDs(request);
            break;
        case SuitesMsgTypes:
            suiteResponse = getSuitesMessageTypes(request);
            break;
        case AllSuites:
            suiteResponse = getSuites(request);
            break;
        case Delete:
            notification = new SuiteConfigNotification(ConfigChangeType.Delete,
                    request, request.getSuite(),
                    this.getAffectedEnabledTransmitterGroups(request));
            deleteSuite(request);
            break;
        case Save:
            suiteResponse = saveSuite(request);
            notification = new SuiteConfigNotification(ConfigChangeType.Update,
                    request, request.getSuite());
            break;
        case GetSuiteByName:
            suiteResponse = getSuiteByName(request);
            break;
        default:
            throw new UnsupportedOperationException(this.getClass()
                    .getSimpleName()
                    + " cannot handle action "
                    + request.getAction());
        }

        BmhMessageProducer.sendConfigMessage(notification,
                request.isOperational());

        return suiteResponse;
    }

    private List<TransmitterGroup> getAffectedEnabledTransmitterGroups(
            SuiteRequest request) throws Exception {
        ProgramDao programDao = new ProgramDao(request.isOperational());
        return programDao.getSuiteEnabledGroups(request.getSuite().getId());
    }

    /**
     * Get suites with name, type, and IDs.
     * 
     * @return List of suites with name, type, and IDs.
     */
    private SuiteResponse getSuitesNameCatIDs(SuiteRequest request) {
        SuiteDao dao = new SuiteDao(request.isOperational());
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
    private SuiteResponse getSuitesMessageTypes(SuiteRequest request) {
        SuiteDao dao = new SuiteDao(request.isOperational());
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
    private SuiteResponse getSuites(SuiteRequest request) {
        SuiteDao dao = new SuiteDao(request.isOperational());
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
    private void deleteSuite(SuiteRequest request) {
        SuiteDao dao = new SuiteDao(request.isOperational());
        Suite suite = request.getSuite();
        dao.delete(suite);

        IUFStatusHandler logger = BMHLoggerUtils.getSrvLogger(request);
        if (logger.isPriorityEnabled(Priority.INFO)) {
            String user = BMHLoggerUtils.getUser(request);
            BMHLoggerUtils.logDelete(request, user, suite);
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
        SuiteDao dao = new SuiteDao(request.isOperational());
        SuiteResponse suiteResponse = new SuiteResponse();
        Suite suite = request.getSuite();
        if (suite != null) {
            IUFStatusHandler logger = BMHLoggerUtils.getSrvLogger(request);
            Suite oldSuite = null;
            if (logger.isPriorityEnabled(Priority.INFO)) {
                oldSuite = dao.getByID(suite.getId());
            }
            dao.persist(request.getSuite());
            suiteResponse.addSuite(request.getSuite());
            if (logger.isPriorityEnabled(Priority.INFO)) {
                String user = BMHLoggerUtils.getUser(request);
                BMHLoggerUtils.logSave(request, user, oldSuite, suite);
            }
        }

        return suiteResponse;
    }

    private SuiteResponse getSuiteByName(SuiteRequest request) {
        SuiteDao dao = new SuiteDao(request.isOperational());
        SuiteResponse suiteResponse = new SuiteResponse();
        Suite suite = dao.getSuiteByName(request.getSuiteName());
        suiteResponse.addSuite(suite);

        return suiteResponse;
    }
}
