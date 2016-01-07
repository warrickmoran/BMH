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
 * The registration message sent by a Dac Transmit running in Maintenance Mode
 * to the Comms Manager.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 28, 2015 4394           bkowal     Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */
@DynamicSerialize
public class DacMaintenanceRegister extends AbstractDacRegistration {

    @DynamicSerializeElement
    private String messageFile;

    @DynamicSerializeElement
    private String transmitterGroup;

    /**
     * Empty constructor for {@link DynamicSerialize}.
     */
    public DacMaintenanceRegister() {
    }

    public DacMaintenanceRegister(int dataPort, String dacAddress,
            int[] transmitters, String messageFile, String transmitterGroup) {
        super(dataPort, dacAddress, transmitters);
        this.messageFile = messageFile;
        this.transmitterGroup = transmitterGroup;
    }

    /**
     * @return the messageFile
     */
    public String getMessageFile() {
        return messageFile;
    }

    /**
     * @param messageFile
     *            the messageFile to set
     */
    public void setMessageFile(String messageFile) {
        this.messageFile = messageFile;
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