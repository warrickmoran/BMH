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
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.JAXB;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.raytheon.uf.common.bmh.dac.dacsession.DacSessionConstants;
import com.raytheon.uf.common.bmh.dac.tones.TonesGenerator;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacMaintenanceMessage;
import com.raytheon.uf.common.bmh.tones.ToneGenerationException;
import com.raytheon.uf.common.bmh.tones.TonesManager;
import com.raytheon.uf.edex.bmh.dactransmit.util.NamedThreadFactory;
import com.raytheon.uf.edex.bmh.msg.logging.DefaultMessageLogger;
import com.raytheon.uf.edex.bmh.msg.logging.IMessageLogger.TONE_TYPE;

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
 * Jan 19, 2015 3912       bsteffen    Use new control thread constructor
 * Apr 09, 2015 4364       bkowal      Utilize the {@link DacMaintenanceReaper}.
 * Apr 16, 2015 4405       rjpeter     Initialize isSync'd.
 * Apr 24, 2015 4394       bkowal      Updated to use {@link DacMaintenanceMessage}. Log tone
 *                                     playback.
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class DacMaintenanceSession implements IDacSession,
        IBroadcastBufferListener {

    private static final Logger logger = LoggerFactory
            .getLogger(DacMaintenanceSession.class);

    private final DacMaintenanceConfig config;

    private final ExecutorService notificationExecutor;

    private final EventBus eventBus;

    private final ControlStatusThread controlThread;

    private final MaintenanceBroadcastTransmitThread transmitThread;

    private final DacMaintenanceMessage message;

    private final byte[] audio;

    private final ScheduledThreadPoolExecutor reaperExecutor = new ScheduledThreadPoolExecutor(
            1);

    private volatile boolean initialSyncCompleted = false;

    /*
     * Scenario-specific. Used to fulfill the tones logging requirement. Keeps
     * track of the first packet that contains end tones.
     */
    private int endTonesStartPacket;

    /**
     * @throws ToneGenerationException
     * 
     */
    public DacMaintenanceSession(final DacMaintenanceConfig config)
            throws Exception {
        this.config = config;
        this.notificationExecutor = Executors
                .newSingleThreadExecutor(new NamedThreadFactory("EventBus"));
        this.eventBus = new AsyncEventBus("DAC-Maintenance",
                notificationExecutor);
        this.transmitThread = new MaintenanceBroadcastTransmitThread(
                "MaintenanceBroadcastTransmitThread", this.eventBus,
                this.config.getDacAddress(), this.config.getDataPort(),
                this.config.getTransmitters(), this.config.getDbTarget(), true);
        this.transmitThread.setListener(this);
        this.controlThread = new ControlStatusThread(this.transmitThread,
                this.config.getDacAddress(), this.config.getControlPort());

        /*
         * Read the message file.
         */
        message = JAXB.unmarshal(
                Files.newInputStream(this.config.getMessageFilePath()),
                DacMaintenanceMessage.class);
        /*
         * Ensure that a duration has been specified for audio maintenance
         * messages.
         */
        if (this.message.isAudio() && this.config.getTestDuration() == null) {
            throw new Exception(
                    "A duration must be specified to broadcast alignment test audio. Specify duration using the -d command line argument.");

        } else if (this.message.isTones()
                && this.config.getTestDuration() != null) {
            logger.info("Ignoring duration: {} for Tone Maintenance Messages.",
                    this.config.getTestDuration());
        }
        message.setPath(this.config.getMessageFilePath());

        /*
         * Determine if we will be reading or generating the maintenance audio.
         */
        if (this.message.isAudio()) {
            this.audio = Files.readAllBytes(Paths.get(this.message
                    .getSoundFile()));
        } else {
            /*
             * Determine what type of tones are required.
             */
            if (this.message.getSAMEtone() != null) {
                byte[] same = TonesGenerator.getSAMEAlertTones(
                        this.message.getSAMEtone(), false, true).array();
                /*
                 * Based on the length of the tones, determine which packet
                 * would mark the beginning of the end tones.
                 */
                int endPacket = same.length
                        / DacSessionConstants.SINGLE_PAYLOAD_SIZE;
                /*
                 * will the SAME tones be within the packet or in the next
                 * packet.
                 */
                if (same.length % DacSessionConstants.SINGLE_PAYLOAD_SIZE == 0) {
                    /*
                     * next packet.
                     */
                    ++endPacket;
                }
                this.endTonesStartPacket = endPacket;

                byte[] endTones = TonesGenerator.getEndOfMessageTones().array();
                ByteBuffer tonesAudioBuffer = ByteBuffer.allocate(same.length
                        + endTones.length);
                tonesAudioBuffer.put(same);
                tonesAudioBuffer.put(endTones);
                this.audio = tonesAudioBuffer.array();
            } else {
                this.audio = TonesManager.generateTransferTone(this.message
                        .getTransferToneType());
            }
        }
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

        /*
         * Initialize the reaper to ensure that the process will not run forever
         * if any part of the initialization fails.
         */
        DacMaintenanceReaper reaper = new DacMaintenanceReaper(this,
                this.transmitThread, this.controlThread);

        /*
         * Determine the total number of packets that will need to be broadcast.
         */
        this.transmitThread.playAudio(this.segmentAudio());

        this.reaperExecutor.schedule(reaper, this.config.getExecutionTimeout(),
                TimeUnit.MINUTES);

        this.controlThread.performInitialSync();

        this.initialSyncCompleted = true;

        this.transmitThread.start();
        this.controlThread.start();
    }

    private List<byte[]> segmentAudio() {
        /*
         * Based on the requested duration, determine how many bits of audio are
         * required.
         */
        final int requiredBytes = (this.message.isAudio()) ? ((this.config
                .getTestDuration() * 1000) / 20) * 160 : this.audio.length;

        /*
         * Stage the playback audio for segmentation.
         */
        ByteBuffer stagingBuffer = ByteBuffer.allocate(requiredBytes);
        /*
         * Determine if the audio that was read need to be sliced or replicated.
         */
        if (this.audio.length >= requiredBytes) {
            stagingBuffer.put(this.audio, 0, requiredBytes);
        } else {
            while (stagingBuffer.remaining() > 0) {
                if (stagingBuffer.remaining() >= this.audio.length) {
                    stagingBuffer.put(this.audio);
                } else {
                    stagingBuffer.put(this.audio, 0, stagingBuffer.remaining());
                }
            }
        }
        stagingBuffer.rewind();

        /*
         * Determine how many segments there will be.
         */
        final int segmentCount = requiredBytes
                / DacSessionConstants.SINGLE_PAYLOAD_SIZE;
        logger.info("Preparing " + segmentCount + " packets of audio.");
        List<byte[]> segmentedAudio = new LinkedList<>();
        while (stagingBuffer.hasRemaining()) {
            byte[] segment = new byte[DacSessionConstants.SINGLE_PAYLOAD_SIZE];
            if (stagingBuffer.remaining() < segment.length) {
                Arrays.fill(segment, stagingBuffer.remaining(), segment.length,
                        DacSessionConstants.SILENCE);
                stagingBuffer.get(segment, 0, stagingBuffer.remaining());
            } else {
                stagingBuffer.get(segment);
            }
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
        this.reaperExecutor.shutdown();

        /*
         * We have finished. Attempt to purge the maintenance message file.
         */
        logger.info("Deleting maintenance message file: "
                + this.message.getPath() + " ...");
        try {
            Files.delete(this.message.getPath());
        } catch (IOException e) {
            logger.error("Failed to delete maintenance message file: "
                    + this.message.getPath() + ".", e);
        }

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

    /**
     * @return the initialSyncCompleted
     */
    public boolean isInitialSyncCompleted() {
        return initialSyncCompleted;
    }

    @Override
    public void packetStreamed(int packetCount) {
        /*
         * If tones are not included in the broadcast, we do not care.
         */
        if (this.message.isAudio()) {
            return;
        }

        /*
         * Complete the tone logging requirement.
         */
        if (this.message.getSAMEtone() != null) {
            if (packetCount == 1) {
                /*
                 * SAME Tones have started.
                 */
                DefaultMessageLogger.getInstance().logMaintenanceTonesActivity(
                        TONE_TYPE.SAME, this.message);
            } else if (packetCount == this.endTonesStartPacket) {
                /*
                 * End Tones have started.
                 */
                DefaultMessageLogger.getInstance().logMaintenanceTonesActivity(
                        TONE_TYPE.END, this.message);
            }
        } else {
            if (packetCount == 1) {
                /*
                 * Transfer tones have started.
                 */
                DefaultMessageLogger.getInstance().logMaintenanceTonesActivity(
                        TONE_TYPE.TRANSFER, this.message);
            }
        }
    }
}