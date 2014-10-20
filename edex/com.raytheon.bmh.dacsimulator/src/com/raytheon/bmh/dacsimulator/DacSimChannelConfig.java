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

/**
 * Configuration for a single channel in the simulated DAC.
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

public class DacSimChannelConfig {

    private final int channelNumber;

    private final int dataPort;

    private final int controlPort;

    /**
     * Constructor.
     * 
     * @param dataPort
     *            Port to listen on for audio data transmission.
     * @param controlPort
     *            Port to lisen on for heartbeat messages.
     * @param channel
     *            Channel number.
     */
    public DacSimChannelConfig(int dataPort, int controlPort, int channel) {
        this.dataPort = dataPort;
        this.controlPort = controlPort;
        this.channelNumber = channel;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DacSimChannelConfig [channelNumber=");
        builder.append(channelNumber);
        builder.append(", dataPort=");
        builder.append(dataPort);
        builder.append(", controlPort=");
        builder.append(controlPort);
        builder.append("]");
        return builder.toString();
    }

    public int getDataPort() {
        return dataPort;
    }

    public int getControlPort() {
        return controlPort;
    }

    public int getChannelNumber() {
        return channelNumber;
    }
}
