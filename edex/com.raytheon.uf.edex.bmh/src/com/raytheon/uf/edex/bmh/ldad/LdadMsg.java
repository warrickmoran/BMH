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
package com.raytheon.uf.edex.bmh.ldad;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Message associated with a {@link LdadConfig}. Ldad Messages are generated
 * similar to the way that {@link BroadcastMsg}s are. The main different is that
 * Ldad Messages are never sent to the playlist manager so that they can be
 * scheduled for broadcast, they are sent to the ldad disseminator so that they
 * can be transferred to a remote location.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 19, 2014 3385       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

@DynamicSerialize
public class LdadMsg {

    /**
     * Id of the ldad configuration that the additional synthesis is being
     * completed for.
     */
    @DynamicSerializeElement
    private long ldadId;

    /**
     * SSML to synthesize.
     */
    @DynamicSerializeElement
    private String ssml;

    /**
     * Flag indicating whether or not the TTS Synthesis was successful.
     */
    @DynamicSerializeElement
    private boolean success;

    /**
     * Output file associated with the audio generated for the ldad
     * configuration. Will only be present when {@link LdadMsg#success} is true.
     */
    @DynamicSerializeElement
    private String outputName;

    /**
     * 
     */
    public LdadMsg() {
    }

    public long getLdadId() {
        return ldadId;
    }

    public void setLdadId(long ldadId) {
        this.ldadId = ldadId;
    }

    public String getSsml() {
        return ssml;
    }

    public void setSsml(String ssml) {
        this.ssml = ssml;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getOutputName() {
        return outputName;
    }

    public void setOutputName(String outputName) {
        this.outputName = outputName;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("LdadMsg [");
        sb.append("ldadId=");
        sb.append(this.ldadId);
        sb.append(", ssml=");
        sb.append(this.ssml);
        sb.append(", success=");
        sb.append(this.success);
        if (this.outputName != null) {
            sb.append(", outputName=");
            sb.append(this.outputName);
        }
        sb.append("]");

        return sb.toString();
    }
}