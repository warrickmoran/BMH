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
package com.raytheon.uf.edex.bmh.dactransmit.rtp;

import java.nio.ByteBuffer;
import java.util.Collection;

import com.raytheon.uf.edex.bmh.dactransmit.dacsession.DacSessionConstants;
import com.raytheon.uf.edex.bmh.dactransmit.util.PrimitiveTypeConversion;

/**
 * RTP-like packet structure for sending audio data to the DAC appliance.
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

public final class RtpPacketIn {

    private static final Integer ONE = Integer.valueOf(1);

    private static final Integer TWO = Integer.valueOf(2);

    private static final Integer THREE = Integer.valueOf(3);

    private static final Integer FOUR = Integer.valueOf(4);

    /**
     * Corresponds to Version: 2, Padding: 0, Extension: 1, CSRC count: 0
     */
    private static final byte FLAGS = (byte) 0x90;

    /**
     * Corresponds to Marker: 0, Payload type: 121
     */
    private static final byte MARKER_PAYLOAD_TYPE = (byte) 0x79;

    // in the protocol specification, this is actually an unsigned short. I
    // use int so the full range of values can be represented.
    private int sequenceNumber;

    // in the protocol specification, this is actually an unsigned 32-bit int. I
    // use long so the full range of values can be represented.
    private long timestamp;

    /**
     * RTP spec's synchronization source identifier. Provides a means to
     * uniquely identify sessions.
     */
    private int ssrc;

    /**
     * Corresponds to RTP extension header ID: 103
     */
    private static final byte[] RTP_EXT_HEADER_TYPE = { (byte) 0x00,
            (byte) 0x67 };

    /**
     * Tells clients we have 1 32-bit piece of RTP header extension data
     */
    private static final byte[] RTP_EXT_HEADER_LENGTH = { (byte) 0x00,
            (byte) 0x01 };

    /**
     * The destination transmitters for this packet. Any combination of values
     * 1-4 are valid.
     */
    private Collection<Integer> transmitters;

    private byte[] previousPayload;

    private byte[] currentPayload;

    /**
     * Constructor for an RTP packet.
     * 
     * @param sequenceNumber
     *            Packet sequence number.
     * @param timestamp
     *            Timestamp for this packet.
     * @param ssrc
     *            Synchronization source identifier
     * @param transmitters
     *            List of transmitters to send this packet to.
     * @param previousPayload
     *            The previous data payload sent.
     * @param currentPayload
     *            The current data payload to send.
     */
    protected RtpPacketIn(int sequenceNumber, long timestamp, int ssrc,
            Collection<Integer> transmitters, final byte[] previousPayload,
            final byte[] currentPayload) {
        this.sequenceNumber = sequenceNumber;
        this.timestamp = timestamp;
        this.ssrc = ssrc;
        this.transmitters = transmitters;

        if (previousPayload.length != DacSessionConstants.SINGLE_PAYLOAD_SIZE) {
            throw new IllegalArgumentException(
                    "Previous payload array should be "
                            + DacSessionConstants.SINGLE_PAYLOAD_SIZE
                            + " bytes in length.");
        }
        if (currentPayload.length != DacSessionConstants.SINGLE_PAYLOAD_SIZE) {
            throw new IllegalArgumentException(
                    "Current payload array should be "
                            + DacSessionConstants.SINGLE_PAYLOAD_SIZE
                            + " bytes in length.");
        }

        this.previousPayload = previousPayload;
        this.currentPayload = currentPayload;
    }

    /**
     * Encode this RTP packet into the raw bytes for sending via a
     * {@code DatagramPacket}.
     * 
     * @return The raw bytes for this RTP packet.
     */
    public byte[] encode() {
        ByteBuffer packet = ByteBuffer
                .allocate(DacSessionConstants.RTP_PACKET_SIZE);

        packet.put(FLAGS);
        packet.put(MARKER_PAYLOAD_TYPE);
        packet.put(PrimitiveTypeConversion.intToUInt16Bytes(sequenceNumber));
        packet.put(PrimitiveTypeConversion.longToUInt32Bytes(timestamp));
        packet.putInt(ssrc);

        packet.put(RTP_EXT_HEADER_TYPE);
        packet.put(RTP_EXT_HEADER_LENGTH);
        packet.putInt(getDacAddressing());

        packet.put(previousPayload);
        packet.put(currentPayload);

        return packet.array();
    }

    private int getDacAddressing() {
        int value = 0;

        if (transmitters.contains(ONE)) {
            value |= 1;
        }
        if (transmitters.contains(TWO)) {
            value |= 2;
        }
        if (transmitters.contains(THREE)) {
            value |= 4;
        }
        if (transmitters.contains(FOUR)) {
            value |= 8;
        }

        return value;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getSsrc() {
        return ssrc;
    }

    public Collection<Integer> getTransmitters() {
        return transmitters;
    }

    public byte[] getPreviousPayload() {
        return previousPayload;
    }

    public byte[] getCurrentPayload() {
        return currentPayload;
    }
}
