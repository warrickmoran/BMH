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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.raytheon.uf.edex.bmh.dactransmit.events.DacStatusUpdateEvent;
import com.raytheon.uf.edex.bmh.dactransmit.events.ShutdownRequestedEvent;
import com.raytheon.uf.edex.bmh.dactransmit.events.handlers.IDacStatusUpdateEventHandler;
import com.raytheon.uf.edex.bmh.dactransmit.events.handlers.IShutdownRequestEventHandler;
import com.raytheon.uf.edex.bmh.dactransmit.playlist.PlaylistDirectoryObserver;
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
 * Jul 17, 2014  #3399     bsteffen     Add comms manager communication.
 * Jul 16, 2014  #3286     dgilling     Initial event bus implementation, add
 *                                      shutdown() method.
 * 
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

public final class DacSession implements IDacStatusUpdateEventHandler,
        IShutdownRequestEventHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final DacSessionConfig config;

    private final PlaylistScheduler playlistMgr;

    private final ExecutorService notificationExecutor;

    private final EventBus eventBus;

    private final DataTransmitThread dataThread;

    private final ControlStatusThread controlThread;

    private final PlaylistDirectoryObserver newPlaylistObserver;

    private volatile boolean isRunning;

    private CommsManagerCommunicator commsManager;

    private final Object shutdownSignal;

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
        this.notificationExecutor = Executors.newSingleThreadExecutor();
        this.eventBus = new AsyncEventBus("DAC-Transmit", notificationExecutor);
        this.playlistMgr = new PlaylistScheduler(
                this.config.getInputDirectory());
        this.controlThread = new ControlStatusThread(this.eventBus,
                this.config.getDacAddress(), this.config.getControlPort());
        this.dataThread = new DataTransmitThread(this.eventBus, playlistMgr,
                this.config.getDacAddress(), this.config.getDataPort(),
                this.config.getTransmitters());
        this.commsManager = new CommsManagerCommunicator(
                this.config.getManagerPort(),
                this.config.getTransmitterGroup(), this.eventBus);
        this.newPlaylistObserver = new PlaylistDirectoryObserver(
                this.config.getInputDirectory(), this.eventBus);
        this.shutdownSignal = new Object();
    }

    /**
     * Starts this transmission session with the DAC. Runs until it hits the end
     * of the audio playlist.
     * 
     * @throws IOException
     *             If a transmission error occurs with the DAC.
     */
    public void startPlayback() throws IOException {
        logger.info("Starting audio playback.");
        logger.info("Session configuration: " + config.toString());
        logger.info("Obtaining sync with DAC.");

        controlThread.performInitialSync();

        logger.info("Obtained sync with DAC and beginning transmission.");

        dataThread.start();
        controlThread.start();
        commsManager.start();
        newPlaylistObserver.start();
        eventBus.register(playlistMgr);
        eventBus.register(this);

        isRunning = true;
    }

    /**
     * Performs orderly shutdown of this {@code DacSession} instance. Will wait
     * for the current message being transmitted to finish before completing.
     * After calling this method, the instance cannot be restarted; a new one
     * must be created.
     * 
     * @throws InterruptedException
     *             If any thread interrupts the current thread while waiting for
     *             all the child threads to die.
     */
    public void shutdown() throws InterruptedException {
        logger.info("Intiating shutdown...");

        dataThread.shutdown();
        playlistMgr.shutdown();
        newPlaylistObserver.shutdown();
        dataThread.join();

        controlThread.shutdown();
        controlThread.join();

        notificationExecutor.shutdown();

        isRunning = false;

        shutdownSignal.notifyAll();
    }

    /**
     * TODO
     */
    public void waitForShutdown() {
        if (isRunning) {
            try {
                shutdownSignal.wait();
            } catch (InterruptedException e) {
                logger.error(
                        "DacSession.waitForShutdown() interrupted by another thread.",
                        e);
            }
        }
    }

    @Override
    @Subscribe
    public void receivedDacStatus(DacStatusUpdateEvent e) {
        // TODO: Is there any messages that come out of validateStatus() that
        // we should simply make part of the message that goes back to the
        // CommsManager?
        e.getStatus().validateStatus(config);
        commsManager.sendConnectionStatus(true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.edex.bmh.dactransmit.events.handlers.
     * IShutdownRequestEventHandler
     * #handleShutdownRequest(com.raytheon.uf.edex.bmh
     * .dactransmit.events.ShutdownRequestedEvent)
     */
    @Override
    @Subscribe
    public void handleShutdownRequest(ShutdownRequestedEvent e) {
        try {
            shutdown();
        } catch (InterruptedException e1) {
            logger.error("Shutdown interrupted.", e1);
        }
    }
}
