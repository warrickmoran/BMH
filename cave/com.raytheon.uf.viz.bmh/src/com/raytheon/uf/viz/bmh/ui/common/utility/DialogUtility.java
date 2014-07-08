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
package com.raytheon.uf.viz.bmh.ui.common.utility;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * 
 * Class with convenience methods used by dialogs.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 7, 2014  #3360      lvenable     Initial creation
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */
public class DialogUtility {

    /**
     * Add a separator to a composite/shell. For use with the GridLayout only.
     * 
     * @param comp
     *            Composite.
     * @param orientation
     *            Separator orientation.
     */
    public static void addSeparator(Composite comp, int orientation) {
        GridData gd;

        if (orientation == SWT.HORIZONTAL) {
            gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        } else {
            gd = new GridData(SWT.DEFAULT, SWT.FILL, false, true);
        }

        Label sepLbl = new Label(comp, SWT.SEPARATOR | orientation);
        sepLbl.setLayoutData(gd);
    }
}
