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

import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.viz.ui.dialogs.CaveSWTDialog;

/**
 * Abstract dialog that provides addition feature for the BMH dialogs and how
 * they are handled.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 15, 2014  #3387     lvenable    Initial creation
 * Jun 05, 2015  4490      rjpeter     Dialog tracked via BMHLauncherDlg.
 * Jan 26, 2016  5054      randerso    Allow dialog to be parented by display
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */
public abstract class AbstractBMHDialog extends CaveSWTDialog {

    /** Flag indicating if the dialog should be closed. */
    private boolean forceCloseFlag = false;

    /**
     * Constructor.
     * 
     * @param parentShell
     *            Parent shell
     * @param style
     *            SWT style.
     * @param caveStyle
     *            Cave style.
     */
    protected AbstractBMHDialog(Shell parentShell, int style, int caveStyle) {
        super(parentShell, style, caveStyle);
    }

    /**
     * Constructor.
     * 
     * @param display
     * @param style
     *            SWT style.
     * @param caveStyle
     *            Cave style.
     */
    protected AbstractBMHDialog(Display display, int style, int caveStyle) {
        super(display, style, caveStyle);
    }

    /**
     * Add a close listener so action can be taken to prevent the dialog from
     * closing if it shouldn't.
     */
    @Override
    protected void initializeComponents(Shell shell) {
        shell.addShellListener(new ShellAdapter() {
            @Override
            public void shellClosed(ShellEvent e) {
                if (forceCloseFlag) {
                    return;
                }

                /*
                 * This is not used for force closing this is for those dialogs
                 * that may need to have confirmation from the user before the
                 * dialog closes.
                 */
                e.doit = okToClose();
            }
        });
    }

    /**
     * This will force close the dialog by-passing any checks made by the
     * okToClose method.
     */
    public final void forceClose() {
        forceCloseFlag = true;
        close();
    }

    /**
     * Method to override that will allow a dialog to perform checks before the
     * dialog is closed. This will allow the code to prevent accidental closing
     * of a dialog.
     * 
     * @return True to close the dialog, false to leave it open.
     */
    public abstract boolean okToClose();
}
