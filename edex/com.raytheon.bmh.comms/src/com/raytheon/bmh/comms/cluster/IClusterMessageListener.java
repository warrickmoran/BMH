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
package com.raytheon.bmh.comms.cluster;

import java.net.Socket;

/**
 * Allows other components to optionally listen for certain message types as
 * they are received by a {@link ClusterCommunicator}.
 * 
 * May potentially be eliminated based on cluster analysis.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 10, 2014 3656       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public interface IClusterMessageListener {
    /**
     * Used to notify a listener that a message has arrived.
     * 
     * @param socket
     *            the {@link Socket} that the message was received on.
     * @param object
     *            the message that has arrived.
     */
    public void clusterMessageReceived(Socket socket, Object object);
}