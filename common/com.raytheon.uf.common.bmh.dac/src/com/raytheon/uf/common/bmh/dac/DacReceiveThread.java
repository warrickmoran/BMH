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
package com.raytheon.uf.common.bmh.dac;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.nio.ByteBuffer;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.raytheon.uf.common.bmh.audio.AudioPacketLogger;
import com.raytheon.uf.common.bmh.dac.data.RTPHeaderStruct;
import com.raytheon.uf.common.bmh.dac.data.RTPPacketStruct;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

/**
 * A thread that will connect to the dac on the specified port, process the
 * bytes that were retrieved, and submit the data to any {@link IDacListener}s
 * that have subscribed to the thread.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 15, 2014 3374       bkowal      Initial creation
 * Jul 15, 2014 3487       bsteffen    Add hasSubscribers
 * Sep 23, 2014 3485       bsteffen    Allow receiving through multicast.
 * Oct 29, 2014 3774       bsteffen    Log Packets
 * Nov 3, 2014  3773       bkowal      Start with an out-of-order packet if
 *                                     too many packets arrive out-of-order within
 *                                     a set amount of time.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class DacReceiveThread extends Thread {

    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(DacReceiveThread.class);

    /*
     * Indicates that no packets have been read yet.
     */
    private static final int NO_PREVIOUS_IDENTIFIER = -9999;

    private static final int MAX_SEQUENCE_VALUE = 65536;

    /*
     * The amount of time that packets can be out of sequence before the process
     * resumes with the packet number that was received as the last packet
     * number (in milliseconds). Dac packets arrive continuously
     */
    private static final long MAX_NOT_CONSECUTIVE_TIME = 100;

    /*
     * Will trigger the potential sequence wrap-around condition when the last
     * encountered sequence number is within the specified range of the maximum
     * integer value. A range is used to mitigate the risk that the actual
     * wrap-around packet may be dropped or missed.
     */
    private static final int WRAP_AROUND_RANGE = 1000;

    /*
     * The total number of dac channels that data is retrieved for.
     */
    private static final int DAC_CHANNELS = 4;

    /*
     * A list of {@link IDacListener} that will be notified whenever valid data
     * arrives.
     */
    private List<IDacListener> subscribers;

    /*
     * Thread pool to asynchronously notify subscribers that data has arrived.
     * Subscribers are asynchronously notified to ensure that the receiver can
     * return to listening for additional data as soon as possible to mitigate
     * the risk of skipping packets.
     * 
     * Only one thread is used to ensure that messages are sent to subscribers
     * one at a time in the proper order.
     */
    private final ExecutorService subscriberNotifierService = Executors
            .newFixedThreadPool(1);

    /*
     * The port that will be used to connect to the dac.
     */
    private final int port;

    /*
     * When receiving through multicast this is the group address to join.
     */
    private final InetAddress multicastAddress;

    /*
     * Used to connect to the DAC and read data.
     */
    private DatagramSocket dacConnection;

    /*
     * Used to stop the thread.
     */
    private volatile boolean halt = false;

    private byte[] packetBuffer;

    private List<byte[]> payloadBuffers;

    /*
     * Used to track the last packet sequence number that was encountered to
     * monitor the packet ordering.
     */
    private int lastSequenceNumber;

    /*
     * Used to track the last source associated with the data that arrived.
     * Different sources may be associated with different sequence numbers.
     */
    private int lastContributingSource = NO_PREVIOUS_IDENTIFIER;

    /*
     * Used to indicate that the sequence number may restart soon. Triggered
     * when the most recent sequence number is within a certain range of the
     * maximum integer value. This flag will help ensure that the dac receiver
     * does not accidentally classify a wrap-around packet as a packet that
     * arrived too late.
     */
    private boolean potentialWrapAround = false;

    /*
     * Flag that is set indicating that the first packet has arrived out of
     * order.
     */
    private boolean outOfOrder = false;

    /*
     * The current time when the first packet arrived out of order.
     */
    private long outOfOrderArrivalTime;

    /**
     * Receive audio playback from the dac using a multicast address.
     * 
     * @param netIf
     *            - the network interface connected to the network the dac is
     *            on. This can be null and will use the default network
     *            interface.
     * @param multicastAddress
     *            the multicast address that the dac is sending audio to.
     * @param port
     *            the port that the dac is sending audio to.
     */
    public DacReceiveThread(final NetworkInterface netIf,
            final InetAddress multicastAddress, final int port) {
        super(DacReceiveThread.class.getName());
        this.subscribers = new ArrayList<>();
        this.port = port;
        this.multicastAddress = multicastAddress;

        try {
            if (multicastAddress == null) {
                this.dacConnection = new DatagramSocket(this.port);
            } else {
                MulticastSocket socket = new MulticastSocket(this.port);
                if (netIf != null) {
                    socket.setNetworkInterface(netIf);
                }
                socket.joinGroup(multicastAddress);
                this.dacConnection = socket;

            }
        } catch (IOException e) {
            statusHandler
                    .fatal("Failed to bind to port: " + this.port + "!", e);
            return;
        }

        this.initDataStorage();

        statusHandler.info("Ready to listen for DAC packets on port: "
                + this.port + ".");
    }

    /**
     * Constructor
     * 
     * @param port
     *            the port that will be used to connect to the dac.
     */
    public DacReceiveThread(final int port) {
        this(null, null, port);
    }

    private void initDataStorage() {
        this.packetBuffer = new byte[RTPPacketStruct.PACKET_SIZE];
        this.payloadBuffers = new ArrayList<byte[]>(DAC_CHANNELS);
        for (int i = 0; i < DAC_CHANNELS; i++) {
            this.payloadBuffers
                    .add(new byte[RTPPacketStruct.DATA_PAYLOAD_SIZE]);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {
        statusHandler.info("Listening for DAC packets ...");
        AudioPacketLogger logger = new AudioPacketLogger("Dac Receive",
                getClass(), 60);
        while (this.halt == false) {
            this.receivePackets();
            logger.packetProcessed();
            this.notifySubscribers();
        }
        logger.close();
        statusHandler.info("Shutting down ...");
        this.closeDacConnection();
    }

    /**
     * Used to set a flag that will stop the dac thread.
     */
    public void halt() {
        this.halt = true;
        // may need to forcefully close the connection just in case the thread
        // is waiting for data at the time the stop command is issued.
        this.closeDacConnection();
        /*
         * stop notifying subscribers.
         */
        this.subscriberNotifierService.shutdownNow();
        statusHandler.info("Initiating shutdown sequence ...");
    }

    /**
     * Closes the socket connection to the dac.
     */
    private void closeDacConnection() {
        if (this.dacConnection == null) {
            return;
        }
        if (this.dacConnection instanceof MulticastSocket
                && multicastAddress != null) {
            MulticastSocket multicast = (MulticastSocket) this.dacConnection;
            try {
                multicast.leaveGroup(multicastAddress);
            } catch (IOException e) {
                statusHandler.error("Failed to leave multicast group: "
                        + multicastAddress, e);
            }
        }
        this.dacConnection.close();
        statusHandler.info("Terminated the connection to the DAC.");
        this.dacConnection = null;
    }

    /**
     * Attempts to read packets from the dac.
     * 
     * @param packetsReceived
     *            the packets that are read from the dac.
     */
    private void receivePackets() {
        DatagramPacket packet = new DatagramPacket(this.packetBuffer,
                this.packetBuffer.length);
        try {
            this.dacConnection.receive(packet);
        } catch (IOException e) {
            if (this.halt) {
                /*
                 * the dac socket may need to be forcefully closed during
                 * shutdown.
                 */
                return;
            }
            statusHandler.error("Failed to receive data from the DAC!", e);
            return;
        }

        /* analyze the header bytes */
        ByteBuffer headerBuffer = ByteBuffer
                .allocate(RTPPacketStruct.RTP_PACKET_HDR_SIZE);
        headerBuffer.put(this.packetBuffer, 0,
                RTPPacketStruct.RTP_PACKET_HDR_SIZE);

        /* create the header to validate the sequence numbers and the source id. */
        RTPHeaderStruct rtpHeader = new RTPHeaderStruct(headerBuffer);

        /* determine if the source id has changed */
        if (this.lastContributingSource == NO_PREVIOUS_IDENTIFIER) {
            this.lastSequenceNumber = NO_PREVIOUS_IDENTIFIER;
            this.lastContributingSource = rtpHeader.getContributingSource();
            this.potentialWrapAround = false;
        } else {
            /* determine if the source has changed. */
            if (this.lastContributingSource != rtpHeader
                    .getContributingSource()) {
                /*
                 * the source has changed. prepare for a potentially different
                 * sequence.
                 */
                this.lastSequenceNumber = NO_PREVIOUS_IDENTIFIER;
                this.potentialWrapAround = false;
            }
        }

        if (this.lastSequenceNumber == NO_PREVIOUS_IDENTIFIER) {
            /* first packet read since starting */
            this.lastSequenceNumber = rtpHeader.getSequenceNumber();
            statusHandler.info("Read first packet with sequence number: "
                    + this.lastSequenceNumber + " for source: "
                    + this.lastContributingSource + ".");
        } else {
            /* verify that the packet arrived in order. */
            if (rtpHeader.getSequenceNumber() < this.lastSequenceNumber
                    && this.potentialWrapAround == false) {
                /* a packet has arrived late. */
                if (this.adjustSequencing(rtpHeader.getSequenceNumber()) == false) {
                    statusHandler.warn("Received packet: "
                            + rtpHeader.getSequenceNumber()
                            + " later than expected. "
                            + this.logLastKnownState() + " Skipping ...");
                    return;
                }
            } else if (rtpHeader.getSequenceNumber() < this.lastSequenceNumber
                    && this.potentialWrapAround) {
                /* the packet sequence has wrapped around and restarted from 0. */
                statusHandler
                        .info("The sequence number has wrapped around and restarted.");
                this.lastSequenceNumber = rtpHeader.getSequenceNumber();
                this.potentialWrapAround = false;
            } else if (rtpHeader.getSequenceNumber() == this.lastSequenceNumber) {
                /* a duplicate packet has been encountered. */
                if (this.adjustSequencing(rtpHeader.getSequenceNumber()) == false) {
                    statusHandler.warn("Received duplicate packet: "
                            + rtpHeader.getSequenceNumber() + "! "
                            + this.logLastKnownState() + " Skipping ...");
                    return;
                }
            } else if (rtpHeader.getSequenceNumber() > (this.lastSequenceNumber + 1)) {
                /* a packet has arrived earlier than expected. */
                statusHandler.warn("Received packet: "
                        + rtpHeader.getSequenceNumber()
                        + " earlier than expected. " + this.logLastKnownState()
                        + " Processing as the current packet ...");
                this.lastSequenceNumber = rtpHeader.getSequenceNumber();
            } else {
                /* the packet arrived as expected */
                this.lastSequenceNumber = rtpHeader.getSequenceNumber();
            }
        }

        /* Determine if there is wrap-around potential for the sequence number. */
        if (this.lastSequenceNumber >= MAX_SEQUENCE_VALUE - WRAP_AROUND_RANGE) {
            this.potentialWrapAround = true;
        }

        /* retrieve the bytes for every channel */
        for (int channel = 1; channel <= DAC_CHANNELS; channel++) {
            int offset = RTPPacketStruct.RTP_PACKET_HDR_SIZE
                    + ((channel - 1) * RTPPacketStruct.DATA_PAYLOAD_SIZE);
            System.arraycopy(this.packetBuffer, offset,
                    this.payloadBuffers.get(channel - 1), 0,
                    RTPPacketStruct.DATA_PAYLOAD_SIZE);
        }

        /*
         * always reset the flag when we reach this point; indicates that
         * packets are arriving in the expected order.
         */
        this.outOfOrder = false;
    }

    private boolean adjustSequencing(final int sequenceNumber) {
        if (this.outOfOrder == false) {
            this.outOfOrder = true;
            this.outOfOrderArrivalTime = System.currentTimeMillis();
            return false;
        } else {
            /*
             * 
             */
            if (this.outOfOrderArrivalTime + MAX_NOT_CONSECUTIVE_TIME >= System
                    .currentTimeMillis()) {
                // time to re-key the sequence
                statusHandler
                        .info("Adjusting the packet sequencing. Restarting the sequencing with packet: "
                                + sequenceNumber + ".");
                this.lastSequenceNumber = sequenceNumber;
                return true;
            } else {
                return false;
            }
        }
    }

    private String logLastKnownState() {
        return "The last sequence read was: " + this.lastSequenceNumber
                + " associated with source: " + this.lastContributingSource
                + ".";
    }

    /**
     * Notifies subscribers that data is available after a successful data read.
     * 
     * @param packetsReceived
     *            the packets that are read from the dac.
     */
    private void notifySubscribers() {
        synchronized (this.subscribers) {
            if (this.subscribers.isEmpty()) {
                return;
            }

            SubscriberNotifier subscriberNotifier = new SubscriberNotifier(
                    this.subscribers, this.payloadBuffers);
            if (this.halt == false) {
                this.subscriberNotifierService.execute(subscriberNotifier);
            }
        }
    }

    /**
     * Allows a {@link IDacListener} to subscribe to the thread.
     * 
     * @param dacListener
     */
    public void subscribe(final IDacListener dacListener) {
        if (dacListener.getChannel() < 0
                || dacListener.getChannel() > DAC_CHANNELS) {
            throw new InvalidParameterException(
                    "The specified DAC channel to listen to must be > 0 and <= "
                            + DAC_CHANNELS + "!");
        }

        synchronized (this.subscribers) {
            this.subscribers.add(dacListener);
            statusHandler.info("Added subscriber: " + dacListener.toString());
        }
    }

    /**
     * Allows a {@link IDacListener} to unsubscribe from the thread.
     * 
     * @param dacListener
     */
    public void unsubscribe(final IDacListener dacListener) {
        synchronized (this.subscribers) {
            this.subscribers.remove(dacListener);
            statusHandler.info("Removed subscriber: " + dacListener.toString());
        }
    }

    public boolean hasSubscribers() {
        synchronized (subscribers) {
            return !subscribers.isEmpty();
        }
    }
}