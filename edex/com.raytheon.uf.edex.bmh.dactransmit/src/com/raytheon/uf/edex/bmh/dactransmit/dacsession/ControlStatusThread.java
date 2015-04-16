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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raytheon.uf.common.bmh.dac.dacsession.DacSessionConstants;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.edex.bmh.dactransmit.events.DacStatusUpdateEvent;
import com.raytheon.uf.edex.bmh.dactransmit.events.LostSyncEvent;
import com.raytheon.uf.edex.bmh.dactransmit.events.RegainSyncEvent;
import com.raytheon.uf.edex.bmh.dactransmit.events.handlers.IDacStatusUpdateEventHandler;
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
 * Jul 02, 2014  #3286     dgilling     Initial creation
 * Jul 14, 2014  #3286     dgilling     Used logback for logging.
 * Jul 16, 2014  #3286     dgilling     Use event bus, force initial sync to
 *                                      get a heartbeat back.
 * Aug 08, 2014  #3286     dgilling     Better handling for transmit/receive
 *                                      errors, use sync lost/gained events.
 * Aug 13, 2014  #3286     dgilling     Do not clear buffer ever when sync-ing.
 * Sep 04, 2014  #3584     dgilling     Use same local UDP port always to fix DAC
 *                                      reconnects.
 * Oct 17, 2014  #3688     dgilling     Use PORT_OFFSET to allow DacTransmit and
 *                                      DacSimulator to run on same host.
 * Oct 17, 2014  #3655     bkowal       Move tones to common.
 * Jan 19, 2015  #3912     bsteffen     Send events directly to a listener.
 * Jan 26, 2015  #3995     rjpeter      Fix heartbeat time out.
 * Apr 02, 2015  #4325     bsteffen     Log sync events.
 * Apr 16, 2015  #4405     rjpeter      Added isSynced.
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

public class ControlStatusThread extends Thread {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final int RECEIVE_BUFFER_SIZE = 256;

    private static final long MILLIS_BETWEEN_HEARTBEAT = 300;

    private static final byte[] INITIAL_SYNC_MSG = "01000"
            .getBytes(StandardCharsets.US_ASCII);

    private static final byte[] HEARTBEAT_SYNC_MSG = "00000"
            .getBytes(StandardCharsets.US_ASCII);

    /**
     * Need to offset the port this thread binds on so that DacTransmit and
     * DacSimulator don't try to bind the same port if both are running on the
     * same host.
     */
    private static final int PORT_OFFSET = 100;

    private final IDacStatusUpdateEventHandler eventHandler;

    private final DatagramSocket socket;

    private final InetAddress dacAddress;

    private final int port;

    private volatile boolean keepRunning;

    private long lastHeartbeatSent;

    private long lastHeartbeatReceived;

    private int heartbeatsMissed;

    private boolean hasSync;

    /**
     * Constructor. Attempts to make a connection to the DAC at the specified
     * address and port.
     * 
     * @param eventHandler
     *            Reference back to ab {@link IDacStatusUpdateEventHandler}
     *            instance for posting necessary status events.
     * @param dacAdress
     *            IP endpoint for the DAC.
     * @param controlPort
     *            UDP port for the DAC's control channel for this session.
     * @throws SocketException
     *             If the socket could not be opened.
     */
    public ControlStatusThread(IDacStatusUpdateEventHandler eventHandler,
            InetAddress dacAdress, int controlPort) throws SocketException {
        super("ControlStatusThread");
        this.eventHandler = eventHandler;
        this.dacAddress = dacAdress;
        this.port = controlPort;
        this.keepRunning = true;
        this.socket = new DatagramSocket(this.port + PORT_OFFSET);
        this.heartbeatsMissed = 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {
        try {
            while (keepRunning) {
                try {
                    if (!hasSync) {
                        RegainSyncEvent notify = performInitialSync();
                        if (notify != null) {
                            eventHandler.regainDacSync(notify);
                        }
                    }

                    /*
                     * We check sync again, just in case we had to break out of
                     * performInitialSync() early because the application was
                     * asked to shutdown.
                     */
                    if (hasSync) {
                        try {
                            DacStatusMessage currentStatus = receiveHeartbeat(DacSessionConstants.DEFAULT_SYNC_TIMEOUT_PERIOD);
                            heartbeatsMissed = 0;
                            // logger.debug("Current DAC status: " +
                            // currentStatus);
                            eventHandler
                                    .receivedDacStatus(new DacStatusUpdateEvent(
                                            currentStatus));
                        } catch (MalformedDacStatusException e) {
                            /*
                             * To this point every status message that comes
                             * back from the DAC that doesn't match our expected
                             * format is the "X----" message it sends when the
                             * device attempting to communicate with it doesn't
                             * have a valid sync. Hence we will invalidate our
                             * sync if we get a malformed status message from
                             * the DAC.
                             */
                            logger.error(
                                    "Malformed status message received from DAC. Invalidating sync.",
                                    e);
                            hasSync = false;
                            eventHandler.lostDacSync(new LostSyncEvent());
                        } catch (Exception e) {
                            logger.warn(
                                    "Did not receive heartbeat message from DAC.",
                                    e);
                            heartbeatsMissed++;
                            if (heartbeatsMissed >= DacSessionConstants.MISSED_HEARTBEATS_THRESHOLD) {
                                logger.error("Missed "
                                        + DacSessionConstants.MISSED_HEARTBEATS_THRESHOLD
                                        + " consecutive heartbeat messages from DAC. Invalidating sync.");
                                hasSync = false;
                                eventHandler.lostDacSync(new LostSyncEvent());
                            }
                        }

                        try {
                            sendHeartbeat();
                        } catch (Exception e) {
                            logger.warn(
                                    "Could not transmit heartbeat message to DAC.",
                                    e);
                            if ((TimeUtil.currentTimeMillis() - lastHeartbeatSent) >= DacSessionConstants.COMPLETE_SYNC_LOST_TIME) {
                                logger.error("Haven't sent a successful heartbeat message to DAC in last "
                                        + DacSessionConstants.COMPLETE_SYNC_LOST_TIME
                                        + " ms. Invalidating sync.");
                                hasSync = false;
                                eventHandler.lostDacSync(new LostSyncEvent());
                            }
                        }
                    }
                } catch (Throwable t) {
                    logger.error("Runtime exception thrown.", t);
                }
            }
        } finally {
            socket.disconnect();
            socket.close();
        }
    }

    /**
     * Informs this thread that it should begin shutdown. If your code desires
     * to block until this thread dies, use {@link #join()} or
     * {@link #isAlive()}.
     * 
     * @see #join()
     * @see #isAlive()
     */
    public void shutdown() {
        keepRunning = false;
    }

    /**
     * Makes the initial connection to the DAC to obtain "sync".
     * 
     * @return A {@code RegainSyncEvent} that informs the caller that sync was
     *         obtained or {@code null} if this thread was told to shutdown
     *         before sync could be obtained.
     */
    public RegainSyncEvent performInitialSync() {
        RegainSyncEvent retVal = null;
        logger.info("Obtaining sync with DAC.");

        while (!hasSync && keepRunning) {
            try {
                DatagramPacket packet = buildPacket(INITIAL_SYNC_MSG);
                socket.send(packet);
                lastHeartbeatSent = TimeUtil.currentTimeMillis();

                // we don't have a valid sync until we start receiving
                // heartbeats from the DAC
                long previousTime = lastHeartbeatReceived;
                receiveHeartbeat(DacSessionConstants.INITIAL_SYNC_TIMEOUT_PERIOD);
                retVal = new RegainSyncEvent(lastHeartbeatReceived
                        - previousTime);
                hasSync = true;
                heartbeatsMissed = 0;
            } catch (Throwable t) {
                logger.error(
                        "Attempt to sync with DAC failed. Re-attempting sync.",
                        t);
                try {
                    Thread.sleep(DacSessionConstants.SYNC_RETRY_PERIOD);
                } catch (InterruptedException e) {
                    logger.error("Wait period for retrying sync interrupted.",
                            e);
                }
            }
        }

        logger.info("Obtained sync with DAC.");

        return retVal;
    }

    private DacStatusMessage receiveHeartbeat(int timeout)
            throws MalformedDacStatusException, IOException {
        byte[] receiveBuffer = new byte[RECEIVE_BUFFER_SIZE];
        DatagramPacket dacHeartbeat = buildPacket(receiveBuffer);

        socket.setSoTimeout(timeout);
        socket.receive(dacHeartbeat);
        int bytesReceived = dacHeartbeat.getLength();
        DacStatusMessage currentStatus = new DacStatusMessage(new String(
                receiveBuffer, 0, bytesReceived, StandardCharsets.US_ASCII));
        lastHeartbeatReceived = TimeUtil.currentTimeMillis();
        return currentStatus;
    }

    private void sendHeartbeat() throws IOException {
        long currentTime = TimeUtil.currentTimeMillis();
        if ((currentTime - lastHeartbeatSent) >= MILLIS_BETWEEN_HEARTBEAT) {
            DatagramPacket packet = buildPacket(HEARTBEAT_SYNC_MSG);
            socket.send(packet);
            lastHeartbeatSent = currentTime;
        }
    }

    private DatagramPacket buildPacket(byte[] buf) {
        return new DatagramPacket(buf, buf.length, dacAddress, port);
    }

    /**
     * True if this control thread has the sync with the DAC.
     * 
     * @return
     */
    public boolean isSynced() {
        return hasSync;
    }
}
