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
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

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
 * Jul 07, 2014  #3360     lvenable    Initial creation
 * Jul 17, 2014   3120     mpduff      Added showMessageBox convenience method.
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

    /**
     * Show a message box to the user and get the results if there are any.
     * 
     * @param comp
     *            Parent composite
     * @param style
     *            Message box style
     * @param title
     *            Title bar text
     * @param message
     *            Message to display
     * @return id of the button selected to close the dialog
     */
    public static int showMessageBox(Shell shell, int style, String title,
            String message) {
        MessageBox mb = new MessageBox(shell, style);
        mb.setText(title);
        mb.setMessage(message);
        return mb.open();
    }

    // TODO Remove this method
    public static void notImplemented(Shell shell) {
        showMessageBox(shell, SWT.ICON_INFORMATION, "Not Implemented",
                "This function is not yet implemented");
    }
}
