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
 * Constants used for data transmission to the DAC.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 08, 2014  #3286     dgilling     Initial creation
 * Jul 14, 2014  #3286     dgilling     Tweaked cycle time constants.
 * Jul 16, 2014  #3286     dgilling     Remove unneeded constants.
 * Jul 31, 2014  #3286     dgilling     Add alert constants for buffer size.
 * Aug 08, 2014  #3286     dgilling     Add constants for sync regain.
 * 
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

public final class DataTransmitConstants {

    public static final long DEFAULT_CYCLE_TIME = 20; // in MS

    public static final long INITIAL_CYCLE_TIME = 5; // in MS

    /*
     * TODO Make these threshold values configurable and use these constants for
     * defaults.
     */
    public static final int WATERMARK_PACKETS_IN_BUFFER = 20;

    public static final int ALERT_HIGH_PACKETS_IN_BUFFER = 2 * WATERMARK_PACKETS_IN_BUFFER;

    public static final int ALERT_LOW_PACKETS_IN_BUFFER = WATERMARK_PACKETS_IN_BUFFER / 4;

    public static final long SYNC_DOWNTIME_RESTART_THRESHOLD = 2 * TimeUtil.MILLIS_PER_SECOND;

    public static final int SEQUENCE_INCREMENT = 1;

    public static final int TIMESTAMP_INCREMENT = 160;

    private DataTransmitConstants() {
        throw new AssertionError(
                "Cannot directly instantiate instances of this class.");
    }
}
