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
 * 
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
     * Do not alter audio with a decibel range below this field.
     */
    @DynamicSerializeElement
    private double dbSilenceLimit;

    /*
     * Disables the use of the dbSilenceLimit field.
     */
    @DynamicSerializeElement
    private boolean disableSilenceLimit;

    /*
     * Do not alter audio with a decibel range above this field.
     */
    @DynamicSerializeElement
    private double dbMaxLimit;

    /*
     * Disables the use of the dbMaxLimit field.
     */
    @DynamicSerializeElement
    private boolean disableMaxLimit;

    @DynamicSerializeElement
    private ALGORITHM regulationAlgorithm;

    @DynamicSerializeElement
    private double audioPlaybackVolume;

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
     * @return the dbSilenceLimit
     */
    public double getDbSilenceLimit() {
        return dbSilenceLimit;
    }

    /**
     * @param dbSilenceLimit
     *            the dbSilenceLimit to set
     */
    public void setDbSilenceLimit(double dbSilenceLimit) {
        this.dbSilenceLimit = dbSilenceLimit;
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
     * @return the dbMaxLimit
     */
    public double getDbMaxLimit() {
        return dbMaxLimit;
    }

    /**
     * @param dbMaxLimit
     *            the dbMaxLimit to set
     */
    public void setDbMaxLimit(double dbMaxLimit) {
        this.dbMaxLimit = dbMaxLimit;
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
     * @return the audioPlaybackVolume
     */
    public double getAudioPlaybackVolume() {
        return audioPlaybackVolume;
    }

    /**
     * @param audioPlaybackVolume
     *            the audioPlaybackVolume to set
     */
    public void setAudioPlaybackVolume(double audioPlaybackVolume) {
        this.audioPlaybackVolume = audioPlaybackVolume;
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
                "AudioRegulationConfiguration [dbSilenceLimit=");
        sb.append(this.dbSilenceLimit).append(", disableSilenceLimit=");
        sb.append(this.disableSilenceLimit).append(", dbMaxLimit=");
        sb.append(this.dbMaxLimit).append(", disableMaxLimit=");
        sb.append(this.disableMaxLimit).append(", regulationAlgorithm=");
        sb.append(this.regulationAlgorithm.name()).append(
                ", audioPlaybackVolume=");
        sb.append(this.audioPlaybackVolume).append(
                ", disableRecordedPreAmplication=");
        sb.append(this.disableRecordedPreAmplication).append(
                ", initialBufferDelay=");
        sb.append(this.initialBufferDelay).append(", bufferDelay=");
        sb.append(this.bufferDelay).append("]");

        return sb.toString();
    }
}