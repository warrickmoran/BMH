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
import javax.persistence.Table;

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
 * 
 * </pre>
 * 
 * @author rjpeter
 * @version 1.0
 */
@Entity
@Table(name = "transmitter_language", schema = "bmh")
@DynamicSerialize
public class TransmitterLanguage {
    @EmbeddedId
    @DynamicSerializeElement
    private TransmitterLanguagePK id;

    // Text: 1-40960 characters
    @Lob
    @Column(nullable = false)
    @DynamicSerializeElement
    private String stationIdMsg;

    // Text: 1-40960 characters
    // Must contain a parsable DTG string
    @Lob
    @Column(nullable = false)
    @DynamicSerializeElement
    private String timeMsg;

    // FK enforced at registration via script
    // @ManyToMany(cascade = CascadeType.ALL)
    // @JoinTable(name = "tx_dict", joinColumns = @JoinColumn(name =
    // "mnemonic"), inverseJoinColumns = @JoinColumn(name = "name"))
    // private Collection<Dictionary> dicts;
    @Column(nullable = false)
    @DynamicSerializeElement
    private String dictionaryName;

    @ManyToOne(optional = false)
    @JoinColumn(name = "voiceNumber")
    private TtsVoice voice;

    /**
     * @return the id
     */
    public TransmitterLanguagePK getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(TransmitterLanguagePK id) {
        this.id = id;
    }

    /**
     * @return the stationIdMsg
     */
    public String getStationIdMsg() {
        return stationIdMsg;
    }

    /**
     * @param stationIdMsg the stationIdMsg to set
     */
    public void setStationIdMsg(String stationIdMsg) {
        this.stationIdMsg = stationIdMsg;
    }

    /**
     * @return the timeMsg
     */
    public String getTimeMsg() {
        return timeMsg;
    }

    /**
     * @param timeMsg the timeMsg to set
     */
    public void setTimeMsg(String timeMsg) {
        this.timeMsg = timeMsg;
    }

    /**
     * @return the dictionaryName
     */
    public String getDictionaryName() {
        return dictionaryName;
    }

    /**
     * @param dictionaryName the dictionaryName to set
     */
    public void setDictionaryName(String dictionaryName) {
        this.dictionaryName = dictionaryName;
    }

    /**
     * @return the voice
     */
    public TtsVoice getVoice() {
        return voice;
    }

    /**
     * @param voice the voice to set
     */
    public void setVoice(TtsVoice voice) {
        this.voice = voice;
    }
}
