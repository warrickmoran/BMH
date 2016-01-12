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
import java.util.HashSet;
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
 * Oct 16, 2014  3687     bsteffen    Implement practice mode.
 * Apr 07, 2015  4370     rjpeter     Added hashCode, equals, toString.
 * Oct 28, 2015  5029     rjpeter     getDacs cannot return null.
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
        if (dacs == null) {
            dacs = new HashSet<>(1, 1);
        }
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
     * @param broadcastLivePort
     *            the broadcastLivePort to set
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

    public static Path getDefaultPath(boolean operational) {
        Path confDir = Paths.get(BMHConstants.getBmhHomeDirectory()).resolve(
                "conf");
        if (operational) {
            return confDir.resolve("comms.xml");
        } else {
            return confDir.resolve("comms-practice.xml");
        }
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + broadcastLivePort;
        result = (prime * result)
                + ((clusterHosts == null) ? 0 : clusterHosts.hashCode());
        result = (prime * result) + clusterPort;
        result = (prime * result) + dacTransmitPort;
        result = (prime * result)
                + ((dacTransmitStarter == null) ? 0 : dacTransmitStarter
                        .hashCode());
        result = (prime * result) + ((dacs == null) ? 0 : dacs.hashCode());
        result = (prime * result)
                + ((jmsConnection == null) ? 0 : jmsConnection.hashCode());
        result = (prime * result) + lineTapPort;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        CommsConfig other = (CommsConfig) obj;
        if (broadcastLivePort != other.broadcastLivePort) {
            return false;
        }
        if (clusterHosts == null) {
            if (other.clusterHosts != null) {
                return false;
            }
        } else if (!clusterHosts.equals(other.clusterHosts)) {
            return false;
        }
        if (clusterPort != other.clusterPort) {
            return false;
        }
        if (dacTransmitPort != other.dacTransmitPort) {
            return false;
        }
        if (dacTransmitStarter == null) {
            if (other.dacTransmitStarter != null) {
                return false;
            }
        } else if (!dacTransmitStarter.equals(other.dacTransmitStarter)) {
            return false;
        }
        if (dacs == null) {
            if (other.dacs != null) {
                return false;
            }
        } else if (!dacs.equals(other.dacs)) {
            return false;
        }
        if (jmsConnection == null) {
            if (other.jmsConnection != null) {
                return false;
            }
        } else if (!jmsConnection.equals(other.jmsConnection)) {
            return false;
        }
        if (lineTapPort != other.lineTapPort) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "CommsConfig [clusterPort=" + clusterPort + ", dacTransmitPort="
                + dacTransmitPort + ", lineTapPort=" + lineTapPort
                + ", broadcastLivePort=" + broadcastLivePort
                + ", dacTransmitStarter=" + dacTransmitStarter
                + ", jmsConnection=" + jmsConnection + ", dacs=" + dacs
                + ", clusterHosts=" + clusterHosts + "]";
    }
}
