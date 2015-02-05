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
package com.raytheon.uf.viz.bmh.standalone;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import com.raytheon.uf.viz.application.component.IStandaloneComponent;
import com.raytheon.uf.viz.bmh.ui.dialogs.BMHLauncherDlg;
import com.raytheon.uf.viz.core.notification.jobs.NotificationManagerJob;
import com.raytheon.viz.ui.dialogs.CaveSWTDialog;

/**
 * Component for starting BMH in Standalone mode.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#   Engineer    Description
 * ------------- --------- ----------- --------------------------
 * Feb 05, 2015  3743      bsteffen    Initial creation
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class BMHComponent implements IStandaloneComponent {

    @Override
    public Object startComponent(String componentName) throws Exception {
        Display display = PlatformUI.createDisplay();
        CaveSWTDialog dialog = null;
        BmhConnectivityInitializer connectivity = new BmhConnectivityInitializer();
        connectivity.run();
        if (connectivity.isOnlyBroadcastLive()) {
            dialog = new StandaloneBroadcastLiveDlg(new Shell(display));
        } else {
            NotificationManagerJob.connect();
            dialog = new BMHLauncherDlg(new Shell(display));
        }
        dialog.open();
        applicationRunning();
        while (!dialog.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        return IApplication.EXIT_OK;
    }

    /**
     * Tell Eclipse that the application is up. Otherwise the splash screen
     * stays up forever.
     */
    private void applicationRunning() {
        BundleContext bundleContext = FrameworkUtil.getBundle(this.getClass())
                .getBundleContext();
        ServiceReference<IApplicationContext> ref = bundleContext
                .getServiceReference(IApplicationContext.class);
        bundleContext.getService(ref).applicationRunning();
        bundleContext.ungetService(ref);
    }
}
