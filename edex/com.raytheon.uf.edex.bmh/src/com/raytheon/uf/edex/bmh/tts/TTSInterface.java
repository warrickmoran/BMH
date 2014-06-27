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

import java.io.IOException;

import voiceware.libttsapi;

import com.raytheon.uf.edex.bmh.tts.TTSConstants.TTS_FORMAT;
import com.raytheon.uf.edex.bmh.tts.TTSConstants.TTS_RETURN_VALUE;

/**
 * Provides a somewhat generic interface to a TTS API. Note: This class is NOT
 * thread safe.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 11, 2014 3228       bkowal      Initial creation
 * Jun 26, 2014 3302       bkowal      Eliminated the use of *DataRecord
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class TTSInterface {

    /* TTS NeoSpeech API Constants */
    // Whether or not the first frame of the voice output is being requested
    private static final int BFIRST = 1;

    // Whether the resulting voice buffer is to be sent in one frame (vs.
    // multiple frames)
    private static final int BALL = 1;

    private final String ttsServer;

    private final int ttsPort;

    private final int ttsConnectionTimeout;

    private libttsapi ttsapi;

    /**
     * 
     */
    public TTSInterface(final String ttsServer, final int ttsPort,
            int ttsConnectionTimeout) {
        this.ttsServer = ttsServer;
        this.ttsPort = ttsPort;
        this.ttsConnectionTimeout = ttsConnectionTimeout;
        this.ttsapi = this.getAPI();
    }

    /**
     * Attempt to connect to the TTS Server to verify that it is running and
     * available. This method does not care about the result of the
     * Text-to-Speech Transformation.
     * 
     * @return the result of the attempt.
     */
    public TTS_RETURN_VALUE validateTTSIsAvailable() throws IOException {
        final String testMessage = "TEST";

        int returnCode = this.ttsapi.ttsRequestBuffer(this.ttsServer,
                this.ttsPort, testMessage, 0, 0, BFIRST, BALL);

        return TTS_RETURN_VALUE.lookup(returnCode);
    }

    /**
     * Attempt to transform the provided SSML information to audio via the TTS
     * API currently in use.
     * 
     * @param ssml
     *            the text to transform
     * @param voice
     *            the voice identifier
     * @param ttsFormat
     *            the format identifier
     * @param output
     *            pass-by-reference object to store generated information in
     * @param getFirstFrame
     *            { true = start data retrieval at the first frame; false =
     *            retrieve the next frame that is available }
     * @return a status identifier
     * @throws IOException
     *             if the request to the TTS Server fails due to an I/O error.
     */
    public TTSReturn transformSSMLToAudio(final String ssml, final int voice,
            final TTS_FORMAT ttsFormat, boolean getFirstFrame)
            throws IOException {
        int firstFrame = getFirstFrame ? libttsapi.TRUE : libttsapi.FALSE;

        int returnCode = this.ttsapi.ttsRequestBufferSSMLEx(this.ttsServer,
                this.ttsPort, ssml, voice, ttsFormat.getCode(), 0, 0, 0, 0,
                firstFrame);
        TTS_RETURN_VALUE returnValue = TTS_RETURN_VALUE.lookup(returnCode);
        TTSReturn ttsReturn = new TTSReturn(returnValue);
        /*
         * If the transformation was successful, copy the result to the output
         * data structure.
         */
        if (returnValue == TTS_RETURN_VALUE.TTS_RESULT_CONTINUE
                || returnValue == TTS_RETURN_VALUE.TTS_RESULT_SUCCESS) {
            ttsReturn.setVoiceData(ttsapi.szVoiceData);
        }

        return ttsReturn;
    }

    /**
     * Returns an instance of the TTS API that is currently being used. Also
     * sets any needed attributes on the API.
     * 
     * @return an instance of the API
     */
    private libttsapi getAPI() {
        libttsapi ttsapi = new libttsapi();
        ttsapi.SetConnectTimeout(this.ttsConnectionTimeout);

        return ttsapi;
    }

    /**
     * Returns the name of the TTS Server.
     * 
     * @return the name of the TTS Server.
     */
    public String getTtsServer() {
        return ttsServer;
    }

    /**
     * Returns the port that the TTS Server is listening on.
     * 
     * @return the port that the TTS Server is listening on.
     */
    public int getTtsPort() {
        return ttsPort;
    }

    /**
     * Returns the amount of time before a connection to the TTS server will
     * timeout (in milliseconds).
     * 
     * @return the amount of time before a connection to the TTS Server will
     *         timeout (in milliseconds).
     */
    public int getTtsConnectionTimeout() {
        return ttsConnectionTimeout;
    }
}