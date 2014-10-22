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

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.bmh.ui.common.utility.DialogUtility;
import com.raytheon.uf.viz.bmh.ui.dialogs.msgtypes.SelectMessageTypeDlg;
import com.raytheon.viz.ui.dialogs.CaveSWTDialog;

/**
 * Message Text Content dialog that allows the user to preview and edit message
 * text.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 13, 2014  #3728     lvenable     Initial creation
 * Oct 22, 2014  #3728     lvenable     Added a preview mode where the text can't be edited.
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */

public class MessageTextContentsDlg extends CaveSWTDialog {

    /** Status handler for reporting errors. */
    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(SelectMessageTypeDlg.class);

    /** StyledText used for previewing and editing the message text. */
    private StyledText messageSt;

    /** Message text to be displayed/edited. */
    private String messageText;

    public enum DialogType {
        PREVIEW, EDIT;
    };

    private boolean preview = false;;

    /**
     * Constructor.
     * 
     * @param parentShell
     *            Parent shell.
     * @param messageText
     *            Message text to be displayed/edited.
     */
    public MessageTextContentsDlg(Shell parentShell, String messageText,
            DialogType dialogType) {
        super(parentShell, SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL,
                CAVE.DO_NOT_BLOCK | CAVE.PERSPECTIVE_INDEPENDENT);

        this.messageText = messageText;

        if (dialogType == DialogType.PREVIEW) {
            preview = true;
        }
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
    protected void initializeComponents(Shell shell) {
        if (preview) {
            setText("Message Text Preview");
        } else {
            setText("Message Text Contents");
        }

        createTextControl();
        createActionButtons();

        if (messageText != null) {
            messageSt.setText(messageText);
        }
    }

    /**
     * Create the styled text control.
     */
    private void createTextControl() {
        GridData gd = new GridData(500, 300);
        messageSt = new StyledText(shell, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL
                | SWT.V_SCROLL);
        messageSt.setWordWrap(true);
        messageSt.setLayoutData(gd);

        if (preview) {
            messageSt.setEditable(false);
        }
    }

    /**
     * Create the action buttons.
     */
    private void createActionButtons() {

        int columns = (preview) ? 2 : 3;

        Composite buttonComp = new Composite(shell, SWT.NONE);
        buttonComp.setLayout(new GridLayout(columns, false));
        buttonComp.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true,
                false));

        int buttonWidth = 75;
        GridData gd = null;

        // OK
        if (preview == false) {
            gd = new GridData(SWT.RIGHT, SWT.DEFAULT, true, false);
            gd.widthHint = buttonWidth;
            Button okBtn = new Button(buttonComp, SWT.PUSH);
            okBtn.setText("OK");

            okBtn.setLayoutData(gd);
            okBtn.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    String text = messageSt.getText().trim();

                    if (text.length() == 0) {
                        StringBuilder msg = new StringBuilder();

                        msg.append("The message does not contain any text.\n");
                        msg.append("Please enter text in the message.");

                        DialogUtility.showMessageBox(getShell(),
                                SWT.ICON_WARNING | SWT.OK, "No Text Error",
                                msg.toString());
                        return;
                    }

                    setReturnValue(text);

                    close();
                }
            });
        }

        // Play
        if (preview) {
            gd = new GridData(SWT.RIGHT, SWT.DEFAULT, true, false);
            gd.widthHint = buttonWidth;
        } else {
            gd = new GridData(buttonWidth, SWT.DEFAULT);
        }
        Button playBtn = new Button(buttonComp, SWT.PUSH);
        playBtn.setText("Play");
        playBtn.setLayoutData(gd);
        playBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // TODO - add play action.
            }
        });

        // Cancel
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
}
