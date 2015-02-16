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
package com.raytheon.uf.edex.bmh.dactransmit.ipc;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * 
 * A message, sent to a dac transmit process telling it to scan, or not to scan
 * the playlist directory. When the comms manager is listening for changes then
 * there is no need to scan the directory.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Feb 16, 2015  4107     bsteffen    Initial Creation
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
@DynamicSerialize
public class DacTransmitScanPlaylists {

    @DynamicSerializeElement
    private boolean scan;

    public DacTransmitScanPlaylists() {
    }

    public DacTransmitScanPlaylists(boolean scan) {
        this.scan = scan;
    }

    public boolean isScan() {
        return scan;
    }

    public void setScan(boolean scan) {
        this.scan = scan;
    }

}
