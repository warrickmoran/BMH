package com.raytheon.uf.common.bmh;

import java.util.Set;

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
    public static IUFStatusHandler getSrvLogger(boolean isOperational) {
        if (isOperational) {
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

    /**
     * Standard format for logging a field's old and new value.
     * 
     * @param sb
     * @param title
     *            - Field name
     * @param oldValue
     *            - Field's old value
     * @param newValue
     *            - Field's new value
     */
    public static void logFieldChange(StringBuilder sb, String title,
            Object oldValue, Object newValue) {
        String oldStr = oldValue.toString();
        String newStr = newValue.toString();
        sb.append(title);
        if (oldStr.matches("^[+-]{0,1}\\[.*$")) {
            sb.append(": ").append(oldStr).append(" | ").append(newStr)
                    .append(", ");
        } else {
            sb.append(": \"").append(oldStr).append("\" | \"").append(newStr)
                    .append("\", ");
        }
    }

    /**
     * Determine if the two sets are different.
     * 
     * @param set1
     * @param set2
     * @return true when sets are different
     */
    public static <T> boolean setsDiffer(Set<T> set1, Set<T> set2) {
        // Null and empty sets are the same.
        if (set1 == null) {
            return ((set2 != null) && (set2.size() > 0));
        }

        if (set2 == null) {
            return (set1.size() > 0);
        }

        if (set1.size() != set2.size()) {
            return true;
        }

        // containsAll doesn't always return true for 2 empty sets.
        if (set1.size() > 0) {
            return !set1.containsAll(set2);
        }

        return false;
    }

    public static String nullCheck(String value, String none) {
        if ((value == null) || (value.trim().length() == 0)) {
            return none;
        }
        return "\"" + value + "\"";
    }

    private BMHLoggerUtils() {
    }
}
