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

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.raytheon.bmh.comms.broadcast.ManagedTransmitterGroup.RESPONSIBILITY;
import com.raytheon.bmh.comms.broadcast.ManagedTransmitterGroup.STREAMING_STATUS;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;

/**
 * Used to keep track of {@link TransmitterGroup}s that a
 * {@link BroadcastStreamTask} will be streaming to. Tracks both local and
 * remote dacs and the process that is responsible for streaming information to
 * them.
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

public class TransmitterGroupManager {

    private final ConcurrentMap<TransmitterGroup, ManagedTransmitterGroup> managedTransmitterGroups;

    /**
     * 
     */
    public TransmitterGroupManager(
            final Collection<TransmitterGroup> desiredTransmitterGroups) {
        this.managedTransmitterGroups = new ConcurrentHashMap<>(
                desiredTransmitterGroups.size(), 1.0f);
        for (TransmitterGroup tg : desiredTransmitterGroups) {
            this.managedTransmitterGroups.put(tg, new ManagedTransmitterGroup(
                    tg));
        }
    }

    public List<TransmitterGroup> getManagedTransmitters() {
        return this.getTransmittersWithReponsibility(RESPONSIBILITY.ME);
    }

    /**
     * Determines if the responsible process has been determined for every one
     * of the desired {@link TransmitterGroup}s.
     * 
     * @return true if all responsible process(es) are known; false, otherwise
     */
    public boolean allTransmittersAssigned() {
        return this.getTransmittersWithReponsibility(RESPONSIBILITY.UNKNOWN)
                .isEmpty();
    }

    public List<TransmitterGroup> getUnassignedTransmitters() {
        return this.getTransmittersWithReponsibility(RESPONSIBILITY.UNKNOWN);
    }

    private List<TransmitterGroup> getTransmittersWithReponsibility(
            RESPONSIBILITY responsibility) {
        List<TransmitterGroup> tgList = new ArrayList<>(
                this.managedTransmitterGroups.size());
        for (ManagedTransmitterGroup mtg : this.managedTransmitterGroups
                .values()) {
            if (mtg.getResponsibility() == responsibility) {
                tgList.add(mtg.getTransmitterGroup());
            }
        }
        return tgList;
    }

    public void claimResponsibility(TransmitterGroup tg) {
        this.updateResponsibility(tg, RESPONSIBILITY.ME);
    }

    public void forfeitResponsibility(TransmitterGroup tg) {
        this.updateResponsibility(tg, RESPONSIBILITY.UNKNOWN);
    }

    public void giveResponsibility(TransmitterGroup tg) {
        this.updateResponsibility(tg, RESPONSIBILITY.MEMBER);
    }

    public List<TransmitterGroup> getTransmittersWithStreamStatus(
            STREAMING_STATUS status) {
        List<TransmitterGroup> tgList = new ArrayList<>(
                this.managedTransmitterGroups.size());
        for (ManagedTransmitterGroup mtg : this.managedTransmitterGroups
                .values()) {
            if (mtg.getStatus() == status) {
                tgList.add(mtg.getTransmitterGroup());
            }
        }
        return tgList;
    }

    public boolean doAllTransmittersHaveStreamStatus(STREAMING_STATUS status) {
        return this.getTransmittersWithStreamStatus(status).size() == this.managedTransmitterGroups
                .size();
    }

    public boolean doAnyTransmittersHaveStreamStatus(STREAMING_STATUS status) {
        return this.getTransmittersWithStreamStatus(status).isEmpty() == false;
    }

    public boolean doManagedTransmittersHaveStreamStatus(STREAMING_STATUS status) {
        for (TransmitterGroup tg : this.getManagedTransmitters()) {
            if (this.managedTransmitterGroups.get(tg).getStatus() == status) {
                return true;
            }
        }

        return false;
    }

    public void updateStreamStatus(TransmitterGroup tg, STREAMING_STATUS status) {
        if (this.managedTransmitterGroups.containsKey(tg) == false) {
            return;
        }
        this.managedTransmitterGroups.get(tg).setStatus(status);
    }

    private void updateResponsibility(TransmitterGroup tg,
            RESPONSIBILITY responsibility) {
        if (this.managedTransmitterGroups.containsKey(tg) == false) {
            return;
        }
        this.managedTransmitterGroups.get(tg).setResponsibility(responsibility);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("TransmitterGroupManager [");
        sb.append("managedTransmitterGroups={");
        boolean first = true;
        for (ManagedTransmitterGroup mtg : this.managedTransmitterGroups
                .values()) {
            if (first) {
                first = false;
            } else {
                sb.append(", ");
            }
            sb.append(mtg.toString());
        }
        sb.append("}]");

        return sb.toString();
    }
}