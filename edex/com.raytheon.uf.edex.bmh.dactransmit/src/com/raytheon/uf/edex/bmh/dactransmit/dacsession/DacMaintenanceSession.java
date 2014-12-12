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
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.raytheon.uf.common.bmh.dac.dacsession.DacSessionConstants;
import com.raytheon.uf.edex.bmh.dactransmit.util.NamedThreadFactory;

/**
 * A Dac Session in maintenance mode will stream audio bytes read from an input
 * file. After all audio has been streamed, the session will shutdown. This
 * session does not connect to the comms manager or support live broadcasting
 * from Viz.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 6, 2014  3630       bkowal      Initial creation
 * Dec 12, 2014 3603       bsteffen    Allow negative duration of maintenance messages to run full audio.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class DacMaintenanceSession implements IDacSession {

    private static final Logger logger = LoggerFactory
            .getLogger(DacMaintenanceSession.class);

    private final DacMaintenanceConfig config;

    private final ExecutorService notificationExecutor;

    private final EventBus eventBus;

    private final ControlStatusThread controlThread;

    private final BroadcastTransmitThread transmitThread;

    private final byte[] originalMaintenanceAudio;

    /**
     * 
     */
    public DacMaintenanceSession(final DacMaintenanceConfig config)
            throws IOException {
        this.config = config;
        this.notificationExecutor = Executors
                .newSingleThreadExecutor(new NamedThreadFactory("EventBus"));
        this.eventBus = new AsyncEventBus("DAC-Maintenance",
                notificationExecutor);
        this.controlThread = new ControlStatusThread(this.eventBus,
                this.config.getDacAddress(), this.config.getControlPort());
        this.transmitThread = new BroadcastTransmitThread(
                "MaintenanceBroadcastTransmitThread", this.eventBus,
                this.config.getDacAddress(), this.config.getDataPort(),
                this.config.getTransmitters(), this.config.getDbTarget());
        this.originalMaintenanceAudio = Files.readAllBytes(this.config
                .getInputAudio());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.edex.bmh.dactransmit.dacsession.IDacSession#startPlayback
     * ()
     */
    @Override
    public void startPlayback() throws IOException {
        this.eventBus.register(this);

        logger.info("Running in MAINTENANCE MODE. Running in MAINTENANCE MODE. Running in MAINTENANCE MODE.");
        logger.info("Session configuration: " + config.toString());
        logger.info("Obtaining sync with DAC.");

        this.controlThread.performInitialSync();

        logger.info("Obtained sync with DAC and beginning transmission.");

        this.transmitThread.playAudio(this.segmentAudio());
        this.transmitThread.start();
        this.controlThread.start();
    }

    private List<byte[]> segmentAudio() {
        /*
         * Based on the requested duration, determine how many bits of audio are
         * required.
         */
        int requiredBytes = ((this.config.getTestDuration() * 1000) / 20) * 160;

        if (requiredBytes < 0) {
            /* Allow negative duration to be interpreted as full message. */
            requiredBytes = this.originalMaintenanceAudio.length;
        }

        /*
         * Stage the playback audio for segmentation.
         */
        ByteBuffer stagingBuffer = ByteBuffer.allocate(requiredBytes);
        /*
         * Determine if the audio that was read need to be sliced or replicated.
         */
        if (this.originalMaintenanceAudio.length >= requiredBytes) {
            stagingBuffer.put(this.originalMaintenanceAudio, 0, requiredBytes);
        } else {
            while (stagingBuffer.remaining() > 0) {
                if (stagingBuffer.remaining() >= this.originalMaintenanceAudio.length) {
                    stagingBuffer.put(this.originalMaintenanceAudio);
                } else {
                    stagingBuffer.put(this.originalMaintenanceAudio, 0,
                            stagingBuffer.remaining());
                }
            }
        }
        stagingBuffer.rewind();

        /*
         * Determine how many segments there will be. At this time, we believe
         * that there is no reason to check for partial segments based on the
         * fact that partial seconds are now allowed.
         */
        final int segmentCount = requiredBytes
                / DacSessionConstants.SINGLE_PAYLOAD_SIZE;
        logger.info("Preparing " + segmentCount + " packets of audio.");
        List<byte[]> segmentedAudio = new LinkedList<>();
        while (stagingBuffer.hasRemaining()) {
            byte[] segment = new byte[DacSessionConstants.SINGLE_PAYLOAD_SIZE];
            stagingBuffer.get(segment);
            segmentedAudio.add(segment);
        }

        return segmentedAudio;
    }

    private void shutdown() {
        logger.info("Initiating shutdown...");

        this.controlThread.shutdown();
        try {
            this.controlThread.join();
        } catch (InterruptedException e) {
            // Ignore. Shutting down.
        }

        this.notificationExecutor.shutdown();

        logger.info("Exiting MAINTENANCE MODE. Exiting MAINTENANCE MODE. Exiting MAINTENANCE MODE.");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.edex.bmh.dactransmit.dacsession.IDacSession#waitForShutdown
     * ()
     */
    @Override
    public SHUTDOWN_STATUS waitForShutdown() {
        try {
            this.transmitThread.join();
        } catch (InterruptedException e) {
            logger.warn("Interrupted while waiting for the data transmission thread to shutdown.");
        }

        this.shutdown();

        return this.transmitThread.isError() ? SHUTDOWN_STATUS.FAILURE
                : SHUTDOWN_STATUS.SUCCESS;
    }
}