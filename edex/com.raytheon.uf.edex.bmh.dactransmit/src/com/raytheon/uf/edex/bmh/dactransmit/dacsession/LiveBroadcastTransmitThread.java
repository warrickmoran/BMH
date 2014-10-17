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
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.InetAddress;
import java.util.Collection;

import com.google.common.eventbus.EventBus;
import com.raytheon.uf.common.bmh.audio.AudioConversionException;
import com.raytheon.uf.common.bmh.audio.UnsupportedAudioFormatException;
import com.raytheon.uf.common.bmh.comms.LiveBroadcastStartData;
import com.raytheon.uf.edex.bmh.audio.AudioOverflowException;
import com.raytheon.uf.edex.bmh.audio.AudioRegulator;
import com.raytheon.uf.edex.bmh.dactransmit.ipc.LiveBroadcastStatus;
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

    private final LiveBroadcastStartData data;

    private final PipedInputStream dataStream;

    private final double dbTarget;

    public LiveBroadcastTransmitThread(final EventBus eventBus,
            final InetAddress address, final int port,
            final Collection<Integer> transmitters, final String broadcastId,
            final DataTransmitThread dataThread,
            final CommsManagerCommunicator commsManager,
            final LiveBroadcastStartData data, final PipedOutputStream src,
            final double dbTarget) throws IOException {
        super("LiveBroadcastTransmitThread", eventBus, address, port,
                transmitters);
        this.broadcastId = broadcastId;
        this.dataThread = dataThread;
        this.commsManager = commsManager;
        this.data = data;
        this.dataStream = new PipedInputStream(src);
        this.dbTarget = dbTarget;
    }

    @Override
    public void run() {
        this.dataThread.pausePlayback();

        if (data.isPlayAlertTone()) {
            if (data.getTonesDelay() > 0) {
                /*
                 * Need additional functionality in the EO dialog to handle this
                 * case to allow for using real information.
                 */
            }

            // play tone.
        }
        LiveBroadcastStatus response = new LiveBroadcastStatus();
        response.setBroadcastId(broadcastId);
        response.setTransmitterGroup(this.data.getTransmitterGroup());
        response.setReady(true);
        this.commsManager.sendDacLiveBroadcastMsg(response);

        byte[] nextPayload = new byte[DacSessionConstants.SINGLE_PAYLOAD_SIZE];
        try {
            int bytesCount = 0;
            while (bytesCount != -1) {
                while (!hasSync) {
                    Thread.sleep(DataTransmitConstants.DEFAULT_CYCLE_TIME);

                    // cannot restart audio. should 'onSyncRestartMessage'
                    // indicate an error condition in the case of live
                    // broadcasting?
                    if (hasSync && onSyncRestartMessage) {
                        logger.warn("Application has re-gained sync with the DAC. Unable to restart audio stream!");
                    }
                }

                bytesCount = this.dataStream.read(nextPayload);
                if (bytesCount <= 0) {
                    continue;
                }

                /*
                 * Adjust the audio based on the decibel target.
                 */
                byte[] regulatedAudio = this.adjustAudio(nextPayload);

                RtpPacketIn rtpPacket = buildRtpPacket(previousPacket,
                        regulatedAudio);

                sendPacket(rtpPacket);

                previousPacket = rtpPacket;

                Thread.sleep(nextCycleTime);
            }
        } catch (IOException e) {
            logger.error("Audio Data I/O has failed!", e);
            this.notifyDacError("Audio Data I/O has failed!");
        } catch (InterruptedException e) {
            logger.error("Thread sleep interrupted.", e);
        } catch (AudioOverflowException | UnsupportedAudioFormatException
                | AudioConversionException e) {
            logger.error("Audio regulation failed!", e);
            this.notifyDacError("Audio regulation failed!");
        }

        this.dataThread.resumePlayback();
    }

    private void notifyDacError(final String detail) {
        LiveBroadcastStatus response = new LiveBroadcastStatus();
        response.setBroadcastId(broadcastId);
        response.setTransmitterGroup(this.data.getTransmitterGroup());
        response.setReady(false);
        response.setDetail(detail);
        this.commsManager.sendDacLiveBroadcastMsg(response);
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
        try {
            this.dataStream.close();
        } catch (IOException e) {
            logger.warn(
                    "Failed to close the input data stream during shutdown.", e);
        }
    }

    public String getTransmitter() {
        return this.data.getTransmitterGroup();
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
}