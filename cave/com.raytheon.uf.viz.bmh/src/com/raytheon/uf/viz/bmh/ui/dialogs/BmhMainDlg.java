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
package com.raytheon.uf.viz.bmh.ui.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.viz.bmh.ui.dialogs.systemstatus.SystemStatusDlg;
import com.raytheon.viz.ui.dialogs.CaveSWTDialog;

/**
 * Initial implementation/place holder
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 18, 2014            mpduff     Initial creation
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class BmhMainDlg extends CaveSWTDialog {

    public BmhMainDlg(Shell parentShell) {
        super(parentShell, SWT.DIALOG_TRIM, CAVE.PERSPECTIVE_INDEPENDENT);
    }

    @Override
    protected void initializeComponents(Shell shell) {
        // Launch the system status dlg
        SystemStatusDlg statusDlg = new SystemStatusDlg(shell);
        statusDlg.open();

        Button b = new Button(shell, SWT.PUSH);
        b.setText("BMH...");
        b.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                System.out.println("Launch BMH");
            }
        });

    }
}
