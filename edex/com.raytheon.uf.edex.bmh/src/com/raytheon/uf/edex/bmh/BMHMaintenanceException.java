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
package com.raytheon.uf.edex.bmh;

/**
 * Indicates that a maintenance message could not be successfully generated or
 * written.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 11, 2015 4490       bkowal      Initial creation
 * Jul 23, 2015 4676       bkowal      Added constructor that takes an {@link Exception}.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class BMHMaintenanceException extends Exception {

    private static final long serialVersionUID = 8295066595231153360L;

    /**
     * @param message
     *            details about the cause of the exception
     */
    public BMHMaintenanceException(String message) {
        super(message);
    }

    public BMHMaintenanceException(String message, Exception e) {
        super(message, e);
    }
}