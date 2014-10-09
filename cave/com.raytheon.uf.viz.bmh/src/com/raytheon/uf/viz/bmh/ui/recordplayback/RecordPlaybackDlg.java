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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
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
 * Oct 8, 2014   #3657     bkowal       Implemented audio recording and playback.
 * 
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

    /** Status label. */
    /*
     * TODO: Still need to manage that status label display across the different
     * states. The status text that is displayed will probably be dependent on
     * whether this dialog was launched from the Weather Messages dialog or the
     * Emergency Override dialog (ex: Recording Message will be 'On the Air'
     * when started from the Emergency Override dialog).
     */
    private Label statusLbl;

    /** Status label font. */
    private Font statusLabelFont;

    /** Elapsed time font. */
    private Font elapsedTimeFont;

    /** Recording progress bar. */
    private ProgressBar recordingProgBar;

    /** Update rate in milliseconds. */
    private long updateRate = 1000;

    /** Label showing the time elapsed during recording. */
    private Label elapsedTimeLbl;

    /** Minutes elapsed during recording/playback. */
    private int elapsedMinutes = 0;

    /** Seconds elapsed during recording/playback. */
    private int elapsedSeconds = 0;

    /** Total seconds elapsed during recording/playback */
    private int totalElapsedSeconds = 0;

    /** Maximum number of second allowed for recording. */
    private int maxRecordingSeconds = 0;

    /** Minute/Second format string. */
    private String formatStr = "%02d:%02d";

    /** Recording/playback timer. */
    private ScheduledExecutorService timer;

    /** Record button. */
    private Button recBtn;

    /** Stop button. */
    private Button stopBtn;

    /** Play button. */
    private Button playBtn;

    private enum RecordPlayStatus {
        RECORD, PLAY, STOP
    };

    /*
     * This will not matter for the initial implementation which will just
     * support recording and playback. However, we will want to optimize the
     * number of samples read between audio live streaming segments for the live
     * broadcast.
     */
    private static final int SAMPLE_COUNT = 2;

    private volatile RecordPlayStatus recordPlayStatus = RecordPlayStatus.STOP;

    private AudioRecorderThread recorderThread;

    private AudioPlaybackThread playbackThread;

    private ByteBuffer recordedAudio;

    /**
     * Constructor.
     * 
     * @param parentShell
     *            Parent shell.
     * @param maxRecordingSeconds
     *            Maximum seconds to record.
     */
    public RecordPlaybackDlg(Shell parentShell, int maxRecordingSeconds) {
        super(parentShell, SWT.DIALOG_TRIM | SWT.MIN | SWT.PRIMARY_MODAL,
                CAVE.DO_NOT_BLOCK | CAVE.MODE_INDEPENDENT);

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
        }
        if (this.recorderThread != null) {
            this.recorderThread.halt();
        }
    }

    @Override
    protected void initializeComponents(Shell shell) {
        setText("Message Record/Playback");

        init();
    }

    /**
     * Initialize method.
     */
    private void init() {
        createStatusLabel();
        DialogUtility.addSeparator(shell, SWT.HORIZONTAL);
        createRecordingControls();
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

        statusLbl.setText("Press REC Button to Start Recording...");
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
        elapsedTimeLbl.setText(String.format(formatStr, 0, 0));
        elapsedTimeLbl.setLayoutData(gd);
        elapsedTimeLbl.setFont(elapsedTimeFont);

        /*
         * Label and progress bar.
         */
        Label elapsedLbl = new Label(recordingComp, SWT.NONE);
        elapsedLbl.setText("Elapsed Time: ");

        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gd.minimumWidth = 350;
        recordingProgBar = new ProgressBar(recordingComp, SWT.HORIZONTAL
                | SWT.SMOOTH);
        recordingProgBar.setLayoutData(gd);
        recordingProgBar.setMinimum(0);
        recordingProgBar.setMaximum(maxRecordingSeconds);

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
        Button okBtn = new Button(buttonComp, SWT.PUSH);
        okBtn.setText("OK");
        okBtn.setLayoutData(gd);
        okBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                close();
            }
        });

        gd = new GridData(SWT.LEFT, SWT.DEFAULT, true, false);
        gd.widthHint = buttonWidth;
        Button cancelBtn = new Button(buttonComp, SWT.PUSH);
        cancelBtn.setText("Cancel");
        cancelBtn.setLayoutData(gd);
        cancelBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                close();
            }
        });
    }

    /**
     * Update the recording time label and the progress bar.
     */
    private void updateRecordingTime() {
        calculateMinutesSeconds();
        elapsedTimeLbl.setText(String.format(formatStr, elapsedMinutes,
                elapsedSeconds));

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
    private void calculateMinutesSeconds() {
        ++totalElapsedSeconds;
        ++elapsedSeconds;
        if (elapsedSeconds > 59) {
            elapsedSeconds = 0;
            ++elapsedMinutes;
        }
    }

    /**
     * Record action.
     */
    private void recordAction() {
        resetRecordPlayValues();
        try {
            this.recorderThread = new AudioRecorderThread(SAMPLE_COUNT);
        } catch (AudioException e) {
            statusHandler.error("Audio recording has failed.", e);
            return;
        }
        this.recordingProgBar.setMaximum(this.maxRecordingSeconds);
        this.recordPlayStatus = RecordPlayStatus.RECORD;
        stopBtn.setEnabled(true);
        recBtn.setEnabled(false);
        playBtn.setEnabled(false);
        timer = Executors.newSingleThreadScheduledExecutor();
        this.recorderThread.start();
        timer.scheduleAtFixedRate(new ElapsedTimerTask(), 1000, updateRate,
                TimeUnit.MILLISECONDS);
    }

    /**
     * Stop action.
     */
    private void stopAction() {
        if (this.recordPlayStatus == RecordPlayStatus.RECORD) {
            this.recorderThread.halt();
            try {
                this.recorderThread.join();
            } catch (InterruptedException e) {
                // Ignore.
            }
            this.recordedAudio = this.recorderThread.getAudioSamples();
        } else if (this.recordPlayStatus == RecordPlayStatus.PLAY) {
            this.playbackThread.halt();
            try {
                this.playbackThread.join();
            } catch (InterruptedException e) {
                // Ignore.
            }
        }
        shutdownTimer();
        recBtn.setEnabled(true);
        playBtn.setEnabled(true);
        stopBtn.setEnabled(false);
        this.recordPlayStatus = RecordPlayStatus.STOP;
    }

    /**
     * Play action.
     */
    private void playAction() {
        try {
            this.playbackThread = new AudioPlaybackThread(this.recordedAudio);
            this.playbackThread.setCompleteListener(this);
        } catch (AudioException e) {
            statusHandler.error("Audio playback has failed.", e);
            return;
        }
        this.recordingProgBar.setMaximum(this.playbackThread
                .getAudioLengthInSeconds());
        this.recordPlayStatus = RecordPlayStatus.PLAY;
        this.playbackThread.start();
        recBtn.setEnabled(false);
        stopBtn.setEnabled(true);
        resetRecordPlayValues();
        timer = Executors.newSingleThreadScheduledExecutor();
        timer.scheduleAtFixedRate(new ElapsedTimerTask(), 1000, updateRate,
                TimeUnit.MILLISECONDS);
    }

    /**
     * Reset the recording values to the initial values.
     */
    private void resetRecordPlayValues() {
        elapsedMinutes = 0;
        elapsedSeconds = 0;
        totalElapsedSeconds = 0;
        elapsedTimeLbl.setText(String.format(formatStr, elapsedMinutes,
                elapsedSeconds));
        recordingProgBar.setSelection(0);
        statusLbl.setText("Press REC Button to Start Recording...");
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
        // Not on the UI Thread.
        getDisplay().asyncExec(new Runnable() {
            @Override
            public void run() {
                recBtn.setEnabled(true);
                playBtn.setEnabled(true);
                stopBtn.setEnabled(false);
            }
        });
        this.recordPlayStatus = RecordPlayStatus.STOP;
    }
}