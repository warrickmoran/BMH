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
package com.raytheon.uf.common.bmh.dac.data;

import java.nio.ByteBuffer;

/**
 * A data structure representing a packet.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 10, 2014            bkowal     Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class RTPPacketStruct {
    public static final int RTP_PACKET_HDR_SIZE = 12;

    public static final int DATA_PAYLOAD_SIZE = 160;

    public static final int PACKETS_IN_BUFFER = 4;

    public static final int PACKET_SIZE = RTP_PACKET_HDR_SIZE
            + (DATA_PAYLOAD_SIZE * PACKETS_IN_BUFFER);

    private final RTPHeaderStruct header;

    private final byte[] payload;

    /**
     * Constructor
     * 
     * @param headerBuffer
     *            a buffer with the header information
     * @param payload
     *            the payload bytes
     */
    public RTPPacketStruct(final ByteBuffer headerBuffer, final byte[] payload) {
        this(new RTPHeaderStruct(headerBuffer), payload);
    }

    public RTPPacketStruct(RTPHeaderStruct header, final byte[] payload) {
        this.header = header;
        this.payload = payload;
    }

    /**
     * Returns the packet header
     * 
     * @return the packet header
     */
    public RTPHeaderStruct getHeader() {
        return header;
    }

    /**
     * Returns the packet payload data
     * 
     * @return the packet payload data
     */
    public byte[] getPayload() {
        return this.payload;
    }
}