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
package com.raytheon.bmh.comms.cluster;

import java.util.ArrayList;
import java.util.List;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Message used to send a list of connected dacs between comms manager cluster
 * members.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Sep 24, 2014  3485     bsteffen    Initial creation
 * Nov 11, 2014  3762     bsteffen    Add load balancing of dac transmits.
 * Aug 12, 2015  4424     bkowal      Eliminate Dac Transmit Key.
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
@DynamicSerialize
public class ClusterStateMessage {

    @DynamicSerializeElement
    private final List<String> connectedTransmitters = new ArrayList<>();

    @DynamicSerializeElement
    private String requestedTransmitter = null;

    public void add(String transmitterGroup) {
        this.connectedTransmitters.add(transmitterGroup);
    }

    public boolean remove(String transmitterGroup) {
        return connectedTransmitters.remove(transmitterGroup);
    }

    public boolean contains(String transmitterGroup) {
        return this.connectedTransmitters.contains(transmitterGroup);
    }

    public boolean isRequestedTransmitter(String transmitterGroup) {
        return this.requestedTransmitter != null
                && this.requestedTransmitter.equals(transmitterGroup);
    }

    public boolean hasRequestedTransmitter() {
        return this.requestedTransmitter != null;
    }

    /**
     * @return the requestedTransmitter
     */
    public String getRequestedTransmitter() {
        return requestedTransmitter;
    }

    /**
     * @param requestedTransmitter
     *            the requestedTransmitter to set
     */
    public void setRequestedTransmitter(String requestedTransmitter) {
        this.requestedTransmitter = requestedTransmitter;
    }

    /**
     * @return the connectedTransmitters
     */
    public List<String> getConnectedTransmitters() {
        return connectedTransmitters;
    }
}