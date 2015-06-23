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

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;

import com.raytheon.uf.common.bmh.datamodel.msg.MessageTypeSummary;
import com.raytheon.uf.common.bmh.request.MessageTypeValidationRequest;
import com.raytheon.uf.common.bmh.request.MessageTypeValidationResponse;
import com.raytheon.uf.edex.bmh.dao.MessageTypeDao;
import com.raytheon.uf.edex.bmh.msg.validator.InputMessageParser;

/**
 * Handles afos id validation and lookup.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 23, 2015 4572       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class MessageTypeValidationHandler extends
        AbstractBMHServerRequestHandler<MessageTypeValidationRequest> {

    private static final Pattern afosidPattern = Pattern
            .compile(InputMessageParser.AFOS_ID_REGEX);

    @Override
    public Object handleRequest(MessageTypeValidationRequest request)
            throws Exception {
        MessageTypeValidationResponse response = new MessageTypeValidationResponse();
        Set<String> validAfosIds = request.getAfosIds();
        Iterator<String> afosIdItr = validAfosIds.iterator();
        while (afosIdItr.hasNext()) {
            final String afosId = afosIdItr.next();
            if (afosidPattern.matcher(afosId).matches() == false) {
                afosIdItr.remove();
                response.addInvalidAfosId(afosId);
            }
        }

        final MessageTypeDao dao = new MessageTypeDao(request.isOperational());
        List<MessageTypeSummary> messageTypes = dao.getByAfosIds(validAfosIds);
        for (MessageTypeSummary mts : messageTypes) {
            /*
             * Remove any afos ids that we were able to find associated {@link
             * MessageType}s for.
             */
            validAfosIds.remove(mts.getAfosid());
        }
        if (CollectionUtils.isNotEmpty(validAfosIds)) {
            response.setMissingAfosIds(validAfosIds);
        }
        response.setMessageTypes(new HashSet<>(messageTypes));

        return response;
    }
}