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
package com.raytheon.uf.common.bmh.notify.config;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * 
 * Notification that is used when a {@link TransmitterGroup} or
 * {@link Transmitter} in a group is created, updated or deleted.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 04, 2014  3486     bsteffen    Initial creation
 * Sep 08, 2014  3568     bkowal      Updated the getIds method for
 *                                    dynamicserialize.
 * Jan 22, 2015  4017     bkowal      Replaced the numerical ids with
 *                                    {@link TransmitterGroupIdentifier}s.
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
@DynamicSerialize
public class TransmitterGroupConfigNotification extends ConfigNotification {

    @DynamicSerializeElement
    private List<TransmitterGroupIdentifier> identifiers;

    public TransmitterGroupConfigNotification() {
        super();
        this.identifiers = Collections.emptyList();
    }

    public TransmitterGroupConfigNotification(ConfigChangeType type,
            TransmitterGroup tg) {
        super(type);
        this.identifiers = new ArrayList<>(1);
        this.identifiers.add(new TransmitterGroupIdentifier(tg.getId(), tg
                .getName()));
    }

    public TransmitterGroupConfigNotification(ConfigChangeType type,
            List<TransmitterGroup> groups) {
        super(type);
        this.identifiers = new ArrayList<>(groups.size());
        for (TransmitterGroup tg : groups) {
            this.identifiers.add(new TransmitterGroupIdentifier(tg.getId(), tg
                    .getName()));
        }
    }

    /**
     * @return the identifiers
     */
    public List<TransmitterGroupIdentifier> getIdentifiers() {
        return identifiers;
    }

    /**
     * @param identifiers
     *            the identifiers to set
     */
    public void setIdentifiers(List<TransmitterGroupIdentifier> identifiers) {
        this.identifiers = identifiers;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(
                "TransmitterGroupConfigNotification [identifiers={");
        boolean first = true;
        for (TransmitterGroupIdentifier identifier : this.identifiers) {
            if (first) {
                first = false;
            } else {
                sb.append(", ");
            }
            sb.append(identifier.toString());
        }
        sb.append("}]");

        return sb.toString();
    }
}