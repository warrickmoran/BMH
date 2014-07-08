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
 * Useful constants for the DacSession and its components.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 1, 2014   #3268    dgilling     Initial creation
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

    private DacSessionConstants() {
        throw new AssertionError(
                "Cannot directly instantiate instances of this class.");
    }
}
