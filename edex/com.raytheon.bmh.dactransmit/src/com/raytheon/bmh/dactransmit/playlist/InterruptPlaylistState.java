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
package com.raytheon.bmh.dactransmit.playlist;

/**
 * Basic POJO used to record the state of a broadcast prior to the broadcast of
 * an interrupt.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 2, 2016  5768       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 */

public class InterruptPlaylistState {

    /*
     * The index of the next message in the ordered list of playlist messages
     */
    private final int nextMessageIndex;

    /*
     * The broadcast id of the currently broadcasting message
     */
    private final long currentBroadcastId;

    /*
     * The name of the currently broadcasting suite
     */
    private final String suite;

    public InterruptPlaylistState(final int messageIndex,
            final long currentBroadcastId, final String suite) {
        this.nextMessageIndex = messageIndex;
        this.currentBroadcastId = currentBroadcastId;
        this.suite = suite;
    }

    public int getNextMessageIndex() {
        return nextMessageIndex;
    }

    public long getCurrentBroadcastId() {
        return currentBroadcastId;
    }

    public String getSuite() {
        return suite;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("InterruptPlaylistState [");
        sb.append("nextMessageIndex=").append(nextMessageIndex);
        sb.append(", currentBroadcastId=").append(currentBroadcastId);
        sb.append(", suite=").append(suite);
        sb.append("]");
        return sb.toString();
    }
}