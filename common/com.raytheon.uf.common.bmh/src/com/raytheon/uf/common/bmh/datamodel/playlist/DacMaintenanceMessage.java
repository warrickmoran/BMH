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
package com.raytheon.uf.common.bmh.datamodel.playlist;

import java.nio.file.Path;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.raytheon.uf.common.bmh.tones.TonesManager.TransferType;

/**
 * Metadata associated with a dac maintenance message. Consists of a subset of
 * the information found in {@link DacPlaylistMessage} as well as a few
 * additional fields specific to maintenance messages. The goal is to purge a
 * {@link DacMaintenanceMessage} immediately after it is used. However, the
 * {@link MessagePurger} will attempt to clean up any files that may have been
 * orphaned (indicating that the files are 60 days old).
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 22, 2015 4394       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */
@XmlRootElement(name = "bmhMessage")
@XmlAccessorType(XmlAccessType.NONE)
public class DacMaintenanceMessage {

    @XmlElement
    private String name;

    @XmlElement
    private String soundFile;

    @XmlElement
    private String SAMEtone;

    @XmlElement
    private TransferType transferToneType;

    private transient Path path;

    public boolean isAudio() {
        return this.soundFile != null;
    }

    public boolean isTones() {
        return this.SAMEtone != null || this.transferToneType != null;
    }

    /**
     * 
     */
    public DacMaintenanceMessage() {
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the soundFile
     */
    public String getSoundFile() {
        return soundFile;
    }

    /**
     * @param soundFile
     *            the soundFile to set
     */
    public void setSoundFile(String soundFile) {
        this.soundFile = soundFile;
    }

    /**
     * @return the sAMEtone
     */
    public String getSAMEtone() {
        return SAMEtone;
    }

    /**
     * @param sAMEtone
     *            the sAMEtone to set
     */
    public void setSAMEtone(String sAMEtone) {
        SAMEtone = sAMEtone;
    }

    /**
     * @return the transferToneType
     */
    public TransferType getTransferToneType() {
        return transferToneType;
    }

    /**
     * @param transferToneType
     *            the transferToneType to set
     */
    public void setTransferToneType(TransferType transferToneType) {
        this.transferToneType = transferToneType;
    }

    /**
     * @return the path
     */
    public Path getPath() {
        return path;
    }

    /**
     * @param path
     *            the path to set
     */
    public void setPath(Path path) {
        this.path = path;
    }
}