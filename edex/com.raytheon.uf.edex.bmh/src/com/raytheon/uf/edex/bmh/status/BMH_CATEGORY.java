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

import java.util.Map;
import java.util.HashMap;

/**
 * An enum representing different types of events that can occur within the BMH
 * system. Not all defined events correspond to an error condition.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 16, 2014            bkowal     Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public enum BMH_CATEGORY {
    /* Indicates a successful operation. */
    SUCCESS(0),
    /*
     * Specific to the TTS Components: indicates that additional data can be
     * read from the TTS Server.
     */
    TTS_CONTINUE(1),
    /*
     * Specific to the TTS Components: used to indicate that a TTS operation has
     * failed due to a software or API issue (ex: invalid input).
     */
    TTS_SOFTWARE_ERROR(2),
    /*
     * Specific to the TTS Components: used to indicate that a TTS operation has
     * failed due to a system or server error outside the jurisdiction of the
     * TTS Software and/or TTS components (ex: disk full, networking problems,
     * etc.)
     */
    TTS_SYSTEM_ERROR(3),
    /*
     * Specific to the TTS Components: an error that cannot be recovered from.
     */
    TTS_FATAL_ERROR(4),
    /*
     * Specific to the TTS Components: used to indicate that the TTS components
     * were not configured correctly.
     */
    TTS_CONFIGURATION_ERROR(5),
    /*
     * Indicates that a thread has been interrupted while performing some
     * action.
     */
    INTERRUPTED(1000),
    /* Indicates a failed operation with an unknown or unclear cause. */
    UNKNOWN(9999);

    private static final Map<Integer, BMH_CATEGORY> lookupMap;

    static {
        lookupMap = new HashMap<Integer, BMH_CATEGORY>();
        for (BMH_CATEGORY category : BMH_CATEGORY.values()) {
            lookupMap.put(category.getCode(), category);
        }
    }

    private int code;

    private BMH_CATEGORY(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    public static BMH_CATEGORY lookup(int code) {
        return lookupMap.get(code);
    }
}