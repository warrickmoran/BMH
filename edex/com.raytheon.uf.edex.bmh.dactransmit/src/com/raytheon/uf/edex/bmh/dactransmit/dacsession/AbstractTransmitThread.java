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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.primitives.Ints;
import com.raytheon.uf.edex.bmh.dactransmit.events.DacStatusUpdateEvent;
import com.raytheon.uf.edex.bmh.dactransmit.events.LostSyncEvent;
import com.raytheon.uf.edex.bmh.dactransmit.events.RegainSyncEvent;
import com.raytheon.uf.edex.bmh.dactransmit.events.handlers.IDacStatusUpdateEventHandler;
import com.raytheon.uf.edex.bmh.dactransmit.ipc.ChangeTransmitters;
import com.raytheon.uf.edex.bmh.dactransmit.rtp.RtpPacketIn;
import com.raytheon.uf.edex.bmh.dactransmit.rtp.RtpPacketInFactory;

/**
 * Abstraction of the common functionality that is required to submit data to a
 * DAC.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 14, 2014 3655       bkowal      Initial creation
 * Jan 09, 2015 3942       rjpeter     Made nextCycleTime volatile, updated to set limits on cycle intervals.
 * Jan 19, 2015 3912       bsteffen    Receive sync, status directly instead of subscribing.
 * Feb 06, 2015 4071       bsteffen    Consolidate threading.
 * Apr 16, 2015 4405       rjpeter     Update to have hasSync initialized.
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class AbstractTransmitThread extends Thread implements
        IDacStatusUpdateEventHandler {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected final boolean aggressivePacketInterval = Boolean
            .getBoolean("DacAggressivePacketInterval");

    protected final EventBus eventBus;

    protected final InetAddress address;

    protected final int port;

    protected Collection<Integer> transmitters;

    protected final DatagramSocket socket;

    protected RtpPacketIn previousPacket;

    protected volatile long packetInterval;

    protected int watermarkPackets;

    protected volatile boolean hasSync;

    protected volatile boolean onSyncRestartMessage;

    public AbstractTransmitThread(final String name, final EventBus eventBus,
            final InetAddress address, final int port,
            Collection<Integer> transmitters, boolean hasSync)
            throws SocketException {
        super(name);
        this.eventBus = eventBus;
        this.address = address;
        this.port = port;
        this.transmitters = transmitters;
        this.socket = new DatagramSocket();
        this.previousPacket = null;
        this.packetInterval = DataTransmitConstants.INITIAL_CYCLE_TIME;
        this.watermarkPackets = DataTransmitConstants.WATERMARK_PACKETS_IN_BUFFER;
        this.hasSync = hasSync;
        this.onSyncRestartMessage = false;
    }

    public Integer getLastSequenceNumber() {
        if (previousPacket != null) {
            return previousPacket.getSequenceNumber();
        } else {
            return null;
        }
    }

    protected RtpPacketIn buildRtpPacket(final RtpPacketIn previousPacket,
            final byte[] nextPayload) {
        RtpPacketInFactory factory = RtpPacketInFactory.getInstance();
        if (previousPacket != null) {
            factory.fromPacket(previousPacket, false)
                    .setPreviousPayload(previousPacket.getCurrentPayload())
                    .setCurrentPayload(nextPayload)
                    .incrementSequenceNum(
                            DataTransmitConstants.SEQUENCE_INCREMENT)
                    .incrementTimestamp(
                            DataTransmitConstants.TIMESTAMP_INCREMENT)
                    .setTransmitters(transmitters);
        } else {
            factory = factory.setCurrentPayload(nextPayload)
                    .setSequenceNumber(0).setTimestamp(0)
                    .setTransmitters(transmitters);
        }

        return factory.create();
    }

    protected void sendPacket(final RtpPacketIn packet) {
        try {
            byte[] rawPacket = packet.encode();
            DatagramPacket finalizedPacket = buildPacket(rawPacket);
            socket.send(finalizedPacket);
        } catch (IOException e) {
            logger.error("Error sending RTP packet to DAC.", e);
        }
    }

    protected DatagramPacket buildPacket(byte[] buf) {
        return new DatagramPacket(buf, buf.length, address, port);
    }

    @Override
    public void receivedDacStatus(DacStatusUpdateEvent e) {
        int differenceFromWatermark = this.watermarkPackets
                - e.getStatus().getBufferSize();
        if (aggressivePacketInterval) {
            calculatePacketIntervalAggressive(differenceFromWatermark);
        } else {
            calculatePacketIntervalStable(differenceFromWatermark);
        }
    }

    /**
     * An algorithm for setting the packet interval. Of the two algorithms this
     * one is more aggressive, it attempts to correct the buffer level before
     * the next status message arrives(in 100ms). The problem with this is that
     * if the next status message is late or skipped then it tends to
     * overcorrect very quickly.
     * 
     * @param differenceFromWatermark
     *            the number of packets that need to be added to the buffer to
     *            reach the {@link #watermarkPackets}.
     */
    private void calculatePacketIntervalAggressive(int differenceFromWatermark) {
        long newSleepCycle = DataTransmitConstants.DEFAULT_CYCLE_TIME;

        if (differenceFromWatermark < 0) {
            newSleepCycle = DataTransmitConstants.SLOW_CYCLE_TIME;
        } else {
            int packetsToSendUntilNextStatus = Math
                    .abs(differenceFromWatermark) + 5;

            // make sure we don't speed it up too fast
            newSleepCycle = Math.max(100L / packetsToSendUntilNextStatus,
                    DataTransmitConstants.FAST_CYCLE_TIME);
            // logger.debug("Speeding up cycle time to: " + nextCycleTime);
        }

        packetInterval = newSleepCycle;
    }

    /**
     * An algorithm for setting the packet interval. Of the two algorithms this
     * one is more stable. Corrections take longer to take affect. The slowest
     * correction is when the buffer is only off by a single packet can take up
     * to 400ms to correct. Larger corrections take affect faster. Since most
     * corrections are small the interval over long time periods(minutes) is
     * much more stable and missed or late status packets are not a problem.
     * 
     * @param differenceFromWatermark
     *            the number of packets that need to be added to the buffer to
     *            reach the {@link #watermarkPackets}.
     */
    private void calculatePacketIntervalStable(int differenceFromWatermark) {
        long newSleepCycle = DataTransmitConstants.DEFAULT_CYCLE_TIME
                - differenceFromWatermark;
        newSleepCycle = Math.max(newSleepCycle,
                DataTransmitConstants.FAST_CYCLE_TIME);
        newSleepCycle = Math.min(newSleepCycle,
                DataTransmitConstants.SLOW_CYCLE_TIME);
        packetInterval = newSleepCycle;
    }

    @Override
    public void lostDacSync(LostSyncEvent e) {
        logger.error("Application has lost sync with the DAC. Terminating data transmission.");
        hasSync = false;
    }

    @Override
    public void regainDacSync(RegainSyncEvent e) {
        if (e.getDownTime() >= DataTransmitConstants.SYNC_DOWNTIME_RESTART_THRESHOLD) {
            logger.info("Application has re-gained sync with the DAC. Restarting transmission from beginning of current message.");
            onSyncRestartMessage = true;
        } else {
            logger.info("Application has re-gained sync with the DAC. Resuming transmission.");
            onSyncRestartMessage = false;
        }
        hasSync = true;
    }

    @Subscribe
    public void changeTransmitters(ChangeTransmitters changeEvent) {
        transmitters = Ints.asList(changeEvent.getTransmitters());
    }
}