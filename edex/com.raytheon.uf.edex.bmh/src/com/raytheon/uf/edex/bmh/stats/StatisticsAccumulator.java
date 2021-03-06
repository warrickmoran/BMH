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
package com.raytheon.uf.edex.bmh.stats;

import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg;
import com.raytheon.uf.common.bmh.stats.DeliveryTimeEvent;
import com.raytheon.uf.common.event.EventBus;
import com.raytheon.uf.common.stats.StatisticsEvent;
import com.raytheon.uf.edex.bmh.dao.BroadcastMsgDao;

/**
 * Collects statistics generated by various parts of the system and routes them
 * to the storage destination. Only collects operational statistics.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 15, 2015 4397       bkowal      Initial creation
 * Jul 29, 2015 4686       bkowal      Set the delivered flag on {@link BroadcastMsg}s in
 *                                     response to {@link DeliveryTimeEvent}s.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class StatisticsAccumulator {

    private final BroadcastMsgDao broadcastMsgDao;

    public StatisticsAccumulator(BroadcastMsgDao broadcastMsgDao) {
        this.broadcastMsgDao = broadcastMsgDao;
    }

    public void accumulateStatEvent(StatisticsEvent event) {
        EventBus.publish(event);

        if (event instanceof DeliveryTimeEvent) {
            this.updateMessageDeliveryFlag((DeliveryTimeEvent) event);
        }
    }

    private void updateMessageDeliveryFlag(DeliveryTimeEvent event) {
        BroadcastMsg msg = this.broadcastMsgDao.getByID(event.getBroadcastId());
        if (msg != null && msg.isDelivered() == false) {
            msg.setDelivered(true);
            this.broadcastMsgDao.persist(msg);
        }
    }
}