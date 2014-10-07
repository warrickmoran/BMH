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
 * Generic abstraction of calendar field retrieval using
 * {@link Calendar#get(int)}.
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

public class AbstractCalendarGetRetriever implements ITimeFieldRetriever {

    protected final int timeField;

    /**
     * 
     */
    public AbstractCalendarGetRetriever(final int timeField) {
        this.timeField = timeField;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.common.bmh.ITimeFieldRetriever#getTimeField(java.util
     * .Calendar)
     */
    @Override
    public String getTimeField(Calendar calendar) {
        return Integer.toString(calendar.get(this.timeField));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.common.bmh.ITimeFieldRetriever#isSkipped(java.lang.String
     * )
     */
    @Override
    public boolean isSkipped(String value) {
        return false;
    }
}