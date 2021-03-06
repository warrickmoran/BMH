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
package com.raytheon.bmh.dactransmit.dacsession;

/**
 * Core configuration required to connect to and interact with a dac.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 6, 2014  3630       bkowal      Initial creation
 * Apr 29, 2015 4394       bkowal      Added {@link #managerPort}.
 * Oct 14, 2015 4984       rjpeter     Added {@link #setDbTarget(double)} and {@link #setTransmitters(Collection)}
 * Nov 04, 2015 5068       rjpeter     Switch audio units from dB to amplitude.
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

import java.net.InetAddress;
import java.util.Collection;

public class DacCommonConfig {

    protected final String dacHostname;

    protected final InetAddress dacAddress;

    protected final int dataPort;

    protected final int controlPort;

    protected volatile Collection<Integer> transmitters;

    protected volatile short audioAmplitude;

    private final int managerPort;

    public DacCommonConfig(String dacHostname, InetAddress dacAddress,
            int dataPort, int controlPort, Collection<Integer> transmitters,
            short audioAmplitude, int managerPort) {
        this.dacHostname = dacHostname;
        this.dacAddress = dacAddress;
        this.dataPort = dataPort;
        this.controlPort = controlPort;
        this.transmitters = transmitters;
        this.audioAmplitude = audioAmplitude;
        this.managerPort = managerPort;
    }

    /**
     * The original hostname for the dac as it was received from the command
     * line. This may be an actual hostname or a string representation of the IP
     * address. Depending on the format this may be equivelant to
     * {@link #getDacAddress()}.getHostName() but this is not guaranteed.
     * 
     * @return
     */
    public String getDacHostname() {
        return dacHostname;
    }

    /**
     * The resolved address of the {@link #dacHostname}.
     * 
     * @return
     */
    public InetAddress getDacAddress() {
        return dacAddress;
    }

    public int getDataPort() {
        return dataPort;
    }

    /**
     * @return the controlPort
     */
    public int getControlPort() {
        return controlPort;
    }

    public Collection<Integer> getTransmitters() {
        return transmitters;
    }

    /**
     * @param transmitters
     *            the transmitters to set
     */
    public void setTransmitters(Collection<Integer> transmitters) {
        this.transmitters = transmitters;
    }

    /**
     * @return the audioAmplitude
     */
    public short getAudioAmplitude() {
        return audioAmplitude;
    }

    /**
     * @param audioAmplitude
     *            the audioAmplitude to set
     */
    public void setAudioAmplitude(short audioAmplitude) {
        this.audioAmplitude = audioAmplitude;
    }

    /**
     * @return the managerPort
     */
    public int getManagerPort() {
        return managerPort;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("dacHostname=");
        stringBuilder.append(this.dacHostname);
        stringBuilder.append(", dataPort=");
        stringBuilder.append(this.dataPort);
        stringBuilder.append(", controlPort=");
        stringBuilder.append(this.controlPort);
        stringBuilder.append(", transmitters=");
        stringBuilder.append(this.transmitters);
        stringBuilder.append(", audioAmplitude=");
        stringBuilder.append(this.audioAmplitude);

        return stringBuilder.toString();
    }
}