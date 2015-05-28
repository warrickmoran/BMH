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
package com.raytheon.uf.common.bmh.notify.config;

import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.trace.ITraceable;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * 
 * Notification that is used when a {@link TransmitterGroup}.timezone changes.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 23, 1015 4423       rferrel     Initial creation
 * May 28, 2015 4429       rjpeter     Update for ITraceable
 * </pre>
 * 
 * @author rferrel
 * @version 1.0
 */
@DynamicSerialize
public class ChangeTimeZoneConfigNotification extends ConfigNotification {

    @DynamicSerializeElement
    private String timeZone;

    @DynamicSerializeElement
    private String transmitterGroup;

    public ChangeTimeZoneConfigNotification() {

    }

    public ChangeTimeZoneConfigNotification(ITraceable traceable) {
        super(ConfigChangeType.Update, traceable);
    }

    public ChangeTimeZoneConfigNotification(String timeZone,
            String transmitterGroup, ITraceable traceable) {
        super(ConfigChangeType.Update, traceable);
        this.timeZone = timeZone;
        this.transmitterGroup = transmitterGroup;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public String getTransmitterGroup() {
        return transmitterGroup;
    }

    public void setTransmitterGroup(String transmitterGroup) {
        this.transmitterGroup = transmitterGroup;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(
                " ChangeTimeZoneConfigNotification[timezone=\"");
        sb.append(timeZone);
        sb.append("\", transmitterGroup=").append(transmitterGroup);
        sb.append("]");

        return sb.toString();
    }
}