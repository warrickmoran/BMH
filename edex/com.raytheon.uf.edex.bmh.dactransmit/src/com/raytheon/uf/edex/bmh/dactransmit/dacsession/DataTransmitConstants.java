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
 * 
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

public final class DataTransmitConstants {

    public static final long DEFAULT_CYCLE_TIME = 20; // in MS

    public static final long FAST_CYCLE_TIME = 15; // in MS

    public static final long SLOW_CYCLE_TIME = 21; // in MS

    public static final int WATERMARK_PACKETS_IN_BUFFER = 4;

    public static final int UNKNOWN_BUFFER_SIZE = -1;

    public static final int SEQUENCE_INCREMENT = 1;

    public static final int TIMESTAMP_INCREMENT = 160;

    private DataTransmitConstants() {
        throw new AssertionError(
                "Cannot directly instantiate instances of this class.");
    }
}
