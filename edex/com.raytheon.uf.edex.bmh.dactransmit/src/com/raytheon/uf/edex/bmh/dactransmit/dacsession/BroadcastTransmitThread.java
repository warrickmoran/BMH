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

import java.net.InetAddress;
import java.net.SocketException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.google.common.eventbus.EventBus;
import com.raytheon.uf.common.bmh.audio.AudioConversionException;
import com.raytheon.uf.common.bmh.audio.AudioPacketLogger;
import com.raytheon.uf.common.bmh.audio.UnsupportedAudioFormatException;
import com.raytheon.uf.common.time.util.ITimer;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.edex.bmh.audio.AudioOverflowException;
import com.raytheon.uf.edex.bmh.audio.AudioRegulator;
import com.raytheon.uf.edex.bmh.dactransmit.rtp.RtpPacketIn;

/**
 * Streams all audio data in {@link BroadcastTransmitThread#audioBuffer} to the
 * dac specified in the configuration.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 6, 2014  3630       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class BroadcastTransmitThread extends AbstractTransmitThread {

    private final double dbTarget;

    protected LinkedBlockingQueue<byte[]> audioBuffer = new LinkedBlockingQueue<>();

    protected ITimer transmitTimer;

    protected volatile boolean error;

    /**
     * @param name
     * @param eventBus
     * @param address
     * @param port
     * @param transmitters
     * @throws SocketException
     */
    public BroadcastTransmitThread(String name, EventBus eventBus,
            InetAddress address, int port, Collection<Integer> transmitters,
            final double dbTarget) throws SocketException {
        super(name, eventBus, address, port, transmitters);
        this.dbTarget = dbTarget;
    }

    @Override
    public void run() {
        AudioPacketLogger packetLog = new AudioPacketLogger("Broadcast Audio",
                getClass(), 30);
        while (this.error == false && this.audioBuffer.isEmpty() == false) {
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
                logger.error(
                        "Audio retrieval / streaming has failed! Terminating the transmission ...",
                        e);
                this.error = true;
            }
        }
        /*
         * sleep for the duration of one packet to ensure that all packets had
         * time to stream.
         */
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            // Ignore.
        }

        packetLog.close();
    }

    public void playAudio(List<byte[]> data) {
        this.audioBuffer.addAll(data);
    }

    protected void streamAudio(byte[] data) throws AudioOverflowException,
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

    /**
     * @return the error
     */
    public boolean isError() {
        return error;
    }
}