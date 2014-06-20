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

import java.util.Calendar;
import java.util.Collections;
import java.util.Set;

import com.raytheon.uf.common.bmh.datamodel.msg.InputMessage;
import com.raytheon.uf.common.bmh.datamodel.msg.ValidatedMessage;
import com.raytheon.uf.common.bmh.datamodel.msg.ValidatedMessage.TransmissionStatus;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.time.SimulatedTime;
import com.raytheon.uf.edex.bmh.dao.InputMessageDao;
import com.raytheon.uf.edex.bmh.status.BMHStatusHandler;
import com.raytheon.uf.edex.bmh.status.BMH_CATEGORY;
import com.raytheon.uf.edex.database.DataAccessLayerException;

/**
 * 
 * Validate {@link InputMessage}s to verify they are able to be transmitted.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Jun 23, 2014  3283     bsteffen    Initial creation
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class TransmissionValidator {

    protected static final BMHStatusHandler statusHandler = BMHStatusHandler
            .getInstance(TransmissionValidator.class);

    private InputMessageDao dao = new InputMessageDao();

    public void validate(ValidatedMessage message) {
        InputMessage input = message.getInputMessage();
        try {
            if (isExpired(input)) {
                message.setTransmissionStatus(TransmissionStatus.EXPIRED);
            } else if (checkConfiguration(input)) {
                message.setTransmissionStatus(TransmissionStatus.UNDEFINED);
            } else if (dao.checkDuplicate(input)) {
                message.setTransmissionStatus(TransmissionStatus.DUPLICATE);
            } else {
                Set<TransmitterGroup> groups = getTransmissionGroups(input);
                if (groups.isEmpty()) {
                    message.setTransmissionStatus(TransmissionStatus.UNPLAYABLE);
                } else {
                    groups = checkSuite(input, groups);
                    if (groups.isEmpty()) {
                        message.setTransmissionStatus(TransmissionStatus.UNASSIGNED);
                    } else {
                        message.setTransmitterGroups(groups);
                        message.setTransmissionStatus(TransmissionStatus.ACCEPTED);
                    }
                }
            }
        } catch (DataAccessLayerException e) {
            message.setTransmissionStatus(TransmissionStatus.ERROR);
            statusHandler.error(BMH_CATEGORY.MESSAGE_VALIDATION_ERROR,
                    "Error Parsing InputMessage", e);
        }
    }

    /**
     * Check if a message has expired by comparing the expiration time to the
     * current {@link SimulatedTime}.
     * 
     * @param message
     *            the message to validate.
     * @return true if the message is expired.
     */
    protected static boolean isExpired(InputMessage message) {
        Calendar expirationTime = message.getExpirationTime();
        if (expirationTime == null) {
            return false;
        } else {
            return SimulatedTime.getSystemTime().getMillis() < message
                    .getExpirationTime().getTimeInMillis();
        }
    }

    /**
     * Check if a message type is defined in the BMH configuration
     * 
     * @param message
     *            the message to validate.
     * @return true if the message type is not defined
     */
    protected static boolean checkConfiguration(InputMessage message) {
        // TODO implement this
        return false;
    }

    /**
     * Check if a message Geographical information can be mapped to the
     * broadcast area of any transmitters.
     * 
     * @param message
     *            the message to validate.
     * @return any transmitter groups matching the geographical area of this
     *         message.
     */
    protected Set<TransmitterGroup> getTransmissionGroups(InputMessage message) {
        // TODO implement this
        return Collections.emptySet();
    }

    /**
     * Check if a message type is assigned to any suite.
     * 
     * @param message
     *            the message to validate.
     * @param groups
     *            groups that the message might be sent too
     * @return any transmitter groups from groups whose program contains a suite
     *         for this message type.
     */
    protected static Set<TransmitterGroup> checkSuite(InputMessage message,
            Set<TransmitterGroup> groups) {
        return groups;
    }

}
