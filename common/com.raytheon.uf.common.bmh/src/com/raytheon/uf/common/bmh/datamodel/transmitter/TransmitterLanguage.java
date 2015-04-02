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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.raytheon.uf.common.bmh.datamodel.PositionUtil;
import com.raytheon.uf.common.bmh.datamodel.language.Dictionary;
import com.raytheon.uf.common.bmh.datamodel.language.Language;
import com.raytheon.uf.common.bmh.datamodel.language.TtsVoice;
import com.raytheon.uf.common.bmh.diff.DiffString;
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
 * Oct 28, 2014 3636       rferrel     Implemented logging.
 * Nov 02, 2014 3746       rjpeter     Fix column definition for hibernate upgrade.
 * Jan 13, 2015 3809       bkowal      Fixed {@link #toString()}.
 * Feb 19, 2015 4142       bkowal      Added {@link #speechRate}.
 * Mar 12, 2015 4213       bkowal      Added {@link #staticMessageTypes}.
 * Apr 02, 2015 4248       rjpeter     Updated staticMessageTypes database relation to a set, added ordered returns.
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
    @DiffString
    private TransmitterLanguagePK id;

    @ManyToOne(optional = true)
    @DynamicSerializeElement
    private Dictionary dictionary;

    @ManyToOne(optional = false)
    @JoinColumn(name = "voiceNumber")
    @DynamicSerializeElement
    private TtsVoice voice;

    @Column(nullable = false)
    @DynamicSerializeElement
    private int speechRate = 0;

    @OneToMany(mappedBy = "id.transmitterLanguagePK", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @DynamicSerializeElement
    @Fetch(FetchMode.SUBSELECT)
    private Set<StaticMessageType> staticMessageTypes;

    public void addStaticMessageType(StaticMessageType staticMsgType) {
        if (this.staticMessageTypes == null) {
            this.staticMessageTypes = new HashSet<>();
        }

        staticMsgType.setTransmitterLanguage(this);
        staticMsgType.setPosition(this.staticMessageTypes.size());
        this.staticMessageTypes.add(staticMsgType);
    }

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

    public int getSpeechRate() {
        return speechRate;
    }

    public void setSpeechRate(int speechRate) {
        if ((speechRate < -99) || (speechRate > 99)) {
            throw new IllegalArgumentException(
                    "An invalid speech rate has been specified! The speech rate must be between -99 and 99 inclusive.");
        }

        this.speechRate = speechRate;
    }

    /**
     * @return the staticMessageTypes
     */
    public List<StaticMessageType> getOrderedStaticMessageTypes() {
        return PositionUtil.order(staticMessageTypes);
    }

    /**
     * @param staticMessageTypes
     *            the staticMessageTypes to set
     */
    public void setOrderedStaticMessageTypes(
            List<StaticMessageType> staticMessageTypes) {
        if (staticMessageTypes == null) {
            this.staticMessageTypes = null;
            return;
        }

        PositionUtil.updatePositions(staticMessageTypes);
        setStaticMessageTypes(new HashSet<>(staticMessageTypes));
    }

    /**
     * @return the staticMessageTypes
     */
    public Set<StaticMessageType> getStaticMessageTypes() {
        return staticMessageTypes;
    }

    /**
     * @param staticMessageTypes
     *            the staticMessageTypes to set
     */
    public void setStaticMessageTypes(Set<StaticMessageType> staticMessageTypes) {
        this.staticMessageTypes = staticMessageTypes;
        if ((this.staticMessageTypes == null)
                || this.staticMessageTypes.isEmpty()) {
            return;
        }

        for (StaticMessageType stm : this.staticMessageTypes) {
            stm.setTransmitterLanguage(this);
        }
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("TransmitterLanguage[ id=").append(id);
        if (this.dictionary != null) {
            sb.append(", dictionary=").append(dictionary.getName());
        }
        sb.append(", voice=").append(voice.getVoiceName());
        sb.append(", speechRate=").append(this.speechRate).append("]");
        return sb.toString();
    }
}