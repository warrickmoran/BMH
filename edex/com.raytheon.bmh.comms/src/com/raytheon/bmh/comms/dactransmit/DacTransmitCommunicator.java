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
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raytheon.bmh.comms.CommsManager;
import com.raytheon.bmh.comms.broadcast.BroadcastDelayAlarm;
import com.raytheon.bmh.comms.broadcast.SAMEDurationTruncatedAlarm;
import com.raytheon.bmh.comms.broadcast.WtchOrWrnNotBroadcastAlarm;
import com.raytheon.uf.common.bmh.broadcast.ILiveBroadcastMessage;
import com.raytheon.uf.common.bmh.datamodel.playlist.PlaylistUpdateNotification;
import com.raytheon.uf.common.bmh.notify.DacTransmitShutdownNotification;
import com.raytheon.uf.common.bmh.notify.LiveBroadcastSwitchNotification;
import com.raytheon.uf.common.bmh.notify.MessageBroadcastNotifcation;
import com.raytheon.uf.common.bmh.notify.MessageDelayedBroadcastNotification;
import com.raytheon.uf.common.bmh.notify.MessageNotBroadcastNotification;
import com.raytheon.uf.common.bmh.notify.MessagePlaybackStatusNotification;
import com.raytheon.uf.common.bmh.notify.NoPlaybackMessageNotification;
import com.raytheon.uf.common.bmh.notify.PlaylistSwitchNotification;
import com.raytheon.uf.common.bmh.notify.SAMEMessageTruncatedNotification;
import com.raytheon.uf.common.bmh.notify.status.DacHardwareStatusNotification;
import com.raytheon.uf.common.bmh.stats.DeliveryTimeEvent;
import com.raytheon.uf.common.bmh.stats.LiveBroadcastLatencyEvent;
import com.raytheon.uf.common.serialization.SerializationUtil;
import com.raytheon.uf.edex.bmh.dactransmit.ipc.ChangeDecibelTarget;
import com.raytheon.uf.edex.bmh.dactransmit.ipc.ChangeTimeZone;
import com.raytheon.uf.edex.bmh.dactransmit.ipc.ChangeTransmitters;
import com.raytheon.uf.edex.bmh.dactransmit.ipc.DacTransmitCriticalError;
import com.raytheon.uf.edex.bmh.dactransmit.ipc.DacTransmitScanPlaylists;
import com.raytheon.uf.edex.bmh.dactransmit.ipc.DacTransmitShutdown;
import com.raytheon.uf.edex.bmh.dactransmit.ipc.DacTransmitStatus;

/**
 * 
 * Manages communication with a single instance of the dac transmit application.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Jul 16, 2014  3399     bsteffen    Initial creation
 * Jul 25, 2014  3286     dgilling    Support MessagePlaybackStatusNotification
 *                                    and PlaylistSwitchNotification messages 
 *                                    from DacTransmit.
 * Jul 31, 2014  3286     dgilling    Support DacHardwareStatusNotification.
 * Aug 12, 2014  3486     bsteffen    Support ChangeTransmitters
 * Aug 14, 2014  3286     dgilling    Support DacTransmitCriticalError.
 * Aug 18, 2014  3532     bkowal      Support ChangeDecibelRange.
 * Sep 5, 2014   3532     bkowal      Replace ChangeDecibelRange with ChangeDecibelTarget.
 * Sep 23, 2014  3485     bsteffen    Ensure the manager gets notified of any state changes.
 * Oct 15, 2014  3655     bkowal      Support live broadcasting to the DAC.
 * Oct 16, 2014  3687     bsteffen    Fix disconnect of dac transmits not connected to dac.
 * Oct 21, 2014  3655     bkowal      Use the new message types.
 * Oct 21, 2014  3655     bkowal      Support LiveBroadcastSwitchNotification.
 * Nov 11, 2014  3762     bsteffen    Add load balancing of dac transmits.
 * Jan 12, 2015  3968     bkowal      Handle {@link MessageBroadcastNotifcation}.
 * Jan 14, 2015  3969     bkowal      Handle {@link MessageNotBroadcastNotification}.
 * Jan 19, 2015  4002     bkowal      Handle {@link MessageDelayedBroadcastNotification}.
 * Jan 23, 2015  3912     bsteffen    Handle all Throwables in run.
 * Feb 16, 2015  4107     bsteffen    Add updatePlaylistListener
 * Mar 10, 2015  4228     bsteffen    Track disconnected state more carefully.
 * Mar 20, 2015  4296     bsteffen    Catch all throwables from SerializationUtil.
 * Apr 15, 2015  4397     bkowal      Handle {@link LiveBroadcastLatencyEvent}.
 * Apr 20, 2015  4394     bkowal      Generate a {@link DacTransmitShutdownNotification}
 *                                    to indicate that a dac transmit has shutdown.
 * Apr 21, 2015  4407     bkowal      Made {@link #lastStatus} and {@link #disconnected}
 *                                    volatile.
 * Apr 24, 2015  4423     rferrel     Send {@link ChangeTimeZone} when time zone changes value.
 * Apr 27, 2015  4397     bkowal      Handle {@link DeliveryTimeEvent}.
 * May 04, 2015  4452     bkowal      Handle {@link SAMEMessageTruncatedNotification}.
 * Jun 01, 2015  4490     bkowal      Added {@link #sameDurationTruncatedAlarm} and
 *                                    {@link #wtchOrWrnNotBroadcastAlarm}.
 * Jun 02, 2015  4369     rferrel     Handle {@link NoPlaybackMessageNotification}.
 * Jul 08, 2015  4636     bkowal      Support same and alert decibel levels.
 * Jul 28, 2015  4686     bkowal      Moved statistics to common.
 * Aug 12, 2015  4424     bkowal      Eliminate Dac Transmit Key.
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class DacTransmitCommunicator extends Thread {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Object sendLock = new Object();

    private final CommsManager manager;

    private final String groupName;

    private final Socket socket;

    private int[] radios;

    private double audioDbTarget;

    private double sameDbTarget;

    private double alertDbTarget;

    private volatile DacTransmitStatus lastStatus;

    private String timeZone;

    private volatile boolean disconnected = false;

    private static final BroadcastDelayAlarm broadcastDelayAlarm = new BroadcastDelayAlarm();

    private final SAMEDurationTruncatedAlarm sameDurationTruncatedAlarm = new SAMEDurationTruncatedAlarm();

    private final WtchOrWrnNotBroadcastAlarm wtchOrWrnNotBroadcastAlarm = new WtchOrWrnNotBroadcastAlarm();

    public DacTransmitCommunicator(CommsManager manager, String groupName,
            int[] radios, Socket socket, double audioDbTarget,
            double sameDbTarget, double alertDbTarget) {
        super("DacTransmitCommunicator-" + groupName);
        this.manager = manager;
        this.groupName = groupName;
        Arrays.sort(radios);
        this.radios = radios;
        this.socket = socket;
        this.audioDbTarget = audioDbTarget;
        this.sameDbTarget = sameDbTarget;
        this.alertDbTarget = alertDbTarget;
    }

    public String getGroupName() {
        return groupName;
    }

    @Override
    public void run() {
        manager.dacTransmitConnected(groupName);
        try {
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
        } finally {
            manager.dacTransmitDisconnected(groupName);
        }
    }

    protected void handleMessage(Object message) {
        if (message instanceof DacTransmitStatus) {
            DacTransmitStatus newStatus = (DacTransmitStatus) message;
            if (lastStatus == null || !newStatus.equals(lastStatus)) {
                lastStatus = newStatus;
                if (newStatus.isConnectedToDac()) {
                    manager.dacConnectedLocal(groupName);
                } else {
                    manager.dacDisconnectedLocal(groupName);
                }
            }
        } else if (message instanceof MessagePlaybackStatusNotification) {
            MessagePlaybackStatusNotification notification = (MessagePlaybackStatusNotification) message;
            notification.setTransmitterGroup(groupName);
            manager.transmitDacStatus(notification);
        } else if (message instanceof PlaylistSwitchNotification) {
            PlaylistSwitchNotification notification = (PlaylistSwitchNotification) message;
            notification.setTransmitterGroup(groupName);
            manager.transmitDacStatus(notification);
        } else if (message instanceof DacHardwareStatusNotification) {
            DacHardwareStatusNotification notification = (DacHardwareStatusNotification) message;
            notification.setTransmitterGroup(groupName);
            manager.transmitDacStatus(notification);
        } else if (message instanceof DacTransmitCriticalError) {
            DacTransmitCriticalError notification = (DacTransmitCriticalError) message;
            manager.errorReceived(notification, groupName);
        } else if (message instanceof DacTransmitShutdown) {
            disconnect();
        } else if (message instanceof ILiveBroadcastMessage) {
            manager.forwardDacBroadcastMsg((ILiveBroadcastMessage) message);
        } else if (message instanceof LiveBroadcastSwitchNotification) {
            LiveBroadcastSwitchNotification notification = (LiveBroadcastSwitchNotification) message;
            manager.transmitDacStatus(notification);
        } else if (message instanceof MessageBroadcastNotifcation) {
            MessageBroadcastNotifcation notification = (MessageBroadcastNotifcation) message;
            notification.setTransmitterGroup(this.groupName);
            manager.transmitDacStatus(notification);
        } else if (message instanceof MessageNotBroadcastNotification) {
            MessageNotBroadcastNotification notification = (MessageNotBroadcastNotification) message;
            notification.setTransmitterGroup(this.groupName);
            wtchOrWrnNotBroadcastAlarm.notify(notification);
        } else if (message instanceof NoPlaybackMessageNotification) {
            NoPlaybackMessageNotification notification = (NoPlaybackMessageNotification) message;
            notification.setGroupName(groupName);
            manager.transmitDacStatus(notification);
        } else if (message instanceof MessageDelayedBroadcastNotification) {
            MessageDelayedBroadcastNotification notification = (MessageDelayedBroadcastNotification) message;
            notification.setTransmitterGroup(this.groupName);
            broadcastDelayAlarm.notify(notification);
        } else if (message instanceof LiveBroadcastLatencyEvent) {
            LiveBroadcastLatencyEvent event = (LiveBroadcastLatencyEvent) message;
            event.setTransmitterGroup(this.groupName);
            manager.transmitBMHStat(event);
        } else if (message instanceof DeliveryTimeEvent) {
            DeliveryTimeEvent event = (DeliveryTimeEvent) message;
            event.setTransmitterGroup(this.groupName);
            manager.transmitBMHStat(event);
        } else if (message instanceof SAMEMessageTruncatedNotification) {
            SAMEMessageTruncatedNotification notification = (SAMEMessageTruncatedNotification) message;
            notification.setTransmitterGroup(this.groupName);
            sameDurationTruncatedAlarm.notify(notification);
        } else {
            logger.error("Unexpected message from dac transmit of type: {}",
                    message.getClass().getSimpleName());
        }
    }

    /**
     * @return true if a status message has been received from the dac transmit
     *         process and the socket has not been disconnected, false
     *         otherwise.
     * @See {@link #isDisconnectedFromDacTransmit()}
     */
    public boolean isConnectedToDacTransmit() {
        return lastStatus != null && !disconnected;
    }

    /**
     * This is NOT an exact opposite of {@link #isConnectedToDacTransmit()}.
     * When the socket is connected but a status message has not been retrieved
     * from the DAC then both methods will return false. In all other cases this
     * is just the opposite of {@link #isConnectedToDacTransmit()}
     */
    public boolean isDisconnectedFromDacTransmit() {
        return disconnected;
    }

    public boolean isConnectedToDac() {
        if (isConnectedToDacTransmit()) {
            return lastStatus.isConnectedToDac();
        } else {
            return false;
        }
    }

    public void shutdown(boolean now) {
        send(new DacTransmitShutdown(now));
    }

    public void setRadios(int[] radios) {
        Arrays.sort(radios);
        if (!Arrays.equals(radios, this.radios)) {
            send(new ChangeTransmitters(radios));
            this.radios = radios;
        }
    }

    public void setTransmitterDBTarget(double audioDbTarget,
            double sameDbTarget, double alertDbTarget) {
        if (this.audioDbTarget != audioDbTarget
                || this.sameDbTarget != sameDbTarget
                || this.alertDbTarget != alertDbTarget) {
            this.send(new ChangeDecibelTarget(audioDbTarget, sameDbTarget,
                    alertDbTarget));
            this.audioDbTarget = audioDbTarget;
            this.sameDbTarget = sameDbTarget;
            this.alertDbTarget = alertDbTarget;
        }
    }

    /**
     * When time zone value changes send out the new value.
     * 
     * @param timeZone
     */
    public void setTimeZone(String timeZone) {
        if (timeZone == null) {
            if (this.timeZone == null) {
                return;
            }
        } else if (timeZone.equals(this.timeZone)) {
            return;
        }

        this.send(new ChangeTimeZone(timeZone));
        this.timeZone = timeZone;
    }

    public void sendPlaylistUpdate(PlaylistUpdateNotification notification) {
        send(notification);
    }

    public void sendLiveBroadcastMsg(ILiveBroadcastMessage msg) {
        send(msg);
    }

    public void updatePlaylistListener(boolean active) {
        send(new DacTransmitScanPlaylists(!active));
    }

    private void send(Object toSend) {
        synchronized (sendLock) {
            try {
                SerializationUtil.transformToThriftUsingStream(toSend,
                        socket.getOutputStream());
            } catch (Throwable e) {
                logger.error("Error communicating with DacTransmit: {}",
                        groupName, e);
                disconnect();
            }
        }
    }

    private void disconnect() {
        boolean wasConnectedToDac = isConnectedToDac();
        disconnected = true;
        try {
            socket.close();
        } catch (IOException e) {
            logger.error("Error disconnecting from comms manager", e);
        }
        if (wasConnectedToDac) {
            manager.dacDisconnectedLocal(groupName);
        }
        this.manager.transmitDacShutdown(new DacTransmitShutdownNotification(
                this.groupName), this.groupName);
    }
}
