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
package com.raytheon.uf.common.bmh.comms;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Used to report status information regarding the live broadcast to viz from the
 * comms manager.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 10, 2014 3656       bkowal      Initial creation
 * Oct 15, 2014 3655       bkowal      Rename for general status usage.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

@DynamicSerialize
public class LiveBroadcastClientStatus {

    public static enum STATUS {
        READY, FAILED
    }

    @DynamicSerializeElement
    private String broadcastId;

    @DynamicSerializeElement
    private STATUS status;

    @DynamicSerializeElement
    private String detail;

    /**
     * A list of transmitter group(s) that the broadcast is being streamed to.
     */
    @DynamicSerializeElement
    private String[] transmitterGroups;

    /**
     * 
     */
    public LiveBroadcastClientStatus() {
    }

    /**
     * @return the broadcastId
     */
    public String getBroadcastId() {
        return broadcastId;
    }

    /**
     * @param broadcastId
     *            the broadcastId to set
     */
    public void setBroadcastId(String broadcastId) {
        this.broadcastId = broadcastId;
    }

    /**
     * @return the status
     */
    public STATUS getStatus() {
        return status;
    }

    /**
     * @param status
     *            the status to set
     */
    public void setStatus(STATUS status) {
        this.status = status;
    }

    /**
     * @return the transmitterGroups
     */
    public String[] getTransmitterGroups() {
        return transmitterGroups;
    }

    /**
     * @param transmitterGroups
     *            the transmitterGroups to set
     */
    public void setTransmitterGroups(String[] transmitterGroups) {
        this.transmitterGroups = transmitterGroups;
    }

    /**
     * @return the detail
     */
    public String getDetail() {
        return detail;
    }

    /**
     * @param detail
     *            the detail to set
     */
    public void setDetail(String detail) {
        this.detail = detail;
    }
}