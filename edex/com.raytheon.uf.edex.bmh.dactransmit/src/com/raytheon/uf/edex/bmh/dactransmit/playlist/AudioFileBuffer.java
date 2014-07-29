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
package com.raytheon.uf.edex.bmh.dactransmit.playlist;

import java.nio.ByteBuffer;
import java.util.Arrays;

import com.raytheon.uf.edex.bmh.dactransmit.dacsession.DacSessionConstants;

/**
 * A slightly modified {@code ByteBufer} that buffers the contents of an audio
 * file that will be sent to the DAC. Primary difference between this class and
 * {@code ByteBuffer} is that if a bulk-get overruns the end of the buffer, this
 * class will not throw an {@code Exception}. Instead, it pads the rest of the
 * remaining byte-array with special SILENCE bytes.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 02, 2014  #3286     dgilling     Initial creation
 * Jul 29, 2014  #3286     dgilling     Add capacity().
 * 
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

public final class AudioFileBuffer {

    private final ByteBuffer buffer;

    /**
     * Creates an {@code AudioFileBuffer} for the given byte array of data.
     * 
     * @param data
     *            Data to wrap as an {@code AudioFileBuffer}.
     */
    public AudioFileBuffer(final byte[] data) {
        buffer = ByteBuffer.wrap(data).asReadOnlyBuffer();
    }

    /**
     * Relative bulk get method.
     * <p>
     * This method transfers bytes from this buffer into the given destination
     * array. An invocation of this method of the form <tt>src.get(a)</tt>
     * behaves in exactly the same way as the invocation
     * 
     * <pre>
     * src.get(a, 0, a.length)
     * </pre>
     * 
     * @param dst
     *            The array into which bytes are to be written.
     */
    public void get(byte[] dst) {
        get(dst, 0, dst.length);
    }

    /**
     * Relative bulk get method.
     * <p>
     * This method transfers bytes from this buffer into the given destination
     * array. If there are fewer bytes remaining in the buffer than are required
     * to satisfy the request, that is, if length > remaining(), then the
     * remaining bytes from the audio file are transferred. The rest of the
     * array is filled with SILENCE padding bytes.
     * 
     * @param dst
     *            The array into which bytes are to be written.
     * @param offset
     *            The offset within the array of the first byte to be written;
     *            must be non-negative and no larger than {@code dst.length}.
     * @param length
     *            The maximum number of bytes to be written to the given array;
     *            must be non-negative.
     */
    public void get(byte[] dst, int offset, int length) {
        checkBounds(offset, length, dst.length);

        int bytesFromBuffer = length;
        if (length > buffer.remaining()) {
            bytesFromBuffer = buffer.remaining();
        }

        buffer.get(dst, offset, bytesFromBuffer);
        if (bytesFromBuffer < length) {
            Arrays.fill(dst, offset + bytesFromBuffer, offset + length,
                    DacSessionConstants.SILENCE);
        }
    }

    /**
     * Rewinds this buffer.
     */
    public void rewind() {
        buffer.rewind();
    }

    /**
     * Tells whether there are any elements between the current position and the
     * limit.
     * 
     * @return {@code true} if, and only if, there is at least one element
     *         remaining in this buffer.
     */
    public boolean hasRemaining() {
        return buffer.hasRemaining();
    }

    /**
     * Returns this buffer's capacity.
     * 
     * @return The capacity of this buffer
     */
    public int capcity() {
        return buffer.capacity();
    }

    private static void checkBounds(int off, int len, int size) {
        if ((off | len | (off + len) | (size - (off + len))) < 0) {
            throw new IndexOutOfBoundsException();
        }
    }
}
