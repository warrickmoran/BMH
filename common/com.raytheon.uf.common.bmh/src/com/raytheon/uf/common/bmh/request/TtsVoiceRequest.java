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
package com.raytheon.uf.common.bmh.request;

import com.raytheon.uf.common.bmh.BMHVoice;
import com.raytheon.uf.common.bmh.datamodel.language.Language;
import com.raytheon.uf.common.bmh.datamodel.language.TtsVoice;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Request object for Voice queries.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Aug 11, 2014  3490     lvenable    Initial creation
 * Oct 07, 2014  3687     bsteffen    Extend AbstractBMHServerRequest
 * Dec 16, 2014  3618     bkowal      Support retrieving voice by id; support
 *                                    updating voices.
 * Jan 13, 2015  3809     bkowal      Added {@link TtsVoiceAction#VoiceIdentifiersForLanguage}.
 * Mar 03, 2015  4175     bkowal      Support voice registration.
 * Mar 27, 2015  4315     rferrel     Added {@link TtsVoiceAction#AvailableLanguage}.
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */
@DynamicSerialize
public class TtsVoiceRequest extends AbstractBMHServerRequest {

    public enum TtsVoiceAction {
        AllVoices, VoiceIdentifiers, VoiceIdentifiersForLanguage, GetById,

        UpdateVoice, RegisterVoice, AvailableLanguage;
    }

    @DynamicSerializeElement
    private TtsVoiceAction action;

    @DynamicSerializeElement
    private TtsVoice voice;

    @DynamicSerializeElement
    private int voiceNumber;

    @DynamicSerializeElement
    private Language language;

    @DynamicSerializeElement
    private BMHVoice bmhVoice;

    public TtsVoiceAction getAction() {
        return action;
    }

    public void setAction(TtsVoiceAction action) {
        this.action = action;
    }

    /**
     * @return the voice
     */
    public TtsVoice getVoice() {
        return voice;
    }

    /**
     * @param voice
     *            the voice to set
     */
    public void setVoice(TtsVoice voice) {
        this.voice = voice;
    }

    /**
     * @return the voiceNumber
     */
    public int getVoiceNumber() {
        return voiceNumber;
    }

    /**
     * @param voiceNumber
     *            the voiceNumber to set
     */
    public void setVoiceNumber(int voiceNumber) {
        this.voiceNumber = voiceNumber;
    }

    /**
     * @return the language
     */
    public Language getLanguage() {
        return language;
    }

    /**
     * @param language
     *            the language to set
     */
    public void setLanguage(Language language) {
        this.language = language;
    }

    /**
     * @return the bmhVoice
     */
    public BMHVoice getBmhVoice() {
        return bmhVoice;
    }

    /**
     * @param bmhVoice
     *            the bmhVoice to set
     */
    public void setBmhVoice(BMHVoice bmhVoice) {
        this.bmhVoice = bmhVoice;
    }
}
