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
package com.raytheon.uf.common.bmh.notify;

import java.util.Calendar;

import com.raytheon.uf.common.bmh.datamodel.playlist.DacMaintenanceMessage;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.time.util.TimeUtil;

/**
 * Used to indicate that the broadcast of maintenance message has started.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 28, 2015 4394       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */
@DynamicSerialize
public class MaintenanceMessagePlayback implements INonStandardBroadcast {

    @DynamicSerializeElement
    private Calendar transitTime;

    @DynamicSerializeElement
    private String transmitterGroup;

    @DynamicSerializeElement
    private String name;

    @DynamicSerializeElement
    private long messageDuration;

    @DynamicSerializeElement
    private String sameTone;

    /**
     * Empty constructor for {@link DynamicSerialize}.
     */
    public MaintenanceMessagePlayback() {
    }

    public MaintenanceMessagePlayback(MaintenanceMessagePlayback message) {
        this.transitTime = message.getTransitTime();
        this.transmitterGroup = message.getTransmitterGroup();
        this.name = message.getName();
        this.messageDuration = message.getMessageDuration();
        this.sameTone = message.getSameTone();
    }

    public MaintenanceMessagePlayback(DacMaintenanceMessage message,
            long messageDuration) {
        this.transitTime = TimeUtil.newGmtCalendar();
        this.transmitterGroup = message.getTransmitterGroup();
        this.name = message.getName();
        this.messageDuration = messageDuration;
        this.sameTone = (message.getSAMEtone() == null) ? INonStandardBroadcast.TONE_NONE
                : INonStandardBroadcast.TONE_SENT;
    }

    /**
     * @return the transitTime
     */
    public Calendar getTransitTime() {
        return transitTime;
    }

    /**
     * @param transitTime
     *            the transitTime to set
     */
    public void setTransitTime(Calendar transitTime) {
        this.transitTime = transitTime;
    }

    /**
     * @return the transmitterGroup
     */
    public String getTransmitterGroup() {
        return transmitterGroup;
    }

    /**
     * @param transmitterGroup
     *            the transmitterGroup to set
     */
    public void setTransmitterGroup(String transmitterGroup) {
        this.transmitterGroup = transmitterGroup;
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
     * @return the messageDuration
     */
    public long getMessageDuration() {
        return messageDuration;
    }

    /**
     * @param messageDuration
     *            the messageDuration to set
     */
    public void setMessageDuration(long messageDuration) {
        this.messageDuration = messageDuration;
    }

    /**
     * @return the sameTone
     */
    public String getSameTone() {
        return sameTone;
    }

    /**
     * @param sameTone
     *            the sameTone to set
     */
    public void setSameTone(String sameTone) {
        this.sameTone = sameTone;
    }
}