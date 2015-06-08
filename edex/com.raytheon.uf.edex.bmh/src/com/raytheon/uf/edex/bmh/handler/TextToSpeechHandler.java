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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.raytheon.uf.common.bmh.BMHVoice;
import com.raytheon.uf.common.bmh.TTSConstants.TTS_FORMAT;
import com.raytheon.uf.common.bmh.TTSConstants.TTS_RETURN_VALUE;
import com.raytheon.uf.common.bmh.datamodel.language.TtsVoice;
import com.raytheon.uf.common.bmh.notify.config.VoiceConfigNotification;
import com.raytheon.uf.common.bmh.request.TextToSpeechRequest;
import com.raytheon.uf.common.bmh.schemas.ssml.SSMLDocument;
import com.raytheon.uf.common.bmh.schemas.ssml.SpeechRateFormatter;
import com.raytheon.uf.edex.bmh.dao.TtsVoiceDao;
import com.raytheon.uf.edex.bmh.tts.TTSManager;
import com.raytheon.uf.edex.bmh.tts.TTSReturn;
import com.raytheon.uf.edex.bmh.tts.TTSSynthesisFactory;
import com.raytheon.uf.edex.bmh.xformer.MessageTransformer;
import com.raytheon.uf.edex.bmh.xformer.data.ITextTransformation;

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
 * Aug 20, 2014    3538    bkowal      Use TTS Connection Pooling.
 * Aug 25, 2014    3538    bkowal      Update to use the new TTS Synthesis
 *                                     mechanism. Most of the interaction is
 *                                     now hidden from the client.
 * Oct 2, 2014     3642    bkowal      Use the tts synthesis timeout provided
 *                                     in the request.
 * Oct 13, 2014    3413    rferrel     Implement User roles.
 * Oct 26, 2014    3759    bkowal      Update to support practice mode.
 * Jun 08, 2015    4403    bkowal      Transform text prior to synthesis when the
 *                                     associated flag has been set.
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class TextToSpeechHandler extends
        AbstractBMHServerRequestHandler<TextToSpeechRequest> {
    private final TTSManager ttsManager;

    private final TTSManager practiceTTSManager;

    private final MessageTransformer messageTransformer;

    private final MessageTransformer practiceMessageTransformer;

    private final ConcurrentMap<Integer, TtsVoice> voiceIdentifierMap = new ConcurrentHashMap<>(
            BMHVoice.values().length, 1.0f);

    private final ConcurrentMap<Integer, TtsVoice> practiceVoiceIdentifierMap = new ConcurrentHashMap<>(
            BMHVoice.values().length, 1.0f);

    public TextToSpeechHandler(TTSManager ttsManager,
            TTSManager practiceTTSManager,
            MessageTransformer messageTransformer,
            MessageTransformer practiceMessageTransformer) {
        this.ttsManager = ttsManager;
        this.practiceTTSManager = practiceTTSManager;
        this.messageTransformer = messageTransformer;
        this.practiceMessageTransformer = practiceMessageTransformer;
    }

    @Override
    public Object handleRequest(TextToSpeechRequest request) throws Exception {
        String content = request.getContent();
        if (request.isTransform()) {
            content = this.transformContent(request);
        }

        final TTSSynthesisFactory ttsSynthesisFactory = (request
                .isOperational()) ? this.ttsManager.getSynthesisFactory()
                : this.practiceTTSManager.getSynthesisFactory();
        TTSReturn output = ttsSynthesisFactory.synthesize(content,
                request.getVoice(), TTS_FORMAT.TTS_FORMAT_MULAW,
                request.getTimeout());
        TTS_RETURN_VALUE status = output.getReturnValue();

        request.setStatus(status.getDescription());
        if (status == TTS_RETURN_VALUE.TTS_RESULT_SUCCESS) {
            request.setByteData(output.getVoiceData());
        } else {
            request.setByteData(null);
        }

        return request;
    }

    private String transformContent(TextToSpeechRequest request)
            throws Exception {
        /*
         * Acquire the associated {@link TtsVoice}.
         */
        TtsVoice voice = this.lookupVoice(request.getVoice(),
                request.isOperational());
        if (voice == null) {
            throw new Exception(
                    "Failed to retrieve the voice associated with id: "
                            + request.getVoice() + ".");
        }

        final MessageTransformer messageTransformer = (request.isOperational()) ? this.messageTransformer
                : this.practiceMessageTransformer;
        List<ITextTransformation> textTransformations = messageTransformer
                .mergeDictionaries(voice.getLanguage(), null,
                        voice.getDictionary());
        SSMLDocument ssmlDocument = this.messageTransformer
                .applyTransformations(this.messageTransformer
                        .formatText(request.getContent()), SpeechRateFormatter
                        .formatSpeechRate(SpeechRateFormatter.DEFAULT_RATE),
                        voice.getLanguage(), textTransformations);
        return ssmlDocument.toSSML();
    }

    private TtsVoice lookupVoice(final int id, boolean operational) {
        ConcurrentMap<Integer, TtsVoice> voiceIdMap = (operational) ? this.voiceIdentifierMap
                : this.practiceVoiceIdentifierMap;

        /*
         * The {@link TtsVoice} is already available in the cache.
         */
        if (voiceIdMap.containsKey(id)) {
            return voiceIdMap.get(id);
        }

        /*
         * Attempt to retrieve the {@link TtsVoice} from the database.
         */
        final TtsVoiceDao dao = new TtsVoiceDao(operational);
        TtsVoice voice = dao.getByID(id);
        if (voice != null) {
            voiceIdMap.put(id, voice);
        }
        return voice;
    }

    public void voiceUpdated(VoiceConfigNotification notification) {
        this.voiceIdentifierMap.remove(notification.getId());
    }

    public void voiceUpdatedPractice(VoiceConfigNotification notification) {
        this.practiceVoiceIdentifierMap.remove(notification.getId());
    }
}