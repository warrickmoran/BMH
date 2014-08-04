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
import java.net.InetAddress;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import com.raytheon.uf.common.bmh.comms.LineTapDisconnect;
import com.raytheon.uf.common.bmh.comms.LineTapRequest;
import com.raytheon.uf.common.bmh.dac.DacLiveStreamer;
import com.raytheon.uf.common.bmh.dac.IDacListener;
import com.raytheon.uf.common.serialization.SerializationUtil;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

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
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class MonitorInlineThread extends Thread {

    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(MonitorInlineThread.class);

    // TODO read this from a config file and figure out clustering.
    private static final InetAddress commsAddress = InetAddress
            .getLoopbackAddress();

    // TODO read this from a config file
    private static final int commsPort = 58260;

    private final String transmitterGroup;

    private volatile boolean running = true;

    private Set<DisconnectListener> listeners = new CopyOnWriteArraySet<>();

    public MonitorInlineThread(String transmitterGroup) {
        super("MonitorInlineThread-" + transmitterGroup);
        this.transmitterGroup = transmitterGroup;
    }

    @Override
    public void run() {
        try (Socket socket = new Socket(commsAddress, commsPort)) {
            socket.setTcpNoDelay(true);
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();
            SerializationUtil.transformToThriftUsingStream(new LineTapRequest(
                    transmitterGroup), outputStream);
            byte[] payload = new byte[160];
            IDacListener listener = new DacLiveStreamer(0);
            while (running) {
                int read = inputStream.read(payload);
                if (read > 0) {
                    listener.dataArrived(payload);
                } else if (read < 0) {
                    throw new IOException("Unexpected end of stream.");
                }
            }
            SerializationUtil.transformToThriftUsingStream(
                    new LineTapDisconnect(), outputStream);
            disconnect(null);
        } catch (Throwable e) {
            statusHandler.error("Unexpected error while monitoring + "
                    + transmitterGroup, e);
            disconnect(e);
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
