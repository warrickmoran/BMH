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
package com.raytheon.uf.viz.bmh.ui.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.raytheon.uf.viz.bmh.data.BmhUtils;
import com.raytheon.uf.viz.bmh.ui.dialogs.BMHLauncherDlg;
import com.raytheon.uf.viz.bmh.ui.dialogs.DlgInfo;

/**
 * Action to bring up BHH Menu dialog.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 18, 2014            mpduff     Initial creation
 * Jul 15, 2014  #3387     lvenable   Updated to bring dialog to top if it already exists.
 * Nov 07, 2014  #3413     rferrel    Added user authorization check.
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class BmhAction extends AbstractHandler {

    /** BMH launcher dialog. */
    private BMHLauncherDlg bmhLauncher;

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();

        if (bmhLauncher == null || bmhLauncher.isDisposed()) {
            if (BmhUtils.isAuthorized(shell, DlgInfo.BMH_MENU)) {
                bmhLauncher = new BMHLauncherDlg(shell);
                bmhLauncher.open();
            }
        } else {
            bmhLauncher.bringToTop();
        }
        return null;
    }
}
