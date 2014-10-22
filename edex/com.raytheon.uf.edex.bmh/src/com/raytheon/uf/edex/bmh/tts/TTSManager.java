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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.raytheon.uf.common.bmh.BMH_CATEGORY;
import com.raytheon.uf.common.bmh.TTSConstants.TTS_FORMAT;
import com.raytheon.uf.common.bmh.TTSConstants.TTS_RETURN_VALUE;
import com.raytheon.uf.common.bmh.TTSSynthesisException;
import com.raytheon.uf.common.bmh.audio.BMHAudioFormat;
import com.raytheon.uf.common.bmh.datamodel.language.TtsVoice;
import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastFragment;
import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.edex.bmh.BMHConfigurationException;
import com.raytheon.uf.edex.bmh.BMHConstants;
import com.raytheon.uf.edex.bmh.status.BMHStatusHandler;
import com.raytheon.uf.edex.bmh.status.IBMHStatusHandler;
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

    /* Output root subdirectory */
    private static final String AUDIO_DATA_DIRECTORY = "audio";

    private static final ThreadLocal<SimpleDateFormat> TODAY_DATED_DIRECTORY_FORMAT = TimeUtil
            .buildThreadLocalSimpleDateFormat("yyMMdd",
                    TimeZone.getTimeZone("GMT"));

    private static final ThreadLocal<SimpleDateFormat> FILE_NAME_TIME_FORMAT = TimeUtil
            .buildThreadLocalSimpleDateFormat("HHmmss",
                    TimeZone.getTimeZone("GMT"));

    /* For now, just default the format to a MULAW file. */
    private static final TTS_FORMAT TTS_DEFAULT_FORMAT = TTS_FORMAT.TTS_FORMAT_MULAW;

    private static final String DEFAULT_TTS_FILE_EXTENSION = BMHAudioFormat.ULAW
            .getExtension();

    /* Configuration Property Name Constants */
    private static final String TTS_THREADS = "bmh.tts.threads";

    private static final String TTS_HEARTBEAT = "bmh.tts.heartbeat";

    private static final String TTS_VOICE_VALIDATE_PROPERTY = "bmh.tts.voice.validate";

    private static final String TTS_RETRY_THRESHOLD_PROPERTY = "bmh.tts.retry-threshold";

    private static final String TTS_RETRY_DELAY_PROPERTY = "bmh.tts.retry-delay";

    private static final String TTS_DISABLED_PROPERTY = "bmh.tts.disabled";

    /* Configuration */
    private String bmhDataDirectory;

    private long ttsHeartbeat;

    private Integer ttsValidationVoice;

    private int ttsRetryThreshold;

    private long ttsRetryDelay;

    private final boolean disabled;

    private TTSSynthesisFactory synthesisFactory;

    private ScheduledThreadPoolExecutor heartbeatMonitor;

    public TTSManager() {
        this.disabled = Boolean.getBoolean(TTS_DISABLED_PROPERTY);
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
        this.bmhDataDirectory = BMHConstants.getBmhDataDirectory();
        this.bmhDataDirectory = FilenameUtils.normalize(this.bmhDataDirectory
                + File.separatorChar + AUDIO_DATA_DIRECTORY);

        /*
         * Attempt to retrieve configuration information from the System
         * properties
         */
        Integer ttsThreadInteger = Integer.getInteger(TTS_THREADS, null);
        Long ttsHeartbeatLong = Long.getLong(TTS_HEARTBEAT, null);

        this.ttsValidationVoice = Integer.getInteger(
                TTS_VOICE_VALIDATE_PROPERTY, null);
        Integer retryInteger = Integer.getInteger(TTS_RETRY_THRESHOLD_PROPERTY,
                null);
        Long delayLong = Long.getLong(TTS_RETRY_DELAY_PROPERTY, null);

        /* Verify that the configuration was successfully retrieved. */
        if (ttsThreadInteger == null) {
            throw new BMHConfigurationException(
                    "Failed to retrieve the TTS Available Thread Count from configuration!");
        }
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

        if (this.ttsValidationVoice != null) {
            statusHandler
                    .info("TTS Voice "
                            + this.ttsValidationVoice
                            + " will be used to verify that the TTS synthesis capability is available.");
        }
        /*
         * Perform both checks during the initialization validation.
         */
        this.validateAvailability();
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

    private void validateAvailability() throws BMHConfigurationException,
            IOException {
        int attempt = 0;
        boolean retry = true;
        while (retry) {
            ++attempt;
            TTS_RETURN_VALUE returnValue = this.synthesisFactory
                    .validateServerAvailability();

            retry = this.checkRetry(attempt, returnValue);
        }

        statusHandler.info("Verified the availability of the TTS Server at "
                + this.synthesisFactory.getTtsServer() + " (Status Port = "
                + this.synthesisFactory.getTtsStatusPort() + ").");
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
        if (this.ttsValidationVoice == null) {
            statusHandler
                    .info("A validation voice has not been specified. Please check the configuration. Skipping synthesis validation ...");
            return;
        }

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
                continue;
            }

            retry = this.checkRetry(attempt, ttsReturn.getReturnValue());
        }

        statusHandler.info("Verified the TTS Server at "
                + this.synthesisFactory.getTtsServer() + " running on Port "
                + this.synthesisFactory.getTtsSynthesisPort()
                + " is capable of synthesis.");
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

    /**
     * Invoked by the Camel Route to process the Broadcast Message - the primary
     * purpose of this class.
     * 
     * @param message
     *            the message to process
     * @return
     */
    public BroadcastMsg process(BroadcastMsg message) throws Exception {
        if (message == null) {
            /* Do not send a NULL downstream. */
            throw new Exception(
                    "Receieved an uninitialized or incomplete Broadcast Message to process!");
        }

        if (this.disabled) {
            statusHandler
                    .info("TTS Manager is currently disabled. No messages can be processed.");
            for (BroadcastFragment fragment : message.getFragments()) {
                fragment.setSuccess(false);
            }
            return message;
        }

        statusHandler
                .info("Performing Text-to-Speech Transformation for message: "
                        + message.getId() + ".");
        for (BroadcastFragment fragment : message.getFragments()) {
            if (fragment.isSuccess()
                    && Files.exists(Paths.get(fragment.getOutputName()))) {
                continue;
            }
            int attempt = 0;
            TTSReturn ttsReturn = null;
            boolean success = false;
            IOException ioException = null;

            while (true) {
                ++attempt;
                ioException = null;

                /* Attempt the Conversion */
                try {
                    ttsReturn = this.synthesisFactory.synthesize(fragment
                            .getSsml(), fragment.getVoice().getVoiceNumber(),
                            TTS_DEFAULT_FORMAT);
                } catch (TTSSynthesisException e) {
                    statusHandler.error(BMH_CATEGORY.UNKNOWN,
                            "TTS synthesis of message " + message.getId()
                                    + " has failed! Attempt (" + attempt + ")",
                            e);
                    if (attempt > this.ttsRetryThreshold) {
                        break;
                    } else {
                        continue;
                    }
                }

                if (ttsReturn.isIoFailed()) {
                    ioException = ttsReturn.getIoFailureCause();
                } else {
                    if (ttsReturn.getReturnValue() == TTS_RETURN_VALUE.TTS_RESULT_SUCCESS) {
                        statusHandler.info("Successfully retrieved "
                                + ttsReturn.getVoiceData().length
                                + " bytes of audio data for message: "
                                + message.getId() + "(fragment: "
                                + fragment.getId()
                                + "). Data retrieval complete!");
                        /* Successful Conversion */
                        success = true;
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
                    String logMessage = "Text-to-Speech Transformation was unsuccessful for message: "
                            + message.getId() + "; ";
                    BMH_CATEGORY category = null;
                    if (ioException == null) {
                        /* Build the log message. */
                        logMessage += this.getTTSErrorLogStatement(ttsReturn
                                .getReturnValue());
                        category = ttsReturn.getReturnValue()
                                .getAssociatedBMHCategory();
                    } else {
                        logMessage += "IO Error = "
                                + ioException.getLocalizedMessage();
                        category = BMH_CATEGORY.TTS_SYSTEM_ERROR;
                    }
                    logMessage += "! Attempt (" + attempt + ")";

                    /* Just log a warning. */
                    statusHandler.warn(category, logMessage);

                    this.sleepDelayTime();
                }
            }

            if (success) {
                int totalBytes = ttsReturn.getVoiceData().length;
                statusHandler
                        .info("Text-to-Speech Transformation completed successfully for message: "
                                + message.getId()
                                + ".  Length of playback = "
                                + (((totalBytes / 160) * 20) / 1000)
                                + " seconds");
                /* Write the output file. */
                File outputFile = this.determineOutputFile(message.getId(),
                        message.getInputMessage().getAfosid(),
                        fragment.getVoice());
                try {
                    FileUtils.writeByteArrayToFile(outputFile,
                            ttsReturn.getVoiceData());
                    fragment.setOutputName(outputFile.getAbsolutePath());
                    statusHandler.info("Successfully wrote audio output file: "
                            + outputFile.getAbsolutePath() + " for message: "
                            + message.getId() + ". " + totalBytes
                            + " bytes were written.");
                } catch (IOException e) {
                    ioException = e;
                    StringBuilder stringBuilder = new StringBuilder(
                            "Failed to write audio output file: ");
                    stringBuilder.append(outputFile.getAbsolutePath());
                    stringBuilder.append("; REASON = ");
                    stringBuilder.append(e.getLocalizedMessage());
                    statusHandler.error(BMH_CATEGORY.TTS_SYSTEM_ERROR,
                            stringBuilder.toString(), e);
                    /*
                     * TTS Conversion may have succeeded; however, the file
                     * write has failed!
                     */
                    success = false;
                }
            } else {
                StringBuilder stringBuilder = new StringBuilder(
                        "Text-to-Speech Transformation failed for message: ");
                stringBuilder.append(message.getId());
                if (ioException == null) {
                    stringBuilder.append("; ");
                    stringBuilder.append(this.getTTSErrorLogStatement(ttsReturn
                            .getReturnValue()));
                    stringBuilder.append("!");
                    statusHandler.error(ttsReturn.getReturnValue()
                            .getAssociatedBMHCategory(), stringBuilder
                            .toString());
                } else {
                    stringBuilder.append("!");
                    statusHandler.error(BMH_CATEGORY.TTS_SYSTEM_ERROR,
                            stringBuilder.toString(), ioException);
                }
            }
            fragment.setSuccess(success);
        }
        message.setUpdateDate(TimeUtil.newGmtCalendar());

        return message;
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
     * @param afosID
     *            the afos id
     * @param voice
     *            the voice used to encode the message during the text-to-speech
     *            process
     * @return the output file
     */
    private File determineOutputFile(final long bmhID, final String afosID,
            TtsVoice voice) {

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

        /* formatted creation time */
        fileName.append(FILE_NAME_TIME_FORMAT.get().format(currentDate));

        /* File Extension */
        fileName.append(DEFAULT_TTS_FILE_EXTENSION);

        return new File(FilenameUtils.normalize(fileName.toString()));
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
            this.validateAvailability();
        } catch (BMHConfigurationException | IOException e) {
            statusHandler
                    .error(BMH_CATEGORY.TTS_SYSTEM_ERROR,
                            "Failed to determine the current status of the TTS Server!",
                            e);
        }
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
}