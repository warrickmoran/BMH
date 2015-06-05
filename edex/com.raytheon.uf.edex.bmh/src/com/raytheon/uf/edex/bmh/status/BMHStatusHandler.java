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
package com.raytheon.uf.edex.bmh.status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.raytheon.uf.common.bmh.BMH_CATEGORY;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.edex.bmh.status.BMH_ACTION;
import com.raytheon.uf.edex.core.EDEXUtil;

/**
 * Based on {@link com.raytheon.uf.common.status.IUFStatusHandler}. Used by BMH
 * components to log events and/or send a notification to AlertViz.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 16, 2014 3291       bkowal      Initial creation
 * Jun 01, 2015 4490       bkowal      Use the defined alertviz category.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class BMHStatusHandler implements IBMHStatusHandler {
    private static final Marker FATAL = MarkerFactory.getMarker("FATAL");

    private static final String PLUGIN_NAME = "com.raytheon.uf.edex.bmh";

    private static final String SOURCE = "BMH";

    private Logger logger;

    public synchronized static BMHStatusHandler getInstance(Class<?> clazz) {
        return new BMHStatusHandler(clazz);
    }

    /**
     * 
     */
    protected BMHStatusHandler(Class<?> clazz) {
        this.logger = LoggerFactory.getLogger(clazz);
    }

    @Override
    public void debug(String message) {
        this.handle(Priority.DEBUG, message);
    }

    @Override
    public void debug(BMH_CATEGORY category, String message) {
        this.handle(Priority.DEBUG, category, message);
    }

    @Override
    public void info(String message) {
        this.handle(Priority.INFO, message);
    }

    @Override
    public void info(BMH_CATEGORY category, String message) {
        this.handle(Priority.INFO, category, message);
    }

    @Override
    public void warn(BMH_CATEGORY category, String message) {
        this.handle(Priority.WARN, category, message);
    }

    @Override
    public void error(BMH_CATEGORY category, String message) {
        this.handle(Priority.ERROR, category, message);
    }

    @Override
    public void error(BMH_CATEGORY category, String message, Throwable throwable) {
        this.handle(Priority.ERROR, category, message, throwable);
    }

    @Override
    public void fatal(BMH_CATEGORY category, String message) {
        this.handle(Priority.FATAL, category, message);
    }

    @Override
    public void fatal(BMH_CATEGORY category, String message, Throwable throwable) {
        this.handle(Priority.FATAL, category, message, throwable);
    }

    private void handle(Priority priority, String message) {
        this.handle(priority, null, message);
    }

    private void handle(Priority priority, BMH_CATEGORY category, String message) {
        this.handle(priority, category, message, null);
    }

    private void handle(Priority priority, BMH_CATEGORY category,
            String message, Throwable throwable) {
        // msg has been null if someone does e.getLocalizedMessage()
        // and it is null which causes null pointer exception
        message = String.valueOf(message);
        if (category != null) {
            StringBuilder sb = new StringBuilder(message.length() + 64);
            sb.append(category.toString());

            sb.append(": ");
            sb.append(SOURCE);

            sb.append(" - ");
            sb.append(message);
            message = sb.toString();
        }

        /* Determine what actions to take based on the priority and the category */
        BMHNotificationAction notificationAction = BMHNotificationManager
                .getInstance().getAction(category, priority);
        if (notificationAction == null) {
            // No actions defined, take the default action.
            this.defaultAction(priority, category, message, throwable);
            return;
        }
        for (BMH_ACTION action : notificationAction.getActions()) {
            switch (action) {
            case ACTION_LOG:
                this.logAction(priority, message, throwable);
                break;
            case ACTION_ALERTVIZ:
            case ACTION_ALERTVIZ_AUDIO:
                this.alertVizNotifyAction(priority, category, message,
                        throwable, notificationAction.getAudioFile());
                break;
            case ACTION_DEFAULT:
                // Do Nothing.
                break;
            }
        }
    }

    private void logAction(Priority priority, String message,
            Throwable throwable) {
        switch (priority) {
        case CRITICAL:
            if (throwable != null) {
                this.logger.error(FATAL, message, throwable);
            } else {
                this.logger.error(FATAL, message);
            }
            break;
        case SIGNIFICANT:
            if (throwable != null) {
                this.logger.error(message, throwable);
            } else {
                this.logger.error(message);
            }
            break;
        case PROBLEM:
            this.logger.warn(message);
            break;
        case EVENTA:
        case EVENTB:
            this.logger.info(message);
            break;
        case VERBOSE:
            this.logger.debug(message);
            break;
        }
    }

    private void alertVizNotifyAction(Priority priority, BMH_CATEGORY category,
            String message, Throwable throwable, String audioFile) {
        String details = null;
        if (throwable != null && throwable.getLocalizedMessage() != null) {
            details = throwable.getLocalizedMessage();
        }
        EDEXUtil.sendMessageAlertViz(priority, PLUGIN_NAME, SOURCE,
                category.getAlertVizCategory(), message, details, audioFile);
    }

    private void defaultAction(Priority priority, BMH_CATEGORY category,
            String message, Throwable throwable) {
        switch (priority) {
        case CRITICAL:
        case SIGNIFICANT:
            this.alertVizNotifyAction(priority, category, message, throwable,
                    null);
        default:
            /* the default is to log everything. */
            this.logAction(priority, message, throwable);
        }
    }
}