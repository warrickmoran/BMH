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
package com.raytheon.uf.common.bmh.datamodel.msg;

import java.util.Calendar;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.raytheon.uf.common.bmh.datamodel.language.TtsVoice;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Broadcast Message Record Object. Used to transform text to audio.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 23, 2014 3302       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

@NamedQueries({
        @NamedQuery(name = BroadcastMsg.GET_ALL_MSGS, query = "FROM BroadcastMsg m"),
        @NamedQuery(name = BroadcastMsg.GET_MSGS_BY_AFOS_ID, query = "FROM BroadcastMsg m WHERE m.inputMessage.afosid = :afosID") })
@Entity
@DynamicSerialize
@Table(name = "broadcast_msg", schema = "bmh")
@SequenceGenerator(initialValue = 1, name = BroadcastMsg.GEN, sequenceName = "broadcast_msg_seq")
public class BroadcastMsg {
    public static final String GEN = "Broadcast Msg Generator";

    public static final String GET_ALL_MSGS = "getBroadcastMsgs";

    public static final String GET_MSGS_BY_AFOS_ID = "getBroadcastMsgsByAfosID";

    /* A unique auto-generated numerical id. Long = SQL BIGINT */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = GEN)
    @DynamicSerializeElement
    private long id;

    /* The date that the record was created; mainly for auditing purposes. */
    @Column(nullable = false)
    @DynamicSerializeElement
    private Calendar creationDate;

    /* The date the record was last updated; mainly for auditing purposes. */
    @Column(nullable = false)
    @DynamicSerializeElement
    private Calendar updateDate;

    /* ===== Message Header ===== */

    /*
     * The transmitter group associated with the record. Transmitter Groups will
     * have a dictionary associated with them based on the language (voice
     * determines the language). So, a separate broadcast message will be
     * generated for each { transmitter group, dictionary } combination and this
     * field is set to the transmitter group in that combination.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "transmitter_group_name")
    @DynamicSerializeElement
    private TransmitterGroup transmitterGroup;

    @ManyToOne(optional = false)
    @JoinColumn(name = "input_message_id")
    @DynamicSerializeElement
    private InputMessage inputMessage;

    /* ===== Message Body ===== */

    /* The text to transform in SSML format. */
    @Column(columnDefinition = "TEXT", nullable = false)
    @DynamicSerializeElement
    private String ssml;

    /* The Voice that should be used to transform the SSML text. */
    @ManyToOne(optional = false)
    @JoinColumn(name = "voice_id")
    @DynamicSerializeElement
    private TtsVoice voice;

    /*
     * The name of the output file; generated at the conclusion of the
     * transformation - will initially be NULL; but, will be updated after a
     * successful text transformation.
     */
    @Column(nullable = true)
    @DynamicSerializeElement
    private String outputName;

    /*
     * Indicates whether or not the text was successfully transformed; set at
     * the conclusion of the transformation.
     */
    @Column
    @DynamicSerializeElement
    private boolean success;

    /**
     * 
     */
    public BroadcastMsg() {
    }

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
     * @return the creationDate
     */
    public Calendar getCreationDate() {
        return creationDate;
    }

    /**
     * @param creationDate
     *            the creationDate to set
     */
    public void setCreationDate(Calendar creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * @return the updateDate
     */
    public Calendar getUpdateDate() {
        return updateDate;
    }

    /**
     * @param updateDate
     *            the updateDate to set
     */
    public void setUpdateDate(Calendar updateDate) {
        this.updateDate = updateDate;
    }

    /**
     * @return the transmitterGroup
     */
    public TransmitterGroup getTransmitterGroup() {
        return transmitterGroup;
    }

    /**
     * @param transmitterGroup
     *            the transmitterGroup to set
     */
    public void setTransmitterGroup(TransmitterGroup transmitterGroup) {
        this.transmitterGroup = transmitterGroup;
    }

    /**
     * @return the inputMessage
     */
    public InputMessage getInputMessage() {
        return inputMessage;
    }

    /**
     * @param inputMessage
     *            the inputMessage to set
     */
    public void setInputMessage(InputMessage inputMessage) {
        this.inputMessage = inputMessage;
    }

    /**
     * @return the ssml
     */
    public String getSsml() {
        return ssml;
    }

    /**
     * @param ssml
     *            the ssml to set
     */
    public void setSsml(String ssml) {
        this.ssml = ssml;
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
     * @return the outputName
     */
    public String getOutputName() {
        return outputName;
    }

    /**
     * @param outputName
     *            the outputName to set
     */
    public void setOutputName(String outputName) {
        this.outputName = outputName;
    }

    /**
     * @return the success
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * @param success
     *            the success to set
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }
}