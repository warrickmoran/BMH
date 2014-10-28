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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import voiceware.libttsapi;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.raytheon.uf.common.bmh.TTSSynthesisException;
import com.raytheon.uf.common.bmh.TTSConstants.TTS_FORMAT;
import com.raytheon.uf.common.bmh.TTSConstants.TTS_RETURN_VALUE;
import com.raytheon.uf.edex.bmh.status.BMHStatusHandler;
import com.raytheon.uf.edex.bmh.status.IBMHStatusHandler;

/**
 * Manages TTS Synthesis operations.
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
 * Oct 2, 2014  3642       bkowal      Made NO_TIMEOUT public.
 * Oct 28, 2014 3759       bkowal      Removed extended TTS Synthesis lockout.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class TTSSynthesisFactory {

    private static final IBMHStatusHandler statusHandler = BMHStatusHandler
            .getInstance(TTSSynthesisFactory.class);

    public static final int NO_TIMEOUT = -1;

    private final String ttsServer;

    private final int ttsSynthesisPort;

    private final int ttsStatusPort;

    private final int ttsConnectionTimeout;

    private final String ttsNfsDir;

    private ListeningExecutorService executorService;

    private final Semaphore resourceCounter;

    /**
     * Constructor
     * 
     * @param ttsServer
     *            the server that the TTS Synthesizer is running on
     * @param ttsSynthesisPort
     *            the port that should be used to connect to the server for
     *            synthesis
     * @param ttsStatusPort
     *            the port that should be used to connect to the server for
     *            status information
     * @param ttsConnectionTimeout
     *            the amount of time that the client has to connect to the TTS
     *            Server (in milliseconds)
     * @param ttsNfsDir
     *            the NeoSpeech data directory
     * @param maxConnections
     *            the total number of synthesizers that are available
     */
    public TTSSynthesisFactory(final String ttsServer,
            final int ttsSynthesisPort, final int ttsStatusPort,
            final int ttsConnectionTimeout, final String ttsNfsDir,
            final int maxConnections) {
        this.ttsServer = ttsServer;
        this.ttsSynthesisPort = ttsSynthesisPort;
        this.ttsStatusPort = ttsStatusPort;
        this.ttsConnectionTimeout = ttsConnectionTimeout;
        this.ttsNfsDir = ttsNfsDir;
        this.executorService = MoreExecutors.listeningDecorator(Executors
                .newFixedThreadPool(maxConnections * 2));
        this.resourceCounter = new Semaphore(maxConnections, true);
    }

    /**
     * Verifies that the TTS Server is running.
     * 
     * @return a status indicating whether or not the TTS Server is running.
     * @throws IOException
     */
    public TTS_RETURN_VALUE validateServerAvailability() throws IOException {
        final libttsapi ttsapi = new libttsapi();
        int returnCode = ttsapi.ttsRequestStatus(this.ttsServer,
                this.ttsStatusPort);
        /*
         * The return codes associated with the tts status request are slightly
         * different than the return codes associated with other types of tts
         * requests. So, the return codes need to be translated to what is used
         * throughout the TTS Management Capability.
         */
        switch (returnCode) {
        case libttsapi.TTS_SERVICE_OFF:
        case libttsapi.TTS_SERVICE_PAUSED:
            return TTS_RETURN_VALUE.TTS_RESULT_ERROR;
        case libttsapi.TTS_SERVICE_ON:
            return TTS_RETURN_VALUE.TTS_RESULT_SUCCESS;
        default:
            // indicates < 0; one of the standard TTS error codes
            return TTS_RETURN_VALUE.lookup(returnCode);
        }
    }

    /**
     * Schedules a task that will perform the actual synthesis. Returns the
     * result of the synthesis attempt.
     * 
     * @param ssml
     *            the text to synthesize
     * @param voice
     *            the voice that will be used during synthesis
     * @param ttsFormat
     *            the output format of the data that is synthesized
     * @return the result of the synthesis attempt
     * @throws TTSSynthesisException
     */
    public TTSReturn synthesize(final String ssml, final int voice,
            final TTS_FORMAT ttsFormat) throws TTSSynthesisException {
        return this.synthesize(ssml, voice, ttsFormat, NO_TIMEOUT);
    }

    /**
     * Schedules a task that will perform the actual synthesis. Returns the
     * result of the synthesis attempt.
     * 
     * @param ssml
     *            the text to synthesize
     * @param voice
     *            the voice that will be used during synthesis
     * @param ttsFormat
     *            the output format of the data that is synthesized
     * @param timeout
     *            the maximum amount of time to wait for a synthesizer to become
     *            available (in milliseconds)
     * @return the result of the synthesis attempt
     * @throws TTSSynthesisException
     */
    public TTSReturn synthesize(final String ssml, final int voice,
            final TTS_FORMAT ttsFormat, final int timeout)
            throws TTSSynthesisException {
        /* Wait for a resource to be available. */
        try {
            if (timeout == NO_TIMEOUT) {
                this.resourceCounter.acquire();
            } else {
                boolean acquired = this.resourceCounter.tryAcquire(timeout,
                        TimeUnit.MILLISECONDS);
                if (acquired == false) {
                    throw new TTSSynthesisException(
                            "Failed to acquire a synthesizer before the allotted time!");
                }
            }
        } catch (InterruptedException e) {
            throw new TTSSynthesisException(
                    "Interrupted while trying to acquire a synthesizer!", e);
        }

        TTSSynthesisTask task = new TTSSynthesisTask(this.ttsServer,
                this.ttsSynthesisPort, this.ttsConnectionTimeout, ssml, voice,
                ttsFormat.getCode(), this.ttsNfsDir);
        statusHandler.info("Using TTS Synthesizer: " + task.getIdentifier()
                + ".");
        Future<TTSReturn> future = this.executorService.submit(task);
        TTSReturn ttsReturn = null;
        try {
            ttsReturn = future.get();
        } catch (InterruptedException e) {
            throw new TTSSynthesisException(
                    "Interrupted during the synthesis process!", e);
        } catch (ExecutionException e) {
            throw new TTSSynthesisException("TTS synthesis failed!", e);
        } finally {
            /*
             * unlock the resource.
             */
            this.resourceCounter.release();
        }

        return ttsReturn;
    }

    /**
     * @return the ttsServer
     */
    public String getTtsServer() {
        return ttsServer;
    }

    /**
     * @return the ttsSynthesisPort
     */
    public int getTtsSynthesisPort() {
        return ttsSynthesisPort;
    }

    /**
     * @return the ttsStatusPort
     */
    public int getTtsStatusPort() {
        return ttsStatusPort;
    }
}