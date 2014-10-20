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

import com.raytheon.uf.common.bmh.BMHLoggerUtils;
import com.raytheon.uf.common.bmh.request.CopyOperationalDbRequest;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus.Priority;

/**
 * 
 * Handles {@link CopyOperationalDbRequest}s.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Oct 08, 2014  3687     bsteffen    Initial creation.
 * Oct 13, 2014  3413     rferrel     Implement User roles.
 * Oct 16, 2014  3636     rferrel     Added logger information.
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class CopyOperationalDbHandler extends
        AbstractBMHServerRequestHandler<CopyOperationalDbRequest> {

    @Override
    public Boolean handleRequest(CopyOperationalDbRequest request)
            throws Exception {
        if (request.isOperational()) {
            throw new UnsupportedOperationException(
                    "Cannot copy operational db while in operational mode.");
        }
        new BmhDatabaseCopier().copyAll();
        IUFStatusHandler logger = BMHLoggerUtils.getSrvLogger(request
                .isOperational());
        if (logger.isPriorityEnabled(Priority.INFO)) {
            String user = BMHLoggerUtils.getUser(request);
            logger.info("User " + user + " performed BMH database copy all.");
        }
        return true;
    }

}
