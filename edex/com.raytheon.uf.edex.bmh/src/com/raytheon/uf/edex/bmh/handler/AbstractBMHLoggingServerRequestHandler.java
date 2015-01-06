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
package com.raytheon.uf.edex.bmh.handler;

import com.raytheon.uf.common.bmh.request.AbstractBMHServerRequest;
import com.raytheon.uf.edex.bmh.msg.logging.IMessageLogger;

/**
 * Extension of {@link AbstractBMHServerRequest} that allows for the storage and
 * retrieval of the Spring-instantiated {@link IMessageLogger}s associated with
 * operational and practice modes.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 6, 2015  3651       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public abstract class AbstractBMHLoggingServerRequestHandler<T extends AbstractBMHServerRequest>
        extends AbstractBMHServerRequestHandler<T> {

    protected final IMessageLogger opMessageLogger;

    protected final IMessageLogger pracMessageLogger;

    /**
     * Constructor
     * 
     * @param opMessageLogger
     *            the {@link IMessageLogger} for operational mode
     * @param pracMessageLogger
     *            the {@link IMessageLogger} for practice mode
     */
    protected AbstractBMHLoggingServerRequestHandler(
            final IMessageLogger opMessageLogger,
            final IMessageLogger pracMessageLogger) {
        this.opMessageLogger = opMessageLogger;
        this.pracMessageLogger = pracMessageLogger;
    }

    /**
     * Returns the {@link IMessageLogger} associated with the specified mode of
     * operation.
     * 
     * @param request
     *            a {@link AbstractBMHServerRequest} that identifies the current
     *            mode of operation.
     * @return the {@link IMessageLogger} associated with the specified mode of
     *         operation
     */
    protected IMessageLogger getMessageLogger(AbstractBMHServerRequest request) {
        return request.isOperational() ? this.opMessageLogger
                : this.pracMessageLogger;
    }
}