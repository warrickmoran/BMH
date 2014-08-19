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

import org.apache.commons.lang.math.Range;

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
 * Jul 17, 2014  #3399     bsteffen     Add comms manager port argument.
 * Aug 12, 2014  #3486     bsteffen     Remove tranmistter group name
 * Aug 18, 2014  #3532     bkowal       Add transmitter decibel range.
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

    private final Path inputDirectory;

    private final int managerPort;

    private final Range dbRange;

    public DacSessionConfig(boolean printHelp) {
        this(printHelp, null, -1, -1, null, null, -1, null);
    }

    public DacSessionConfig(boolean printHelp, InetAddress dacAddress,
            int dataPort, int controlPort, Collection<Integer> transmitters,
            Path inputDirectory, int managerPort, Range dbRange) {
        this.printHelp = printHelp;
        this.dacAddress = dacAddress;
        this.dataPort = dataPort;
        this.controlPort = controlPort;
        this.transmitters = transmitters;
        this.inputDirectory = inputDirectory;
        this.managerPort = managerPort;
        this.dbRange = dbRange;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder(
                "DacSessionConfig [dacAddress=");
        stringBuilder.append(this.dacAddress);
        stringBuilder.append(", dataPort=");
        stringBuilder.append(this.dataPort);
        stringBuilder.append(", controlPort=");
        stringBuilder.append(this.controlPort);
        stringBuilder.append(", transmitters=");
        stringBuilder.append(this.transmitters);
        stringBuilder.append(", inputDirectory=");
        stringBuilder.append(this.inputDirectory);
        stringBuilder.append(", managerPort=");
        stringBuilder.append(this.managerPort);
        if (this.dbRange != null) {
            stringBuilder.append(", dbRange=");
            stringBuilder.append(dbRange.toString());
        }
        stringBuilder.append("]");

        return stringBuilder.toString();
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

    public Path getInputDirectory() {
        return inputDirectory;
    }

    public int getManagerPort() {
        return managerPort;
    }

    /**
     * @return the dbRange
     */
    public Range getDbRange() {
        return dbRange;
    }
}