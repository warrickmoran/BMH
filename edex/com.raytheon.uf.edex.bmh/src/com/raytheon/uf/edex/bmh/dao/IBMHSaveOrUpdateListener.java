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

import org.hibernate.event.SaveOrUpdateEvent;

/**
 * Defines the BMH Database Listener.
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

public interface IBMHSaveOrUpdateListener {
    /**
     * Initiates a custom action just before a specified database entity is
     * saved to the database.
     * 
     * @param event
     *            details about the event
     */
    public void onSaveOrUpdate(SaveOrUpdateEvent event);

    /**
     * Defines the class the implementing update listener is interested in.
     * 
     * @return class the implementing update listener is interested in.
     */
    public Class<?> getEntityClass();
}
