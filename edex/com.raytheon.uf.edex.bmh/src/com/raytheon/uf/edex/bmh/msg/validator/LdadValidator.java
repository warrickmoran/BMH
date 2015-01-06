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

import com.raytheon.uf.common.bmh.BMH_CATEGORY;
import com.raytheon.uf.common.bmh.datamodel.msg.InputMessage;
import com.raytheon.uf.common.bmh.datamodel.msg.ValidatedMessage;
import com.raytheon.uf.common.bmh.datamodel.msg.ValidatedMessage.LdadStatus;
import com.raytheon.uf.edex.bmh.dao.LdadConfigDao;
import com.raytheon.uf.edex.bmh.msg.logging.IMessageLogger;
import com.raytheon.uf.edex.bmh.msg.logging.ErrorActivity.BMH_ACTIVITY;
import com.raytheon.uf.edex.bmh.msg.logging.ErrorActivity.BMH_COMPONENT;
import com.raytheon.uf.edex.bmh.status.BMHStatusHandler;

/**
 * 
 * Validate {@link InputMessage}s to verify they are acceptable for ldad.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Jun 23, 2014  3283     bsteffen    Initial creation
 * Nov 19, 2014  3385     bkowal      Implemented.
 * Nov 20, 2014  3385     bkowal      Initialize {@link LdadConfigDao} based on the
 *                                    run mode.
 * Jan 05, 2015  3651     bkowal      Use {@link IMessageLogger} to log message errors.
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class LdadValidator {

    protected static final BMHStatusHandler statusHandler = BMHStatusHandler
            .getInstance(LdadValidator.class);

    private final LdadConfigDao ldadConfigDao;

    private final IMessageLogger messageLogger;

    public LdadValidator(boolean operational, final IMessageLogger messageLogger) {
        this.ldadConfigDao = new LdadConfigDao(operational);
        this.messageLogger = messageLogger;
    }

    public void validate(ValidatedMessage message) {
        LdadStatus status = LdadStatus.NONE;
        try {
            if (this.ldadConfigDao.getLdadConfigsForMsgType(message
                    .getInputMessage().getAfosid()) != null) {
                status = LdadStatus.ACCEPTED;
            }
        } catch (Exception e) {
            statusHandler.error(
                    BMH_CATEGORY.MESSAGE_VALIDATION_FAILED,
                    "Failed to determine if validated message: "
                            + message.getId()
                            + " has any applicable ldad configurations.", e);
            status = LdadStatus.ERROR;
            this.messageLogger.logError(BMH_COMPONENT.LDAD_VALIDATOR,
                    BMH_ACTIVITY.MESSAGE_VALIDATION, message, e);
        }

        message.setLdadStatus(status);
    }
}