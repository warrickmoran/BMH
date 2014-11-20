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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.raytheon.uf.common.bmh.datamodel.language.TtsVoice;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Broadcast Message Fragment Object. Used to transform text to audio.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Sep 11, 2014  3588     bsteffen    Initial creation
 * Oct 23, 2014  3748     bkowal      Make the TTS Voice optional
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
@Entity
@DynamicSerialize
@Table(name = "broadcast_fragment", schema = "bmh")
@SequenceGenerator(initialValue = 1, name = BroadcastFragment.GEN, sequenceName = "broadcast_fragment_seq")
public class BroadcastFragment {
    public static final String GEN = "Broadcast Msg Fragment Generator";

    /* A unique auto-generated numerical id. Long = SQL BIGINT */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = GEN)
    @DynamicSerializeElement
    private long id;

    @ManyToOne(optional = false)
    // No dynamic serialize due to bi-directional relationship
    private BroadcastMsg message;

    /* ===== Message Header ===== */

    /* ===== Message Body ===== */

    /* The text to transform in SSML format. */
    @Column(columnDefinition = "TEXT", nullable = false)
    @DynamicSerializeElement
    private String ssml;

    /* The Voice that should be used to transform the SSML text. */
    @ManyToOne(optional = true)
    @JoinColumn(name = "voice_id")
    @DynamicSerializeElement
    private TtsVoice voice;

    /*
     * The name of the output file; generated at the conclusion of the
     * synthesis - will initially be NULL; but, will be updated after a
     * successful text synthesis.
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

    @Column(nullable = false)
    @DynamicSerializeElement
    private int position;

    /**
     * 
     */
    public BroadcastFragment() {
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

    public BroadcastMsg getMessage() {
        return message;
    }

    public void setMessage(BroadcastMsg message) {
        this.message = message;
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

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

}