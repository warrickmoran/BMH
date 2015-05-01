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
package com.raytheon.uf.viz.bmh.ui.dialogs.wxmessages;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.bmh.data.BmhUtils;
import com.raytheon.uf.viz.bmh.dialogs.notify.BMHDialogNotificationManager;
import com.raytheon.uf.viz.bmh.ui.common.utility.RecordImages;
import com.raytheon.uf.viz.bmh.ui.common.utility.RecordImages.RecordAction;
import com.raytheon.uf.viz.bmh.ui.recordplayback.AudioException;
import com.raytheon.uf.viz.bmh.ui.recordplayback.AudioPlaybackCompleteNotification;
import com.raytheon.uf.viz.bmh.ui.recordplayback.AudioPlaybackThread;
import com.raytheon.uf.viz.bmh.ui.recordplayback.AudioRecordPlaybackNotification;
import com.raytheon.uf.viz.bmh.ui.recordplayback.IPlaybackCompleteListener;
import com.raytheon.uf.viz.core.VizApp;

/**
 * Handles synthesis and audio playback management of text entered into the
 * message field on the {@link MessageContentsDlg}. Handling has been moved into
 * this delegate to avoid polluting the {@link MessageContentsDlg} with a lot of
 * extra code specific to the management of audio playback.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 9, 2014  3883       bkowal      Initial creation
 * Feb 16, 2015 4112       bkowal      Publish a {@link AudioPlaybackCompleteNotification} when
 *                                     playback concludes.
 * Apr 29, 2015 4451       bkowal      Synthesize the message text using the Voice
 *                                     associated with the selected message type.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class TextAudioPlaybackDelegate implements ModifyListener,
        IPlaybackCompleteListener {
    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(TextAudioPlaybackDelegate.class);

    private static enum PLAY_PAUSE_STATE {
        PLAY, PAUSE
    }

    private final String PAUSE_TEXT = "Pause";

    /**
     * Tooltip text for the Pause {@link Button} indicating that the Pause
     * {@link Button} will pause the audio playback.
     */
    private final String PAUSE_TOOLTIP_STATE_P = "Pause message playback";

    /**
     * Tooltip text for the Pause {@link Button} indicating that the Pause
     * {@link Button} will resume the audio playback.
     */
    private final String PAUSE_TOOLTIP_STATE_R = "Resume message playback";

    private final Shell shell;

    private final Button playPauseButton;

    private final Button stopButton;

    /** Button Images **/
    private final RecordImages recordImages;

    /** The initial play {@link Button} text **/
    private final String playText;

    /** The initial play tooltip {@link Button} text **/
    private final String playTooltip;

    /** Text entered into the {@link StyledText} text area **/
    private String inputText;

    private Exception pMonitorSynException;

    /** Cache the text that was synthesized and the result of the synthesis **/
    private byte[] textAudio;

    private String encodedText;

    private PLAY_PAUSE_STATE playPauseState;

    /** Controls audio playback. Will be recreated whenever it is stopped. **/
    private AudioPlaybackThread playbackThread;

    /**
     * Exists to handle the case when a user closes the dialog during audio
     * playback.
     **/
    private volatile boolean disposing;

    private final int voiceNumber;

    /**
     * Constructor
     * 
     * @param shell
     *            the {@link Shell} associated with the
     *            {@link MessageContentsDlg}.
     * @param startPauseButton
     *            the {@link Button} added to the {@link MessageContentsDlg}
     *            that will be used to start/pause audio playback
     * @param stopButton
     *            the {@link Button} that was added to the
     *            {@link MessageContentsDlg} that will be used to stop audio
     *            playback.
     * @param recordImages
     *            Class that generates the Record, Stop, and Play images.
     */
    public TextAudioPlaybackDelegate(final Shell shell,
            final Button startPauseButton, final Button stopButton,
            RecordImages recordImages, final int voiceNumber) {
        this.shell = shell;

        this.playPauseButton = startPauseButton;

        this.stopButton = stopButton;

        this.recordImages = recordImages;

        this.voiceNumber = voiceNumber;

        this.playText = this.playPauseButton.getText();
        this.playTooltip = this.playPauseButton.getToolTipText();

        /*
         * Initial States.
         */
        this.playPauseButton.setEnabled(false);
        this.stopButton.setEnabled(false);
        this.playPauseState = PLAY_PAUSE_STATE.PLAY;

        /*
         * Event Handling.
         */
        this.playPauseButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handlePlayPauseTransition();
            }
        });
        this.stopButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleStopAction();
            }
        });
    }

    /**
     * Stops any running playback threads so that the dialog can safely be
     * closed.
     */
    public void dispose() {
        this.disposing = true;
        if (this.playbackThread != null) {
            this.playbackThread.halt();
        }
    }

    /**
     * Handles user interactions with the {@link #playPauseButton}. Final action
     * taken is based on the current {@link #playPauseState}.
     */
    private void handlePlayPauseTransition() {
        switch (this.playPauseState) {
        case PAUSE:
            this.handlePause();
            break;
        case PLAY:
            this.handlePlay();
            break;
        }
    }

    /**
     * Handles the {@link #playPauseButton} action when the
     * {@link #playPauseState} is in the {@link PLAY_PAUSE_STATE#PLAY} state.
     */
    private void handlePlay() {
        /* Determine if audio needs to be synthesized */
        if (this.encodedText == null
                || this.encodedText.equals(this.inputText) == false) {
            try {
                this.synthesizeAudio();
            } catch (Exception e) {
                // audio synthesis has failed.
                statusHandler.error("Failed to play the audio!", e);
                return;
            }
        }

        /* audio is ready, thread the playback. */
        try {
            this.playbackThread = new AudioPlaybackThread(this.textAudio);
        } catch (AudioException e) {
            statusHandler.error("Failed to initialize audio playback!", e);
            return;
        }
        BMHDialogNotificationManager.getInstance().post(
                new AudioRecordPlaybackNotification());
        this.playbackThread.setCompleteListener(this);

        this.stopButton.setEnabled(true);
        this.playbackThread.start();

        /* transition to the pause state */
        this.playPauseState = PLAY_PAUSE_STATE.PAUSE;
        this.updatePlayPauseButton();
    }

    /**
     * Handles the {@link #playPauseButton} action when the
     * {@link #playPauseState} is in the {@link PLAY_PAUSE_STATE#PAUSE} state.
     */
    private void handlePause() {
        if (this.playbackThread.isPaused()) {
            this.playPauseButton.setToolTipText(PAUSE_TOOLTIP_STATE_P);
            this.playbackThread.resumePlayback();
        } else {
            this.playPauseButton.setToolTipText(PAUSE_TOOLTIP_STATE_R);
            this.playbackThread.pausePlayback();
        }
    }

    /**
     * Updates the {@link #playPauseButton} text, tooltip, and image to match
     * the current {@link #playPauseState}.
     */
    private void updatePlayPauseButton() {
        final String buttonTxt = (this.playPauseState == PLAY_PAUSE_STATE.PAUSE) ? PAUSE_TEXT
                : this.playText;
        final String tooltipTxt = (this.playPauseState == PLAY_PAUSE_STATE.PAUSE) ? PAUSE_TOOLTIP_STATE_P
                : this.playTooltip;
        final RecordAction recordAction = (this.playPauseState == PLAY_PAUSE_STATE.PAUSE) ? RecordAction.PAUSE
                : RecordAction.PLAY;
        this.playPauseButton.setText(buttonTxt);
        this.playPauseButton.setToolTipText(tooltipTxt);
        this.playPauseButton.setImage(this.recordImages.getImage(recordAction));
    }

    /**
     * Handles the stop action when the user interacts with the
     * {@link #stopButton}.
     */
    private void handleStopAction() {
        this.playbackThread.halt();

        this.executeStopTransition();
    }

    /**
     * Performs any needed GUI updates when the audio playback has been
     * terminated. Separate from {@link #handleStopAction()} to handle stop
     * actions triggered by the {@link IPlaybackCompleteListener}.
     */
    private void executeStopTransition() {
        BMHDialogNotificationManager.getInstance().post(
                new AudioPlaybackCompleteNotification());

        if (this.disposing) {
            return;
        }
        /* transition to the play state */
        this.playPauseState = PLAY_PAUSE_STATE.PLAY;
        this.stopButton.setEnabled(false);
        this.updatePlayPauseButton();
    }

    /**
     * Uses {@link BmhUtil} to invoke audio synthesis.
     * 
     * @throws Exception
     *             if the audio synthesis fails
     */
    private void synthesizeAudio() throws Exception {
        pMonitorSynException = null;

        /**
         * Synthesis of audio is currently not an operation that can be
         * cancelled and we do not want there to be a void between when the user
         * clicks on the "Play" button and audio actually starts playing.
         */
        ProgressMonitorDialog dialog = new ProgressMonitorDialog(this.shell);
        dialog.run(true, false, new IRunnableWithProgress() {
            @Override
            public void run(IProgressMonitor monitor) {
                monitor.beginTask("Generating Audio ...",
                        IProgressMonitor.UNKNOWN);
                try {
                    textAudio = BmhUtils.textToAudio(inputText, voiceNumber);
                } catch (Exception e) {
                    pMonitorSynException = e;
                }
                monitor.done();
            }
        });

        if (pMonitorSynException != null) {
            throw pMonitorSynException;
        }

        // on success
        this.encodedText = this.inputText;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events
     * .ModifyEvent)
     */
    @Override
    public void modifyText(ModifyEvent e) {
        StyledText messageST = (StyledText) e.getSource();
        this.inputText = messageST.getText().trim();
        if (this.inputText.isEmpty()) {
            this.playPauseButton.setEnabled(false);
        } else {
            this.playPauseButton.setEnabled(true);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.viz.bmh.ui.recordplayback.IPlaybackCompleteListener#
     * notifyPlaybackComplete()
     */
    @Override
    public void notifyPlaybackComplete() {
        this.playbackThread = null;
        VizApp.runAsync(new Runnable() {
            @Override
            public void run() {
                executeStopTransition();
            }
        });
    }
}