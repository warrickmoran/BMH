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
package com.raytheon.uf.viz.bmh.voice;

import java.util.List;

import com.raytheon.uf.common.bmh.datamodel.language.Language;
import com.raytheon.uf.common.bmh.datamodel.language.TtsVoice;
import com.raytheon.uf.common.bmh.request.TtsVoiceRequest;
import com.raytheon.uf.common.bmh.request.TtsVoiceResponse;
import com.raytheon.uf.common.bmh.request.TtsVoiceRequest.TtsVoiceAction;
import com.raytheon.uf.viz.bmh.data.BmhUtils;

/**
 * Data manager for the {@link TtsVoice} data.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 12, 2014  3803      bkowal      Initial creation
 * Dec 16, 2014  3618      bkowal      Added {@link #getIdentifiers()},
 *                                     {@link #getVoiceById(int)}, and
 *                                     {@link #saveTtsVoice(TtsVoice)}.
 * Jan 13, 2015  3809      bkowal      Added {@link #getIdentfiersForLanguage(Language)}.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class VoiceDataManager {
    public List<TtsVoice> getAllVoices() throws Exception {
        TtsVoiceRequest voiceRequest = new TtsVoiceRequest();
        voiceRequest.setAction(TtsVoiceAction.AllVoices);
        TtsVoiceResponse voiceResponse = (TtsVoiceResponse) BmhUtils
                .sendRequest(voiceRequest);

        return voiceResponse.getTtsVoiceList();
    }

    public List<TtsVoice> getIdentifiers() throws Exception {
        TtsVoiceRequest voiceRequest = new TtsVoiceRequest();
        voiceRequest.setAction(TtsVoiceAction.VoiceIdentifiers);
        TtsVoiceResponse voiceResponse = (TtsVoiceResponse) BmhUtils
                .sendRequest(voiceRequest);

        return voiceResponse.getTtsVoiceList();
    }

    public List<TtsVoice> getIdentfiersForLanguage(Language language)
            throws Exception {
        TtsVoiceRequest voiceRequest = new TtsVoiceRequest();
        voiceRequest.setAction(TtsVoiceAction.VoiceIdentifiersForLanguage);
        voiceRequest.setLanguage(language);
        TtsVoiceResponse voiceResponse = (TtsVoiceResponse) BmhUtils
                .sendRequest(voiceRequest);

        return voiceResponse.getTtsVoiceList();
    }

    public TtsVoice getVoiceById(int voiceNumber) throws Exception {
        TtsVoiceRequest voiceRequest = new TtsVoiceRequest();
        voiceRequest.setAction(TtsVoiceAction.GetById);
        voiceRequest.setVoiceNumber(voiceNumber);
        TtsVoiceResponse voiceResponse = (TtsVoiceResponse) BmhUtils
                .sendRequest(voiceRequest);

        if (voiceResponse.getTtsVoiceList() == null
                || voiceResponse.getTtsVoiceList().isEmpty()) {
            return null;
        }

        return voiceResponse.getTtsVoiceList().get(0);
    }

    public void saveTtsVoice(TtsVoice voice) throws Exception {
        TtsVoiceRequest voiceRequest = new TtsVoiceRequest();
        voiceRequest.setAction(TtsVoiceAction.UpdateVoice);
        voiceRequest.setVoice(voice);

        BmhUtils.sendRequest(voiceRequest);
    }
}