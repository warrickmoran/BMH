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
package com.raytheon.uf.edex.bmh.msg.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Message logger implementation that allows for logging via the
 * declared {@link DefaultMessageLogger#ACTIVITY_LOGGER} and
 * {@link DefaultMessageLogger#ERROR_LOGGER} loggers.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 8, 2014  3651       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class DefaultMessageLogger implements IMessageLogger {

    private static final String ACTIVITY_LOGGER = "MessageActivityLogger";

    private static final String ERROR_LOGGER = "MessageErrorLogger";

    private final Logger activityLogger;

    private final Logger errorLogger;

    private static final DefaultMessageLogger instance = new DefaultMessageLogger(
            ACTIVITY_LOGGER, ERROR_LOGGER);

    /**
     * 
     */
    protected DefaultMessageLogger(final String activityLoggerName,
            final String errorLoggerName) {
        this.activityLogger = LoggerFactory.getLogger(activityLoggerName);

        this.errorLogger = LoggerFactory.getLogger(errorLoggerName);
    }

    public static DefaultMessageLogger getInstance() {
        return instance;
    }

    @Override
    public void logActivity() {
        // TODO: Implement.
        this.activityLogger.info("TEST TEST TEST");
    }

    @Override
    public void logError() {
        // TODO: Implement.
        this.errorLogger.error("TEST TEST TEST");
    }
}