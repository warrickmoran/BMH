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
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Collection;

import com.raytheon.uf.edex.bmh.dactransmit.playlist.AudioFileBuffer;
import com.raytheon.uf.edex.bmh.dactransmit.playlist.AudioFileDirectoryPlaylist;
import com.raytheon.uf.edex.bmh.dactransmit.playlist.PlaylistMessage;
import com.raytheon.uf.edex.bmh.dactransmit.playlist.PlaylistMessageCache;
import com.raytheon.uf.edex.bmh.dactransmit.rtp.RtpPacketIn;
import com.raytheon.uf.edex.bmh.dactransmit.rtp.RtpPacketInFactory;

/**
 * Thread for sending audio data to the DAC. Runs on a cycle time of 20 ms.
 * Every cycle this class will read the next chunk of audio data to send to the
 * DAC, package it up and transmit it via UDP. This process will continue until
 * this process has exhausted all audio files in its playlist.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 2, 2014   #3286    dgilling     Initial creation
 * 
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

public final class DataTransmitThread extends Thread {

    private final DacSession session;

    private final InetAddress address;

    private final int port;

    private final Collection<Integer> transmitters;

    private final DatagramSocket socket;

    private final AudioFileDirectoryPlaylist playlist;

    private final PlaylistMessageCache audioCache;

    private RtpPacketIn previousPacket;

    private long nextCycleTime;

    /**
     * Constructor for this thread. Attempts to open a {@code DatagramSocket}
     * and connect to DAC IP endpoint specified by IP address and port.
     * 
     * @param session
     *            Reference back to {@code DacSession} that spawned this thread.
     *            Needed to retrieve buffer status.
     * @param cache
     *            {@code PlaylistMessageCache} that holds the buffered audio
     *            data for every message in the playlist.
     * @param transmitters
     *            List of destination transmitters for this audio data.
     * @param playlist
     *            Playlist of audio files to transmit.
     * @param dacAdress
     *            {@code InetAddress} of DAC IP endpoint.
     * @param dataPort
     *            Port to send data over.
     * @throws SocketException
     *             If the socket could not be opened.
     */
    public DataTransmitThread(final DacSession session,
            final PlaylistMessageCache cache, InetAddress address, int port,
            Collection<Integer> transmitters,
            AudioFileDirectoryPlaylist playlist) throws SocketException {
        super("DataTransmitThread");
        this.session = session;
        this.audioCache = cache;
        this.address = address;
        this.port = port;
        this.transmitters = transmitters;
        this.playlist = playlist;
        this.previousPacket = null;
        this.nextCycleTime = DataTransmitConstants.DEFAULT_CYCLE_TIME;
        this.socket = new DatagramSocket();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {
        try {
            /*
             * this is effectively an infinite loop since
             * AudioFileDirectoryPlaylist's iterator loops over the same files
             * forever.
             */
            for (PlaylistMessage audioFile : playlist) {
                System.out
                        .println("DEBUG [DataTransmitThread] : Switching to file ["
                                + audioFile.getFilename() + "].");

                AudioFileBuffer fileBuffer = audioCache.get(audioFile);
                while (fileBuffer.hasRemaining()) {
                    try {
                        byte[] nextPayload = new byte[DacSessionConstants.SINGLE_PAYLOAD_SIZE];
                        fileBuffer.get(nextPayload);

                        RtpPacketIn rtpPacket = buildRtpPacket(previousPacket,
                                nextPayload);

                        sendPacket(rtpPacket);

                        previousPacket = rtpPacket;

                        int bufferSize = session.getCurrentBufferSize();
                        if ((bufferSize == DataTransmitConstants.WATERMARK_PACKETS_IN_BUFFER)
                                || (bufferSize == DataTransmitConstants.UNKNOWN_BUFFER_SIZE)) {
                            nextCycleTime = DataTransmitConstants.DEFAULT_CYCLE_TIME;
                        } else if (bufferSize < DataTransmitConstants.WATERMARK_PACKETS_IN_BUFFER) {
                            nextCycleTime = DataTransmitConstants.FAST_CYCLE_TIME;
                        } else {
                            nextCycleTime = DataTransmitConstants.SLOW_CYCLE_TIME;
                        }

                        Thread.sleep(nextCycleTime);
                    } catch (InterruptedException e) {
                        System.out
                                .println("ERROR [ControlStatusThread] : Thread sleep interrupted.");
                        e.printStackTrace();
                    } catch (Throwable t) {
                        System.out
                                .println("ERROR [ControlStatusThread] : Runtime exception thrown.");
                        t.printStackTrace();
                    }
                }
            }
        } finally {
            socket.disconnect();
            socket.close();
        }
    }

    private RtpPacketIn buildRtpPacket(final RtpPacketIn previousPacket,
            final byte[] nextPayload) {
        RtpPacketInFactory factory = RtpPacketInFactory.getInstance();
        if (previousPacket != null) {
            factory.fromPacket(previousPacket, false)
                    .setPreviousPayload(previousPacket.getCurrentPayload())
                    .setCurrentPayload(nextPayload)
                    .incrementSequenceNum(
                            DataTransmitConstants.SEQUENCE_INCREMENT)
                    .incrementTimestamp(
                            DataTransmitConstants.TIMESTAMP_INCREMENT);
        } else {
            factory = factory.setCurrentPayload(nextPayload)
                    .setSequenceNumber(0).setTimestamp(0)
                    .setTransmitters(transmitters);
        }

        return factory.create();
    }

    private void sendPacket(final RtpPacketIn packet) {
        try {
            byte[] rawPacket = packet.encode();
            DatagramPacket finalizedPacket = buildPacket(rawPacket);
            socket.send(finalizedPacket);
        } catch (IOException e) {
            System.out
                    .println("ERROR [DataTransmitThread] : Error sending RTP packet to DAC.");
            e.printStackTrace();
        }
    }

    private DatagramPacket buildPacket(byte[] buf) {
        return new DatagramPacket(buf, buf.length, address, port);
    }
}
