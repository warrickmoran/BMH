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
package com.raytheon.uf.edex.bmh.dactransmit.dacsession;

import com.raytheon.uf.common.time.util.TimeUtil;

/**
 * Useful constants for the DacSession and its components.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 01, 2014  #3286     dgilling     Initial creation
 * Jul 22, 2014  #3283     dgilling     Add DAC heartbeat constants.
 * Aug 08, 2014  #3286     dgilling     Add/update constants to support sync
 *                                      lost and regain events.
 * Sep 04, 2014  #3584     dgilling     Add SYNC_RETRY_PERIOD.
 * 
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

public final class DacSessionConstants {

    /**
     * The size (in bytes) of one of the payload fields that is part of the
     * RTP-like packets sent to the DAC.
     */
    public static final int SINGLE_PAYLOAD_SIZE = 160;

    /**
     * Size (in bytes) of the combined collection of payloads in a single RTP
     * packet for the DAC.
     */
    public static final int COMBINED_PAYLOAD_SIZE = SINGLE_PAYLOAD_SIZE * 2;

    /**
     * Total size (in bytes) of the specialized RTP packets sent to the DAC.
     * Value is 340 bytes (12 bytes for the RTP header, 8 bytes for the
     * extension header, and 320 bytes for the 2 160 byte payloads).
     */
    public static final int RTP_PACKET_SIZE = 340;

    public static final byte SILENCE = (byte) 0xFF;

    public static final int DAC_HEARTBEAT_CYCLE_TIME = 100; // in ms

    public static final int INITIAL_SYNC_TIMEOUT_PERIOD = DAC_HEARTBEAT_CYCLE_TIME * 2;

    public static final int DEFAULT_SYNC_TIMEOUT_PERIOD = 150;

    public static final int MISSED_HEARTBEATS_THRESHOLD = 3;

    public static final long COMPLETE_SYNC_LOST_TIME = 5 * TimeUtil.MILLIS_PER_SECOND;

    public static final long SYNC_RETRY_PERIOD = 1 * TimeUtil.MILLIS_PER_SECOND;

    private DacSessionConstants() {
        throw new AssertionError(
                "Cannot directly instantiate instances of this class.");
    }
}
