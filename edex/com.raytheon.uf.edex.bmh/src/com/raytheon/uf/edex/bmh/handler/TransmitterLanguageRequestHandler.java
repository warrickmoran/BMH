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
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterLanguage;
import com.raytheon.uf.common.bmh.notify.config.ConfigNotification.ConfigChangeType;
import com.raytheon.uf.common.bmh.notify.config.TransmitterLanguageConfigNotification;
import com.raytheon.uf.common.bmh.request.TransmitterLanguageRequest;
import com.raytheon.uf.common.bmh.request.TransmitterLanguageResponse;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.edex.bmh.BmhMessageProducer;
import com.raytheon.uf.edex.bmh.dao.TransmitterLanguageDao;

/**
 * Thrift handler for {@link TransmitterLanguage} objects.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Aug 29, 2014  3568     bkowal     Initial creation
 * Oct 07, 2014  3687     bsteffen    Handle non-operational requests.
 * Oct 13, 2014  3413     rferrel     Implement User roles.
 * Oct 24, 2014  3636     rferrel     Implement logging.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class TransmitterLanguageRequestHandler extends
        AbstractBMHServerRequestHandler<TransmitterLanguageRequest> {

    @Override
    public Object handleRequest(TransmitterLanguageRequest request)
            throws Exception {
        TransmitterLanguageResponse response = null;
        TransmitterLanguageConfigNotification notification = null;
        switch (request.getAction()) {
        case GetTransmitterLanguagesForTransmitterGrp:
            response = this.getTransmitterLanguagesForTransmitterGroup(request);
            break;
        case UpdateTransmitterLanguage:
            /*
             * Ideally the dialog will ensure that users cannot save a
             * transmitter language that has not been modified.
             */
            response = this.updateTransmitterLanguage(request);
            notification = new TransmitterLanguageConfigNotification(
                    ConfigChangeType.Update, request.getTransmitterLanguage());
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

    private TransmitterLanguageResponse getTransmitterLanguagesForTransmitterGroup(
            TransmitterLanguageRequest request) throws Exception {
        if (request.getTransmitterGroup() == null) {
            throw new IllegalArgumentException(
                    "Transmitter group cannot be NULL when the requested action is: "
                            + request.getAction().toString() + "!");
        }
        TransmitterLanguageDao transmitterLanguageDao = new TransmitterLanguageDao(
                request.isOperational());
        List<TransmitterLanguage> transmitterLanguages = transmitterLanguageDao
                .getLanguagesForTransmitterGroup(request.getTransmitterGroup());

        TransmitterLanguageResponse response = new TransmitterLanguageResponse();
        response.setTransmitterLanguages(transmitterLanguages);

        return response;
    }

    private TransmitterLanguageResponse updateTransmitterLanguage(
            TransmitterLanguageRequest request) throws Exception {
        if (request.getTransmitterLanguage() == null) {
            throw new IllegalArgumentException(
                    "Transmitter language cannot be NULL when the requested action is: "
                            + request.getAction().toString() + "!");
        }
        TransmitterLanguageDao transmitterLanguageDao = new TransmitterLanguageDao(
                request.isOperational());
        TransmitterLanguage transmitterLanguage = request
                .getTransmitterLanguage();

        IUFStatusHandler logger = BMHLoggerUtils.getSrvLogger(request);

        TransmitterLanguage tl = request.getTransmitterLanguage();
        TransmitterLanguage oldTl = null;
        if (logger.isPriorityEnabled(Priority.INFO)) {
            oldTl = transmitterLanguageDao.getByID(tl.getId());
        }
        transmitterLanguageDao.saveOrUpdate(tl);

        TransmitterLanguageResponse response = new TransmitterLanguageResponse();
        response.addTransmitterLanguage(transmitterLanguage);

        if (logger.isPriorityEnabled(Priority.INFO)) {
            String user = BMHLoggerUtils.getUser(request);
            BMHLoggerUtils.logSave(request, user, oldTl, tl);
        }

        return response;
    }
}