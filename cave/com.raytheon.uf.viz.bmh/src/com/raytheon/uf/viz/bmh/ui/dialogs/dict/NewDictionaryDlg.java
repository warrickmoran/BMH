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
package com.raytheon.uf.viz.bmh.ui.dialogs.dict;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.raytheon.uf.common.bmh.datamodel.language.Dictionary;
import com.raytheon.uf.common.bmh.datamodel.language.Language;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.bmh.data.DictionaryManager;
import com.raytheon.viz.ui.dialogs.CaveSWTDialog;

/**
 * Create new {@link Dictionary} dialog.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 1, 2014     3355    mpduff      Initial creation
 * Sep 10, 2014    3355    mpduff      Added SWT.PRIMARY_MODAL
 * Dec 16, 2014    3618    bkowal      Allow the user to create a national dictionary
 *                                     if one does not already exist for the selected language.
 * Jan 05, 2015    3618    bkowal      Fix "National Dictionary" checkbox text.
 * Jan 07, 2015    3931    bkowal      Abstracted to allow for a {@link Dictionary} renaming 
 *                                     capability.
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class NewDictionaryDlg extends CaveSWTDialog {
    protected final IUFStatusHandler statusHandler = UFStatus
            .getHandler(getClass());

    /**
     * Used to retrieve {@link Dictionary} information.
     */
    protected final DictionaryManager dm = new DictionaryManager();

    /**
     * Dictionary name text field
     */
    protected Text nameTxt;

    /**
     * Dictionary language combo
     */
    protected Combo languageCombo;

    /**
     * National Dictionary checkbox. Note, this button will only be visible if
     * there is not already a national dictionary for the selected language.
     */
    protected Button nationalBtn;

    /**
     * Constructor.
     * 
     * @param parentShell
     */
    public NewDictionaryDlg(Shell parentShell) {
        super(parentShell, SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL,
                CAVE.PERSPECTIVE_INDEPENDENT);

        setText("New Dictionary");
    }

    @Override
    protected Layout constructShellLayout() {
        return new GridLayout(1, false);
    }

    @Override
    protected Object constructShellLayoutData() {
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        return gd;
    }

    @Override
    protected void initializeComponents(Shell shell) {
        GridData gd = new GridData(SWT.CENTER, SWT.DEFAULT, true, false);
        Label msgLbl = new Label(shell, SWT.NONE);
        msgLbl.setText("Enter the new dictionary name:");
        msgLbl.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        nameTxt = new Text(shell, SWT.BORDER);
        nameTxt.setLayoutData(gd);
        nameTxt.setTextLimit(20);

        gd = new GridData(SWT.CENTER, SWT.DEFAULT, false, false);
        Label languageLbl = new Label(shell, SWT.NONE);
        languageLbl.setText("Select the new dictionary language:");
        languageLbl.setLayoutData(gd);

        gd = new GridData(SWT.CENTER, SWT.DEFAULT, true, false);
        languageCombo = new Combo(shell, SWT.SINGLE);
        languageCombo.setLayoutData(gd);
        Language[] languages = Language.values();
        String[] langStr = new String[languages.length];
        for (int i = 0; i < languages.length; i++) {
            Language l = languages[i];
            langStr[i] = l.name();
        }
        languageCombo.setItems(langStr);
        languageCombo.select(0);
        languageCombo.addSelectionListener(new SelectionAdapter() {
            /*
             * (non-Javadoc)
             * 
             * @see
             * org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse
             * .swt.events.SelectionEvent)
             */
            @Override
            public void widgetSelected(SelectionEvent e) {
                determineNationalAvailability();
            }
        });

        gd = new GridData(SWT.CENTER, SWT.DEFAULT, false, false);
        this.nationalBtn = new Button(shell, SWT.CHECK);
        this.nationalBtn.setText("National Dictionary");
        this.nationalBtn.setLayoutData(gd);

        gd = new GridData(SWT.CENTER, SWT.DEFAULT, false, false);
        GridLayout gl = new GridLayout(2, false);
        Composite btnComp = new Composite(shell, SWT.NONE);
        btnComp.setLayout(gl);
        btnComp.setLayoutData(gd);

        gd = new GridData(75, SWT.DEFAULT);
        Button okBtn = new Button(btnComp, SWT.PUSH);
        okBtn.setText("OK");
        okBtn.setLayoutData(gd);
        okBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleOkAction();
            }
        });

        gd = new GridData(75, SWT.DEFAULT);
        Button cancelBtn = new Button(btnComp, SWT.PUSH);
        cancelBtn.setText("Cancel");
        cancelBtn.setLayoutData(gd);
        cancelBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setReturnValue(null);
                close();
            }
        });

        shell.setDefaultButton(okBtn);
        this.determineNationalAvailability();
    }

    /**
     * Action invoked when the user clicks on the OK button.
     */
    protected void handleOkAction() {
        if (this.validate() == false) {
            return;
        }

        Dictionary dict = new Dictionary();
        dict.setName(nameTxt.getText().trim());
        dict.setLanguage(Language.valueOf(languageCombo.getText()));
        dict.setNational(nationalBtn.getSelection());
        setReturnValue(dict);
        close();
    }

    /**
     * Validate that all required information has been entered correctly.
     * 
     * @return {@code true}, if the form has been filled out correctly;
     *         {@code false}, otherwise
     */
    protected boolean validate() {
        if (nameTxt.getText().trim().isEmpty()) {
            MessageBox mb = new MessageBox(getShell(), SWT.ICON_WARNING
                    | SWT.OK);
            mb.setText("Enter Name");
            mb.setMessage("Enter a dictionary name.");
            mb.open();
            return false;
        }

        return true;
    }

    private void determineNationalAvailability() {
        final Language selectedLanguage = Language.valueOf(languageCombo
                .getText());
        boolean visible = false;
        /*
         * Attempt to retrieve the national dictionary for the selected language
         * if one exists.
         */
        try {
            /**
             * If there is not a national {@link Dictionary}, users will have
             * the option to create a national {@link Dictionary}.
             */
            visible = this.dm
                    .getNationalDictionaryForLanguage(selectedLanguage) == null;
        } catch (Exception e) {
            statusHandler.error(
                    "Failed to determine if a national dictionary exists for language: "
                            + selectedLanguage.toString() + ".", e);
        }

        this.nationalBtn.setVisible(visible);
        this.nationalBtn.setSelection(false);
    }
}