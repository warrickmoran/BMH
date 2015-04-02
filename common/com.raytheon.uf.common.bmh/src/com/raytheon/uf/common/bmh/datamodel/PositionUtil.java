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
package com.raytheon.uf.common.bmh.datamodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Helper methods dealing with Collections of PositionOrdered objects.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 20, 2015 4248       rjpeter     Initial creation
 * 
 * </pre>
 * 
 * @author rjpeter
 * @version 1.0
 */

public class PositionUtil {

    /**
     * Removes holes and updates positions accordingly with the set of objects.
     * 
     * @param vals
     */
    public static void updatePositions(Set<? extends PositionOrdered> vals) {
        updatePositions(order(vals));
    }

    /**
     * Removes holes and updates positions accordingly within the list.
     * 
     * @param vals
     */
    public static void updatePositions(List<? extends PositionOrdered> vals) {
        int count = 0;

        if (vals != null) {
            for (PositionOrdered obj : vals) {
                obj.setPosition(count++);
            }
        }
    }

    /**
     * Returns the collection in position order.
     * 
     * @param vals
     * @return
     */
    public static <T extends PositionOrdered> List<T> order(Collection<T> vals) {
        if (vals == null) {
            return Collections.emptyList();
        }

        List<T> rval = new ArrayList<>(vals);
        Collections.sort(rval, new PositionComparator());
        return rval;
    }
}
