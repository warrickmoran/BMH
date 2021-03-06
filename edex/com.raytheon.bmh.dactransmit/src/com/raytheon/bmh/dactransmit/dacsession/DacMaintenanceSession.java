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
package com.raytheon.bmh.dactransmit.dacsession;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
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
import com.raytheon.bmh.dactransmit.events.DacStatusUpdateEvent;
import com.raytheon.bmh.dactransmit.events.LostSyncEvent;
import com.raytheon.bmh.dactransmit.events.RegainSyncEvent;
import com.raytheon.bmh.dactransmit.events.handlers.IDacStatusUpdateEventHandler;
import com.raytheon.bmh.dactransmit.util.NamedThreadFactory;
import com.raytheon.uf.common.bmh.audio.AudioRegulationConfiguration;
import com.raytheon.uf.common.bmh.audio.AudioRegulationFactory;
import com.raytheon.uf.common.bmh.audio.IAudioRegulator;
import com.raytheon.uf.common.bmh.dac.dacsession.DacSessionConstants;
import com.raytheon.uf.common.bmh.dac.tones.TonesGenerator;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacMaintenanceMessage;
import com.raytheon.uf.common.bmh.notify.MaintenanceMessagePlayback;
import com.raytheon.uf.common.bmh.tones.ToneGenerationException;
import com.raytheon.uf.common.bmh.tones.TonesManager;
import com.raytheon.uf.common.bmh.tones.TonesManager.TransferType;
import com.raytheon.uf.common.bmh.trace.TraceableUtil;
import com.raytheon.uf.edex.bmh.audio.LoadedAudioRegulationConfiguration;
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
 * Apr 29, 2015 4394       bkowal      Connect to the Comms Manager. Submit a
 *                                     {@link MaintenanceMessagePlayback} when playback begins.
 * May 13, 2015 4429       rferrel     Changes for traceId logging.
 * Jun 11, 2015 4490       bkowal      Maintenance traceability improvements.
 * Jul 08, 2015 4636       bkowal      Support same and alert decibel levels.
 * Jul 13, 2015 4636       bkowal      Support separate 2.4K and 1.8K transfer tone types.
 * Jul 15, 2015 4636       bkowal      Alter audio before it is packaged into packets.
 * Aug 17, 2015 4757       bkowal      Relocated regulation to BMH common.
 * Aug 24, 2015 4770       bkowal      Utilize the {@link AudioRegulationConfiguration}.
 * Aug 25, 2015 4771       bkowal      Updated to use {@link IAudioRegulator}.
 * Nov 04, 2015 5068       rjpeter     Switch audio units from dB to amplitude.
 * Sep 30, 2016 5912       bkowal      Default SAME padding to 0.
 * </pre>
 * 
 * @author bkowal
 */

public class DacMaintenanceSession implements IDacSession,
        IBroadcastBufferListener, IDacStatusUpdateEventHandler {

    private static final Logger logger = LoggerFactory
            .getLogger(DacMaintenanceSession.class);

    private final DacMaintenanceConfig config;

    private final ExecutorService notificationExecutor;

    private final EventBus eventBus;

    private final ControlStatusThread controlThread;

    private final MaintenanceBroadcastTransmitThread transmitThread;

    private final DacMaintenanceMessage message;

    private byte[] audio;

    private final ScheduledThreadPoolExecutor reaperExecutor = new ScheduledThreadPoolExecutor(
            1);

    private final CommsManagerMaintenanceCommunicator commsManager;

    private volatile boolean initialSyncCompleted = false;

    private long playbackTime;

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
                this.config.getTransmitters(), this.config.getAudioAmplitude(),
                true);
        this.transmitThread.setListener(this);
        this.controlThread = new ControlStatusThread(this.transmitThread,
                this.config.getDacAddress(), this.config.getControlPort());

        /*
         * Read the message file.
         */
        Path messagePath = this.config.getMessageFilePath();
        try {
            message = JAXB.unmarshal(Files.newInputStream(messagePath),
                    DacMaintenanceMessage.class);
        } catch (Exception e) {
            /*
             * Specify the file that could not be loaded.
             */
            logger.error("Failed to unmarshal maintenance message file: {}.",
                    messagePath.toString());
            throw e;
        }

        message.setTraceId(messagePath.getFileName().toString());

        /*
         * Ensure that a duration has been specified for audio maintenance
         * messages.
         */
        if (this.message.isAudio() && this.config.getTestDuration() == null) {
            throw new Exception(
                    TraceableUtil.createTraceMsgHeader(message)
                            + "A duration must be specified to broadcast alignment test audio. Specify duration using the -d command line argument.");

        } else if (this.message.isTones()
                && this.config.getTestDuration() != null) {
            logger.info(TraceableUtil.createTraceMsgHeader(message)
                    + "Ignoring duration: {} for Tone Maintenance Messages.",
                    this.config.getTestDuration());
        }
        message.setPath(this.config.getMessageFilePath());

        /*
         * Determine if we will be reading or generating the maintenance audio.
         */
        boolean regulateAudio = true;
        if (this.message.isAudio()) {
            this.audio = Files.readAllBytes(Paths.get(this.message
                    .getSoundFile()));
        } else {
            /*
             * Determine what type of tones are required.
             */
            if (this.message.getSAMEtone() != null) {
                byte[] same = TonesGenerator
                        .getSAMEAlertTones(
                                this.message.getSAMEtone(),
                                false,
                                true,
                                config.getSamePaddingConfiguration()
                                        .getSamePadding()).combineTonesArray()
                        .array();
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

                byte[] endTones = TonesGenerator.getEndOfMessageTones(
                        config.getSamePaddingConfiguration()
                                .getSameEOMPadding()).array();
                ByteBuffer tonesAudioBuffer = ByteBuffer.allocate(same.length
                        + endTones.length);
                tonesAudioBuffer.put(same);
                tonesAudioBuffer.put(endTones);
                this.audio = tonesAudioBuffer.array();
            } else {
                this.audio = TonesManager.generateTransferTone(this.message
                        .getTransferToneType());

                byte[] segment1 = Arrays.copyOfRange(audio, 0,
                        (audio.length / 2) - 1);
                byte[] segment2 = Arrays.copyOfRange(audio, (audio.length / 2),
                        audio.length - 1);

                /*
                 * Depending on the transfer type, the 2400 audio bytes will
                 * either be at the end or the beginning. Default is correct for
                 * primary to secondary.
                 */
                short seg1Amplitude = this.config.getAudioAmplitude();
                short seg2Amplitude = this.config.getTransferAmplitude();
                if (this.message.getTransferToneType() == TransferType.SECONDARY_TO_PRIMARY) {
                    seg1Amplitude = this.config.getTransferAmplitude();
                    seg2Amplitude = this.config.getAudioAmplitude();
                }

                /*
                 * alter the audio based on transfer tones rules.
                 */
                final IAudioRegulator regulator = AudioRegulationFactory
                        .getAudioRegulator(LoadedAudioRegulationConfiguration
                                .getConfiguration());
                segment1 = regulator.regulateAudioVolume(segment1,
                        seg1Amplitude, segment1.length);
                segment2 = regulator.regulateAudioVolume(segment2,
                        seg2Amplitude, segment2.length);
                ByteBuffer regulatedAudio = ByteBuffer
                        .allocate(this.audio.length);
                regulatedAudio.put(segment1);
                regulatedAudio.put(segment2);
                this.audio = regulatedAudio.array();
                regulateAudio = false;
            }
        }

        if (regulateAudio) {
            final IAudioRegulator regulator = AudioRegulationFactory
                    .getAudioRegulator(LoadedAudioRegulationConfiguration
                            .getConfiguration());
            this.audio = regulator.regulateAudioVolume(this.audio,
                    this.config.getAudioAmplitude(), this.audio.length);
        }

        this.commsManager = new CommsManagerMaintenanceCommunicator(this,
                this.message.getTransmitterGroup());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.bmh.dactransmit.dacsession.IDacSession#startPlayback ()
     */
    @Override
    public void startPlayback() throws IOException {
        this.commsManager.start();
        this.eventBus.register(this);
        String header = TraceableUtil.createTraceMsgHeader(this.message);
        logger.info(header
                + "Running in MAINTENANCE MODE. Running in MAINTENANCE MODE. Running in MAINTENANCE MODE.");
        logger.info(header + "Session configuration: " + config.toString());

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

        this.commsManager
                .forwardPlaybackNotification(new MaintenanceMessagePlayback(
                        this.message, this.playbackTime));

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
         * Calculate the duration of the audio in ms.
         */
        playbackTime = (requiredBytes / 160) * 20;

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
        logger.info(TraceableUtil.createTraceMsgHeader(this.message)
                + "Preparing " + segmentCount + " packets of audio.");
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
        String header = TraceableUtil.createTraceMsgHeader(this.message);
        logger.info(header + "Initiating shutdown...");

        this.controlThread.shutdown();
        try {
            this.controlThread.join();
        } catch (InterruptedException e) {
            // Ignore. Shutting down.
        }

        this.notificationExecutor.shutdown();
        this.reaperExecutor.shutdown();
        this.commsManager.shutdown();

        /*
         * We have finished. Attempt to purge the maintenance message file.
         */
        logger.info(header + "Deleting maintenance message file: "
                + this.message.getPath() + " ...");
        try {
            Files.delete(this.message.getPath());
        } catch (IOException e) {
            logger.error(header + "Failed to delete maintenance message file: "
                    + this.message.getPath() + ".", e);
        }

        logger.info(header
                + "Exiting MAINTENANCE MODE. Exiting MAINTENANCE MODE. Exiting MAINTENANCE MODE.");
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.bmh.dactransmit.dacsession.IDacSession#waitForShutdown
     * ()
     */
    @Override
    public SHUTDOWN_STATUS waitForShutdown() {
        try {
            this.transmitThread.join();
        } catch (InterruptedException e) {
            logger.warn(TraceableUtil.createTraceMsgHeader(this.message)
                    + "Interrupted while waiting for the data transmission thread to shutdown.");
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
                        this.message, TONE_TYPE.SAME, this.message);
            } else if (packetCount == this.endTonesStartPacket) {
                /*
                 * End Tones have started.
                 */
                DefaultMessageLogger.getInstance().logMaintenanceTonesActivity(
                        this.message, TONE_TYPE.END, this.message);
            }
        } else {
            if (packetCount == 1) {
                /*
                 * Transfer tones have started.
                 */
                DefaultMessageLogger.getInstance().logMaintenanceTonesActivity(
                        this.message, TONE_TYPE.TRANSFER, this.message);
            }
        }
    }

    /**
     * @return the config
     */
    public DacMaintenanceConfig getConfig() {
        return config;
    }

    @Override
    public void receivedDacStatus(DacStatusUpdateEvent e) {
        // Ignore.
    }

    @Override
    public void lostDacSync(LostSyncEvent e) {
        commsManager.sendConnectionStatus(false);
    }

    @Override
    public void regainDacSync(RegainSyncEvent e) {
        commsManager.sendConnectionStatus(true);
    }
}