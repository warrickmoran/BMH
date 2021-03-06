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
package com.raytheon.uf.viz.bmh.ui.dialogs.voice;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.common.bmh.BMHVoice;
import com.raytheon.uf.common.bmh.datamodel.language.Dictionary;
import com.raytheon.uf.common.bmh.datamodel.language.TtsVoice;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.bmh.ui.common.utility.DialogUtility;
import com.raytheon.uf.viz.bmh.ui.dialogs.AbstractBMHDialog;
import com.raytheon.uf.viz.bmh.ui.dialogs.DlgInfo;
import com.raytheon.uf.viz.bmh.voice.VoiceDataManager;
import com.raytheon.viz.ui.dialogs.ICloseCallback;

/**
 * TTS Voice Configuration dialog. This dialog allows users to see the voices
 * and assign a dictionary to voices that are available in the system.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 13, 2014 3618       bkowal      Initial creation
 * Dec 16, 2014 3618       bkowal      Implemented
 * Mar 03, 2015 4175       bkowal      Implemented voice registration.
 * Jun 05, 2015 4490       rjpeter     Updated constructor.
 * Jul 22, 2015 4676       bkowal      Toggle adding/removing dictionaries.
 * Aug 04, 2015 4424       bkowal      Use {@link DictionaryAssignmentComp}.
 * Dec 08, 2015 5159       bkowal      Keep track of a {@link Dictionary} when it is
 *                                     removed from a voice.
 * Mar 30, 2016 5504       bkowal      Fix GUI sizing issues.
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class VoiceConfigDialog extends AbstractBMHDialog implements
        IDictionarySelectionListener {
    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(VoiceConfigDialog.class);

    private final String GENDER_MALE = "MALE";

    private final String GENDER_FEMALE = "FEMALE";

    private final Map<String, Integer> voiceIdentifierMap = new HashMap<>();

    private Map<Integer, BMHVoice> unregisteredVoices;

    /**
     * Used to retrieve and store {@link TtsVoice} information.
     */
    private final VoiceDataManager vdm = new VoiceDataManager();

    private Label voiceNameLabel;

    private Label voiceLanguageLabel;

    private Label voiceGenderLabel;

    private DictionaryAssignmentComp dictionaryAssign;

    private Button saveBtn;

    private Button addVoiceButton;

    private TtsVoice selectedVoice;

    /**
     * Used to display the names of each {@link TtsVoice} for selection.
     */
    private List voiceList;

    public VoiceConfigDialog(Shell parentShell) {
        super(parentShell, SWT.DIALOG_TRIM, CAVE.PERSPECTIVE_INDEPENDENT
                | CAVE.DO_NOT_BLOCK);
        this.setText(DlgInfo.TTS_VOICE_CONFIGURATION.getTitle());
    }

    /**
     * TODO: Suggest promoting {@link #constructShellLayout()} and
     * {@link #constructShellLayoutData()} to {@link AbstractBMHDialog} because
     * they all have the same implementation across multiple dialogs.
     */

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
     * com.raytheon.uf.viz.bmh.ui.dialogs.AbstractBMHDialog#initializeComponents
     * (org.eclipse.swt.widgets.Shell)
     */
    @Override
    protected void initializeComponents(Shell shell) {
        super.initializeComponents(shell);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        GridLayout gl = new GridLayout(2, false);
        gl.horizontalSpacing = 0;
        gl.verticalSpacing = 0;
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        Composite mainComp = new Composite(shell, SWT.NONE);
        mainComp.setLayout(gl);
        mainComp.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gl = new GridLayout(1, false);
        Composite left = new Composite(mainComp, SWT.NONE);
        left.setLayout(gl);
        left.setLayoutData(gd);

        createVoicesList(left);

        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gl = new GridLayout(1, false);
        Composite right = new Composite(mainComp, SWT.NONE);
        right.setLayout(gl);
        right.setLayoutData(gd);

        createVoiceAttributesGroup(right);

        this.createCloseButton(mainComp);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.viz.ui.dialogs.CaveSWTDialogBase#opened()
     */
    @Override
    protected void opened() {
        this.populateVoiceList();
        /*
         * Handle the initial attribute population.
         */
        this.populateVoiceAttributes();
    }

    /**
     * Constructs the {@link List} that will display the voices that are
     * registered in the system.
     * 
     * @param composite
     *            the component that the list will be created within.
     */
    private void createVoicesList(Composite composite) {
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        GridLayout gl = new GridLayout(1, false);
        /* Create the framing component */
        Group voicesGroup = new Group(composite, SWT.BORDER);
        voicesGroup.setText("Voices");
        voicesGroup.setLayout(gl);
        voicesGroup.setLayoutData(gd);

        /* Create the list. */
        voiceList = new List(voicesGroup, SWT.V_SCROLL | SWT.H_SCROLL
                | SWT.SINGLE | SWT.BORDER);
        GC gc = new GC(voiceList);
        int textWidth = gc.getFontMetrics().getAverageCharWidth() * 20;
        gc.dispose();
        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.widthHint = textWidth;
        voiceList.setLayoutData(gd);
        voiceList.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                populateVoiceAttributes();
            }
        });

        addVoiceButton = new Button(voicesGroup, SWT.PUSH);
        gd = new GridData(SWT.CENTER, SWT.FILL, true, false);
        gd.minimumWidth = addVoiceButton.getDisplay().getDPI().x;
        addVoiceButton.setText("Add Voice ...");
        addVoiceButton.setLayoutData(gd);
        addVoiceButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleAddVoiceAction();
            }
        });
    }

    /**
     * Creates the section of the dialog that will display voice attributes.
     * Attributes include the voice name, the voice language, the voice gender,
     * and the configurable voice dictionary.
     * 
     * @param composite
     *            the component that the attributes display will be created
     *            within.
     */
    private void createVoiceAttributesGroup(Composite composite) {
        GridLayout gl = new GridLayout(1, false);
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        /* Create the framing component */
        Group attributesGroup = new Group(composite, SWT.BORDER);
        attributesGroup.setText("Attributes");
        attributesGroup.setLayout(gl);
        attributesGroup.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gl = new GridLayout(2, false);
        Composite attributesComp = new Composite(attributesGroup, SWT.NONE);
        attributesComp.setLayoutData(gd);
        attributesComp.setLayout(gl);

        /* Voice Name */
        gd = new GridData(SWT.FILL, SWT.DEFAULT, false, false);
        Label nameLabel = new Label(attributesComp, SWT.RIGHT);
        nameLabel.setText("Name:");
        nameLabel.setLayoutData(gd);
        gd = new GridData(SWT.FILL, SWT.DEFAULT, false, false);
        voiceNameLabel = new Label(attributesComp, SWT.NONE);
        voiceNameLabel.setLayoutData(gd);

        /* Voice Language */
        gd = new GridData(SWT.FILL, SWT.DEFAULT, false, false);
        Label languageLabel = new Label(attributesComp, SWT.RIGHT);
        languageLabel.setText("Language:");
        languageLabel.setLayoutData(gd);
        gd = new GridData(SWT.FILL, SWT.DEFAULT, false, false);
        voiceLanguageLabel = new Label(attributesComp, SWT.NONE);
        voiceLanguageLabel.setLayoutData(gd);

        /* Voice Gender */
        gd = new GridData(SWT.FILL, SWT.DEFAULT, false, false);
        Label genderLabel = new Label(attributesComp, SWT.RIGHT);
        genderLabel.setText("Gender:");
        genderLabel.setLayoutData(gd);
        gd = new GridData(SWT.FILL, SWT.DEFAULT, false, false);
        voiceGenderLabel = new Label(attributesComp, SWT.NONE);
        voiceGenderLabel.setLayoutData(gd);

        /* Voice Dictionary */
        this.dictionaryAssign = new DictionaryAssignmentComp(attributesComp,
                this.getShell(), this);
        this.dictionaryAssign.disable();

        /*
         * Save Button - will only be enabled if the dictionary has been
         * changed.
         */
        saveBtn = new Button(attributesGroup, SWT.PUSH);
        gd = new GridData(SWT.CENTER, SWT.DEFAULT, true, false);
        gd.horizontalSpan = 3;
        gd.minimumWidth = saveBtn.getDisplay().getDPI().x;
        saveBtn.setText("Save");
        saveBtn.setEnabled(false);
        saveBtn.setLayoutData(gd);
        saveBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleSaveAction();
            }
        });
    }

    /**
     * Creates the "Close" button at the bottom of the dialog.
     * 
     * @param mainComposite
     *            The component that the Close button should be created within.
     */
    private void createCloseButton(Composite mainComposite) {
        GridLayout gl = new GridLayout(1, false);
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gd.horizontalSpan = 2;
        Composite btnComp = new Composite(mainComposite, SWT.NONE);
        btnComp.setLayout(gl);
        btnComp.setLayoutData(gd);

        Button closeBtn = new Button(btnComp, SWT.PUSH);
        gd = new GridData(SWT.CENTER, SWT.DEFAULT, true, false);
        gd.minimumWidth = closeBtn.getDisplay().getDPI().x;
        closeBtn.setText("Close");
        closeBtn.setLayoutData(gd);
        closeBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                checkForUnsavedChanges();

                close();
            }
        });
    }

    private void populateVoiceList() {
        /**
         * First, retrieve the available {@link TtsVoice}(s).
         */
        java.util.List<TtsVoice> availableVoices = null;
        try {
            availableVoices = this.vdm.getIdentifiers();
        } catch (Exception e) {
            statusHandler.error("Failed to retrieve the available voices.", e);
            return;
        }

        unregisteredVoices = new HashMap<>(BMHVoice.values().length, 1.0f);
        for (BMHVoice bmhVoice : BMHVoice.values()) {
            unregisteredVoices.put(bmhVoice.getId(), bmhVoice);
        }

        if (availableVoices == null || availableVoices.isEmpty()) {
            return;
        }

        for (TtsVoice voice : availableVoices) {
            this.voiceList.add(voice.getVoiceName());
            this.voiceIdentifierMap.put(voice.getVoiceName(),
                    voice.getVoiceNumber());

            this.unregisteredVoices.remove(voice.getVoiceNumber());
        }
        this.voiceList.select(0);

        if (unregisteredVoices.isEmpty()) {
            this.addVoiceButton.setEnabled(false);
        }
    }

    private void populateVoiceAttributes() {
        this.checkForUnsavedChanges();

        if (this.voiceList.getSelection().length <= 0) {
            return;
        }
        this.resetDisplay();

        /*
         * determine the id of the selected voice.
         */
        final String selectedVoiceName = this.voiceList.getSelection()[0];
        int voiceId = this.voiceIdentifierMap.get(selectedVoiceName);

        selectedVoice = null;
        try {
            selectedVoice = this.vdm.getVoiceById(voiceId);
        } catch (Exception e) {
            statusHandler.error("Failed to retreive voice: "
                    + selectedVoiceName + " (id = " + voiceId + ").", e);
            return;
        }

        if (selectedVoice == null) {
            statusHandler.error("Failed to find voice: " + selectedVoiceName
                    + " (id = " + voiceId + ").");
            return;
        }

        this.voiceNameLabel.setText(selectedVoice.getVoiceName());
        this.voiceLanguageLabel.setText(selectedVoice.getLanguage().toString());
        this.dictionaryAssign.setSelectedLanguage(this.selectedVoice
                .getLanguage());
        final String voiceGender = selectedVoice.isMale() ? GENDER_MALE
                : GENDER_FEMALE;
        this.voiceGenderLabel.setText(voiceGender);
        this.dictionaryAssign.setSelectedDictionary(selectedVoice
                .getDictionary());
        this.dictionaryAssign.enable();
    }

    private void resetDisplay() {
        this.voiceNameLabel.setText(StringUtils.EMPTY);
        this.voiceLanguageLabel.setText(StringUtils.EMPTY);
        this.voiceGenderLabel.setText(StringUtils.EMPTY);
        this.dictionaryAssign.disable();
        this.saveBtn.setEnabled(false);
    }

    private void handleSaveAction() {
        if (this.selectedVoice == null) {
            return;
        }

        this.selectedVoice.setDictionary(this.dictionaryAssign
                .getSelectedDictionary());

        try {
            this.vdm.saveTtsVoice(this.selectedVoice);
        } catch (Exception e) {
            statusHandler.error(
                    "Failed to save Voice: "
                            + this.selectedVoice.getVoiceName() + ".", e);
            return;
        }

        this.saveBtn.setEnabled(false);
    }

    private void checkForUnsavedChanges() {
        if (this.selectedVoice == null) {
            return;
        }

        /**
         * Determine if the user has unsaved changes.
         */
        if (this.saveBtn.isEnabled() == false) {
            return;
        }

        StringBuilder sb = new StringBuilder(
                "You have unsaved changes. Would you like to save the changes you made to Voice: ");
        sb.append(this.selectedVoice.getVoiceName());
        sb.append("?");

        int option = DialogUtility.showMessageBox(this.shell, SWT.ICON_WARNING
                | SWT.YES | SWT.NO, "Voice Configuration", sb.toString());
        if (option != SWT.YES) {
            return;
        }

        this.handleSaveAction();
    }

    @Override
    public void dictionarySelected(Dictionary dictionary) {
        this.saveBtn.setEnabled(this.dictionaryAssign
                .dictionaryChanged(this.selectedVoice.getDictionary()));
        if (this.saveBtn.isEnabled()) {
            this.selectedVoice.setRemovedDictionary(this.selectedVoice
                    .getDictionary());
        } else {
            this.selectedVoice.setRemovedDictionary(null);
        }
    }

    private void handleAddVoiceAction() {
        SelectVoiceDlg selectVoiceDlg = new SelectVoiceDlg(this.shell,
                this.unregisteredVoices);
        selectVoiceDlg.setCloseCallback(new ICloseCallback() {
            @Override
            public void dialogClosed(Object returnValue) {
                if (returnValue == null
                        || returnValue instanceof BMHVoice == false) {
                    return;
                }

                addVoice((BMHVoice) returnValue);
            }
        });
        selectVoiceDlg.open();
    }

    private void addVoice(BMHVoice bmhVoice) {
        TtsVoice registeredVoice = null;

        try {
            registeredVoice = this.vdm.registerTtsVoice(bmhVoice);
        } catch (Exception e) {
            statusHandler.error("Failed to register BMH Voice: "
                    + registeredVoice.getVoiceName() + "!", e);
            return;
        }

        if (registeredVoice == null) {
            /*
             * the voice could not be registered.
             */
            StringBuilder sb = new StringBuilder("Unable to register voice: ");
            sb.append(bmhVoice.getName())
                    .append(". It has not been installed or the current TTS License does not support it.");

            DialogUtility.showMessageBox(this.shell, SWT.ICON_INFORMATION
                    | SWT.OK, "Voice Registration", sb.toString());
            return;
        }

        this.voiceIdentifierMap.put(registeredVoice.getVoiceName(),
                registeredVoice.getVoiceNumber());
        this.voiceList.add(registeredVoice.getVoiceName());
        this.unregisteredVoices.remove(registeredVoice.getVoiceNumber());
        if (this.unregisteredVoices.isEmpty()) {
            this.addVoiceButton.setEnabled(false);
        }

        StringBuilder sb = new StringBuilder("Successfully registered voice: ");
        sb.append(bmhVoice.getName()).append(".");

        DialogUtility.showMessageBox(this.shell, SWT.ICON_INFORMATION | SWT.OK,
                "Voice Registration", sb.toString());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.viz.bmh.ui.dialogs.AbstractBMHDialog#okToClose()
     */
    @Override
    public boolean okToClose() {
        this.checkForUnsavedChanges();

        return true;
    }
}