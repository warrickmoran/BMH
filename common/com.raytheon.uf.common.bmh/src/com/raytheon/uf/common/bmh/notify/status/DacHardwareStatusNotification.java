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

import org.apache.commons.lang3.builder.EqualsBuilder;

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
 * Jan 27, 2015  #4029     bkowal       Added {@link #equals(Object)} and {@link #toString()}.
 * Feb 09, 2016  #5082     bkowal       Updates for Apache commons lang 3.
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

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }

        DacHardwareStatusNotification other = (DacHardwareStatusNotification) obj;

        EqualsBuilder eq = new EqualsBuilder();
        eq.append(this.transmitterGroup, other.transmitterGroup);
        eq.append(this.psu1Voltage, other.psu1Voltage);
        eq.append(this.psu2Voltage, other.psu2Voltage);
        eq.append(this.bufferSize, other.bufferSize);
        eq.append(this.validChannels, other.validChannels);
        eq.append(this.outputGain, other.outputGain);
        eq.append(this.voiceStatus, other.voiceStatus);
        eq.append(this.recoverablePacketErrors, other.recoverablePacketErrors);
        eq.append(this.unrecoverablePacketErrors,
                other.unrecoverablePacketErrors);

        return eq.isEquals();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("DacHardwareStatusNotification [");
        sb.append("transmitterGroup=").append(this.transmitterGroup);
        sb.append(", psu1Voltage=").append(this.psu1Voltage);
        sb.append(", psu2Voltage=").append(this.psu2Voltage);
        sb.append(", bufferSize=").append(this.bufferSize);
        if (this.validChannels.length > 0) {
            sb.append(", validChannels={");
            boolean first = true;
            for (int channel : this.validChannels) {
                if (first) {
                    first = false;
                } else {
                    sb.append(", ");
                }
                sb.append(channel);
            }
            sb.append("}");
        }
        if (this.outputGain.length > 0) {
            sb.append(", outputGain={");
            boolean first = true;
            for (double gain : this.outputGain) {
                if (first) {
                    first = false;
                } else {
                    sb.append(", ");
                }
                sb.append(gain);
            }
            sb.append("}");
        }
        if (this.voiceStatus.length > 0) {
            sb.append(", voiceStatus={");
            boolean first = true;
            for (DacVoiceStatus status : this.voiceStatus) {
                if (first) {
                    first = false;
                } else {
                    sb.append(", ");
                }
                sb.append(status.toString());
            }
            sb.append("}");
        }
        sb.append(", recoverablePacketErrors=").append(
                this.recoverablePacketErrors);
        sb.append(", unrecoverablePacketErrors=").append(
                this.unrecoverablePacketErrors);
        sb.append("]");

        return sb.toString();
    }
}