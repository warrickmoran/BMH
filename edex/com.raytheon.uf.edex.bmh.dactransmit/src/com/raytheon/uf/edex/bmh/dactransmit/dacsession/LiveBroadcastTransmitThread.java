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
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.InetAddress;
import java.util.Collection;

import com.google.common.eventbus.EventBus;
import com.raytheon.uf.common.bmh.audio.AudioConversionException;
import com.raytheon.uf.common.bmh.audio.UnsupportedAudioFormatException;
import com.raytheon.uf.common.bmh.broadcast.BroadcastStatus;
import com.raytheon.uf.common.bmh.broadcast.BroadcastTransmitterConfiguration;
import com.raytheon.uf.common.bmh.broadcast.ILiveBroadcastMessage;
import com.raytheon.uf.common.bmh.dac.dacsession.DacSessionConstants;
import com.raytheon.uf.common.bmh.notify.LiveBroadcastSwitchNotification;
import com.raytheon.uf.common.bmh.notify.LiveBroadcastSwitchNotification.STATE;
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

    private final PipedInputStream dataStream;

    private final double dbTarget;

    private volatile boolean error;

    public LiveBroadcastTransmitThread(final EventBus eventBus,
            final InetAddress address, final int port,
            final Collection<Integer> transmitters, final String broadcastId,
            final DataTransmitThread dataThread,
            final CommsManagerCommunicator commsManager,
            final BroadcastTransmitterConfiguration config,
            final PipedOutputStream src, final double dbTarget)
            throws IOException {
        super("LiveBroadcastTransmitThread", eventBus, address, port,
                transmitters);
        this.broadcastId = broadcastId;
        this.dataThread = dataThread;
        this.commsManager = commsManager;
        this.config = config;
        this.dataStream = new PipedInputStream(src);
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
        ByteArrayInputStream tonesInputStream = new ByteArrayInputStream(
                this.config.getToneAudio());
        byte[] nextPayload = new byte[DacSessionConstants.SINGLE_PAYLOAD_SIZE];
        try {
            while (tonesInputStream.read(nextPayload) != -1) {
                this.streamAudio(nextPayload);
            }
        } catch (IOException | AudioOverflowException
                | UnsupportedAudioFormatException | AudioConversionException
                | InterruptedException e) {
            this.notifyDacError("Failed to stream the SAME / Alert Tones!", e);
        }

        BroadcastStatus status = new BroadcastStatus();
        status.setMsgSource(ILiveBroadcastMessage.SOURCE_DAC_TRANSMIT);
        status.setStatus(true);
        status.setBroadcastId(this.broadcastId);
        status.addTransmitter(this.config.getTransmitter());
        this.commsManager.sendDacLiveBroadcastMsg(status);

        try {
            int bytesCount = 0;
            while (bytesCount != -1) {
                bytesCount = this.dataStream.read(nextPayload);
                if (bytesCount <= 0) {
                    continue;
                }

                this.streamAudio(nextPayload);
            }
        } catch (IOException e) {
            this.notifyDacError("Audio Data I/O has failed!", e);
        } catch (InterruptedException e) {
            logger.error("Thread sleep interrupted.", e);
        } catch (AudioOverflowException | UnsupportedAudioFormatException
                | AudioConversionException e) {
            this.notifyDacError("Audio regulation failed!", e);
        }

        this.dataThread.resumePlayback();
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

        previousPacket = rtpPacket;

        Thread.sleep(nextCycleTime);

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
        // Build playlist switch notification
        this.notifyBroadcastSwitch(STATE.FINISHED);        
        
        try {
            this.dataStream.close();
        } catch (IOException e) {
            logger.warn(
                    "Failed to close the input data stream during shutdown.", e);
        }
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

    public void attemptPipeReconnection(final PipedOutputStream out)
            throws IOException {
        this.dataStream.connect(out);
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