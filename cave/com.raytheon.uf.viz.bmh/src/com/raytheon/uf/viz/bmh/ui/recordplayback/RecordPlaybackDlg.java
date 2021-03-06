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
package com.raytheon.uf.viz.bmh.ui.recordplayback;

import java.nio.ByteBuffer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.common.bmh.audio.AudioRegulationConfiguration;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.bmh.data.BmhUtils;
import com.raytheon.uf.viz.bmh.dialogs.notify.BMHDialogNotificationManager;
import com.raytheon.uf.viz.bmh.ui.common.utility.DialogUtility;
import com.raytheon.uf.viz.bmh.ui.common.utility.RecordImages;
import com.raytheon.uf.viz.bmh.ui.common.utility.RecordImages.RecordAction;
import com.raytheon.viz.ui.dialogs.CaveSWTDialog;

/**
 * Dialog to record and play back messages.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 15, 2014  #3329     lvenable     Initial creation
 * Sep 14, 2014  #3610     lvenable     Updated to show when 30 seconds of recording
 *                                      is left and fixed a couple of bugs.
 * Oct 08, 2014  #3479     lvenable     Changed MODE_INDEPENDENT to PERSPECTIVE_INDEPENDENT.
 * Oct 8, 2014   #3657     bkowal       Implemented audio recording and playback.
 * Oct 10, 2014  #3656     bkowal       Adjustments to allow for extension.
 * Oct 16, 2014  #3657     bkowal       Block until close to capture recorded audio.
 * Oct 21, 2014  #3655     bkowal       Handle forced dialog closures.
 * Oct 26, 2014  #3712     bkowal       Prevent the dialog from being closed during
 *                                      recording / playback.
 * Nov 1, 2014   #3655     bkowal       Increased the number of live audio bytes
 *                                      that are sent to the comms manager.
 * Nov 1, 2014   #3657     bkowal       Created okAction and cancelAction for subclasses.
 * Nov 5, 2014   #3780     bkowal       Prevent timer update when the dialog has been closed / is closing.
 * Nov 12, 2014  #3819     bkowal       Disable OK/Cancel during audio playback.
 * Nov 17, 2014  #3820     bkowal       okToClose is now protected.
 * Nov 17, 2014  #3808     bkowal       Support broadcast live.
 * Nov 22, 2014  #3862     bkowal       Preparation for realtime graphing of audio decibel levels.
 * Nov 24, 2014  #3863     bkowal       Use {@link DecibelPlotsComp} to provide a realtime plot
 *                                      of the decibel levels of the recorded audio.
 * Dec 09, 2014  #3904     bkowal       Publish a {@link AudioRecordPlaybackNotification}
 *                                      prior to the start of audio recording / playback.
 * Feb 12, 2015  #4076     bkowal       Update the status label based on the action that was performed.
 * Feb 16, 2015  #4112     bkowal       Publish a {@link AudioPlaybackCompleteNotification} when audio
 *                                      playback or recording has concluded.
 * Jun 18, 2015  #4490     bkowal       Disseminate a {@link AudioPlaybackCompleteNotification} a
 *                                      after the playback of recorded audio concludes.
 * Aug 17, 2015  #4757     bkowal       Recorded audio retrieval can now trigger an unlikely Exception.
 * Aug 24, 2015  #4770     bkowal       Handle {@link Exception} from the {@link AudioRecorderThread}.
 * Aug 25, 2015  #4771     bkowal       Updated to retrieve and use {@link AudioRegulationConfiguration}.
 * Sep 01, 2015  #4771     bkowal       {@link AudioRegulationConfiguration} retrieval has been relocated
 *                                      to {@link BmhUtils}.
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */
public class RecordPlaybackDlg extends CaveSWTDialog implements
        IPlaybackCompleteListener {

    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(RecordPlaybackDlg.class);

    public static final int INDETERMINATE_PROGRESS = Integer.MAX_VALUE;

    private static final String DEFAULT_STATUS_TEXT = "Press REC Button to Start Recording...";

    /** Status label. */
    /*
     * TODO: Still need to manage that status label display across the different
     * states. The status text that is displayed will probably be dependent on
     * whether this dialog was launched from the Weather Messages dialog or the
     * Emergency Override dialog (ex: Recording Message will be 'On the Air'
     * when started from the Emergency Override dialog).
     */
    protected Label statusLbl;

    /** Status label font. */
    private Font statusLabelFont;

    /** Elapsed time font. */
    private Font elapsedTimeFont;

    /** Recording progress bar. */
    protected ProgressBar recordingProgBar;

    /** Update rate in milliseconds. */
    private long updateRate = 1000;

    /** Label showing the time elapsed during recording. */
    private Label elapsedTimeLbl;

    /** Minutes elapsed during recording/playback. */
    private int elapsedMinutes = 0;

    /** Seconds elapsed during recording/playback. */
    private int elapsedSeconds = 0;

    /** Hours elapsed during recording/playback. */
    private int elapsedHours = 0;

    /** Total seconds elapsed during recording/playback */
    private int totalElapsedSeconds = 0;

    /** Maximum number of second allowed for recording. */
    private int maxRecordingSeconds = 0;

    /** Minute/Second format string. */
    private String minutesSecondsFormatStr = "%02d:%02d";

    /** Recording/playback timer. */
    private ScheduledExecutorService timer;

    /** Record button. */
    protected Button recBtn;

    /** Stop button. */
    protected Button stopBtn;

    /** Play button. */
    protected Button playBtn;

    /*
     * TODO: may want to update okBtn so that it is only enabled after a
     * successful recording / broadcast.
     */
    protected Button okBtn;

    protected Button cancelBtn;

    private enum RecordPlayStatus {
        RECORD, PLAY, STOP
    };

    /*
     * This will not matter for the initial implementation which will just
     * support recording and playback. However, we will want to optimize the
     * number of samples read between audio live streaming segments for the live
     * broadcast.
     */
    private static final int SAMPLE_COUNT = 20;

    private volatile RecordPlayStatus recordPlayStatus = RecordPlayStatus.STOP;

    private AudioRecorderThread recorderThread;

    private AudioPlaybackThread playbackThread;

    protected ByteBuffer recordedAudio;

    protected volatile boolean okToClose;

    private DecibelPlotsComp decibelPlotsComp;

    protected final AudioRegulationConfiguration configuration;

    /**
     * Constructor.
     * 
     * @param parentShell
     *            Parent shell.
     * @param maxRecordingSeconds
     *            Maximum seconds to record.
     */
    public RecordPlaybackDlg(Shell parentShell, int maxRecordingSeconds)
            throws Exception {
        super(parentShell, SWT.DIALOG_TRIM | SWT.MIN | SWT.PRIMARY_MODAL,
                CAVE.PERSPECTIVE_INDEPENDENT | CAVE.DO_NOT_BLOCK);
        configuration = BmhUtils.retrieveRegulationConfiguration();
        this.maxRecordingSeconds = maxRecordingSeconds;
    }

    @Override
    protected Layout constructShellLayout() {
        // Create the main layout for the shell.
        GridLayout mainLayout = new GridLayout(1, false);
        mainLayout.marginHeight = 2;
        mainLayout.marginWidth = 2;

        return mainLayout;
    }

    @Override
    protected Object constructShellLayoutData() {
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        return gd;
    }

    @Override
    protected void disposed() {
        shutdownTimer();
        statusLabelFont.dispose();
        elapsedTimeFont.dispose();
        if (this.playbackThread != null) {
            this.playbackThread.dispose();
            try {
                this.playbackThread.join();
            } catch (InterruptedException e) {
                // Do Nothing.
            }
            this.playbackThread = null;
        }
        if (this.recorderThread != null) {
            this.recorderThread.halt();
            try {
                this.recorderThread.join();
            } catch (InterruptedException e) {
                // Do Nothing.
            }
        }
    }

    @Override
    protected void initializeComponents(Shell shell) {
        setText("Message Record/Playback");

        init();
        shell.addShellListener(new ShellAdapter() {
            @Override
            public void shellClosed(ShellEvent e) {
                /*
                 * only allow the user to close the dialog when the audio
                 * threads (record / play) are stopped.
                 */
                e.doit = okToClose;
            }
        });
    }

    /**
     * Initialize method.
     */
    private void init() {
        createStatusLabel();
        DialogUtility.addSeparator(shell, SWT.HORIZONTAL);
        createRecordingControls();
        DialogUtility.addSeparator(shell, SWT.HORIZONTAL);
        this.createDecibelPlots();
        DialogUtility.addSeparator(shell, SWT.HORIZONTAL);
        createBottomButtons();
    }

    /**
     * Create the status label.
     */
    private void createStatusLabel() {
        Composite labelComp = new Composite(shell, SWT.NONE);
        labelComp.setLayout(new GridLayout(1, true));
        labelComp
                .setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        statusLbl = new Label(labelComp, SWT.CENTER);

        statusLbl.setText(DEFAULT_STATUS_TEXT);
        statusLbl.setLayoutData(gd);
        FontData fd = statusLbl.getFont().getFontData()[0];
        fd.setHeight(18);
        statusLabelFont = new Font(getDisplay(), fd);
        statusLbl.setFont(statusLabelFont);
        statusLbl.setForeground(getDisplay().getSystemColor(SWT.COLOR_RED));
    }

    /**
     * Create the recoding controls.
     */
    private void createRecordingControls() {
        Composite recordingComp = new Composite(shell, SWT.NONE);
        recordingComp.setLayout(new GridLayout(2, false));
        recordingComp.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true,
                false));

        // Filler label
        new Label(recordingComp, SWT.NONE);

        elapsedTimeFont = new Font(getDisplay(), "Courier", 12, SWT.BOLD);
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        elapsedTimeLbl = new Label(recordingComp, SWT.CENTER);
        elapsedTimeLbl.setText(String.format(minutesSecondsFormatStr, 0, 0));
        elapsedTimeLbl.setLayoutData(gd);
        elapsedTimeLbl.setFont(elapsedTimeFont);

        /*
         * Label and progress bar.
         */
        Label elapsedLbl = new Label(recordingComp, SWT.NONE);
        elapsedLbl.setText("Elapsed Time: ");

        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gd.minimumWidth = 350;
        if (this.maxRecordingSeconds == INDETERMINATE_PROGRESS) {
            recordingProgBar = new ProgressBar(recordingComp, SWT.HORIZONTAL
                    | SWT.SMOOTH | SWT.INDETERMINATE);
        } else {
            recordingProgBar = new ProgressBar(recordingComp, SWT.HORIZONTAL
                    | SWT.SMOOTH);
            recordingProgBar.setMinimum(0);
            recordingProgBar.setMaximum(maxRecordingSeconds);
        }
        recordingProgBar.setLayoutData(gd);

        // Filler label
        new Label(recordingComp, SWT.NONE);

        /*
         * Recording buttons.
         */
        Composite recordButtonsComp = new Composite(recordingComp, SWT.NONE);
        recordButtonsComp.setLayout(new GridLayout(3, false));
        recordButtonsComp.setLayoutData(new GridData(SWT.CENTER, SWT.DEFAULT,
                true, false));

        RecordImages ri = new RecordImages(shell);

        int buttonWidth = 80;

        gd = new GridData(buttonWidth, SWT.DEFAULT);
        recBtn = new Button(recordButtonsComp, SWT.PUSH);
        recBtn.setImage(ri.getImage(RecordAction.RECORD));
        recBtn.setText("Rec");
        recBtn.setLayoutData(gd);
        recBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                recordAction();
            }
        });

        gd = new GridData(buttonWidth, SWT.DEFAULT);
        stopBtn = new Button(recordButtonsComp, SWT.PUSH);
        stopBtn.setImage(ri.getImage(RecordAction.STOP));
        stopBtn.setText("Stop");
        stopBtn.setEnabled(false);
        stopBtn.setLayoutData(gd);
        stopBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                stopAction();
            }
        });

        gd = new GridData(buttonWidth, SWT.DEFAULT);
        playBtn = new Button(recordButtonsComp, SWT.PUSH);
        playBtn.setImage(ri.getImage(RecordAction.PLAY));
        playBtn.setText("Play");
        playBtn.setEnabled(false);
        playBtn.setLayoutData(gd);
        playBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                playAction();
            }
        });
    }

    private void createDecibelPlots() {
        this.decibelPlotsComp = new DecibelPlotsComp(this.shell);
    }

    /**
     * Create the bottom action buttons.
     */
    private void createBottomButtons() {
        Composite buttonComp = new Composite(shell, SWT.NONE);
        buttonComp.setLayout(new GridLayout(2, true));
        buttonComp.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true,
                false));

        int buttonWidth = 70;

        GridData gd = new GridData(SWT.RIGHT, SWT.DEFAULT, true, false);
        gd.widthHint = buttonWidth;
        okBtn = new Button(buttonComp, SWT.PUSH);
        okBtn.setText("OK");
        okBtn.setLayoutData(gd);
        okBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                okAction();
            }
        });

        gd = new GridData(SWT.LEFT, SWT.DEFAULT, true, false);
        gd.widthHint = buttonWidth;
        cancelBtn = new Button(buttonComp, SWT.PUSH);
        cancelBtn.setText("Cancel");
        cancelBtn.setLayoutData(gd);
        cancelBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                cancelAction();
            }
        });
    }

    /**
     * Update the recording time label and the progress bar.
     */
    private void updateRecordingTime() {
        if (this.isDisposed()) {
            /*
             * Handle the rare case when the dialog may have been closed just as
             * the last timer task was executed due to the fact that the timer
             * shutdown allows any scheduled tasks to run one final time.
             */
            return;
        }
        calculateHoursMinutesSeconds();
        String hoursString = StringUtils.EMPTY;
        if (this.elapsedHours > 0) {
            hoursString = Integer.toString(elapsedHours) + ":";
        }
        elapsedTimeLbl.setText(hoursString
                + String.format(minutesSecondsFormatStr, elapsedMinutes,
                        elapsedSeconds));

        this.updateRecordingProgressBar();
    }

    protected void updateRecordingProgressBar() {
        if (totalElapsedSeconds <= recordingProgBar.getMaximum()) {
            recordingProgBar.setSelection(totalElapsedSeconds);
        }

        if (this.recordPlayStatus != RecordPlayStatus.RECORD) {
            return;
        }

        // If the total time equals the maximum number of seconds then the limit
        // was reached and the recording needs to stop.
        if (totalElapsedSeconds >= maxRecordingSeconds) {
            stopAction();
        }

        if ((maxRecordingSeconds - totalElapsedSeconds) <= 30
                && recordPlayStatus == RecordPlayStatus.RECORD) {
            statusLbl.setText("Warning: 30 secs for recording");
        }
    }

    /**
     * Calculate the minutes and seconds to be displayed.
     */
    private void calculateHoursMinutesSeconds() {
        ++totalElapsedSeconds;
        ++elapsedSeconds;
        if (elapsedSeconds > 59) {
            elapsedSeconds = 0;
            ++elapsedMinutes;
        }
        if (elapsedMinutes > 59) {
            elapsedMinutes = 0;
            ++elapsedHours;
        }
    }

    /**
     * Record action.
     */
    protected void recordAction() {
        this.recordAction(null);
    }

    /**
     * Record action.
     * 
     * @param listener
     *            the optional audio recording listener.
     */
    protected void recordAction(final IAudioRecorderListener listener) {
        this.okToClose = false;
        resetRecordPlayValues();
        this.decibelPlotsComp.resetPlots();
        try {
            this.recorderThread = new AudioRecorderThread(SAMPLE_COUNT,
                    this.decibelPlotsComp, this.configuration);
        } catch (Exception e) {
            statusHandler.error("Audio recording has failed.", e);
            return;
        }

        BMHDialogNotificationManager.getInstance().post(
                new AudioRecordPlaybackNotification());

        this.recorderThread.setRecordingListener(listener);
        this.recordingProgBar.setMaximum(this.maxRecordingSeconds);
        this.recordPlayStatus = RecordPlayStatus.RECORD;
        okBtn.setEnabled(false);
        cancelBtn.setEnabled(false);
        stopBtn.setEnabled(true);
        recBtn.setEnabled(false);
        playBtn.setEnabled(false);
        timer = Executors.newSingleThreadScheduledExecutor();
        this.statusLbl.setText("Recording ...");
        this.recorderThread.start();
        timer.scheduleAtFixedRate(new ElapsedTimerTask(), 1000, updateRate,
                TimeUnit.MILLISECONDS);
    }

    /**
     * Stop action.
     */
    protected void stopAction() {
        if (this.recordPlayStatus == RecordPlayStatus.RECORD) {
            this.recorderThread.halt();
            try {
                this.recorderThread.join();
            } catch (InterruptedException e) {
                // Ignore.
            }
            try {
                this.recordedAudio = this.recorderThread.getAudioSamples();
            } catch (Exception e) {
                statusHandler.error("Failed to retrieve recorded audio.", e);
                this.recordedAudio = null;
            }
        } else if (this.recordPlayStatus == RecordPlayStatus.PLAY) {
            this.playbackThread.halt();
            try {
                this.playbackThread.join();
            } catch (InterruptedException e) {
                // Ignore.
            }
        }

        BMHDialogNotificationManager.getInstance().post(
                new AudioPlaybackCompleteNotification());

        shutdownTimer();
        recBtn.setEnabled(true);
        playBtn.setEnabled(true);
        stopBtn.setEnabled(false);
        if (this.recordedAudio != null) {
            this.okBtn.setEnabled(true);
        }
        this.cancelBtn.setEnabled(true);
        this.recordPlayStatus = RecordPlayStatus.STOP;
        this.statusLbl.setText(DEFAULT_STATUS_TEXT);
        this.okToClose = true;
    }

    /**
     * Play action.
     */
    protected void playAction() {
        this.okToClose = false;
        try {
            this.playbackThread = new AudioPlaybackThread(this.recordedAudio);
            this.playbackThread.setCompleteListener(this);
        } catch (AudioException e) {
            statusHandler.error("Audio playback has failed.", e);
            return;
        }

        BMHDialogNotificationManager.getInstance().post(
                new AudioRecordPlaybackNotification());

        this.recordingProgBar.setMaximum(this.playbackThread
                .getAudioLengthInSeconds());
        this.recordPlayStatus = RecordPlayStatus.PLAY;
        okBtn.setEnabled(false);
        cancelBtn.setEnabled(false);
        this.playbackThread.start();
        recBtn.setEnabled(false);
        stopBtn.setEnabled(true);
        resetRecordPlayValues();
        this.statusLbl.setText("Playing ...");
        timer = Executors.newSingleThreadScheduledExecutor();
        timer.scheduleAtFixedRate(new ElapsedTimerTask(), 1000, updateRate,
                TimeUnit.MILLISECONDS);
    }

    protected void cancelAction() {
        setReturnValue(null);
        close();
    }

    protected void okAction() {
        setReturnValue(recordedAudio);
        close();
    }

    /**
     * Reset the recording values to the initial values.
     */
    private void resetRecordPlayValues() {
        elapsedMinutes = 0;
        elapsedSeconds = 0;
        totalElapsedSeconds = 0;
        elapsedTimeLbl.setText(String.format(minutesSecondsFormatStr,
                elapsedMinutes, elapsedSeconds));
        recordingProgBar.setSelection(0);
    }

    /**
     * Shutdown the elapse time timer.
     */
    private void shutdownTimer() {
        if (timer != null && !timer.isShutdown()) {
            timer.shutdown();
        }
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
                    updateRecordingTime();
                }
            });
        }
    }

    @Override
    public void notifyPlaybackComplete() {
        this.shutdownTimer();

        BMHDialogNotificationManager.getInstance().post(
                new AudioPlaybackCompleteNotification());
        // Not on the UI Thread.
        getDisplay().asyncExec(new Runnable() {
            @Override
            public void run() {
                recBtn.setEnabled(true);
                playBtn.setEnabled(true);
                stopBtn.setEnabled(false);

                okBtn.setEnabled(true);
                cancelBtn.setEnabled(true);
                okToClose = true;
                statusLbl.setText(DEFAULT_STATUS_TEXT);
            }
        });
        this.recordPlayStatus = RecordPlayStatus.STOP;
    }
}