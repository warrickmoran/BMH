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

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * 
 * Config for a comms manager cluster host.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Sep 25, 2014  3485     bsteffen    Initial creation
 * Apr 07, 2015  4370     rjpeter     Added toString.
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
@XmlAccessorType(XmlAccessType.NONE)
public class CommsHostConfig {

    @XmlAttribute
    private String ipAddress;

    @XmlAttribute
    private String dacInterface;

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getDacInterface() {
        return dacInterface;
    }

    public void setDacInterface(String dacInterface) {
        this.dacInterface = dacInterface;
    }

    public boolean isLocalHost() throws UnknownHostException, SocketException {
        if (ipAddress == null) {
            return false;
        }
        InetAddress address = InetAddress.getByName(ipAddress);
        return NetworkInterface.getByInetAddress(address) != null;
    }

    public NetworkInterface getDacNetworkInterface()
            throws UnknownHostException, SocketException {
        if ((dacInterface == null) || dacInterface.isEmpty()) {
            return null;
        } else {
            try {
                return NetworkInterface.getByName(dacInterface);
            } catch (SocketException e) {
                InetAddress address = InetAddress.getByName(dacInterface);
                return NetworkInterface.getByInetAddress(address);

            }
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result)
                + ((dacInterface == null) ? 0 : dacInterface.hashCode());
        result = (prime * result)
                + ((ipAddress == null) ? 0 : ipAddress.hashCode());
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
        CommsHostConfig other = (CommsHostConfig) obj;
        if (dacInterface == null) {
            if (other.dacInterface != null) {
                return false;
            }
        } else if (!dacInterface.equals(other.dacInterface)) {
            return false;
        }
        if (ipAddress == null) {
            if (other.ipAddress != null) {
                return false;
            }
        } else if (!ipAddress.equals(other.ipAddress)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "CommsHostConfig [ipAddress=" + ipAddress + ", dacInterface="
                + dacInterface + "]";
    }

}
