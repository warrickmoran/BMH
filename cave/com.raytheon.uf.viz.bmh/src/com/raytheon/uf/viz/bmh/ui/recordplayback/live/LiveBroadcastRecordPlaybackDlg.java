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

import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.common.bmh.broadcast.BroadcastTransmitterConfiguration;
import com.raytheon.uf.common.bmh.broadcast.ILiveBroadcastMessage;
import com.raytheon.uf.common.bmh.broadcast.LiveBroadcastStartCommand.BROADCASTTYPE;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.bmh.ui.dialogs.emergencyoverride.AbstractBroadcastSettingsBuilder;
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
 * Nov 21, 2014 3845       bkowal      Use AbstractBroadcastSettingsBuilder.
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

    private final AbstractBroadcastSettingsBuilder settingsBuilder;

    private volatile boolean shutdownInitializationFailure;

    /**
     * @param parentShell
     * @param maxRecordingSeconds
     * 
     *            Will also eventually include additional information specified
     *            on the Emergency Override dialog.
     */
    public LiveBroadcastRecordPlaybackDlg(Shell parentShell,
            int maxRecordingSeconds,
            AbstractBroadcastSettingsBuilder settingsBuilder) {
        super(parentShell, maxRecordingSeconds);
        this.settingsBuilder = settingsBuilder;
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
        if (this.settingsBuilder.getType() == BROADCASTTYPE.EO) {
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
        return this.settingsBuilder.buildBroadcastStartCommand();
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