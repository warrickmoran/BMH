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
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.ForeignKey;

import com.raytheon.uf.common.bmh.diff.DiffString;
import com.raytheon.uf.common.bmh.diff.DiffTitle;
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
 * Dec 11, 2014 3618       bkowal      Added {@link #dictionary}.
 * Dec 16, 2014 3618       bkowal      Added {@link #GET_VOICE_IDENTIFIERS}.
 * Jan 13, 2015 3809       bkowal      Added {@link #GET_VOICE_IDENTIFIERS_FOR_LANGUAGE_QUERY}.
 * May 12, 2015 4248       rjpeter     Remove bmh schema, standardize foreign/unique keys.
 * Dec 03, 2015 5158       bkowal      Added {@link #GET_DEFAULT_VOICE_FOR_LANGUAGE}.
 * Dec 08, 2015 5159       bkowal      Added {@link #GET_VOICE_FOR_DICTIONARY}.
 * </pre>
 * 
 * @author rjpeter
 * @version 1.0
 */
@NamedQueries({
        @NamedQuery(name = TtsVoice.GET_VOICE_IDENTIFIERS, query = TtsVoice.GET_VOICE_IDENTIFIERS_QUERY),
        @NamedQuery(name = TtsVoice.GET_VOICE_IDENTIFIERS_FOR_LANGUAGE, query = TtsVoice.GET_VOICE_IDENTIFIERS_FOR_LANGUAGE_QUERY),
        @NamedQuery(name = TtsVoice.GET_DEFAULT_VOICE_FOR_LANGUAGE, query = TtsVoice.GET_DEFAULT_VOICE_FOR_LANGUAGE_QUERY),
        @NamedQuery(name = TtsVoice.GET_VOICE_FOR_DICTIONARY, query = TtsVoice.GET_VOICE_FOR_DICTIONARY_QUERY) })
@Entity
@Table(name = "tts_voice")
@DynamicSerialize
public class TtsVoice {

    public static final String GET_VOICE_IDENTIFIERS = "getVoiceIdentifiers";

    protected static final String GET_VOICE_IDENTIFIERS_QUERY = "SELECT v.voiceNumber, v.voiceName FROM TtsVoice v";

    public static final String GET_VOICE_IDENTIFIERS_FOR_LANGUAGE = "getVoiceIdentifiersForLanguage";

    protected static final String GET_VOICE_IDENTIFIERS_FOR_LANGUAGE_QUERY = "SELECT v.voiceNumber, v.voiceName FROM TtsVoice v WHERE v.language = :language";

    /*
     * For now there is only one {@link TtsVoice} for each {@link Language}, so
     * this query is essentially nothing more than a retrieve TtsVoice by
     * Language. However, if there is ever a time when BMH needs to support more
     * than one TtsVoice for a particular Language, there will need to be a way
     * to specify which TtsVoice is the default for a particular Language. The
     * default TtsVoice is used when the Language specified in the header of a
     * BMH message does not match the TtsVoice associated with the message type
     * associated with the BMH message.
     */
    public static final String GET_DEFAULT_VOICE_FOR_LANGUAGE = "getDefaultVoiceForLanguage";

    protected static final String GET_DEFAULT_VOICE_FOR_LANGUAGE_QUERY = "SELECT v FROM TtsVoice v WHERE v.language = :language";

    public static final String GET_VOICE_FOR_DICTIONARY = "getVoiceForDictionary";

    protected static final String GET_VOICE_FOR_DICTIONARY_QUERY = "SELECT v from TtsVoice v WHERE v.dictionary = :dictionary";

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

    @ManyToOne(optional = true)
    @ForeignKey(name = "fk_tts_voice_to_dict")
    @DynamicSerializeElement
    private Dictionary dictionary;

    /**
     * {@link #removedDictionary} is used to ensure that static messages are
     * updated correctly in response to {@link TtsVoice}-level
     * {@link Dictionary} changes.
     */
    @Transient
    @DynamicSerializeElement
    private Dictionary removedDictionary;

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

    /**
     * @return the dictionary
     */
    public Dictionary getDictionary() {
        return dictionary;
    }

    /**
     * @param dictionary
     *            the dictionary to set
     */
    public void setDictionary(Dictionary dictionary) {
        this.dictionary = dictionary;
    }

    /**
     * @return the removedDictionary
     */
    public Dictionary getRemovedDictionary() {
        return removedDictionary;
    }

    /**
     * @param removedDictionary
     *            the removedDictionary to set
     */
    public void setRemovedDictionary(Dictionary removedDictionary) {
        this.removedDictionary = removedDictionary;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + voiceNumber;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TtsVoice other = (TtsVoice) obj;
        if (voiceNumber != other.voiceNumber) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "TtsVoice [voiceNumber=" + voiceNumber + ", voiceName="
                + voiceName + ", language" + language + ", male=" + male + "]";
    }

}
