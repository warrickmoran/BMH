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
package com.raytheon.uf.common.bmh;

import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType.Designation;

/**
 * Common utility to identify static message types.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 5, 2015  4085       bkowal      Initial creation
 * Mar 13, 2015 4213       bkowal      Message types with a station id {@link Designation}
 *                                     will also be considered static.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class StaticMessageIdentifier {

    public static final Designation stationIdDesignation = Designation.StationID;

    public static final Designation timeDesignation = Designation.TimeAnnouncement;

    /**
     * Protected constructor to ensure that it will not be possible to
     * instantiate this class.
     */
    protected StaticMessageIdentifier() {
    }

    public static boolean isStaticMsgType(MessageType messageType) {
        if (messageType == null) {
            throw new IllegalArgumentException(
                    "Required argument messageType cannot be NULL.");
        }
        return messageType.getDesignation() == stationIdDesignation
                || messageType.getDesignation() == timeDesignation;
    }
}