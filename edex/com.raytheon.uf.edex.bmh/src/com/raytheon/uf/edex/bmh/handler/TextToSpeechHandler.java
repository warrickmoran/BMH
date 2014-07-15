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
package com.raytheon.uf.edex.bmh.handler;

import java.io.ByteArrayOutputStream;

import com.raytheon.uf.common.bmh.TTSConstants.TTS_FORMAT;
import com.raytheon.uf.common.bmh.TTSConstants.TTS_RETURN_VALUE;
import com.raytheon.uf.common.bmh.request.TextToSpeechRequest;
import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.edex.bmh.tts.TTSInterface;
import com.raytheon.uf.edex.bmh.tts.TTSManager;
import com.raytheon.uf.edex.bmh.tts.TTSReturn;

/**
 * Handle the CAVE text to speech requests.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 16, 2014    3355    mpduff      Initial creation
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class TextToSpeechHandler implements
        IRequestHandler<TextToSpeechRequest> {
    private final TTSManager ttsManager;

    public TextToSpeechHandler(TTSManager ttsManager) {
        this.ttsManager = ttsManager;
    }

    @Override
    public Object handleRequest(TextToSpeechRequest request) throws Exception {
        TTSInterface ttsInterface = ttsManager.getInterface();
        TTSReturn output = ttsInterface.transformSSMLToAudio(
                request.getPhoneme(), request.getVoice(),
                TTS_FORMAT.TTS_FORMAT_MULAW, true);
        TTS_RETURN_VALUE status = output.getReturnValue();

        if (status == TTS_RETURN_VALUE.TTS_RESULT_SUCCESS) {
            request.setByteData(output.getVoiceData());
        } else if (status == TTS_RETURN_VALUE.TTS_RESULT_CONTINUE) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(output.getVoiceData());
            while (status == TTS_RETURN_VALUE.TTS_RESULT_CONTINUE) {
                output = ttsInterface.transformSSMLToAudio(
                        request.getPhoneme(), request.getVoice(),
                        TTS_FORMAT.TTS_FORMAT_MULAW, false);
                outputStream.write(output.getVoiceData());
                status = output.getReturnValue();
            }
            request.setByteData(outputStream.toByteArray());
        }

        request.setStatus(status.getDescription());

        return request;
    }
}