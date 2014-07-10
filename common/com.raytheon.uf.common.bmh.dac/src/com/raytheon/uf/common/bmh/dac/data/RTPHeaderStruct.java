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
 * A data structure representing the header of a RTP Packet.
 * 
 * Reference: http://www.siptutorial.net/RTP/header.html
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 10, 2014 3374       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class RTPHeaderStruct {
    private int version;

    private int contributingSource;

    private boolean padding;

    private boolean extensions;

    private boolean marker;

    private int payloadType;

    private int sequenceNumber;

    private long timestamp;

    private long syncSource;

    /**
     * Constructor
     * 
     * @param headerBytes
     *            a buffer with the header information
     */
    public RTPHeaderStruct(final ByteBuffer headerBytes) {
        this.readFlags(headerBytes.get(0));
        this.readMPT(headerBytes.get(1));
        this.readSequenceNumber(headerBytes.getShort(2));
        this.readTimestamp(headerBytes);
        this.readSynchronizationSource(headerBytes);
    }

    /**
     * Determines the version and various flags based on the first byte in the
     * header.
     * 
     * @param headerByte
     *            the first byte in the header
     */
    private void readFlags(final byte headerByte) {
        this.version = (headerByte & 0xC0) >> 6;
        this.contributingSource = headerByte & 0x0F;
        this.padding = (headerByte & 0x20) == 0x020;
        this.extensions = (headerByte & 0x10) == 0x010;
    }

    /**
     * Determines the marker and the payload type based on the second byte in
     * the header.
     * 
     * @param headerByte
     *            the second byte in the header
     */
    private void readMPT(final byte headerByte) {
        this.marker = (headerByte & 0xff & 0x80) == 0x80;
        this.payloadType = (headerByte & 0xff & 0x7f);
    }

    /**
     * Determines the sequence number based on the third and fourth bits in the
     * header.
     * 
     * @param sequenceNumber
     *            the third and fourth bytes in the header as a short
     */
    private void readSequenceNumber(short sequenceNumber) {
        this.sequenceNumber = (sequenceNumber & 0xffff);
    }

    /**
     * Determines the timestamp based on the fourth, fifth, sixth, and seventh
     * bits in the header.
     * 
     * @param headerBytes
     */
    private void readTimestamp(final ByteBuffer headerBytes) {
        this.timestamp = ((long) (headerBytes.get(4) & 0xff) << 24)
                | ((long) (headerBytes.get(5) & 0xff) << 16)
                | ((long) (headerBytes.get(6) & 0xff) << 8)
                | ((long) (headerBytes.get(7) & 0xff));
    }

    /**
     * Determines the synchronization source based on the eighth, ninth, tenth,
     * and eleventh bits in the header.
     * 
     * @param headerBytes
     *            a buffer with the header information
     */
    private void readSynchronizationSource(final ByteBuffer headerBytes) {
        this.syncSource = ((long) (headerBytes.get(8) & 0xff) << 24)
                | ((long) (headerBytes.get(9) & 0xff) << 16)
                | ((long) (headerBytes.get(10) & 0xff) << 8)
                | ((long) (headerBytes.get(11) & 0xff));
    }

    /**
     * Returns the RTP Version
     * 
     * @return the RTP Version
     */
    public int getVersion() {
        return version;
    }

    /**
     * Returns the RTP Contributing Source
     * 
     * @return the RTP Contributing Source
     */
    public int getContributingSource() {
        return contributingSource;
    }

    /**
     * Indicates whether or not the padding bit is set.
     * 
     * @return a boolean indicating whether or not the padding bit has been set.
     */
    public boolean isPadding() {
        return padding;
    }

    /**
     * Indicates whether or not the extension bit is set.
     * 
     * @return a boolean indicating whether or not the extension bit has been
     *         set.
     */
    public boolean isExtensions() {
        return extensions;
    }

    /**
     * Indicates whether or not the marker bit has been set.
     * 
     * @return a boolean indicating whether or not the marker bit has been set.
     */
    public boolean isMarker() {
        return marker;
    }

    /**
     * Returns the payload type
     * 
     * @return the payload type
     */
    public int getPayloadType() {
        return payloadType;
    }

    /**
     * Returns the sequence number
     * 
     * @return the sequence number
     */
    public int getSequenceNumber() {
        return sequenceNumber;
    }

    /**
     * Returns the timestamp
     * 
     * @return the timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Returns the synchronization source
     * 
     * @return the synchronization source
     */
    public long getSyncSource() {
        return syncSource;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("[ version = ");
        stringBuilder.append(this.version);
        stringBuilder.append(" ; contributingSource = ");
        stringBuilder.append(this.contributingSource);
        stringBuilder.append(" ; padding = ");
        stringBuilder.append(this.padding);
        stringBuilder.append(" ; extensions = ");
        stringBuilder.append(this.extensions);
        stringBuilder.append(" ; marker = ");
        stringBuilder.append(this.marker);
        stringBuilder.append(" ; payloadType = ");
        stringBuilder.append(this.payloadType);
        stringBuilder.append(" ; sequenceNumber = ");
        stringBuilder.append(this.sequenceNumber);
        stringBuilder.append(" ; timestamp = ");
        stringBuilder.append(this.timestamp);
        stringBuilder.append(" ; syncSource = ");
        stringBuilder.append(this.syncSource);
        stringBuilder.append(" ]");

        return stringBuilder.toString();
    }
}
