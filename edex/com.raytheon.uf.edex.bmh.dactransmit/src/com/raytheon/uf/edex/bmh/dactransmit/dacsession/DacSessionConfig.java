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
package com.raytheon.uf.edex.bmh.dactransmit.dacsession;

import java.net.InetAddress;
import java.nio.file.Path;
import java.util.Collection;

/**
 * Configuration parameters for a DacSession object. Defines all the necessary
 * parameters to construct a DacSession so it can transmit data.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 01, 2014  #3286     dgilling     Initial creation
 * Jul 14, 2014  #3286     dgilling     Added transmitter group.
 * 
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

public final class DacSessionConfig {

    private final boolean printHelp;

    private final InetAddress dacAddress;

    private final int dataPort;

    private final int controlPort;

    private final Collection<Integer> transmitters;

    private final String transmitterGroup;

    private final Path inputDirectory;

    public DacSessionConfig(boolean printHelp) {
        this(printHelp, null, -1, -1, null, null, null);
    }

    public DacSessionConfig(boolean printHelp, InetAddress dacAddress,
            int dataPort, int controlPort, Collection<Integer> transmitters,
            String transmitterGroup, Path inputDirectory) {
        this.printHelp = printHelp;
        this.dacAddress = dacAddress;
        this.dataPort = dataPort;
        this.controlPort = controlPort;
        this.transmitters = transmitters;
        this.transmitterGroup = transmitterGroup;
        this.inputDirectory = inputDirectory;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "DacSessionConfig [dacAddress=" + dacAddress + ", dataPort="
                + dataPort + ", controlPort=" + controlPort + ", transmitters="
                + transmitters + ", transmitterGroup=" + transmitterGroup
                + ", inputDirectory=" + inputDirectory + "]";
    }

    public boolean isPrintHelp() {
        return printHelp;
    }

    public InetAddress getDacAddress() {
        return dacAddress;
    }

    public int getDataPort() {
        return dataPort;
    }

    public int getControlPort() {
        return controlPort;
    }

    public Collection<Integer> getTransmitters() {
        return transmitters;
    }

    public String getTransmitterGroup() {
        return transmitterGroup;
    }

    public Path getInputDirectory() {
        return inputDirectory;
    }
}
