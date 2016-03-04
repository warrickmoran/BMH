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

import java.net.InetAddress;
import java.net.SocketException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.google.common.eventbus.EventBus;
import com.raytheon.bmh.dactransmit.rtp.RtpPacketIn;
import com.raytheon.uf.common.bmh.audio.AudioConversionException;
import com.raytheon.uf.common.bmh.audio.AudioOverflowException;
import com.raytheon.uf.common.bmh.audio.AudioPacketLogger;
import com.raytheon.uf.common.bmh.audio.UnsupportedAudioFormatException;

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
 * Feb 11, 2015 4098       bsteffen    Maintain jitter buffer during broadcast.
 * Apr 16, 2015 4405       rjpeter     Update to have hasSync initialized.
 * Apr 24, 2015 4394       bkowal      Updated to support {@link IBroadcastBufferListener}.
 * Jul 08, 2015 4636       bkowal      Support same and alert decibel levels.
 * Jul 13, 2015 4636       bkowal      Support separate 2.4K and 1.8K transfer tone types.
 * Jul 15, 2015 4636       bkowal      No longer alter audio packet-by-packet.
 * Aug 17, 2015 4757       bkowal      Relocated regulation to BMH common.
 * Nov 04, 2015 5068       rjpeter     Switch audio units from dB to amplitude.
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class BroadcastTransmitThread extends AbstractTransmitThread {

    protected final short audioAmplitude;

    protected final short sameAmplitude;

    protected final short alertAmplitude;

    protected LinkedBlockingQueue<byte[]> audioBuffer = new LinkedBlockingQueue<>();

    protected volatile boolean error;

    private IBroadcastBufferListener listener;

    private int packetCount = 0;

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
            final short audioAmplitude, final short sameAmplitude,
            final short alertAmplitude, boolean hasSync) throws SocketException {
        super(name, eventBus, address, port, transmitters, hasSync);
        this.audioAmplitude = audioAmplitude;
        this.sameAmplitude = sameAmplitude;
        this.alertAmplitude = alertAmplitude;
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

        RtpPacketIn rtpPacket = buildRtpPacket(previousPacket, data);

        sendPacket(rtpPacket);
        if (this.listener != null) {
            ++this.packetCount;
            this.listener.packetStreamed(this.packetCount);
        }

        previousPacket = rtpPacket;

        Thread.sleep(packetInterval);

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

    /**
     * @return the error
     */
    public boolean isError() {
        return error;
    }

    /**
     * @return the listener
     */
    public IBroadcastBufferListener getListener() {
        return listener;
    }

    /**
     * @param listener
     *            the listener to set
     */
    public void setListener(IBroadcastBufferListener listener) {
        this.listener = listener;
    }
}