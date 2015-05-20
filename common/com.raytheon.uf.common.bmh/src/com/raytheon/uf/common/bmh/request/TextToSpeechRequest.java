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

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Object for text to speech requests from CAVE
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Jun 16, 2014  3355     mpduff      Initial creation.
 * Aug 25, 2014  3558     rjpeter     Default to Paul.
 * Oct 02, 2014  3642     bkowal      Added timeout.
 * Oct 07, 2014  3687     bsteffen    Extend AbstractBMHServerRequest
 * Feb 19, 2015  4142     bkowal      Added {@link #DEFAULT_VOICE}.
 * May 20, 2015  4490     bkowal      Made {@link #DEFAULT_VOICE} private.
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */
@DynamicSerialize
public class TextToSpeechRequest extends AbstractBMHServerRequest {

    private static final int DEFAULT_VOICE = 101;

    /**
     * Phoneme/Phrase to speak
     */
    @DynamicSerializeElement
    private String phoneme;

    /**
     * The voice id
     */
    @DynamicSerializeElement
    private int voice = DEFAULT_VOICE; // Defaulting to Paul

    /**
     * Sound byte data
     */
    @DynamicSerializeElement
    private byte[] byteData;

    /**
     * Text to Speech status
     */
    @DynamicSerializeElement
    private String status;

    /**
     * The maximum amount of time to wait for a synthesizer (in milliseconds).
     */
    @DynamicSerializeElement
    private int timeout;

    /**
     * @return the phoneme
     */
    public String getPhoneme() {
        return phoneme;
    }

    /**
     * @param phoneme
     *            the phoneme to set
     */
    public void setPhoneme(String phoneme) {
        this.phoneme = phoneme;
    }

    /**
     * @return the voice
     */
    public int getVoice() {
        return voice;
    }

    /**
     * @param voice
     *            the voice to set
     */
    public void setVoice(int voice) {
        this.voice = voice;
    }

    /**
     * @return the byteData
     */
    public byte[] getByteData() {
        return byteData;
    }

    /**
     * @param byteData
     *            the byteData to set
     */
    public void setByteData(byte[] byteData) {
        this.byteData = byteData;
    }

    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status
     *            the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
