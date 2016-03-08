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
package com.raytheon.uf.common.bmh.broadcast;

import java.util.Collection;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * 
 * Message sent from the comms manager in response to a
 * {@link LiveBroadcastListGroupsCommand}. Contains all the transmitter groups
 * that can sent a live broadcast.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date          Ticket#   Engineer    Description
 * ------------- --------- ----------- --------------------------
 * Feb 05, 2015  3743      bsteffen    Initial creation
 * Nov 11, 2015  5114      rjpeter     Updated to return mode of comms manager.
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
@DynamicSerialize
public class LiveBroadcastGroupsMessage {
    @DynamicSerializeElement
    private boolean operational;

    @DynamicSerializeElement
    private String[] groups;

    public LiveBroadcastGroupsMessage(boolean operational,
            Collection<String> groups) {
        this.operational = operational;
        this.groups = groups.toArray(new String[0]);
    }

    public LiveBroadcastGroupsMessage(boolean operational, String[] groups) {
        this.operational = operational;
        this.groups = groups;
    }

    public LiveBroadcastGroupsMessage() {

    }

    /**
     * @return the operational
     */
    public boolean isOperational() {
        return operational;
    }

    /**
     * @param operational
     *            the operational to set
     */
    public void setOperational(boolean operational) {
        this.operational = operational;
    }

    public String[] getGroups() {
        return groups;
    }

    public void setGroups(String[] groups) {
        this.groups = groups;
    }

}
