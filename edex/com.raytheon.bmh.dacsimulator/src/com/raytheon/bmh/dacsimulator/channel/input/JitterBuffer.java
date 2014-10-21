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
package com.raytheon.bmh.dacsimulator.channel.input;

import org.apache.commons.collections.Buffer;
import org.apache.commons.collections.BufferUtils;
import org.apache.commons.collections.buffer.CircularFifoBuffer;


/**
 * Simulates the buffer that received audio data packets are stored in. This
 * buffer is fixed size and if the buffer hits capacity, the next packet
 * received will cause the oldest packet in the buffer to be removed. This
 * buffer also has an attribute called minimum buffer size, which determines
 * when this buffer is ready to broadcast. To be ready for broadcast the buffer
 * must accumulate at least the minimum number of packets in it. Once broadcast
 * begins, it continues to broadcast until the buffer has been emptied.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 09, 2014  #3688     dgilling     Initial creation
 * Oct 21, 2014  #3688     dgilling     Support audio streams that can go
 *                                      to specified output channel(s).
 * 
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

public class JitterBuffer {

    private static final int BUFFER_CAPACITY = 255;

    private final Buffer buffer;

    private final int minBufferSize;

    private volatile boolean readyForBroadcast;

    /**
     * Constructor.
     * 
     * @param minBufferSize
     *            Number of packets required to begin broadcasting from this
     *            buffer.
     */
    public JitterBuffer(int minBufferSize) {
        this.buffer = BufferUtils.synchronizedBuffer(new CircularFifoBuffer(
                BUFFER_CAPACITY));
        this.minBufferSize = minBufferSize;
        this.readyForBroadcast = false;
    }

    @SuppressWarnings("unchecked")
    public boolean add(AudioPacket element) {
        boolean retVal = buffer.add(element);

        if (!readyForBroadcast) {
            readyForBroadcast = (buffer.size() >= minBufferSize);
        }

        return retVal;
    }

    public AudioPacket get() {
        AudioPacket retVal = (AudioPacket) buffer.remove();

        if (readyForBroadcast && buffer.isEmpty()) {
            readyForBroadcast = false;
        }

        return retVal;
    }

    /**
     * Determines if this buffer has met the qualifications to begin
     * broadcasting. To begin broadcasting, the buffer must have at least the
     * minimum number of packets in the buffer at a moment in time. If the
     * buffer ever completely empties, the buffer must be refilled to the
     * minimum before broadcasting can begin again.
     * 
     * @return Whether or not this buffer is ready for broadcast.
     */
    public boolean isReadyForBroadcast() {
        return readyForBroadcast;
    }

    /**
     * Returns the number of elements in this collection.
     * 
     * @return the number of elements in this collection
     */
    public int size() {
        return buffer.size();
    }
}
