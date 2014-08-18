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
import java.util.Arrays;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.raytheon.uf.edex.bmh.BMHConstants;

/**
 * 
 * Config for a single group on the DAC.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Jul 16, 2014  3399     bsteffen    Initial creation
 * Aug 12, 2014  3486     bsteffen    Add getInputDirectory
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
@XmlAccessorType(XmlAccessType.NONE)
public class DacChannelConfig {

    @XmlAttribute
    private String transmitterGroup;

    @XmlAttribute
    private int[] radios;
    
    @XmlAttribute
    private int dataPort;
    
    @XmlAttribute
    private Integer controlPort;

    public Path getInputDirectory() {
        return Paths.get(BMHConstants.getBmhDataDirectory(),
                "playlist", transmitterGroup);
    }

    public String getTransmitterGroup() {
        return transmitterGroup;
    }

    public void setTransmitterGroup(String transmitterGroup) {
        this.transmitterGroup = transmitterGroup;
    }

    public int[] getRadios() {
        return radios;
    }

    public void setRadios(int[] radios) {
        this.radios = radios;
    }

    public int getDataPort() {
        return dataPort;
    }

    public void setDataPort(int dataPort) {
        this.dataPort = dataPort;
    }

    public Integer getControlPort() {
        return controlPort;
    }

    public void setControlPort(Integer controlPort) {
        this.controlPort = controlPort;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((controlPort == null) ? 0 : controlPort.hashCode());
        result = prime * result + dataPort;
        result = prime * result + Arrays.hashCode(radios);
        result = prime
                * result
                + ((transmitterGroup == null) ? 0 : transmitterGroup.hashCode());
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
        DacChannelConfig other = (DacChannelConfig) obj;
        if (controlPort == null) {
            if (other.controlPort != null)
                return false;
        } else if (!controlPort.equals(other.controlPort))
            return false;
        if (dataPort != other.dataPort)
            return false;
        if (!Arrays.equals(radios, other.radios))
            return false;
        if (transmitterGroup == null) {
            if (other.transmitterGroup != null)
                return false;
        } else if (!transmitterGroup.equals(other.transmitterGroup))
            return false;
        return true;
    }

}