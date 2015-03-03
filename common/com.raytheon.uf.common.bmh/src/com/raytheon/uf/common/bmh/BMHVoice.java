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
package com.raytheon.uf.common.bmh;

import com.raytheon.uf.common.bmh.datamodel.language.Language;
import com.raytheon.uf.common.bmh.datamodel.language.TtsVoice;

/**
 * An enumeration of the English & Spanish voices supported by v3.10 of the
 * NeoSpeech TTS API.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 2, 2015  4175       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public enum BMHVoice {
    /* The TTS Paul Voice - the DEFAULT BMH Voice. */
    PAUL("Paul", 101, Language.ENGLISH, true),
    /* The TTS Violeta Voice - The DEFAULT BMH Spanish (ONLY) Voice. */
    VIOLETA("Violeta", 400, Language.SPANISH, false);

    private final String name;

    private final int id;

    private final Language language;

    private final boolean male;

    private BMHVoice(String name, int id, Language language, boolean male) {
        this.name = name;
        this.id = id;
        this.language = language;
        this.male = male;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public Language getLanguage() {
        return this.language;
    }

    public TtsVoice getVoice() {
        TtsVoice voice = new TtsVoice();
        voice.setVoiceNumber(this.id);
        voice.setVoiceName(this.name);
        voice.setLanguage(this.language);
        voice.setMale(this.male);

        return voice;
    }
}