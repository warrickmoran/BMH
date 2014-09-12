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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raytheon.uf.common.bmh.audio.AudioConversionException;
import com.raytheon.uf.common.bmh.audio.UnsupportedAudioFormatException;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessage;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.edex.bmh.audio.AudioOverflowException;
import com.raytheon.uf.edex.bmh.audio.AudioRegulator;
import com.raytheon.uf.edex.bmh.dactransmit.tones.TonesGenerator;
import com.raytheon.uf.edex.bmh.generate.tones.ToneGenerationException;

/**
 * Allows for the asynchronous retrieval of audio data. Will adjust the audio
 * data according to a specified decibel range after successfully retrieving it.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 19, 2014 3532       bkowal      Initial creation
 * Aug 26, 2014 3558       rjpeter     Disable audio attenuation.
 * Sep 4, 2014  3532       bkowal      Use a decibel target instead of a range. Re-enable attenuation.
 * Sep 12, 2014 3588       bsteffen    Support audio fragments.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class RetrieveAudioJob implements PrioritizableCallable<AudioFileBuffer> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /* equivalent to 0.125 seconds */
    private static final int AUDIO_SAMPLE_SIZE = 1000;

    private final int priority;

    private final double dbTarget;

    private final DacPlaylistMessage message;

    private IAudioJobListener listener;

    private String taskId;

    /**
     * Constructor
     * 
     * @param priority
     *            the priority; the higher the number, the higher the associated
     *            priority will be.
     * @param dbTarget
     *            the target audio decibel. No portion of the audio should
     *            exceed the target.
     * @param message
     *            identifying information
     */
    public RetrieveAudioJob(final int priority, final double dbTarget,
            final DacPlaylistMessage message) {
        this.priority = priority;
        this.dbTarget = dbTarget;
        this.message = message;
    }

    /**
     * Constructor
     * 
     * @param priority
     *            the priority; the higher the number, the higher the associated
     *            priority will be.
     * @param dbTarget
     *            the target audio decibel. No portion of the audio should
     *            exceed the target.
     * @param message
     *            identifying information
     * @param listener
     *            a class to notify when the audio retrieval has finished
     *            (optional)
     * @param taskId
     *            identifier associated with a retrieval job for tracking
     *            purposes (optional)
     */
    public RetrieveAudioJob(final int priority, final double dbTarget,
            final DacPlaylistMessage message, final IAudioJobListener listener,
            final String taskId) {
        this(priority, dbTarget, message);
        this.listener = listener;
        this.taskId = taskId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.concurrent.Callable#call()
     */
    @Override
    public AudioFileBuffer call() throws Exception {
        final long start = System.currentTimeMillis();
        List<byte[]> rawDataArrays = new ArrayList<>(this.message
                .getSoundFiles().size());
        int concatenatedSize = 0;
        for (String soundFile : this.message.getSoundFiles()) {
            Path filePath = Paths.get(soundFile);

            /*
             * We really don't want to leave essentially null messages in the
             * cache for playback. However for the highly-controlled environment
             * of the initial demo version, file read errors and tone generation
             * errors shouldn't happen.
             * 
             * FIXME: if caching a message's audio data fails, perform retry of
             * some sort.
             */
            try {
                byte[] rawData = Files.readAllBytes(filePath);
                concatenatedSize += rawData.length;
                rawDataArrays.add(rawData);
            } catch (IOException e) {
                String msg = "Failed to buffer audio file for message: "
                        + message.getBroadcastId() + ", file: " + filePath;
                this.notifyAttemptComplete();
                throw new AudioRetrievalException(msg, e);
            }
        }
        byte[] rawData = null;
        if (rawDataArrays.size() == 1) {
            rawData = rawDataArrays.get(0);
        } else {
            ByteBuffer concatenation = ByteBuffer.allocate(concatenatedSize);
            for (byte[] array : rawDataArrays) {
                concatenation.put(array);
            }
            rawData = concatenation.array();
        }
        /* Adjust the raw audio data as needed. */
        byte[] regulatedRawData = this.adjustAudio(rawData, message, "Body");

        ByteBuffer tones = null;
        ByteBuffer endOfMessage = null;
        if ((message.getSAMEtone() != null)
                && (!message.getSAMEtone().isEmpty())) {
            try {
                endOfMessage = TonesGenerator.getEndOfMessageTones();
                tones = TonesGenerator.getSAMEAlertTones(message.getSAMEtone(),
                        message.isAlertTone());
            } catch (ToneGenerationException e) {
                String msg = "Unable to generate tones for message: "
                        + message.getBroadcastId();
                this.notifyAttemptComplete();
                throw new AudioRetrievalException(msg, e);
            }
        }

        /* regulate tones (if they exist). */
        if (tones != null) {
            byte[] regulatedTones = adjustAudio(tones.array(), message,
                    "SAME/Alert Tones");
            tones = ByteBuffer.wrap(regulatedTones);
        }
        if (endOfMessage != null) {
            byte[] regulatedEndOfMessage = adjustAudio(endOfMessage.array(),
                    message, "End of Message");
            endOfMessage = ByteBuffer.wrap(regulatedEndOfMessage);
        }

        AudioFileBuffer buffer = new AudioFileBuffer(regulatedRawData, tones,
                endOfMessage);
        logger.info("Successfully retrieved audio for message: "
                + message.getBroadcastId() + " in "
                + TimeUtil.prettyDuration(System.currentTimeMillis() - start)
                + ".");
        this.notifyAttemptComplete();

        return buffer;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.edex.bmh.dactransmit.playlist.PrioritizableRunnable#
     * getPriority()
     */
    @Override
    public Integer getPriority() {
        return this.priority;
    }

    private void notifyAttemptComplete() {
        if (this.listener != null && this.taskId != null) {
            this.listener.audioRetrievalFinished(this.taskId, this.message);
        }
    }

    /**
     * Adjusts the specified audio data so that it will be within the decibel
     * range of the transmitter.
     * 
     * @param originalAudio
     *            the specified audio data
     * @param message
     *            identifies the audio data; used for logging purposes
     * @param part
     *            identifies the portion of audio that is being adjusted; used
     *            for logging purposes
     * @return the adjusted audio data
     * @throws AudioRetrievalException
     */
    private byte[] adjustAudio(final byte[] originalAudio,
            final DacPlaylistMessage message, final String part)
            throws AudioRetrievalException {
        byte[] regulatedAudio = new byte[0];
        try {
            AudioRegulator audioRegulator = new AudioRegulator();
            regulatedAudio = audioRegulator.regulateAudioVolume(originalAudio,
                    this.dbTarget, AUDIO_SAMPLE_SIZE);
            logger.info("Successfully finished audio attenuation/amplification in "
                    + audioRegulator.getDuration()
                    + " ms for message: "
                    + message.getBroadcastId() + " (" + part + ").");
        } catch (UnsupportedAudioFormatException | AudioConversionException
                | AudioOverflowException e) {
            final String msg = "Failed to adjust the audio signal to the target "
                    + this.dbTarget
                    + " dB for message: "
                    + message.getBroadcastId();
            this.notifyAttemptComplete();
            throw new AudioRetrievalException(msg, e);
        }

        return regulatedAudio;
    }
}