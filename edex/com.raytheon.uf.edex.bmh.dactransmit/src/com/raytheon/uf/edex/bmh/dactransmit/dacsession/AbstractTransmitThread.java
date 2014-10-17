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
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class AbstractTransmitThread extends Thread implements
        IDacStatusUpdateEventHandler {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected final EventBus eventBus;

    protected final InetAddress address;

    protected final int port;

    protected Collection<Integer> transmitters;

    protected final DatagramSocket socket;

    protected RtpPacketIn previousPacket;

    protected long nextCycleTime;

    protected int watermarkPackets;

    protected volatile boolean hasSync;

    protected volatile boolean onSyncRestartMessage;

    /**
     * 
     */
    public AbstractTransmitThread(final String name, final EventBus eventBus,
            final InetAddress address, final int port,
            Collection<Integer> transmitters) throws SocketException {
        super(name);
        this.eventBus = eventBus;
        this.address = address;
        this.port = port;
        this.transmitters = transmitters;
        this.socket = new DatagramSocket();
        this.previousPacket = null;
        this.nextCycleTime = DataTransmitConstants.INITIAL_CYCLE_TIME;
        this.watermarkPackets = DataTransmitConstants.WATERMARK_PACKETS_IN_BUFFER;
        this.hasSync = true;
        this.onSyncRestartMessage = false;
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
    @Subscribe
    public void receivedDacStatus(DacStatusUpdateEvent e) {
        int differenceFromWatermark = this.watermarkPackets
                - e.getStatus().getBufferSize();

        if (differenceFromWatermark <= 0) {
            nextCycleTime = DataTransmitConstants.DEFAULT_CYCLE_TIME;
        } else {
            int packetsToSendUntilNextStatus = Math
                    .abs(differenceFromWatermark) + 5;
            nextCycleTime = 100L / packetsToSendUntilNextStatus;
            // logger.debug("Speeding up cycle time to: " + nextCycleTime);
        }
    }

    @Subscribe
    public void lostDacSync(LostSyncEvent e) {
        logger.error("Application has lost sync with the DAC. Terminating data transmission.");
        hasSync = false;
    }

    @Subscribe
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