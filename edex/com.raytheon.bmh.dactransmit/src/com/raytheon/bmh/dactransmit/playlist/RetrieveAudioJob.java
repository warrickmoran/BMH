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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Table;
import com.raytheon.uf.common.bmh.TIME_MSG_TOKENS;
import com.raytheon.uf.common.bmh.audio.AudioRetrievalException;
import com.raytheon.uf.common.bmh.dac.tones.TonesGenerator;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessage;
import com.raytheon.uf.common.bmh.tones.GeneratedTonesBuffer;
import com.raytheon.uf.common.bmh.tones.ToneGenerationException;
import com.raytheon.uf.common.time.util.ITimer;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.edex.bmh.msg.logging.DefaultMessageLogger;
import com.raytheon.uf.edex.bmh.msg.logging.ErrorActivity.BMH_ACTIVITY;
import com.raytheon.uf.edex.bmh.msg.logging.ErrorActivity.BMH_COMPONENT;

/**
 * Allows for the asynchronous retrieval of audio data. Will adjust the audio
 * data according to a specified amplitude after successfully retrieving it.
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
 * Oct 2, 2014  3642       bkowal      Updated to use the audio buffer abstraction and to
 *                                     populate a {@link TimeMsgCache} for dynamic messages.
 * Oct 17, 2014 3655       bkowal      Move tones to common.
 * Oct 23, 2014 3748       bkowal      AudioRetrievalException is now in common
 * Nov 03, 2014 3781       dgilling    Allow alert tone without SAME tones.
 * Dec 11, 2014 3651       bkowal      Updates to {@link AbstractAudioFileBuffer}.
 * Dec 12, 2014 3603       bsteffen    Updates to TonesGenerator.
 * Jan 05, 2015 3651       bkowal      Use {@link DefaultMessageLogger} to log msg errors.
 * May 13, 2015 4429       rferrel     Changes to {@link DefaultMessageLogger} for traceId.
 * Jun 29, 2015 4602       bkowal      Provide the buffer when notifying listeners that
 *                                     the audio retrieval attempt is complete.
 * Jul 08, 2015 4636       bkowal      Support same and alert decibel levels.
 * Nov 04, 2015 5068       rjpeter     Switch audio units from dB to amplitude.
 * Mar 08, 2016 5382       bkowal      Check for null message data.
 * Apr 26, 2016 5561       bkowal      Retry at least once if broadcast audio initialization 
 *                                     fails.
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class RetrieveAudioJob extends AbstractAudioJob<IAudioFileBuffer> {
    private static final long RANDOM_INIT_RETRY_MS = 20L;

    private IAudioJobListener listener;

    private String taskId;

    /**
     * Constructor
     * 
     * @param priority
     *            the priority; the higher the number, the higher the associated
     *            priority will be.
     * @param audioAmplitude
     *            the target audio amplitude. No portion of the audio should
     *            exceed the target.
     * @param sameAmplitude
     *            the target audio amplitude for SAME Tones.
     * @param alertAmplitude
     *            the target audio amplitude for Alert Tones.
     * @param message
     *            identifying information
     */
    public RetrieveAudioJob(final int priority, final short audioAmplitude,
            final short sameAmplitude, final short alertAmplitude,
            final DacPlaylistMessage message) {
        super(priority, audioAmplitude, sameAmplitude, alertAmplitude, message);
    }

    /**
     * Constructor
     * 
     * @param priority
     *            the priority; the higher the number, the higher the associated
     *            priority will be.
     * @param audioAmplitude
     *            the target audio amplitude for voice. No portion of the audio
     *            should exceed the target.
     * @param sameAmplitude
     *            the target audio amplitude for SAME Tones.
     * @param alertAmplitude
     *            the target audio amplitude for Alert Tones.
     * @param message
     *            identifying information
     * @param listener
     *            a class to notify when the audio retrieval has finished
     *            (optional)
     * @param taskId
     *            identifier associated with a retrieval job for tracking
     *            purposes (optional)
     */
    public RetrieveAudioJob(final int priority, final short audioAmplitude,
            final short sameAmplitude, final short alertAmplitude,
            final DacPlaylistMessage message, final IAudioJobListener listener,
            final String taskId) {
        this(priority, audioAmplitude, sameAmplitude, alertAmplitude, message);
        this.listener = listener;
        this.taskId = taskId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.concurrent.Callable#call()
     */
    @Override
    public IAudioFileBuffer call() throws Exception {
        if (this.message == null) {
            return null;
        }

        ITimer timer = TimeUtil.getTimer();
        timer.start();

        IAudioFileBuffer buffer = null;
        boolean retry = true;
        try {
            buffer = initAudioBuffer();
            retry = false;
        } catch (Exception e) {
            final String msg = "Failed to initialize audio buffer for message: "
                    + message.getBroadcastId() + ". Retrying ...";
            logger.warn(msg, e);
            try {
                Thread.sleep(RANDOM_INIT_RETRY_MS);
            } catch (InterruptedException e1) {
                // Ignore
            }
        }

        if (retry) {
            try {
                buffer = initAudioBuffer();
            } catch (Exception e) {
                this.notifyAttemptComplete(null);
                throw e;
            }
        }

        timer.stop();
        logger.info("Successfully retrieved audio for message: "
                + message.getBroadcastId() + " in "
                + TimeUtil.prettyDuration(timer.getElapsedTime()) + ".");
        this.notifyAttemptComplete(buffer);

        return buffer;
    }

    private IAudioFileBuffer initAudioBuffer() throws IOException,
            AudioRetrievalException {
        List<byte[]> rawDataArrays = new ArrayList<>(this.message
                .getSoundFiles().size());
        Map<Integer, TIME_MSG_TOKENS> dynamicAudioPositionMap = new LinkedHashMap<>(
                rawDataArrays.size());
        TimeMsgCache timeCache = null;
        IAudioFileBuffer buffer = null;
        boolean dynamicMsg = false;

        int concatenatedSize = 0;

        for (int i = 0; i < this.message.getSoundFiles().size(); i++) {
            Path filePath = Paths.get(this.message.getSoundFiles().get(i));

            if (Files.isDirectory(filePath)) {
                dynamicMsg = true;
                rawDataArrays.add(null);
                if (timeCache == null) {
                    timeCache = new TimeMsgCache();
                }
                TIME_MSG_TOKENS token = timeCache.loadCache(filePath);
                /*
                 * This audio is dynamic. Use the tokens to keep track of the
                 * order of the dynamic audio blocks.
                 */
                dynamicAudioPositionMap.put(i, token);
                continue;
            }

            try {
                byte[] rawData = Files.readAllBytes(filePath);
                concatenatedSize += rawData.length;
                byte[] regulatedRawData = this.adjustAudio(rawData,
                        "Body (segment " + i + ")", this.audioAmplitude);
                rawDataArrays.add(regulatedRawData);
                /*
                 * Not dynamic data. So, no lookup key.
                 */
                dynamicAudioPositionMap.put(i, null);
            } catch (IOException e) {
                String msg = "Failed to buffer audio file for message: "
                        + message.getBroadcastId() + ", file: " + filePath;
                AudioRetrievalException audioEx = new AudioRetrievalException(
                        msg, e);
                DefaultMessageLogger.getInstance().logError(this.message,
                        BMH_COMPONENT.DAC_TRANSMIT, BMH_ACTIVITY.AUDIO_READ,
                        this.message, audioEx);
                throw audioEx;
            } catch (AudioRetrievalException e) {
                DefaultMessageLogger.getInstance().logError(this.message,
                        BMH_COMPONENT.DAC_TRANSMIT,
                        BMH_ACTIVITY.AUDIO_ALTERATION, this.message, e);
                throw e;
            }
        }

        GeneratedTonesBuffer generatedTones = null;
        if (message.isSAMETones()) {
            try {
                generatedTones = TonesGenerator.getSAMEAlertTones(
                        message.getSAMEtone(), message.isAlertTone(), false);
            } catch (ToneGenerationException e) {
                String msg = "Unable to generate SAME/alert tones for message: "
                        + message.getBroadcastId();
                this.notifyAttemptComplete(null);
                throw new AudioRetrievalException(msg, e);
            }
        } else if (message.isAlertTone()) {
            try {
                generatedTones = TonesGenerator.getOnlyAlertTones();
            } catch (ToneGenerationException e) {
                String msg = "Unable to generate alert tones for message: "
                        + message.getBroadcastId();
                this.notifyAttemptComplete(null);
                throw new AudioRetrievalException(msg, e);
            }
        }

        ByteBuffer endOfMessage = null;
        if (message.isSAMETones()) {
            try {
                endOfMessage = TonesGenerator.getEndOfMessageTones();
            } catch (ToneGenerationException e) {
                String msg = "Unable to generate end of message SAME tones for message: "
                        + message.getBroadcastId();
                throw new AudioRetrievalException(msg, e);
            }
        }

        /* regulate tones (if they exist). */
        if (generatedTones != null) {
            if (generatedTones.getSameTones() != null) {
                byte[] regulatedTones = adjustAudio(
                        generatedTones.getSameTones(), "Same Tones",
                        this.sameAmplitude);
                generatedTones.setSameTones(regulatedTones);
            }
            if (generatedTones.getAlertTones() != null) {
                byte[] regulatedTones = adjustAudio(
                        generatedTones.getAlertTones(), "Alert Tones",
                        this.alertAmplitude);
                generatedTones.setAlertTones(regulatedTones);
            }
        }
        if (endOfMessage != null) {
            byte[] regulatedEndOfMessage = adjustAudio(endOfMessage.array(),
                    "End of Message", this.sameAmplitude);
            endOfMessage = ByteBuffer.wrap(regulatedEndOfMessage);
        }

        if (dynamicMsg == false) {
            byte[] rawData = null;
            if (rawDataArrays.size() == 1) {
                rawData = rawDataArrays.get(0);
            } else {
                ByteBuffer concatenation = ByteBuffer
                        .allocate(concatenatedSize);
                for (byte[] array : rawDataArrays) {
                    concatenation.put(array);
                }
                rawData = concatenation.array();
            }
            buffer = new AudioFileBuffer(this.message, rawData,
                    (generatedTones == null ? null
                            : generatedTones.combineTonesArray()), endOfMessage);
        } else {
            /*
             * Adjust the time cache audio.
             */
            ITimer timeCacheAdjustTimer = TimeUtil.getTimer();
            timeCacheAdjustTimer.start();
            for (Table.Cell<String, String, byte[]> timeCell : timeCache
                    .getTimeCache().cellSet()) {
                final String rowKey = timeCell.getRowKey();
                final String columnKey = timeCell.getColumnKey();
                timeCache.getTimeCache().put(
                        rowKey,
                        columnKey,
                        super.adjustAudio(timeCell.getValue(), "Time Cache: { "
                                + rowKey + ", " + columnKey + " }",
                                this.audioAmplitude));
            }
            timeCacheAdjustTimer.stop();
            logger.info("Successfully finished audio attenuation/amplification in "
                    + TimeUtil.prettyDuration(timeCacheAdjustTimer
                            .getElapsedTime()) + " for all time cached audio.");
            buffer = new DynamicTimeAudioFileBuffer(this.message,
                    (generatedTones == null ? null : generatedTones
                            .combineTonesArray()),
                    rawDataArrays, dynamicAudioPositionMap, endOfMessage,
                    timeCache);
        }

        return buffer;
    }

    private void notifyAttemptComplete(final IAudioFileBuffer buffer) {
        if (this.listener != null && this.taskId != null) {
            this.listener.audioRetrievalFinished(this.taskId, this.message,
                    buffer);
        }
    }
}