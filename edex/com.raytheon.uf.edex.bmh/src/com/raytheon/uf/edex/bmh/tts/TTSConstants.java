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
package com.raytheon.uf.edex.bmh.tts;

import java.util.Map;
import java.util.HashMap;

import com.raytheon.uf.edex.bmh.status.BMH_CATEGORY;

import voiceware.libttsapi;

/**
 * A collection of constants that are primarily utilized by the TTS Manager
 * component.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 10, 2014 3228       bkowal      Initial creation
 * Jun 17, 2014 3291       bkowal      Added a method to translate the TTS return values to BMH categories.
 * 
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public final class TTSConstants {

    public static enum TTS_RETURN_VALUE {
        TTS_RESULT_SUCCESS(libttsapi.TTS_RESULT_SUCCESS, "Success"), TTS_RESULT_CONTINUE(
                libttsapi.TTS_RESULT_CONTINUE,
                "Success; however, additional frame buffers must be retrieved."), TTS_RESULT_ERROR(
                libttsapi.TTS_RESULT_ERROR,
                "Problem encountered during synthesis. Please check server logs."), TTS_HOSTNAME_ERROR(
                libttsapi.TTS_HOSTNAME_ERROR,
                "An invalid IP address has been provided. An IP address is invalid when the format is incorrect."), TTS_SOCKET_ERROR(
                libttsapi.TTS_SOCKET_ERROR,
                "The socket() used by the API has returned an error."), TTS_CONNECT_ERROR(
                libttsapi.TTS_CONNECT_ERROR,
                "The connect() function used by the API has returned an error."), TTS_READWRITE_ERROR(
                libttsapi.TTS_READWRITE_ERROR,
                "An error was encountered when sending data to or receiving data from the server."), TTS_MEMORY_ERROR(
                libttsapi.TTS_MEMORY_ERROR, "TTS Memory Error"), TTS_TEXT_ERROR(
                libttsapi.TTS_TEXT_ERROR,
                "An invalid text length was specified."), TTS_VOICEFORMAT_ERROR(
                libttsapi.TTS_VOICEFORMAT_ERROR,
                "An invalid format was specified."), TTS_PARAM_ERROR(
                libttsapi.TTS_PARAM_ERROR,
                "Invalid values have been assigned to the 'szSaveFile' or 'szSaveDir' parameters."), TTS_SPEAKER_ERROR(
                libttsapi.TTS_SPEAKER_ERROR,
                "An invalid or unavailable voice was specified."), TTS_DISK_ERROR(
                libttsapi.TTS_DISK_ERROR, "Disk I/O problem was encountered."), TTS_SSML_ERROR(
                libttsapi.TTS_SSML_ERROR,
                "Grammatical errors were encountered in the requested SSML texts."), TTS_ENC_ERROR(
                libttsapi.TTS_ENC_ERROR,
                "Encryption and/or Decryption problems were encountered."), TTS_ABNORMAL_ERROR(
                libttsapi.TTS_ABNORMAL_ERROR,
                "Incorrect usage of the VoiceText(TM) Server protocol has occurred."), TTS_MAX_ERROR(
                libttsapi.TTS_MAX_ERROR, "No available threads."), TTS_UNKNOWN_ERROR(
                libttsapi.TTS_UNKNOWN_ERROR, "Unknown Error");

        private static final Map<Integer, TTS_RETURN_VALUE> lookupMap;

        static {
            lookupMap = new HashMap<Integer, TTS_RETURN_VALUE>();

            for (TTS_RETURN_VALUE ttsReturnValue : TTS_RETURN_VALUE.values()) {
                lookupMap.put(ttsReturnValue.getCode(), ttsReturnValue);
            }
        }

        private int code;

        private String description;

        private TTS_RETURN_VALUE(int code, String description) {
            this.code = code;
            this.description = description;
        }

        public int getCode() {
            return this.code;
        }

        public String getDescription() {
            return this.description;
        }

        public static TTS_RETURN_VALUE lookup(int code) {
            if (lookupMap.containsKey(code) == false) {
                return TTS_RETURN_VALUE.TTS_UNKNOWN_ERROR;
            }

            return lookupMap.get(code);
        }

        public BMH_CATEGORY getAssociatedBMHCategory() {
            switch (this) {
            case TTS_RESULT_SUCCESS:
                return BMH_CATEGORY.SUCCESS;
            case TTS_RESULT_CONTINUE:
                return BMH_CATEGORY.TTS_CONTINUE;
            case TTS_RESULT_ERROR:
            case TTS_READWRITE_ERROR:
            case TTS_MEMORY_ERROR:
            case TTS_TEXT_ERROR:
            case TTS_VOICEFORMAT_ERROR:
            case TTS_PARAM_ERROR:
            case TTS_SPEAKER_ERROR:
            case TTS_SSML_ERROR:
            case TTS_ENC_ERROR:
            case TTS_ABNORMAL_ERROR:
            case TTS_MAX_ERROR:
                return BMH_CATEGORY.TTS_SOFTWARE_ERROR;
            case TTS_SOCKET_ERROR:
            case TTS_CONNECT_ERROR:
            case TTS_DISK_ERROR:
                return BMH_CATEGORY.TTS_SYSTEM_ERROR;
            case TTS_HOSTNAME_ERROR:
                return BMH_CATEGORY.TTS_FATAL_ERROR;
            default:
                return BMH_CATEGORY.UNKNOWN;
            }
        }
    }

    /*
     * Note: this is only a subset of the full set of available formats.
     * However, the functions that the TTSInterface currently exposes only
     * supports the subset of formats defined in this enum.
     */
    public static enum TTS_FORMAT {
        TTS_FORMAT_DEFAULT(libttsapi.FORMAT_DEFAULT, "Default"), TTS_FORMAT_PCM(
                libttsapi.FORMAT_PCM, "16bit linear PCM"), TTS_FORMAT_MULAW(
                libttsapi.FORMAT_MULAW, "8bit Mu-law PCM"), TTS_FORMAT_ALAW(
                libttsapi.FORMAT_ALAW, "8bit A-law PCM");

        private int code;

        private String description;

        private TTS_FORMAT(int code, String description) {
            this.code = code;
            this.description = description;
        }

        public int getCode() {
            return this.code;
        }

        public String getDescription() {
            return this.description;
        }
    }

    /**
     * 
     */
    protected TTSConstants() {
    }
}