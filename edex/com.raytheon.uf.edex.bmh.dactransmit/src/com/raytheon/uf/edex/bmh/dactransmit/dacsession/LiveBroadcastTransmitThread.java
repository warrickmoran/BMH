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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.google.common.eventbus.EventBus;
import com.raytheon.uf.common.bmh.audio.AudioConversionException;
import com.raytheon.uf.common.bmh.audio.AudioPacketLogger;
import com.raytheon.uf.common.bmh.audio.UnsupportedAudioFormatException;
import com.raytheon.uf.common.bmh.broadcast.BroadcastStatus;
import com.raytheon.uf.common.bmh.broadcast.BroadcastTransmitterConfiguration;
import com.raytheon.uf.common.bmh.broadcast.ILiveBroadcastMessage;
import com.raytheon.uf.common.bmh.dac.dacsession.DacSessionConstants;
import com.raytheon.uf.common.bmh.notify.LiveBroadcastSwitchNotification;
import com.raytheon.uf.common.bmh.notify.LiveBroadcastSwitchNotification.STATE;
import com.raytheon.uf.common.time.util.ITimer;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.edex.bmh.audio.AudioOverflowException;
import com.raytheon.uf.edex.bmh.audio.AudioRegulator;
import com.raytheon.uf.edex.bmh.dactransmit.rtp.RtpPacketIn;

/**
 * Transmits audio from a live data source (rather than a pre-recorded data
 * source) to the DAC.
 * 
 * TODO: may want to add a monitor thread that will cause a failover to the data
 * thread if the broadcast thread runs without receiving data for some time X.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 14, 2014 3655       bkowal      Initial creation
 * Oct 17, 2014 3655       bkowal      Move tones to common.
 * Oct 21, 2014 3655       bkowal      Support tone playback prior to the live
 *                                     broadcast.
 * Oct 21, 2014 3655       bkowal      Broadcast a LiveBroadcastSwitchNotification
 *                                     before the live stream begins.
 * Oct 27, 2014 3712       bkowal      Broadcast a LiveBroadcastSwitchNotification
 *                                     at the conclusion of the live stream.
 * Oct 29, 2014 3774       bsteffen    Log Packets
 * Nov 1, 2014  3655       bkowal      Play end of message tones at the end of a live
 *                                     broadcast.
 * Nov 3, 2014  3655       bkowal      Viz now caches the audio. Adjusted timeout between
 *                                     packet transmits based on the rate that audio arrives.
 * Nov 4, 2014  3655       bkowal      Eliminate audio echo. Decrease buffer delay.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class LiveBroadcastTransmitThread extends AbstractTransmitThread {

    private final String broadcastId;

    private final DataTransmitThread dataThread;

    private final CommsManagerCommunicator commsManager;

    private final BroadcastTransmitterConfiguration config;

    private final double dbTarget;

    private volatile boolean error;

    private ITimer transmitTimer;

    private volatile boolean streaming;

    private LinkedBlockingQueue<byte[]> audioBuffer = new LinkedBlockingQueue<>();

    public LiveBroadcastTransmitThread(final EventBus eventBus,
            final InetAddress address, final int port,
            final Collection<Integer> transmitters, final String broadcastId,
            final DataTransmitThread dataThread,
            final CommsManagerCommunicator commsManager,
            final BroadcastTransmitterConfiguration config,
            final double dbTarget) throws IOException {
        super("LiveBroadcastTransmitThread", eventBus, address, port,
                transmitters);
        this.broadcastId = broadcastId;
        this.dataThread = dataThread;
        this.commsManager = commsManager;
        this.config = config;
        this.dbTarget = dbTarget;
    }

    @Override
    public void run() {
        if (config.getDelayMilliseconds() > 0) {
            try {
                Thread.sleep(config.getDelayMilliseconds());
            } catch (InterruptedException e) {
                logger.warn(
                        "LiveBroadcastTransmitThread sleep was interrupted.", e);
            }
        }

        this.dataThread.pausePlayback();

        // Build playlist switch notification
        this.notifyBroadcastSwitch(STATE.STARTED);
        // play the Alert / SAME tones.
        this.playTones(this.config.getToneAudio(), "SAME / Alert");

        BroadcastStatus status = new BroadcastStatus();
        status.setMsgSource(ILiveBroadcastMessage.SOURCE_DAC_TRANSMIT);
        status.setStatus(true);
        status.setBroadcastId(this.broadcastId);
        status.addTransmitter(this.config.getTransmitter());
        this.commsManager.sendDacLiveBroadcastMsg(status);
        this.streaming = true;
        AudioPacketLogger packetLog = new AudioPacketLogger(
                "Live Broadcast Audio", getClass(), 30);
        this.transmitTimer = null;
        /*
         * end the broadcast only after all buffered audio has been streamed.
         */
        while (streaming || this.audioBuffer.isEmpty() == false) {
            try {
                // check for data every 5ms, we only have a 20ms window.
                // 0 - 5 ms delay between end of audio and end of the broadcast.
                byte[] audio = this.audioBuffer.poll(5, TimeUnit.MILLISECONDS);
                if (audio == null) {
                    continue;
                }
                this.streamAudio(audio);
                packetLog.packetProcessed();
            } catch (AudioOverflowException | UnsupportedAudioFormatException
                    | AudioConversionException | InterruptedException e) {
                this.notifyDacError(
                        "Failed to stream the buffered live audio.", e);
            }
        }
        packetLog.close();

        this.playTones(this.config.getEndToneAudio(), "End of Message");
        // give the tones enough time to finish.
        long duration = ((this.config.getEndToneAudio().length / 160) * 20) + 1;
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            logger.warn("LiveBroadcastTransmitThread sleep was interrupted.", e);
        }
        this.notifyBroadcastSwitch(STATE.FINISHED);

        this.dataThread.resumePlayback();
    }

    private void playTones(byte[] toneAudio, final String toneType) {
        AudioPacketLogger packetLog = new AudioPacketLogger(
                "Live Broadcast Tones", getClass(), 30);

        ByteArrayInputStream tonesInputStream = new ByteArrayInputStream(
                toneAudio);
        byte[] nextPayload = new byte[DacSessionConstants.SINGLE_PAYLOAD_SIZE];
        try {
            while (tonesInputStream.read(nextPayload) != -1) {
                this.streamAudio(nextPayload);
                packetLog.packetProcessed();
            }
        } catch (IOException | AudioOverflowException
                | UnsupportedAudioFormatException | AudioConversionException
                | InterruptedException e) {
            this.notifyDacError("Failed to stream the " + toneType + " Tones!",
                    e);
        }
    }

    public void playAudio(List<byte[]> data) {
        this.audioBuffer.addAll(data);
    }

    private void streamAudio(byte[] data) throws AudioOverflowException,
            UnsupportedAudioFormatException, AudioConversionException,
            InterruptedException {
        /*
         * Adjust the audio based on the decibel target.
         */
        byte[] regulatedAudio = this.adjustAudio(data);

        RtpPacketIn rtpPacket = buildRtpPacket(previousPacket, regulatedAudio);

        sendPacket(rtpPacket);
        if (this.transmitTimer == null) {
            this.transmitTimer = TimeUtil.getTimer();
            this.transmitTimer.start();
        } else {
            this.transmitTimer.stop();
            logger.info(
                    "A total of {} elapsed between the transmission of the current packet and the previous packet.",
                    TimeUtil.prettyDuration(this.transmitTimer.getElapsedTime()));
            this.transmitTimer.reset();
            this.transmitTimer.start();
        }

        previousPacket = rtpPacket;

        /*
         * Data (multiple packets) arrives in realtime after an initial buffer
         * is saved up.
         */
        Thread.sleep(DataTransmitConstants.DEFAULT_CYCLE_TIME);

        while (!hasSync) {
            Thread.sleep(DataTransmitConstants.DEFAULT_CYCLE_TIME);

            // cannot restart audio. should 'onSyncRestartMessage'
            // indicate an error condition in the case of live
            // broadcasting?
            if (hasSync && onSyncRestartMessage) {
                logger.warn("Application has re-gained sync with the DAC. Unable to restart audio stream!");
            }
        }
    }

    private void notifyDacError(final String detail, final Exception e) {
        this.error = true;
        logger.error(detail, e);
        BroadcastStatus status = new BroadcastStatus();
        status.setMsgSource(ILiveBroadcastMessage.SOURCE_DAC_TRANSMIT);
        status.setStatus(false);
        status.setBroadcastId(this.broadcastId);
        status.addTransmitter(this.config.getTransmitter());
        status.setMessage(detail);
        status.setException(e);
        this.commsManager.sendDacLiveBroadcastMsg(status);
    }

    private byte[] adjustAudio(final byte[] sourceAudio)
            throws AudioOverflowException, UnsupportedAudioFormatException,
            AudioConversionException {
        byte[] regulatedAudio = new byte[0];

        AudioRegulator audioRegulator = new AudioRegulator();
        regulatedAudio = audioRegulator.regulateAudioVolume(sourceAudio,
                this.dbTarget, sourceAudio.length);
        logger.info("Successfully finished audio attenuation/amplification in "
                + audioRegulator.getDuration()
                + " ms for message: 'Live Audio Stream'");

        return regulatedAudio;
    }

    public void shutdown() {
        this.streaming = false;
    }

    /**
     * @param watermarkPackets
     *            the watermarkPackets to set
     * 
     *            Just a set method for now ... waiting to see what the source
     *            of the value will be.
     */
    public void setWatermarkPackets(int watermarkPackets) {
        this.watermarkPackets = watermarkPackets;
    }

    /**
     * @return the error
     */
    public boolean isError() {
        return error;
    }

    private void notifyBroadcastSwitch(final STATE broadcastState) {
        LiveBroadcastSwitchNotification notification = new LiveBroadcastSwitchNotification();
        notification.setBroadcastState(broadcastState);
        notification.setTransmitterGroup(this.config.getTransmitter()
                .getMnemonic());
        notification.setMessageType(this.config.getSelectedMessageType());
        notification.setTransitTime(this.config.getEffectiveTime());
        notification.setExpirationTime(this.config.getExpireTime());
        notification.setSameTone(true);
        notification.setAlertTone(this.config.isPlayAlertTones());
        eventBus.post(notification);
    }
}