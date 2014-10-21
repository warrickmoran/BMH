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
package com.raytheon.uf.edex.bmh.dao;


import org.hibernate.event.spi.SaveOrUpdateEvent;

import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg;
import com.raytheon.uf.common.time.util.TimeUtil;

/**
 * Listens for updates to {@link BroadcastMsg} entities.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 1, 2014  3302       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class BroadcastMsgListener implements IBMHSaveOrUpdateListener {

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.edex.bmh.dao.IBMHSaveOrUpdateListener#onSaveOrUpdate(
     * org.hibernate.event.SaveOrUpdateEvent)
     */
    @Override
    public void onSaveOrUpdate(SaveOrUpdateEvent event) {
        BroadcastMsg msg = (BroadcastMsg) event.getObject();
        if (msg.getCreationDate() == null) {
            msg.setCreationDate(TimeUtil.newGmtCalendar());
            /*
             * Ensure that the creation date and update date are exactly the
             * same.
             */
            msg.setUpdateDate(msg.getCreationDate());
        } else {
            msg.setUpdateDate(TimeUtil.newGmtCalendar());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.edex.bmh.dao.IBMHSaveOrUpdateListener#getEntityClass()
     */
    // Candidate for abstraction
    @Override
    public Class<?> getEntityClass() {
        return BroadcastMsg.class;
    }
}