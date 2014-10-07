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
 * Implementation of {@link AbstractCalendarGetRetriever} used to retrieve the
 * {@link Calendar#AM_PM} field from a {@link Calendar}.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 7, 2014  3642       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class PeriodRetriever extends AbstractCalendarGetRetriever {

    public PeriodRetriever() {
        super(Calendar.AM_PM);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.common.bmh.AbstractCalendarGetRetriever#getTimeField(
     * java.util.Calendar)
     */
    @Override
    public String getTimeField(Calendar calendar) {
        final int am_pm = calendar.get(this.timeField);
        // {@link Calendar} returns a numeric field that maps to am or pm.
        if (am_pm == Calendar.AM) {
            return "AM";
        }
        // either "am" or "pm". not verifying against the pm constant.
        return "PM";
    }
}