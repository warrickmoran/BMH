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
package com.raytheon.uf.common.bmh;

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
 * Jul 7, 2014  3302       bkowal      Added a category for failed SSML generation.
 * Jul 08, 2014 3355       mpduff      Moved to common
 * Jul 10, 2014 3285       bsteffen    Added a category for playlist manager.
 * Jul 17, 2014 3175       rjpeter     Added legacy parsing category.
 * Oct 2, 2014  3642       bkowal      Added STATIC_MSG_ERROR.
 * Nov 19, 2014 3385       bkowal      Added {@link BMH_CATEGORY#LDAD_ERROR}
 * Nov 26, 2014 3821       bsteffen    Add some dac and comms categories
 * Jan 19, 2015 4002       bkowal      Added {@link #DAC_TRANSMIT_BROADCAST_DELAY}.
 * Feb 17, 2015 4136       bkowal      Added {@link #AUDIO_TRUNCATED}.
 * Feb 18, 2015 4136       bkowal      Added {@link #EXCESSIVE_FILE_SIZE}.
 * Mar 31, 2015 4339       bkowal      Added {@link #SAME_TRUNCATION}.
 * May 20, 2015 4430       rjpeter     Added alertVizCategory.
 * Jun 01, 2015 4490       bkowal      Added {@link #SAME_AREA_TRUNCATION}, {@link #SAME_DURATION_TRUNCATION},
 *                                     and {@link #WTCH_OR_WRN_NOT_BROADCAST}.
 * Sep 24, 2015 4924       bkowal      Added Validation Failure specific categories.
 * Nov 16, 2015 5127       rjpeter     Added MESSAGE_ARCHIVE_FAILED.
 * Nov 23, 2015 5113       bkowal      Added {@link #DAC_SYNC_ISSUE} and {@link #DAC_SYNC_VERIFY_FAIL}.
 * Feb 04, 2016 5308       rjpeter     Removed MESSAGE_VALIDATION_DUPLICATE.
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public enum BMH_CATEGORY {
    /* Indicates a successful operation. */
    SUCCESS(0, "BMH"),
    /*
     * Specific to the TTS Components: indicates that additional data can be
     * read from the TTS Server.
     */
    TTS_CONTINUE(1, "TTS"),
    /*
     * Specific to the TTS Components: used to indicate that a TTS operation has
     * failed due to a software or API issue (ex: invalid input).
     */
    TTS_SOFTWARE_ERROR(2, "TTS"),
    /*
     * Specific to the TTS Components: used to indicate that a TTS operation has
     * failed due to a system or server error outside the jurisdiction of the
     * TTS Software and/or TTS components (ex: disk full, networking problems,
     * etc.)
     */
    TTS_SYSTEM_ERROR(3, "TTS"),
    /*
     * Specific to the TTS Components: an error that cannot be recovered from.
     */
    TTS_FATAL_ERROR(4, "TTS"),
    /*
     * Specific to the TTS Components: used to indicate that the TTS components
     * were not configured correctly.
     */
    TTS_CONFIGURATION_ERROR(5, "TTS"),
    /*
     * Specific to the input message parser. used to indicate that the parser
     * cannot understand the format of the input.
     */
    INPUT_MESSAGE_PARSE_ERROR(6, "MESSAGE_PARSE"),
    /*
     * Specific to the message validation component: used to indicate that an
     * unexpected error occured during validation.
     */
    MESSAGE_VALIDATION_ERROR(7, "MESSAGE_VALIDATION"),
    /*
     * Specific to the message validation component: used to indicate that a
     * massage has failed validation.
     */
    MESSAGE_VALIDATION_FAILED(8, "MESSAGE_VALIDATION"),
    /*
     * Specific to the Message Transformation Components; used to indicate that
     * an afos id was encountered without an associated message type.
     */
    XFORM_MISSING_MSG_TYPE(9, "MESSAGE_TRANSFORM"),
    /*
     * Specific to the Message Transformation Components; used to indicate that
     * a transmitter group, language combination was encountered that did not
     * have an associated dictionary.
     */
    XFORM_MISSING_DICTIONARY(10, "MESSAGE_TRANSFORM"),
    /*
     * Specific to the message validation component: used to indicate that a
     * message area is not in the configuration.
     */
    MESSAGE_AREA_UNCONFIGURED(11, "MESSAGE_VALIDATION"),
    /*
     * Specific to the Message Transformation Components; used to indicate that
     * SSML Generation failed when attempting to transform a message using a
     * dictionary.
     */
    XFORM_SSML_GENERATION_FAILED(12, "MESSAGE_TRANSFORM"),
    /*
     * An error has occurred adding a message to a playlist.
     */
    PLAYLIST_MANAGER_ERROR(13, "PLAYLIST_MANAGER"),
    /*
     * An error has occurred while tracking the status of a transmitter.
     */
    TRANSMITTER_STATUS_ERROR(14, "TRANSMITTER"),
    /*
     * An error has occurred while configuring the comms manager.
     */
    COMMS_CONFIGURATOR_ERROR(15, "COMMS_MANAGER"),
    /*
     * An error has occurred during static message generation.
     */
    STATIC_MSG_ERROR(16, "STATIC_MSG"),

    COMMS_MANAGER_ERROR(17, "COMMS_MANAGER"),

    DAC_TRANSMIT_ERROR(18, "DAC_TRANSMIT"),

    DAC_TRANSMIT_SILENCE(19, "DAC_TRANSMIT"),
    /*
     * An error has occurred during ldad processing.
     */
    LDAD_ERROR(20, "LDAD"),
    /*
     * indicates that a warning or interrupt cannot be broadcast on a
     * transmitter due to an active broadcast live session.
     */
    DAC_TRANSMIT_BROADCAST_DELAY(21, "DAC_TRANSMIT"),
    /*
     * indicates that audio has been truncated because its duration exceeded the
     * maximum allowed duration.
     */
    AUDIO_TRUNCATED(22, "TTS"),
    /*
     * indicates that an incoming file has been discarded because its size was
     * greater than the maximum allowed size.
     */
    EXCESSIVE_FILE_SIZE(23, "MESSAGE_PARSE"),
    /*
     * indicates that a SAME Message has been truncated to two minutes during
     * the initial broadcast.
     */
    SAME_DURATION_TRUNCATION(25, "DAC_TRANSMIT"),
    /*
     * indicates that one or more areas have not been included in a SAME tone
     * because the maximum number of areas has been exceeded.
     */
    SAME_AREA_TRUNCATION(26, "PLAYLIST_MANAGER"),
    /*
     * indicates that a watch or warning message has expired before it could be
     * broadcast.
     */
    WTCH_OR_WRN_NOT_BROADCAST(27, "DAC_TRANSMIT"),
    /*
     * indicates that a message failed validation because it was already expired
     * upon arrival.
     */
    MESSAGE_VALIDATION_EXPIRED(28, "MESSAGE_VALIDATION"),
    /*
     * indicates that a message failed validation because it was not eligible
     * for playback on any of the existing transmitter groups due to the fact
     * that it was not found in any of the geographical areas recognized by the
     * existing transmitter groups.
     */
    MESSAGE_VALIDATION_UNPLAYABLE(29, "MESSAGE_VALIDATION"),
    /*
     * indicates that a message failed validation because an associated message
     * type was not found in the system.
     */
    MESSAGE_VALIDATION_UNDEFINED(30, "MESSAGE_VALIDATION"),
    /*
     * indicates that the message failed validation because the associated
     * message type is not in any suites associated with the transmitter groups
     * eligible for playback indicated by the geographical areas associated with
     * the message.
     */
    MESSAGE_VALIDATION_UNASSIGNED(31, "MESSAGE_VALIDATION"),

    /*
     * indicates that a message failed validation because it contained
     * unacceptable words.
     */
    MESSAGE_VALIDATION_UNACCEPTABLE(33, "MESSAGE_VALIDATION"),

    /*
     * indicates that a message failed to copy to the archive.
     */
    MESSAGE_ARCHIVE_FAILED(34, "MESSAGE_PARSE"),
    /*
     * indicates that a DAC and BMH {@link Dac} are out of sync.
     */
    DAC_SYNC_ISSUE(35, "BMH"),
    /*
     * indicates that an attempt to verify that BMH {@link Dac}s were in sync
     * with the DACs has failed.
     */
    DAC_SYNC_VERIFY_FAIL(36, "BMH"),
    /*
     * Specific to the legacy database import. Used to indicate an issue
     * occurred with the legacy database import.
     */
    LEGACY_DATABASE_IMPORT(500, "DATABASE_IMPORT"),
    /*
     * Indicates that a thread has been interrupted while performing some
     * action.
     */
    INTERRUPTED(1000, "BMH"),
    /* Indicates a failed operation with an unknown or unclear cause. */
    UNKNOWN(9999, "BMH");

    private static final Map<Integer, BMH_CATEGORY> lookupMap;

    static {
        lookupMap = new HashMap<Integer, BMH_CATEGORY>();
        for (BMH_CATEGORY category : BMH_CATEGORY.values()) {
            lookupMap.put(category.getCode(), category);
        }
    }

    private final int code;

    private final String alertVizCategory;

    private BMH_CATEGORY(int code, String alertVizCategory) {
        this.code = code;
        this.alertVizCategory = alertVizCategory;
    }

    public int getCode() {
        return this.code;
    }

    public String getAlertVizCategory() {
        return this.alertVizCategory;
    }

    public static BMH_CATEGORY lookup(int code) {
        return lookupMap.get(code);
    }
}