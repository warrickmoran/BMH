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

import com.raytheon.uf.common.bmh.comms.LiveBroadcastStartData;
import com.raytheon.uf.common.bmh.comms.StartLiveBroadcastRequest;
import com.raytheon.uf.viz.bmh.ui.recordplayback.RecordPlaybackDlg;
import com.raytheon.uf.viz.bmh.ui.recordplayback.live.LiveBroadcastThread.BROADCAST_STATE;
import com.raytheon.uf.viz.core.VizApp;

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
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class LiveBroadcastRecordPlaybackDlg extends RecordPlaybackDlg implements
        IBroadcastStateListener {

    private LiveBroadcastThread broadcastThread;

    /**
     * @param parentShell
     * @param maxRecordingSeconds
     * 
     *            Will also eventually include additional information specified
     *            on the Emergency Override dialog.
     */
    public LiveBroadcastRecordPlaybackDlg(Shell parentShell,
            int maxRecordingSeconds) {
        super(parentShell, maxRecordingSeconds);
    }

    @Override
    protected void recordAction() {
        stopBtn.setEnabled(false);
        recBtn.setEnabled(false);
        playBtn.setEnabled(false);
        this.statusLbl.setText("Initializing ...");

        this.initializeBroadcastLive();
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
    }

    private void initializeBroadcastLive() {
        /* Prepare the request objects. */
        // Create the start broadcast request.
        StartLiveBroadcastRequest request = new StartLiveBroadcastRequest();
        request.setWsid(VizApp.getWsId().toString());

        // Sample Broadcast Live Configuration
        // Actual information will be based on the Emergency Override Dialog
        // Could potentially be created in the broadcast live thread.
        LiveBroadcastStartData startData = new LiveBroadcastStartData();
        startData.setTransmitterGroup("BAS");
        request.addLiveBroadcastStartData(startData);

        this.broadcastThread = new LiveBroadcastThread(request);
        this.broadcastThread.setListener(this);
        this.broadcastThread.start();
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
        } else if (state == BROADCAST_STATE.ERROR) {
            getDisplay().asyncExec(new Runnable() {
                @Override
                public void run() {
                    // reset the dialog
                    stopBtn.setEnabled(false);
                    recBtn.setEnabled(true);
                    playBtn.setEnabled(false);
                }
            });
        }
    }
}