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

import com.raytheon.uf.common.bmh.request.GetAllDataRequest;
import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.edex.bmh.dao.TransmitterDao;

/**
 * Handler for GetDallDataRequest.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 30, 2014 3175       rjpeter     Initial creation
 * 
 * </pre>
 * 
 * @author rjpeter
 * @version 1.0
 */
public class GetAllDataHandler implements IRequestHandler<GetAllDataRequest> {

    @Override
    public Object handleRequest(GetAllDataRequest request) throws Exception {
        switch (request.getDataType()) {
        case Areas:
            break;
        case MessageTypes:
            break;
        case Programs:
            break;
        case Suites:
            break;
        case Transmitters:
            TransmitterDao dao = new TransmitterDao();
            dao.getTransmitters();
            break;
        case Zones:
            break;
        default:
            break;
        }
        return null;
    }

}
