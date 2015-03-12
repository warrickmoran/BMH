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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.statushandlers.StatusManager;

import com.raytheon.uf.common.message.StatusMessage;
import com.raytheon.uf.common.status.AbstractHandlerFactory;
import com.raytheon.uf.common.status.FilterPatternContainer;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.IUFStatusHandlerFactory;
import com.raytheon.uf.common.status.StatusHandler;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.viz.alertviz.AlertVizClient;
import com.raytheon.uf.viz.alertviz.AlertvizException;
import com.raytheon.uf.viz.bmh.Activator;
import com.raytheon.uf.viz.core.localization.LocalizationConstants;
import com.raytheon.uf.viz.core.localization.LocalizationManager;

/**
 * 
 * {@link IUFStatusHandlerFactory} to use when running the BMH component
 * standalone. Will attempt to route all logging to an alert server if the
 * connection can be established. To handle Worst Case Scenerios, if any errors
 * occur communicating with the alert server, this will fall back to popping up
 * a status dialog. All messages are also logged to the {@link StatusManager} so
 * they can appear in console logs.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Mar 11, 2015  4266     bsteffen    Initial creation
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class BmhStatusHandlerFactory extends AbstractHandlerFactory {

    private static final String CATEGORY = "WORKSTATION";

    private static final StatusHandler instance = new StatusHandler(
            StatusHandler.class.getPackage().getName(), CATEGORY, CATEGORY);

    private AlertVizClient alertClient;

    private boolean useAlertViz = true;

    public BmhStatusHandlerFactory() {
        super(CATEGORY);
    }

    @Override
    protected FilterPatternContainer createSourceContainer() {
        return FilterPatternContainer.createDefault();
    }

    @Override
    protected void log(Priority priority, String pluginId, String category,
            String source, String message, Throwable throwable) {
        IStatus status = new Status(getSeverity(priority), pluginId,
                message, throwable);
        boolean success = logToAlertViz(priority, pluginId, category, source,
                message, throwable);
        if (!success && priority != Priority.VERBOSE) {
            logToDialog(status);
        }
        /* Send to console, and/or anything else that cares. */
        StatusManager.getManager().handle(status);
    }

    /**
     * convert from ufstatus concept of priority to eclipse concept of severity.
     */
    protected int getSeverity(Priority priority) {
        switch (priority) {
        case CRITICAL:
        case SIGNIFICANT:
            return IStatus.ERROR;
        case PROBLEM:
            return IStatus.WARNING;
        case EVENTA:
        case EVENTB:
        case VERBOSE:
        default:
            return IStatus.INFO;
        }
    }

    protected boolean logToAlertViz(Priority priority, String pluginId,
            String category, String source, String message, Throwable throwable) {
        if (alertClient == null) {
            String alertServer = LocalizationManager.getInstance()
                    .getLocalizationStore()
                    .getString(LocalizationConstants.P_ALERT_SERVER);
            alertClient = new AlertVizClient(alertServer, false);
            try {
                alertClient.start();
            } catch (AlertvizException e) {
                useAlertViz = false;
                IStatus status = new Status(IStatus.INFO, Activator.PLUGIN_ID,
                        "Unable to start alertviz.", e);
                StatusManager.getManager().handle(status);
            }
        }
        if (useAlertViz) {
            try {
                StatusMessage statusMessage = new StatusMessage(source,
                        category, priority, pluginId, message, throwable);
                AlertVizClient.sendMessage(statusMessage);
            } catch (AlertvizException e) {
                useAlertViz = false;
                IStatus status = new Status(IStatus.INFO, Activator.PLUGIN_ID,
                        "Unable to send to alertviz.", e);
                StatusManager.getManager().handle(status);
            }
        }
        return useAlertViz;
    }

    private void logToDialog(final IStatus status) {
        final Display display = Display.getDefault();
        display.asyncExec(new Runnable() {

            @Override
            public void run() {
                ErrorDialog.openError(new Shell(Display.getDefault()),
                        "BMH Status", null, status);
            }
        });
    }

    @Override
    public IUFStatusHandler getInstance() {
        return instance;
    }

    @Override
    protected IUFStatusHandler createMonitorInstance(String pluginId,
            String monitorSource) {
        throw new IllegalStateException(
                "BMH does not support handling the status of monitors.");
    }

    @Override
    public IUFStatusHandler createInstance(AbstractHandlerFactory factory,
            String pluginId, String category) {
        return createInstance(pluginId, category, getSource(null, pluginId));
    }

    @Override
    protected IUFStatusHandler createInstance(String pluginId, String category,
            String source) {
        return new StatusHandler(pluginId, category, source);

    }

}
