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
package com.raytheon.bmh.comms.broadcast;

import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;

/**
 * Used to track the state and status of a {@link TransmitterGroup} needed for a
 * live stream.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 25, 2014 3797       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class ManagedTransmitterGroup {

    public static enum RESPONSIBILITY {
        /**
         * The process responsible for managing interactions with the
         * {@link TransmitterGroup} is not known at this time or it cannot be
         * found at all.
         */
        UNKNOWN,
        /**
         * The current process is responsible for managing interactions with the
         * {@link TransmitterGroup}.
         */
        ME,
        /**
         * A different cluster process that is a recognized member of the
         * cluster is responsible for managing interactions with the
         * {@link TransmitterGroup}.
         */
        MEMBER
    }

    public static enum STREAMING_STATUS {
        /**
         * The availability of the {@link TransmitterGroup} to participate in a
         * streaming broadcast is not known.
         */
        UNKNOWN,
        /**
         * The {@link TransmitterGroup} is available to participate in a
         * streaming broadcast.
         */
        AVAILABLE,
        /**
         * The {@link TransmitterGroup} is not available to participate in a
         * streaming broadcast. This status generally indicates that the
         * {@link TransmitterGroup} had started streaming an interrupt message.
         */
        BUSY,
        /**
         * Indicates that the {@link TransmitterGroup} is ready to start
         * receiving audio to stream.
         */
        READY
    }

    private final TransmitterGroup transmitterGroup;

    private RESPONSIBILITY responsibility;

    private STREAMING_STATUS status;

    /**
     * 
     */
    public ManagedTransmitterGroup(final TransmitterGroup transmitterGroup) {
        this.transmitterGroup = transmitterGroup;
        this.responsibility = RESPONSIBILITY.UNKNOWN;
        this.status = STREAMING_STATUS.UNKNOWN;
    }

    public RESPONSIBILITY getResponsibility() {
        return responsibility;
    }

    public void setResponsibility(RESPONSIBILITY responsibility) {
        this.responsibility = responsibility;
    }

    public STREAMING_STATUS getStatus() {
        return status;
    }

    public void setStatus(STREAMING_STATUS status) {
        this.status = status;
    }

    public TransmitterGroup getTransmitterGroup() {
        return transmitterGroup;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ManagedTransmitterGroup [");
        sb.append("transmitterGroup=");
        sb.append(this.transmitterGroup.getName());
        sb.append(", responsibility=");
        sb.append(this.responsibility.toString());
        sb.append(", status=");
        sb.append(this.status.toString());
        sb.append("]");

        return sb.toString();
    }
}