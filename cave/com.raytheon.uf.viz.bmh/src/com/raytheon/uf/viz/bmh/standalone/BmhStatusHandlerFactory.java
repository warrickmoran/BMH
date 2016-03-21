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
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.raytheon.uf.common.logback.appender.AbstractJmsAppender;
import com.raytheon.uf.common.status.AbstractHandlerFactory;
import com.raytheon.uf.common.status.FilterPatternContainer;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.IUFStatusHandlerFactory;
import com.raytheon.uf.common.status.StatusHandler;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.common.status.slf4j.Slf4JBridge;
import com.raytheon.uf.common.status.slf4j.UFMarkers;

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
 * Jun 22, 2015  4570     bkowal      Updated to use the new slf4j CAVE logging.
 * Mar 21, 2016  5489     bkowal      Verify that the AlertViz appender was successfully
 *                                    retrieved from logback configuration.
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class BmhStatusHandlerFactory extends AbstractHandlerFactory {

    private static final String CATEGORY = "WORKSTATION";

    private final Logger logger = (Logger) LoggerFactory
            .getLogger("CaveLogger");

    private final AbstractJmsAppender alertvizAppender = (AbstractJmsAppender) logger
            .getAppender("AsyncAlertvizAppender");

    private static final StatusHandler instance = new StatusHandler(
            StatusHandler.class.getPackage().getName(), CATEGORY, CATEGORY);

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
        IStatus status = new Status(getSeverity(priority), pluginId, message,
                throwable);

        /*
         * com.raytheon.uf.viz.core.status.VizStatusHandlerFactory.log(Priority,
         * String, String, String, String, Throwable) also creates a {@link
         * Marker} using the same method. It may be a better idea to add a
         * protected method to {@link AbstractHandlerFactory} so all required
         * information could be passed in to create a {@link Marker}.
         */
        Marker m = MarkerFactory.getDetachedMarker("bmh");
        if (pluginId != null) {
            m.add(UFMarkers.getPluginMarker(pluginId));
        }

        if (category != null) {
            m.add(UFMarkers.getCategoryMarker(category));
        }
        if (source != null) {
            m.add(UFMarkers.getSourceMarker(source));
        }
        m.add(UFMarkers.getUFPriorityMarker(priority));

        Slf4JBridge.logToSLF4J(logger, priority, m, message, throwable);

        if ((this.alertvizAppender == null || this.alertvizAppender
                .isConnected() == false) && priority != Priority.VERBOSE) {
            /*
             * It is unlikely that messages will be logged to AlertViz, display
             * a notification dialog.
             */
            logToDialog(status);
        }
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