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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType.Designation;

/**
 * BMH DAO for {@link MessageType}.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 24, 2014 3302       bkowal      Initial creation
 * Aug 5, 2014  #3490      lvenable    added getMessageTypes()
 * Sep 2, 2014  3568       bkowal      Added getMessageTypeForDesignation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class MessageTypeDao extends AbstractBMHDao<MessageType, Integer> {

    public MessageTypeDao() {
        super(MessageType.class);
    }

    /**
     * Get a list of fully populate message types.
     * 
     * @return List of message types.
     */
    public List<MessageType> getMessgeTypes() {
        List<Object> allObjects = this.loadAll();
        if (allObjects == null) {
            return Collections.emptyList();
        }

        List<MessageType> messageTypeList = new ArrayList<MessageType>(
                allObjects.size());
        for (Object obj : allObjects) {
            MessageType mt = (MessageType) obj;
            messageTypeList.add(mt);
        }

        return messageTypeList;
    }

    /**
     * Looks up the MessageType for the given afosId.
     * 
     * @param afosId
     * @return
     */
    public MessageType getByAfosId(final String afosId) {
        List<MessageType> types = txTemplate
                .execute(new TransactionCallback<List<MessageType>>() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public List<MessageType> doInTransaction(
                            TransactionStatus status) {
                        HibernateTemplate ht = getHibernateTemplate();
                        return ht.findByNamedQueryAndNamedParam(
                                MessageType.GET_MESSAGETYPE_FOR_AFOSID,
                                new String[] { "afosid" },
                                new Object[] { afosId });
                    }
                });
        if ((types != null) && !types.isEmpty()) {
            return types.get(0);
        }

        return null;
    }

    public List<MessageType> getMessageTypeForDesignation(
            final Designation designation) {
        List<?> types = txTemplate.execute(new TransactionCallback<List<?>>() {
            @Override
            public List<?> doInTransaction(TransactionStatus status) {
                HibernateTemplate ht = getHibernateTemplate();
                return ht.findByNamedQueryAndNamedParam(
                        MessageType.GET_MESSAGETYPE_FOR_DESIGNATION,
                        new String[] { "designation" },
                        new Object[] { designation });
            }
        });

        if (types == null || types.isEmpty()) {
            return null;
        }

        List<MessageType> messageTypes = new ArrayList<>(types.size());
        for (Object type : types) {
            if (type instanceof MessageType) {
                messageTypes.add((MessageType) type);
            }
        }

        return messageTypes;
    }
}