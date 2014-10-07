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
package com.raytheon.uf.common.bmh;

import java.util.Calendar;

/**
 * Definition of an implementation that will retrieve a specific field
 * associated with a {@link TIME_MSG_TOKENS} from a {@link Calendar}.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 7, 2014  3642       bkowal     Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public interface ITimeFieldRetriever {
    /**
     * Used to get a specific field from the specified {@link Calendar}
     * 
     * @param calendar
     *            the specified {@link Calendar}
     * @return the requested field converted to a {@link String}
     */
    public String getTimeField(Calendar calendar);

    /**
     * Used to indicate when a field should be skipped. No audio should be
     * retrieved for it.
     * 
     * @param value
     * @return
     */
    public boolean isSkipped(String value);
}