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
package com.raytheon.bmh.dactransmit.rtp;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Random;

import com.raytheon.uf.common.bmh.dac.dacsession.DacSessionConstants;

/**
 * Factory class for building {@code RtpPacketIn} objects.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 2, 2014   #3268    dgilling     Initial creation
 * Oct 17, 2014  #3655    bkowal       Move tones to common.
 * 
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

public final class RtpPacketInFactory {

    private Integer sequenceNumber;

    private Long timestamp;

    /**
     * RTP spec's synchronization source identifier. Provides a means to
     * uniquely identify sessions.
     */
    private Integer ssrc;

    /**
     * The destination transmitters for this packet. Any combination of values
     * 1-4 are valid.
     */
    private Collection<Integer> transmitters;

    private byte[] previosPayload;

    private byte[] currentPayload;

    public static RtpPacketInFactory getInstance() {
        return new RtpPacketInFactory();
    }

    private RtpPacketInFactory() {
        this.sequenceNumber = null;
        this.timestamp = null;
        this.ssrc = null;
        this.transmitters = null;
        this.previosPayload = null;
        this.currentPayload = null;
    }

    public RtpPacketInFactory fromPacket(final RtpPacketIn packet) {
        return fromPacket(packet, true);
    }

    public RtpPacketInFactory fromPacket(final RtpPacketIn packet,
            boolean copyPayloads) {
        this.sequenceNumber = packet.getSequenceNumber();
        this.timestamp = packet.getTimestamp();
        this.ssrc = packet.getSsrc();
        this.transmitters = packet.getTransmitters();
        if (copyPayloads) {
            this.previosPayload = packet.getPreviousPayload();
            this.currentPayload = packet.getCurrentPayload();
        }
        return this;
    }

    public RtpPacketInFactory setSequenceNumber(final int newSequenceNum) {
        this.sequenceNumber = newSequenceNum;
        return this;
    }

    public RtpPacketInFactory incrementSequenceNum(final int increment) {
        return setSequenceNumber(sequenceNumber + increment);
    }

    public RtpPacketInFactory setTimestamp(final long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public RtpPacketInFactory incrementTimestamp(final long increment) {
        return setTimestamp(timestamp + increment);
    }

    public RtpPacketInFactory setSSRC(final int ssrc) {
        this.ssrc = ssrc;
        return this;
    }

    public RtpPacketInFactory addTransmitter(final int transmitter) {
        this.transmitters.add(transmitter);
        return this;
    }

    public RtpPacketInFactory setTransmitters(
            final Collection<Integer> transmitters) {
        this.transmitters = new HashSet<>(transmitters);
        return this;
    }

    public RtpPacketInFactory setPreviousPayload(final byte[] payload) {
        this.previosPayload = payload;
        return this;
    }

    public RtpPacketInFactory setCurrentPayload(final byte[] payload) {
        this.currentPayload = payload;
        return this;
    }

    /**
     * Given the factory arguments, construct the {@code RtpPacketIn} that
     * corresponds to these arguments.
     * 
     * @return The {@code RtpPacketIn} that corresponds to the factory
     *         arguments.
     */
    public RtpPacketIn create() {
        if (sequenceNumber == null) {
            throw new IllegalStateException(
                    "No packet sequence number specified.");
        }

        if (timestamp == null) {
            throw new IllegalStateException("No timestamp specified.");
        }

        if (ssrc == null) {
            ssrc = new Random().nextInt();
        }

        if ((transmitters == null) || (transmitters.isEmpty())) {
            throw new IllegalStateException(
                    "No destination transmitter specified.");
        }

        if (previosPayload == null) {
            previosPayload = new byte[DacSessionConstants.SINGLE_PAYLOAD_SIZE];
            Arrays.fill(previosPayload, DacSessionConstants.SILENCE);
        }

        if (currentPayload == null) {
            throw new IllegalStateException("No payload packet specified.");
        }

        return new RtpPacketIn(sequenceNumber, timestamp, ssrc, transmitters,
                previosPayload, currentPayload);
    }
}
