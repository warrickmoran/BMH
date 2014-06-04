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
package com.raytheon.uf.edex.bmh.msg;

import com.raytheon.uf.common.bmh.datamodel.language.TtsVoice;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Manifestation of the body of the broadcast message. Contains all of the
 * information that is required to complete the Text To Speech Transformation.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 2, 2014  3228       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */
@DynamicSerialize
public class BroadcastMsgBody {

    /*
     * Used to name the output audio file.
     */
    @DynamicSerializeElement
    private String afosID;

    /*
     * SSML information used to encode the audio.
     * 
     * http://www.w3.org/TR/speech-synthesis/
     */
    @DynamicSerializeElement
    private String ssml;

    /*
     * Voice information required by TTS.
     */
    @DynamicSerializeElement
    private TtsVoice voice;

    /*
     * Output file name.
     */
    @DynamicSerializeElement
    private String outputName;

    /*
     * TTS Engine Conversion Result.
     */
    @DynamicSerializeElement
    private boolean success;

    /**
     * 
     */
    public BroadcastMsgBody() {
    }

    public String getAfosID() {
        return afosID;
    }

    public void setAfosID(String afosID) {
        this.afosID = afosID;
    }

    public String getSsml() {
        return ssml;
    }

    public void setSsml(String ssml) {
        this.ssml = ssml;
    }

    public TtsVoice getVoice() {
        return voice;
    }

    public void setVoice(TtsVoice voice) {
        this.voice = voice;
    }

    public String getOutputName() {
        return outputName;
    }

    public void setOutputName(String outputName) {
        this.outputName = outputName;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
