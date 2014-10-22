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
import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
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
    protected void disposed() {
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

        super.disposed();
    }

    @Override
    protected void recordAction() {
        /*
         * Disable Cancel and OK earlier.
         */
        okBtn.setEnabled(false);
        cancelBtn.setEnabled(false);

        try {
            this.initializeBroadcastLive();
        } catch (Exception e) {
            statusHandler.error(
                    "Failed to generate tones for the live broadcast!", e);
            this.statusLbl.setText("ERROR");
            okBtn.setEnabled(true);
            cancelBtn.setEnabled(true);
            return;
        }

        stopBtn.setEnabled(false);
        recBtn.setEnabled(false);
        playBtn.setEnabled(false);
        this.statusLbl.setText("Initializing ...");
    }

    @Override
    protected void stopAction() {
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
        recBtn.setEnabled(false);
        this.statusLbl.setText("Broadcast Complete");
    }

    private void initializeBroadcastLive() throws Exception {
        ILiveBroadcastMessage command = this.configureBroadcastLive();

        this.broadcastThread = new LiveBroadcastThread(command);
        this.broadcastThread.setListener(this);
        this.broadcastThread.start();
    }

    private ILiveBroadcastMessage configureBroadcastLive() throws Exception {
        /*
         * first build the SAME tones.
         */
        Map<Transmitter, byte[]> transmitterToneMap = new HashMap<>();
        long longestDurationMS = this.settings
                .getTransmitterSAMETones(transmitterToneMap);

        // TODO: calculate delays ...

        // Build the configuration
        LiveBroadcastStartCommand startCommand = new LiveBroadcastStartCommand();
        startCommand.setMsgSource(ILiveBroadcastMessage.SOURCE_VIZ);
        for (Transmitter transmitter : transmitterToneMap.keySet()) {
            BroadcastTransmitterConfiguration config = new BroadcastTransmitterConfiguration();
            config.setTransmitter(transmitter);
            byte[] tonesAudio = transmitterToneMap.get(transmitter);
            long duration = tonesAudio.length / 160L * 20L;
            config.setToneAudio(transmitterToneMap.get(transmitter));
            config.setDelayMilliseconds(longestDurationMS - duration);
            startCommand.addTransmitterConfiguration(config);
        }
        startCommand.addAllTransmitter(startCommand.getRequestedTransmitters());
        return startCommand;
    }

    private void startBroadcastLive() {
        if (this.isDisposed()) {
            return;
        }
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
        } else if (state == BROADCAST_STATE.ERROR) {
            /*
             * Eliminate any saved audio so that a failed message cannot be
             * scheduled.
             */
            this.recordedAudio = null;

            getDisplay().asyncExec(new Runnable() {
                @Override
                public void run() {
                    if (isDisposed()) {
                        return;
                    }
                    stopAction();

                    statusLbl.setText("ERROR");
                    // reset the dialog
                    stopBtn.setEnabled(false);
                    recBtn.setEnabled(true);
                    playBtn.setEnabled(false);
                }
            });
        }
    }
}