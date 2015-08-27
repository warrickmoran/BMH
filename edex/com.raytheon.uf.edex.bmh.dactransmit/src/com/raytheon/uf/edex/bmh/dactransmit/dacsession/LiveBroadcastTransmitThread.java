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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.common.eventbus.EventBus;
import com.raytheon.uf.common.bmh.audio.AudioConversionException;
import com.raytheon.uf.common.bmh.audio.AudioOverflowException;
import com.raytheon.uf.common.bmh.audio.AudioPacketLogger;
import com.raytheon.uf.common.bmh.audio.AudioRegulator;
import com.raytheon.uf.common.bmh.audio.CollectibleAudioRegulator;
import com.raytheon.uf.common.bmh.audio.UnsupportedAudioFormatException;
import com.raytheon.uf.common.bmh.broadcast.BroadcastStatus;
import com.raytheon.uf.common.bmh.broadcast.BroadcastTransmitterConfiguration;
import com.raytheon.uf.common.bmh.broadcast.LiveBroadcastStartCommand.BROADCASTTYPE;
import com.raytheon.uf.common.bmh.broadcast.OnDemandBroadcastConstants.MSGSOURCE;
import com.raytheon.uf.common.bmh.dac.dacsession.DacSessionConstants;
import com.raytheon.uf.common.bmh.notify.LiveBroadcastSwitchNotification;
import com.raytheon.uf.common.bmh.notify.LiveBroadcastSwitchNotification.STATE;
import com.raytheon.uf.common.bmh.stats.LiveBroadcastLatencyEvent;
import com.raytheon.uf.common.time.util.TimeUtil;

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
 * Nov 7, 2014  3630       bkowal      Refactor for maintenance mode.
 * Nov 10, 2014 3630       bkowal      Re-factor to support on-demand broadcasting.
 * Nov 17, 2014 3808       bkowal      Support broadcast live. Initial transition to
 *                                     transmitter group.
 * Nov 21, 2014 3845       bkowal      Transition to transmitter group complete.
 * Feb 11, 2015 4098       bsteffen    Maintain jitter buffer during broadcast.
 * Apr 15, 2015 4397       bkowal      Added {@link #generateStatistics()}.
 * Apr 16, 2015 4405       rjpeter     Update to have hasSync initialized.
 * Jul 08, 2015 4636       bkowal      Support same and alert decibel levels.
 * Jul 15, 2015 4636       bkowal      Eliminate packet-level audio alterations.
 * Jul 28, 2015 4686       bkowal      Moved statistics to common.
 * Aug 17, 2015 4757       bkowal      Relocated regulation to BMH common.
 * Aug 19, 2015 4764       bkowal      Default the {@link #live} flag to true. No longer
 *                                     alter the {@link #live} flag mid-broadcast.
 * Aug 24, 2015 4769       bkowal      Handle the case when no Transmitter has associated tones.
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class LiveBroadcastTransmitThread extends BroadcastTransmitThread {

    private final String broadcastId;

    private final DataTransmitThread dataThread;

    private final CommsManagerCommunicator commsManager;

    private final BroadcastTransmitterConfiguration config;

    private final BROADCASTTYPE type;

    private final long requestTime;

    private volatile boolean live = true;

    private boolean bytesReceived = false;

    public LiveBroadcastTransmitThread(final EventBus eventBus,
            final InetAddress address, final int port,
            final Collection<Integer> transmitters, final String broadcastId,
            final DataTransmitThread dataThread,
            final CommsManagerCommunicator commsManager,
            final BroadcastTransmitterConfiguration config,
            final double dbTarget, final double sameDbTarget,
            final double alertDbTarget, final BROADCASTTYPE type,
            final long requestTime, final boolean hasSync) throws IOException {
        super("LiveBroadcastTransmitThread", eventBus, address, port,
                transmitters, dbTarget, sameDbTarget, alertDbTarget, hasSync);
        this.broadcastId = broadcastId;
        this.dataThread = dataThread;
        this.commsManager = commsManager;
        this.config = config;
        this.type = type;
        this.requestTime = requestTime;
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

        this.previousPacket = this.dataThread.pausePlayback();
        // Build playlist switch notification
        this.notifyBroadcastSwitch(STATE.STARTED);
        if (this.type == BROADCASTTYPE.EO && this.config.getToneAudio() != null) {
            // play the Alert / SAME tones.
            AudioPacketLogger packetLog = new AudioPacketLogger("SAME Tones",
                    getClass(), 30);
            if (this.config.getToneAudio().getSameTones() != null) {
                this.playTones(this.config.getToneAudio().getSameTones(),
                        "SAME", this.sameDbTarget, packetLog);
            }
            if (this.config.getToneAudio().getBeforeAlertTonePause() != null) {
                this.playTones(this.config.getToneAudio()
                        .getBeforeAlertTonePause(), "SAME", this.dbTarget,
                        packetLog);
            }
            packetLog.close();
            packetLog = new AudioPacketLogger("Alert Tones", getClass(), 30);
            if (this.config.getToneAudio().getAlertTones() != null) {
                this.playTones(this.config.getToneAudio().getAlertTones(),
                        "Alert", this.alertDbTarget, packetLog);
            }
            if (this.config.getToneAudio().getBeforeMessagePause() != null) {
                this.playTones(this.config.getToneAudio()
                        .getBeforeMessagePause(), "Alert", this.dbTarget,
                        packetLog);
            }
            packetLog.close();
        }
        BroadcastStatus status = new BroadcastStatus();
        status.setMsgSource(MSGSOURCE.DAC);
        status.setStatus(true);
        status.setBroadcastId(this.broadcastId);
        status.addTransmitterGroup(this.config.getTransmitterGroup());
        this.commsManager.sendDacLiveBroadcastMsg(status);

        waitForAudio();

        AudioPacketLogger packetLog = new AudioPacketLogger(
                "Live Broadcast Audio", getClass(), 30);
        /*
         * end the broadcast only after all buffered audio has been streamed.
         */
        while (live || this.audioBuffer.isEmpty() == false) {
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

        packetLog = new AudioPacketLogger("End of Message Tones", getClass(),
                30);
        if (this.type == BROADCASTTYPE.EO && this.config.getToneAudio() != null) {
            this.playTones(this.config.getEndToneAudio(), "End of Message",
                    this.sameDbTarget, packetLog);
        }
        packetLog.close();
        this.notifyBroadcastSwitch(STATE.FINISHED);

        this.dataThread.resumePlayback(previousPacket);
    }

    @Override
    public void playAudio(List<byte[]> data) {
        if (this.bytesReceived == false) {
            this.bytesReceived = true;
            this.generateStatistics();
        }
        /*
         * Attenuate / amplify all received data before it is added to the
         * buffer. Here we know that the audio db target will be used.
         */
        CollectibleAudioRegulator regulator = new CollectibleAudioRegulator(
                data);
        try {
            data = regulator.regulateAudioCollection(this.dbTarget);
        } catch (Exception e) {
            logger.error("Failed to amplify/attenuate received audio.", e);
        }

        super.playAudio(data);
    }

    private void playTones(byte[] toneAudio, final String toneType,
            final double dbTarget, AudioPacketLogger packetLog) {
        /*
         * Attenuate / amplify all received data before it is added to the
         * buffer. Here we know that the audio db target will be used.
         */
        AudioRegulator regulator = new AudioRegulator();
        try {
            toneAudio = regulator.regulateAudioVolume(toneAudio, dbTarget,
                    toneAudio.length);
        } catch (Exception e) {
            logger.error("Failed to amplify/attenuate the " + toneType
                    + " tone audio.", e);
        }

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

    private void waitForAudio() {
        /*
         * Stream silence until the first audio packet arrives to keep the
         * jitter buffer full to the watermark level.
         */
        AudioPacketLogger packetLog = new AudioPacketLogger(
                "Live Broadcast Buffering", getClass(), 10);
        byte[] silence = new byte[DacSessionConstants.SINGLE_PAYLOAD_SIZE];
        Arrays.fill(silence, DacSessionConstants.SILENCE);
        while (live && this.audioBuffer.isEmpty()) {
            try {
                this.streamAudio(silence);
                packetLog.packetProcessed();
            } catch (AudioOverflowException | UnsupportedAudioFormatException
                    | AudioConversionException | InterruptedException e) {
                this.notifyDacError(
                        "Failed to stream the buffered live audio.", e);
            }
        }
        packetLog.close();
    }

    private void notifyDacError(final String detail, final Exception e) {
        this.error = true;
        logger.error(detail, e);
        BroadcastStatus status = new BroadcastStatus();
        status.setMsgSource(MSGSOURCE.DAC);
        status.setStatus(false);
        status.setBroadcastId(this.broadcastId);
        status.addTransmitterGroup(this.config.getTransmitterGroup());
        status.setMessage(detail);
        status.setException(e);
        this.commsManager.sendDacLiveBroadcastMsg(status);
    }

    private void generateStatistics() {
        LiveBroadcastLatencyEvent event = new LiveBroadcastLatencyEvent();
        event.setBroadcastIdentifier(this.broadcastId);
        event.setLatency(System.currentTimeMillis() - this.requestTime);
        this.commsManager.forwardStatistics(event);
    }

    public void shutdown() {
        this.live = false;
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

    private void notifyBroadcastSwitch(final STATE broadcastState) {
        LiveBroadcastSwitchNotification notification = new LiveBroadcastSwitchNotification();

        /* identification and broadcast state information */
        notification.setType(this.type);
        notification.setBroadcastState(broadcastState);
        notification.setTransmitterGroup(this.config.getTransmitterGroup());

        /* broadcast cycle dialog playlist display information */
        notification.setTransitTime(TimeUtil.newGmtCalendar());
        notification.setMessageId(this.config.getMessageId());
        notification.setMessageTitle(this.config.getMessageTitle());
        notification.setMessageName(this.config.getMessageName());
        notification.setExpirationTime(this.config.getExpirationTime());
        notification.setSameTone(this.config.getSame());
        notification.setAlertTone(this.config.getAlert());

        eventBus.post(notification);
    }
}