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
package com.raytheon.uf.viz.bmh;

import com.raytheon.uf.viz.core.VizServers;
import com.raytheon.viz.core.mode.CAVEMode;

/**
 * Constants for the BMH Servers
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 09, 2014 3656       bkowal      Initial creation
 * Oct 17, 2014 3687       bsteffen    Support practice servers.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public final class BMHServers {

    private static final String LINETAP_SERVER = "bmh.comms.manager.linetap";
    
    private static final String BROADCAST_SERVER = "bmh.comms.manager.broadcast";

    private static final String PRACTICE_LINETAP_SERVER = "bmh.practice.comms.manager.linetap";

    private static final String PRACTICE_BROADCAST_SERVER = "bmh.practice.comms.manager.broadcast";

    public static String getLineTapServer() {
        String key = LINETAP_SERVER;
        if (CAVEMode.getMode() != CAVEMode.OPERATIONAL) {
            key = PRACTICE_LINETAP_SERVER;
        }
        return VizServers.getInstance().getServerLocation(key);
    }

    public static String getBroadcastServerKey() {
        String key = BROADCAST_SERVER;
        if (CAVEMode.getMode() != CAVEMode.OPERATIONAL) {
            key = PRACTICE_BROADCAST_SERVER;
        }
        return key;
    }

    public static String getBroadcastServer() {
        return VizServers.getInstance().getServerLocation(
                getBroadcastServerKey());
    }

    /**
     * 
     */
    protected BMHServers() {
    }
}