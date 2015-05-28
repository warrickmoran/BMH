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
package com.raytheon.uf.common.bmh.trace;

import java.util.Calendar;

import com.raytheon.uf.common.time.util.TimeUtil;

/**
 * Useful static methods for Traceable products.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 12, 2015 4429       rferrel     Initial creation
 * May 28, 2015 4429       rjpeter     Added createCurrentTraceId().
 * </pre>
 * 
 * @author rferrel
 * @version 1.0
 */

public class TraceableUtil {
    private TraceableUtil() {
    }

    /**
     * Obtain traceId.
     * 
     * @param traceable
     * @return traceId or No trace tag when no traceId
     */
    public static String getTraceId(ITraceable traceable) {
        if (hasTraceId(traceable)) {
            return traceable.getTraceId();
        }
        return "<No traceId>";
    }

    /**
     * Standard trace header for log messages.
     * 
     * @param traceable
     * @return traceHeader
     */
    public static String createTraceMsgHeader(ITraceable traceable) {
        return createTraceMsgHeader(getTraceId(traceable));
    }

    /**
     * Standard trace header for log messages.
     * 
     * @param traceId
     * @return traceHeader
     */
    public static String createTraceMsgHeader(String traceId) {
        if (traceId == null) {
            traceId = "<No traceId>";
        }
        return "traceId=" + traceId + ": ";
    }

    /**
     * Determine if there is a traceId
     * 
     * @param traceable
     * @return true when traceId available
     */
    public static boolean hasTraceId(ITraceable traceable) {
        return (traceable != null) && (traceable.getTraceId() != null)
                && !traceable.getTraceId().trim().isEmpty();
    }

    public static ITraceable createCurrentTraceId(String msg) {
        Calendar cal = TimeUtil.newGmtCalendar();
        return new Traceable(String.format(
                "%s_%2$tY-%2$tm-%2$td-%2$tk%2$tM.%2$tS", msg, cal));
    }
}
