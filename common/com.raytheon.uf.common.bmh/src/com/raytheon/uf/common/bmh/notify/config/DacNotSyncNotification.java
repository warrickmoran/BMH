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

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * {@link ConfigNotification} that is used to notify others whenever the group
 * of {@link Dac}s that are not in sync changes.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 23, 2015 5113       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */
@DynamicSerialize
public class DacNotSyncNotification extends ConfigNotification {

    @DynamicSerializeElement
    private List<Integer> desyncedDacsList;

    public DacNotSyncNotification() {
    }

    public DacNotSyncNotification(List<Integer> desyncedDacsList) {
        this.desyncedDacsList = desyncedDacsList;
    }

    /**
     * @return the desyncedDacsList
     */
    public List<Integer> getDesyncedDacsList() {
        return desyncedDacsList;
    }

    /**
     * @param desyncedDacsList
     *            the desyncedDacsList to set
     */
    public void setDesyncedDacsList(List<Integer> desyncedDacsList) {
        this.desyncedDacsList = desyncedDacsList;
    }
}