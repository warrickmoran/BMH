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
package com.raytheon.uf.edex.bmh.dactransmit.dacsession;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import com.raytheon.uf.edex.bmh.dactransmit.playlist.PlaylistScheduler;

/**
 * Manages a transmission session to the DAC. Class pre-buffers all audio data
 * that will be sent to the DAC, and then fires 2 threads to manage the session.
 * First thread simply sends the audio in RTP-like packets until files have been
 * played. Second thread manages the control/status channel and maintains a sync
 * with the DAC and sends heartbeat messages to keep the connection alive.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 01, 2014  #3286     dgilling     Initial creation
 * Jul 14, 2014  #3286     dgilling     Switched to PlaylistScheduler to feed
 *                                      audio data to DataTransmitThread.
 * 
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

public final class DacSession {

    private final DacSessionConfig config;

    private final PlaylistScheduler playlistMgr;

    private final ExecutorService notificationExecutor;

    private final AtomicInteger bufferSize;

    /**
     * Constructor for the {@code DacSession} class. Reads the input directory
     * and buffers all audio files found.
     * 
     * @param config
     *            The paramters for this session.
     * @throws IOException
     *             If the specified input directory could not be read.
     */
    public DacSession(final DacSessionConfig config) throws IOException {
        this.config = config;
        this.playlistMgr = new PlaylistScheduler(config.getInputDirectory());
        this.notificationExecutor = Executors.newSingleThreadExecutor();
        this.bufferSize = new AtomicInteger(
                DataTransmitConstants.UNKNOWN_BUFFER_SIZE);
    }

    /**
     * Starts this transmission session with the DAC. Runs until it hits the end
     * of the audio playlist.
     * 
     * @throws IOException
     *             If a transmission error occurs with the DAC.
     * @throws InterruptedException
     *             If any thread has interrupted the current thread.
     */
    public void startPlayback() throws IOException, InterruptedException {
        DataTransmitThread dataThread = new DataTransmitThread(this,
                playlistMgr, config.getDacAddress(), config.getDataPort(),
                config.getTransmitters());

        ControlStatusThread controlThread = new ControlStatusThread(this,
                config.getDacAddress(), config.getControlPort());
        controlThread.performInitialSync();

        dataThread.start();
        controlThread.start();

        dataThread.join();
        notificationExecutor.shutdown();
    }

    public void receivedDacStatus(final DacStatusMessage dacStatus) {
        Runnable validateJob = new Runnable() {

            @Override
            public void run() {
                bufferSize.set(dacStatus.getBufferSize());
                dacStatus.validateStatus(config);
            }
        };
        try {
            notificationExecutor.submit(validateJob);
        } catch (RejectedExecutionException e) {
            // tried to submit during shutdown, ignoring...
        }
    }

    public int getCurrentBufferSize() {
        return bufferSize.get();
    }
}