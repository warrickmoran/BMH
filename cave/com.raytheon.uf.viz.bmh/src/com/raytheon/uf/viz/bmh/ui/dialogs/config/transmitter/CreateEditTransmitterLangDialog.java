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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.common.bmh.datamodel.language.Dictionary;
import com.raytheon.uf.common.bmh.datamodel.language.Language;
import com.raytheon.uf.common.bmh.datamodel.language.TtsVoice;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterLanguage;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterLanguagePK;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.bmh.ui.common.utility.DialogUtility;
import com.raytheon.uf.viz.bmh.ui.dialogs.voice.SelectDictionaryDlg;
import com.raytheon.uf.viz.bmh.voice.VoiceDataManager;
import com.raytheon.viz.ui.dialogs.CaveSWTDialog;
import com.raytheon.viz.ui.dialogs.ICloseCallback;

/**
 * Dialog used to create or update a {@link TransmitterLanguage}.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 13, 2015 3809       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class CreateEditTransmitterLangDialog extends CaveSWTDialog {

    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(CreateEditTransmitterLangDialog.class);

    private final VoiceDataManager vdm = new VoiceDataManager();

    private static final String CREATE_TITLE = "Create Transmitter Language";

    private static final String EDIT_TITLE = "Edit Transmitter Language";

    private static final String SELECT_LANGUAGE = "Select a Language";

    private static final String SELECT_VOICE = "Select a Voice";

    private final TransmitterGroup transmitterGroup;

    private TransmitterLanguage transmitterLanguage;

    private final List<Language> unassignedLanguages;

    private Map<String, Integer> voiceNameIdentifierMap = new HashMap<>();

    /*
     * Allows the user to select an available language when creating a
     * transmitter language.
     */
    private Combo languageCombo;

    /*
     * the language that is currently selected for the transmitter language.
     * will be NULL is a language has not been selected yet.
     */
    private Language selectedLanguage;

    /*
     * Allows the user to select an available voice. The voices that are
     * displayed in this combo will be based on the selected language. If a
     * language has not been selected yet, no voices will be displayed.
     */
    private Combo voiceCombo;

    /*
     * Used for dictionary management.
     */
    private Label selectedDictionaryLabel;

    private Button changeBtn;

    /*
     * the dictionary that is currently selected for the transmitter language.
     * will be NULL if a dictionary has not been selected yet.
     */
    private Dictionary selectedDictionary;

    private StyledText stationIdTxt;

    private StyledText timePreambleTxt;

    private StyledText timePostambleTxt;

    public CreateEditTransmitterLangDialog(Shell parentShell,
            List<Language> unassignedLanguages,
            TransmitterGroup transmitterGroup) {
        super(parentShell, SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL,
                CAVE.PERSPECTIVE_INDEPENDENT);
        this.unassignedLanguages = unassignedLanguages;
        this.transmitterGroup = transmitterGroup;
        this.setText(CREATE_TITLE);
    }

    public CreateEditTransmitterLangDialog(Shell parentShell,
            TransmitterLanguage transmitterLanguage,
            TransmitterGroup transmitterGroup) {
        super(parentShell, SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL,
                CAVE.PERSPECTIVE_INDEPENDENT);
        this.transmitterLanguage = transmitterLanguage;
        this.unassignedLanguages = Collections.emptyList();
        this.transmitterGroup = transmitterGroup;
        this.setText(EDIT_TITLE);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.viz.ui.dialogs.CaveSWTDialogBase#constructShellLayout()
     */
    @Override
    protected Layout constructShellLayout() {
        return new GridLayout(1, false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.viz.ui.dialogs.CaveSWTDialogBase#constructShellLayoutData()
     */
    @Override
    protected Object constructShellLayoutData() {
        return new GridData(SWT.FILL, SWT.FILL, true, true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.viz.ui.dialogs.CaveSWTDialogBase#initializeComponents(org
     * .eclipse.swt.widgets.Shell)
     */
    @Override
    protected void initializeComponents(Shell shell) {
        this.createAttributeFields(shell);
        this.populateDialog();

        DialogUtility.addSeparator(shell, SWT.HORIZONTAL);

        this.createBottomButtons(shell);
    }

    private void createAttributeFields(final Shell shell) {
        /* Create the frame. */
        GridLayout gl = new GridLayout(1, false);
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, true);
        Group attributesGroup = new Group(shell, SWT.BORDER);
        attributesGroup.setText(" Attributes ");
        attributesGroup.setLayout(gl);
        attributesGroup.setLayoutData(gd);

        /* The composite for the fields. */
        gl = new GridLayout(3, false);
        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        final Composite attributesComp = new Composite(attributesGroup,
                SWT.NONE);
        attributesComp.setLayout(gl);
        attributesComp.setLayoutData(gd);

        /*
         * language field: read-only when this is an existing transmitter
         * language
         */
        gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
        Label languageLabel = new Label(attributesComp, SWT.NONE);
        languageLabel.setText("Language:");
        languageLabel.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
        gd.horizontalSpan = 2;
        gd.verticalIndent = 5;
        gd.widthHint = 180;
        if (this.transmitterLanguage == null) {
            /*
             * This is a new transmitter language. Allow the user to select a
             * language.
             */
            this.languageCombo = new Combo(attributesComp, SWT.BORDER
                    | SWT.READ_ONLY);
            this.languageCombo.setLayoutData(gd);

            this.languageCombo.add(SELECT_LANGUAGE);
            this.languageCombo.select(0);

            for (Language language : this.unassignedLanguages) {
                this.languageCombo.add(language.toString());
            }
            this.languageCombo.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    handleLanguageSelection();
                }
            });
        } else {
            /*
             * This is an existing transmitter language. Just display the
             * associated language.
             */
            Label displayLanguageLabel = new Label(attributesComp, SWT.NONE);
            displayLanguageLabel.setText(this.transmitterLanguage.getLanguage()
                    .toString());
            displayLanguageLabel.setLayoutData(gd);
        }

        /*
         * voice field: limited to the voices associated with the selected
         * language.
         */
        gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
        Label voiceLabel = new Label(attributesComp, SWT.NONE);
        voiceLabel.setText("Voice:");
        voiceLabel.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
        gd.horizontalSpan = 2;
        gd.verticalIndent = 5;
        this.voiceCombo = new Combo(attributesComp, SWT.BORDER | SWT.READ_ONLY);
        this.voiceCombo.setLayoutData(gd);
        this.voiceCombo.setEnabled(false);

        /*
         * Dictionary field. No limits.
         */
        gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
        Label dictionaryLabel = new Label(attributesComp, SWT.NONE);
        dictionaryLabel.setText("Dictionary:");
        dictionaryLabel.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
        gd.verticalIndent = 5;
        gd.widthHint = 200;
        selectedDictionaryLabel = new Label(attributesComp, SWT.BORDER);
        selectedDictionaryLabel.setLayoutData(gd);

        /*
         * Dictionary selection button. Only enabled when a language has been
         * selected.
         */
        gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
        changeBtn = new Button(attributesComp, SWT.PUSH);
        changeBtn.setText("Change...");
        changeBtn.setLayoutData(gd);
        changeBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleChangeAction();
            }
        });
        changeBtn.setEnabled(false);

        /*
         * Station ID Message - standard editable field.
         */
        gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
        Label stationMsgLabel = new Label(attributesComp, SWT.NONE);
        stationMsgLabel.setText("Station Id:");
        stationMsgLabel.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
        gd.verticalIndent = 5;
        gd.widthHint = 200;
        gd.heightHint = 65;
        gd.horizontalSpan = 2;
        this.stationIdTxt = new StyledText(attributesComp, SWT.BORDER
                | SWT.MULTI | SWT.V_SCROLL);
        this.stationIdTxt.setLayoutData(gd);
        this.stationIdTxt.setWordWrap(true);

        /*
         * Time Preamble - standard editable field.
         */
        gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
        Label timePreambleLabel = new Label(attributesComp, SWT.NONE);
        timePreambleLabel.setText("Time Preamble:");
        timePreambleLabel.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
        gd.verticalIndent = 5;
        gd.widthHint = 200;
        gd.heightHint = 65;
        gd.horizontalSpan = 2;
        this.timePreambleTxt = new StyledText(attributesComp, SWT.BORDER
                | SWT.MULTI | SWT.V_SCROLL);
        this.timePreambleTxt.setLayoutData(gd);
        this.timePreambleTxt.setWordWrap(true);

        /*
         * Time Postamble - standard editable field.
         */
        gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
        Label timePostambleLabel = new Label(attributesComp, SWT.NONE);
        timePostambleLabel.setText("Time Postamble:");
        timePostambleLabel.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
        gd.verticalIndent = 5;
        gd.widthHint = 200;
        gd.heightHint = 65;
        gd.horizontalSpan = 2;
        this.timePostambleTxt = new StyledText(attributesComp, SWT.BORDER
                | SWT.MULTI | SWT.V_SCROLL);
        this.timePostambleTxt.setLayoutData(gd);
        this.timePostambleTxt.setWordWrap(true);
    }

    private void populateDialog() {
        if (this.transmitterLanguage == null) {
            return;
        }

        this.selectedLanguage = this.transmitterLanguage.getLanguage();
        this.populateVoices();
        this.voiceCombo.setText(this.transmitterLanguage.getVoice()
                .getVoiceName());

        this.selectedDictionary = this.transmitterLanguage.getDictionary();
        if (this.selectedDictionary != null) {
            this.selectedDictionaryLabel.setText(this.selectedDictionary
                    .getName());
        }
        this.changeBtn.setEnabled(true);

        if (this.transmitterLanguage.getStationIdMsg() != null) {
            this.stationIdTxt.setText(this.transmitterLanguage
                    .getStationIdMsg());
        }

        if (this.transmitterLanguage.getTimeMsgPreamble() != null) {
            this.timePreambleTxt.setText(this.transmitterLanguage
                    .getTimeMsgPreamble());
        }

        if (this.transmitterLanguage.getTimeMsgPostamble() != null) {
            this.timePostambleTxt.setText(this.transmitterLanguage
                    .getTimeMsgPostamble());
        }
    }

    private void handleLanguageSelection() {
        final String languageText = this.languageCombo.getText();
        if (SELECT_LANGUAGE.equals(languageText)) {
            selectedLanguage = null;
            /*
             * clear the voice and dictionary selections. wait for the user to
             * select a language.
             */
            this.voiceCombo.setItems(new String[] {});
            this.voiceCombo.setEnabled(false);
            this.voiceNameIdentifierMap.clear();

            selectedDictionary = null;
            this.selectedDictionaryLabel.setText("");
            this.changeBtn.setEnabled(false);
            return;
        }

        /*
         * a language has been selected.
         */
        selectedLanguage = null;
        for (Language language : Language.values()) {
            if (language.toString().equals(languageText)) {
                selectedLanguage = language;
                break;
            }
        }

        /*
         * Retrieve applicable voices and populate the voice combo.
         */
        this.populateVoices();

        /*
         * Enable dictionary selection.
         */
        this.changeBtn.setEnabled(true);
    }

    private void populateVoices() {
        this.voiceCombo.add(SELECT_VOICE);
        List<TtsVoice> voices = null;
        try {
            voices = this.vdm.getIdentfiersForLanguage(selectedLanguage);
        } catch (Exception e) {
            statusHandler.error("Failed to retrieve the voices for language: "
                    + selectedLanguage.toString() + ".", e);
            return;
        }
        for (TtsVoice voice : voices) {
            this.voiceCombo.add(voice.getVoiceName());
            this.voiceNameIdentifierMap.put(voice.getVoiceName(),
                    voice.getVoiceNumber());
        }
        this.voiceCombo.select(0);
        this.voiceCombo.setEnabled(true);
    }

    private void handleChangeAction() {
        SelectDictionaryDlg selectDictDlg = new SelectDictionaryDlg(this.shell,
                this.selectedLanguage);
        selectDictDlg.setFilterDictionary(this.selectedDictionary);
        selectDictDlg.setCloseCallback(new ICloseCallback() {
            @Override
            public void dialogClosed(Object returnValue) {
                if (returnValue == null
                        || returnValue instanceof Dictionary == false) {
                    return;
                }

                selectedDictionary = (Dictionary) returnValue;
                selectedDictionaryLabel.setText(selectedDictionary.getName());
            }
        });
        selectDictDlg.open();
    }

    private void createBottomButtons(final Shell shell) {
        GridLayout gl = new GridLayout(2, false);
        GridData gd = new GridData(SWT.CENTER, SWT.DEFAULT, false, false);
        Composite comp = new Composite(getShell(), SWT.NONE);
        comp.setLayout(gl);
        comp.setLayoutData(gd);

        gd = new GridData(75, SWT.DEFAULT);
        Button saveUpdateBtn = new Button(comp, SWT.PUSH);
        saveUpdateBtn.setText("Save");
        saveUpdateBtn.setLayoutData(gd);
        saveUpdateBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleSaveAction();
            }
        });

        gd = new GridData(75, SWT.DEFAULT);
        Button closeBtn = new Button(comp, SWT.PUSH);
        closeBtn.setText("Close");
        closeBtn.setLayoutData(gd);
        closeBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setReturnValue(null);
                close();
            }
        });
    }

    private boolean validate() {
        if (this.transmitterLanguage == null) {
            /*
             * verify that a language has been specified.
             */
            if (SELECT_LANGUAGE.equals(this.languageCombo.getText())) {
                DialogUtility
                        .showMessageBox(this.shell, SWT.ICON_ERROR | SWT.OK,
                                "Transmitter Language - Language",
                                "Language is a required field. Please select a Language.");
                return false;
            }
        }

        /*
         * verify that a voice has been selected.
         */
        if (SELECT_VOICE.equals(this.voiceCombo.getText())) {
            DialogUtility.showMessageBox(this.shell, SWT.ICON_ERROR | SWT.OK,
                    "Transmitter Language - Voice",
                    "Voice is a required field. Please select a Voice.");
            return false;
        }

        return true;
    }

    private void handleSaveAction() {
        if (this.validate() == false) {
            return;
        }

        /*
         * Determine if we are creating a new transmitter language or updating
         * an existing transmitter language.
         */
        if (this.transmitterLanguage == null) {
            /*
             * new transmitter language. set the fields that would never change
             * on an existing transmitter language.
             */
            this.transmitterLanguage = new TransmitterLanguage();
            TransmitterLanguagePK pk = new TransmitterLanguagePK();
            pk.setTransmitterGroup(this.transmitterGroup);
            pk.setLanguage(this.selectedLanguage);
            this.transmitterLanguage.setId(pk);
        }

        /*
         * check voice.
         */
        final int voiceId = this.voiceNameIdentifierMap.get(this.voiceCombo
                .getText());
        if (this.transmitterLanguage.getVoice() == null
                || this.transmitterLanguage.getVoice().getVoiceNumber() != voiceId) {
            /*
             * retrieve the voice associated with the selected id and update the
             * transmitter language.
             */
            try {
                TtsVoice voice = this.vdm.getVoiceById(voiceId);
                this.transmitterLanguage.setVoice(voice);
            } catch (Exception e) {
                statusHandler.error(
                        "Failed to retrieve the voice associated with id: "
                                + voiceId + ".", e);
                return;
            }
        }

        /*
         * dictionary
         */
        this.transmitterLanguage.setDictionary(this.selectedDictionary);

        /*
         * message fields.
         */
        this.transmitterLanguage.setStationIdMsg(this.stationIdTxt.getText()
                .trim());
        this.transmitterLanguage.setTimeMsgPreamble(this.timePreambleTxt
                .getText().trim());
        this.transmitterLanguage.setTimeMsgPostamble(this.timePostambleTxt
                .getText().trim());
        setReturnValue(this.transmitterLanguage);
        close();
    }
}