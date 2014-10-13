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
package com.raytheon.uf.edex.bmh.comms;

import java.io.File;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.raytheon.uf.edex.bmh.BMHConstants;

/**
 * 
 * Basic confic object for comms manager.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Jul 16, 2014  3399     bsteffen    Initial creation
 * Aug 04, 2014  2487     bsteffen    Rename config options.
 * Sep 25, 2014  3485     bsteffen    Add cluster options
 * Sep 29, 2014  3291     bkowal      Updated to use the bmh home directory.
 * Oct 10, 2014  3656     bkowal      Added broadcastLivePort
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
@XmlRootElement(name = "commsConfig")
@XmlAccessorType(XmlAccessType.NONE)
public class CommsConfig {

    @XmlAttribute
    private int clusterPort = 58258;

    @XmlAttribute
    private int dacTransmitPort = 58259;

    @XmlAttribute
    private int lineTapPort = 58260;
    
    @XmlAttribute
    private int broadcastLivePort = 58269;

    @XmlElement
    private String dacTransmitStarter = BMHConstants.getBmhHomeDirectory()
            + File.separator + "bin" + File.separator + "dactransmit.sh";

    @XmlElement
    private String jmsConnection;

    @XmlElement(name = "dac")
    private Set<DacConfig> dacs;

    @XmlElement(name = "clusterHost")
    private Set<CommsHostConfig> clusterHosts;

    public Set<DacConfig> getDacs() {
        return dacs;
    }

    public void setDacs(Set<DacConfig> dacs) {
        this.dacs = dacs;
    }

    public int getClusterPort() {
        return clusterPort;
    }

    public void setClusterPort(int clusterPort) {
        this.clusterPort = clusterPort;
    }

    public int getDacTransmitPort() {
        return dacTransmitPort;
    }

    public void setDacTransmitPort(int dacTransmitPort) {
        this.dacTransmitPort = dacTransmitPort;
    }

    public int getLineTapPort() {
        return lineTapPort;
    }

    public void setLineTapPort(int lineTapPort) {
        this.lineTapPort = lineTapPort;
    }

    /**
     * @return the broadcastLivePort
     */
    public int getBroadcastLivePort() {
        return broadcastLivePort;
    }

    /**
     * @param broadcastLivePort the broadcastLivePort to set
     */
    public void setBroadcastLivePort(int broadcastLivePort) {
        this.broadcastLivePort = broadcastLivePort;
    }

    public String getDacTransmitStarter() {
        return dacTransmitStarter;
    }

    public void setDacTransmitStarter(String dacTransmitStarter) {
        this.dacTransmitStarter = dacTransmitStarter;
    }

    public String getJmsConnection() {
        return jmsConnection;
    }

    public void setJmsConnection(String jmsConnection) {
        this.jmsConnection = jmsConnection;
    }

    public static Path getDefaultPath() {
        return Paths.get(BMHConstants.getBmhHomeDirectory()).resolve("conf")
                .resolve("comms.xml");
    }

    public Set<CommsHostConfig> getClusterHosts() {
        return clusterHosts;
    }

    public void setClusterHosts(Set<CommsHostConfig> clusterHosts) {
        this.clusterHosts = clusterHosts;
    }

    public CommsHostConfig getLocalClusterHost() throws UnknownHostException,
            SocketException {
        if (clusterHosts == null) {
            return null;
        } else {
            for (CommsHostConfig clusterHost : clusterHosts) {
                if (clusterHost.isLocalHost()) {
                    return clusterHost;
                }
            }
            return null;
        }

    }

}
