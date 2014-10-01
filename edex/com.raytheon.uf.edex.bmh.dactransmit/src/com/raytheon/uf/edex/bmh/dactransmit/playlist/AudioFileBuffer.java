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
 * Aug 12, 2014  #3286     dgilling     Integrate tone playback.
 * Sep 12, 2014  #3588     bsteffen     Support audio fragments.
 * Oct 01, 2014  #3485     bsteffen     Add skip() and isInTones()
 * 
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

public final class AudioFileBuffer {

    private final ByteBuffer tonesBuffer;

    private final ByteBuffer messageBuffer;

    private final ByteBuffer endOfMessageBuffer;

    private boolean returnTones;

    /**
     * Creates an {@code AudioFileBuffer} for the given byte array of data. No
     * tones will ever be played when playing this message.
     * 
     * @param data
     *            Data to wrap as an {@code AudioFileBuffer}.
     */
    public AudioFileBuffer(final byte[] data) {
        this(data, null, null);
    }

    /**
     * Creates an {@code AudioFileBuffer} for the given message and its
     * specified tones.
     * 
     * @param message
     *            The main audio message to play.
     * @param tones
     *            The tones to play prior to playing {@code message}.
     * @param endOfMessage
     *            The tones to play after playing {@code message}.
     */
    public AudioFileBuffer(final byte[] message, ByteBuffer tones,
            ByteBuffer endOfMessage) {
        this.messageBuffer = ByteBuffer.wrap(message).asReadOnlyBuffer();
        this.tonesBuffer = (tones != null) ? tones.asReadOnlyBuffer()
                : ByteBuffer.allocate(0).asReadOnlyBuffer();
        this.endOfMessageBuffer = (endOfMessage != null) ? endOfMessage
                .asReadOnlyBuffer() : ByteBuffer.allocate(0).asReadOnlyBuffer();
        this.returnTones = false;
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

        int bytesRemaining = length;
        if ((bytesRemaining > 0) && (returnTones)
                && (tonesBuffer.hasRemaining())) {
            int bytesToRead = Math.min(bytesRemaining, tonesBuffer.remaining());
            tonesBuffer.get(dst, offset, bytesToRead);
            bytesRemaining -= bytesToRead;
        }

        if ((bytesRemaining > 0) && (messageBuffer.hasRemaining())) {
            int bytesToRead = Math.min(bytesRemaining,
                    messageBuffer.remaining());
            messageBuffer.get(dst, offset, bytesToRead);
            bytesRemaining -= bytesToRead;
        }

        if ((bytesRemaining > 0) && (returnTones)
                && (endOfMessageBuffer.hasRemaining())) {
            int bytesToRead = Math.min(bytesRemaining,
                    endOfMessageBuffer.remaining());
            endOfMessageBuffer.get(dst, offset, bytesToRead);
            bytesRemaining -= bytesToRead;
        }

        if (bytesRemaining > 0) {
            Arrays.fill(dst, offset + (length - bytesRemaining), offset
                    + length, DacSessionConstants.SILENCE);
        }
    }
    
    public void skip(int length) {
        int bytesRemaining = length;
        if ((bytesRemaining > 0) && (returnTones)
                && (tonesBuffer.hasRemaining())) {
            int bytesToRead = Math.min(bytesRemaining, tonesBuffer.remaining());
            tonesBuffer.position(tonesBuffer.position() + bytesToRead);
            bytesRemaining -= bytesToRead;
        }

        if ((bytesRemaining > 0) && (messageBuffer.hasRemaining())) {
            int bytesToRead = Math.min(bytesRemaining,
                    messageBuffer.remaining());
            messageBuffer.position(messageBuffer.position() + bytesToRead);
            bytesRemaining -= bytesToRead;
        }

        if ((bytesRemaining > 0) && (returnTones)
                && (endOfMessageBuffer.hasRemaining())) {
            int bytesToRead = Math.min(bytesRemaining,
                    endOfMessageBuffer.remaining());
            endOfMessageBuffer.position(endOfMessageBuffer.position()
                    + bytesToRead);
            bytesRemaining -= bytesToRead;
        }
    }

    public boolean isInTones() {
        return returnTones
                && (tonesBuffer.hasRemaining() || (!messageBuffer
                        .hasRemaining() && endOfMessageBuffer.hasRemaining()));
    }

    /**
     * Rewinds this buffer.
     */
    public void rewind() {
        messageBuffer.rewind();
        tonesBuffer.rewind();
        endOfMessageBuffer.rewind();
    }

    /**
     * Tells whether there are any elements between the current position and the
     * limit.
     * 
     * @return {@code true} if, and only if, there is at least one element
     *         remaining in this buffer.
     */
    public boolean hasRemaining() {
        return returnTones ? (messageBuffer.hasRemaining()
                || tonesBuffer.hasRemaining() || endOfMessageBuffer
                .hasRemaining()) : messageBuffer.hasRemaining();
    }

    /**
     * Returns this buffer's capacity.
     * 
     * @param includeTones
     *            Whether or not to include the tones data in this calculation.
     * @return The capacity of this buffer
     */
    public int capacity(boolean includeTones) {
        int capacity = messageBuffer.capacity();
        if (includeTones) {
            capacity += (tonesBuffer.capacity() + endOfMessageBuffer.capacity());
        }
        return capacity;
    }

    /**
     * Whether or not to include the tones when retrieving data from this
     * buffer. This method will only have an effect if called before calling
     * get() for the first time.
     * 
     * @param returnTones
     *            {@code true} if the tones should be included.
     */
    public void setReturnTones(boolean returnTones) {
        if ((messageBuffer.position() == 0) && (tonesBuffer.position() == 0)
                && (endOfMessageBuffer.position() == 0)) {
            this.returnTones = returnTones;
        }
    }

    private static void checkBounds(int off, int len, int size) {
        if ((off | len | (off + len) | (size - (off + len))) < 0) {
            throw new IndexOutOfBoundsException();
        }
    }
}
