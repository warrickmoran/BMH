package com.raytheon.uf.common.bmh;

import com.raytheon.uf.common.bmh.diff.LoggerUtils;
import com.raytheon.uf.common.bmh.request.AbstractBMHServerRequest;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

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

/**
 * Common methods for handlers logging user changes to BMH database.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 15, 2014 #3636      rferrel     Initial creation
 * 
 * </pre>
 * 
 * @author rferrel
 * @version 1.0
 */

public class BMHLoggerUtils {
    private final static String SRV_LOGGER_NAME = "BMHSrvRequestLogger";

    private final static String PRACTICE_SRV_LOGGER_NAME = "PracticeBMHSrvRequestLogger";

    /**
     * Get the desired BMH Service Request Logger.
     * 
     * @param isOperational
     * @return logger
     */
    public static IUFStatusHandler getSrvLogger(AbstractBMHServerRequest request) {
        if (request.isOperational()) {
            return UFStatus.getNamedHandler(SRV_LOGGER_NAME);
        }
        return UFStatus.getNamedHandler(PRACTICE_SRV_LOGGER_NAME);
    }

    /**
     * 
     * @param request
     * @return user
     */
    public static String getUser(AbstractBMHServerRequest request) {
        return request.getUser().uniqueId().toString();
    }

    private BMHLoggerUtils() {
    }

    public static <T> void logSave(AbstractBMHServerRequest request,
            String user, T oldObj, T newObj) {
        LoggerUtils.logSave(getSrvLogger(request), user, oldObj, newObj);
    }

    public static <T> void logDelete(AbstractBMHServerRequest request,
            String user, T delObj) {
        LoggerUtils.logDelete(getSrvLogger(request), user, delObj);
    }
}
