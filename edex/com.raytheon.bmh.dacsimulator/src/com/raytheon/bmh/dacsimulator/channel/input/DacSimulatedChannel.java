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
package com.raytheon.bmh.dacsimulator.channel.input;

import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.raytheon.bmh.dacsimulator.DacSimChannelConfig;
import com.raytheon.bmh.dacsimulator.channel.output.DacSimulatedBroadcast;

/**
 * Simulates a complete channel on the simulated DAC, which includes the
 * capability to receive audio transmissions and receive and send heartbeat
 * messages to a connected transmission client.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 07, 2014  #3688     dgilling     Initial creation
 * Oct 21, 2014  #3688     dgilling     Refactor to support RTP packet's
 *                                      ability to address to output channels.
 * 
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

public class DacSimulatedChannel {

    private final int channelNumber;

    private final JitterBuffer buffer;

    private final DacHeartbeatReceiveThread heartbeatReceiveThread;

    private final DacHeartbeatSendThread heartbeatSendThread;

    private final DacReceiveDataThread dataThread;

    private final ExecutorService eventThread;

    private final EventBus eventBus;

    /**
     * Constructor.
     * 
     * @param config
     *            Configuration for this channel.
     * @param minBufferSize
     *            The minimum number of packets that must be received over this
     *            input channel before it will begin broadcasting.
     * @param broadcast
     *            {@code DacSimulatedBroadcast} instance needed for getting
     *            channel status for the heartbeat messages.
     * @throws SocketException
     *             If one of the threads could not open a socket, or the socket
     *             could not bind to a local port.
     */
    public DacSimulatedChannel(DacSimChannelConfig config, int minBufferSize,
            DacSimulatedBroadcast broadcast) throws SocketException {
        this.channelNumber = config.getChannelNumber();
        this.buffer = new JitterBuffer(minBufferSize);

        this.eventThread = Executors.newSingleThreadExecutor();
        this.eventBus = new AsyncEventBus("Channel" + this.channelNumber
                + "-EventBus", eventThread);

        this.heartbeatReceiveThread = new DacHeartbeatReceiveThread(
                config.getControlPort(), config.getChannelNumber(),
                this.eventBus);
        this.heartbeatSendThread = new DacHeartbeatSendThread(
                config.getChannelNumber(), this.buffer, this.eventBus,
                broadcast);
        this.dataThread = new DacReceiveDataThread(this.buffer,
                config.getDataPort(), config.getChannelNumber(), this.eventBus);
    }

    public void start() {
        heartbeatReceiveThread.start();
    }

    public void shutdown() {
        heartbeatReceiveThread.shutdown();
        heartbeatSendThread.shutdown();
        dataThread.shutdown();
        eventThread.shutdown();
    }

    public void waitForTermination() throws InterruptedException {
        heartbeatReceiveThread.join();
        heartbeatSendThread.join();
        dataThread.join();
    }

    public JitterBuffer getBuffer() {
        return buffer;
    }
}
