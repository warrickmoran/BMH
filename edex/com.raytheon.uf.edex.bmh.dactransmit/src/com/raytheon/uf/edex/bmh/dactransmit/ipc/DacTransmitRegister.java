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
package com.raytheon.uf.edex.bmh.dactransmit.ipc;

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
 * 
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
    private double audioDbTarget;
    
    @DynamicSerializeElement
    private double sameDbTarget;
    
    @DynamicSerializeElement
    private double alertDbTarget;
    
    @DynamicSerializeElement
    private String transmitterGroup;

    public DacTransmitRegister() {
    }

    public DacTransmitRegister(String inputDirectory, int dataPort,
            String dacAddress, int[] transmitters, double audioDbTarget,
            double sameDbTarget, double alertDbTarget, String transmitterGroup) {
        super(dataPort, dacAddress, transmitters);
        this.inputDirectory = inputDirectory;
        this.transmitters = transmitters;
        this.audioDbTarget = audioDbTarget;
        this.sameDbTarget = sameDbTarget;
        this.alertDbTarget = alertDbTarget;
        this.transmitterGroup = transmitterGroup;
    }

    public String getInputDirectory() {
        return inputDirectory;
    }

    public void setInputDirectory(String inputDirectory) {
        this.inputDirectory = inputDirectory;
    }

    public int[] getTransmitters() {
        return transmitters;
    }

    public void setTransmitters(int[] transmitters) {
        this.transmitters = transmitters;
    }

    /**
     * @return the dbTarget
     */
    public double getAudioDbTarget() {
        return audioDbTarget;
    }

    /**
     * @param audioDbTarget
     *            the dbTarget to set
     */
    public void setAudioDbTarget(double audioDbTarget) {
        this.audioDbTarget = audioDbTarget;
    }

    /**
     * @return the sameDbTarget
     */
    public double getSameDbTarget() {
        return sameDbTarget;
    }

    /**
     * @param sameDbTarget the sameDbTarget to set
     */
    public void setSameDbTarget(double sameDbTarget) {
        this.sameDbTarget = sameDbTarget;
    }

    /**
     * @return the alertDbTarget
     */
    public double getAlertDbTarget() {
        return alertDbTarget;
    }

    /**
     * @param alertDbTarget the alertDbTarget to set
     */
    public void setAlertDbTarget(double alertDbTarget) {
        this.alertDbTarget = alertDbTarget;
    }

    /**
     * @return the transmitterGroup
     */
    public String getTransmitterGroup() {
        return transmitterGroup;
    }

    /**
     * @param transmitterGroup the transmitterGroup to set
     */
    public void setTransmitterGroup(String transmitterGroup) {
        this.transmitterGroup = transmitterGroup;
    }
}