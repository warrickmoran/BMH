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

import java.util.Calendar;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * 
 * Xml representation of a playlist message that is sent from the playlist
 * manager to the comms manager.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Jul 01, 2014  3285     bsteffen    Initial creation
 * Jul 14, 2014  3286     dgilling    Implement hashCode()/equals().
 * Aug 18, 2014  3540     dgilling    Implement toString().
 * Jan 05, 2015  3913     bsteffen    Handle future replacements.
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
@XmlAccessorType(XmlAccessType.NONE)
public class DacPlaylistMessageId {

    @XmlAttribute
    protected long broadcastId;

    /**
     * The replace time is persisted as part of the message id in the playlist,
     * but not as part of the message itself. This is because a message can be
     * part of multiple playlists and each might have a different replace time.
     */
    @XmlAttribute
    protected Calendar replaceTime;

    public DacPlaylistMessageId() {

    }

    public DacPlaylistMessageId(long broadcastId) {
        this.broadcastId = broadcastId;
    }

    public long getBroadcastId() {
        return broadcastId;
    }

    public void setBroadcastId(long broadcastId) {
        this.broadcastId = broadcastId;
    }

    public Calendar getReplaceTime() {
        return replaceTime;
    }

    public void setReplaceTime(Calendar replaceTime) {
        this.replaceTime = replaceTime;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DacPlaylistMessageId [broadcastId=");
        builder.append(broadcastId);
        if(replaceTime != null){
            builder.append(",replaceTime=");
            builder.append(replaceTime);
        }
        builder.append("]");
        return builder.toString();
    }

    @Override
    public int hashCode() {
        return new Long(broadcastId).hashCode();
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
        DacPlaylistMessageId other = (DacPlaylistMessageId) obj;
        if (broadcastId != other.broadcastId) {
            return false;
        }
        return true;
    }

}
