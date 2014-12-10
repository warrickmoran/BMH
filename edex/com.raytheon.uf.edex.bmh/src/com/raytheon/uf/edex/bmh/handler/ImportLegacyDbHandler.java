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
import com.raytheon.uf.common.bmh.request.ImportLegacyDbRequest;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus.Priority;

/**
 * 
 * Handles {@link ImportLegacyDbRequest}s.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Dec 05, 2014  3824     rferrel     Initial creation.
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class ImportLegacyDbHandler extends
        AbstractBMHServerRequestHandler<ImportLegacyDbRequest> {

    @Override
    public Boolean handleRequest(ImportLegacyDbRequest request)
            throws Exception {
        String input = new String(request.getInput());
        String source = request.getSource();

        boolean operational = request.isOperational();
        IUFStatusHandler logger = BMHLoggerUtils.getSrvLogger(request);

        new ImportLegacyDatabase(input, source, operational).saveImport();

        if (logger.isPriorityEnabled(Priority.INFO)) {
            String user = BMHLoggerUtils.getUser(request);
            logger.info("User " + user + " performed import legacy database: "
                    + source);
        }
        return true;
    }
}
