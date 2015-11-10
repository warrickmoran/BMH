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
package com.raytheon.uf.common.bmh.audio;

import javax.xml.bind.annotation.XmlRootElement;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Configurable audio regulation settings.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 24, 2015 4770       bkowal      Initial creation
 * Aug 25, 2015 4771       bkowal      Added additional configurable options.
 * Sep 01, 2015 4771       bkowal      Added additional configurable options that
 *                                     are used for audio playback via weather messages.
 * Nov 04, 2015 5068       rjpeter     Switch audio units from dB to amplitude.
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

@DynamicSerialize
@XmlRootElement(name = AudioRegulationConfiguration.ROOT_NAME)
public class AudioRegulationConfiguration {

    protected static final String ROOT_NAME = "regulationConfiguration";

    public static final String XML_NAME = ROOT_NAME + ".xml";

    /*
     * Just a placeholder for now.
     */
    public static enum ALGORITHM {
        LINEAR_PCM, DEVIATION_EXCLUSION;
    }

    /*
     * Do not alter audio with a amplitude below this field.
     */
    @DynamicSerializeElement
    private short amplitudeSilenceLimit;

    /*
     * Disables the use of the amplitdeSilenceLimit field.
     */
    @DynamicSerializeElement
    private boolean disableSilenceLimit;

    /*
     * Do not alter audio with a amplitude above this field.
     */
    @DynamicSerializeElement
    private short amplitudeMaxLimit;

    /*
     * Disables the use of the amplitudeMaxLimit field.
     */
    @DynamicSerializeElement
    private boolean disableMaxLimit;

    @DynamicSerializeElement
    private ALGORITHM regulationAlgorithm;

    @DynamicSerializeElement
    private short audioPlaybackAmplitude;

    @DynamicSerializeElement
    private boolean disableRecordedPreAmplication;

    /*
     * Specifies the amount of time (in milliseconds) to buffer audio before
     * transmitting it to the server the first time.
     */
    @DynamicSerializeElement
    private int initialBufferDelay;

    /*
     * Specifies the amount of time (in milliseconds) to buffer audio before
     * transmitting it to the server every additional time thereafter.
     */
    @DynamicSerializeElement
    private int bufferDelay;

    public AudioRegulationConfiguration() {
    }

    /**
     * @return the amplitudeSilenceLimit
     */
    public short getAmplitudeSilenceLimit() {
        return amplitudeSilenceLimit;
    }

    /**
     * @param amplitudeSilenceLimit
     *            the amplitudeSilenceLimit to set
     */
    public void setAmplitudeSilenceLimit(short amplitudeSilenceLimit) {
        this.amplitudeSilenceLimit = amplitudeSilenceLimit;
    }

    /**
     * @return the disableSilenceLimit
     */
    public boolean isDisableSilenceLimit() {
        return disableSilenceLimit;
    }

    /**
     * @param disableSilenceLimit
     *            the disableSilenceLimit to set
     */
    public void setDisableSilenceLimit(boolean disableSilenceLimit) {
        this.disableSilenceLimit = disableSilenceLimit;
    }

    /**
     * @return the amplitudeMaxLimit
     */
    public short getAmplitudeMaxLimit() {
        return amplitudeMaxLimit;
    }

    /**
     * @param amplitudeMaxLimit
     *            the amplitudeMaxLimit to set
     */
    public void setAmplitudeMaxLimit(short amplitudeMaxLimit) {
        this.amplitudeMaxLimit = amplitudeMaxLimit;
    }

    /**
     * @return the disableMaxLimit
     */
    public boolean isDisableMaxLimit() {
        return disableMaxLimit;
    }

    /**
     * @param disableMaxLimit
     *            the disableMaxLimit to set
     */
    public void setDisableMaxLimit(boolean disableMaxLimit) {
        this.disableMaxLimit = disableMaxLimit;
    }

    /**
     * @return the regulationAlgorithm
     */
    public ALGORITHM getRegulationAlgorithm() {
        return regulationAlgorithm;
    }

    /**
     * @param regulationAlgorithm
     *            the regulationAlgorithm to set
     */
    public void setRegulationAlgorithm(ALGORITHM regulationAlgorithm) {
        this.regulationAlgorithm = regulationAlgorithm;
    }

    /**
     * @return the audioPlaybackAmplitude
     */
    public short getAudioPlaybackAmplitude() {
        return audioPlaybackAmplitude;
    }

    /**
     * @param audioPlaybackAmplitude
     *            the audioPlaybackAmplitude to set
     */
    public void setAudioPlaybackAmplitude(short audioPlaybackAmplitude) {
        this.audioPlaybackAmplitude = audioPlaybackAmplitude;
    }

    /**
     * @return the disableRecordedPreAmplication
     */
    public boolean isDisableRecordedPreAmplication() {
        return disableRecordedPreAmplication;
    }

    /**
     * @param disableRecordedPreAmplication
     *            the disableRecordedPreAmplication to set
     */
    public void setDisableRecordedPreAmplication(
            boolean disableRecordedPreAmplication) {
        this.disableRecordedPreAmplication = disableRecordedPreAmplication;
    }

    /**
     * @return the initialBufferDelay
     */
    public int getInitialBufferDelay() {
        return initialBufferDelay;
    }

    /**
     * @param initialBufferDelay
     *            the initialBufferDelay to set
     */
    public void setInitialBufferDelay(int initialBufferDelay) {
        this.initialBufferDelay = initialBufferDelay;
    }

    /**
     * @return the bufferDelay
     */
    public int getBufferDelay() {
        return bufferDelay;
    }

    /**
     * @param bufferDelay
     *            the bufferDelay to set
     */
    public void setBufferDelay(int bufferDelay) {
        this.bufferDelay = bufferDelay;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(
                "AudioRegulationConfiguration [amplitudeSilenceLimit=");
        sb.append(this.amplitudeSilenceLimit).append(", disableSilenceLimit=");
        sb.append(this.disableSilenceLimit).append(", amplitudeMaxLimit=");
        sb.append(this.amplitudeMaxLimit).append(", disableMaxLimit=");
        sb.append(this.disableMaxLimit).append(", regulationAlgorithm=");
        sb.append(this.regulationAlgorithm.name()).append(
                ", audioPlaybackAmplitude=");
        sb.append(this.audioPlaybackAmplitude).append(
                ", disableRecordedPreAmplication=");
        sb.append(this.disableRecordedPreAmplication).append(
                ", initialBufferDelay=");
        sb.append(this.initialBufferDelay).append(", bufferDelay=");
        sb.append(this.bufferDelay).append("]");

        return sb.toString();
    }
}