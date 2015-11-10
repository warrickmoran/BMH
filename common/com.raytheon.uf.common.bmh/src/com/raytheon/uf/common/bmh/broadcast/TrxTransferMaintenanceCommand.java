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
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Extension of the maintenance command used to provide additional amplitude
 * target level information.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 13, 2015 4636       bkowal      Initial creation
 * Nov 04, 2015 5068       rjpeter     Switch audio units from dB to amplitude.
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */
@DynamicSerialize
public class TrxTransferMaintenanceCommand extends
        TransmitterMaintenanceCommand {

    @DynamicSerializeElement
    private short transferAmplitude24;

    public TrxTransferMaintenanceCommand() {
    }

    /**
     * @return the transferAmplitude24
     */
    public short getTransferAmplitude24() {
        return transferAmplitude24;
    }

    /**
     * @param transferAmplitude24
     *            the transferAmplitude24 to set
     */
    public void setTransferAmplitude24(short transferAmplitude24) {
        this.transferAmplitude24 = transferAmplitude24;
    }
}