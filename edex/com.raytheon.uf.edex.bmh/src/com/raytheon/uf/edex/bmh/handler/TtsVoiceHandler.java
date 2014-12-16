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

import java.util.ArrayList;
import java.util.List;

import com.raytheon.uf.common.bmh.datamodel.language.TtsVoice;
import com.raytheon.uf.common.bmh.request.TtsVoiceRequest;
import com.raytheon.uf.common.bmh.request.TtsVoiceResponse;
import com.raytheon.uf.edex.bmh.dao.TtsVoiceDao;

/**
 * Handles any requests to get or modify the state of {@link TtsVoice}s
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Aug 11, 2014  3490     lvenable    Initial creation
 * Oct 07, 2014  3687     bsteffen    Handle non-operational requests.
 * Oct 13, 2014  3413     rferrel     Implement User roles.
 * Dec 16, 2014  3618     bkowal      Added {@link #getIdentifiers(TtsVoiceRequest)},
 *                                    {@link #getVoiceById(TtsVoiceRequest)}, and
 *                                    {@link #updateVoice(TtsVoiceRequest)}.
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */

public class TtsVoiceHandler extends
        AbstractBMHServerRequestHandler<TtsVoiceRequest> {

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.common.serialization.comm.IRequestHandler#handleRequest
     * (com.raytheon.uf.common.serialization.comm.IServerRequest)
     */
    @Override
    public Object handleRequest(TtsVoiceRequest request) throws Exception {
        TtsVoiceResponse ttsVoiceResponse = new TtsVoiceResponse();

        switch (request.getAction()) {
        case AllVoices:
            ttsVoiceResponse = getVoices(request);
            break;
        case VoiceIdentifiers:
            ttsVoiceResponse = getIdentifiers(request);
            break;
        case GetById:
            ttsVoiceResponse = getVoiceById(request);
            break;
        case UpdateVoice:
            ttsVoiceResponse = updateVoice(request);
            break;
        default:
            throw new UnsupportedOperationException(this.getClass()
                    .getSimpleName()
                    + " cannot handle action "
                    + request.getAction());
        }

        return ttsVoiceResponse;
    }

    /**
     * Get a list of the voices.
     * 
     * @return TtsVoice response.
     */
    private TtsVoiceResponse getVoices(TtsVoiceRequest request) {
        TtsVoiceDao dao = new TtsVoiceDao(request.isOperational());
        TtsVoiceResponse response = new TtsVoiceResponse();

        List<TtsVoice> voiceList = dao.getVoices();
        response.setTtsVoiceList(voiceList);

        return response;
    }

    /**
     * Get a list of {@link TtsVoice} identifiers.
     * 
     * @param request
     *            {@link TtsVoiceRequest} indicating which database to access.
     * @return a {@link TtsVoiceResponse}.
     */
    private TtsVoiceResponse getIdentifiers(TtsVoiceRequest request) {
        TtsVoiceDao dao = new TtsVoiceDao(request.isOperational());
        TtsVoiceResponse response = new TtsVoiceResponse();

        List<TtsVoice> voiceList = dao.getVoiceIdentifiers();
        response.setTtsVoiceList(voiceList);

        return response;
    }

    /**
     * Retrieves a {@link TtsVoice} by id.
     * 
     * @param request
     *            {@link TtsVoiceRequest} indicating which database to access
     *            and includes the id of the {@link TtsVoice} to retrieve.
     * @return a {@link TtsVoiceResponse}.
     */
    private TtsVoiceResponse getVoiceById(TtsVoiceRequest request) {
        TtsVoiceDao dao = new TtsVoiceDao(request.isOperational());
        TtsVoiceResponse response = new TtsVoiceResponse();

        TtsVoice voice = dao.getByID(request.getVoiceNumber());
        if (voice != null) {
            List<TtsVoice> voices = new ArrayList<>(1);
            voices.add(voice);
            response.setTtsVoiceList(voices);
        }

        return response;
    }

    /**
     * Updates the specified {@link TtsVoice}.
     * 
     * @param request
     *            {@link TtsVoiceRequest} indicating which database to access
     *            and includes the {@link TtsVoice} to update.
     * @return a {@link TtsVoiceResponse}.
     */
    private TtsVoiceResponse updateVoice(TtsVoiceRequest request) {
        TtsVoiceDao dao = new TtsVoiceDao(request.isOperational());
        TtsVoiceResponse response = new TtsVoiceResponse();

        dao.saveOrUpdate(request.getVoice());

        return response;
    }
}