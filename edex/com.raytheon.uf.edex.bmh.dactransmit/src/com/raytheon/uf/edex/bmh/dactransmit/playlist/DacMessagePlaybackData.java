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
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Calendar;

import javax.xml.bind.JAXB;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raytheon.uf.common.bmh.dac.dacsession.DacSessionConstants;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessage;
import com.raytheon.uf.common.bmh.notify.MessagePlaybackStatusNotification;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.edex.bmh.dactransmit.dacsession.DataTransmitConstants;

/**
 * All the necessary data needed by the {@code DataTransmitThread} to play a
 * file, including the raw audio data itself.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 24, 2014  #3286     dgilling     Initial creation
 * Jul 25, 2014  #3286     dgilling     Add additional methods needed for
 *                                      sending playback updates to 
 *                                      CommsManager.
 * Aug 08, 2014  #3286     dgilling     Add resetAudio().
 * Aug 13, 2014  #3286     dgilling     Send status for tones playback.
 * Oct 01, 2014  #3485     bsteffen     Add ability to resume.
 * Oct 17, 2014  #3655     bkowal       Move tones to common.
 * Oct 30, 2014  #3617     dgilling     Support tone blackout.
 * Nov 03, 2014  #3781     dgilling     Allow alert tones to be played
 *                                      independently from SAME tones.
 * Jan 05, 2015  #3913     bsteffen     Handle future replacements.
 * Jan 09, 2015  #3942     rjpeter      Added flag to control use of positionStream.
 * 
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

public final class DacMessagePlaybackData {

    private static final boolean USE_POSITION_STREAM = Boolean
            .getBoolean("usePositionStream");

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private DacPlaylistMessage message;

    private AudioFileBuffer audio;

    private boolean interrupt;

    private boolean firstCallToGet = true;

    private boolean resume = false;

    /**
     * The stream to write position information for resume if this process dies
     * unexpectedly. The current mechanism writes a single byte to the stream
     * for each packet sent. On resume the index in the audio stream can be
     * easily calculated. This method should be atomic and allows for no
     * possibility of a corrupt or incomplete state. When running multiple dac
     * transmits there is a possibility this will become too IO intensive in
     * which case a less intense resume method is needed or the concept of
     * resuming mid-stream may need to be abandoned.
     */
    private OutputStream positionStream = null;

    /**
     * Allow this playback to resume where a previous incarnation has left off.
     * Calling this method does not guarantee the data will resume, this will
     * only resume if a valid position file is found that is not too old and was
     * not positioned within tones.
     * 
     */
    public void allowResume() {
        resume = true;
    }

    /**
     * Determine if it is possible to resume and position the stream
     * accordingly. This method must be called during the first call to get().
     * If the current position file is too old, or if it is positioned within
     * tones then the message starts over.
     * 
     * @return true if the resume was successful.
     */
    private boolean resumePlayback() {
        this.resume = false;
        Path positionFile = message.getPositionPath();
        if (Files.exists(positionFile)) {
            int position = 0;
            try {
                BasicFileAttributes attrs = Files.readAttributes(positionFile,
                        BasicFileAttributes.class);
                long timeOffset = TimeUtil.currentTimeMillis()
                        - attrs.lastModifiedTime().toMillis();
                if (timeOffset < DataTransmitConstants.SYNC_DOWNTIME_RESTART_THRESHOLD) {
                    position = (int) Files.size(positionFile);
                } else {
                    logger.info(
                            "Message restarted because position file is {}ms old",
                            timeOffset);
                }
            } catch (IOException e) {
                logger.error(
                        "Unable to determine position of message {} resetting playback.",
                        message.getBroadcastId(), e);
            }
            if (position > 0) {
                audio.skip(position * DacSessionConstants.SINGLE_PAYLOAD_SIZE);
                if (audio.isInTones()) {
                    audio.rewind();
                    return false;
                } else if (USE_POSITION_STREAM) {
                    try {
                        positionStream = Files.newOutputStream(positionFile,
                                StandardOpenOption.APPEND);
                    } catch (IOException e) {
                        logger.error(
                                "Unable to open position file, position tracking will be disabled.",
                                e);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Should be called on first get.
     */
    private MessagePlaybackStatusNotification firstGet() {
        int playCount = message.getPlayCount() + 1;
        message.setPlayCount(playCount);
        Calendar transmitTime = TimeUtil.newGmtCalendar();
        message.setLastTransmitTime(transmitTime);

        boolean playedSameTone = message.isPlayedSameTone();
        boolean playedAlertTone = message.isPlayedAlertTone();
        if (audio.isReturnTones()) {
            playedSameTone = message.isSAMETones();
            playedAlertTone = message.isAlertTone();
        }
        message.setPlayedAlertTone(playedAlertTone);
        message.setPlayedSameTone(playedSameTone);
        MessagePlaybackStatusNotification playbackStatus = new MessagePlaybackStatusNotification(
                message.getBroadcastId(), transmitTime, playCount,
                playedSameTone, playedAlertTone, null);
        firstCallToGet = false;
        if (!resume || !resumePlayback()) {
            if (USE_POSITION_STREAM) {
                try {
                    positionStream = Files.newOutputStream(
                            message.getPositionPath(),
                            StandardOpenOption.CREATE);
                } catch (IOException e) {
                    logger.error(
                            "Unable to open position file, position tracking will be disabled.",
                            e);
                }
            }
        }
        return playbackStatus;
    }

    /**
     * Called when the message is no longer played to persist the current state
     * and remove the position tracking file.
     */
    public void endPlayback() {
        if (positionStream != null) {
            try {
                positionStream.close();
            } catch (IOException e) {
                logger.error("Unable to close position file.", e);
            }
            try {
                Files.delete(message.getPositionPath());
            } catch (IOException e) {
                logger.error("Unable to delete position file.", e);
            }
        }
        message.setReplaceTime(null);
        /*
         * TODO the notification was sent at the beginning of playing but the
         * state is not persisted until the end.
         */
        Path msgPath = message.getPath();
        Path tmpPath = msgPath.resolveSibling(msgPath.getFileName().toString()
                .replace(".xml", ".tmp.xml"));
        try {
            JAXB.marshal(message, tmpPath.toFile());
            Files.move(tmpPath, msgPath, StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (Throwable e) {
            logger.error("Unable to persist message state.", e);
        }

    }

    public MessagePlaybackStatusNotification get(byte[] dst) {
        MessagePlaybackStatusNotification playbackStatus = null;
        if (firstCallToGet) {
            playbackStatus = firstGet();
        }
        audio.get(dst, 0, dst.length);
        if ((positionStream != null) && audio.hasRemaining()) {
            try {
                /*
                 * TODO make this async or timeout. If the nfs write is failing
                 * we do NOT want to stop all playback.
                 */
                positionStream.write(0);
                positionStream.flush();
            } catch (IOException e) {
                logger.error(
                        "Unable to write position file, position tracking will be disabled.",
                        e);
                try {
                    positionStream.close();
                } catch (IOException e1) {
                    logger.error("Unable to close position file.", e);
                }
                try {
                    Files.delete(message.getPositionPath());
                } catch (IOException e1) {
                    logger.error("Unable to delete position file.", e);
                }
            }
        }
        return playbackStatus;
    }

    public boolean hasRemaining() {
        return audio.hasRemaining();
    }

    public void resetAudio() {
        if (positionStream != null) {
            try {
                positionStream.close();
            } catch (IOException e) {
                logger.error("Unable to close position file.", e);
            }
        }
        if (USE_POSITION_STREAM) {
            try {
                positionStream = Files.newOutputStream(
                        message.getPositionPath(), StandardOpenOption.CREATE);
            } catch (IOException e) {
                logger.error(
                        "Unable to open position file, position tracking will be disabled.",
                        e);
            }
        }
        audio.rewind();
    }

    public DacPlaylistMessage getMessage() {
        return message;
    }

    public AudioFileBuffer getAudio() {
        return audio;
    }

    public boolean isInterrupt() {
        return interrupt;
    }

    public void setMessage(DacPlaylistMessage message) {
        this.message = message;
    }

    public void setAudio(AudioFileBuffer audio) {
        this.audio = audio;
    }

    public void setInterrupt(boolean interrupt) {
        this.interrupt = interrupt;
    }
}
