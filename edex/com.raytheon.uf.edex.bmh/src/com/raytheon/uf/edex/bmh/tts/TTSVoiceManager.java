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

import com.raytheon.uf.common.bmh.BMHVoice;
import com.raytheon.uf.common.bmh.BMH_CATEGORY;
import com.raytheon.uf.common.bmh.TTSConstants.TTS_FORMAT;
import com.raytheon.uf.common.bmh.TTSConstants.TTS_RETURN_VALUE;
import com.raytheon.uf.common.bmh.TTSSynthesisException;
import com.raytheon.uf.common.bmh.datamodel.language.Language;
import com.raytheon.uf.edex.bmh.dao.TtsVoiceDao;
import com.raytheon.uf.edex.bmh.status.BMHStatusHandler;
import com.raytheon.uf.edex.bmh.status.IBMHStatusHandler;

/**
 * Tracks the allowed voices and used to determine if a voice can be registered
 * within the system.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 2, 2015  4175       bkowal      Initial creation
 * Mar 27, 2015  4315     rferrel      Added {@link #verifyLanguageAvailable(Language)}.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class TTSVoiceManager {

    private static final IBMHStatusHandler statusHandler = BMHStatusHandler
            .getInstance(TTSVoiceManager.class);

    private static final int MAX_AVAILABILITY_RETRIES = 5;

    private static final String ENGLISH_TEXT = "Available";

    private static final String SPANISH_TEXT = "Disponible";

    private final TtsVoiceDao ttsVoiceDao;

    private final TTSSynthesisFactory synthesisFactory;

    public TTSVoiceManager(final TtsVoiceDao ttsVoiceDao,
            final TTSSynthesisFactory synthesisFactory) {
        this.ttsVoiceDao = ttsVoiceDao;
        this.synthesisFactory = synthesisFactory;
    }

    public boolean verifyLanguageAvailable(Language language) {
        switch (language) {
        case ENGLISH:
            return verifyVoiceAvailability(BMHVoice.PAUL);
        case SPANISH:
            return verifyVoiceAvailability(BMHVoice.VIOLETA);

        default:
            return false;
        }
    }

    public boolean verifyVoiceAvailability(BMHVoice bmhVoice) {
        final String availabilityText = (bmhVoice.getLanguage() == Language.ENGLISH) ? ENGLISH_TEXT
                : SPANISH_TEXT;

        int attempt = 0;
        boolean retry = true;

        while (retry) {
            ++attempt;

            TTSReturn ttsReturn;
            try {
                ttsReturn = this.synthesisFactory.synthesize(availabilityText,
                        bmhVoice.getId(), TTS_FORMAT.TTS_FORMAT_MULAW);
            } catch (TTSSynthesisException e) {
                StringBuilder sb = new StringBuilder(
                        "Failed to determine if Voice ");
                sb.append(bmhVoice.name()).append(
                        " is available for synthesis.");

                statusHandler.error(BMH_CATEGORY.TTS_SOFTWARE_ERROR,
                        sb.toString(), e);
                return false;
            }

            if (ttsReturn.getReturnValue() == TTS_RETURN_VALUE.TTS_RESULT_SUCCESS) {
                /*
                 * The voice is available for synthesis.
                 */
                return true;
            } else if (ttsReturn.getReturnValue() == TTS_RETURN_VALUE.TTS_SPEAKER_ERROR) {
                /*
                 * The voice was not available for synthesis.
                 */
                return false;
            } else if (ttsReturn.getReturnValue() == TTS_RETURN_VALUE.TTS_MAX_ERROR) {
                /*
                 * The synthesizer did not have any available threads. Retry if
                 * the maximum number of retries have not been already
                 * exhausted.
                 */
                if (attempt <= MAX_AVAILABILITY_RETRIES) {
                    continue;
                }
            }

            /*
             * synthesis failed for a different reason and/or we have exhausted
             * all of our retry attempts.
             */
            retry = false;
        }

        return false;
    }

    public void determineSpanishSupport() {
        BMHVoice defaultSpanishVoice = BMHVoice.VIOLETA;

        /*
         * Does the default Spanish Voice currently exist in the database?
         */
        if (this.ttsVoiceDao.getByID(defaultSpanishVoice.getId()) != null) {
            return;
        }

        /*
         * The default Spanish Voice does not exist. Determine if the TTS Engine
         * supports it.
         */
        if (this.verifyVoiceAvailability(defaultSpanishVoice) == false) {
            /*
             * The Voice is not supported.
             */
            return;
        }

        /*
         * The TTS Engine supports the default Spanish Voice, create it.
         */
        this.ttsVoiceDao.saveOrUpdate(defaultSpanishVoice.getVoice());
    }
}