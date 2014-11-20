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
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.raytheon.uf.common.bmh.audio.BMHAudioFormat;
import com.raytheon.uf.common.bmh.datamodel.language.Dictionary;
import com.raytheon.uf.common.bmh.datamodel.language.TtsVoice;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageTypeSummary;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * LDAD Configuration.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 30, 2014 3175       rjpeter     Initial creation
 * Nov 11, 2014 3803       bkowal      Added fields and methods.
 * Nov 13, 2014 3803       bkowal      Added selectLdadConfigByName
 * Nov 19, 2014 3385       bkowal      Added {@link LdadConfig#SELECT_LDAD_CONFIG_BY_MSG_TYPE_QUERY}
 * 
 * </pre>
 * 
 * @author rjpeter
 * @version 1.0
 */
@Entity
@DynamicSerialize
@NamedQueries({
        @NamedQuery(name = LdadConfig.SELECT_LDAD_CONFIG_REFERENCES, query = LdadConfig.SELECT_LDAD_CONFIG_REFERENCES_QUERY),
        @NamedQuery(name = LdadConfig.SELECT_LDAD_CONFIG_BY_NAME, query = LdadConfig.SELECT_LDAD_CONFIG_BY_NAME_QUERY),
        @NamedQuery(name = LdadConfig.SELECT_LDAD_CONFIG_BY_MSG_TYPE, query = LdadConfig.SELECT_LDAD_CONFIG_BY_MSG_TYPE_QUERY) })
@Table(name = "ldad_config", schema = "bmh", uniqueConstraints = { @UniqueConstraint(columnNames = { "name" }) })
@SequenceGenerator(initialValue = 1, schema = "bmh", name = LdadConfig.GEN, sequenceName = "ldad_config_gen")
@BatchSize(size = 100)
public class LdadConfig {
    protected static final String GEN = "Ldad Config Id Generator";

    public static final String SELECT_LDAD_CONFIG_REFERENCES = "selectedLdadConfigReferences";

    protected static final String SELECT_LDAD_CONFIG_REFERENCES_QUERY = "SELECT l.id, l.name, l.host, l.directory, l.encoding FROM LdadConfig l";

    public static final String SELECT_LDAD_CONFIG_BY_NAME = "selectLdadConfigByName";

    protected static final String SELECT_LDAD_CONFIG_BY_NAME_QUERY = "FROM LdadConfig l WHERE l.name = :name";

    public static final String SELECT_LDAD_CONFIG_BY_MSG_TYPE = "selectLdadConfigByMsgType";

    protected static final String SELECT_LDAD_CONFIG_BY_MSG_TYPE_QUERY = "SELECT l FROM LdadConfig l INNER JOIN l.messageTypes mts WHERE mts.afosid = :afosid";

    // use surrogate key
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = GEN)
    @DynamicSerializeElement
    protected long id;

    @Column(length = 40)
    @DynamicSerializeElement
    private String name;

    @Column(length = 60)
    @DynamicSerializeElement
    private String host;

    @Column(length = 250)
    @DynamicSerializeElement
    private String directory;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "ldad_msg_type", schema = "bmh", joinColumns = @JoinColumn(name = "ldad_id"), inverseJoinColumns = @JoinColumn(name = "msg_type_id"))
    @DynamicSerializeElement
    @Fetch(FetchMode.SUBSELECT)
    private Set<MessageTypeSummary> messageTypes;

    @ManyToOne(optional = true)
    @DynamicSerializeElement
    private Dictionary dictionary;

    @ManyToOne(optional = false)
    @JoinColumn(name = "voiceNumber")
    @DynamicSerializeElement
    private TtsVoice voice;

    @Column(length = 10)
    @DynamicSerializeElement
    @Enumerated(EnumType.STRING)
    private BMHAudioFormat encoding;

    /**
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the host
     */
    public String getHost() {
        return host;
    }

    /**
     * @param host
     *            the host to set
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * @return the directory
     */
    public String getDirectory() {
        return directory;
    }

    /**
     * @param directory
     *            the directory to set
     */
    public void setDirectory(String directory) {
        this.directory = directory;
    }

    /**
     * @return the messageTypes
     */
    public Set<MessageTypeSummary> getMessageTypes() {
        return messageTypes;
    }

    /**
     * @param messageTypes
     *            the messageTypes to set
     */
    public void setMessageTypes(Set<MessageTypeSummary> messageTypes) {
        this.messageTypes = messageTypes;
    }

    public void addMessageType(MessageTypeSummary messageType) {
        if (this.messageTypes == null) {
            this.messageTypes = new HashSet<>();
        }
        this.messageTypes.add(messageType);
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
     * @return the encoding
     */
    public BMHAudioFormat getEncoding() {
        return encoding;
    }

    /**
     * @param encoding
     *            the encoding to set
     */
    public void setEncoding(BMHAudioFormat encoding) {
        this.encoding = encoding;
    }
}