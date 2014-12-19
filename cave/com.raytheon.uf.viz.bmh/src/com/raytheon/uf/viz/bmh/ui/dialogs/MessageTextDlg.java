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
package com.raytheon.uf.viz.bmh.ui.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.viz.ui.dialogs.CaveSWTDialogBase;

/**
 * A modal non-blocking dialog with a label and a scrolling text for messages.
 * 
 * <pre>
 * 
 * TODO This should be placed in com.raytheon.viz.ui.dialogs to allow any
 * package to access it.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 15, 2014 3824       rferrel     Initial creation
 * 
 * </pre>
 * 
 * @author rferrel
 * @version 1.0
 */
public class MessageTextDlg extends CaveSWTDialogBase {

    /** Label may be multiple lines. */
    private final String labelText;

    /** Messages text to place in a scrolling Text. */
    private final String messageText;

    /** Type of message. */
    private final int swtIcon;

    public MessageTextDlg(Shell parentShell, String titleText,
            String labelText, String messageText, int swtIcon) {
        super(parentShell, SWT.PRIMARY_MODAL | SWT.RESIZE, CAVE.DO_NOT_BLOCK);

        if (titleText != null) {
            setText(titleText);
        }

        this.labelText = labelText;
        this.messageText = messageText;
        this.swtIcon = swtIcon;
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
    }

    @Override
    protected void initializeComponents(Shell shell) {

        createLabelControls();
        createMessageControls();
        createBottomButtons();
    }

    @Override
    protected void opened() {
        shell.setMinimumSize(shell.getSize());
    }

    private void createLabelControls() {
        Composite labelComp = new Composite(shell, SWT.NONE);
        labelComp.setLayout(new GridLayout(2, false));
        labelComp
                .setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        GridData gd = new GridData(SWT.DEFAULT, SWT.CENTER, false, true);
        Label iconLbl = new Label(labelComp, SWT.NONE);
        iconLbl.setImage(getDisplay().getSystemImage(swtIcon));
        iconLbl.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.CENTER, true, true);
        Label labelLbl = new Label(labelComp, SWT.NONE);
        labelLbl.setText(labelText);
        labelLbl.setLayoutData(gd);
    }

    private void createMessageControls() {
        Composite msgComp = new Composite(shell, SWT.NONE);
        msgComp.setLayout(new GridLayout(1, false));
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        msgComp.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.widthHint = 400;
        gd.heightHint = 300;
        StyledText stText = new StyledText(msgComp, SWT.BORDER | SWT.READ_ONLY
                | SWT.H_SCROLL | SWT.V_SCROLL);
        stText.setText(messageText);
        stText.setLayoutData(gd);
        stText.setBackground(shell.getBackground());
    }

    private void createBottomButtons() {
        int buttonWidth = 80;
        GridData gd = new GridData(SWT.CENTER, SWT.DEFAULT, true, false);
        Composite mainButtonComp = new Composite(shell, SWT.NONE);
        mainButtonComp.setLayout(new GridLayout(2, false));
        mainButtonComp.setLayoutData(gd);

        gd = new GridData(buttonWidth, SWT.DEFAULT);
        Button okBtn = new Button(mainButtonComp, SWT.PUSH);
        okBtn.setText("OK");
        okBtn.setLayoutData(gd);
        okBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setReturnValue(new Integer(SWT.OK));
                close();
            }
        });

        setReturnValue(new Integer(SWT.CANCEL));
        gd = new GridData(buttonWidth, SWT.DEFAULT);
        Button cancelBtn = new Button(mainButtonComp, SWT.PUSH);
        cancelBtn.setText("Cancel");
        cancelBtn.setLayoutData(gd);
        cancelBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // TODO
                close();
            }
        });
    }
}
