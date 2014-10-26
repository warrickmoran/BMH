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
package com.raytheon.uf.viz.bmh.ui.dialogs.broadcastcycle;

import java.util.Comparator;

/**
 * Compares Broadcast Cycle Table entries by transmit time.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 21, 2014            mpduff      Initial creation
 * Oct 26, 2014   3750     mpduff      Null check the incoming objects.
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class BroadcastCycleTableEntryComparator implements
        Comparator<BroadcastCycleTableDataEntry> {

    @Override
    public int compare(BroadcastCycleTableDataEntry o1,
            BroadcastCycleTableDataEntry o2) {
        if (o1 == null && o2 == null) {
            return 0;
        }
        if (o1 == null) {
            return 1;
        }
        if (o2 == null) {
            return -1;
        }

        return o1.getTransmitTime().compareTo(o2.getTransmitTime());
    }
}
