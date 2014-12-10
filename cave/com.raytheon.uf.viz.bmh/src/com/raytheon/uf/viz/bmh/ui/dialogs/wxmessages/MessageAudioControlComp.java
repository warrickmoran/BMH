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

import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;

import com.raytheon.uf.common.bmh.request.InputMessageAudioData;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.bmh.dialogs.notify.BMHDialogNotificationManager;
import com.raytheon.uf.viz.bmh.ui.common.utility.RecordImages;
import com.raytheon.uf.viz.bmh.ui.common.utility.RecordImages.RecordAction;
import com.raytheon.uf.viz.bmh.ui.recordplayback.AudioException;
import com.raytheon.uf.viz.bmh.ui.recordplayback.AudioPlaybackThread;
import com.raytheon.uf.viz.bmh.ui.recordplayback.AudioRecordPlaybackNotification;
import com.raytheon.uf.viz.bmh.ui.recordplayback.IPlaybackCompleteListener;

/**
 * 
 * Composite that displays the audio description, a play progress bar or label
 * indicating if the audio generation failed, and the audio play buttons. If the
 * audio failed to generate then the buttons are not displayed.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 26, 2014  #3728     lvenable     Initial creation
 * Oct 26, 2014  #3748     bkowal       Implement audio playback.
 * Dec 09, 2014  #3904     bkowal       Publish a {@link AudioRecordPlaybackNotification}
 *                                      prior to the start of audio playback.
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */
public class MessageAudioControlComp extends Composite implements
        IPlaybackCompleteListener {

    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(MessageAudioControlComp.class);

    private InputMessageAudioData audioData;

    /** Progress bar used for displaying the audio being played. */
    private ProgressBar progressBar;

    /** Font use with the fail generation label. */
    private Font lblFont;

    /** Record images used for displaying on buttons, etc. */
    private RecordImages recordImages;

    /** The Play button. */
    private Button playBtn;

    /** The Pause button. */
    private Button pauseBtn;

    /** The stop button. */
    private Button stopBtn;

    /** Flag indicating if the audio file is being played. */
    private boolean isPlaying = false;

    /** Callback call when the audio buttons are clicked. */
    private IAudioControlAction audioControlActionCB = null;

    /** Controls audio playback. Will be recreated whenever it is stopped. **/
    private AudioPlaybackThread playbackThread;

    /** playback timer - used to update the progress bar. */
    private ScheduledExecutorService timer;

    private volatile boolean shouldResume = false;

    /**
     * Constructor.
     * 
     * @param parent
     *            Parent composite.
     * @param audioData
     *            Audio data
     * @param recordImages
     *            Record images.
     * @param audioControlActionCB
     *            CallbacK called when the audio buttons are clicked.
     */
    public MessageAudioControlComp(Composite parent,
            InputMessageAudioData audioData, RecordImages recordImages,
            IAudioControlAction audioControlActionCB) {
        super(parent, SWT.NONE);

        this.audioData = audioData;
        this.recordImages = recordImages;
        this.audioControlActionCB = audioControlActionCB;

        parent.addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {
                if (lblFont != null) {
                    lblFont.dispose();
                }
            }
        });

        init();

        enableAudioControls(true);

        this.pack();
    }

    /**
     * Initialize method.
     */
    private void init() {
        GridLayout gl = new GridLayout(5, false);
        gl.marginHeight = 1;
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        this.setLayout(gl);
        this.setLayoutData(gd);

        gd = new GridData(200, SWT.DEFAULT);
        Label transmitterNameLbl = new Label(this, SWT.NONE);
        transmitterNameLbl.setText(audioData.getTransmitterGroupName());
        transmitterNameLbl.setLayoutData(gd);
        transmitterNameLbl.setToolTipText(audioData.getTransmitterGroupName());

        if (audioData.isSuccess()) {
            gd = new GridData(SWT.FILL, SWT.CENTER, true, true);
            gd.horizontalIndent = 10;
            gd.minimumWidth = 200;
            gd.heightHint = 15;
            progressBar = new ProgressBar(this, SWT.HORIZONTAL | SWT.SMOOTH);
            progressBar.setLayoutData(gd);

            progressBar.setMinimum(0);
            // maximum is length of the audio in seconds.
            progressBar.setMaximum(this.audioData.getAudioDuration());
        } else {
            gd = new GridData(SWT.FILL, SWT.CENTER, true, true);
            gd.minimumWidth = 200;
            gd.horizontalIndent = 10;
            Label failedLbl = new Label(this, SWT.BORDER | SWT.CENTER);
            failedLbl.setText("Generation Failed");
            failedLbl.setLayoutData(gd);

            FontData fd = failedLbl.getFont().getFontData()[0];
            fd.setStyle(SWT.BOLD);
            lblFont = new Font(getDisplay(), fd);
            failedLbl.setFont(lblFont);

            failedLbl.setBackground(getDisplay().getSystemColor(SWT.COLOR_RED));
        }

        gd = new GridData();
        gd.horizontalIndent = 10;
        playBtn = new Button(this, SWT.PUSH);
        playBtn.setImage(recordImages.getImage(RecordAction.PLAY));
        playBtn.setLayoutData(gd);
        playBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                isPlaying = true;
                fireEnableCallbackAction(false);
                handlePlayAction();
            }
        });

        pauseBtn = new Button(this, SWT.PUSH);
        pauseBtn.setImage(recordImages.getImage(RecordAction.PAUSE));
        pauseBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                isPlaying = false;
                fireEnableCallbackAction(true);
                handlePauseAction();
            }
        });

        stopBtn = new Button(this, SWT.PUSH);
        stopBtn.setImage(recordImages.getImage(RecordAction.STOP));
        stopBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                isPlaying = false;
                fireEnableCallbackAction(true);
                handleStopAction();
            }
        });

        /*
         * Hide the buttons if the audio generation failed. The reason we create
         * the buttons and then hide them is it will maintain the alignment with
         * the controls on the other audio composites.
         */
        if (audioData.isSuccess() == false) {
            playBtn.setVisible(false);
            pauseBtn.setVisible(false);
            stopBtn.setVisible(false);
        }
    }

    private void handlePlayAction() {
        BMHDialogNotificationManager.getInstance().post(
                new AudioRecordPlaybackNotification());

        try {
            this.playbackThread = new AudioPlaybackThread(
                    this.audioData.getAudio());
            this.playbackThread.setCompleteListener(this);
        } catch (AudioException e) {
            statusHandler.error("Failed to load the audio for playback!", e);
            return;
        }
        this.progressBar.setSelection(0);
        timer = Executors.newSingleThreadScheduledExecutor();
        this.playbackThread.start();
        timer.scheduleAtFixedRate(new ElapsedTimerTask(), 1000, 1000,
                TimeUnit.MILLISECONDS);
    }

    private void handlePauseAction() {
        if (this.shouldResume) {
            this.shouldResume = false;
            timer = Executors.newSingleThreadScheduledExecutor();
            this.playbackThread.resumePlayback();
            timer.scheduleAtFixedRate(new ElapsedTimerTask(), 1000, 1000,
                    TimeUnit.MILLISECONDS);
        } else {
            this.shouldResume = true;
            this.playbackThread.pausePlayback();
            this.shutdownTimer();
        }
    }

    private void handleStopAction() {
        this.shouldResume = false;
        this.shutdownTimer();
        this.playbackThread.halt();
        this.progressBar.setSelection(0);
    }

    private void shutdownTimer() {
        if (this.timer == null) {
            // stopped while paused.
            return;
        }
        timer.shutdown();
        timer = null;
    }

    /**
     * Call the callback to enable/disable the other audio controls on the
     * display.
     * 
     * @param enable
     *            True to enable, false to disable.
     */
    private void fireEnableCallbackAction(boolean enable) {
        if (audioControlActionCB != null) {
            audioControlActionCB.enableAudioControls(enable);
        }
    }

    /**
     * Enable/Disable the audio controls using the enable flag.
     * 
     * @param enable
     *            True to enable, false to disable.
     */
    public void enableAudioControls(boolean enable) {
        // If the audio is currently playing then return as no action is needed.
        if (isPlaying) {
            return;
        }

        // If the audio was not successfully generated then return as the audio
        // control are not visible anyway.
        if (audioData.isSuccess() == false) {
            return;
        }

        // Enable/Disable the audio controls.
        playBtn.setEnabled(enable);
        pauseBtn.setEnabled(enable);
        stopBtn.setEnabled(enable);
    }

    private void updatePlaybackStatus() {
        if (this.progressBar.getSelection() == this.progressBar.getMaximum()) {
            return;
        }
        this.progressBar.setSelection(this.progressBar.getSelection() + 1);
    }

    /**
     * Elapsed timer task called when the timer fires.
     */
    private class ElapsedTimerTask extends TimerTask {
        @Override
        public void run() {
            getDisplay().asyncExec(new Runnable() {
                @Override
                public void run() {
                    updatePlaybackStatus();
                }
            });
        }
    }

    @Override
    public void notifyPlaybackComplete() {
        this.shutdownTimer();
        this.shouldResume = false;
        this.isPlaying = false;
        // Not on the UI Thread.
        getDisplay().asyncExec(new Runnable() {
            @Override
            public void run() {
                fireEnableCallbackAction(true);
                shutdownTimer();
            }
        });
    }
}