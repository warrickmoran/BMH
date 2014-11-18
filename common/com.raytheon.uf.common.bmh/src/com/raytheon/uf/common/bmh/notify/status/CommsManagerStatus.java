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
package com.raytheon.uf.common.bmh.notify.status;

import java.util.HashSet;
import java.util.Set;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * 
 * Status Message sent periodically by every running comms manager.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Nov 18, 2014  3817     bsteffen    Initial creation
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
@DynamicSerialize
public class CommsManagerStatus extends PeriodicStatusMessage {

    @DynamicSerializeElement
    private Set<String> connectedTransmitterGroups;

    public CommsManagerStatus() {
        super();
    }

    public CommsManagerStatus(String host,
            Set<String> connectedTransmitterGroups) {
        super(host);
        this.connectedTransmitterGroups = connectedTransmitterGroups;
    }

    public Set<String> getConnectedTransmitterGroups() {
        return connectedTransmitterGroups;
    }

    public void setConnectedTransmitterGroups(
            Set<String> connectedTransmitterGroups) {
        this.connectedTransmitterGroups = connectedTransmitterGroups;
    }

    public boolean addConnectedTransmitterGroup(String connectedTransmitterGroup) {
        if (connectedTransmitterGroups == null) {
            connectedTransmitterGroups = new HashSet<>();
        }
        return connectedTransmitterGroups.add(connectedTransmitterGroup);
    }

    public boolean containsConnectedTransmitterGroup(
            String connectedTransmitterGroup) {
        if (connectedTransmitterGroups == null) {
            return false;
        }
        return connectedTransmitterGroups.contains(connectedTransmitterGroup);
    }

}
