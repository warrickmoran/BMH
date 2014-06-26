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

import java.util.HashMap;
import java.util.Map;

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
 * Jun 16, 2014            bkowal      Initial creation
 * Jun 26, 2014 3302       bkowal      Added categories for the Message Transformer.
 * Jul 01, 2014 3283       bsteffen    Added categories for message validation.
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
     * Specific to the input message parser. used to indicate that the parser
     * cannot understand the format of the input.
     */
    INPUT_MESSAGE_PARSE_ERROR(6),
    /*
     * Specific to the message validation component: used to indicate that an
     * unexpected error occured during validation.
     */
    MESSAGE_VALIDATION_ERROR(7),
    /*
     * Specific to the message validation component: used to indicate that a
     * massage has failed validation.
     */
    MESSAGE_VALIDATION_FAILED(8),
    /*
     * Specific to the Message Transformation Components; used to indicate that
     * an afos id was encountered without an associated message type.
     */
    XFORM_MISSING_MSG_TYPE(9),
    /*
     * Specific to the Message Transformation Components; used to indicate that
     * a transmitter group, language combination was encountered that did not
     * have an associated dictionary.
     */
    XFORM_MISSING_DICTIONARY(10),
    /*
     * Specific to the message validation component: used to indicate that a
     * massage area is not in the configuration.
     */
    MESSAGE_AREA_UNCONFIGURED(11),
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