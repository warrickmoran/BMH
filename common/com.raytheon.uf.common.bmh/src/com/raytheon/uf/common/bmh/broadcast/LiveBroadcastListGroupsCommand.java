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
package com.raytheon.uf.common.bmh.broadcast;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;

/**
 * 
 * Command which can be sent to the comms manager broadcast port to get a list
 * of the groups that can be sent a broadcast. The comms manager will respond
 * with a {@link LiveBroadcastGroupsMessage} and then the socket will be closed.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#   Engineer    Description
 * ------------- --------- ----------- --------------------------
 * Feb 05, 2015  3743      bsteffen    Initial creation
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
@DynamicSerialize
public class LiveBroadcastListGroupsCommand {
    /* No code is needed */
}
