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
package com.raytheon.uf.viz.bmh.ui.recordplayback.live;

/**
 * Indicates that a problem occurred in preparation for or during a live
 * broadcast.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 10, 2014 3656       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class BroadcastException extends Exception {
    private static final long serialVersionUID = 2395910520425856157L;

    /**
     * Constructor
     * 
     * @param message
     *            the detail message.
     */
    public BroadcastException(String message) {
        super(message);
    }

    /**
     * Constructor
     * 
     * @param message
     *            the detail message.
     * @param cause
     *            the cause.
     */
    public BroadcastException(String message, Throwable cause) {
        super(message, cause);
    }
}
