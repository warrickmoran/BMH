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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

import com.raytheon.uf.common.bmh.datamodel.dac.Dac;
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
 * Oct 08, 2014  #3479     lvenable     Changed MODE_INDEPENDENT to PERSPECTIVE_INDEPENDENT.
 * Oct 19, 2014  #3699     mpduff       Implement dialog
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */

public class CreateEditDacConfigDlg extends CaveSWTDialog {

    private final String CHANNEL_BASE_PORT = "Channel Base Port:";

    /** Status handler for reporting errors. */
    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(CreateEditDacConfigDlg.class);

    /** DAC Name text field */
    private Text dacNameTF;

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

    /** The new/edited dac */
    private Dac dac;

    /** List of the port text fields */
    private final List<Text> dacPortTxtFldList = new ArrayList<>(4);

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
                CAVE.DO_NOT_BLOCK | CAVE.PERSPECTIVE_INDEPENDENT);

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

    @Override
    protected void opened() {
        if (dac != null) {
            populate();
        }
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
        Label dacNameDescLbl = new Label(controlComp, SWT.NONE);
        dacNameDescLbl.setText("DAC Name:");

        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        dacNameTF = new Text(controlComp, SWT.BORDER);
        dacNameTF.setTextLimit(10);
        dacNameTF.setLayoutData(gd);

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
        channel1DescLbl.setText(CHANNEL_BASE_PORT);

        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gd.minimumWidth = textFieldMinWidth;
        channel1TF = new Text(controlComp, SWT.BORDER);
        channel1TF.setLayoutData(gd);
        dacPortTxtFldList.add(channel1TF);

        Label channel2DescLbl = new Label(controlComp, SWT.NONE);
        channel2DescLbl.setText(CHANNEL_BASE_PORT);

        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gd.minimumWidth = textFieldMinWidth;
        channel2TF = new Text(controlComp, SWT.BORDER);
        channel2TF.setLayoutData(gd);
        dacPortTxtFldList.add(channel2TF);

        Label channel3DescLbl = new Label(controlComp, SWT.NONE);
        channel3DescLbl.setText(CHANNEL_BASE_PORT);

        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gd.minimumWidth = textFieldMinWidth;
        channel3TF = new Text(controlComp, SWT.BORDER);
        channel3TF.setLayoutData(gd);
        dacPortTxtFldList.add(channel3TF);

        Label channel4DescLbl = new Label(controlComp, SWT.NONE);
        channel4DescLbl.setText(CHANNEL_BASE_PORT);

        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gd.minimumWidth = textFieldMinWidth;
        channel4TF = new Text(controlComp, SWT.BORDER);
        channel4TF.setLayoutData(gd);
        dacPortTxtFldList.add(channel4TF);
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
                boolean success = true;
                if (dialogType == DialogType.CREATE) {
                    success = handleCreateAction();
                } else if (dialogType == DialogType.EDIT) {
                    success = handleSaveAction();
                }

                if (success) {
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
                close();
            }
        });
    }

    /**
     * Populate the dialog.
     */
    private void populate() {
        this.dacNameTF.setText(dac.getName());
        this.dacIpTF.setText(dac.getAddress());
        this.receiveAddressTF.setText(dac.getReceiveAddress());
        this.receivePortTF.setText(String.valueOf(dac.getReceivePort()));
        List<Integer> ports = new ArrayList<Integer>(dac.getDataPorts());
        Collections.sort(ports);

        if (ports.size() > dacPortTxtFldList.size()) {
            statusHandler
                    .warn("The DAC's only have 4 ports.  The database has "
                            + ports.size()
                            + " ports configured.  This should be investigated");
        }

        for (int i = 0; i < ports.size(); i++) {
            int port = ports.get(i);
            Text tf = dacPortTxtFldList.get(i);
            tf.setText(String.valueOf(port));
        }
    }

    /**
     * Create a new dac and save it.
     * 
     * @return true if successful
     */
    private boolean handleCreateAction() {
        dac = new Dac();
        return handleSaveAction();
    }

    /**
     * Save the dac.
     * 
     * @return true if successful
     */
    private boolean handleSaveAction() {
        boolean isValid = true;
        StringBuilder errMsg = new StringBuilder("Invalid values entered \n\n");

        String name = dacNameTF.getText().trim();
        if (name.length() == 0) {
            isValid = false;
            errMsg.append("DAC name is required\n");
        }

        DacDataManager dm = new DacDataManager();
        if (this.dialogType == DialogType.CREATE) {
            try {
                // TODO create a getDacNames query
                List<Dac> dacs = dm.getDacs();
                for (Dac d : dacs) {
                    if (name.equals(d.getName())) {
                        isValid = false;
                        errMsg.append("Dac name must be unique. Use a different name\n");
                        break;
                    }
                }
            } catch (Exception e1) {
                statusHandler.error("Error connecting to database.", e1);
                return false;
            }
        }

        String address = this.dacIpTF.getText().trim();
        String receiveAddress = this.receiveAddressTF.getText().trim();

        String receivePort = this.receivePortTF.getText().trim();
        int receivePortInt = 0;
        if (receivePort.length() > 0) {
            try {
                receivePortInt = Integer.parseInt(receivePort);
            } catch (NumberFormatException e) {
                isValid = false;
                errMsg.append("Receive port must be an integer value\n");
            }
        }

        Set<Integer> ports = new HashSet<Integer>();

        int portNum = 1;
        try {
            for (int i = 0; i < dacPortTxtFldList.size(); i++) {
                String text = this.dacPortTxtFldList.get(i).getText().trim();
                if (text.length() > 0) {
                    ports.add(Integer.valueOf(text));
                }
                portNum++;
            }
        } catch (NumberFormatException e) {
            isValid = false;
            errMsg.append("Channel " + portNum + " must be an integer value");
        }

        if (isValid) {
            dac.setAddress(address);
            dac.setName(name);
            dac.setReceiveAddress(receiveAddress);
            dac.setReceivePort(receivePortInt);
            dac.setDataPorts(ports);

            try {
                dac = dm.saveDac(dac);
                setReturnValue(dac);
            } catch (Exception e) {
                statusHandler.error("Error saving DAC configuation.", e);
            }
        } else {
            DialogUtility.showMessageBox(getShell(), SWT.ICON_ERROR,
                    "Invalid entries", errMsg.toString());
        }

        return isValid;
    }

    /**
     * @return the dac
     */
    public Dac getDac() {
        return dac;
    }

    /**
     * @param dac
     *            the dac to set
     */
    public void setDac(Dac dac) {
        this.dac = dac;
    }
}
