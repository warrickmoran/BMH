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
import com.raytheon.uf.edex.bmh.status.BMHStatusHandler;

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
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class InputMessageValidator {

    protected static final BMHStatusHandler statusHandler = BMHStatusHandler
            .getInstance(InputMessageValidator.class);

    private final TransmissionValidator transmissionCheck = new TransmissionValidator();

    private final LdadValidator ldadCheck = new LdadValidator();

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
        transmissionCheck.validate(valid);
        ldadCheck.validate(valid);
        if (!valid.isAccepted()) {
            statusHandler.error(BMH_CATEGORY.MESSAGE_VALIDATION_FAILED,
                    input.getAfosid()
                            + " failed to validate with transmission status: "
                            + valid.getTransmissionStatus());
        }
        return valid;
    }
}
