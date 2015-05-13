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
import java.util.Formatter;

import com.raytheon.uf.common.bmh.trace.ITraceable;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Notification message generated when DacTransmit switches its currently
 * playing message.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 25, 2014  #3286     dgilling     Initial creation
 * May 13, 2015  #4429     rferrel      Implement {@link ITraceable}.
 * 
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

@DynamicSerialize
public class MessagePlaybackStatusNotification {

    @DynamicSerializeElement
    private long broadcastId;

    @DynamicSerializeElement
    private Calendar transmitTime;

    @DynamicSerializeElement
    private int playCount;

    @DynamicSerializeElement
    private boolean playedAlertTone;

    @DynamicSerializeElement
    private boolean playedSameTone;

    @DynamicSerializeElement
    private String transmitterGroup;

    @DynamicSerializeElement
    private String traceId;

    public MessagePlaybackStatusNotification() {
        // empty constructor for serialization support
    }

    public MessagePlaybackStatusNotification(long broadcastId,
            Calendar transmitTime, int playCount, boolean playedAlertTone,
            boolean playedSameTone, String transmitterGroup) {
        this.broadcastId = broadcastId;
        this.transmitTime = transmitTime;
        this.playCount = playCount;
        this.playedAlertTone = playedAlertTone;
        this.playedSameTone = playedSameTone;
        this.transmitterGroup = transmitterGroup;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MessagePlaybackStatusNotification [broadcastId=");
        builder.append(broadcastId);
        builder.append(", traceId=").append(traceId);
        builder.append(", transmitTime=");
        try (Formatter formatter = new Formatter(builder)) {
            formatter.format("%1$tY%1$tm%1$td%1$tH%1$tM", transmitTime);
        }
        builder.append(", playCount=");
        builder.append(playCount);
        builder.append(", playedAlertTone=");
        builder.append(playedAlertTone);
        builder.append(", playedSameTone=");
        builder.append(playedSameTone);
        builder.append(", transmitterGroup=");
        builder.append(transmitterGroup);
        builder.append("]");
        return builder.toString();
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public long getBroadcastId() {
        return broadcastId;
    }

    public void setBroadcastId(long broadcastId) {
        this.broadcastId = broadcastId;
    }

    public Calendar getTransmitTime() {
        return transmitTime;
    }

    public void setTransmitTime(Calendar transmitTime) {
        this.transmitTime = transmitTime;
    }

    public int getPlayCount() {
        return playCount;
    }

    public void setPlayCount(int playCount) {
        this.playCount = playCount;
    }

    public boolean isPlayedAlertTone() {
        return playedAlertTone;
    }

    public void setPlayedAlertTone(boolean playedAlertTone) {
        this.playedAlertTone = playedAlertTone;
    }

    public boolean isPlayedSameTone() {
        return playedSameTone;
    }

    public void setPlayedSameTone(boolean playedSameTone) {
        this.playedSameTone = playedSameTone;
    }

    public String getTransmitterGroup() {
        return transmitterGroup;
    }

    public void setTransmitterGroup(String transmitterGroup) {
        this.transmitterGroup = transmitterGroup;
    }
}
