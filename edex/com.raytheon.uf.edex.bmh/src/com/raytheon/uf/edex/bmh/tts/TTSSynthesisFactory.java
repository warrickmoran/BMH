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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.raytheon.uf.common.bmh.TTSSynthesisException;
import com.raytheon.uf.common.bmh.TTSConstants.TTS_FORMAT;
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
 * Jan 27, 2015 4026       bkowal      Removed validateServerAvailability.
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
            final int ttsSynthesisPort, final int ttsConnectionTimeout,
            final String ttsNfsDir, final int maxConnections) {
        this.ttsServer = ttsServer;
        this.ttsSynthesisPort = ttsSynthesisPort;
        this.ttsConnectionTimeout = ttsConnectionTimeout;
        this.ttsNfsDir = ttsNfsDir;
        this.executorService = MoreExecutors.listeningDecorator(Executors
                .newFixedThreadPool(maxConnections * 2));
        this.resourceCounter = new Semaphore(maxConnections, true);
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
}