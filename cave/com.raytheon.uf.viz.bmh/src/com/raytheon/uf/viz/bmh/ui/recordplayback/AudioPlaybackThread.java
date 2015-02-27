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
package com.raytheon.uf.viz.bmh.ui.recordplayback;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineEvent.Type;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;

/**
 * Manages the playback of recorded audio.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 7, 2014  3657       bkowal      Initial creation
 * Oct 26, 2014 3748       bkowal      Implemented new/resume.
 * Dec 10, 2014 3883       bkowal      Added {@link #isPaused()}.
 * Feb 27, 2015 4149       rferrel     Add check for null audio clip in halt().
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class AudioPlaybackThread extends Thread implements LineListener {

    // TODO: need a common location for this format throughout BMH.
    private static final AudioFormat ULAW_AUDIO_FMT = new AudioFormat(
            Encoding.ULAW, 8000, 8, 1, 1, 8000, true);

    private IPlaybackCompleteListener listener;

    protected volatile boolean isPaused = false;

    private Clip audioClip;

    public AudioPlaybackThread(ByteBuffer audio) throws AudioException {
        this(audio.array());
    }

    public AudioPlaybackThread(byte[] audio) throws AudioException {
        super(AudioPlaybackThread.class.getName());

        AudioInputStream audioInputStream = new AudioInputStream(
                new ByteArrayInputStream(audio), ULAW_AUDIO_FMT, audio.length);

        try {
            this.audioClip = AudioSystem.getClip();
            this.audioClip.addLineListener(this);
            this.audioClip.open(audioInputStream);
        } catch (LineUnavailableException | IOException e) {
            this.dispose();
            throw new AudioException("Failed to load the audio!", e);
        }
    }

    public int getAudioLengthInSeconds() {
        if (this.audioClip == null) {
            return 0;
        }
        return (int) this.audioClip.getMicrosecondLength() / 1000000;
    }

    public void setCompleteListener(IPlaybackCompleteListener listener) {
        this.listener = listener;
    }

    public void dispose() {
        if (this.audioClip == null) {
            return;
        }

        this.audioClip.close();
        this.audioClip = null;
    }

    @Override
    public void run() {
        this.audioClip.loop(0);
        this.audioClip.start();
    }

    /**
     * Stops the audio clip. But, also leaves the clip in a state so that it can
     * be resumed.
     */
    public void pausePlayback() {
        this.isPaused = true;
        this.audioClip.stop();
    }

    public void resumePlayback() {
        this.isPaused = false;
        this.audioClip.start();
    }

    public void halt() {
        if (this.audioClip != null) {
            this.audioClip.stop();
            this.dispose();
        }
    }

    @Override
    public void update(LineEvent event) {
        if (event.getType() == Type.STOP && this.isPaused == false) {
            this.dispose();
            if (this.listener != null) {
                this.listener.notifyPlaybackComplete();
            }
        }
    }

    /**
     * Returns a boolean indicating whether or not the playback thread is
     * currently paused.
     * 
     * @return true if the playback thread has been paused; false, otherwise
     */
    public boolean isPaused() {
        return isPaused;
    }
}