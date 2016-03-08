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
 * Message sent from the dac transmit application to the comms manager to
 * indicate the current status of the connection to the dac.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Jul 16, 2014  3399     bsteffen    Initial creation
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
@DynamicSerialize
public class DacTransmitStatus {

    @DynamicSerializeElement
    private boolean connectedToDac;

    public DacTransmitStatus() {
    }

    public DacTransmitStatus(boolean connectedToDAC) {
        this.connectedToDac = connectedToDAC;
    }

    public boolean isConnectedToDac() {
        return connectedToDac;
    }

    public void setConnectedToDac(boolean connectedToDac) {
        this.connectedToDac = connectedToDac;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (connectedToDac ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DacTransmitStatus other = (DacTransmitStatus) obj;
        if (connectedToDac != other.connectedToDac)
            return false;
        return true;
    }

}
