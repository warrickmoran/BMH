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

import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.viz.bmh.ui.dialogs.AbstractBMHDialog;
import com.raytheon.uf.viz.bmh.ui.dialogs.DlgInfo;

/**
 * TTS Voice Configuration dialog. This dialog allows users to see the voices
 * and assign a dictionary to voices that are available in the system.
 * 
 * TODO: implement dialog. This initial check-in just includes dialog design.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 13, 2014 3618       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class VoiceConfigDialog extends AbstractBMHDialog {

    public VoiceConfigDialog(Map<AbstractBMHDialog, String> map,
            Shell parentShell) {
        super(map, DlgInfo.TTS_VOICE_CONFIGURATION.getTitle(), parentShell,
                SWT.DIALOG_TRIM, CAVE.PERSPECTIVE_INDEPENDENT
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
        voicesGroup.setText(" Voices ");
        voicesGroup.setLayout(gl);
        voicesGroup.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.widthHint = 125;
        /* Create the list. */
        List voiceList = new List(voicesGroup, SWT.V_SCROLL | SWT.H_SCROLL
                | SWT.SINGLE | SWT.BORDER);
        voiceList.setLayoutData(gd);
        voiceList.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // TODO: Implement
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
        attributesGroup.setText(" Attributes ");
        attributesGroup.setLayout(gl);
        attributesGroup.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gl = new GridLayout(3, false);
        Composite attributesComp = new Composite(attributesGroup, SWT.NONE);
        attributesComp.setLayoutData(gd);
        attributesComp.setLayout(gl);

        /* Voice Name */
        gd = new GridData(SWT.FILL, SWT.DEFAULT, false, false);
        gd.verticalIndent = 5;
        Label nameLabel = new Label(attributesComp, SWT.RIGHT);
        nameLabel.setText("Name: ");
        nameLabel.setLayoutData(gd);
        gd = new GridData(SWT.FILL, SWT.DEFAULT, false, false);
        gd.widthHint = 225;
        gd.horizontalSpan = 2;
        gd.verticalIndent = 5;
        Label voiceNameLabel = new Label(attributesComp, SWT.NONE);
        voiceNameLabel.setText("TTS Voice NameWWWWWW");
        voiceNameLabel.setLayoutData(gd);

        /* Voice Language */
        gd = new GridData(SWT.FILL, SWT.DEFAULT, false, false);
        gd.verticalIndent = 5;
        Label languageLabel = new Label(attributesComp, SWT.RIGHT);
        languageLabel.setText("Language: ");
        languageLabel.setLayoutData(gd);
        gd = new GridData(SWT.FILL, SWT.DEFAULT, false, false);
        gd.horizontalSpan = 2;
        gd.verticalIndent = 5;
        Label voiceLanguageLabel = new Label(attributesComp, SWT.NONE);
        voiceLanguageLabel.setText("ENGLISH");
        voiceLanguageLabel.setLayoutData(gd);

        /* Voice Gender */
        gd = new GridData(SWT.FILL, SWT.DEFAULT, false, false);
        gd.verticalIndent = 5;
        Label genderLabel = new Label(attributesComp, SWT.RIGHT);
        genderLabel.setText("Gender: ");
        genderLabel.setLayoutData(gd);
        gd = new GridData(SWT.FILL, SWT.DEFAULT, false, false);
        gd.horizontalSpan = 2;
        gd.verticalIndent = 5;
        Label voiceGenderLabel = new Label(attributesComp, SWT.NONE);
        voiceGenderLabel.setText("MALE");
        voiceGenderLabel.setLayoutData(gd);

        /* Voice Dictionary */
        gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
        gd.verticalIndent = 5;
        Label dictionaryLabel = new Label(attributesComp, SWT.RIGHT);
        dictionaryLabel.setText("Dictionary: ");
        dictionaryLabel.setLayoutData(gd);
        gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
        gd.widthHint = 200;
        gd.verticalIndent = 5;
        Label voiceDictionaryLabel = new Label(attributesComp, SWT.BORDER);
        voiceDictionaryLabel.setText("");
        voiceDictionaryLabel.setLayoutData(gd);
        gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
        Button changeBtn = new Button(attributesComp, SWT.PUSH);
        changeBtn.setText("Change...");
        // TODO: implement Change Button
        changeBtn.setLayoutData(gd);

        /*
         * Save Button - will only be enabled if the dictionary has been
         * changed.
         */
        gd = new GridData(SWT.CENTER, SWT.DEFAULT, false, false);
        gd.horizontalSpan = 3;
        gd.widthHint = 75;
        gd.verticalIndent = 8;

        Button saveBtn = new Button(attributesGroup, SWT.PUSH);
        saveBtn.setText("Save");
        saveBtn.setEnabled(false);
        saveBtn.setLayoutData(gd);
        // TODO: implement Save Button
    }

    /**
     * Creates the "Close" button at the bottom of the dialog.
     * 
     * @param mainComposite
     *            The component that the Close button should be created within.
     */
    private void createCloseButton(Composite mainComposite) {
        GridLayout gl = new GridLayout(2, false);
        GridData gd = new GridData(SWT.CENTER, SWT.DEFAULT, false, false);
        gd.horizontalSpan = 2;
        Composite btnComp = new Composite(mainComposite, SWT.NONE);
        btnComp.setLayout(gl);
        btnComp.setLayoutData(gd);

        gd = new GridData(75, SWT.DEFAULT);
        Button closeBtn = new Button(btnComp, SWT.PUSH);
        closeBtn.setText("Close");
        closeBtn.setLayoutData(gd);
        closeBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                close();
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.viz.bmh.ui.dialogs.AbstractBMHDialog#okToClose()
     */
    @Override
    public boolean okToClose() {
        return true;
    }
}