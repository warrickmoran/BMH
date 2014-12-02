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
import com.raytheon.uf.common.bmh.datamodel.msg.ValidatedMessage;
import com.raytheon.uf.common.bmh.datamodel.msg.ValidatedMessage.LdadStatus;
import com.raytheon.uf.common.bmh.datamodel.msg.ValidatedMessage.TransmissionStatus;
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
 * Nov 20, 2014  3385     bkowal      Create an operational {@link LdadValidator}.
 * Dec 02, 2014  3614     bsteffen    Check for unacceptable words.
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

    /**
     * Currently {@link InputMessageValidator} is only used in operational mode.
     */
    private final LdadValidator ldadCheck = new LdadValidator(true);

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

        }
        return valid;
    }
}
