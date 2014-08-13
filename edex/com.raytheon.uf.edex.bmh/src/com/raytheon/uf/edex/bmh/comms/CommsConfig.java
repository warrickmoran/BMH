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
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
@XmlRootElement(name = "dacs")
@XmlAccessorType(XmlAccessType.NONE)
public class CommsConfig {

    @XmlAttribute
    private int dacTransmitPort = 58259;

    @XmlAttribute
    private int lineTapPort = 58260;

    @XmlElement
    private String dacTransmitStarter = "/awips2/bmh/bin/dactransmit.sh";

    @XmlElement
    private String jmsConnection;

    @XmlElement(name = "dac")
    private Set<DacConfig> dacs;

    public Set<DacConfig> getDacs() {
        return dacs;
    }

    public void setDacs(Set<DacConfig> dacs) {
        this.dacs = dacs;
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
        return Paths.get(BMHConstants.getBmhDataDirectory())
                .resolveSibling("conf").resolve("comms.xml");
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + dacTransmitPort;
        result = prime
                * result
                + ((dacTransmitStarter == null) ? 0 : dacTransmitStarter
                        .hashCode());
        result = prime * result + ((dacs == null) ? 0 : dacs.hashCode());
        result = prime * result
                + ((jmsConnection == null) ? 0 : jmsConnection.hashCode());
        result = prime * result + lineTapPort;
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
        CommsConfig other = (CommsConfig) obj;
        if (dacTransmitPort != other.dacTransmitPort)
            return false;
        if (dacTransmitStarter == null) {
            if (other.dacTransmitStarter != null)
                return false;
        } else if (!dacTransmitStarter.equals(other.dacTransmitStarter))
            return false;
        if (dacs == null) {
            if (other.dacs != null)
                return false;
        } else if (!dacs.equals(other.dacs))
            return false;
        if (jmsConnection == null) {
            if (other.jmsConnection != null)
                return false;
        } else if (!jmsConnection.equals(other.jmsConnection))
            return false;
        if (lineTapPort != other.lineTapPort)
            return false;
        return true;
    }
}
