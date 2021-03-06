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

import com.raytheon.uf.common.bmh.audio.BMHAudioFormat;
import com.raytheon.uf.common.bmh.datamodel.transmitter.LdadConfig;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Request object for {@link LdadConfig} database operations.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 11, 2014 3803       bkowal      Initial creation
 * Dec 4, 2014  3880       bkowal      Added RetrieveSupportedEncodings.
 * Feb 16, 2015 4118       bkowal      Added fields to support audio conversion.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */
@DynamicSerialize
public class LdadConfigRequest extends AbstractBMHServerRequest {

    public enum LdadConfigAction {
        /*
         * Retrieves references to every ldad record in existence consisting of
         * id and a few fields
         */
        RetrieveReferences,
        /* Retrieves an entire ldad config record based on id */
        RetrieveRecord,
        /* Retrieves an entire ldad config record by name */
        RetrieveRecordByName,
        /* Save or updates an ldad config record */
        Save,
        /* Deletes an existing ldad config record */
        Delete,
        /*
         * Retrieves the {@link BMHAudioFormat}s that the system has audio
         * converters for
         */
        RetrieveSupportedEncodings,
        /*
         * Converts the audio from the specified format to the ulaw format
         */
        ConvertAudio
    }

    @DynamicSerializeElement
    private LdadConfigAction action;

    @DynamicSerializeElement
    private long id;

    @DynamicSerializeElement
    private String name;

    @DynamicSerializeElement
    private LdadConfig ldadConfig;
    
    @DynamicSerializeElement
    private BMHAudioFormat sourceFormat;
    
    @DynamicSerializeElement
    private byte[] audioBytes;

    /**
     * 
     */
    public LdadConfigRequest() {
    }

    /**
     * @param operational
     */
    public LdadConfigRequest(boolean operational) {
        super(operational);
    }

    /**
     * @return the action
     */
    public LdadConfigAction getAction() {
        return action;
    }

    /**
     * @param action
     *            the action to set
     */
    public void setAction(LdadConfigAction action) {
        this.action = action;
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
     * @return the ldadConfig
     */
    public LdadConfig getLdadConfig() {
        return ldadConfig;
    }

    /**
     * @param ldadConfig
     *            the ldadConfig to set
     */
    public void setLdadConfig(LdadConfig ldadConfig) {
        this.ldadConfig = ldadConfig;
    }

    /**
     * @return the sourceFormat
     */
    public BMHAudioFormat getSourceFormat() {
        return sourceFormat;
    }

    /**
     * @param sourceFormat the sourceFormat to set
     */
    public void setSourceFormat(BMHAudioFormat sourceFormat) {
        this.sourceFormat = sourceFormat;
    }

    /**
     * @return the audioBytes
     */
    public byte[] getAudioBytes() {
        return audioBytes;
    }

    /**
     * @param audioBytes the audioBytes to set
     */
    public void setAudioBytes(byte[] audioBytes) {
        this.audioBytes = audioBytes;
    }
}