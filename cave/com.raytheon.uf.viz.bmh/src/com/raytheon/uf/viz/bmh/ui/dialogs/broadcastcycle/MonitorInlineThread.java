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
package com.raytheon.uf.viz.bmh.ui.dialogs.broadcastcycle;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import com.raytheon.uf.common.bmh.audio.AudioPacketLogger;
import com.raytheon.uf.common.bmh.comms.LineTapDisconnect;
import com.raytheon.uf.common.bmh.comms.LineTapRequest;
import com.raytheon.uf.common.bmh.dac.DacLiveStreamer;
import com.raytheon.uf.common.serialization.SerializationUtil;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.bmh.BMHServers;

/**
 * 
 * A Thread that listens for audio packets from the comms manager and plays
 * them.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Aug 04, 2014  2487     bsteffen    Initial creation
 * Oct 10, 2014  3656     bkowal      Use the BMH Servers constants.
 * Oct 17, 2014  3687     bsteffen    Support practice servers.
 * Oct 29, 2014  3774     bsteffen    Log Packets
 * Apr 14, 2015  4394     bkowal      Updated constructor to require information that is
 *                                    already known.
 * Jun 17, 2015  4482     rjpeter     Only process in 160 byte chunks.
 * Jun 18, 2015  4490     bkowal      Added {@link #getChannel()}.
 * Nov 11, 2015  5114     rjpeter     Updated CommsManager to use a single port.
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class MonitorInlineThread extends Thread {

    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(MonitorInlineThread.class);

    private final String transmitterGroup;

    private final String dacReceiveAddress;

    private final int receivePort;

    private final int channel;

    private volatile boolean running = true;

    private final Set<DisconnectListener> listeners = new CopyOnWriteArraySet<>();

    public MonitorInlineThread(String transmitterGroup,
            final String dacReceiveAddress, final int receivePort,
            final int channel) {
        super("MonitorInlineThread-" + transmitterGroup);
        this.transmitterGroup = transmitterGroup;
        this.dacReceiveAddress = dacReceiveAddress;
        this.receivePort = receivePort;
        this.channel = channel;
    }

    @Override
    public void run() {
        String commsLoc = BMHServers.getCommsManager();
        if (commsLoc == null) {
            Exception e = new IllegalStateException(
                    "No address for comms manager, unable to monitor "
                            + transmitterGroup);
            statusHandler.error(e.getLocalizedMessage(), e);
            disconnect(e);
            return;
        }
        URI commsURI = null;
        try {
            commsURI = new URI(commsLoc);
        } catch (URISyntaxException e) {
            statusHandler.error(
                    "Invalid address for comms maanger, unable to monitor "
                            + transmitterGroup, e);
            disconnect(e);
            return;
        }

        DacLiveStreamer listener = null;
        try (Socket socket = new Socket(commsURI.getHost(), commsURI.getPort());
                AudioPacketLogger logger = new AudioPacketLogger(
                        transmitterGroup + " monitor inline", this.getClass(),
                        60)) {
            socket.setTcpNoDelay(true);
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();
            SerializationUtil.transformToThriftUsingStream(new LineTapRequest(
                    transmitterGroup, this.dacReceiveAddress, this.receivePort,
                    this.channel), outputStream);
            byte[] payload = new byte[160];
            listener = new DacLiveStreamer(0);
            int totalRead = 0;
            int curRead = 0;
            while (running) {
                totalRead = 0;
                // need the full 160 bytes
                while (totalRead < payload.length) {
                    curRead = inputStream.read(payload, totalRead,
                            payload.length - totalRead);
                    if (curRead < 0) {
                        throw new IOException(
                                "Unexpected end of stream on monitor inline.");
                    }

                    totalRead += curRead;
                }

                logger.packetProcessed();
                listener.dataArrived(payload);
            }

            SerializationUtil.transformToThriftUsingStream(
                    new LineTapDisconnect(), outputStream);
            disconnect(null);
            listener.dispose();
        } catch (Throwable e) {
            statusHandler.error("Unexpected error while monitoring "
                    + transmitterGroup, e);
            disconnect(e);
            if (listener != null) {
                listener.dispose();
            }
        }
    }

    private void disconnect(Throwable error) {
        for (DisconnectListener listener : listeners) {
            listener.disconnected(error);
        }
    }

    public void addDisconnectListener(DisconnectListener listener) {
        listeners.add(listener);
    }

    public void removeDisconnectListener(DisconnectListener listener) {
        listeners.remove(listener);
    }

    /**
     * Stop this thread from playing audio.
     */
    public void cancel() {
        running = false;
    }

    /**
     * @return the channel
     */
    public int getChannel() {
        return channel;
    }

    /**
     * Interfaces that wish to be notified when a disconnect occured. If the
     * disconnect was due to {@link MonitorInlineThread#cancel()} being called
     * then the error will be null. Otherwise the error will indicate the
     * problem that caused the disconnect. The error will be logged before
     * calling any listeners so listeners do not need to log the exception.
     */
    public static interface DisconnectListener {

        public void disconnected(Throwable error);

    }

}
