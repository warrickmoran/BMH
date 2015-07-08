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
package com.raytheon.uf.edex.bmh.dactransmit.dacsession;

import java.net.InetAddress;
import java.net.SocketException;
import java.util.Collection;

import com.google.common.eventbus.EventBus;

/**
 * Extension of the {@link BroadcastTransmitThread} used for maintenance dac
 * sessions. This implementation of the broadcast thread allows a user to
 * forcefully end a broadcast via the {@link #forceShutdown()} method and to
 * retrieve the number of packets that have yet to be broadcast via the
 * {@link #getRemainingPacketCount()} method.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 9, 2015  4364       bkowal      Initial creation
 * Apr 16, 2015 4405       rjpeter     Update to have hasSync initialized.
 * Jul 08, 2015 4636       bkowal      Support same and alert decibel levels.
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class MaintenanceBroadcastTransmitThread extends BroadcastTransmitThread {

    public MaintenanceBroadcastTransmitThread(String name, EventBus eventBus,
            InetAddress address, int port, Collection<Integer> transmitters,
            double dbTarget, boolean hasSync) throws SocketException {
        super(name, eventBus, address, port, transmitters, dbTarget, -999,
                -999, hasSync);
    }

    /**
     * Returns the remaining number of packets that need to be streamed.
     * 
     * @return the remaining number of packets that need to be streamed.
     */
    public int getRemainingPacketCount() {
        return this.audioBuffer.size();
    }

    /**
     * Sets the error flag which will force the Broadcast thread to shutdown
     * early.
     */
    public void forceShutdown() {
        this.error = true;
    }
}