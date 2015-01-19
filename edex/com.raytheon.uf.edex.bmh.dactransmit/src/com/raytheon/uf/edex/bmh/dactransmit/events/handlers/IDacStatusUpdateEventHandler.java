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
package com.raytheon.uf.edex.bmh.dactransmit.events.handlers;

import com.raytheon.uf.edex.bmh.dactransmit.events.DacStatusUpdateEvent;
import com.raytheon.uf.edex.bmh.dactransmit.events.LostSyncEvent;
import com.raytheon.uf.edex.bmh.dactransmit.events.RegainSyncEvent;

/**
 * Event handler for {@code DacStatusUpdateEvent}.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 16, 2014  #3286     dgilling     Initial creation
 * Jan 19, 2014  #3912     bsteffen     Add methods to handle sync events.
 * 
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

public interface IDacStatusUpdateEventHandler {

    /**
     * Event handler method for {@code DacStatusUpdateEvent}.
     * 
     * @param e
     *            {@code DacStatusUpdateEvent} event object
     */
    void receivedDacStatus(DacStatusUpdateEvent e);

    /**
     * Event handler method for {@link LostSyncEvent}.
     * 
     * @param e
     *            {@link LostSyncEvent} event object
     */
    public void lostDacSync(LostSyncEvent e);

    /**
     * Event handler method for {@link RegainSyncEvent}.
     * 
     * @param e
     *            {@link RegainSyncEvent} event object
     */
    public void regainDacSync(RegainSyncEvent e);
}
