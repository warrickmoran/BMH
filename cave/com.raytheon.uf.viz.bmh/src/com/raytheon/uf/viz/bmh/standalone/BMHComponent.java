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
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.application.component.IStandaloneComponent;
import com.raytheon.uf.viz.bmh.ui.dialogs.BMHLauncherDlg;
import com.raytheon.uf.viz.core.notification.jobs.NotificationManagerJob;
import com.raytheon.viz.core.mode.CAVEMode;
import com.raytheon.viz.ui.dialogs.CaveSWTDialog;

/**
 * Component for starting BMH in Standalone mode.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Feb 05, 2015  3743     bsteffen    Initial creation
 * Feb 16, 2015  4168     bsteffen    Set CAVEMode.
 * Mar 11, 2015  4266     bsteffen    Better error/ufstatus handling.
 * Jan 26, 2016  5054     randerso    Change dialogs to be parented by display
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
        CAVEMode.performStartupDuties();
        CaveSWTDialog dialog = null;
        BmhConnectivityInitializer connectivity = new BmhConnectivityInitializer();
        connectivity.run();
        if (connectivity.isOnlyBroadcastLive()) {
            dialog = new StandaloneBroadcastLiveDlg(display);
        } else {
            NotificationManagerJob.connect();
            dialog = new BMHLauncherDlg(display);
        }
        UFStatus.setHandlerFactory(new BmhStatusHandlerFactory());
        dialog.open();
        applicationRunning();
        while (!dialog.isDisposed()) {
            try {
                if (!display.readAndDispatch()) {
                    display.sleep();
                }
            } catch (Throwable e) {
                UFStatus.getHandler().error(
                        "Unexpected Exception in BMH Dialog.", e);
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
