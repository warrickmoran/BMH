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
package com.raytheon.bmh.dactransmit.ipc;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * 
 * Message sent by the dac transmit application to comms manager on initial
 * connection.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Jul 16, 2014  3399     bsteffen    Initial creation
 * Aug 12, 2014  3486     bsteffen    Remove tranmistter group name
 * Aug 18, 2014  3532     bkowal      Support transmitter decibel range
 * Sep 5, 2014   3532     bkowal      Use a decibel target instead of a range.
 * Apr 29, 2015  4394     bkowal      Extend {@link AbstractDacRegistration}.
 * Jul 08, 2015 4636      bkowal      Support same and alert decibel levels.
 * Aug 12, 2015  4424     bkowal      Eliminate Dac Transmit Key.
 * Nov 04, 2015 5068      rjpeter     Switch audio units from dB to amplitude.
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
@DynamicSerialize
public class DacTransmitRegister extends AbstractDacRegistration {

    @DynamicSerializeElement
    private String inputDirectory;

    @DynamicSerializeElement
    private int[] transmitters;

    @DynamicSerializeElement
    private short audioAmplitude;

    @DynamicSerializeElement
    private short sameAmplitude;

    @DynamicSerializeElement
    private short alertAmplitude;

    @DynamicSerializeElement
    private String transmitterGroup;

    public DacTransmitRegister() {
    }

    public DacTransmitRegister(String inputDirectory, int dataPort,
            String dacAddress, int[] transmitters, short audioAmplitude,
            short sameAmplitude, short alertAmplitude, String transmitterGroup) {
        super(dataPort, dacAddress, transmitters);
        this.inputDirectory = inputDirectory;
        this.transmitters = transmitters;
        this.audioAmplitude = audioAmplitude;
        this.sameAmplitude = sameAmplitude;
        this.alertAmplitude = alertAmplitude;
        this.transmitterGroup = transmitterGroup;
    }

    public String getInputDirectory() {
        return inputDirectory;
    }

    public void setInputDirectory(String inputDirectory) {
        this.inputDirectory = inputDirectory;
    }

    @Override
    public int[] getTransmitters() {
        return transmitters;
    }

    @Override
    public void setTransmitters(int[] transmitters) {
        this.transmitters = transmitters;
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
     * @return the sameAmplitude
     */
    public short getSameAmplitude() {
        return sameAmplitude;
    }

    /**
     * @param sameAmplitude
     *            the sameAmplitude to set
     */
    public void setSameAmplitude(short sameAmplitude) {
        this.sameAmplitude = sameAmplitude;
    }

    /**
     * @return the alertAmplitude
     */
    public short getAlertAmplitude() {
        return alertAmplitude;
    }

    /**
     * @param alertAmplitude
     *            the alertAmplitude to set
     */
    public void setAlertAmplitude(short alertAmplitude) {
        this.alertAmplitude = alertAmplitude;
    }

    /**
     * @return the transmitterGroup
     */
    public String getTransmitterGroup() {
        return transmitterGroup;
    }

    /**
     * @param transmitterGroup
     *            the transmitterGroup to set
     */
    public void setTransmitterGroup(String transmitterGroup) {
        this.transmitterGroup = transmitterGroup;
    }
}