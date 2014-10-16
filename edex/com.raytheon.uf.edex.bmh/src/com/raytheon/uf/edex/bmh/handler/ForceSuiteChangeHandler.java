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
import com.raytheon.uf.common.bmh.datamodel.msg.Suite;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.request.ForceSuiteChangeRequest;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.edex.bmh.playlist.PlaylistManager;

/**
 * Request handler for {@code ForceSuiteChangeRequest} objects.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 25, 2014  #3589     dgilling     Initial creation
 * Oct 13, 2014  #3413     rferrel     Implement User roles.
 * Oct 15, 2014, #3635     rferrel     Implement Logging
 * 
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

public final class ForceSuiteChangeHandler extends
        AbstractBMHServerRequestHandler<ForceSuiteChangeRequest> {

    private final PlaylistManager playlistMgr;

    public ForceSuiteChangeHandler(PlaylistManager playlistMgr) {
        this.playlistMgr = playlistMgr;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.common.serialization.comm.IRequestHandler#handleRequest
     * (com.raytheon.uf.common.serialization.comm.IServerRequest)
     */
    @Override
    public Boolean handleRequest(ForceSuiteChangeRequest request)
            throws Exception {
        TransmitterGroup group = request.getTransmitterGroup();
        Suite suite = request.getSelectedSuite();
        playlistMgr.processForceSuiteSwitch(group, suite);

        IUFStatusHandler logger = BMHLoggerUtils.getSrvLogger(request
                .isOperational());
        if (logger.isPriorityEnabled(Priority.INFO)) {
            String user = BMHLoggerUtils.getUser(request);
            logger.info("User "
                    + user
                    + " Force Suite Change Request for TransmitterGroup name/id: "
                    + group.getName() + "/" + group.getId()
                    + ", Suite name/id: " + suite.getName() + "/"
                    + suite.getId() + " type: " + suite.getType());
        }

        return Boolean.TRUE;
    }
}
