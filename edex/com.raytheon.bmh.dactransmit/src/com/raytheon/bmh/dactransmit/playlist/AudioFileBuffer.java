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
package com.raytheon.bmh.dactransmit.playlist;

import java.nio.ByteBuffer;
import java.util.Arrays;

import com.raytheon.uf.common.bmh.dac.dacsession.DacSessionConstants;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessage;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.edex.bmh.msg.logging.DefaultMessageLogger;
import com.raytheon.uf.edex.bmh.msg.logging.IMessageLogger.TONE_TYPE;

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
 * Oct 2, 2014   #3642     bkowal       Abstract to support dynamic audio fragments.
 * Oct 17, 2014  #3655     bkowal       Move tones to common.
 * Oct 30, 2014  #3617     dgilling     Add getter for field returnTones.
 * Oct 31, 2014  #3779     dgilling     Add a second of silence to the end of
 *                                      each complete audio file.
 * Dec 11, 2014  3651      bkowal       Use {@link DefaultMessageLogger} to log tone
 *                                      msg activity.
 * Jan 15, 2015  3999      bkowal       Adjust offsets as data is added to the destination
 *                                      array in {@link #get(byte[], int, int)}.
 * Feb 02, 2015  4093      bsteffen     Add position()
 * Mar 13, 2015  4251      bkowal       Limit messages accompanied by tones to 2 minutes.
 * Mar 17, 2015  4251      bkowal       Handle shorter interrupts without buffer underflows.
 * May 04, 2015  4452      bkowal       Added {@link #toneTruncationRequired()}.
 * May 13, 2015  4429       rferrel     Changes to {@link DefaultMessageLogger} for traceId.
 * 
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

public class AudioFileBuffer extends AbstractAudioFileBuffer {

    /**
     * A message can only be a maximum of two (2) minutes when accompanied by
     * tones.
     */
    private static final int MAX_TONES_MSG_DURATION_SECONDS = 120;

    public static final int MAX_TONES_MSG_AUDIO_BYTE_COUNT = ((MAX_TONES_MSG_DURATION_SECONDS * (int) TimeUtil.MILLIS_PER_SECOND) / 20) * 160;

    /**
     * Between each message we must play around 1 second of silence. That's the
     * same as 8000 bytes of silence.
     */
    private static final int SILENCE_LENGTH = 8000;

    private static final byte[] END_OF_MESSAGE_BYTES = new byte[SILENCE_LENGTH];

    static {
        Arrays.fill(END_OF_MESSAGE_BYTES, DacSessionConstants.SILENCE);
    }

    private final ByteBuffer tonesBuffer;

    private final ByteBuffer messageBuffer;

    private final ByteBuffer endOfMessageTones;

    private final ByteBuffer endOfMessageSilence;

    private boolean returnTones;

    /**
     * Creates an {@code AudioFileBuffer} for the given byte array of data. No
     * tones will ever be played when playing this message.
     * 
     * @param data
     *            Data to wrap as an {@code AudioFileBuffer}.
     */
    public AudioFileBuffer(final DacPlaylistMessage dacMsg, final byte[] data) {
        this(dacMsg, data, null, null);
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
    public AudioFileBuffer(final DacPlaylistMessage dacMsg,
            final byte[] message, ByteBuffer tones, ByteBuffer endOfMessage) {
        super(dacMsg);
        this.messageBuffer = ByteBuffer.wrap(message).asReadOnlyBuffer();
        this.tonesBuffer = (tones != null) ? tones.asReadOnlyBuffer()
                : ByteBuffer.allocate(0).asReadOnlyBuffer();
        this.endOfMessageTones = (endOfMessage != null) ? endOfMessage
                .asReadOnlyBuffer() : ByteBuffer.allocate(0).asReadOnlyBuffer();
        this.endOfMessageSilence = ByteBuffer.wrap(END_OF_MESSAGE_BYTES)
                .asReadOnlyBuffer();
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
            offset += bytesToRead;
            if (this.tonesBuffer.hasRemaining() == false) {
                // have we reached the end of the tones?

                // SAME and/or alert tones have been broadcast, log it.
                if (this.message.getSAMEtone() != null) {
                    DefaultMessageLogger.getInstance().logTonesActivity(
                            this.message, TONE_TYPE.SAME, this.message);
                }
                if (this.message.isAlertTone()) {
                    DefaultMessageLogger.getInstance().logTonesActivity(
                            this.message, TONE_TYPE.ALERT, this.message);
                }
            }
        }

        if ((bytesRemaining > 0)
                && (messageBuffer.hasRemaining() && this.isToneTruncated() == false)) {
            int bytesToRead = Math.min(bytesRemaining,
                    (this.returnTones) ? this.getTruncatedBytesRemaining()
                            : messageBuffer.remaining());
            messageBuffer.get(dst, offset, bytesToRead);
            bytesRemaining -= bytesToRead;
            offset += bytesToRead;
        }

        if ((bytesRemaining > 0) && (returnTones)
                && (endOfMessageTones.hasRemaining())) {
            int bytesToRead = Math.min(bytesRemaining,
                    endOfMessageTones.remaining());
            endOfMessageTones.get(dst, offset, bytesToRead);
            bytesRemaining -= bytesToRead;
            offset += bytesToRead;
            if (this.endOfMessageTones.hasRemaining() == false) {
                // end tones have been broadcast, log it.
                DefaultMessageLogger.getInstance().logTonesActivity(
                        this.message, TONE_TYPE.END, this.message);
            }
        }

        if ((bytesRemaining > 0) && (endOfMessageSilence.hasRemaining())) {
            int bytesToRead = Math.min(bytesRemaining,
                    endOfMessageSilence.remaining());
            endOfMessageSilence.get(dst, offset, bytesToRead);
            bytesRemaining -= bytesToRead;
            offset += bytesToRead;
        }

        if (bytesRemaining > 0) {
            /*
             * fill starting from the current offset to the length of the array.
             * fromIndex is inclusive and the endIndex is exclusive.
             */
            Arrays.fill(dst, offset, length, DacSessionConstants.SILENCE);
        }
    }

    public int position() {
        int position = 0;
        if (returnTones) {
            position += tonesBuffer.position();
        }
        position += messageBuffer.position();
        if (returnTones) {
            position += endOfMessageTones.position();
        }
        position += endOfMessageSilence.position();
        return position;
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
                && (endOfMessageTones.hasRemaining())) {
            int bytesToRead = Math.min(bytesRemaining,
                    endOfMessageTones.remaining());
            endOfMessageTones.position(endOfMessageTones.position()
                    + bytesToRead);
            bytesRemaining -= bytesToRead;
        }

        if ((bytesRemaining > 0) && (endOfMessageSilence.hasRemaining())) {
            int bytesToRead = Math.min(bytesRemaining,
                    endOfMessageSilence.remaining());
            endOfMessageSilence.position(endOfMessageSilence.position()
                    + bytesToRead);
            bytesRemaining -= bytesToRead;
        }
    }

    public boolean isInTones() {
        return returnTones
                && (tonesBuffer.hasRemaining() || (!messageBuffer
                        .hasRemaining() && endOfMessageTones.hasRemaining()));
    }

    public boolean isToneTruncated() {
        return this.returnTones
                && (this.messageBuffer.position() >= MAX_TONES_MSG_AUDIO_BYTE_COUNT);
    }

    public boolean toneTruncationRequired() {
        return this.returnTones
                && this.messageBuffer.capacity() > MAX_TONES_MSG_AUDIO_BYTE_COUNT;
    }

    public int getTruncatedBytesRemaining() {
        if (this.messageBuffer.capacity() <= MAX_TONES_MSG_AUDIO_BYTE_COUNT) {
            return this.messageBuffer.remaining();
        }

        int calculatedBytesRemaining = MAX_TONES_MSG_AUDIO_BYTE_COUNT
                - this.messageBuffer.position();
        return (calculatedBytesRemaining < 0) ? 0 : calculatedBytesRemaining;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.bmh.dactransmit.playlist.AbstractAudioFileBuffer
     * #rewind()
     */
    @Override
    public void rewind() {
        messageBuffer.rewind();
        tonesBuffer.rewind();
        endOfMessageTones.rewind();
        endOfMessageSilence.rewind();
    }

    /**
     * Tells whether there are any elements between the current position and the
     * limit.
     * 
     * @return {@code true} if, and only if, there is at least one element
     *         remaining in this buffer.
     */
    public boolean hasRemaining() {
        return returnTones ? (getTruncatedBytesRemaining() > 0
                || tonesBuffer.hasRemaining()
                || endOfMessageTones.hasRemaining() || endOfMessageSilence
                .hasRemaining())
                : (messageBuffer.hasRemaining() || endOfMessageSilence
                        .hasRemaining());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.bmh.dactransmit.playlist.IAudioFileBuffer#capacity
     * (boolean)
     */
    @Override
    public int capacity(boolean includeTones) {
        int capacity = (includeTones) ? Math.min(messageBuffer.capacity(),
                MAX_TONES_MSG_AUDIO_BYTE_COUNT) : messageBuffer.capacity();
        capacity += endOfMessageSilence.capacity();
        if (includeTones) {
            capacity += (tonesBuffer.capacity() + endOfMessageTones.capacity());
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
                && (endOfMessageTones.position() == 0)
                && (endOfMessageSilence.position() == 0)) {
            this.returnTones = returnTones;
        }
    }

    public boolean isReturnTones() {
        return returnTones;
    }

    private static void checkBounds(int off, int len, int size) {
        if ((off | len | (off + len) | (size - (off + len))) < 0) {
            throw new IndexOutOfBoundsException();
        }
    }
}
