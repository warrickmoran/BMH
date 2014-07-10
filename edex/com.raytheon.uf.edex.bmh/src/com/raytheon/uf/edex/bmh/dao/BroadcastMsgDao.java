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

import java.util.List;

import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg;

/**
 * BMH DAO for {@link BroadcastMsg}.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Jun 26, 2014  3302     bkowal      Initial creation
 * Jul 10, 2014  3285     bsteffen    Add getMessagesByAfosid()
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class BroadcastMsgDao extends AbstractBMHDao<BroadcastMsg, Long> {

    public BroadcastMsgDao() {
        super(BroadcastMsg.class);
    }

    public List<BroadcastMsg> getMessagesByAfosid(final String afosid){
        List<?> messages = txTemplate
                .execute(new TransactionCallback<List<?>>() {
                    @Override
                    public List<?> doInTransaction(TransactionStatus status) {
                        HibernateTemplate ht = getHibernateTemplate();
                        return ht.findByNamedQueryAndNamedParam(
                                BroadcastMsg.GET_MSGS_BY_AFOS_ID,
                                new String[] {"afosid" },
                                new String[] { afosid });
                    }
                });
        @SuppressWarnings("unchecked")
        List<BroadcastMsg> result = (List<BroadcastMsg>) messages;
        return result;
    }
}