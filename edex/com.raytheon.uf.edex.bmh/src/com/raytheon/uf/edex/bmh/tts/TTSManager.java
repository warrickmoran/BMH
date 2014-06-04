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

import java.io.IOException;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.List;
import java.util.LinkedList;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.FileUtils;

import com.raytheon.uf.edex.bmh.BMHConstants;
import com.raytheon.uf.edex.bmh.msg.BroadcastMsg;
import com.raytheon.uf.edex.bmh.msg.BroadcastMsgBody;
import com.raytheon.uf.edex.bmh.tts.TTSConstants.TTS_FORMAT;
import com.raytheon.uf.edex.bmh.tts.TTSConstants.TTS_RETURN_VALUE;
import com.raytheon.uf.edex.core.IContextStateProcessor;
import com.raytheon.uf.common.bmh.datamodel.language.TtsVoice;
import com.raytheon.uf.common.datastorage.records.ByteDataRecord;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.common.time.util.TimeUtil;

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
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class TTSManager implements IContextStateProcessor {
    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(TTSManager.class);

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

    private static final String DEFAULT_TTS_FILE_EXTENSION = ".ulaw";

    /* Configuration Property Name Constants */
    private static final String TTS_SERVER_PROPERTY = "bmh.tts.server";

    private static final String TTS_PORT_PROPERTY = "bmh.tts.port";

    private static final String TTS_CONNECT_TIMEOUT_PROPERTY = "bmh.tts.connect-timeout";

    private static final String TTS_RETRY_THRESHOLD_PROPERTY = "bmh.tts.retry-threshold";

    private static final String TTS_RETRY_DELAY_PROPERTY = "bmh.tts.retry-delay";

    private static final String TTS_DISABLED_PROPERTY = "bmh.tts.disabled";

    /* Configuration */
    private String bmhDataDirectory;

    private String ttsServer;

    private int ttsPort;

    private int ttsConnectionTimeout;

    private int ttsRetryThreshold;

    private long ttsRetryDelay;

    private boolean disabled;

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
        } catch (TTSConfigurationException | IOException e) {
            statusHandler.handle(Priority.CRITICAL,
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
        // Do Nothing
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
     * @throws TTSConfigurationException
     * @throws IOException
     */
    private void initialize() throws TTSConfigurationException, IOException {
        statusHandler.info("Initializing the TTS Manager ...");

        /* Attempt to retrieve the location of BMH_DATA */
        this.bmhDataDirectory = BMHConstants.getBmhDataDirectory();
        this.bmhDataDirectory = FilenameUtils.normalize(this.bmhDataDirectory
                + File.separatorChar + AUDIO_DATA_DIRECTORY);

        /* Attempt to retrieve the server and port from the System properties */
        this.ttsServer = System.getProperty(TTS_SERVER_PROPERTY, null);
        Integer portInteger = Integer.getInteger(TTS_PORT_PROPERTY, null);
        Integer timeoutInteger = Integer.getInteger(
                TTS_CONNECT_TIMEOUT_PROPERTY, null);
        Integer retryInteger = Integer.getInteger(TTS_RETRY_THRESHOLD_PROPERTY,
                null);
        Long delayLong = Long.getLong(TTS_RETRY_DELAY_PROPERTY, null);

        /* Verify that the configuration was successfully retrieved. */
        if (this.ttsServer == null) {
            throw new TTSConfigurationException(
                    "Failed to retrieve the TTS Server from configuration!");
        }
        if (portInteger == null) {
            throw new TTSConfigurationException(
                    "Failed to retrieve the TTS Port from configuration!");
        }
        if (timeoutInteger == null) {
            throw new TTSConfigurationException(
                    "Failed to retrieve the TTS Connection Timeout from configuration!");
        }
        if (retryInteger == null) {
            throw new TTSConfigurationException(
                    "Failed to retrieve the TTS Retry Threshold from configuration!");
        }
        if (delayLong == null) {
            throw new TTSConfigurationException(
                    "Failed to retrieve the TTS Retry Delay from configuration!");
        }

        /* TTS Port is numeric. */
        this.ttsPort = portInteger;
        /* TTS Timeout is numeric. */
        this.ttsConnectionTimeout = timeoutInteger;
        /* TTS Retry Threshold is numeric. */
        this.ttsRetryThreshold = retryInteger;
        /* TTS Retry Delay is numeric. */
        this.ttsRetryDelay = delayLong;

        /* Ensure that the TTS Retry Delay is >= 0 */
        if (this.ttsRetryDelay < 0) {
            throw new TTSConfigurationException("TTS Retry Delay must be >= 0!");
        }
        /* Ensure that the TTS Connect Timeout is >= 0 */
        if (timeoutInteger < 0) {
            throw new TTSConfigurationException(
                    "TTS Connection Timeout must be >= 0!");
        }

        this.validate();
        statusHandler.info("TTS Connection Timeout is: " + timeoutInteger
                + " ms");
        statusHandler.info("BMH Audio Directory is: " + this.bmhDataDirectory);
        statusHandler.info("TTS Retry Threshold is: " + this.ttsRetryThreshold);
        statusHandler
                .info("TTS Retry Delays is: " + this.ttsRetryDelay + " ms");
        statusHandler.info("Initialization Successful!");
    }

    /**
     * Verifies that it is possible to connect to the TTS Server specified in
     * the configuration. This test does not care about the actual results of
     * the Text-to-Speech transformation that is tested.
     * 
     * @throws TTSConfigurationException
     * @throws IOException
     */
    private void validate() throws TTSConfigurationException, IOException {
        int attempt = 0;
        boolean retry = true;

        TTSInterface ttsInterface = this.getInterface();
        while (retry) {
            ++attempt;

            TTS_RETURN_VALUE returnValue = ttsInterface
                    .validateTTSIsAvailable();

            /* check the return code. */

            /* did the connection fail due to bad configuration? */
            if (returnValue == TTS_RETURN_VALUE.TTS_HOSTNAME_ERROR) {
                throw new TTSConfigurationException(
                        "Failed to connect to the TTS Server; "
                                + this.getTTSErrorLogStatement(returnValue)
                                + "!");
            }

            /*
             * did the connection fail due to an issue that could potentially be
             * intermittent?
             */
            if (returnValue != TTS_RETURN_VALUE.TTS_CONNECT_ERROR
                    && returnValue != TTS_RETURN_VALUE.TTS_SOCKET_ERROR) {
                retry = false;
            } else {
                statusHandler.warn("Connection Attempt (" + attempt
                        + ") to the TTS Server has failed! "
                        + this.getTTSErrorLogStatement(returnValue)
                        + ". Retrying ...");
                this.sleepDelayTime();
            }
        }

        statusHandler.info("Successfully connected to the TTS Server at "
                + this.ttsServer + " running on Port " + this.ttsPort + ".");
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
        if (message == null || message.getBody() == null) {
            /* Do not send a NULL downstream. */
            throw new Exception(
                    "Receieved an uninitialized or incomplete Broadcast Message to process!");
        }

        if (this.disabled) {
            statusHandler
                    .info("TTS Manager is currently disabled. No messages can be processed.");
            message.getBody().setSuccess(false);
            return message;
        }

        statusHandler
                .info("Performing Text-to-Speech Transformation for message: "
                        + message.getId() + ".");

        /* The TTS Manager only uses the body of the message. */
        BroadcastMsgBody body = message.getBody();

        /* LinkedList to maintain the order of the converted data. */
        List<ByteDataRecord> convertedData = new LinkedList<ByteDataRecord>();
        int attempt = 0;
        TTS_RETURN_VALUE returnValue = null;
        boolean getFirstFrame = true;
        boolean success = false;
        IOException ioException = null;

        TTSInterface ttsInterface = this.getInterface();

        while (true) {
            ByteDataRecord output = new ByteDataRecord();
            ++attempt;

            /* Attempt the Conversion */
            try {
                returnValue = ttsInterface.transformSSMLToAudio(body.getSsml(),
                        body.getVoice().getVoiceNumber(), TTS_DEFAULT_FORMAT,
                        output, getFirstFrame);
            } catch (IOException e1) {
                /*
                 * Prepare to retry on an IOException. Save the current
                 * IOException just in case we have reached the maximum number
                 * of attempts.
                 */
                ioException = e1;
            }

            if (returnValue == TTS_RETURN_VALUE.TTS_RESULT_SUCCESS) {
                convertedData.add(output);
                statusHandler.info("Successfully retrieved "
                        + output.getSizeInBytes()
                        + " bytes of audio data for message: "
                        + message.getId() + ". Data retrieval complete!");
                /* Successful Conversion */
                success = true;
                break;
            }

            if (returnValue == TTS_RETURN_VALUE.TTS_RESULT_CONTINUE) {
                convertedData.add(output);
                statusHandler.info("Successfully retrieved "
                        + output.getSizeInBytes()
                        + " bytes of audio data for message: "
                        + message.getId() + ". Retrieving additional data ...");
                /*
                 * The conversion was successful; however, there is additional
                 * data to retrieve.
                 */

                // Ensure that we will get the next available frame instead of
                // the first frame.
                getFirstFrame = false;
                /* Reset the attempt count. */
                attempt = 0;
                continue;
            }

            if (this.retry(returnValue) == false) {
                /* Absolute Failure */
                break;
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
                if (ioException == null) {
                    /* Build the log message. */
                    logMessage += this.getTTSErrorLogStatement(returnValue);
                } else {
                    logMessage += "IO Error = "
                            + ioException.getLocalizedMessage();
                }
                logMessage += "! Attempt (" + attempt + ")";

                /* Just log a warning. */
                statusHandler.warn(logMessage);

                this.sleepDelayTime();
            }
        }

        if (success) {
            statusHandler
                    .info("Text-to-Speech Transformation completed successfully for message: "
                            + message.getId() + ".");
            /* Write the output file. */
            File outputFile = this.determineOutputFile(message.getId(),
                    body.getAfosID(), body.getVoice());
            try {
                boolean append = false;
                int byteCount = 0;
                for (ByteDataRecord data : convertedData) {
                    FileUtils.writeByteArrayToFile(outputFile,
                            data.getByteData(), append);
                    byteCount += data.getSizeInBytes();
                    append = true;
                }
                message.getBody().setOutputName(outputFile.getAbsolutePath());
                statusHandler.info("Successfully wrote audio output file: "
                        + outputFile.getAbsolutePath() + " for message: "
                        + message.getId() + "." + byteCount
                        + " bytes were written.");
            } catch (IOException e) {
                statusHandler.handle(
                        Priority.PROBLEM,
                        "Failed to write audio output file: "
                                + outputFile.getAbsolutePath() + "; REASON = "
                                + e.getLocalizedMessage(), e);
                /*
                 * TTS Conversion may have succeeded; however, the file write
                 * has failed!
                 */
                success = false;
            }
        } else {
            if (ioException == null) {
                statusHandler.handle(
                        Priority.PROBLEM,
                        "Text-to-Speech Transformation failed for message: "
                                + message.getId() + "; "
                                + this.getTTSErrorLogStatement(returnValue)
                                + "!");
            } else {
                statusHandler.handle(Priority.PROBLEM,
                        "Text-to-Speech Transformation failed for message: "
                                + message.getId() + "!", ioException);
            }
        }
        message.getBody().setSuccess(success);

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
            statusHandler.warn("Failed to wait " + this.ttsRetryDelay
                    + "milliseconds!", e.getLocalizedMessage());
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
    private File determineOutputFile(final String bmhID, final String afosID,
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

    private TTSInterface getInterface() {
        TTSInterface ttsInterface = new TTSInterface(this.ttsServer,
                this.ttsPort, this.ttsConnectionTimeout);

        return ttsInterface;
    }

    private String getTTSErrorLogStatement(TTS_RETURN_VALUE returnValue) {
        StringBuilder stringBuilder = new StringBuilder("TTS ERROR = ");
        stringBuilder.append(returnValue.getDescription());
        stringBuilder.append(" (");
        stringBuilder.append(returnValue.toString());
        stringBuilder.append(")");

        return stringBuilder.toString();
    }
}
