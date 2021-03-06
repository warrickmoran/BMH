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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.util.CollectionUtils;

import com.raytheon.uf.common.bmh.BMHLoggerUtils;
import com.raytheon.uf.common.bmh.datamodel.language.Dictionary;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType.Designation;
import com.raytheon.uf.common.bmh.datamodel.transmitter.StaticMessageType;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterLanguage;
import com.raytheon.uf.common.bmh.notify.config.ConfigNotification;
import com.raytheon.uf.common.bmh.notify.config.ConfigNotification.ConfigChangeType;
import com.raytheon.uf.common.bmh.notify.config.StaticMsgTypeConfigNotification;
import com.raytheon.uf.common.bmh.notify.config.TransmitterLanguageConfigNotification;
import com.raytheon.uf.common.bmh.request.StaticMsgValidationResult;
import com.raytheon.uf.common.bmh.request.TransmitterLanguageRequest;
import com.raytheon.uf.common.bmh.request.TransmitterLanguageResponse;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.edex.bmh.BmhMessageProducer;
import com.raytheon.uf.edex.bmh.dao.StaticMessageTypeDao;
import com.raytheon.uf.edex.bmh.dao.TransmitterLanguageDao;
import com.raytheon.uf.edex.bmh.msg.validator.UnacceptableWordFilter;

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
 * Jan 13, 2015  3809     bkowal      Fixed {@link #updateTransmitterLanguage(TransmitterLanguageRequest)}.
 * Jan 19, 2015  4011     bkowal      Added {@link #deleteTransmitterLanguage(TransmitterLanguageRequest)}.
 * Feb 09, 2015  4096     bsteffen    Filter Unacceptable Words.
 * Mar 13, 2015  4213     bkowal      Added support for saving and deleting {@link StaticMessageType}s.
 * Apr 28, 2015  4248     bkowal      Added {@link #validateStaticMessageType(TransmitterLanguageRequest)}.
 * May 11, 2015  4476     bkowal      Removed deprecated methods.
 * May 22, 2015  4481     bkowal      Added {@link #getStaticMsgTypeForTransmitterGroupAndMessageType(TransmitterLanguageRequest)}.
 * May 28, 2015  4429     rjpeter     Add ITraceable
 * Jul 06, 2015  4603     bkowal      Return a {@link StaticMsgValidationResult} when validating static message text.
 * Dec 03, 2015  5159     bkowal      Potentially trigger message re-generation even when a non-national
 *                                    {@link Dictionary} is altered.
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
        ConfigNotification notification = null;

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
                    ConfigChangeType.Update, request.getTransmitterLanguage(),
                    request);
            Set<String> updatedWords = new HashSet<>();
            if (request.getTransmitterLanguage().getDictionary() != null) {
                updatedWords.addAll(request.getTransmitterLanguage()
                        .getDictionary().getAllWordsAsStrings());
            }
            if (request.getTransmitterLanguage().getRemovedDictionary() != null) {
                updatedWords.addAll(request.getTransmitterLanguage()
                        .getRemovedDictionary().getAllWordsAsStrings());
            }
            if (updatedWords.isEmpty() == false) {
                ((TransmitterLanguageConfigNotification) notification)
                        .setUpdatedWords(new ArrayList<>(updatedWords));
            }
            break;
        case DeleteTransmitterLanguage:
            response = new TransmitterLanguageResponse();
            notification = new TransmitterLanguageConfigNotification(
                    ConfigChangeType.Delete, request.getTransmitterLanguage(),
                    request);
            this.deleteTransmitterLanguage(request);
            break;
        case ValidateStaticMsgType:
            return this.validateStaticMessageType(request);
        case GetStaticMsgTypeForTransmitterGrpAndAfosId:
            response = this
                    .getStaticMsgTypeForTransmitterGroupAndMessageType(request);
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

        IUFStatusHandler logger = BMHLoggerUtils.getSrvLogger(request);

        TransmitterLanguage tl = request.getTransmitterLanguage();
        TransmitterLanguage oldTl = null;
        if (logger.isPriorityEnabled(Priority.INFO)) {
            oldTl = transmitterLanguageDao.getByID(tl.getId());
        }
        transmitterLanguageDao.saveOrUpdate(tl);

        /**
         * Publish the required {@link StaticMsgTypeConfigNotification} if any
         * {@link StaticMessageType}s have been removed as part of this update.
         */
        if (CollectionUtils.isEmpty(request.getTransmitterLanguage()
                .getRemovedStaticMsgTypes()) == false) {
            for (StaticMessageType staticMsgType : request
                    .getTransmitterLanguage().getRemovedStaticMsgTypes()) {
                /*
                 * TODO: should update StaticMsgTypeConfigNotification to
                 * support multiple afos ids simultaneously.
                 */
                BmhMessageProducer.sendConfigMessage(
                        new StaticMsgTypeConfigNotification(
                                ConfigChangeType.Delete, request
                                        .getTransmitterLanguage()
                                        .getTransmitterGroup(),
                                request.getTransmitterLanguage().getLanguage(),
                                staticMsgType.getMsgTypeSummary().getAfosid(),
                                request), request.isOperational());
            }
        }

        TransmitterLanguageResponse response = new TransmitterLanguageResponse();
        response.addTransmitterLanguage(tl);

        if (logger.isPriorityEnabled(Priority.INFO)) {
            String user = BMHLoggerUtils.getUser(request);
            BMHLoggerUtils.logSave(request, user, oldTl, tl);
        }

        return response;
    }

    private StaticMsgValidationResult validateStaticMessageType(
            TransmitterLanguageRequest request) {
        StaticMessageType staticMsgType = request.getStaticMsgType();

        UnacceptableWordFilter uwf = UnacceptableWordFilter.getFilter(request
                .getLanguage());
        List<String> uw = uwf.check(staticMsgType.getTextMsg1());
        if (uw.isEmpty() == false) {
            final String textFieldName = (staticMsgType.getMsgTypeSummary()
                    .getDesignation() == Designation.StationID) ? "Station Id"
                    : "Time Preamble";
            return new StaticMsgValidationResult(
                    false,
                    textFieldName
                            + " failed to validate because it contains the following unacceptable words: "
                            + uw.toString());
        }

        /*
         * If this is a time announcement, we also need to check the second text
         * field.
         */
        if (staticMsgType.getMsgTypeSummary().getDesignation() == Designation.TimeAnnouncement) {
            uw = uwf.check(staticMsgType.getTextMsg2());
            if (uw.isEmpty() == false) {
                return new StaticMsgValidationResult(
                        false,
                        "Time Postamble failed to validate because it contains the following unacceptable words: "
                                + uw.toString());
            }
        }

        return new StaticMsgValidationResult();
    }

    private void deleteTransmitterLanguage(TransmitterLanguageRequest request) {
        if (request.getTransmitterLanguage() == null) {
            throw new IllegalArgumentException(
                    "Transmitter language cannot be NULL when the requested action is: "
                            + request.getAction().toString() + "!");
        }
        TransmitterLanguageDao transmitterLanguageDao = new TransmitterLanguageDao(
                request.isOperational());

        IUFStatusHandler logger = BMHLoggerUtils.getSrvLogger(request);

        TransmitterLanguage tl = request.getTransmitterLanguage();
        TransmitterLanguage oldTl = null;
        if (logger.isPriorityEnabled(Priority.INFO)) {
            oldTl = transmitterLanguageDao.getByID(tl.getId());
        }
        transmitterLanguageDao.delete(tl);

        if (logger.isPriorityEnabled(Priority.INFO)) {
            String user = BMHLoggerUtils.getUser(request);
            BMHLoggerUtils.logDelete(request, user, oldTl);
        }
    }

    private TransmitterLanguageResponse getStaticMsgTypeForTransmitterGroupAndMessageType(
            TransmitterLanguageRequest request) {
        TransmitterLanguageResponse response = new TransmitterLanguageResponse();
        StaticMessageTypeDao dao = new StaticMessageTypeDao(
                request.isOperational());
        response.setStaticMsgType(dao.getStaticForMsgTypeAndTransmittergroup(
                request.getAfosId(), request.getTransmitterGroup()));

        return response;
    }
}