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
 * Aug 18, 2014  3532     bkowal      Added transmitter decibel range
 * Sep 5, 2014   3532     bkowal      Replaced decibel range with decibel target
 * Oct 2, 2014   3642     bkowal      Added transmitter timezone.
 * Oct 16, 2014  3687     bsteffen    Add playlistDirectory to xml.
 * Nov 26, 2014  3821     bsteffen    Add silenceAlarm
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
    private String playlistDirectory;

    @XmlAttribute
    private double dbTarget;

    @XmlAttribute
    private String timezone;

    @XmlAttribute
    private int[] radios;

    @XmlAttribute
    private int dataPort;

    @XmlAttribute
    private Integer controlPort;

    @XmlAttribute
    private boolean silenceAlarm;

    public Path getInputDirectory() {
        return Paths.get(playlistDirectory);
    }

    public String getTransmitterGroup() {
        return transmitterGroup;
    }

    public void setTransmitterGroup(String transmitterGroup) {
        this.transmitterGroup = transmitterGroup;
    }

    public String getPlaylistDirectory() {
        return playlistDirectory;
    }

    public void setPlaylistDirectory(String playlistDirectory) {
        this.playlistDirectory = playlistDirectory;
    }

    public void setPlaylistDirectoryPath(Path playlistDirectory) {
        this.playlistDirectory = playlistDirectory.toString();
    }

    /**
     * @return the dbTarget
     */
    public double getDbTarget() {
        return dbTarget;
    }

    /**
     * @param dbTarget
     *            the dbTarget to set
     */
    public void setDbTarget(double dbTarget) {
        this.dbTarget = dbTarget;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
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

    public boolean isSilenceAlarm() {
        return silenceAlarm;
    }

    public void setSilenceAlarm(boolean silenceAlarm) {
        this.silenceAlarm = silenceAlarm;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((controlPort == null) ? 0 : controlPort.hashCode());
        result = prime * result + dataPort;
        long temp;
        temp = Double.doubleToLongBits(dbTarget);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime
                * result
                + ((playlistDirectory == null) ? 0 : playlistDirectory
                        .hashCode());
        result = prime * result + Arrays.hashCode(radios);
        result = prime * result + (silenceAlarm ? 1231 : 1237);
        result = prime * result
                + ((timezone == null) ? 0 : timezone.hashCode());
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
        if (Double.doubleToLongBits(dbTarget) != Double
                .doubleToLongBits(other.dbTarget))
            return false;
        if (playlistDirectory == null) {
            if (other.playlistDirectory != null)
                return false;
        } else if (!playlistDirectory.equals(other.playlistDirectory))
            return false;
        if (!Arrays.equals(radios, other.radios))
            return false;
        if (silenceAlarm != other.silenceAlarm)
            return false;
        if (timezone == null) {
            if (other.timezone != null)
                return false;
        } else if (!timezone.equals(other.timezone))
            return false;
        if (transmitterGroup == null) {
            if (other.transmitterGroup != null)
                return false;
        } else if (!transmitterGroup.equals(other.transmitterGroup))
            return false;
        return true;
    }
}