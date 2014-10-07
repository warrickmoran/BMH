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
package com.raytheon.uf.common.bmh.datamodel.transmitter;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import com.raytheon.uf.common.bmh.datamodel.language.Dictionary;
import com.raytheon.uf.common.bmh.datamodel.language.Language;
import com.raytheon.uf.common.bmh.datamodel.language.TtsVoice;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Information Specific to a language selection on a transmitter.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 30, 2014 3175       rjpeter     Initial creation
 * Jun 26, 2014 3302       bkowal      Added getters/setters for all data.
 * Aug 05, 2014 3175       rjpeter     Fixed serialization.
 * Aug 29, 2014 3568       bkowal      Added query to retrieve transmitter language(s) associated
 *                                     with a transmitter group
 * Oct 2, 2014  3642       bkowal      Updated time message field to be: preamble and postamble
 * </pre>
 * 
 * @author rjpeter
 * @version 1.0
 */
@NamedQueries({ @NamedQuery(name = TransmitterLanguage.GET_LANGUAGES_FOR_GROUP, query = TransmitterLanguage.GET_LANGUAGES_FOR_GROUP_QUERY) })
@Entity
@Table(name = "transmitter_language", schema = "bmh")
@DynamicSerialize
public class TransmitterLanguage {

    public static final String GET_LANGUAGES_FOR_GROUP = "getLanguagesForGroup";

    protected static final String GET_LANGUAGES_FOR_GROUP_QUERY = "FROM TransmitterLanguage tl WHERE tl.id.transmitterGroup = :group";

    @EmbeddedId
    @DynamicSerializeElement
    private TransmitterLanguagePK id;

    // Text: 1-40960 characters
    @Lob
    @Column(nullable = false)
    @DynamicSerializeElement
    private String stationIdMsg;

    /*
     * Note: the time preamble and postamble are optional. Additional
     * information about the time message, itself, is still required.
     */
    @Lob
    @Column(nullable = false)
    @DynamicSerializeElement
    private String timeMsgPreamble;

    @Lob
    @Column(nullable = true)
    @DynamicSerializeElement
    private String timeMsgPostamble;

    @ManyToOne(optional = true)
    @DynamicSerializeElement
    private Dictionary dictionary;

    @ManyToOne(optional = false)
    @JoinColumn(name = "voiceNumber")
    @DynamicSerializeElement
    private TtsVoice voice;

    /**
     * @return the id
     */
    public TransmitterLanguagePK getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(TransmitterLanguagePK id) {
        this.id = id;
    }

    /**
     * 
     * @return the language
     */
    public Language getLanguage() {
        if (id != null) {
            return id.getLanguage();
        }

        return null;
    }

    /**
     * 
     * @param language
     */
    public void setLanguage(Language language) {
        if (id == null) {
            id = new TransmitterLanguagePK();
        }

        id.setLanguage(language);
    }

    /**
     * 
     * @return the transmitter group name
     */
    public TransmitterGroup getTransmitterGroup() {
        if (id != null) {
            return id.getTransmitterGroup();
        }

        return null;
    }

    public void setTransmitterGroup(TransmitterGroup transmitterGroup) {
        if (id == null) {
            id = new TransmitterLanguagePK();
        }

        id.setTransmitterGroup(transmitterGroup);
    }

    /**
     * @return the stationIdMsg
     */
    public String getStationIdMsg() {
        return stationIdMsg;
    }

    /**
     * @param stationIdMsg
     *            the stationIdMsg to set
     */
    public void setStationIdMsg(String stationIdMsg) {
        this.stationIdMsg = stationIdMsg;
    }

    public String getTimeMsgPreamble() {
        return timeMsgPreamble;
    }

    public void setTimeMsgPreamble(String timeMsgPreamble) {
        this.timeMsgPreamble = timeMsgPreamble;
    }

    public String getTimeMsgPostamble() {
        return timeMsgPostamble;
    }

    public void setTimeMsgPostamble(String timeMsgPostamble) {
        this.timeMsgPostamble = timeMsgPostamble;
    }

    /**
     * 
     * @return the dictionary
     */
    public Dictionary getDictionary() {
        return dictionary;
    }

    /**
     * 
     * @param dictionary
     *            the dictionary to set
     */
    public void setDictionary(Dictionary dictionary) {
        this.dictionary = dictionary;
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((id == null) ? 0 : id.hashCode());
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
        TransmitterLanguage other = (TransmitterLanguage) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }
}