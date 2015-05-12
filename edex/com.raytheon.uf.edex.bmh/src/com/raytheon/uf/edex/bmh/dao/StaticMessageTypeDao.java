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

import com.raytheon.uf.common.bmh.datamodel.transmitter.StaticMessageType;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;

/**
 * DAO for {@link StaticMessageType} objects.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 11, 2015 4213       bkowal      Initial creation
 * May 12, 2015 4248       rjpeter     Updated primary key field.
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class StaticMessageTypeDao extends
        AbstractBMHDao<StaticMessageType, Integer> {

    /**
     * Constructor
     */
    public StaticMessageTypeDao() {
        super(StaticMessageType.class);
    }

    /**
     * Constructor
     * 
     * @param operational
     */
    public StaticMessageTypeDao(boolean operational) {
        super(operational, StaticMessageType.class);
    }

    public StaticMessageType getStaticForMsgTypeAndTransmittergroup(
            String afosId, TransmitterGroup transmitterGroup) {
        List<?> returnObjects = super.findByNamedQueryAndNamedParam(
                StaticMessageType.GET_STATIC_MSG_BY_MSG_TYPE_AND_GROUP,
                new String[] { "transmitterGroup", "afosid" }, new Object[] {
                        transmitterGroup, afosId });

        if (returnObjects == null || returnObjects.isEmpty()
                || returnObjects.get(0) instanceof StaticMessageType == false) {
            return null;
        }

        return (StaticMessageType) returnObjects.get(0);
    }
}