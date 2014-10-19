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
package com.raytheon.uf.viz.bmh.ui.dialogs.dac;

import java.util.Comparator;

import com.raytheon.uf.common.bmh.datamodel.dac.Dac;

/**
 * Comparator for Dac sorting by name.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 18, 2014    3699    mpduff      Initial creation
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class DacNameComparator implements Comparator<Dac> {

    @Override
    public int compare(Dac d1, Dac d2) {
        return d1.getName().compareTo(d2.getName());
    }
}
