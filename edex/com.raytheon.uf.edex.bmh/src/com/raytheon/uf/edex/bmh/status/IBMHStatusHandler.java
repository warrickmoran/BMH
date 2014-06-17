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
package com.raytheon.uf.edex.bmh.status;

import com.raytheon.uf.edex.bmh.status.BMH_CATEGORY;

/**
 * Defines the BMH status handler.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 16, 2014 3291       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public interface IBMHStatusHandler {
    public void debug(String message);

    public void debug(BMH_CATEGORY category, String message);

    public void info(String message);

    public void info(BMH_CATEGORY category, String message);

    public void warn(BMH_CATEGORY category, String message);

    public void error(BMH_CATEGORY category, String message);

    public void error(BMH_CATEGORY category, String message, Throwable throwable);

    public void fatal(BMH_CATEGORY category, String message);

    public void fatal(BMH_CATEGORY category, String message, Throwable throwable);
}