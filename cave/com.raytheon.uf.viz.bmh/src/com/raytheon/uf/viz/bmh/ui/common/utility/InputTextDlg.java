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
package com.raytheon.uf.viz.bmh.ui.common.utility;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.raytheon.viz.ui.dialogs.CaveSWTDialog;

/**
 * 
 * A dialog that accepts text input from the user and returns it to the calling
 * program.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 7, 2014   #3174     lvenable     Initial creation
 * Aug 03, 2014  #3479      lvenable    Updated code for validator changes.
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */
public class InputTextDlg extends CaveSWTDialog {

    /** Input text field. */
    private Text inputTf;

    /** Description message. */
    private String descriptionTxt;

    /** Text to populate the text field with on start up. */
    private String textFieldText = null;

    /** Text validator. */
    private IInputTextValidator textValidator;

    /**
     * Constructor.
     * 
     * @param parentShell
     *            Parent shell.
     * @param title
     *            Dialog title.
     * @param descriptionTxt
     *            Description text.
     * @param textValidator
     *            Text validator.
     */
    public InputTextDlg(Shell parentShell, String title, String descriptionTxt,
            IInputTextValidator textValidator) {
        this(parentShell, title, descriptionTxt, null, textValidator);
    }

    /**
     * Constructor.
     * 
     * @param parentShell
     *            Parent shell.
     * @param title
     *            Dialog title.
     * @param descriptionTxt
     *            Description text.
     * @param tfText
     *            Text to be put in the input text control.
     * @param textValidator
     *            Text validator.
     */
    public InputTextDlg(Shell parentShell, String title, String descriptionTxt,
            String tfText, IInputTextValidator textValidator) {
        super(parentShell, SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL,
                CAVE.DO_NOT_BLOCK | CAVE.MODE_INDEPENDENT);

        this.descriptionTxt = descriptionTxt;
        this.textFieldText = tfText;
        this.textValidator = textValidator;

        setText(title);
    }

    @Override
    protected Layout constructShellLayout() {
        // Create the main layout for the shell.
        GridLayout mainLayout = new GridLayout(1, false);
        return mainLayout;
    }

    @Override
    protected Object constructShellLayoutData() {
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        return gd;
    }

    @Override
    protected void disposed() {
    }

    @Override
    protected void initializeComponents(Shell shell) {
        createTextControl();
        createBottomButtons();
    }

    /**
     * Create the text control.
     */
    private void createTextControl() {
        Label messageLbl = new Label(shell, SWT.NONE);
        messageLbl.setText(descriptionTxt);

        GridData gd = new GridData(300, SWT.DEFAULT);
        inputTf = new Text(shell, SWT.BORDER);
        inputTf.setLayoutData(gd);

        if (textFieldText != null) {
            inputTf.setText(textFieldText);
        }
    }

    /**
     * Create the bottom action buttons.
     */
    private void createBottomButtons() {
        Composite buttonComp = new Composite(shell, SWT.NONE);
        buttonComp.setLayout(new GridLayout(2, false));
        buttonComp.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true,
                false));

        int buttonWidth = 70;

        GridData gd = new GridData(SWT.RIGHT, SWT.DEFAULT, true, false);
        gd.widthHint = buttonWidth;
        Button okBtn = new Button(buttonComp, SWT.PUSH);
        okBtn.setText(" OK ");
        okBtn.setLayoutData(gd);
        okBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // If the text validator is null then verify there is text in
                // the input field before returning.

                inputTf.setText(inputTf.getText().trim());

                if (textValidator == null) {
                    if (validInput()) {
                        setReturnValue(inputTf.getText());
                        close();
                    }
                } else if (textValidator.validateInputText(shell,
                        inputTf.getText())) {
                    setReturnValue(inputTf.getText());
                    close();
                }
            }
        });

        gd = new GridData(SWT.LEFT, SWT.DEFAULT, true, false);
        gd.widthHint = buttonWidth;
        Button cancelBtn = new Button(buttonComp, SWT.PUSH);
        cancelBtn.setText(" Cancel ");
        cancelBtn.setLayoutData(gd);
        cancelBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setReturnValue(null);
                close();
            }
        });
    }

    /**
     * Validate the text the user entered in.
     * 
     * @return True if the text was valid.
     */
    private boolean validInput() {
        if (inputTf.getText().length() == 0) {
            MessageBox mb = new MessageBox(shell, SWT.ICON_QUESTION | SWT.OK);
            mb.setText("Invalid Entry");
            mb.setMessage("No text was entered in.  Please input a value.");
            mb.open();
            return false;
        }

        return true;
    }
}
