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
package com.raytheon.uf.common.bmh.dac;

/**
 * Defines common names for {@link Dac} and DAC fields that can be shared
 * between the client and server when verifying that a {@link Dac} and DAC are
 * in sync.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 20, 2015 5113       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public final class DacSyncFields {

    public static final String FIELD_DAC_NAME = "DAC Name";

    public static final String FIELD_DAC_IP_ADDRESS = "DAC IP Address";

    public static final String FIELD_DAC_NET_MASK = "Net Mask";

    public static final String FIELD_DAC_GATEWAY = "Gateway";

    public static final String FIELD_BROADCAST_BUFFER = "Broadcast Buffer";

    public static final String FIELD_DAC_RECEIVE_ADDRESS = "Receive Address";

    public static final String FIELD_DAC_RECEIVE_PORT = "Receive Port";

    public static final String FIELD_DAC_CHANNEL_FMT = "Channel %s";

    public static final String FIELD_DAC_CHANNEL_LVL_FMT = FIELD_DAC_CHANNEL_FMT
            + " Level";

    protected DacSyncFields() {
    }
}