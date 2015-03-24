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
package com.raytheon.uf.viz.bmh.ui.dialogs.config.transmitter;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;

import com.raytheon.uf.common.bmh.datamodel.language.Language;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterLanguage;
import com.raytheon.uf.common.bmh.schemas.ssml.Prosody;
import com.raytheon.uf.common.bmh.schemas.ssml.SSMLConversionException;
import com.raytheon.uf.common.bmh.schemas.ssml.SSMLDocument;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.bmh.data.BmhUtils;
import com.raytheon.uf.viz.bmh.dialogs.notify.BMHDialogNotificationManager;
import com.raytheon.uf.viz.bmh.ui.common.utility.DialogUtility;
import com.raytheon.uf.viz.bmh.ui.recordplayback.AudioPlaybackCompleteNotification;
import com.raytheon.uf.viz.bmh.ui.recordplayback.AudioPlaybackThread;
import com.raytheon.uf.viz.bmh.ui.recordplayback.AudioRecordPlaybackNotification;
import com.raytheon.uf.viz.bmh.ui.recordplayback.IPlaybackCompleteListener;
import com.raytheon.uf.viz.core.VizApp;

/**
 * Allows the user to adjust and sample the rate of speech for a
 * {@link TransmitterLanguage}.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 18, 2015 4142       bkowal      Initial creation
 * Feb 19, 2015 4142       bkowal      Use {@link #sampleVoice} when synthesizing
 *                                     the rate of speech sample.
 * Feb 24, 2015 4157       bkowal      Added Spanish sample text.
 * Mar 24, 2015 4303       rferrel     When playing audio suspend others such as in line monitor.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class RateOfSpeechComp {

    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(RateOfSpeechComp.class);

    private final Map<Language, String> sampleTextLanguageMap = new HashMap<>(
            Language.values().length, 1.0f);

    private final String SAMPLE_TEXT_ENGLISH = "This is a test of the rate of speech option.";

    /**
     * Translated the {@link #SAMPLE_TEXT_ENGLISH} using
     * http://www.bing.com/translator/.
     */
    private final String SAMPLE_TEXT_SPANISH = "Esta es una prueba del índice de la opción de discurso.";

    private final int SCALED_RATE_MIN = 1;

    private final int SCALED_RATE_MAX = 199;

    private final int SCALED_ZERO = 100;

    private final int DEFAULT_SCALE_INCREMENT = 1;

    private final String RATE_POSITIVE_DISPLAY_PREFIX = "+";

    private final String RATE_DISPLAY_SUFFIX = "%";

    private Scale rateOfSpeechScale;

    private Label rateLabel;

    private Button playButton;

    private int sampleVoice = -1;

    private Language sampleLanguage;

    private final Composite parent;

    private final Object audioLock = new Object();

    private AudioPlaybackThread audioThread = null;

    /**
     * Constructor
     * 
     * @param parent
     *            the parent {@link Composite} the components will be placed in.
     * @param horizontalSpan
     *            the number of column(s) this component should occupy when
     *            rendered.
     */
    public RateOfSpeechComp(Composite parent, final int horizontalSpan) {
        this.parent = parent;
        GridLayout gl = new GridLayout(3, false);
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gd.horizontalSpan = horizontalSpan;
        Composite controlComp = new Composite(parent, SWT.NONE);
        controlComp.setLayout(gl);
        controlComp.setLayoutData(gd);

        /* Rate of Speech Slider */
        gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
        gd.widthHint = 210;
        this.rateOfSpeechScale = new Scale(controlComp, SWT.NONE);
        this.rateOfSpeechScale.setMinimum(SCALED_RATE_MIN);
        this.rateOfSpeechScale.setMaximum(SCALED_RATE_MAX);
        this.rateOfSpeechScale.setIncrement(DEFAULT_SCALE_INCREMENT);
        this.rateOfSpeechScale.setPageIncrement(DEFAULT_SCALE_INCREMENT);
        this.rateOfSpeechScale.setSelection(SCALED_ZERO);
        this.rateOfSpeechScale.setLayoutData(gd);
        this.rateOfSpeechScale.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleRateScaleChange();
            }
        });

        gd = new GridData(SWT.CENTER, SWT.CENTER, false, false);
        gd.widthHint = 40;
        this.rateLabel = new Label(controlComp, SWT.CENTER);
        this.rateLabel.setLayoutData(gd);
        this.handleRateScaleChange();

        gd = new GridData(SWT.DEFAULT, SWT.CENTER, true, false);
        this.playButton = new Button(controlComp, SWT.PUSH);
        this.playButton.setText("Play");
        this.playButton.setLayoutData(gd);
        this.playButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handlePlayAction();
            }
        });

        if (this.sampleVoice == -1) {
            this.rateOfSpeechScale.setEnabled(false);
            this.playButton.setEnabled(false);
        }

        /*
         * Populate the sample language map.
         */
        this.sampleTextLanguageMap.put(Language.ENGLISH, SAMPLE_TEXT_ENGLISH);
        this.sampleTextLanguageMap.put(Language.SPANISH, SAMPLE_TEXT_SPANISH);
        this.parent.addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent e) {
                synchronized (audioLock) {
                    if (audioThread != null) {
                        audioThread.halt();
                        finishAudio();
                    }
                }
            }
        });
    }

    /**
     * Sets the voice that will be used for the sample audio playback
     * 
     * @param voiceNumber
     *            the identifier to the voice to use for audio playback
     */
    public void setSampleVoice(int voiceNumber, Language language) {
        this.sampleVoice = voiceNumber;
        this.sampleLanguage = language;

        boolean enabled = (this.sampleVoice != -1);
        this.rateOfSpeechScale.setEnabled(enabled);
        this.playButton.setEnabled(enabled);
    }

    /**
     * Used to set the initial rate of Speech displayed on the dialog based on
     * the selected Transmitter Language.
     * 
     * @param rateOfSpeech
     *            the initial rate of speech to set.
     */
    public void setInitialRateOfSpeech(final int rateOfSpeech) {
        this.rateOfSpeechScale.setSelection(rateOfSpeech + SCALED_ZERO);
        this.handleRateScaleChange();
    }

    /**
     * Returns the currently selected rate of speech as an integer without any
     * extra formatting.
     * 
     * @return the currently selected rate of speech without any extra
     *         formatting.
     */
    public int getSelectedRateOfSpeech() {
        return this.rateOfSpeechScale.getSelection() - SCALED_ZERO;
    }

    /**
     * Updates the {@link #rateLabel} based on the newly selected rate.
     */
    private void handleRateScaleChange() {
        this.rateLabel.setText(this.getActualRate());
    }

    /**
     * Determines the selected rate. Returns a formatted and non-scaled version
     * of the currently selected rate.
     * 
     * @return a formatted and non-scaled version of the currently selected
     *         rate.
     */
    private String getActualRate() {
        int newScaledRate = this.rateOfSpeechScale.getSelection() - SCALED_ZERO;
        StringBuilder rateDisplaySB = new StringBuilder();
        if (newScaledRate >= 0) {
            rateDisplaySB.append(RATE_POSITIVE_DISPLAY_PREFIX);
        }
        rateDisplaySB.append(newScaledRate).append(RATE_DISPLAY_SUFFIX);

        return rateDisplaySB.toString();
    }

    /**
     * Plays sample audio based on the selected rate of speech.
     */
    private void handlePlayAction() {
        SSMLDocument ssmlDoc = new SSMLDocument(this.sampleLanguage);

        /*
         * Construct the prosody tag.
         */
        Prosody prosody = ssmlDoc.getFactory().createProsody();
        prosody.setRate(this.getActualRate());

        /*
         * Add the text. The text will be the sample text. We could optionally
         * use some other field on the form; however, we may risk locking a TTS
         * thread and the dialog for an extended period of time if one of the
         * form messages is significantly longer than the sample text. Add the
         * text to the prosody.
         */
        prosody.getContent().add(
                this.sampleTextLanguageMap.get(this.sampleLanguage));

        ssmlDoc.getRootTag().getContent().add(prosody);
        final String ssml;
        try {
            ssml = ssmlDoc.toSSML();
        } catch (SSMLConversionException e) {
            statusHandler
                    .error("Failed to generate the sample SSML for audio playback.",
                            e);
            return;
        }

        synchronized (audioLock) {
            try {
                byte[] audio = BmhUtils.textToAudio(ssml, this.sampleVoice);
                audioThread = new AudioPlaybackThread(audio);
            } catch (Exception e) {
                String msg = "Unable to play audio.\n\n"
                        + BmhUtils.getRootCauseMessage(e);

                DialogUtility.showMessageBox(parent.getShell(), SWT.ICON_ERROR
                        | SWT.OK, "Audio Play", msg);

                if (audioThread != null) {
                    audioThread.dispose();
                    audioThread = null;
                }
                return;
            }
            BMHDialogNotificationManager.getInstance().post(
                    new AudioRecordPlaybackNotification());
        }

        audioThread.setCompleteListener(new IPlaybackCompleteListener() {

            @Override
            public void notifyPlaybackComplete() {
                finishAudio();
            }
        });
        playButton.setEnabled(false);
        audioThread.start();
    }

    /**
     * Clean up when done playing the audio.
     */
    private void finishAudio() {
        synchronized (audioLock) {
            if (audioThread != null) {
                BMHDialogNotificationManager.getInstance().post(
                        new AudioPlaybackCompleteNotification());
                audioThread = null;
                VizApp.runAsync(new Runnable() {

                    @Override
                    public void run() {
                        if (!playButton.isDisposed()) {
                            playButton.setEnabled(true);
                        }
                    }
                });
            }
        }
    }

}