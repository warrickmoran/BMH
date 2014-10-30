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

import com.raytheon.uf.common.bmh.datamodel.transmitter.BMHTimeZone;

/**
 * Definition of {@link ITimeFieldRetriever} used to retrieve the timezone from
 * a {@link Calendar}.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 07, 2014 3642       bkowal      Initial creation
 * Oct 30, 2014 3617       dgilling    Fix for changes to transmitter group 
 *                                     time zone field.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class ZoneRetriever implements ITimeFieldRetriever {
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.common.bmh.ITimeFieldRetriever#getTimeField(java.util
     * .Calendar)
     */
    @Override
    public String getTimeField(Calendar calendar) {
        BMHTimeZone tz = BMHTimeZone.getTimeZoneByID(calendar.getTimeZone()
                .getID());
        return tz.getShortDisplayName();
    }

    @Override
    public boolean isSkipped(String value) {
        return false;
    }
}