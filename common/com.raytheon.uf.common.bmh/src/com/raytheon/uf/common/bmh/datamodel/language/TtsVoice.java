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
package com.raytheon.uf.common.bmh.datamodel.language;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

import com.raytheon.uf.common.bmh.diff.DiffTitle;
import com.raytheon.uf.common.bmh.diff.DiffString;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Record for the TTS Voice. In the case of neospeech each voice is assigned a
 * unique number. The license on the server process controls which voice numbers
 * are allowed to be used.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 30, 2014 3175       rjpeter     Initial creation
 * Oct 24, 2014 3636       rferrel     Implement logging.
 * </pre>
 * 
 * @author rjpeter
 * @version 1.0
 */
@Entity
@Table(name = "tts_voice", schema = "bmh")
@DynamicSerialize
public class TtsVoice {
    @Id
    @Column
    @DynamicSerializeElement
    @DiffTitle(position = 3)
    private int voiceNumber;

    @Column(length = 20, nullable = false)
    @DynamicSerializeElement
    @DiffTitle(position = 2)
    @DiffString
    private String voiceName;

    @Enumerated(EnumType.STRING)
    @Column(length = Language.LENGTH, nullable = false)
    @DynamicSerializeElement
    @DiffTitle(position = 1)
    private Language language;

    @Column(nullable = false)
    @DynamicSerializeElement
    private boolean male;

    public int getVoiceNumber() {
        return voiceNumber;
    }

    public void setVoiceNumber(int voiceNumber) {
        this.voiceNumber = voiceNumber;
    }

    public String getVoiceName() {
        return voiceName;
    }

    public void setVoiceName(String voiceName) {
        this.voiceName = voiceName;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public boolean isMale() {
        return male;
    }

    public void setMale(boolean male) {
        this.male = male;
    }

}
