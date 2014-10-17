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
package com.raytheon.bmh.dacsimulator;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Configuration information for an entire simulated DAC device.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 08, 2014  #3688     dgilling     Initial creation
 * 
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

public class DacSimulatorConfig {

    private final boolean printHelp;

    private final InetAddress rebroadcastAddress;

    private final int rebroadcastPort;

    private final int minimumBufferSize;

    private final List<DacSimChannelConfig> dacChannels;

    /**
     * Constructor to use if you want DacSimulator to simply print out its usage
     * statement. Don't use this constructor in any other circumstance.
     * 
     * @param printHelp
     *            Should always be called with this argument set to {@code true}
     *            so the usage statement is printed.
     */
    public DacSimulatorConfig(boolean printHelp) {
        this(printHelp, 0, -1, null, -1, -1);
    }

    /**
     * Construct a regular simulated DAC instance.
     * 
     * @param printHelp
     *            Whether or not to print the usage statement. If set to
     *            {@code true}, all other arguments will be ignored.
     * @param numChannels
     *            Number of channels on this simulated DAC.
     * @param firstChannelPort
     *            The first port to reserve. This port and the next numChannels
     *            * 2 - 1 ports will be reserved for communicating with
     *            DacTransmit instances.
     * @param rebroadcastAddresss
     *            Address to rebroadcast audio from all channels to.
     * @param rebroadcastPort
     *            Port to rebroadcast audio from all channels to.
     * @param bufferSize
     *            Minimum {@code JitterBuffer} size. Audio broadcast will not
     *            start until the buffer has at least this many packets in it.
     */
    public DacSimulatorConfig(boolean printHelp, int numChannels,
            int firstChannelPort, InetAddress rebroadcastAddresss,
            int rebroadcastPort, int bufferSize) {
        this.printHelp = printHelp;
        this.rebroadcastAddress = rebroadcastAddresss;
        this.rebroadcastPort = rebroadcastPort;
        this.minimumBufferSize = bufferSize;

        this.dacChannels = new ArrayList<>(numChannels);
        int port = firstChannelPort;
        for (int i = 0; i < numChannels; i++) {
            int dataPort = port;
            int controlPort = ++port;
            this.dacChannels.add(new DacSimChannelConfig(dataPort, controlPort,
                    (i + 1)));
            port++;
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DacSimulatorConfig [rebroadcastAddress=");
        builder.append(rebroadcastAddress);
        builder.append(", rebroadcastPort=");
        builder.append(rebroadcastPort);
        builder.append(", minimumBufferSize=");
        builder.append(minimumBufferSize);
        builder.append(", dacChannels=");
        builder.append(dacChannels);
        builder.append("]");
        return builder.toString();
    }

    public InetAddress getRebroadcastAddress() {
        return rebroadcastAddress;
    }

    public int getRebroadcastPort() {
        return rebroadcastPort;
    }

    public int getMinimumBufferSize() {
        return minimumBufferSize;
    }

    public List<DacSimChannelConfig> getDacChannels() {
        return dacChannels;
    }

    public boolean isPrintHelp() {
        return printHelp;
    }
}
