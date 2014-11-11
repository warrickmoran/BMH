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
import java.util.ArrayList;

import com.raytheon.uf.common.bmh.datamodel.transmitter.LdadConfig;
import com.raytheon.uf.common.bmh.request.LdadConfigRequest;
import com.raytheon.uf.common.bmh.request.LdadConfigResponse;
import com.raytheon.uf.edex.bmh.dao.LdadConfigDao;

/**
 * Used to retrieve, update, and delete {@link LdadConfig} records as specified
 * in the {@link LdadConfigRequest}.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 11, 2014 3803       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class LdadConfigHandler extends
        AbstractBMHServerRequestHandler<LdadConfigRequest> {

    @Override
    public Object handleRequest(LdadConfigRequest request) throws Exception {
        LdadConfigResponse response = new LdadConfigResponse();

        switch (request.getAction()) {
        case Delete:
            this.handleDeleteRecord(response, request);
            break;
        case RetrieveRecord:
            this.handleRetrieveRecord(response, request);
            break;
        case RetrieveReferences:
            this.handleRetrieveReferences(response, request);
            break;
        case Save:
            this.handleSaveRecord(response, request);
            break;
        }

        return response;
    }

    private void handleRetrieveReferences(LdadConfigResponse response,
            final LdadConfigRequest request) throws Exception {
        LdadConfigDao dao = new LdadConfigDao(request.isOperational());
        response.setLdadConfigurations(dao.selectConfigReferences());
    }

    private void handleRetrieveRecord(LdadConfigResponse response,
            final LdadConfigRequest request) throws Exception {
        LdadConfigDao dao = new LdadConfigDao(request.isOperational());
        response.setLdadConfigurations(this.wrapSingleRecordList(dao
                .getByID(request.getId())));
    }

    private void handleSaveRecord(LdadConfigResponse response,
            final LdadConfigRequest request) throws Exception {
        LdadConfigDao dao = new LdadConfigDao(request.isOperational());
        LdadConfig ldadConfig = request.getLdadConfig();
        dao.saveOrUpdate(ldadConfig);
        response.setLdadConfigurations(this.wrapSingleRecordList(ldadConfig));
    }

    private void handleDeleteRecord(LdadConfigResponse response,
            final LdadConfigRequest request) throws Exception {
        LdadConfigDao dao = new LdadConfigDao(request.isOperational());
        dao.delete(request.getLdadConfig());
    }

    private List<LdadConfig> wrapSingleRecordList(LdadConfig ldadConfig) {
        List<LdadConfig> configRecords = new ArrayList<>(1);
        configRecords.add(ldadConfig);

        return configRecords;
    }
}