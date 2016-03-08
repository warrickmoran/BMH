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
 * Used to keep track of the start and end dates of active triggers.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 21, 2016 5278       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

@XmlAccessorType(XmlAccessType.NONE)
public class DacTriggerSpan implements Comparable<DacTriggerSpan> {

    @XmlAttribute
    private Long broadcastId;

    @XmlAttribute
    private Calendar start;

    @XmlAttribute
    private Calendar end;

    public DacTriggerSpan() {
    }

    public DacTriggerSpan(Long broadcastId, Calendar start, Calendar end) {
        this.broadcastId = broadcastId;
        this.start = start;
        this.end = end;
    }

    public Long getBroadcastId() {
        return broadcastId;
    }

    public void setBroadcastId(Long broadcastId) {
        this.broadcastId = broadcastId;
    }

    public Calendar getStart() {
        return start;
    }

    public void setStart(Calendar start) {
        this.start = start;
    }

    public Calendar getEnd() {
        return end;
    }

    public void setEnd(Calendar end) {
        this.end = end;
    }

    public boolean withinSpan(final long timeT) {
        return timeT >= this.start.getTimeInMillis()
                && (this.end == null || timeT <= this.end.getTimeInMillis());
    }

    @Override
    public int compareTo(DacTriggerSpan o) {
        return this.start.compareTo(o.start);
    }
}