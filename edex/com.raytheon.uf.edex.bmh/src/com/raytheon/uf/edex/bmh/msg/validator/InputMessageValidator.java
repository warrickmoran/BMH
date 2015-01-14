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
package com.raytheon.uf.edex.bmh.msg.validator;

import java.util.List;

import com.raytheon.uf.common.bmh.BMH_CATEGORY;
import com.raytheon.uf.common.bmh.datamodel.msg.InputMessage;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType.Designation;
import com.raytheon.uf.common.bmh.datamodel.msg.ValidatedMessage;
import com.raytheon.uf.common.bmh.datamodel.msg.ValidatedMessage.LdadStatus;
import com.raytheon.uf.common.bmh.datamodel.msg.ValidatedMessage.TransmissionStatus;
import com.raytheon.uf.common.bmh.notify.MessageExpiredNotification;
import com.raytheon.uf.common.serialization.SerializationException;
import com.raytheon.uf.edex.bmh.BmhMessageProducer;
import com.raytheon.uf.edex.bmh.dao.MessageTypeDao;
import com.raytheon.uf.edex.bmh.msg.logging.IMessageLogger;
import com.raytheon.uf.edex.bmh.msg.logging.ErrorActivity.BMH_ACTIVITY;
import com.raytheon.uf.edex.bmh.msg.logging.ErrorActivity.BMH_COMPONENT;
import com.raytheon.uf.edex.bmh.status.BMHStatusHandler;
import com.raytheon.uf.edex.core.EdexException;

/**
 * Validate {@link InputMessage}, generating a {@link ValidatedMessage}.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Jun 23, 2014  3283     bsteffen    Initial creation
 * Nov 20, 2014  3385     bkowal      Create an operational {@link LdadValidator}.
 * Dec 02, 2014  3614     bsteffen    Check for unacceptable words.
 * Jan 05, 2015  3651     bkowal      Use {@link IMessageLogger} to log message errors.
 * Jan 06, 2015  3651     bkowal      Support AbstractBMHPersistenceLoggingDao.
 * Jan 14, 2015  3969     bkowal      Post a {@link MessageExpiredNotification} when a
 *                                    watch/warning fails validation because it expired.
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class InputMessageValidator {

    protected static final BMHStatusHandler statusHandler = BMHStatusHandler
            .getInstance(InputMessageValidator.class);

    private final TransmissionValidator transmissionCheck;

    private final MessageTypeDao messageTypeDao = new MessageTypeDao(true);

    /**
     * Currently {@link InputMessageValidator} is only used in operational mode.
     */
    private final LdadValidator ldadCheck;

    private final IMessageLogger messageLogger;

    public InputMessageValidator(final IMessageLogger messageLogger) {
        this.messageLogger = messageLogger;
        this.ldadCheck = new LdadValidator(true, messageLogger);
        this.transmissionCheck = new TransmissionValidator(messageLogger);
    }

    /**
     * Validate an InputMessage.
     * 
     * @param message
     *            the message to validate
     * @return a ValidatedMessage containing the status of validating the
     *         InputMessage.
     */
    public ValidatedMessage validate(InputMessage input) {
        ValidatedMessage valid = new ValidatedMessage();
        valid.setInputMessage(input);
        List<String> unacceptableWords = UnacceptableWordFilter.check(input);
        if (unacceptableWords.isEmpty()) {
            transmissionCheck.validate(valid);
            ldadCheck.validate(valid);
            if (!valid.isAccepted()) {
                statusHandler
                        .error(BMH_CATEGORY.MESSAGE_VALIDATION_FAILED,
                                input.getName()
                                        + " failed to validate with transmission status: "
                                        + valid.getTransmissionStatus());
                this.messageLogger.logError(
                        BMH_COMPONENT.INPUT_MESSAGE_VALIDATOR,
                        BMH_ACTIVITY.MESSAGE_VALIDATION, valid);
                /*
                 * did validation fail because the message expired?
                 */
                if (valid.getTransmissionStatus() == TransmissionStatus.EXPIRED) {
                    /*
                     * is the message a watch or warning?
                     */
                    MessageType mt = this.messageTypeDao.getByAfosId(input
                            .getAfosid());
                    if (mt != null
                            && (mt.getDesignation() == Designation.Watch || mt
                                    .getDesignation() == Designation.Warning)) {
                        /*
                         * notify the user that the watch/warning will not be
                         * broadcast.
                         */
                        StringBuilder sb = new StringBuilder(mt
                                .getDesignation().toString());
                        /* identify the message */
                        sb.append("InputMessage [id=").append(input.getId());
                        sb.append(", name=").append(input.getName());
                        sb.append(", afosid=").append(input.getAfosid());
                        sb.append("] has expired and will never be broadcast on any transmitters.");
                        /*
                         * Currently Input Message Validator is only
                         * operational.
                         */
                        MessageExpiredNotification notification = new MessageExpiredNotification(
                                input, mt.getDesignation().toString());
                        try {
                            BmhMessageProducer.sendStatusMessage(notification,
                                    true);
                        } catch (EdexException | SerializationException e) {
                            statusHandler.error(BMH_CATEGORY.UNKNOWN,
                                    "Failed to send notification: "
                                            + notification.toString() + ".", e);
                        }
                    }
                }
            }
        } else {
            valid.setTransmissionStatus(TransmissionStatus.UNACCEPTABLE);
            valid.setLdadStatus(LdadStatus.UNACCEPTABLE);
            statusHandler
                    .error(BMH_CATEGORY.MESSAGE_VALIDATION_FAILED,
                            input.getName()
                                    + "("
                                    + input.getAfosid()
                                    + ") failed to validate because it contains unacceptable words.");
            this.messageLogger.logError(BMH_COMPONENT.INPUT_MESSAGE_VALIDATOR,
                    BMH_ACTIVITY.MESSAGE_VALIDATION, valid);
        }
        return valid;
    }
}
