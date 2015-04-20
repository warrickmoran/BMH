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
package com.raytheon.uf.edex.bmh.staticmsg;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.raytheon.uf.common.bmh.TIME_MSG_TOKENS;
import com.raytheon.uf.common.bmh.datamodel.language.Language;
import com.raytheon.uf.common.bmh.datamodel.language.TtsVoice;
import com.raytheon.uf.common.bmh.datamodel.transmitter.BMHTimeZone;
import com.raytheon.uf.common.bmh.schemas.ssml.SSMLDocument;
import com.raytheon.uf.common.bmh.schemas.ssml.SpeechRateFormatter;
import com.raytheon.uf.common.time.util.ITimer;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.edex.bmh.BMHConfigurationException;
import com.raytheon.uf.edex.bmh.BMHConstants;
import com.raytheon.uf.edex.bmh.status.BMHStatusHandler;
import com.raytheon.uf.edex.bmh.status.IBMHStatusHandler;
import com.raytheon.uf.edex.bmh.tts.TTSManager;
import com.raytheon.uf.edex.bmh.tts.TTSReturn;

/**
 * Pre-synthesizes time messages in segments for a specifc {@link TtsVoice} and
 * time zones. The time messages are created for use in static message
 * transmission.
 * 
 * This class could be updated to use a thread pool. However, the synthesis of a
 * single time takes <= 5 ms.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 30, 2014 3642       bkowal      Initial creation
 * Nov 3, 2014  3759       bkowal      Practice Mode Support.
 * Nov 5, 2014  3630       bkowal      Use constants audio directory. BMH Data is
 *                                     now set in the constructor.
 * Feb 24, 2015    4157    bkowal      Specify a {@link Language} for the {@link SSMLDocument}.
 * Mar 27, 2015 4314       bkowal      Time messages are now generated based on {@link Language}
 *                                     and rate of speech.
 * Apr 20, 2015 4314       bkowal      Use {@link TTSManager} synthesis to verify that the
 *                                     retry logic will be utilized.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class TimeMessagesGenerator {

    private static final IBMHStatusHandler statusHandler = BMHStatusHandler
            .getInstance(TimeMessagesGenerator.class);

    private final TTSManager ttsManager;

    /* Output root subdirectories */
    private final String bmhDataDirectory;

    private static final String TIME_DATA_DIRECTORY = "time";

    private static final String TIME_HOUR_DIRECTORY = TIME_MSG_TOKENS.HOUR
            .getIdentifier();

    private static final String TIME_MINUTE_DIRECTORY = TIME_MSG_TOKENS.MINUTE
            .getIdentifier();

    private static final String TIME_PERIOD_DIRECTORY = TIME_MSG_TOKENS.PERIOD
            .getIdentifier();

    private static final String TIME_ZONE_DIRECTORY = TIME_MSG_TOKENS.TIME_ZONE
            .getIdentifier();

    /* Time properties */
    public static final int MAX_HOUR = 12;

    public static final int MAX_MINUTE = 59;

    /* http://en.wikipedia.org/wiki/12-hour_clock */
    public static final String[] TIME_PERIODS = { "AM", "PM" };

    private Map<Language, SSMLTimeCache> ssmlLanguageTimeCacheMap = new HashMap<>(
            Language.values().length, 1.0f);

    private Path audioTimePath;

    public TimeMessagesGenerator(final TTSManager ttsManager,
            final String bmhDataDirectory) {
        this.ttsManager = ttsManager;
        this.bmhDataDirectory = bmhDataDirectory;
    }

    public void initialize() throws BMHConfigurationException {
        statusHandler.info("Initializing the Time Messages Generator ...");
        audioTimePath = Paths.get(this.bmhDataDirectory,
                BMHConstants.AUDIO_DATA_DIRECTORY, TIME_DATA_DIRECTORY);
        /*
         * Determine if the location exists.
         */
        if (Files.exists(audioTimePath) == false) {
            try {
                Files.createDirectories(audioTimePath);
            } catch (IOException e) {
                throw new BMHConfigurationException(
                        "Failed to create the root audio time directory for pre-synthesized time messages: "
                                + audioTimePath.toString(), e);
            }
        }

        statusHandler.info("BMH Audio Time Directory is: " + audioTimePath);
        statusHandler.info("Initializing the SSML time cache(s) ...");

        /**
         * Generate a {@link SSMLTimeCache} for every {@link Language}.
         */
        for (Language language : Language.values()) {
            this.ssmlLanguageTimeCacheMap.put(language, new SSMLTimeCache(
                    language));
            statusHandler
                    .info("Successfully initialized ssml time cache for language: "
                            + language.name() + ".");
        }

        statusHandler.info("Initialization Successful!");
    }

    private boolean generateStaticTimeMsg(final String ssml,
            final Path timeFilePath, final TtsVoice voice,
            final String logIdentifier) throws StaticGenerationException {
        ITimer fileTimer = TimeUtil.getTimer();
        fileTimer.start();
        if (Files.exists(timeFilePath)) {
            return false;
        }

        TTSReturn ttsReturn = this.ttsManager.attemptAudioSynthesis(ssml,
                voice.getVoiceNumber(), logIdentifier);
        if (ttsReturn.isIoFailed()) {
            throw new StaticGenerationException(
                    "Failed to generate a static time message: "
                            + timeFilePath.toString() + " with voice "
                            + voice.getVoiceNumber() + "!",
                    ttsReturn.getIoFailureCause());
        }
        if (ttsReturn.isSynthesisSuccess() == false
                || ttsReturn.getVoiceData() == null) {
            throw new StaticGenerationException(
                    "Failed to generate a static time message: "
                            + timeFilePath.toString() + " with voice "
                            + voice.getVoiceNumber() + " due to TTS Error "
                            + ttsReturn.getReturnValue().getDescription() + "!");
        }
        try {
            Files.write(timeFilePath, ttsReturn.getVoiceData());
        } catch (IOException e) {
            throw new StaticGenerationException(
                    "Failed to write static time message: "
                            + timeFilePath.toString() + "!", e);
        }
        fileTimer.stop();
        statusHandler.info("Successfully generated and wrote time audio file: "
                + timeFilePath.toString() + " in "
                + TimeUtil.prettyDuration(fileTimer.getElapsedTime()) + ".");
        return true;
    }

    public void process(final TtsVoice ttsVoice, final BMHTimeZone timeZone,
            final int speechRate) throws StaticGenerationException {
        ITimer overallTimer = TimeUtil.getTimer();
        overallTimer.start();

        /** VERIFY DIRECTORY EXISTENCE */
        Path timeHourDirPath = getTimeVoiceHourDirectory(ttsVoice, speechRate);
        Path timeMinuteDirPath = getTimeVoiceMinuteDirectory(ttsVoice,
                speechRate);
        Path timePeriodDirPath = getTimeVoicePeriodDirectory(ttsVoice,
                speechRate);
        Path timeZoneDirPath = getTimeVoiceTZDirectory(ttsVoice, speechRate);
        final Path[] timeDirectoriesToVerify = new Path[] { timeHourDirPath,
                timeMinuteDirPath, timePeriodDirPath, timeZoneDirPath };
        for (Path timeDirPath : timeDirectoriesToVerify) {
            if (Files.exists(timeDirPath)) {
                continue;
            }
            try {
                Files.createDirectories(timeDirPath);
            } catch (IOException e) {
                throw new StaticGenerationException(
                        "Failed to create time directory: "
                                + timeDirPath.toString() + " for voice "
                                + ttsVoice.getVoiceNumber() + "!");
            }
        }

        int totalFilesWritten = 0;
        final SSMLTimeCache cache = this.ssmlLanguageTimeCacheMap.get(ttsVoice
                .getLanguage());
        if (cache == null) {
            throw new StaticGenerationException(
                    "Unable to find a ssml time cache for language "
                            + ttsVoice.getLanguage().name() + ".");
        }

        Iterator<Integer> hoursIterator = cache
                .getHourIteratorForSpeechRate(speechRate);
        while (hoursIterator.hasNext()) {
            int hh = hoursIterator.next();
            final String logIdentifier = ttsVoice.getLanguage().name()
                    + " Hour " + hh + " (Speech Rate: " + speechRate + ")";
            if (this.generateStaticTimeMsg(cache.getHourSSML(speechRate, hh),
                    this.getTimeVoiceHourFilePath(ttsVoice, speechRate, hh),
                    ttsVoice, logIdentifier)) {
                ++totalFilesWritten;
            }
        }

        Iterator<Integer> minutesIterator = cache
                .getMinuteIteratorForSpeechRate(speechRate);
        while (minutesIterator.hasNext()) {
            int mm = minutesIterator.next();
            final String logIdentifier = ttsVoice.getLanguage().name()
                    + " Minute " + mm + " (Speech Rate: " + speechRate + ")";
            if (this.generateStaticTimeMsg(cache.getMinuteSSML(speechRate, mm),
                    this.getTimeVoiceMinuteFilePath(ttsVoice, speechRate, mm),
                    ttsVoice, logIdentifier)) {
                ++totalFilesWritten;
            }
        }

        for (String period : TIME_PERIODS) {
            final String logIdentifier = ttsVoice.getLanguage().name()
                    + " Period " + period + " (Speech Rate: " + speechRate
                    + ")";
            if (this.generateStaticTimeMsg(
                    cache.getPeriodSSML(speechRate, period),
                    this.getTimePeriodFilePath(ttsVoice, speechRate, period),
                    ttsVoice, logIdentifier)) {
                ++totalFilesWritten;
            }
        }

        Set<String> timezones = new HashSet<>(2, 1.0f);
        timezones.add(timeZone.getShortDisplayName(true));
        timezones.add(timeZone.getShortDisplayName(false));
        Iterator<String> timezoneIterator = timezones.iterator();
        while (timezoneIterator.hasNext()) {
            final String timezone = timezoneIterator.next();
            final String logIdentifier = ttsVoice.getLanguage().name()
                    + " Timezone " + timezone + " (Speech Rate: " + speechRate
                    + ")";
            if (this.generateStaticTimeMsg(
                    cache.getTimezoneSSML(speechRate, timezone),
                    this.getTimeVoiceTZFilePath(ttsVoice, speechRate, timezone),
                    ttsVoice, logIdentifier)) {
                ++totalFilesWritten;
            }
        }

        overallTimer.stop();
        if (totalFilesWritten > 0) {
            statusHandler.info("Successfully generated " + totalFilesWritten
                    + " time audio files for voice "
                    + ttsVoice.getVoiceNumber() + " and speech rate "
                    + SpeechRateFormatter.formatSpeechRate(speechRate) + " in "
                    + TimeUtil.prettyDuration(overallTimer.getElapsedTime())
                    + ".");
        } else {
            statusHandler
                    .info("Successfully verified the existence of time audio files for voice "
                            + ttsVoice.getVoiceNumber()
                            + " and speech rate "
                            + SpeechRateFormatter.formatSpeechRate(speechRate)
                            + " in "
                            + TimeUtil.prettyDuration(overallTimer
                                    .getElapsedTime()) + ".");
        }
    }

    public Path getTimeVoiceDirectory(final TtsVoice voice, final int speechRate) {
        return Paths.get(audioTimePath.toString(),
                Integer.toString(voice.getVoiceNumber())).resolve(
                this.speechRateToDirectoryName(speechRate));
    }

    public Path getTimeVoiceHourDirectory(final TtsVoice voice,
            final int speechRate) {
        return Paths.get(getTimeVoiceDirectory(voice, speechRate).toString(),
                TIME_HOUR_DIRECTORY);
    }

    public Path getTimeVoiceMinuteDirectory(final TtsVoice voice,
            final int speechRate) {
        return Paths.get(getTimeVoiceDirectory(voice, speechRate).toString(),
                TIME_MINUTE_DIRECTORY);
    }

    public Path getTimeVoicePeriodDirectory(final TtsVoice voice,
            final int speechRate) {
        return Paths.get(getTimeVoiceDirectory(voice, speechRate).toString(),
                TIME_PERIOD_DIRECTORY);
    }

    public Path getTimeVoiceTZDirectory(final TtsVoice voice,
            final int speechRate) {
        return Paths.get(getTimeVoiceDirectory(voice, speechRate).toString(),
                TIME_ZONE_DIRECTORY);
    }

    public Path getTimeVoiceHourFilePath(final TtsVoice voice,
            final int speechRate, final int hour) {
        return Paths.get(getTimeVoiceHourDirectory(voice, speechRate)
                .toString(), Integer.toString(hour));
    }

    public Path getTimeVoiceMinuteFilePath(final TtsVoice voice,
            final int speechRate, final int minute) {
        return Paths.get(getTimeVoiceMinuteDirectory(voice, speechRate)
                .toString(), Integer.toString(minute));
    }

    public Path getTimePeriodFilePath(final TtsVoice voice,
            final int speechRate, final String period) {
        return Paths.get(getTimeVoicePeriodDirectory(voice, speechRate)
                .toString(), period);
    }

    public Path getTimeVoiceTZFilePath(final TtsVoice voice,
            final int speechRate, final String timezone) {
        return Paths.get(getTimeVoiceTZDirectory(voice, speechRate).toString(),
                timezone);
    }

    public String speechRateToDirectoryName(final int speechRate) {
        StringBuilder sb = new StringBuilder();
        if (speechRate < 0) {
            sb.append("negative");
        } else {
            sb.append("positive");
        }
        sb.append(Math.abs(speechRate));

        return sb.toString();
    }
}