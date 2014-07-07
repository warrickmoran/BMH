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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.hibernate.event.SaveOrUpdateEvent;
import org.hibernate.event.def.DefaultSaveOrUpdateEventListener;
import org.apache.commons.lang.ClassUtils;

/**
 * Generic update listener wrapper. Allows for the registration of a listener
 * associated with a specific Entity class.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 30, 2014 3302       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class ProxyBMHSaveOrUpdateListener extends
        DefaultSaveOrUpdateEventListener {

    private static final long serialVersionUID = -7444708217603045046L;

    private static final ProxyBMHSaveOrUpdateListener instance = new ProxyBMHSaveOrUpdateListener();

    private static Map<Class<?>, IBMHSaveOrUpdateListener> listenerMapping = Collections
            .synchronizedMap(new HashMap<Class<?>, IBMHSaveOrUpdateListener>());

    public static ProxyBMHSaveOrUpdateListener getInstance() {
        return instance;
    }

    public ProxyBMHSaveOrUpdateListener registerListener(
            IBMHSaveOrUpdateListener listener) {
        listenerMapping.put(listener.getEntityClass(), listener);
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onSaveOrUpdate(SaveOrUpdateEvent event) {
        if (event.getObject() != null) {
            List<Class<?>> classesToCompare = ClassUtils
                    .getAllSuperclasses(event.getObject().getClass());
            /* Start with the original class. */
            classesToCompare.add(0, event.getObject().getClass());

            for (Class<?> clazz : classesToCompare) {
                IBMHSaveOrUpdateListener listener = listenerMapping.get(clazz);
                if (listener != null) {
                    listener.onSaveOrUpdate(event);
                    break;
                }
            }
        }

        super.onSaveOrUpdate(event);
    }
}