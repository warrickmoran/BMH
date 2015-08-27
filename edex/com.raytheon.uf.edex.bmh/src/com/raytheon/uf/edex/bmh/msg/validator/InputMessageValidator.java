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
import com.raytheon.uf.common.bmh.trace.TraceableUtil;
import com.raytheon.uf.common.serialization.SerializationException;
import com.raytheon.uf.edex.bmh.BMHRejectionDataManager;
import com.raytheon.uf.edex.bmh.BMHRejectionException;
import com.raytheon.uf.edex.bmh.BmhMessageProducer;
import com.raytheon.uf.edex.bmh.dao.InputMessageDao;
import com.raytheon.uf.edex.bmh.dao.MessageTypeDao;
import com.raytheon.uf.edex.bmh.dao.ValidatedMessageDao;
import com.raytheon.uf.edex.bmh.msg.logging.ErrorActivity.BMH_ACTIVITY;
import com.raytheon.uf.edex.bmh.msg.logging.ErrorActivity.BMH_COMPONENT;
import com.raytheon.uf.edex.bmh.msg.logging.IMessageLogger;
import com.raytheon.uf.edex.bmh.msg.logging.MessageActivity.MESSAGE_ACTIVITY;
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
 * May 13, 2015  4429     rferrel     Set traceId.
 * May 21, 2015  4429     rjpeter     Added additional logging.
 * Jul 29, 2015  4690     rjpeter     Added reject/checkReject methods.
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

    private final InputMessageDao inputMessageDao;

    private final ValidatedMessageDao validatedMessageDao;

    /**
     * Currently {@link InputMessageValidator} is only used in operational mode.
     */
    private final LdadValidator ldadCheck;

    private final IMessageLogger messageLogger;

    private final BMHRejectionDataManager rejectionManager;

    public InputMessageValidator(final IMessageLogger messageLogger,
            final BMHRejectionDataManager rejectionManager) {
        this.messageLogger = messageLogger;
        this.ldadCheck = new LdadValidator(true, messageLogger);
        this.transmissionCheck = new TransmissionValidator(messageLogger);
        this.inputMessageDao = new InputMessageDao(true, messageLogger);
        this.validatedMessageDao = new ValidatedMessageDao(true, messageLogger);
        this.rejectionManager = rejectionManager;
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
        valid.setTraceId(input.getName() + "_" + input.getId());
        messageLogger.logMessageActivity(valid,
                MESSAGE_ACTIVITY.VALIDATION_START, input);
        List<String> unacceptableWords = UnacceptableWordFilter.check(input);
        if (unacceptableWords.isEmpty()) {
            transmissionCheck.validate(valid);
            ldadCheck.validate(valid);
            if (!valid.isAccepted()) {
                statusHandler
                        .error(BMH_CATEGORY.MESSAGE_VALIDATION_FAILED,
                                TraceableUtil.createTraceMsgHeader(valid)
                                        + "failed to validate with transmission status: "
                                        + valid.getTransmissionStatus());
                this.messageLogger.logError(valid,
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
                                    TraceableUtil.createTraceMsgHeader(valid)
                                            + "Failed to send notification: "
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
                            TraceableUtil.createTraceMsgHeader(valid)
                                    + "("
                                    + input.getAfosid()
                                    + ") failed to validate because it contains unacceptable words.");
            this.messageLogger.logError(valid,
                    BMH_COMPONENT.INPUT_MESSAGE_VALIDATOR,
                    BMH_ACTIVITY.MESSAGE_VALIDATION, valid);
        }
        this.messageLogger.logValidationActivity(valid);

        return valid;
    }

    /**
     * Saves original message to reject folder depending on TransmissionStatus.
     * Also deletes the InputMessage in this case.
     * 
     * @param message
     */
    public void checkReject(ValidatedMessage message) {
        InputMessage im = message.getInputMessage();

        switch (message.getTransmissionStatus()) {
        case UNPLAYABLE: // fall through
        case UNDEFINED: // fall through
        case UNACCEPTABLE:
            reject(im);
            // fall through
        case DUPLICATE:
            inputMessageDao.delete(im);
            break;
        default:
            validatedMessageDao.persist(message);
            break;
        }
    }

    /**
     * Saves the original message to the reject directory.
     * 
     * @param message
     */
    public void reject(InputMessage message) {
        try {
            rejectionManager.rejectFile(message.getOriginalFile().toPath(),
                    BMH_CATEGORY.MESSAGE_VALIDATION_ERROR);
        } catch (BMHRejectionException e) {
            statusHandler.error(BMH_CATEGORY.MESSAGE_VALIDATION_ERROR,
                    "Unable to write reject message", e);
        }
    }
}
