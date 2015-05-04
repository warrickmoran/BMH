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
package com.raytheon.bmh.comms.dactransmit;

import java.io.IOException;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raytheon.bmh.comms.CommsManager;
import com.raytheon.uf.common.bmh.notify.DacTransmitShutdownNotification;
import com.raytheon.uf.common.bmh.notify.MaintenanceMessagePlayback;
import com.raytheon.uf.common.serialization.SerializationUtil;
import com.raytheon.uf.edex.bmh.dactransmit.ipc.DacTransmitShutdown;

/**
 * Allows the Comms Manager to communicate with a single instance of a Dac
 * Transmit running in maintenance mode.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 28, 2015 4394       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class DacMaintenanceCommunicator extends Thread {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final CommsManager manager;

    private final String groupName;

    private final Socket socket;

    public DacMaintenanceCommunicator(CommsManager manager, String groupName,
            Socket socket) {
        super("DacMaintenanceCommunicator-" + groupName);
        this.manager = manager;
        this.groupName = groupName;
        this.socket = socket;
    }

    @Override
    public void run() {
        while (!socket.isClosed()) {
            try {
                Object message = SerializationUtil.transformFromThrift(
                        Object.class, socket.getInputStream());
                handleMessage(message);
            } catch (Throwable e) {
                logger.error("Error reading message from DacTransmit: {}",
                        groupName, e);
                disconnect();
            }
        }
    }

    protected void handleMessage(Object message) {
        if (message instanceof MaintenanceMessagePlayback) {
            this.manager.transmitDacStatus(message);
        } else if (message instanceof DacTransmitShutdown) {
            disconnect();
        }
    }

    private void disconnect() {
        try {
            socket.close();
        } catch (IOException e) {
            logger.error("Error disconnecting from comms manager", e);
        }
        this.manager.transmitDacShutdown(new DacTransmitShutdownNotification(
                this.groupName));
    }
}