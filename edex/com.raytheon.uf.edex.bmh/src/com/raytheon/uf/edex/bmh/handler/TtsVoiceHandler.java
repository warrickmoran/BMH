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

import java.util.List;

import com.raytheon.uf.common.bmh.datamodel.language.TtsVoice;
import com.raytheon.uf.common.bmh.request.TtsVoiceRequest;
import com.raytheon.uf.common.bmh.request.TtsVoiceResponse;
import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.edex.bmh.dao.TtsVoiceDao;

/**
 * TTS Voice Server Request Handler
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 11, 2014 #3490      lvenable     Initial creation
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */

public class TtsVoiceHandler implements IRequestHandler<TtsVoiceRequest> {

    @Override
    public Object handleRequest(TtsVoiceRequest request) throws Exception {

        TtsVoiceResponse ttsVoiceResponce = new TtsVoiceResponse();

        switch (request.getAction()) {
        case AllVoices:
            ttsVoiceResponce = getVoices();
            break;
        default:
            break;
        }

        return ttsVoiceResponce;
    }

    /**
     * Get a list of the voices.
     * 
     * @return TtsVoice response.
     */
    private TtsVoiceResponse getVoices() {
        TtsVoiceDao dao = new TtsVoiceDao();
        TtsVoiceResponse response = new TtsVoiceResponse();

        List<TtsVoice> voiceList = dao.getVoices();
        response.setTtsVoiceList(voiceList);

        return response;
    }
}
