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

import java.util.List;

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
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
@DynamicSerialize
public class TransmitterGroupConfigNotification extends ConfigNotification {

    @DynamicSerializeElement
    private int[] ids;

    public TransmitterGroupConfigNotification() {
        super();
    }

    public TransmitterGroupConfigNotification(ConfigChangeType type,
            TransmitterGroup tg) {
        super(type);
        this.ids = new int[] { tg.getId() };
    }

    public TransmitterGroupConfigNotification(ConfigChangeType type,
            List<TransmitterGroup> groups) {
        super(type);
        this.ids = new int[groups.size()];
        for (int i = 0; i < ids.length; i += 1) {
            ids[i] = groups.get(i).getId();
        }
    }

    public int[] getId() {
        return ids;
    }

    public void setId(int[] id) {
        this.ids = id;
    }

}
