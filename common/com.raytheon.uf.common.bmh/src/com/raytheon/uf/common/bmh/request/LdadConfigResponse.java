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

import java.util.List;
import java.util.Set;

import com.raytheon.uf.common.bmh.audio.BMHAudioFormat;
import com.raytheon.uf.common.bmh.datamodel.transmitter.LdadConfig;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Response object generated by the {@link LdadConfigHandler}. Contains
 * references to the {@link LdadConfig} records that have been retrieved or
 * saved.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 11, 2014 3803       bkowal      Initial creation
 * Dec 4, 2014  3880       bkowal      Added encodings.
 * Feb 16, 2015 4118       bkowal      Added {@link #convertedAudio}.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */
@DynamicSerialize
public class LdadConfigResponse {

    @DynamicSerializeElement
    private List<LdadConfig> ldadConfigurations;

    @DynamicSerializeElement
    private Set<BMHAudioFormat> encodings;

    @DynamicSerializeElement
    private byte[] convertedAudio;

    /**
     * 
     */
    public LdadConfigResponse() {
    }

    /**
     * @return the ldadConfigurations
     */
    public List<LdadConfig> getLdadConfigurations() {
        return ldadConfigurations;
    }

    /**
     * @param ldadConfigurations
     *            the ldadConfigurations to set
     */
    public void setLdadConfigurations(List<LdadConfig> ldadConfigurations) {
        this.ldadConfigurations = ldadConfigurations;
    }

    /**
     * @return the encodings
     */
    public Set<BMHAudioFormat> getEncodings() {
        return encodings;
    }

    /**
     * @param encodings
     *            the encodings to set
     */
    public void setEncodings(Set<BMHAudioFormat> encodings) {
        this.encodings = encodings;
    }

    /**
     * @return the convertedAudio
     */
    public byte[] getConvertedAudio() {
        return convertedAudio;
    }

    /**
     * @param convertedAudio
     *            the convertedAudio to set
     */
    public void setConvertedAudio(byte[] convertedAudio) {
        this.convertedAudio = convertedAudio;
    }
}