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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Table;
import com.raytheon.uf.common.bmh.TIME_MSG_TOKENS;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessage;
import com.raytheon.uf.common.time.util.ITimer;
import com.raytheon.uf.common.time.util.TimeUtil;
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
 * Oct 2, 2014  3642       bkowal      Updated to use the audio buffer abstraction and to
 *                                     populate a {@link TimeMsgCache} for dynamic messages.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class RetrieveAudioJob extends AbstractAudioJob<IAudioFileBuffer> {
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
        super(priority, dbTarget, message);
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
    public IAudioFileBuffer call() throws Exception {
        final long start = System.currentTimeMillis();
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
                byte[] regulatedRawData = this.adjustAudio(rawData,
                        "Body (segment " + i + ")");
                rawDataArrays.add(regulatedRawData);
                /*
                 * Not dynamic data. So, no lookup key.
                 */
                dynamicAudioPositionMap.put(i, null);
            } catch (IOException e) {
                String msg = "Failed to buffer audio file for message: "
                        + message.getBroadcastId() + ", file: " + filePath;
                this.notifyAttemptComplete();
                throw new AudioRetrievalException(msg, e);
            } catch (AudioRetrievalException e) {
                this.notifyAttemptComplete();
                throw e;
            }
        }

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
            try {
                byte[] regulatedTones = adjustAudio(tones.array(),
                        "SAME/Alert Tones");
                tones = ByteBuffer.wrap(regulatedTones);
            } catch (AudioRetrievalException e) {
                this.notifyAttemptComplete();
                throw e;
            }
        }
        if (endOfMessage != null) {
            try {
                byte[] regulatedEndOfMessage = adjustAudio(
                        endOfMessage.array(), "End of Message");
                endOfMessage = ByteBuffer.wrap(regulatedEndOfMessage);
            } catch (AudioRetrievalException e) {
                this.notifyAttemptComplete();
                throw e;
            }
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
            buffer = new AudioFileBuffer(rawData, tones, endOfMessage);
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
                                + rowKey + ", " + columnKey + " }"));
            }
            timeCacheAdjustTimer.stop();
            logger.info("Successfully finished audio attenuation/amplification in "
                    + TimeUtil.prettyDuration(timeCacheAdjustTimer
                            .getElapsedTime()) + " for all time cached audio.");
            buffer = new DynamicTimeAudioFileBuffer(tones, rawDataArrays,
                    dynamicAudioPositionMap, endOfMessage, timeCache);
        }

        logger.info("Successfully retrieved audio for message: "
                + message.getBroadcastId() + " in "
                + TimeUtil.prettyDuration(System.currentTimeMillis() - start)
                + ".");
        this.notifyAttemptComplete();

        return buffer;
    }

    private void notifyAttemptComplete() {
        if (this.listener != null && this.taskId != null) {
            this.listener.audioRetrievalFinished(this.taskId, this.message);
        }
    }
}