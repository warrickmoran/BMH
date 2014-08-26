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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.Callable;

import com.raytheon.uf.common.bmh.BMH_CATEGORY;
import com.raytheon.uf.common.bmh.TTSConstants.TTS_RETURN_VALUE;
import com.raytheon.uf.edex.bmh.status.BMHStatusHandler;
import com.raytheon.uf.edex.bmh.status.IBMHStatusHandler;

import voiceware.libttsapi;

/**
 * Performs the Text-To-Speech Synthesis.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 21, 2014 3538       bkowal      Initial creation
 * Aug 26, 2014 3559       bkowal      Notify the user of a configuration error when the
 *                                     synthesis validation fails due to an improperly
 *                                     set bmh tts nfs directory.
 *                                     Convert the audio length to the time that it would
 *                                     take to play the audio.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class TTSSynthesisTask implements Callable<TTSReturn> {
    private static final IBMHStatusHandler statusHandler = BMHStatusHandler
            .getInstance(TTSSynthesisTask.class);

    private final String PCM_EXTENSION = ".pcm";

    private final String identifier;

    private final String ttsServer;

    private final int ttsPort;

    private final int ttsConnectionTimeout;

    private final String ssml;

    private final int voice;

    private final int format;

    private final String ttsNfsDir;

    private String synthesisOutputFileName;

    /**
     * Constructor
     * 
     * @param ttsServer
     *            the server that the TTS Synthesizer is running on
     * @param ttsPort
     *            the port that should be used to connect to the server for
     *            synthesis
     * @param ttsConnectionTimeout
     *            the amount of time that the client has to connect to the TTS
     *            Server (in milliseconds)
     * @param ssml
     *            the text to synthesize
     * @param voice
     *            the voice that will be used during synthesis
     * @param format
     *            the output format of the data that is synthesized
     * @param ttsNfsDir
     *            the NeoSpeech data directory
     */
    public TTSSynthesisTask(final String ttsServer, final int ttsPort,
            final int ttsConnectionTimeout, final String ssml, final int voice,
            final int format, final String ttsNfsDir) {
        this.identifier = UUID.randomUUID().toString();
        this.ttsServer = ttsServer;
        this.ttsPort = ttsPort;
        this.ttsConnectionTimeout = ttsConnectionTimeout;
        this.ssml = ssml;
        this.voice = voice;
        this.format = format;
        this.ttsNfsDir = ttsNfsDir;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.concurrent.Callable#call()
     */
    @Override
    public TTSReturn call() throws Exception {
        long startTime = System.currentTimeMillis();
        this.generateSynthesisOutputFile();

        libttsapi ttsapi = new libttsapi();
        ttsapi.SetConnectTimeout(this.ttsConnectionTimeout);
        int returnCode = TTS_RETURN_VALUE.TTS_UNKNOWN_ERROR.getCode();
        returnCode = ttsapi.ttsRequestFileSSML(this.ttsServer, this.ttsPort,
                this.ssml, null, this.synthesisOutputFileName, this.voice,
                this.format);
        TTS_RETURN_VALUE returnValue = TTS_RETURN_VALUE.lookup(returnCode);
        TTSReturn ttsReturn = new TTSReturn(returnValue);
        if (returnValue != TTS_RETURN_VALUE.TTS_RESULT_SUCCESS) {
            /*
             * Failed to synthesize the data.
             */
            StringBuilder message = new StringBuilder("TTS Synthesizer ");
            message.append(this.identifier);
            message.append(" failed to synthesize the data in ");
            message.append(System.currentTimeMillis() - startTime);
            message.append(" ms.");
            statusHandler.info(message.toString());

            return ttsReturn;
        }

        /* Read the bytes from the generated file. */
        Path synthesisOutputFilePath = Paths.get(this.ttsNfsDir,
                this.synthesisOutputFileName + PCM_EXTENSION);
        /*
         * Indicate that synthesis is complete. Even if reading the raw byte
         * data fails (which is unlikely), the resource will still need to be
         * locked for the length of the audio duration before it can be used
         * again.
         */
        if (synthesisOutputFilePath.toFile().exists()) {
            ttsReturn.synthesisIsComplete(synthesisOutputFilePath.toFile()
                    .length());
        } else {
            ttsReturn = new TTSReturn(TTS_RETURN_VALUE.TTS_READWRITE_ERROR);
            /*
             * the synthesis output does not exist at the expected location.
             * Indicates a configuration problem, otherwise the TTS Server would
             * have failed with a file write or I/O error.
             */
            // synthesis was completed. but, we have no way to know the size of
            // the audio file. lock the synthesizer for two (2) minutes. This
            // error will be unlikely and the BMH EDEX will not completely start
            // until the cause is corrected.
            ttsReturn.synthesisIsComplete(960000);

            /*
             * Announce as a Configuration Error
             */
            StringBuilder message = new StringBuilder(
                    "Unable to find the TTS audio output file ");
            message.append(synthesisOutputFilePath.toString());
            message.append(". Please verify that the BMH NFS TTS Directory is set correctly in the configuration.");
            IOException exception = new IOException(message.toString());

            statusHandler.error(BMH_CATEGORY.TTS_CONFIGURATION_ERROR,
                    "Text Synthesis is Incomplete!", exception);
            ttsReturn.ioHasFailed(exception);

            return ttsReturn;
        }
        try {
            ttsReturn.setVoiceData(Files.readAllBytes(synthesisOutputFilePath));
        } catch (IOException e) {
            ttsReturn.ioHasFailed(e);
        } finally {
            synthesisOutputFilePath.toFile().delete();
        }

        StringBuilder message = new StringBuilder("TTS Synthesizer ");
        message.append(this.identifier);
        message.append(" successfully synthesized the data in ");
        message.append(System.currentTimeMillis() - startTime);
        message.append(" ms.");
        statusHandler.info(message.toString());

        return ttsReturn;
    }

    /**
     * Creates the name of the output file that the synthesized data will be
     * written to.
     */
    private void generateSynthesisOutputFile() {
        StringBuilder stringBuilder = new StringBuilder(this.voice);
        stringBuilder.append("_");
        stringBuilder.append(this.identifier);
        stringBuilder.append("_");
        stringBuilder.append(this.format);
        this.synthesisOutputFileName = stringBuilder.toString();
    }

    /**
     * @return the identifier
     */
    public String getIdentifier() {
        return identifier;
    }
}