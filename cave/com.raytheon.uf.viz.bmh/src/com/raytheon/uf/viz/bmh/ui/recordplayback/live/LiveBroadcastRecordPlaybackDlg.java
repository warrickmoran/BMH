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
package com.raytheon.uf.viz.bmh.ui.recordplayback.live;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.common.bmh.broadcast.BroadcastTransmitterConfiguration;
import com.raytheon.uf.common.bmh.broadcast.ILiveBroadcastMessage;
import com.raytheon.uf.common.bmh.broadcast.LiveBroadcastStartCommand;
import com.raytheon.uf.common.bmh.broadcast.LiveBroadcastStartCommand.BROADCASTTYPE;
import com.raytheon.uf.common.bmh.broadcast.OnDemandBroadcastConstants.MSGSOURCE;
import com.raytheon.uf.common.bmh.dac.tones.TonesGenerator;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.bmh.ui.dialogs.emergencyoverride.LiveBroadcastSettings;
import com.raytheon.uf.viz.bmh.ui.recordplayback.RecordPlaybackDlg;
import com.raytheon.uf.viz.bmh.ui.recordplayback.live.LiveBroadcastThread.BROADCAST_STATE;

/**
 * An extension of the @{link RecordPlaybackDlg} that supports live streaming
 * the audio to the Comms Manager as it is recorded.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 9, 2014  3656       bkowal      Initial creation
 * Oct 15, 2014 3655       bkowal      Reset the dialog on ERROR.
 * Oct 17, 2014 3655       bkowal      Prepare configuration for the live broadcast
 *                                     based on a {@link LiveBroadcastSettings}.
 * Oct 21, 2014 3655       bkowal      Send tone information to the broadcast live
 *                                     streamer.
 * Oct 21, 2014 3655       bkowal      Add additional information to the 
 *                                     {@link BroadcastTransmitterConfiguration} so
 *                                     that it can be used in a 
 *                                     {@link LiveBroadcastSwitchNotification}.
 * Oct 26, 2014 3712       bkowal      Prevent the dialog from being closed during
 *                                     recording / playback.
 * Nov 1, 2014  3655       bkowal      Include end of message tones in the live broadcast
 *                                     configuration.
 * Nov 1, 2014  3657       bkowal      Updated dialog flow to match legacy system.
 * Nov 10, 2014 3630       bkowal      Re-factor to support on-demand broadcasting.
 * Nov 17, 2014 3820       bkowal      Execute a separate shutdown sequence when stopping
 *                                     due to broadcast initialization failure.
 * Nov 17, 2014 3808       bkowal      Support broadcast live.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class LiveBroadcastRecordPlaybackDlg extends RecordPlaybackDlg implements
        IBroadcastStateListener {

    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(LiveBroadcastRecordPlaybackDlg.class);

    private LiveBroadcastThread broadcastThread;

    private final LiveBroadcastSettings settings;

    private volatile boolean shutdownInitializationFailure;

    /**
     * @param parentShell
     * @param maxRecordingSeconds
     * 
     *            Will also eventually include additional information specified
     *            on the Emergency Override dialog.
     */
    public LiveBroadcastRecordPlaybackDlg(Shell parentShell,
            int maxRecordingSeconds, LiveBroadcastSettings settings) {
        super(parentShell, maxRecordingSeconds);
        this.settings = settings;
    }

    @Override
    protected void initializeComponents(Shell shell) {
        super.initializeComponents(shell);
        this.recBtn.setVisible(false);
        this.playBtn.setVisible(false);
        this.okBtn.setVisible(false);
        this.cancelBtn.setVisible(false);
        this.recordAction();
    }

    @Override
    protected void disposed() {
        if (this.shutdownInitializationFailure == false) {
            /**
             * Do not join with the {@link LiveBroadcastThread} if the shutdown
             * is due to an error because it is the one that is triggering the
             * shutdown.
             */
            if (this.broadcastThread != null) {
                this.broadcastThread.halt();
                try {
                    this.broadcastThread.join();
                } catch (InterruptedException e) {
                    // Do Nothing.
                }
                statusHandler
                        .warn("You have just improperly terminated a live broadcast!");
            }
            this.broadcastThread = null;
        }

        super.disposed();
    }

    @Override
    protected void recordAction() {
        try {
            this.initializeBroadcastLive();
        } catch (Exception e) {
            statusHandler.error(
                    "Failed to generate tones for the live broadcast!", e);
            super.cancelAction();
            return;
        }

        this.statusLbl.setText("Initializing ...");
    }

    @Override
    protected void stopAction() {
        if (this.shutdownInitializationFailure) {
            this.okToClose = true;
            /**
             * Do not join with the {@link LiveBroadcastThread} if the shutdown
             * is due to an error because it is the one that is triggering the
             * shutdown.
             */
            return;
        }
        super.stopAction();
        if (this.broadcastThread == null) {
            return;
        }
        this.broadcastThread.halt();
        try {
            this.broadcastThread.join();
        } catch (InterruptedException e) {
            // Do Nothing.
        }
        this.broadcastThread = null;
        super.okAction();
    }

    @Override
    protected void updateRecordingProgressBar() {
        if (this.settings.getType() == BROADCASTTYPE.EO) {
            super.updateRecordingProgressBar();
        }
    }

    private void initializeBroadcastLive() throws Exception {
        ILiveBroadcastMessage command = this.configureBroadcastLive();

        this.broadcastThread = new LiveBroadcastThread(command);
        this.broadcastThread.setListener(this);
        this.broadcastThread.start();
    }

    private ILiveBroadcastMessage configureBroadcastLive() throws Exception {
        // Build the configuration
        LiveBroadcastStartCommand startCommand = new LiveBroadcastStartCommand();
        startCommand.setMsgSource(MSGSOURCE.VIZ);
        if (this.settings.getType() == BROADCASTTYPE.EO) {
            /*
             * first build the SAME tones.
             */
            Map<Transmitter, byte[]> transmitterToneMap = new HashMap<>();
            long longestDurationMS = this.settings
                    .getTransmitterSAMETones(transmitterToneMap);

            for (Transmitter transmitter : transmitterToneMap.keySet()) {
                BroadcastTransmitterConfiguration config = new BroadcastTransmitterConfiguration();
                config.setSelectedMessageType(this.settings
                        .getSelectedMessageType());
                config.setTransmitter(transmitter);
                byte[] tonesAudio = transmitterToneMap.get(transmitter);
                long duration = tonesAudio.length / 160L * 20L;
                config.setToneAudio(transmitterToneMap.get(transmitter));
                config.setEndToneAudio(TonesGenerator.getEndOfMessageTones()
                        .array());
                config.setDelayMilliseconds(longestDurationMS - duration);
                config.setEffectiveTime(this.settings.getEffectiveTime());
                config.setExpireTime(this.settings.getExpireTime());
                config.setPlayAlertTones(this.settings.isPlayAlertTones());
                startCommand.addTransmitterConfiguration(config);
            }
            startCommand.addAllTransmitter(startCommand
                    .getRequestedTransmitters());
        } else if (this.settings.getType() == BROADCASTTYPE.BL) {
            startCommand.setType(this.settings.getType());

            for (TransmitterGroup transmitterGrp : this.settings
                    .getSelectedTransmitterGroups()) {
                BroadcastTransmitterConfiguration config = new BroadcastTransmitterConfiguration();
                config.setTransmitterGroup(transmitterGrp);
                // no tones for broadcast live
                config.setEffectiveTime(this.settings.getEffectiveTime());
                config.setExpireTime(this.settings.getExpireTime());
                config.setPlayAlertTones(this.settings.isPlayAlertTones());
                startCommand.addTransmitterConfiguration(config);
            }
            startCommand.addAllTransmitterGroup(this.settings
                    .getSelectedTransmitterGroups());
        }
        return startCommand;
    }

    private void startBroadcastLive() {
        super.recordAction(this.broadcastThread);
        statusLbl.setText("On the Air!"); // Temporary
    }

    @Override
    public void stateChanged(BROADCAST_STATE state) {
        if (state == BROADCAST_STATE.LIVE) {
            getDisplay().asyncExec(new Runnable() {
                @Override
                public void run() {
                    startBroadcastLive();
                }
            });
        } else if (state == BROADCAST_STATE.ERROR
                || state == BROADCAST_STATE.INIT_ERROR) {

            if (state == BROADCAST_STATE.INIT_ERROR) {
                this.shutdownInitializationFailure = true;
            }

            /*
             * Eliminate any saved audio so that a failed message cannot be
             * scheduled.
             */
            this.recordedAudio = null;

            getDisplay().asyncExec(new Runnable() {
                @Override
                public void run() {
                    stopAction();
                    cancelAction();
                }
            });
        }
    }
}