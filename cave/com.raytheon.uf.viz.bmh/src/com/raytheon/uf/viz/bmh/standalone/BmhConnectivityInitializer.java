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
package com.raytheon.uf.viz.bmh.standalone;

import com.raytheon.uf.common.localization.msgs.GetServersResponse;
import com.raytheon.uf.viz.core.VizApp;
import com.raytheon.uf.viz.core.VizServers;
import com.raytheon.uf.viz.core.localization.LocalizationInitializer;

/**
 * Initialize the server connections that are necessary for running BMH
 * standalone.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#   Engineer    Description
 * ------------- --------- ----------- --------------------------
 * Feb 05, 2015  3743      bsteffen    Initial creation
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class BmhConnectivityInitializer extends LocalizationInitializer {

    private boolean onlyBroadcastLive;

    BmhConnectivityInitializer() {
        super(true, false);
    }

    @Override
    public void run() throws Exception {
        BmhConnectivityPreferenceDialog dlg = new BmhConnectivityPreferenceDialog();
        if (dlg.open() == true) {
            System.exit(0);
        }
        if (dlg.isUseBmhServer() || dlg.isOnlyBroadcastLive()) {
            GetServersResponse servers = dlg.getServers();
            VizServers.getInstance().setServerLocations(
                    servers.getServerLocations());
            String jmsConnection = servers.getJmsConnectionString();
            if (jmsConnection != null) {
                VizApp.setJmsConnectionString(jmsConnection);
            }
        } else {
            processGetServers();
        }
        this.onlyBroadcastLive = dlg.isOnlyBroadcastLive();
    }

    public boolean isOnlyBroadcastLive() {
        return onlyBroadcastLive;
    }

}