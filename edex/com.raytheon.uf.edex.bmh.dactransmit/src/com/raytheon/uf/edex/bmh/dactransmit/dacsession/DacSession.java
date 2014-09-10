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
import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.raytheon.uf.common.bmh.notify.DacHardwareStatusNotification;
import com.raytheon.uf.edex.bmh.dactransmit.events.DacStatusUpdateEvent;
import com.raytheon.uf.edex.bmh.dactransmit.events.LostSyncEvent;
import com.raytheon.uf.edex.bmh.dactransmit.events.RegainSyncEvent;
import com.raytheon.uf.edex.bmh.dactransmit.events.ShutdownRequestedEvent;
import com.raytheon.uf.edex.bmh.dactransmit.events.handlers.IDacStatusUpdateEventHandler;
import com.raytheon.uf.edex.bmh.dactransmit.events.handlers.IShutdownRequestEventHandler;
import com.raytheon.uf.edex.bmh.dactransmit.playlist.PlaylistDirectoryObserver;
import com.raytheon.uf.edex.bmh.dactransmit.playlist.PlaylistScheduler;
import com.raytheon.uf.edex.bmh.dactransmit.util.NamedThreadFactory;

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
 * Jul 24, 2014  #3286     dgilling     Fix NullPointerException in 
 *                                      waitForShutdown().
 * Jul 29, 2014  #3286     dgilling     Use NamedThreadFactory.
 * Jul 31, 2014  #3286     dgilling     Send DacHardwareStatusNotification back
 *                                      to CommsManager.
 * Aug 08, 2014  #3286     dgilling     Inform CommsManager when sync is lost
 *                                      or re-gained.
 * Aug 12, 2014  #3486     bsteffen     Remove tranmistter group name
 * Aug 18, 2014  #3532     bkowal       Add transmitter decibel range
 * Aug 26, 2014  #3286     dgilling     Make construction of playlist directory
 *                                      observer optional.
 * Sep 4, 2014   #3532     bkowal       Use a decibel target instead of a range.
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

    private final Semaphore shutdownSignal;

    private DacStatusMessage previousStatus;

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
        this.notificationExecutor = Executors
                .newSingleThreadExecutor(new NamedThreadFactory("EventBus"));
        this.eventBus = new AsyncEventBus("DAC-Transmit", notificationExecutor);
        this.playlistMgr = new PlaylistScheduler(
                this.config.getInputDirectory(), this.eventBus,
                this.config.getDbTarget());
        this.controlThread = new ControlStatusThread(this.eventBus,
                this.config.getDacAddress(), this.config.getControlPort());
        this.dataThread = new DataTransmitThread(this.eventBus, playlistMgr,
                this.config.getDacAddress(), this.config.getDataPort(),
                this.config.getTransmitters());
        this.commsManager = new CommsManagerCommunicator(this.config,
                this.eventBus);
        if (Boolean.getBoolean("enableDirectoryObserver")) {
            this.newPlaylistObserver = new PlaylistDirectoryObserver(
                    this.config.getInputDirectory(), this.eventBus);
        } else {
            this.newPlaylistObserver = null;
        }
        this.shutdownSignal = new Semaphore(1);
        this.previousStatus = null;
    }

    /**
     * Starts this transmission session with the DAC. Runs until it hits the end
     * of the audio playlist.
     * 
     * @throws IOException
     *             If a transmission error occurs with the DAC.
     */
    public void startPlayback() throws IOException {
        try {
            shutdownSignal.acquire();
        } catch (InterruptedException e) {
            // don't care
        }

        logger.info("Starting audio playback.");
        logger.info("Session configuration: " + config.toString());
        logger.info("Obtaining sync with DAC.");

        commsManager.start();
        eventBus.register(this);

        controlThread.performInitialSync();

        logger.info("Obtained sync with DAC and beginning transmission.");

        dataThread.start();
        controlThread.start();
        if (newPlaylistObserver != null) {
            newPlaylistObserver.start();
        }
        eventBus.register(playlistMgr);

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
        if (newPlaylistObserver != null) {
            newPlaylistObserver.shutdown();
        }
        dataThread.join();

        controlThread.shutdown();
        controlThread.join();

        notificationExecutor.shutdown();

        isRunning = false;

        shutdownSignal.release();
    }

    /**
     * Blocking method that allows the main thread to wait until the DacSession
     * has been shutdown.
     */
    public void waitForShutdown() {
        if (isRunning) {
            try {
                shutdownSignal.acquire();
            } catch (InterruptedException e) {
                logger.error(
                        "DacSession.waitForShutdown() interrupted by another thread.",
                        e);
            } finally {
                shutdownSignal.release();
            }
        }
    }

    @Override
    @Subscribe
    public void receivedDacStatus(DacStatusUpdateEvent e) {
        DacStatusMessage newStatus = e.getStatus();
        DacHardwareStatusNotification notify = newStatus.validateStatus(config,
                previousStatus);
        previousStatus = newStatus;
        commsManager.sendConnectionStatus(true);
        if (notify != null) {
            commsManager.sendDacHardwareStatus(notify);
        }
    }

    @Subscribe
    public void lostDacSync(LostSyncEvent e) {
        commsManager.sendConnectionStatus(false);
    }

    @Subscribe
    public void regainDacSync(RegainSyncEvent e) {
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
