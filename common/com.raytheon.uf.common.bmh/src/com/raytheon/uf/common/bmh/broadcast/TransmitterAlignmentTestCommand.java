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
package com.raytheon.uf.common.bmh.broadcast;

import java.util.Set;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Used to trigger a transmitter alignment test. Sent to the comms manager and
 * used to configure a dac session that will be ran in maintenance mode.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 7, 2014  3630       bkowal      Initial creation
 * Nov 15, 2014 3630       bkowal      Added allowedDataPorts
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */
@DynamicSerialize
public class TransmitterAlignmentTestCommand extends
        AbstractOnDemandBroadcastMessage {

    @DynamicSerializeElement
    private String dacHostname;

    @DynamicSerializeElement
    private Set<Integer> allowedDataPorts;

    @DynamicSerializeElement
    private int[] radios;

    @DynamicSerializeElement
    private double decibelTarget;

    @DynamicSerializeElement
    private String inputAudioFile;

    @DynamicSerializeElement
    private int broadcastDuration;

    /**
     * 
     */
    public TransmitterAlignmentTestCommand() {
    }

    /**
     * @return the dacHostname
     */
    public String getDacHostname() {
        return dacHostname;
    }

    /**
     * @param dacHostname
     *            the dacHostname to set
     */
    public void setDacHostname(String dacHostname) {
        this.dacHostname = dacHostname;
    }

    /**
     * @return the allowedDataPorts
     */
    public Set<Integer> getAllowedDataPorts() {
        return allowedDataPorts;
    }

    /**
     * @param allowedDataPorts
     *            the allowedDataPorts to set
     */
    public void setAllowedDataPorts(Set<Integer> allowedDataPorts) {
        this.allowedDataPorts = allowedDataPorts;
    }

    /**
     * @return the radios
     */
    public int[] getRadios() {
        return radios;
    }

    /**
     * @param radios
     *            the radios to set
     */
    public void setRadios(int[] radios) {
        this.radios = radios;
    }

    /**
     * @return the decibelTarget
     */
    public double getDecibelTarget() {
        return decibelTarget;
    }

    /**
     * @param decibelTarget
     *            the decibelTarget to set
     */
    public void setDecibelTarget(double decibelTarget) {
        this.decibelTarget = decibelTarget;
    }

    /**
     * @return the inputAudioFile
     */
    public String getInputAudioFile() {
        return inputAudioFile;
    }

    /**
     * @param inputAudioFile
     *            the inputAudioFile to set
     */
    public void setInputAudioFile(String inputAudioFile) {
        this.inputAudioFile = inputAudioFile;
    }

    /**
     * @return the broadcastDuration
     */
    public int getBroadcastDuration() {
        return broadcastDuration;
    }

    /**
     * @param broadcastDuration
     *            the broadcastDuration to set
     */
    public void setBroadcastDuration(int broadcastDuration) {
        this.broadcastDuration = broadcastDuration;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder(
                "TransmitterAlignmentTestCommand [");
        stringBuilder.append("dacHostname=");
        stringBuilder.append(this.dacHostname);
        stringBuilder.append(", allowedDataPorts={");
        boolean firstPort = true;
        for (Integer dataPort : this.allowedDataPorts) {
            if (firstPort == false) {
                stringBuilder.append(", ");
            } else {
                firstPort = false;
            }
            stringBuilder.append(dataPort);
        }
        stringBuilder.append("}");
        stringBuilder.append(", radios=");
        StringBuilder radiosSB = new StringBuilder("{");
        boolean firstRadio = true;
        for (int radio : this.radios) {
            if (firstRadio == false) {
                radiosSB.append(", ");
            } else {
                firstRadio = false;
            }
            radiosSB.append(radio);
        }
        radiosSB.append("}");
        stringBuilder.append(radiosSB.toString());
        stringBuilder.append(", decibelTarget=");
        stringBuilder.append(this.decibelTarget);
        stringBuilder.append(", inputAudioFile=");
        stringBuilder.append(this.inputAudioFile);
        stringBuilder.append(", broadcastDuration=");
        stringBuilder.append(this.broadcastDuration);
        stringBuilder.append("]");

        return stringBuilder.toString();
    }
}