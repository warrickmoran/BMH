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
package com.raytheon.uf.viz.bmh.ui.dialogs.dac;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.bmh.ui.common.utility.DialogUtility;
import com.raytheon.viz.ui.dialogs.CaveSWTDialog;

/**
 * Dialog for creating a new DAC configuration or editing an existing DAC
 * configuration.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 24, 2014  #3660     lvenable     Initial creation
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */

public class CreateEditDacConfigDlg extends CaveSWTDialog {

    /** Status handler for reporting errors. */
    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(CreateEditDacConfigDlg.class);

    /** DAC ID label. */
    private Label dacIdLbl;

    /** DAC IP text control. */
    private Text dacIpTF;

    /** Receive Port text control. */
    private Text receivePortTF;

    /** Receive address text control. */
    private Text receiveAddressTF;

    /** Channel 1 text control. */
    private Text channel1TF;

    /** Channel 2 text control. */
    private Text channel2TF;

    /** Channel 3 text control. */
    private Text channel3TF;

    /** Channel 4 text control. */
    private Text channel4TF;

    /** Enumeration of dialog types. */
    public enum DialogType {
        CREATE, EDIT;
    };

    /** Type of dialog (Create or Edit). */
    private DialogType dialogType = DialogType.CREATE;

    /**
     * Constructor.
     * 
     * @param parentShell
     *            Parent shell.
     * @param dialogType
     *            Dialog type.
     */
    public CreateEditDacConfigDlg(Shell parentShell, DialogType dialogType) {
        super(parentShell, SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL,
                CAVE.DO_NOT_BLOCK | CAVE.MODE_INDEPENDENT);

        this.dialogType = dialogType;
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
        if (dialogType == DialogType.CREATE) {
            setText("Create DAC Configuration");
        } else if (dialogType == DialogType.EDIT) {
            setText("Edit DAC Configuration");
        }

        createControls();
        DialogUtility.addSeparator(shell, SWT.HORIZONTAL);
        createBottomActionButtons();
    }

    /**
     * Create the input text controls and labels.
     */
    private void createControls() {
        Composite controlComp = new Composite(shell, SWT.NONE);
        controlComp.setLayout(new GridLayout(2, false));
        controlComp.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true,
                false));

        int textFieldMinWidth = 200;

        // DAC ID
        Label dacIdDescLbl = new Label(controlComp, SWT.NONE);
        dacIdDescLbl.setText("DAC Id:");

        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        dacIdDescLbl = new Label(controlComp, SWT.NONE);
        dacIdDescLbl.setLayoutData(gd);

        // DAC IP
        Label dacIPDescLbl = new Label(controlComp, SWT.NONE);
        dacIPDescLbl.setText("DAC IP Address:");

        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gd.minimumWidth = textFieldMinWidth;
        dacIpTF = new Text(controlComp, SWT.BORDER);
        dacIpTF.setLayoutData(gd);

        // Receive Port
        Label receivePortDescLbl = new Label(controlComp, SWT.NONE);
        receivePortDescLbl.setText("Receive Port:");

        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gd.minimumWidth = textFieldMinWidth;
        receivePortTF = new Text(controlComp, SWT.BORDER);
        receivePortTF.setLayoutData(gd);

        // Receive Address
        Label receiveAddressDescLbl = new Label(controlComp, SWT.NONE);
        receiveAddressDescLbl.setText("Receive Address:");

        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gd.minimumWidth = textFieldMinWidth;
        receiveAddressTF = new Text(controlComp, SWT.BORDER);
        receiveAddressTF.setLayoutData(gd);

        // Channels
        Label channel1DescLbl = new Label(controlComp, SWT.NONE);
        channel1DescLbl.setText("Channel 1:");

        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gd.minimumWidth = textFieldMinWidth;
        channel1TF = new Text(controlComp, SWT.BORDER);
        channel1TF.setLayoutData(gd);

        Label channel2DescLbl = new Label(controlComp, SWT.NONE);
        channel2DescLbl.setText("Channel 2:");

        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gd.minimumWidth = textFieldMinWidth;
        channel2TF = new Text(controlComp, SWT.BORDER);
        channel2TF.setLayoutData(gd);

        Label channel3DescLbl = new Label(controlComp, SWT.NONE);
        channel3DescLbl.setText("Channel 3:");

        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gd.minimumWidth = textFieldMinWidth;
        channel3TF = new Text(controlComp, SWT.BORDER);
        channel3TF.setLayoutData(gd);

        Label channel4DescLbl = new Label(controlComp, SWT.NONE);
        channel4DescLbl.setText("Channel 4:");

        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gd.minimumWidth = textFieldMinWidth;
        channel4TF = new Text(controlComp, SWT.BORDER);
        channel4TF.setLayoutData(gd);
    }

    /**
     * Create the save/create & cancel actions buttons.
     */
    private void createBottomActionButtons() {
        Composite buttonComp = new Composite(shell, SWT.NONE);
        buttonComp.setLayout(new GridLayout(2, true));
        buttonComp.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true,
                false));

        int buttonWidth = 75;

        GridData gd = new GridData(SWT.RIGHT, SWT.DEFAULT, true, false);
        gd.widthHint = buttonWidth;
        Button createSaveBtn = new Button(buttonComp, SWT.PUSH);
        if (dialogType == DialogType.CREATE) {
            createSaveBtn.setText(" Create ");
        } else if (dialogType == DialogType.EDIT) {
            createSaveBtn.setText(" Save ");
        }

        createSaveBtn.setLayoutData(gd);
        createSaveBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (dialogType == DialogType.CREATE) {
                    handleCreateAction();
                } else if (dialogType == DialogType.EDIT) {
                    handleSaveAction();
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
                close();
            }
        });
    }

    private void handleCreateAction() {
        // TODO : add create action code and verify the user input
    }

    private void handleSaveAction() {
        // TODO : add save action code and verify the user input
    }
}
