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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.raytheon.uf.common.bmh.TIME_MSG_TOKENS;
import com.raytheon.uf.common.bmh.datamodel.language.Language;
import com.raytheon.uf.common.bmh.datamodel.language.TtsVoice;
import com.raytheon.uf.common.bmh.request.TextToSpeechRequest;
import com.raytheon.uf.common.bmh.schemas.ssml.SSMLConversionException;
import com.raytheon.uf.common.bmh.schemas.ssml.SSMLDocument;
import com.raytheon.uf.common.bmh.schemas.ssml.SayAs;
import com.raytheon.uf.common.time.util.ITimer;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.edex.bmh.BMHConfigurationException;
import com.raytheon.uf.edex.bmh.BMHConstants;
import com.raytheon.uf.edex.bmh.handler.TextToSpeechHandler;
import com.raytheon.uf.edex.bmh.status.BMHStatusHandler;
import com.raytheon.uf.edex.bmh.status.IBMHStatusHandler;
import com.raytheon.uf.edex.bmh.tts.TTSSynthesisFactory;

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
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class TimeMessagesGenerator {

    private static final IBMHStatusHandler statusHandler = BMHStatusHandler
            .getInstance(TimeMessagesGenerator.class);

    private final TextToSpeechHandler ttsHandler;

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
    private static final int MAX_HOUR = 12;

    private static final int MAX_MINUTE = 59;

    /* http://en.wikipedia.org/wiki/12-hour_clock */
    private static final String[] TIME_PERIODS = { "AM", "PM" };

    /* SSML Properties */
    private static final String SAY_AS_INTERPRET_AS = "time";

    private static final String SAY_AS_FORMAT = "hms12";

    private static final String SAY_AS_CHARACTERS = "characters";

    private static final String TIME_COLON = ":";

    // 1 - 12
    private Map<Integer, String> ssmlHourMap = new HashMap<>(MAX_HOUR);

    // 1 - 59 (no 0 minute because that corresponds to o'clock which is ignored)
    private Map<Integer, String> ssmlMinuteMap = new HashMap<>(MAX_MINUTE);

    private Map<String, String> ssmlPeriodMap = new HashMap<>(
            TIME_PERIODS.length);

    private Path audioTimePath;

    public TimeMessagesGenerator(final TextToSpeechHandler ttsHandler,
            final String bmhDataDirectory) {
        this.ttsHandler = ttsHandler;
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
        statusHandler.info("Initializing the SSML time maps ...");
        /*
         * Prepare the SSML that may be needed to generate the audio.
         */
        // No 0 hour in 12-hour time; 00:00 -> 12:00 AM
        for (int hh = 1; hh <= MAX_HOUR; hh++) {
            String timeSSML;
            try {
                /*
                 * hours are encoded as numbers to ensure that o'clock is
                 * excluded.
                 */
                timeSSML = this.constructSSML(Integer.toString(hh));
            } catch (SSMLConversionException e) {
                throw new BMHConfigurationException(
                        "Failed to create the time SSML for hour: " + hh + "!",
                        e);
            }
            this.ssmlHourMap.put(hh, timeSSML);
        }
        for (int mm = 1; mm <= MAX_MINUTE; mm++) {
            String timeSSML;
            try {
                String timeStr = TIME_COLON
                        + StringUtils.leftPad(Integer.toString(mm), 2, "0");
                timeSSML = this.constructSSMLSayAs(timeStr);
            } catch (SSMLConversionException e) {
                throw new BMHConfigurationException(
                        "Failed to create the time SSML for minute: " + mm
                                + "!");
            }
            this.ssmlMinuteMap.put(mm, timeSSML);
        }
        for (String timePeriod : TIME_PERIODS) {
            String timeSSML;
            try {
                timeSSML = this.constructSSMLSayAs(timePeriod);
            } catch (SSMLConversionException e) {
                throw new BMHConfigurationException(
                        "Failed to create the time SSML for period: "
                                + timePeriod + "!");
            }
            this.ssmlPeriodMap.put(timePeriod, timeSSML);
        }

        statusHandler.info("Successfully initialized the SSML Time Maps.");
        statusHandler.info("Initialization Successful!");
    }

    private String constructSSMLSayAs(final String text)
            throws SSMLConversionException {
        SSMLDocument ssmlDocument = new SSMLDocument(Language.ENGLISH);
        SayAs sayAsTag = ssmlDocument.getFactory().createSayAs();
        sayAsTag.setInterpretAs(SAY_AS_INTERPRET_AS);
        sayAsTag.setFormat(SAY_AS_FORMAT);
        sayAsTag.setContent(text);
        ssmlDocument.getRootTag().getContent().add(sayAsTag);

        return ssmlDocument.toSSML();
    }

    private String constructSSML(final String text)
            throws SSMLConversionException {
        SSMLDocument ssmlDocument = new SSMLDocument(Language.ENGLISH);
        ssmlDocument.getRootTag().getContent().add(text);

        return ssmlDocument.toSSML();
    }

    private boolean generateStaticTimeMsg(final String ssml,
            final Path timeFilePath, final TtsVoice voice)
            throws StaticGenerationException {
        ITimer fileTimer = TimeUtil.getTimer();
        fileTimer.start();
        if (Files.exists(timeFilePath)) {
            return false;
        }

        TextToSpeechRequest request = new TextToSpeechRequest();
        request.setVoice(voice.getVoiceNumber());
        request.setPhoneme(ssml);
        request.setTimeout(TTSSynthesisFactory.NO_TIMEOUT);
        try {
            request = (TextToSpeechRequest) this.ttsHandler
                    .handleRequest(request);
        } catch (Exception e) {
            throw new StaticGenerationException(
                    "Failed to generate a static time message: "
                            + timeFilePath.toString() + " with voice "
                            + voice.getVoiceNumber() + "!", e);
        }
        if (request.getByteData() == null) {
            throw new StaticGenerationException(
                    "Failed to generate a static time message: "
                            + timeFilePath.toString() + " with voice "
                            + voice.getVoiceNumber() + " due to TTS Error "
                            + request.getStatus() + "!");
        }
        try {
            Files.write(timeFilePath, request.getByteData());
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

    public void process(TtsVoice ttsVoice, final String timezoneDaylight,
            final String timezoneNoDaylight) throws StaticGenerationException {
        ITimer overallTimer = TimeUtil.getTimer();
        overallTimer.start();

        /** VERIFY DIRECTORY EXISTENCE */
        Path timeHourDirPath = getTimeVoiceHourDirectory(ttsVoice);
        Path timeMinuteDirPath = getTimeVoiceMinuteDirectory(ttsVoice);
        Path timePeriodDirPath = getTimeVoicePeriodDirectory(ttsVoice);
        Path timeZoneDirPath = getTimeVoiceTZDirectory(ttsVoice);
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
        /** HOURS **/
        Iterator<Integer> hoursKeyIterator = this.ssmlHourMap.keySet()
                .iterator();
        while (hoursKeyIterator.hasNext()) {
            int hh = hoursKeyIterator.next();
            if (this.generateStaticTimeMsg(this.ssmlHourMap.get(hh),
                    getTimeVoiceHourFilePath(ttsVoice, hh), ttsVoice)) {
                ++totalFilesWritten;
            }
        }
        /** MINUTES **/
        Iterator<Integer> minutesKeyIterator = this.ssmlMinuteMap.keySet()
                .iterator();
        while (minutesKeyIterator.hasNext()) {
            int mm = minutesKeyIterator.next();
            if (this.generateStaticTimeMsg(this.ssmlMinuteMap.get(mm),
                    getTimeVoiceMinuteFilePath(ttsVoice, mm), ttsVoice)) {
                ++totalFilesWritten;
            }
        }
        /** PERIODS **/
        for (String timePeriod : TIME_PERIODS) {
            if (this.generateStaticTimeMsg(this.ssmlPeriodMap.get(timePeriod),
                    getTimePeriodFilePath(ttsVoice, timePeriod), ttsVoice)) {
                ++totalFilesWritten;
            }
        }

        List<String> timezones = new ArrayList<>(2);
        timezones.add(timezoneDaylight);
        timezones.add(timezoneNoDaylight);

        /*** TIMEZONE FILE ***/
        for (String timezone : timezones) {
            if (timezone == null) {
                continue;
            }
            Path tzFilePath = getTimeVoiceTZFilePath(ttsVoice, timezone);
            if (Files.exists(tzFilePath) == false) {
                /*
                 * Generate the timezone audio.
                 */
                SSMLDocument ssmlDocument = new SSMLDocument(Language.ENGLISH);
                SayAs sayAsTag = ssmlDocument.getFactory().createSayAs();
                sayAsTag.setInterpretAs(SAY_AS_CHARACTERS);
                sayAsTag.setContent(timezone);
                ssmlDocument.getRootTag().getContent().add(sayAsTag);

                String tzSSML = null;
                try {
                    tzSSML = ssmlDocument.toSSML();
                } catch (SSMLConversionException e) {
                    throw new StaticGenerationException(
                            "Failed to create the timezone ssml for timezone "
                                    + timezone + "!", e);
                }

                if (this.generateStaticTimeMsg(tzSSML, tzFilePath, ttsVoice)) {
                    ++totalFilesWritten;
                }
            }
        }

        overallTimer.stop();
        if (totalFilesWritten > 0) {
            statusHandler.info("Successfully generated " + totalFilesWritten
                    + " time audio files for voice "
                    + ttsVoice.getVoiceNumber() + " in "
                    + TimeUtil.prettyDuration(overallTimer.getElapsedTime())
                    + ".");
        } else {
            statusHandler
                    .info("Successfully verified the existence of time audio files for voice "
                            + ttsVoice.getVoiceNumber()
                            + " in "
                            + TimeUtil.prettyDuration(overallTimer
                                    .getElapsedTime()) + ".");
        }
    }

    public Path getTimeVoiceDirectory(final TtsVoice voice) {
        return Paths.get(audioTimePath.toString(),
                Integer.toString(voice.getVoiceNumber()));
    }

    public Path getTimeVoiceHourDirectory(final TtsVoice voice) {
        return Paths.get(getTimeVoiceDirectory(voice).toString(),
                TIME_HOUR_DIRECTORY);
    }

    public Path getTimeVoiceMinuteDirectory(final TtsVoice voice) {
        return Paths.get(getTimeVoiceDirectory(voice).toString(),
                TIME_MINUTE_DIRECTORY);
    }

    public Path getTimeVoicePeriodDirectory(final TtsVoice voice) {
        return Paths.get(getTimeVoiceDirectory(voice).toString(),
                TIME_PERIOD_DIRECTORY);
    }

    public Path getTimeVoiceTZDirectory(final TtsVoice voice) {
        return Paths.get(getTimeVoiceDirectory(voice).toString(),
                TIME_ZONE_DIRECTORY);
    }

    public Path getTimeVoiceHourFilePath(final TtsVoice voice, final int hour) {
        return Paths.get(getTimeVoiceHourDirectory(voice).toString(),
                Integer.toString(hour));
    }

    public Path getTimeVoiceMinuteFilePath(final TtsVoice voice,
            final int minute) {
        return Paths.get(getTimeVoiceMinuteDirectory(voice).toString(),
                Integer.toString(minute));
    }

    public Path getTimePeriodFilePath(final TtsVoice voice, final String period) {
        return Paths.get(getTimeVoicePeriodDirectory(voice).toString(), period);
    }

    public Path getTimeVoiceTZFilePath(final TtsVoice voice,
            final String timezone) {
        return Paths.get(getTimeVoiceTZDirectory(voice).toString(), timezone);
    }
}