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
import javax.sound.sampled.LineUnavailableException;
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
     * Used to stream audio. Currently, to the default audio device.
     */
    private SourceDataLine line;

    /*
     * Allows classes that implement {@link LineListener} to subscribe to audio
     * events.
     */
    private List<LineListener> registeredListeners;

    private final int bufferSize;

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
        /* prepare the live audio stream */
        try {
            this.line = AudioSystem.getSourceDataLine(null);
            this.line.addLineListener(this);
            /* 1 second audio buffer */
            int recommendedSize = (int) (ULAW_AUDIO_FMT.getFrameSize()
                    * ULAW_AUDIO_FMT.getFrameRate() * 1);
            this.line.open(ULAW_AUDIO_FMT, recommendedSize);
            bufferSize = this.line.getBufferSize();
        } catch (LineUnavailableException e) {
            this.closeAudioStream();
            throw new DacPlaybackException(e);
        }
    }

    /**
     * Closes the live audio stream. This destructor should be used whenever the
     * listener is no longer required.
     */
    public void dispose() {
        this.closeAudioStream();
    }

    /**
     * Closes the live audio stream.
     */
    private void closeAudioStream() {
        if (this.line == null) {
            return;
        }

        this.line.close();
        this.line = null;
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
        int available = this.line.available();

        if (this.line.isActive()) {
            if (bufferSize == available) {
                /*
                 * If buffer empty, stop the line and allow buffer to partially
                 * fill.
                 */
                this.line.stop();
                statusHandler
                        .info("Audio buffer empty, pausing audio to buffer audio");
            }
        } else {
            if (available < payload.length) {
                /*
                 * When the line is inactive(not playing) and the buffer is
                 * full, start playing
                 */
                this.line.start();
                statusHandler.info("Starting audio.  Buffered: "
                        + (bufferSize - available));
            }
        }

        int bytesWritten = 0;

        while (bytesWritten < payload.length) {
            bytesWritten += line.write(payload, bytesWritten, payload.length
                    - bytesWritten);
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
}