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

/**
 * Constants used for message activity logging.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 10, 2014 3651       bkowal      Initial creation
 * Mar 25, 2015 4290       bsteffen    Switch to global replacement.
 * May 19, 2015 4429       rferrel     Added Transformer messages.
 * May 21, 2015  4429      rjpeter     Added additional activities.
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class MessageActivity {

    /**
     * Constant Formats
     */
    private static final String MESSAGE_FMT = "Message %s";

    private static final String TRANSMITTER_GRP_FMT = "Transmitter Group %s";

    private static final String PLAYLIST_FMT = "Playlist %s";

    private static final String TONES_FMT = "%s Tones";

    private static final String MESSAGE_EXPIRES_FMT = "(Message Expires: %s)";

    private static final String PLAYLIST_EXPIRES_FMT = "(Playlist Expires: %s)";

    /**
     * Activity-Specific Formats
     */
    protected static final String BROADCAST_FORMAT = MESSAGE_FMT
            + " has been broadcast " + MESSAGE_EXPIRES_FMT + ".";

    protected static final String REPLACEMENT_FORMAT = MESSAGE_FMT
            + " has replaced " + MESSAGE_FMT + " " + MESSAGE_EXPIRES_FMT + ".";

    protected static final String PLAYLIST_MSG_FORMAT = MESSAGE_FMT
            + " was successfully created and will be broadcast to "
            + TRANSMITTER_GRP_FMT + " " + MESSAGE_EXPIRES_FMT + ".";

    protected static final String ACTIVATION_FORMAT = MESSAGE_FMT
            + " has been activated and will be broadcast to "
            + TRANSMITTER_GRP_FMT + " " + MESSAGE_EXPIRES_FMT + ".";

    protected static final String TONES_FORMAT = TONES_FMT
            + " were broadcast for " + MESSAGE_FMT + " " + MESSAGE_EXPIRES_FMT
            + ".";

    protected static final String SAME_TONES_FORMAT = TONES_FMT
            + " were broadcast for " + MESSAGE_FMT + " (SAME Encoding: %s) "
            + MESSAGE_EXPIRES_FMT + ".";

    protected static final String TRIGGER_FORMAT = MESSAGE_FMT
            + " has triggered a playlist switch to " + PLAYLIST_FMT + " for "
            + TRANSMITTER_GRP_FMT + " " + PLAYLIST_EXPIRES_FMT + ".";

    protected static final String PLAYLIST_FORMAT = PLAYLIST_FMT
            + " has been updated/created " + PLAYLIST_EXPIRES_FMT + ".";

    protected static final String TRANSFORM_START_FORMAT = MESSAGE_FMT
            + " starting message transform.";

    protected static final String TRANSFORM_END_FORMAT = MESSAGE_FMT
            + " message successfully transformed";

    protected static final String TTS_START_FORMAT = MESSAGE_FMT
            + " starting text to speech";

    protected static final String TTS_SUCCESS_FORMAT = MESSAGE_FMT
            + " generated audio file %s.  Audio length: %s";

    protected static final String TTS_END_FORMAT = MESSAGE_FMT
            + " text to speech complete";

    protected static final String VALIDATION_START_FORMAT = MESSAGE_FMT
            + " starting validation";

    protected static final String VALIDATION_END_FORMAT = MESSAGE_FMT
            + " validation status is: transmissionStatus=%s, ldadStatus=%s";

    /**
     * Enum identifying the types of message activities that need to be logged.
     * 
     * <pre>
     * 
     * SOFTWARE HISTORY
     * 
     * Date         Ticket#    Engineer    Description
     * ------------ ---------- ----------- --------------------------
     * Dec 11, 2014 3651       bkowal      Initial creation
     * 
     * </pre>
     * 
     * @author bkowal
     * @version 1.0
     */
    public enum MESSAGE_ACTIVITY {
        /* A message has been broadcast */
        BROADCAST(BROADCAST_FORMAT),
        /* A message has been replaced */
        REPLACEMENT(REPLACEMENT_FORMAT),
        /* A message has been created */
        PLAYLIST_MSG(PLAYLIST_MSG_FORMAT),
        /* A message has been activated */
        ACTIVATION(ACTIVATION_FORMAT),
        /* Alert or End Tones have been broadcast */
        TONE(TONES_FORMAT),
        /* Same Tones have been broadcast */
        SAME_TONE(SAME_TONES_FORMAT),
        /* Automatic Playlist switch occurred due to a trigger message */
        TRIGGER(TRIGGER_FORMAT),
        /* A playlist was created / updated */
        PLAYLIST(PLAYLIST_FORMAT),
        /* An input message starts transform. */
        TRANSFORM_START(TRANSFORM_START_FORMAT),
        /* An input message ends transform. */
        TRANSFORM_END(TRANSFORM_END_FORMAT),
        /* An input message starts transform. */
        TTS_START(TTS_START_FORMAT),
        /* An input message ends transform. */
        TTS_SUCCESS(TTS_SUCCESS_FORMAT),
        /* An input message ends transform. */
        TTS_END(TTS_END_FORMAT),
        /* An input message starts validation */
        VALIDATION_START(VALIDATION_START_FORMAT),
        /* An input message finishes validation */
        VALIDATION_END(VALIDATION_END_FORMAT);

        private final String logMsgFormat;

        private MESSAGE_ACTIVITY(final String logMsgFormat) {
            this.logMsgFormat = logMsgFormat;
        }

        public String getLogMsgFormat() {
            return this.logMsgFormat;
        }
    }

    /**
     * Constructor
     */
    protected MessageActivity() {
    }
}