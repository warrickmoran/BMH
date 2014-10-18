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

import com.raytheon.uf.common.bmh.datamodel.msg.InputMessage;
import com.raytheon.uf.common.bmh.request.InputMessageRequest;
import com.raytheon.uf.common.bmh.request.InputMessageResponse;
import com.raytheon.uf.edex.bmh.dao.InputMessageDao;

/**
 * Handles any requests to get or modify the state of {@link InputMessage}s.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 16, 2014  #3728     lvenable     Initial creation
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */

public class InputMessageHandler extends
        AbstractBMHServerRequestHandler<InputMessageRequest> {

    @Override
    public Object handleRequest(InputMessageRequest request) throws Exception {
        InputMessageResponse inputMessageResponse = new InputMessageResponse();

        switch (request.getAction()) {
        case AllInputMessages:
            inputMessageResponse = getAllInputMessages(request);
            break;
        case ListIdNameAfosCreation:
            inputMessageResponse = getIdNameAfosCreation(request);
            break;
        case GetByPkId:
            inputMessageResponse = getByPkId(request);
            break;
        default:
            throw new UnsupportedOperationException(this.getClass()
                    .getSimpleName()
                    + " cannot handle action "
                    + request.getAction());
        }

        return inputMessageResponse;
    }

    /**
     * Get a list on input messages containing the Id, Name, Afos Id, and
     * Creation time.
     * 
     * @param request
     *            Input Message request.
     * @return Input message response containing the requested information.
     */
    private InputMessageResponse getIdNameAfosCreation(
            InputMessageRequest request) {
        InputMessageDao dao = new InputMessageDao(request.isOperational());
        InputMessageResponse response = new InputMessageResponse();

        List<InputMessage> inputMessageList = dao
                .getInputMsgsIdNameAfosCreation();
        response.setInputMessageList(inputMessageList);

        return response;
    }

    /**
     * Get all of the input messages fully populated with data.
     * 
     * @param request
     *            Input Message request.
     * @return Input message response containing the requested information.
     */
    private InputMessageResponse getAllInputMessages(InputMessageRequest request) {
        InputMessageDao dao = new InputMessageDao(request.isOperational());
        InputMessageResponse response = new InputMessageResponse();

        List<InputMessage> inputMessageList = dao.getInputMessages();
        response.setInputMessageList(inputMessageList);

        return response;
    }

    /**
     * Get the input message by the primary key ID.
     * 
     * @param request
     *            Input Message request.
     * @return Input message response containing the requested information.
     */
    private InputMessageResponse getByPkId(InputMessageRequest request) {
        InputMessageDao dao = new InputMessageDao(request.isOperational());
        InputMessageResponse response = new InputMessageResponse();

        InputMessage im = dao.getByID((int) request.getPkId());
        response.addInputMessage(im);

        return response;
    }
}
