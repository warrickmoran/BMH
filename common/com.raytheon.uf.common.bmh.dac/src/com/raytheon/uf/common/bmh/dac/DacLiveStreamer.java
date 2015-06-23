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
package com.raytheon.uf.common.bmh.dac;

import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.SourceDataLine;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

/**
 * An implementation of the {@link IDacListener}. Collects data from the dac
 * receive thread and live streams it as enough data is accumulated.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 15, 2014 3374       bkowal      Initial creation
 * Aug 25, 2014 3487       bsteffen    Do not start playback until buffer is full.
 * Jun 18, 2015 4482       rjpeter     Increase buffer to 2 seconds.  Buffer 1
 *                                     second before starting audio.
 * Jun 22, 2015 4482       rjpeter     Added dedicated audio streaming thread with 0.5s 
 *                                     buffer in audio line and additional 0.5s in memory.
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class DacLiveStreamer implements IDacListener, LineListener {
    /*
     * Data from the DAC will arrive in ulaw format.
     */
    private static final AudioFormat ULAW_AUDIO_FMT = new AudioFormat(
            Encoding.ULAW, 8000, 8, 1, 1, 8000, true);

    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(DacLiveStreamer.class);

    /*
     * The channel to retrieve data for and stream data from.
     */
    private final int channel;

    /*
     * Allows classes that implement {@link LineListener} to subscribe to audio
     * events.
     */
    private List<LineListener> registeredListeners;

    private volatile boolean closed = false;

    private final PacketStreamingThread streamingThread;

    /**
     * /** Constructor
     * 
     * @param channel
     *            the channel to receive data for
     * 
     * @throws DacPlaybackException
     *             if the live stream cannot be successfully instantiated and
     *             started.
     */
    public DacLiveStreamer(final int channel) throws DacPlaybackException {
        this.channel = channel;
        streamingThread = new PacketStreamingThread();
        streamingThread.start();
    }

    /**
     * Closes the live audio stream. This destructor should be used whenever the
     * listener is no longer required.
     */
    public void dispose() {
        closed = true;
        streamingThread.interrupt();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.common.bmh.dac.IDacListener#dataArrived(com.raytheon.
     * uf.common.bmh.dac.data.RTPPacketStruct)
     */
    @Override
    public void dataArrived(final byte[] payload) {
        if (!closed) {
            try {
                streamingThread.addData(payload);
            } catch (InterruptedException e) {
                // ignore
            }
        } else {
            throw new IllegalStateException(
                    "Cannot write to audio line.  Audio line closed");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.common.bmh.dac.IDacListener#getChannel()
     */
    @Override
    public int getChannel() {
        return this.channel;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.sound.sampled.LineListener#update(javax.sound.sampled.LineEvent)
     */
    @Override
    public void update(LineEvent event) {
        synchronized (this.registeredListeners) {
            for (LineListener listener : this.registeredListeners) {
                listener.update(event);
            }
        }
    }

    /**
     * Registers a new {@link LineListener}.
     * 
     * @param listener
     *            the listener to register
     */
    public void registerPlaybackListener(LineListener listener) {
        synchronized (this.registeredListeners) {
            this.registeredListeners.add(listener);
        }
    }

    /**
     * Removes an existing {@link LineListener} from the currently registered
     * listeners
     * 
     * @param listener
     *            the listener to remove
     */
    public void deregisterPlaybackListener(LineListener listener) {
        synchronized (this.registeredListeners) {
            this.registeredListeners.remove(listener);
        }
    }

    private class PacketStreamingThread extends Thread {
        /* 0.5 second audio buffer */
        private final int bufferSize = 4000;

        private final byte[] buffer = new byte[bufferSize];

        private int size = 0;

        private int head = 0;

        private int tail = 0;

        @Override
        public void run() {
            int tryCount = 0;

            while (!closed) {
                /* prepare the live audio stream */
                try (SourceDataLine line = AudioSystem.getSourceDataLine(null)) {
                    line.addLineListener(DacLiveStreamer.this);

                    line.open(ULAW_AUDIO_FMT, bufferSize);

                    int available = line.available();

                    // initial buffering of line
                    while (!closed && available > 0) {
                        writeDataToLine(line, available);
                        available = line.available();
                    }

                    // line fully buffered, start audio
                    statusHandler.debug("Starting audio.  available: "
                            + available + ", total buffer: "
                            + line.getBufferSize());
                    line.start();
                    available = line.available();

                    while (!closed && lineOk(line)) {
                        writeDataToLine(line, available);
                        available = line.available();
                        while (!closed && available < 160) {
                            Thread.sleep(20);
                            available = line.available();
                        }
                    }

                    if (!closed) {
                        /*
                         * Loop exited due to audio line buffer problem. Let
                         * audio line close and be recreated.
                         */
                        statusHandler
                                .warn("Audio line error detected, closing audio line.  New audio line will be opened and buffered.");
                        synchronized (buffer) {
                            /*
                             * Use entire buffer as it will have already wrapped
                             * around.
                             */
                            head = tail;
                            size = buffer.length;
                        }
                    }
                } catch (InterruptedException e) {
                    // ignore
                } catch (Exception e) {
                    if (tryCount > 3) {
                        closed = true;
                    } else {
                        tryCount++;
                    }

                    statusHandler.error("Error occurred with audio line", e);
                }

                synchronized (buffer) {
                    buffer.notifyAll();
                }
            }
        }

        /**
         * Write data from byte buffer to line. Will block if no data current in
         * byte buffer.
         * 
         * @param line
         * @param available
         * @throws InterruptedException
         */
        public void writeDataToLine(SourceDataLine line, int available)
                throws InterruptedException {
            synchronized (buffer) {
                while (size <= 0 && !closed) {
                    buffer.wait();
                }

                int bytesToWrite = available;
                if (bytesToWrite > size) {
                    bytesToWrite = size;
                }

                if (bytesToWrite > 0) {
                    /* check buffer wrap around */
                    if (head + bytesToWrite >= buffer.length) {
                        int wrapBytesToWrite = buffer.length - head;
                        line.write(buffer, head, wrapBytesToWrite);
                        bytesToWrite -= wrapBytesToWrite;
                        size -= wrapBytesToWrite;
                        head = 0;
                    }

                    /* may have written entire buffer already */
                    if (bytesToWrite > 0) {
                        line.write(buffer, head, bytesToWrite);
                        size -= bytesToWrite;
                        head += bytesToWrite;
                    }

                    buffer.notify();
                }
            }
        }

        /**
         * Write data to circular byte buffer. Will block if not enough room
         * currently available.
         * 
         * @param payload
         * @throws InterruptedException
         */
        public void addData(byte[] payload) throws InterruptedException {
            synchronized (buffer) {
                if (payload.length > buffer.length) {
                    throw new IllegalArgumentException(
                            "payload too large to write to buffer");
                }

                while (size + payload.length > buffer.length && !closed) {
                    buffer.wait();
                }

                int bytesToWrite = payload.length;

                /* Check buffer wrap around */
                if (tail + bytesToWrite >= buffer.length) {
                    int wrapBytesToWrite = buffer.length - tail;
                    System.arraycopy(payload, 0, buffer, tail, wrapBytesToWrite);
                    bytesToWrite -= wrapBytesToWrite;
                    tail = 0;
                }

                /* may have written entire buffer already */
                if (bytesToWrite > 0) {
                    System.arraycopy(payload, 0, buffer, tail, bytesToWrite);
                    tail += bytesToWrite;
                }

                size += payload.length;
                buffer.notify();
            }
        }

        private boolean lineOk(SourceDataLine line) {
            if (line.available() > line.getBufferSize()) {
                return false;
            } else if (line.available() == line.getBufferSize()) {
                return false;
            }

            return true;
        }
    }
}