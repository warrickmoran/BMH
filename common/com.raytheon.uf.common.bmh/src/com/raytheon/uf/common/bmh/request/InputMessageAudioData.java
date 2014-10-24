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
 * Provides pre-formatted information about the audio associated with a
 * {@link InputMessage} grouped by {@link TransmitterGroup}.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 23, 2014 3478       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

@DynamicSerialize
public class InputMessageAudioData implements Comparable<InputMessageAudioData> {

    /**
     * The name of the transmitter group that the audio was generated for.
     */
    @DynamicSerializeElement
    private String transmitterGroupName;

    /**
     * Boolean flag indicating whether or not audio generation was successful.
     */
    @DynamicSerializeElement
    private boolean success;

    /**
     * The generated audio
     */
    @DynamicSerializeElement
    private byte[] audio;

    /**
     * The duration of the audio in seconds.
     */
    @DynamicSerializeElement
    private int audioDuration;

    /**
     * The audio duration converted to the format hours:minutes:seconds. Will be
     * removed if not used.
     */
    @DynamicSerializeElement
    private String formattedAudioDuration;

    /**
     * Constructor
     */
    public InputMessageAudioData() {
    }

    /**
     * @return the transmitterGroupName
     */
    public String getTransmitterGroupName() {
        return transmitterGroupName;
    }

    /**
     * @param transmitterGroupName the transmitterGroupName to set
     */
    public void setTransmitterGroupName(String transmitterGroupName) {
        this.transmitterGroupName = transmitterGroupName;
    }

    /**
     * @return the success
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * @param success the success to set
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * @return the audio
     */
    public byte[] getAudio() {
        return audio;
    }

    /**
     * @param audio the audio to set
     */
    public void setAudio(byte[] audio) {
        this.audio = audio;
    }

    /**
     * @return the audioDuration
     */
    public int getAudioDuration() {
        return audioDuration;
    }

    /**
     * @param audioDuration the audioDuration to set
     */
    public void setAudioDuration(int audioDuration) {
        this.audioDuration = audioDuration;
    }

    /**
     * @return the formattedAudioDuration
     */
    public String getFormattedAudioDuration() {
        return formattedAudioDuration;
    }

    /**
     * @param formattedAudioDuration the formattedAudioDuration to set
     */
    public void setFormattedAudioDuration(String formattedAudioDuration) {
        this.formattedAudioDuration = formattedAudioDuration;
    }

    @Override
    public int compareTo(InputMessageAudioData o) {
        return o.transmitterGroupName.compareTo(this.transmitterGroupName);
    }
}