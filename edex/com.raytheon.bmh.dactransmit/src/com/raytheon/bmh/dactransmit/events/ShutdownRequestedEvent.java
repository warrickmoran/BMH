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
package com.raytheon.bmh.dactransmit.events;

/**
 * Event object for shutting down this application.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 22, 2014  #3286     dgilling     Initial creation
 * Nov 11, 2014  #3762     bsteffen     Add delayed shutdown.
 * 
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

public final class ShutdownRequestedEvent {

    /**
     * When true, shutdown should be immediate, false indicates shutdown should
     * occur before beginning another message.
     */
    private boolean now;

    public ShutdownRequestedEvent() {
        this(true);
    }

    public ShutdownRequestedEvent(boolean now) {
        this.now = now;
    }

    public boolean isNow() {
        return now;
    }

    public void setNow(boolean now) {
        this.now = now;
    }

}
