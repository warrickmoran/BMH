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
package com.raytheon.bmh.comms;

import com.raytheon.uf.edex.bmh.comms.DacChannelConfig;
import com.raytheon.uf.edex.bmh.comms.DacConfig;


/**
 * 
 * Unique map key for finding config information about connected DAC transmit
 * applications.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Aug 12, 2014  3486     bsteffen    Initial Creation
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class DacTransmitKey {
    private final String inputDirectory;

    private final int dataPort;

    private final String dacAddress;

    private final int hashCode;

    public DacTransmitKey(String inputDirectory, int dataPort,
            String dacAddress) {
        super();
        this.inputDirectory = inputDirectory;
        this.dataPort = dataPort;
        this.dacAddress = dacAddress;
        final int prime = 31;
        int hashCode = 1;
        hashCode = prime * hashCode + dataPort;
        hashCode = prime * hashCode
                + ((dacAddress == null) ? 0 : dacAddress.hashCode());
        hashCode = prime
                * hashCode
                + ((inputDirectory == null) ? 0 : inputDirectory.hashCode());
        this.hashCode = hashCode;
    }

    public DacTransmitKey(DacConfig dac, DacChannelConfig channel) {
        this(channel.getInputDirectory().toString(), channel.getDataPort(),
                dac.getIpAddress());
    }

    public String getInputDirectory() {
        return inputDirectory;
    }

    public int getDataPort() {
        return dataPort;
    }

    public String getDacAddress() {
        return dacAddress;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DacTransmitKey other = (DacTransmitKey) obj;
        if (dataPort != other.dataPort)
            return false;
        if (dacAddress == null) {
            if (other.dacAddress != null)
                return false;
        } else if (!dacAddress.equals(other.dacAddress))
            return false;
        if (inputDirectory == null) {
            if (other.inputDirectory != null)
                return false;
        } else if (!inputDirectory.equals(other.inputDirectory))
            return false;
        return true;
    }

}