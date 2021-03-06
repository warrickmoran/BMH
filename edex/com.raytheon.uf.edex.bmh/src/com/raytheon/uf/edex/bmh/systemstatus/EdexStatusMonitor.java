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
package com.raytheon.uf.edex.bmh.systemstatus;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.raytheon.uf.common.bmh.notify.status.BmhEdexStatus;
import com.raytheon.uf.common.bmh.systemstatus.SystemStatusMonitor;

/**
 * 
 * Object to hold and monitor the status of various BMH components.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Nov 18, 2014  3817     bsteffen    Initial creation
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class EdexStatusMonitor extends SystemStatusMonitor {

    public BmhEdexStatus getCurrentStatus() throws UnknownHostException {
        return new BmhEdexStatus(InetAddress.getLocalHost().getHostName());
    }
    
}
