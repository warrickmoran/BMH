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
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
@DynamicSerialize
public class DacTransmitRegister {

    @DynamicSerializeElement
    private String inputDirectory;

    @DynamicSerializeElement
    private int dataPort;

    @DynamicSerializeElement
    private String dacAddress;

    @DynamicSerializeElement
    private int[] transmitters;

    @DynamicSerializeElement
    private double dbTarget;

    public DacTransmitRegister() {

    }

    public DacTransmitRegister(String inputDirectory, int dataPort,
            String dacAddress, int[] transmitters, double dbTarget) {
        super();
        this.inputDirectory = inputDirectory;
        this.dataPort = dataPort;
        this.dacAddress = dacAddress;
        this.transmitters = transmitters;
        this.dbTarget = dbTarget;
    }

    public String getInputDirectory() {
        return inputDirectory;
    }

    public void setInputDirectory(String inputDirectory) {
        this.inputDirectory = inputDirectory;
    }

    public int getDataPort() {
        return dataPort;
    }

    public void setDataPort(int dataPort) {
        this.dataPort = dataPort;
    }

    public String getDacAddress() {
        return dacAddress;
    }

    public void setDacAddress(String dacAddress) {
        this.dacAddress = dacAddress;
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
    public double getDbTarget() {
        return dbTarget;
    }

    /**
     * @param dbTarget
     *            the dbTarget to set
     */
    public void setDbTarget(double dbTarget) {
        this.dbTarget = dbTarget;
    }
}