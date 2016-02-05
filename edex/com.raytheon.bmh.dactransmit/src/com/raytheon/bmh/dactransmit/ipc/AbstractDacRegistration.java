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
 * Abstraction of the registration records that allow a dac transmit to interface
 * with the comms manager.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 28, 2015 4394       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */
@DynamicSerialize
public abstract class AbstractDacRegistration {

    @DynamicSerializeElement
    private int dataPort;

    @DynamicSerializeElement
    private String dacAddress;

    @DynamicSerializeElement
    private int[] transmitters;

    /**
     * Empty constructor for {@link DynamicSerialize}.
     */
    public AbstractDacRegistration() {
    }

    public AbstractDacRegistration(int dataPort, String dacAddress,
            int[] transmitters) {
        this.dataPort = dataPort;
        this.dacAddress = dacAddress;
        this.transmitters = transmitters;
    }

    /**
     * @return the dataPort
     */
    public int getDataPort() {
        return dataPort;
    }

    /**
     * @param dataPort the dataPort to set
     */
    public void setDataPort(int dataPort) {
        this.dataPort = dataPort;
    }

    /**
     * @return the dacAddress
     */
    public String getDacAddress() {
        return dacAddress;
    }

    /**
     * @param dacAddress the dacAddress to set
     */
    public void setDacAddress(String dacAddress) {
        this.dacAddress = dacAddress;
    }

    /**
     * @return the transmitters
     */
    public int[] getTransmitters() {
        return transmitters;
    }

    /**
     * @param transmitters the transmitters to set
     */
    public void setTransmitters(int[] transmitters) {
        this.transmitters = transmitters;
    }
}