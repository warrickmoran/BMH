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
package com.raytheon.uf.edex.bmh.comms.config;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

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
    private int ipcPort;

    @XmlElement
    private String dacTransmitStarter;

    @XmlElement
    private String jmsConnection;

    @XmlElement(name = "dac")
    private List<DacConfig> dacs;

    public List<DacConfig> getDacs() {
        return dacs;
    }

    public void setDacs(List<DacConfig> dacs) {
        this.dacs = dacs;
    }

    public int getIpcPort() {
        return ipcPort;
    }

    public void setIpcPort(int ipcPort) {
        this.ipcPort = ipcPort;
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

}
