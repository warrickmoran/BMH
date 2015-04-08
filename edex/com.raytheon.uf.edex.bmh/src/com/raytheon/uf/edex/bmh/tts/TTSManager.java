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
package com.raytheon.uf.edex.bmh.tts;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FilenameUtils;

import com.raytheon.uf.common.bmh.BMHVoice;
import com.raytheon.uf.common.bmh.BMH_CATEGORY;
import com.raytheon.uf.common.bmh.TTSConstants.TTS_FORMAT;
import com.raytheon.uf.common.bmh.TTSConstants.TTS_RETURN_VALUE;
import com.raytheon.uf.common.bmh.TTSSynthesisException;
import com.raytheon.uf.common.bmh.audio.AudioConversionException;
import com.raytheon.uf.common.bmh.audio.BMHAudioFormat;
import com.raytheon.uf.common.bmh.audio.UnsupportedAudioFormatException;
import com.raytheon.uf.common.bmh.datamodel.language.TtsVoice;
import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastFragment;
import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg;
import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsgGroup;
import com.raytheon.uf.common.bmh.notify.status.TTSStatus;
import com.raytheon.uf.common.serialization.SerializationUtil;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.edex.bmh.BMHConfigurationException;
import com.raytheon.uf.edex.bmh.BMHConstants;
import com.raytheon.uf.edex.bmh.audio.EdexAudioConverterManager;
import com.raytheon.uf.edex.bmh.ldad.LdadMsg;
import com.raytheon.uf.edex.bmh.msg.logging.ErrorActivity.BMH_ACTIVITY;
import com.raytheon.uf.edex.bmh.msg.logging.ErrorActivity.BMH_COMPONENT;
import com.raytheon.uf.edex.bmh.msg.logging.IMessageLogger;
import com.raytheon.uf.edex.bmh.status.BMHStatusHandler;
import com.raytheon.uf.edex.bmh.status.IBMHStatusHandler;
import com.raytheon.uf.edex.core.EDEXUtil;
import com.raytheon.uf.edex.core.IContextStateProcessor;

/**
 * The TTS Manager is responsible for interacting with the TTS Server to convert
 * ssml data to audio data. Data that is successfully converted will be written
 * to an audio file in $BMH_DATA.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 2, 2014  3228       bkowal      Initial creation
 * Jun 17, 2014 3291       bkowal      Updated to use the IBMHStatusHandler
 * Jun 24, 2014 3302       bkowal      Updated to use the BroadcastMsg Entity. Eliminated the
 *                                     use of *DataRecord.
 * Jul 1, 2014  3302       bkowal      Fixed log message.
 * Aug 20, 2014 3538       bkowal      Use TTS Connection Pooling. Implement Monitoring.
 *                                     Truly verify TTS Synthesis is available.
 * Aug 25, 2014 3538       bkowal      Update to use the new TTS Synthesis
 *                                     mechanism. Cleanup.
 * Aug 26, 2014 3559       bkowal      Notify the user of a configuration error when the
 *                                     synthesis validation fails due to an improperly
 *                                     set bmh tts nfs directory.
 * Aug 29, 2014 3568       bkowal      Success field in broadcast msg is now set correctly.
 * Sep 12, 2014 3588       bsteffen    Support audio fragments.
 * Sep 29, 2014 3291       bkowal      Cleanup. Do not depend on properties that are not
 *                                     directly used.
 * Oct 2, 2014  3642       bkowal      Updated for compatibility with other changes
 * Oct 21, 2014 3747       bkowal      Set update time manually.
 * Oct 26, 2014 3759       bkowal      Update to support practice mode.
 * Nov 5, 2014  3630       bkowal      Use constants audio directory.
 * Nov 19, 2014 3385       bkowal      Initial ldad implementation.
 * Nov 20, 2014 3817       bsteffen    send status messages.
 * Nov 20, 2014 3385       bkowal      Support synthesis for ldad.
 * Jan 05, 2015 3651       bkowal      Use {@link IMessageLogger} to log message errors.
 * Jan 26, 2015 4020       bkowal      Determine the actual TTS Host when publishing
 *                                     a TTS Status.
 * Jan 27, 2015 4026       bkowal      Updated bmh availability verification.
 * Jan 29, 2015 4060       bkowal      Include fragment id in the name of the generated
 *                                     audio files.
 * Feb 09, 2015 4091       bkowal      Use {@link EdexAudioConverterManager}.
 * Feb 17, 2015 4136       bkowal      Truncate any audio data with duration that is greater
 *                                     than the maximum default or specified duration.
 * Feb 03, 2015 4175       bkowal      Use the required and guaranteed Paul voice for
 *                                     TTS Availability Verification.
 * Mar 25, 2015 4290       bsteffen    Switch to global replacement.
 * Apr 07, 2015 4293       bkowal      Support {@link BroadcastMsg} reuse by adding to the
 *                                     message contents.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class TTSManager implements IContextStateProcessor, Runnable {
    private static final IBMHStatusHandler statusHandler = BMHStatusHandler
            .getInstance(TTSManager.class);

    private static final int CORE_POOL_SIZE = 1;

    private static final ThreadLocal<SimpleDateFormat> TODAY_DATED_DIRECTORY_FORMAT = TimeUtil
            .buildThreadLocalSimpleDateFormat("yyMMdd",
                    TimeZone.getTimeZone("GMT"));

    private static final ThreadLocal<SimpleDateFormat> FILE_NAME_TIME_FORMAT = TimeUtil
            .buildThreadLocalSimpleDateFormat("HHmmss",
                    TimeZone.getTimeZone("GMT"));

    /* For now, just default the format to a MULAW file. */
    private static final TTS_FORMAT TTS_DEFAULT_FORMAT = TTS_FORMAT.TTS_FORMAT_MULAW;

    private static final BMHAudioFormat DEFAULT_OUTPUT_FORMAT = BMHAudioFormat.ULAW;

    private static final String DEFAULT_TTS_FILE_EXTENSION = DEFAULT_OUTPUT_FORMAT
            .getExtension();

    /* Configuration Property Name Constants */
    private static final String TTS_HEARTBEAT = "bmh.tts.heartbeat";

    private static final String TTS_RETRY_THRESHOLD_PROPERTY = "bmh.tts.retry-threshold";

    private static final String TTS_RETRY_DELAY_PROPERTY = "bmh.tts.retry-delay";

    private static final String TTS_DISABLED_PROPERTY = "bmh.tts.disabled";

    private static final int DEFAULT_MAX_AUDIO_DURATION_SECONDS = 600;

    /*
     * property used to override the 10-minute maximum audio length. property
     * value specified in seconds.
     */
    private static final String MAX_AUDIO_DURATION = "bmh.audio.max-duration";

    /* Configuration */
    private String bmhDataDirectory;

    private String bmhStatusDestination;

    private long ttsHeartbeat;

    private final Integer ttsValidationVoice = BMHVoice.PAUL.getId();

    private int ttsRetryThreshold;

    private long ttsRetryDelay;

    private final int maxAudioDuration;

    private final int maxAudioByteCount;

    private final boolean disabled;

    private TTSSynthesisFactory synthesisFactory;

    private ScheduledThreadPoolExecutor heartbeatMonitor;

    private final IMessageLogger messageLogger;

    public TTSManager(final IMessageLogger messageLogger) {
        this.disabled = Boolean.getBoolean(TTS_DISABLED_PROPERTY);
        this.messageLogger = messageLogger;

        this.maxAudioDuration = Integer.getInteger(MAX_AUDIO_DURATION,
                DEFAULT_MAX_AUDIO_DURATION_SECONDS);
        this.maxAudioByteCount = ((this.maxAudioDuration * (int) TimeUtil.MILLIS_PER_SECOND) / 20) * 160;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.edex.core.IContextStateProcessor#preStart()
     */
    @Override
    public void preStart() {
        if (this.disabled) {
            statusHandler
                    .info("TTS Manager is currently disabled. Skipping Initialization.");
            return;
        }

        try {
            this.initialize();
        } catch (BMHConfigurationException | IOException e) {
            statusHandler.fatal(BMH_CATEGORY.TTS_CONFIGURATION_ERROR,
                    "TTS Manager Initialization Failed!", e);
            /* Halt the context startup. */
            throw new RuntimeException("TTS Manager Initialization Failed!", e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.edex.core.IContextStateProcessor#postStart()
     */
    @Override
    public void postStart() {
        // Do Nothing
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.edex.core.IContextStateProcessor#preStop()
     */
    @Override
    public void preStop() {
        this.dispose();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.edex.core.IContextStateProcessor#postStop()
     */
    @Override
    public void postStop() {
        // Do Nothing
    }

    /**
     * Retrieves and validates the configuration required by the TTS Manager.
     * 
     * @throws BMHConfigurationException
     * @throws IOException
     */
    private void initialize() throws BMHConfigurationException, IOException {
        statusHandler.info("Initializing the TTS Manager ...");

        /* Attempt to retrieve the location of BMH_DATA */
        this.bmhDataDirectory = FilenameUtils.normalize(this.bmhDataDirectory
                + File.separatorChar + BMHConstants.AUDIO_DATA_DIRECTORY);

        /*
         * Attempt to retrieve configuration information from the System
         * properties
         */
        Long ttsHeartbeatLong = Long.getLong(TTS_HEARTBEAT, null);

        Integer retryInteger = Integer.getInteger(TTS_RETRY_THRESHOLD_PROPERTY,
                null);
        Long delayLong = Long.getLong(TTS_RETRY_DELAY_PROPERTY, null);

        /* Verify that the configuration was successfully retrieved. */
        if (ttsHeartbeatLong == null) {
            throw new BMHConfigurationException(
                    "Failed to retrieve the TTS Heartbeat Interval from configuration!");
        }

        if (retryInteger == null) {
            throw new BMHConfigurationException(
                    "Failed to retrieve the TTS Retry Threshold from configuration!");
        }
        if (delayLong == null) {
            throw new BMHConfigurationException(
                    "Failed to retrieve the TTS Retry Delay from configuration!");
        }

        this.ttsHeartbeat = ttsHeartbeatLong;

        this.ttsRetryThreshold = retryInteger;
        this.ttsRetryDelay = delayLong;

        /* Ensure that the heartbeat interval is > 0 */
        if (this.ttsHeartbeat <= 0) {
            throw new BMHConfigurationException(
                    "TTS Heartbeat Interval must be > 0!");
        }

        /* Ensure that the TTS Retry Delay is >= 0 */
        if (this.ttsRetryDelay < 0) {
            throw new BMHConfigurationException("TTS Retry Delay must be >= 0!");
        }

        this.validateSynthesis();

        statusHandler.info("TTS Heartbeat Interval is " + this.ttsHeartbeat);
        statusHandler.info("BMH Audio Directory is: " + this.bmhDataDirectory);
        statusHandler.info("TTS Retry Threshold is: " + this.ttsRetryThreshold);
        statusHandler
                .info("TTS Retry Delays is: " + this.ttsRetryDelay + " ms");
        statusHandler.info("Starting TTS Server Monitor ...");
        this.heartbeatMonitor = new ScheduledThreadPoolExecutor(CORE_POOL_SIZE);
        this.heartbeatMonitor.scheduleAtFixedRate(this, this.ttsHeartbeat,
                this.ttsHeartbeat, TimeUnit.MILLISECONDS);
        statusHandler.info("Initialization Successful!");
    }

    public void dispose() {
        if (this.heartbeatMonitor != null) {
            statusHandler.info("Stopping TTS Server Monitor ...");
            this.heartbeatMonitor.shutdownNow();
        }
    }

    /**
     * Verifies that it is possible to connect to the TTS Server specified in
     * the configuration. This test does not care about the actual results of
     * the Text-to-Speech transformation that is tested.
     * 
     * @throws BMHConfigurationException
     * @throws IOException
     */
    private void validateSynthesis() throws BMHConfigurationException,
            IOException {
        int attempt = 0;
        boolean retry = true;

        while (retry) {
            ++attempt;

            TTSReturn ttsReturn;
            try {
                ttsReturn = this.synthesisFactory.synthesize("TEST",
                        this.ttsValidationVoice, TTS_DEFAULT_FORMAT);
            } catch (TTSSynthesisException e) {
                statusHandler.error(BMH_CATEGORY.UNKNOWN,
                        "TTS Synthesis validation has failed!", e);
                this.notifyTTSStatus(false);
                continue;
            }

            this.notifyTTSStatus((ttsReturn.getReturnValue() == TTS_RETURN_VALUE.TTS_RESULT_SUCCESS));
            retry = this.checkRetry(attempt, ttsReturn.getReturnValue());
        }

        statusHandler.info("Verified the TTS Server at "
                + this.synthesisFactory.getTtsServer() + " running on Port "
                + this.synthesisFactory.getTtsSynthesisPort()
                + " is available.");
    }

    private void notifyTTSStatus(final boolean connected) {
        if (this.bmhStatusDestination != null) {
            try {
                TTSStatus status = new TTSStatus(InetAddress.getLocalHost()
                        .getHostName(), InetAddress.getByName(
                        this.synthesisFactory.getTtsServer())
                        .getCanonicalHostName(), connected);
                EDEXUtil.getMessageProducer().sendAsyncUri(
                        bmhStatusDestination,
                        SerializationUtil.transformToThrift(status));
            } catch (Throwable e) {
                statusHandler.error(BMH_CATEGORY.TTS_SOFTWARE_ERROR,
                        "Unable to send status of TTS", e);
            }
        }
    }

    private boolean checkRetry(int attempt, TTS_RETURN_VALUE returnValue) {
        if (returnValue == TTS_RETURN_VALUE.TTS_RESULT_SUCCESS) {
            return false;
        }

        StringBuilder stringBuilder = new StringBuilder("Connection Attempt (");
        stringBuilder.append(attempt);
        stringBuilder.append(") to the TTS Server ");
        stringBuilder.append(this.synthesisFactory.getTtsServer());
        stringBuilder.append(" has failed! ");
        stringBuilder.append(this.getTTSErrorLogStatement(returnValue));
        stringBuilder.append(". Retrying ...");
        statusHandler.warn(returnValue.getAssociatedBMHCategory(),
                stringBuilder.toString());

        this.sleepDelayTime();

        return true;
    }

    public BroadcastMsgGroup process(BroadcastMsgGroup group) throws Exception {
        for (BroadcastMsg message : group.getMessages()) {
            process(message);
        }
        return group;
    }

    /**
     * Invoked by the Camel Route to process the Broadcast Message - the primary
     * purpose of this class.
     * 
     * @param message
     *            the message to process
     * @return an updated {@link BroadcastMsg} that will indicate whether or not
     *         the synthesis and file write are successful as well as the output
     *         file location when synthesis is successful
     */
    public BroadcastMsg process(BroadcastMsg message) throws Exception {
        if (message == null
                || message.getLatestBroadcastContents() == null
                || message.getLatestBroadcastContents().getFragments() == null
                || message.getLatestBroadcastContents().getFragments()
                        .isEmpty()) {
            /* Do not send a NULL downstream. */
            throw new Exception(
                    "Receieved an uninitialized or incomplete Broadcast Message to process!");
        }

        if (this.disabled) {
            statusHandler
                    .info("TTS Manager is currently disabled. No messages can be processed.");
            for (BroadcastFragment fragment : message
                    .getLatestBroadcastContents().getFragments()) {
                fragment.setSuccess(false);
            }
            return message;
        }

        statusHandler
                .info("Performing Text-to-Speech Transformation for message: "
                        + message.getId() + ".");
        for (BroadcastFragment fragment : message.getLatestBroadcastContents()
                .getFragments()) {
            if (fragment.isSuccess()
                    && Files.exists(Paths.get(fragment.getOutputName()))) {
                continue;
            }

            final StringBuilder logIdentifier = new StringBuilder("message: ");
            logIdentifier.append(message.getId());
            logIdentifier.append(" (fragment: ");
            logIdentifier.append(fragment.getId());
            logIdentifier.append(")");

            TTSReturn ttsReturn = this.attemptAudioSynthesis(
                    fragment.getSsml(), fragment.getVoice().getVoiceNumber(),
                    logIdentifier.toString());

            fragment.setSuccess(ttsReturn.isSynthesisSuccess());
            if (ttsReturn.isSynthesisSuccess()) {
                /* Synthesis Success */
                byte[] synthesizedAudio = ttsReturn.getVoiceData();
                int totalPlaybackSeconds = this
                        .getAudioPlaybackDurationSeconds(synthesizedAudio);
                statusHandler
                        .info("Text-to-Speech Transformation completed successfully for "
                                + logIdentifier.toString()
                                + ".  Length of playback = "
                                + totalPlaybackSeconds + " seconds");

                /* Write the output file. */
                File outputFile = this.determineOutputFile(message.getId(),
                        fragment.getId(),
                        message.getInputMessage().getAfosid(),
                        fragment.getVoice());
                boolean writeSuccess = true;
                try {
                    this.writeSynthesizedAudio(synthesizedAudio,
                            Paths.get(outputFile.getAbsolutePath()),
                            logIdentifier.toString());
                } catch (IOException e) {
                    writeSuccess = false;
                    this.messageLogger.logError(BMH_COMPONENT.TTS_MANAGER,
                            BMH_ACTIVITY.AUDIO_WRITE, message, e);
                }
                fragment.setSuccess(writeSuccess);
                if (writeSuccess) {
                    fragment.setOutputName(outputFile.getAbsolutePath());
                }
            } else {
                /* Synthesis Failed */
                this.logSynthesisError(ttsReturn, logIdentifier.toString());
                this.messageLogger.logError(BMH_COMPONENT.TTS_MANAGER,
                        BMH_ACTIVITY.AUDIO_SYNTHESIS, message);
            }
        }
        message.setUpdateDate(TimeUtil.newGmtCalendar());

        return message;
    }

    /**
     * Special case to synthesize audio data for ldad configurations. This audio
     * may be converted to another format and it is never added to a playlist
     * for broadcast.
     * 
     * @param message
     *            the {@link LdadMessage} to synthesize audio for
     * @return an updated {@link LdadMessage} that will indicate whether or not
     *         the synthesis and file write are successful as well as the output
     *         file location when synthesis is successful
     * @throws Exception
     */
    public LdadMsg process(LdadMsg message) throws Exception {
        statusHandler
                .info("Performing Text-to-Speech Transformation for ldad configuration: "
                        + message.getLdadId());

        /*
         * TODO: optimize the ldad generation to share similar files across ldad
         * messages. Files with the same format and dictionary.
         */

        StringBuilder logIdentifier = new StringBuilder("ldad configuration: ");
        logIdentifier.append(message.getLdadId());
        logIdentifier.append(" (message type: ");
        logIdentifier.append(message.getAfosid());
        logIdentifier.append(")");

        /*
         * Attempt Synthesis.
         */
        TTSReturn ttsReturn = this.attemptAudioSynthesis(message.getSsml(),
                message.getVoiceNumber(), logIdentifier.toString());
        message.setSuccess(ttsReturn.isSynthesisSuccess());
        if (ttsReturn.isSynthesisSuccess()) {
            /* Synthesis Success */
            int totalBytes = ttsReturn.getVoiceData().length;
            statusHandler
                    .info("Text-to-Speech Transformation completed successfully for "
                            + logIdentifier.toString()
                            + ". Length of playback = "
                            + (((totalBytes / 160) * 20) / 1000) + " seconds");

            Path outputPath = this.determineLdadOutputPath(message.getLdadId(),
                    message.getAfosid(), message.getEncoding());
            /*
             * Determine if the audio needs to be converted to a different
             * format.
             */
            byte[] audioData = ttsReturn.getVoiceData();
            if (DEFAULT_OUTPUT_FORMAT != message.getEncoding()) {
                /*
                 * need the audio to be in a format other than the default.
                 */
                try {
                    audioData = EdexAudioConverterManager.getInstance()
                            .convertAudio(audioData, DEFAULT_OUTPUT_FORMAT,
                                    message.getEncoding());
                } catch (UnsupportedAudioFormatException
                        | AudioConversionException e) {
                    statusHandler.error(
                            BMH_CATEGORY.LDAD_ERROR,
                            "Failed to convert the audio for "
                                    + logIdentifier.toString() + " from the "
                                    + DEFAULT_OUTPUT_FORMAT.toString()
                                    + " format to the "
                                    + message.getEncoding().toString()
                                    + " format.");
                    /*
                     * Audio conversion failed; no need to attempt to write the
                     * audio.
                     */
                    message.setSuccess(false);

                    return message;
                }
            }
            /* Write the output file. */
            boolean writeSuccess = true;
            try {
                this.writeSynthesizedAudio(ttsReturn.getVoiceData(),
                        outputPath, logIdentifier.toString());
            } catch (IOException e) {
                writeSuccess = false;
                this.messageLogger.logError(BMH_COMPONENT.TTS_MANAGER,
                        BMH_ACTIVITY.AUDIO_WRITE, message, e);
            }
            message.setSuccess(writeSuccess);
            if (writeSuccess) {
                message.setOutputName(outputPath.toString());
            }
        } else {
            /* Synthesis Failed */
            this.logSynthesisError(ttsReturn, logIdentifier.toString());
            this.messageLogger.logError(BMH_COMPONENT.TTS_MANAGER,
                    BMH_ACTIVITY.AUDIO_SYNTHESIS, message);
        }

        return message;
    }

    /**
     * Attempt to synthesize the specified ssml into audio using the specified
     * voice identifier.
     * 
     * @param ssml
     *            the specified ssml
     * @param voiceNumber
     *            the specified voice identifier
     * @param logIdentifier
     *            identifies the entity that triggered the synthesis. exists for
     *            logging purposes.
     * @return a {@link TTSReturn} with all applicable synthesis results
     */
    private TTSReturn attemptAudioSynthesis(final String ssml,
            final int voiceNumber, final String logIdentifier) {

        int attempt = 0;
        TTSReturn ttsReturn = null;

        while (true) {
            ++attempt;

            /* Attempt the Conversion */
            try {
                ttsReturn = this.synthesisFactory.synthesize(ssml, voiceNumber,
                        TTS_DEFAULT_FORMAT);
            } catch (TTSSynthesisException e) {
                statusHandler.error(BMH_CATEGORY.UNKNOWN, "TTS synthesis of "
                        + logIdentifier + " has failed! Attempt (" + attempt
                        + ")", e);
                if (attempt > this.ttsRetryThreshold) {
                    break;
                } else {
                    continue;
                }
            }

            if (ttsReturn.isIoFailed() == false) {
                if (ttsReturn.getReturnValue() == TTS_RETURN_VALUE.TTS_RESULT_SUCCESS) {
                    statusHandler.info("Successfully retrieved "
                            + ttsReturn.getVoiceData().length
                            + " bytes of audio data for " + logIdentifier
                            + ". Data retrieval complete!");
                    break;
                }

                if (this.retry(ttsReturn.getReturnValue()) == false) {
                    /* Absolute Failure */
                    break;
                }
            }

            /* Have we reached the retry threshold? */

            if (attempt > this.ttsRetryThreshold) {
                /*
                 * maximum retry count reached; halt text-to-speech
                 * transformation attempts
                 */
                break;
            } else {
                String logMessage = "Text-to-Speech Transformation was unsuccessful for "
                        + logIdentifier + "; ";
                BMH_CATEGORY category = null;
                if (ttsReturn.getIoFailureCause() == null) {
                    /* Build the log message. */
                    logMessage += this.getTTSErrorLogStatement(ttsReturn
                            .getReturnValue());
                    category = ttsReturn.getReturnValue()
                            .getAssociatedBMHCategory();
                } else {
                    logMessage += "IO Error = "
                            + ttsReturn.getIoFailureCause()
                                    .getLocalizedMessage();
                    category = BMH_CATEGORY.TTS_SYSTEM_ERROR;
                }
                logMessage += "! Attempt (" + attempt + ")";

                /* Just log a warning. */
                statusHandler.warn(category, logMessage);

                this.sleepDelayTime();
            }
        }

        return ttsReturn;
    }

    /**
     * Convenience method to log synthesis errors with all applicable
     * information. Will throw a {@link IllegalStateException} if the tts
     * results indicate that the synthesis was actually successful.
     * 
     * @param ttsReturn
     *            the results of the synthesis attempt
     * @param logIdentifier
     *            identifies the entity that triggered the synthesis. exists for
     *            logging purposes.
     */
    private void logSynthesisError(final TTSReturn ttsReturn,
            final String logIdentifier) {
        if (ttsReturn.isSynthesisSuccess()) {
            throw new IllegalStateException(
                    "The specified TTSReturn synthesis results indicate that the synthesis completed successfully. There are no synthesis errors to log.");
        }

        StringBuilder stringBuilder = new StringBuilder(
                "Text-to-Speech Transformation failed for ");
        stringBuilder.append(logIdentifier);
        if (ttsReturn.getIoFailureCause() == null) {
            stringBuilder.append("; ");
            stringBuilder.append(this.getTTSErrorLogStatement(ttsReturn
                    .getReturnValue()));
            stringBuilder.append("!");
            statusHandler.error(ttsReturn.getReturnValue()
                    .getAssociatedBMHCategory(), stringBuilder.toString());
        } else {
            stringBuilder.append("!");
            statusHandler.error(BMH_CATEGORY.TTS_SYSTEM_ERROR,
                    stringBuilder.toString(), ttsReturn.getIoFailureCause());
        }
    }

    /**
     * Convenience method to write the specified audio bytes to the specified
     * audio file. Handles logging I/O failures that cause the file write to
     * fail.
     * 
     * @param audio
     *            the specified audio bytes to write
     * @param outputPath
     *            the specified path to the audio file to write
     * @param logIdentifier
     *            identifies the entity that triggered the synthesis. exists for
     *            logging purposes.
     * 
     * @return true if the file was written successfully; false, otherwise
     */
    private void writeSynthesizedAudio(final byte[] audio,
            final Path outputPath, final String logIdentifier)
            throws IOException {
        /*
         * Ensure that the required directories exist.
         */
        final Path containingDirectory = outputPath.getParent();
        if (Files.exists(containingDirectory) == false) {
            /*
             * attempt to create the root directories.
             */
            try {
                Files.createDirectories(containingDirectory);
            } catch (IOException e) {
                StringBuilder stringBuilder = new StringBuilder(
                        "Failed to create audio directory: ");
                stringBuilder.append(containingDirectory.toString());
                stringBuilder.append("; REASON = ");
                stringBuilder.append(e.getLocalizedMessage());
                statusHandler.error(BMH_CATEGORY.TTS_SYSTEM_ERROR,
                        stringBuilder.toString(), e);

                throw e;
            }
        }

        final int totalPlaybackSeconds = this
                .getAudioPlaybackDurationSeconds(audio);
        int writeLength = audio.length;

        /*
         * Verify the duration is not greater than the maximum allowed duration.
         */
        if (totalPlaybackSeconds > this.maxAudioDuration) {
            StringBuilder sb = new StringBuilder("Truncating audio for ");
            sb.append(logIdentifier.toString()).append(" from ");
            sb.append(totalPlaybackSeconds).append(" seconds to ");
            sb.append(this.maxAudioDuration).append(" seconds.");
            statusHandler.warn(BMH_CATEGORY.AUDIO_TRUNCATED, sb.toString());
            writeLength = this.maxAudioByteCount;
        }

        /**
         * We use a {@link OutputStream} to complete the write just in case we
         * only want to write a portion of the audio bytes. If we were to copy
         * an audio array that we want to truncate into a smaller audio array,
         * we risk a java.lang.OutOfMemoryError which would allow external users
         * to DDOS the system by constantly sending data files with large blocks
         * of text that when synthesized are significantly > 10 minutes.
         */
        try (OutputStream os = Files.newOutputStream(outputPath)) {
            os.write(audio, 0, writeLength);
            os.flush();
            statusHandler.info("Successfully wrote audio output file: "
                    + outputPath.toString() + " for "
                    + logIdentifier.toString() + ". " + audio.length
                    + " bytes were written.");
        } catch (Exception e) {
            StringBuilder stringBuilder = new StringBuilder(
                    "Failed to write audio output file: ");
            stringBuilder.append(outputPath.toString());
            stringBuilder.append("; REASON = ");
            stringBuilder.append(e.getLocalizedMessage());
            statusHandler.error(BMH_CATEGORY.TTS_SYSTEM_ERROR,
                    stringBuilder.toString(), e);
            /*
             * TTS Conversion may have succeeded; however, the file write has
             * failed!
             */
            throw e;
        }
    }

    /**
     * Sleeps a configurable number of milliseconds before continuing.
     */
    private void sleepDelayTime() {
        /* Wait 'X' milliseconds. */
        try {
            Thread.sleep(this.ttsRetryDelay);
        } catch (InterruptedException e) {
            /*
             * Not sure what the actual likelihood of this error actually is.
             */
            StringBuilder stringBuilder = new StringBuilder("Failed to wait ");
            stringBuilder.append(this.ttsRetryDelay);
            stringBuilder.append("milliseconds!");
            if (e.getLocalizedMessage() != null) {
                stringBuilder.append(" REASON = ");
                stringBuilder.append(e.getLocalizedMessage());
            }
            statusHandler.warn(BMH_CATEGORY.INTERRUPTED,
                    stringBuilder.toString());

            /* Just log the error and continue. */
        }
    }

    /**
     * Determines the name of the output file based on the current date / time
     * and other parameters that are available in the broadcast message.
     * 
     * @param bmhID
     *            the id associated with the broadcast message in the database
     * @param fragmentId
     *            the id of the broadcast fragment that the file will be
     *            generated for
     * @param afosID
     *            the afos id
     * @param voice
     *            the voice used to encode the message during the text-to-speech
     *            process
     * @return the output file
     */
    private File determineOutputFile(final long bmhID, final long fragmentId,
            final String afosID, TtsVoice voice) {

        final String fileNamePartsSeparator = "_";

        StringBuilder fileName = new StringBuilder(this.bmhDataDirectory);
        fileName.append(File.separatorChar);

        /* get the current date and time */
        Date currentDate = TimeUtil.newDate();

        /* todays directory */
        fileName.append(TODAY_DATED_DIRECTORY_FORMAT.get().format(currentDate));
        fileName.append(File.separatorChar);

        /* afos id */
        fileName.append(afosID);

        /* "separator" */
        fileName.append(fileNamePartsSeparator);

        /* language identifier */
        fileName.append(voice.getLanguage().getIdentifier());

        /* "separator" */
        fileName.append(fileNamePartsSeparator);

        /* voice alias */
        fileName.append(voice.getVoiceName());

        /* "separator" */
        fileName.append(fileNamePartsSeparator);

        /* BMH ID */
        fileName.append(bmhID);

        /* "separator" */
        fileName.append(fileNamePartsSeparator);

        /* Fragment ID */
        fileName.append(fragmentId);

        /* "separator" */
        fileName.append(fileNamePartsSeparator);

        /* formatted creation time */
        fileName.append(FILE_NAME_TIME_FORMAT.get().format(currentDate));

        /* File Extension */
        fileName.append(DEFAULT_TTS_FILE_EXTENSION);

        return new File(FilenameUtils.normalize(fileName.toString()));
    }

    /**
     * Determines the name of the ldad audio output file based on the date/time
     * and other parameters that are available in the ldad configuration.
     * 
     * The audio conversion and output could optionally be relocated to the ldad
     * route. However, this fits into the current TTS flow of synthesizing the
     * audio and writing the output.
     * 
     * @param ldadId
     *            id of the ldad configuration that triggered the audio
     *            generation
     * @param afosId
     *            afos id associated with the message type that has been
     *            assigned to the ldad configuration
     * @param encoding
     *            the file format of the output file. Ldad audio allows for more
     *            than just the standard {@link BMHAudioFormat#ULAW} format.
     * @return {@link Path} to the location of the ldad audio output file
     */
    private Path determineLdadOutputPath(final long ldadId,
            final String afosId, final BMHAudioFormat encoding) {

        final String fileNamePartsSeparator = "_";

        /* get the current date and time */
        Date currentDate = TimeUtil.newDate();

        /*
         * ldad audio included in the dated audio directories for automatic
         * removal by purge.
         */
        final String bmhDatedDirectory = TODAY_DATED_DIRECTORY_FORMAT.get()
                .format(currentDate);

        StringBuilder fileName = new StringBuilder("LDAD");
        fileName.append(fileNamePartsSeparator);
        /* Id of the ldad configuration this audio was generated for */
        fileName.append(ldadId);
        fileName.append(fileNamePartsSeparator);
        /* afos id of the message type associated with the ldad configuration */
        fileName.append(afosId);
        fileName.append(fileNamePartsSeparator);
        /* formatted creation time */
        fileName.append(FILE_NAME_TIME_FORMAT.get().format(currentDate));
        /* file extension */
        fileName.append(encoding.getExtension());

        return Paths.get(this.bmhDataDirectory).resolve(bmhDatedDirectory)
                .resolve(fileName.toString());
    }

    /**
     * Determines if additional attempts should be made to convert the message
     * after a failure based on the reason the conversion failed.
     * 
     * @param returnCode
     *            the error code associated with a failed text-to-speech
     *            conversion attempt
     * @return {true : if additional conversion attempts should be made, false:
     *         to fail the conversion attempt }
     */
    private boolean retry(TTS_RETURN_VALUE returnValue) {

        switch (returnValue) {
        case TTS_CONNECT_ERROR:
        case TTS_SOCKET_ERROR:
        case TTS_READWRITE_ERROR:
        case TTS_DISK_ERROR:
        case TTS_MAX_ERROR:
            return true;
        default:
            return false;
        }
    }

    private String getTTSErrorLogStatement(TTS_RETURN_VALUE returnValue) {
        StringBuilder stringBuilder = new StringBuilder("TTS ERROR = ");
        stringBuilder.append(returnValue.getDescription());
        stringBuilder.append(" (");
        stringBuilder.append(returnValue.toString());
        stringBuilder.append(")");

        return stringBuilder.toString();
    }

    @Override
    public void run() {
        try {
            /**
             * Port scanning would have been another option. However, we would
             * just be sending a blank request to the TTS Server that it would
             * not understand and log as an error. So, it is just better to send
             * an extremely simple request to the TTS Server that it can handle
             * correctly without tying up any of the processing threads for a
             * significant period of time.
             * 
             * Checking the running processes for the TTS Server process would
             * be another option. However, we would be assuming that the TTS
             * Server was running locally.
             */
            this.validateSynthesis();
        } catch (BMHConfigurationException | IOException e) {
            statusHandler
                    .error(BMH_CATEGORY.TTS_SYSTEM_ERROR,
                            "Failed to determine the current status of the TTS Server!",
                            e);
        }
    }

    /**
     * @return the bmhDataDirectory
     */
    public String getBmhDataDirectory() {
        return bmhDataDirectory;
    }

    /**
     * @param bmhDataDirectory
     *            the bmhDataDirectory to set
     */
    public void setBmhDataDirectory(String bmhDataDirectory) {
        this.bmhDataDirectory = bmhDataDirectory;
    }

    public void setBmhStatusDestination(String bmhStatusDestination) {
        this.bmhStatusDestination = bmhStatusDestination;
    }

    /**
     * @return the synthesisFactory
     */
    public TTSSynthesisFactory getSynthesisFactory() {
        return synthesisFactory;
    }

    /**
     * @param synthesisFactory
     *            the synthesisFactory to set
     */
    public void setSynthesisFactory(TTSSynthesisFactory synthesisFactory) {
        this.synthesisFactory = synthesisFactory;
    }

    private int getAudioPlaybackDurationSeconds(final byte[] audio) {
        // TODO: need to centralize audio playback time calculation.
        /* calculate the duration in seconds */
        final long playbackTimeMS = audio.length / 160L * 20L;
        // swt component expects an Integer
        final int playbackTimeS = (int) playbackTimeMS / 1000;

        return playbackTimeS;
    }
}