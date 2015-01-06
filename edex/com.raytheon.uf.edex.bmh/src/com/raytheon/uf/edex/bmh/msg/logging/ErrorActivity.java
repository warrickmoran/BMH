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

import com.raytheon.uf.edex.bmh.msg.validator.InputMessageParser;
import com.raytheon.uf.edex.bmh.msg.validator.InputMessageValidator;
import com.raytheon.uf.edex.bmh.msg.validator.LdadValidator;
import com.raytheon.uf.edex.bmh.msg.validator.TransmissionValidator;
import com.raytheon.uf.edex.bmh.xformer.MessageTransformer;
import com.raytheon.uf.edex.bmh.tts.TTSManager;
import com.raytheon.uf.edex.bmh.playlist.PlaylistManager;
import com.raytheon.uf.edex.bmh.ldad.LdadDisseminator;

/**
 * Constants used for error message logging.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 11, 2014 3651       bkowal      Initial creation
 * Jan 05, 2015 3651       bkowal      Added {@link BMH_ACTIVITY#AUDIO_BROADCAST}.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class ErrorActivity {

    public static final String LOG_FORMAT = "%s encountered a problem while %s for Message %s on host %s";

    /**
     * Enum identifying the BMH Component that encountered the error.
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
    public enum BMH_COMPONENT {
        /** The BMH {@link InputMessageParser} **/
        INPUT_MESSAGE_PARSER("Input Message Parser"),
        /** The BMH {@link InputMessageValidator} **/
        INPUT_MESSAGE_VALIDATOR("Input Message Validator"),
        /** The BMH {@link LdadValidator} **/
        LDAD_VALIDATOR("Ldad Validator"),
        /** The BMH {@link TransmissionValidator} **/
        TRANSMISSION_VALIDATOR("Transmission Validator"),
        /** The BMH {@link MessageTransformer} **/
        MESSAGE_TRANSFORMER("Message Transformer"),
        /** The BMH {@link TTSManager} **/
        TTS_MANAGER("TTS Manager"),
        /** The BMH {@link PlaylistManager} **/
        PLAYLIST_MANAGER("Playlist Manager"),
        /** The BMH {@link LdadDisseminator} **/
        LDAD_DISSEMINATOR("Ldad Disseminator"),
        /** The BMH Comms Manager process **/
        COMMS_MANAGER("Comms Manager"),
        /** The BMH Dac Transmit process **/
        DAC_TRANSMIT("Dac Transmit");

        private final String prettyPrintComponent;

        private BMH_COMPONENT(final String prettyPrintComponent) {
            this.prettyPrintComponent = prettyPrintComponent;
        }

        public String prettyPrint() {
            return this.prettyPrintComponent;
        }
    }

    /**
     * Enum identifying the action that was occurring when the error was
     * encountered.
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
    public enum BMH_ACTIVITY {
        /** Synthesizing audio via TTS **/
        AUDIO_SYNTHESIS,
        /** Amplifying or attenuating audio **/
        AUDIO_ALTERATION,
        /** Writing an audio file **/
        AUDIO_WRITE,
        /** Reading an audio file for broadcast **/
        AUDIO_READ,
        /** Validating an input message that has been parsed **/
        MESSAGE_VALIDATION,
        /** Parsing an input file **/
        MESSAGE_PARSING,
        /** Generating the SSML required for synthesis **/
        SSML_GENERATION,
        /** Disseminating audio associated with ldad configuration **/
        SCP_DISSEMINATION,
        /** Writing the playlist xml **/
        PLAYLIST_WRITE,
        /** Reading the playlist xml **/
        PLAYLIST_READ,
        /** Generation of tones for a message **/
        TONE_GENERATION,
        /** Retrieval of message type records from the database **/
        DATA_RETRIEVAL,
        /** Storage of message type records to the database **/
        DATA_STORAGE,
        /** Broadcast of audio to the dac. **/
        AUDIO_BROADCAST
    }

    /**
     * Constructor
     */
    protected ErrorActivity() {
    }
}