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

import java.util.List;

/**
 * Indicates that messages in a playlist could not be re-ordered based on mrd
 * follows rules due to a circular dependency.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 13, 2015 4484       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class CircularFollowsException extends Exception {

    private static final long serialVersionUID = -2495782665869055279L;

    /**
     * 
     */
    public CircularFollowsException(List<Long> followsPath) {
        super(buildCycleExceptionText(followsPath));
    }

    private static String buildCycleExceptionText(List<Long> followsPath) {
        StringBuilder sb = new StringBuilder("Found MRD Follows Cycle at: ");
        boolean first = true;
        for (Long id : followsPath) {
            if (first) {
                first = false;
            } else {
                sb.append(" -> ");
            }
            sb.append(Long.toString(id));
        }
        sb.append(".");

        return sb.toString();
    }
}