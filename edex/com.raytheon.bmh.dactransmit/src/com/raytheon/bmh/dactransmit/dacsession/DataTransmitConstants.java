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
package com.raytheon.bmh.dactransmit.dacsession;

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
 * Aug 25, 2014  #3286     dgilling     Adjust buffer size alert constants.
 * Oct 01, 2014  #3485     bsteffen     Add STARTUP_RESUME_THRESHOLD
 * Jan 09, 2015  #3942     rjpeter      Increased watermark to 25.
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

public final class DataTransmitConstants {

    // interval to maintain current queue depth
    public static final long DEFAULT_CYCLE_TIME = 20; // in MS

    // interval to reduce queue depth by 1 message per 100ms
    public static final long SLOW_CYCLE_TIME = 25; // in MS

    // max send interval to avoid dropped packets
    public static final long FAST_CYCLE_TIME = 10; // in MS

    public static final long INITIAL_CYCLE_TIME = 10; // in MS

    /*
     * TODO Make these threshold values configurable and use these constants for
     * defaults.
     */
    public static final int WATERMARK_PACKETS_IN_BUFFER = 25;

    public static final int ALERT_HIGH_PACKETS_IN_BUFFER = WATERMARK_PACKETS_IN_BUFFER
            + (WATERMARK_PACKETS_IN_BUFFER / 5);

    public static final int ALERT_LOW_PACKETS_IN_BUFFER = WATERMARK_PACKETS_IN_BUFFER
            - (WATERMARK_PACKETS_IN_BUFFER / 5);

    public static final long SYNC_DOWNTIME_RESTART_THRESHOLD = 2 * TimeUtil.MILLIS_PER_SECOND;

    /**
     * When a dac transmit is restarted it can attempt to resume where it left
     * off, this is the amount of time that resume is valid. If more than this
     * amount of time has elapsed the message will replay from the beginning.
     */
    public static final long STARTUP_RESUME_THRESHOLD = 4 * TimeUtil.MILLIS_PER_SECOND;

    public static final int SEQUENCE_INCREMENT = 1;

    public static final int TIMESTAMP_INCREMENT = 160;

    private DataTransmitConstants() {
        throw new AssertionError(
                "Cannot directly instantiate instances of this class.");
    }
}
