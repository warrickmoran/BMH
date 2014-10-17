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
package com.raytheon.bmh.dacsimulator.events;

import java.net.InetAddress;

/**
 * Event when a DAC channels make a successful sync with a client.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 17, 2014  #3688     dgilling     Initial creation
 * 
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

public class SyncObtainedEvent {

    private final InetAddress syncHost;

    private final int syncPort;

    /**
     * Constructor.
     * 
     * @param syncHost
     *            Host that the sync was made with.
     * @param syncPort
     *            Port the sync was made on.
     */
    public SyncObtainedEvent(InetAddress syncHost, int syncPort) {
        this.syncHost = syncHost;
        this.syncPort = syncPort;
    }

    public InetAddress getSyncHost() {
        return syncHost;
    }

    public int getSyncPort() {
        return syncPort;
    }
}
