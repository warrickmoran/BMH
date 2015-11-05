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

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Used to trigger transmitter maintenance operations(alignment test or transfer
 * tones). Sent to the comms manager and used to configure a dac session that
 * will be ran in maintenance mode.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 7, 2014  3630       bkowal      Initial creation
 * Nov 15, 2014 3630       bkowal      Added allowedDataPorts
 * Dec 12, 2014 3603       bsteffen    Rename and add maintenanceDetails
 * Apr 09, 2015 4364       bkowal      Added {@link #broadcastTimeout}.
 * Jul 01, 2015 4602       rjpeter     Use specific dataport.
 * Nov 04, 2015 5068       rjpeter     Switch audio units from dB to amplitude.
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */
@DynamicSerialize
public class TransmitterMaintenanceCommand extends
        AbstractOnDemandBroadcastMessage {

    @DynamicSerializeElement
    private String maintenanceDetails;

    @DynamicSerializeElement
    private String dacHostname;

    @DynamicSerializeElement
    private int dataPort;

    @DynamicSerializeElement
    private int[] radios;

    @DynamicSerializeElement
    private short audioAmplitude;

    @DynamicSerializeElement
    private String inputAudioFile;

    @DynamicSerializeElement
    private int broadcastDuration;

    @DynamicSerializeElement
    private int broadcastTimeout;

    public TransmitterMaintenanceCommand() {
    }

    public String getMaintenanceDetails() {
        return maintenanceDetails;
    }

    public void setMaintenanceDetails(String maintenanceDetails) {
        this.maintenanceDetails = maintenanceDetails;
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
    public int getDataPort() {
        return dataPort;
    }

    /**
     * @param dataPort
     *            the dataPort to set
     */
    public void setDataPort(int dataPort) {
        this.dataPort = dataPort;
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
     * @return the audioAmplitude
     */
    public short getAudioAmplitude() {
        return audioAmplitude;
    }

    /**
     * @param audioAmplitude
     *            the audioAmplitude to set
     */
    public void setAudioAmplitude(short audioAmplitude) {
        this.audioAmplitude = audioAmplitude;
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

    /**
     * @return the broadcastTimeout (in minutes)
     */
    public int getBroadcastTimeout() {
        return broadcastTimeout;
    }

    /**
     * @param broadcastTimeout
     *            the broadcastTimeout to set (in minutes)
     */
    public void setBroadcastTimeout(int broadcastTimeout) {
        this.broadcastTimeout = broadcastTimeout;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder(
                "TransmitterMaintenanceCommand [");
        stringBuilder.append("maintenanceDetails=");
        stringBuilder.append(this.maintenanceDetails);
        stringBuilder.append("dacHostname=");
        stringBuilder.append(this.dacHostname);
        stringBuilder.append(", dataPort=");
        stringBuilder.append(this.dataPort);
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
        stringBuilder.append(", audioAmplitude=");
        stringBuilder.append(this.audioAmplitude);
        stringBuilder.append(", inputAudioFile=");
        stringBuilder.append(this.inputAudioFile);
        stringBuilder.append(", broadcastDuration=");
        stringBuilder.append(this.broadcastDuration);
        stringBuilder.append(", broadcastTimeout=");
        stringBuilder.append(this.broadcastTimeout);
        stringBuilder.append("]");

        return stringBuilder.toString();
    }
}