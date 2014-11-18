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
package com.raytheon.uf.common.bmh.notify.status;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Notification for DAC hardware state.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 31, 2014  #3286     dgilling     Initial creation
 * 
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */
@DynamicSerialize
public final class DacHardwareStatusNotification {

    @DynamicSerializeElement
    private String transmitterGroup;

    @DynamicSerializeElement
    private double psu1Voltage;

    @DynamicSerializeElement
    private double psu2Voltage;

    @DynamicSerializeElement
    private int bufferSize;

    @DynamicSerializeElement
    private int[] validChannels;

    @DynamicSerializeElement
    private double[] outputGain;

    @DynamicSerializeElement
    private DacVoiceStatus[] voiceStatus;

    @DynamicSerializeElement
    private int recoverablePacketErrors;

    @DynamicSerializeElement
    private int unrecoverablePacketErrors;

    public DacHardwareStatusNotification() {
        // for serialization use only
    }

    /**
     * Constructor.
     * 
     * @param transmitterGroup
     *            Transmitter group this notification applies to.
     * @param psu1Voltage
     *            Current voltage reading of DAC's first power supply.
     * @param psu2Voltage
     *            Current voltage reading of DAC's second power supply.
     * @param bufferSize
     *            Current size of DAC jitter buffer for this connection.
     * @param validChannels
     *            Destination transmitter ports for this connections. Valid
     *            values are 1-4.
     * @param outputGain
     *            Output gain levels for the channels being used for this
     *            transmission.
     * @param voiceStatus
     *            {@code DacVoiceStatus} values for the channels being used for
     *            this transmission.
     * @param recoverablePacketErrors
     *            Number of recoverable packet errors for this session.
     * @param unrecoverablePacketErrors
     *            Number of unrecoverable packet errors for this session.
     */
    public DacHardwareStatusNotification(String transmitterGroup,
            double psu1Voltage, double psu2Voltage, int bufferSize,
            int[] validChannels, double[] outputGain,
            DacVoiceStatus[] voiceStatus, int recoverablePacketErrors,
            int unrecoverablePacketErrors) {
        this.transmitterGroup = transmitterGroup;
        this.psu1Voltage = psu1Voltage;
        this.psu2Voltage = psu2Voltage;
        this.bufferSize = bufferSize;
        this.validChannels = validChannels;
        this.outputGain = outputGain;
        this.voiceStatus = voiceStatus;
        this.recoverablePacketErrors = recoverablePacketErrors;
        this.unrecoverablePacketErrors = unrecoverablePacketErrors;
    }

    public String getTransmitterGroup() {
        return transmitterGroup;
    }

    public void setTransmitterGroup(String transmitterGroup) {
        this.transmitterGroup = transmitterGroup;
    }

    public double getPsu1Voltage() {
        return psu1Voltage;
    }

    public void setPsu1Voltage(double psu1Voltage) {
        this.psu1Voltage = psu1Voltage;
    }

    public double getPsu2Voltage() {
        return psu2Voltage;
    }

    public void setPsu2Voltage(double psu2Voltage) {
        this.psu2Voltage = psu2Voltage;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public int[] getValidChannels() {
        return validChannels;
    }

    public void setValidChannels(int[] validChannels) {
        this.validChannels = validChannels;
    }

    public double[] getOutputGain() {
        return outputGain;
    }

    public void setOutputGain(double[] outputGain) {
        this.outputGain = outputGain;
    }

    public DacVoiceStatus[] getVoiceStatus() {
        return voiceStatus;
    }

    public void setVoiceStatus(DacVoiceStatus[] voiceStatus) {
        this.voiceStatus = voiceStatus;
    }

    public int getRecoverablePacketErrors() {
        return recoverablePacketErrors;
    }

    public void setRecoverablePacketErrors(int recoverablePacketErrors) {
        this.recoverablePacketErrors = recoverablePacketErrors;
    }

    public int getUnrecoverablePacketErrors() {
        return unrecoverablePacketErrors;
    }

    public void setUnrecoverablePacketErrors(int unrecoverablePacketErrors) {
        this.unrecoverablePacketErrors = unrecoverablePacketErrors;
    }
}
