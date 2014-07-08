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
import java.nio.charset.StandardCharsets;

import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.edex.bmh.dactransmit.exceptions.MalformedDacStatusException;

/**
 * Thread in control of DAC's status and control channel for a
 * {@code DacSession}. On initiation, must create a sync with the DAC. After the
 * initial sync, the DAC will send a heartbeat every 100ms. This message also
 * contains current status information about the DAC and the current session. To
 * maintain sync with DAC we must send a heartbeat every second.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 2, 2014    #3286   dgilling     Initial creation
 * 
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

public class ControlStatusThread extends Thread {

    private static final int RECEIVE_BUFFER_SIZE = 256;

    private static final long MILLIS_BETWEEN_HEARTBEAT = 300;

    private static final byte[] INITIAL_SYNC_MSG = "01000"
            .getBytes(StandardCharsets.US_ASCII);

    private static final byte[] HEARTBEAT_SYNC_MSG = "00000"
            .getBytes(StandardCharsets.US_ASCII);

    private static final byte[] CLEAR_BUFFER_MSG = "5000"
            .getBytes(StandardCharsets.US_ASCII);

    private final DacSession session;

    private final DatagramSocket socket;

    private final InetAddress dacAddress;

    private final int port;

    private long lastHeartbeatSent;

    /**
     * Constructor. Attempts to make a connection to the DAC at the specified
     * address and port.
     * 
     * @param session
     *            Reference back to the {@code DacSession} that created this
     *            thread for posting status updates.
     * @param dacAdress
     *            IP endpoint for the DAC.
     * @param controlPort
     *            UDP port for the DAC's control channel for this session.
     * @throws SocketException
     *             If the socket could not be opened.
     */
    public ControlStatusThread(DacSession session, InetAddress dacAdress,
            int controlPort) throws SocketException {
        super("ControlStatusThread");
        this.session = session;
        this.dacAddress = dacAdress;
        this.port = controlPort;
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
            while (true) {
                try {
                    try {
                        DacStatusMessage currentStatus = receiveHeartbeat();
                        // System.out
                        // .println("DEBUG [ControlStatusThread] : Current DAC status"
                        // + currentStatus);
                        session.receivedDacStatus(currentStatus);
                    } catch (MalformedDacStatusException e) {
                        System.out
                                .println("ERROR [ControlStatusThread] : Invalid status message received from DAC.");
                        e.printStackTrace();
                    }

                    sendHeartbeat();
                } catch (Throwable t) {
                    System.out
                            .println("ERROR [ControlStatusThread] : Runtime exception thrown.");
                    t.printStackTrace();
                }
            }
        } finally {
            socket.disconnect();
            socket.close();
        }
    }

    public void performInitialSync() throws IOException {
        DatagramPacket packet = buildPacket(CLEAR_BUFFER_MSG);
        socket.send(packet);

        packet = buildPacket(INITIAL_SYNC_MSG);
        socket.send(packet);

        lastHeartbeatSent = TimeUtil.currentTimeMillis();
    }

    public DacStatusMessage receiveHeartbeat()
            throws MalformedDacStatusException {
        byte[] receiveBuffer = new byte[RECEIVE_BUFFER_SIZE];
        DatagramPacket dacHeartbeat = buildPacket(receiveBuffer);

        DacStatusMessage currentStatus = null;
        try {
            // TODO: do we set a timeout here???
            socket.receive(dacHeartbeat);
            int bytesReceived = dacHeartbeat.getLength();
            currentStatus = new DacStatusMessage(new String(receiveBuffer, 0,
                    bytesReceived, StandardCharsets.US_ASCII));
        } catch (IOException e) {
            System.out
                    .println("ERROR [ControlStatusThread] : Error while waiting to receive DAC's heartbeat message.");
            e.printStackTrace();
        }
        return currentStatus;
    }

    public void sendHeartbeat() {
        long currentTime = TimeUtil.currentTimeMillis();
        if ((currentTime - lastHeartbeatSent) >= MILLIS_BETWEEN_HEARTBEAT) {
            try {
                DatagramPacket packet = buildPacket(HEARTBEAT_SYNC_MSG);
                socket.send(packet);
                lastHeartbeatSent = currentTime;
            } catch (IOException e) {
                System.out
                        .println("ERROR [ControlStatusThread] : Could not transmit heartbeat to DAC.");
                e.printStackTrace();
            }
        }
    }

    private DatagramPacket buildPacket(byte[] buf) {
        return new DatagramPacket(buf, buf.length, dacAddress, port);
    }
}
