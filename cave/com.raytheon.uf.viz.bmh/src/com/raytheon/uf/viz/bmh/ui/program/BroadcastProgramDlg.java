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
package com.raytheon.uf.viz.bmh.ui.program;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.viz.bmh.ui.common.table.TableCellData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableColumnData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableRowData;
import com.raytheon.uf.viz.bmh.ui.common.utility.InputTextDlg;
import com.raytheon.uf.viz.bmh.ui.dialogs.AbstractBMHDialog;
import com.raytheon.uf.viz.bmh.ui.dialogs.msgtypes.MsgTypeTable;
import com.raytheon.viz.ui.dialogs.ICloseCallback;

/**
 * Dialog that manages programs and suites.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 20, 2014  #3174      lvenable     Initial creation
 * Aug 01, 2014  #3479      lvenable    Added additional capability for managing the controls.
 * Aug 03, 2014  #3479      lvenable    Updated code for validator changes.
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */
public class BroadcastProgramDlg extends AbstractBMHDialog {

    /** Program combo box. */
    private Combo programCbo;

    /** New program button. */
    private Button newProgramBtn;

    /** Rename program button. */
    private Button renameProgramBtn;

    /** Delete program button. */
    private Button deleteProgramBtn;

    /** Assign transmitter button. */
    private Button assignTransmitterBtn;

    /** Group that contains the controls for configuring the suites. */
    private SuiteConfigGroup suiteConfigGroup;

    /** Message type table. */
    private MsgTypeTable msgTypeTable;

    /** Message Type group prefix text. */
    private final String messgaeTypeGrpPrefix = " Message Types in Suite: ";

    /** Label show the assigned transmitters for the selected programs */
    private Label transmitterListLbl;

    /** Message type group. */
    private Group messageTypeGroup;

    /** List of program controls. */
    private List<Control> programControls = new ArrayList<Control>();

    /**
     * Constructor.
     * 
     * @param parentShell
     *            Parent shell.
     * @param dlgMap
     *            Map to add this dialog to for closing purposes.
     */
    public BroadcastProgramDlg(Shell parentShell,
            Map<AbstractBMHDialog, String> dlgMap) {
        super(dlgMap, "Broadcast Program Dialog", parentShell, SWT.DIALOG_TRIM
                | SWT.MIN, CAVE.DO_NOT_BLOCK | CAVE.MODE_INDEPENDENT);
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
        setText("Broadcast Program Configuration");

        createProgramControls();
        createTransmitterControls();
        addLabelSeparator(shell);
        createSuiteGroup();
        createMessageTypeGroup();
        createBottomButtons();

        if (programCbo.getItemCount() > 0) {
            programCbo.select(0);
            enableProgramControls(true);
        }

        updateSuiteGroupText();
    }

    /**
     * Create the Program controls.
     */
    private void createProgramControls() {
        Composite progComp = new Composite(shell, SWT.NONE);
        GridLayout gl = new GridLayout(5, false);
        progComp.setLayout(gl);
        progComp.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        /*
         * Program controls.
         */
        Label programLbl = new Label(progComp, SWT.NONE);
        programLbl.setText("Program: ");

        GridData gd = new GridData(SWT.DEFAULT, SWT.CENTER, false, true);
        gd.widthHint = 200;
        programCbo = new Combo(progComp, SWT.VERTICAL | SWT.DROP_DOWN
                | SWT.BORDER | SWT.READ_ONLY);
        programCbo.setLayoutData(gd);
        programCbo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateSuiteGroupText();
            }
        });

        populateProgramCombo();

        int buttonWidth = 90;

        gd = new GridData();
        gd.widthHint = buttonWidth;
        gd.horizontalIndent = 10;
        newProgramBtn = new Button(progComp, SWT.PUSH);
        newProgramBtn.setText(" New... ");
        newProgramBtn.setLayoutData(gd);
        newProgramBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                CreateNewProgram cnp = new CreateNewProgram(shell);
                cnp.open();
            }
        });

        gd = new GridData();
        gd.widthHint = buttonWidth;
        renameProgramBtn = new Button(progComp, SWT.PUSH);
        renameProgramBtn.setText(" Rename... ");
        renameProgramBtn.setLayoutData(gd);
        renameProgramBtn.setEnabled(false);
        renameProgramBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {

                ProgramNameValidator pnv = new ProgramNameValidator();

                InputTextDlg inputDlg = new InputTextDlg(shell,
                        "Rename Program", "Type in a new program name:", pnv);
                inputDlg.setCloseCallback(new ICloseCallback() {
                    @Override
                    public void dialogClosed(Object returnValue) {
                        if (returnValue != null
                                && returnValue instanceof String) {
                            String name = (String) returnValue;
                            System.out.println("Program name = " + name);
                        }
                    }
                });
                inputDlg.open();
            }
        });
        programControls.add(renameProgramBtn);

        gd = new GridData(SWT.LEFT, SWT.DEFAULT, true, false);
        gd.widthHint = buttonWidth;
        deleteProgramBtn = new Button(progComp, SWT.PUSH);
        deleteProgramBtn.setText(" Delete ");
        deleteProgramBtn.setLayoutData(gd);
        deleteProgramBtn.setEnabled(false);
        deleteProgramBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                deleteProgramAction();
            }
        });
        programControls.add(deleteProgramBtn);
    }

    /**
     * Create the transmitter controls.
     */
    private void createTransmitterControls() {
        Composite progTransComp = new Composite(shell, SWT.NONE);
        GridLayout gl = new GridLayout(3, false);
        progTransComp.setLayout(gl);
        progTransComp.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true,
                false));

        /*
         * Transmitter controls.
         */
        Label transmitterLbl = new Label(progTransComp, SWT.NONE);
        transmitterLbl.setText("Assigned Transmitters: ");

        GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, true);
        gd.minimumWidth = 300;
        transmitterListLbl = new Label(progTransComp, SWT.BORDER);
        transmitterListLbl.setLayoutData(gd);
        populateTransmitters();

        assignTransmitterBtn = new Button(progTransComp, SWT.PUSH);
        assignTransmitterBtn.setText(" Assign Transmitter(s)... ");
        assignTransmitterBtn.setEnabled(false);
        assignTransmitterBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                AddTransmittersDlg atd = new AddTransmittersDlg(shell,
                        programCbo.getItem(programCbo.getSelectionIndex()));
                atd.open();
            }
        });
        programControls.add(assignTransmitterBtn);
    }

    /**
     * Create the suite group.
     */
    private void createSuiteGroup() {
        suiteConfigGroup = new SuiteConfigGroup(shell);
    }

    /**
     * Create the message types group.
     */
    private void createMessageTypeGroup() {

        messageTypeGroup = new Group(shell, SWT.SHADOW_OUT);
        GridLayout gl = new GridLayout(1, false);
        messageTypeGroup.setLayout(gl);
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        messageTypeGroup.setLayoutData(gd);
        messageTypeGroup.setText(messgaeTypeGrpPrefix);

        msgTypeTable = new MsgTypeTable(messageTypeGroup, 400, 150);

        populateMsgTypeTable();
    }

    /**
     * Update the suite group text with the currently selected program name.
     */
    private void updateSuiteGroupText() {
        if (programCbo.getItemCount() > 0
                && programCbo.getSelectionIndex() >= 0) {
            suiteConfigGroup.updateSuiteGroupText(programCbo.getItem(programCbo
                    .getSelectionIndex()));
        } else {
            suiteConfigGroup.updateSuiteGroupText("N/A");
        }
    }

    /**
     * Create the bottom action buttons.
     */
    private void createBottomButtons() {
        Composite buttonComp = new Composite(shell, SWT.NONE);
        buttonComp.setLayout(new GridLayout(1, false));
        buttonComp.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true,
                false));

        int buttonWidth = 70;

        GridData gd = new GridData(SWT.CENTER, SWT.DEFAULT, true, false);
        gd.widthHint = buttonWidth;
        Button closeBtn = new Button(buttonComp, SWT.PUSH);
        closeBtn.setText(" Close ");
        closeBtn.setLayoutData(gd);
        closeBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                close();
            }
        });
    }

    /**
     * Add a separator to the display.
     * 
     * @param comp
     *            Composite.
     * @param orientation
     *            Separator orientation.
     */
    private void addLabelSeparator(Composite comp) {

        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gd.heightHint = 4;

        Label sepLbl = new Label(comp, SWT.NONE);
        sepLbl.setLayoutData(gd);
        sepLbl.setBackground(getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
    }

    /**
     * Enable/Disable the program controls.
     * 
     * @param enable
     *            True to enable controls, false to disable.
     */
    private void enableProgramControls(boolean enable) {
        for (Control ctrl : programControls) {
            ctrl.setEnabled(enable);
        }
    }

    /**
     * Action taken when deleting a program.
     */
    private void deleteProgramAction() {
        // TODO: delete the selected program

        if (programCbo.getItemCount() > 0) {
            programCbo.select(0);
            enableProgramControls(true);
        }
    }

    /**
     * Method to check if the dialog can close.
     * 
     * For example: if there are items that are unsaved then the user should be
     * prompted that the dialog has unsaved items and be given the opportunity
     * to prevent the dialog from closing.
     */
    @Override
    public boolean okToClose() {
        /*
         * TODO:
         * 
         * Need to put in code to check/validate if the dialog can close (need
         * to save before closing, etc).
         */
        return true;
    }

    /***********************************************************************
     * 
     * TODO: REMOVE THESE WHEN DONE
     * 
     * */

    private void populateProgramCombo() {
        // for (int i = 0; i < 20; i++) {
        // programCbo.add("Program_Name_" + i);
        // }
        programCbo.add("Sample Program");
        programCbo.add("Sever Weather");
        programCbo.add("Lee Test Program");
        programCbo.add("Omaha Winter Weather");

    }

    private void populateTransmitters() {
        transmitterListLbl.setText(" LNK, ESX, SHU, HNK, OMA, GID");
        transmitterListLbl.setToolTipText(" LNK, ESX, SHU, HNK, OMA, GID");
    }

    private void populateMsgTypeTable() {

        List<TableColumnData> columnNames = new ArrayList<TableColumnData>();
        TableColumnData tcd = new TableColumnData("Message Type", 150);
        columnNames.add(tcd);
        tcd = new TableColumnData("Message Title");
        columnNames.add(tcd);
        tcd = new TableColumnData("Trigger");
        columnNames.add(tcd);

        TableData td = new TableData(columnNames);

        TableRowData trd = new TableRowData();

        trd.addTableCellData(new TableCellData("MessageType - 1"));
        trd.addTableCellData(new TableCellData("MessageType - 1 - Description"));
        trd.addTableCellData(new TableCellData("Yes"));

        td.addDataRow(trd);

        trd = new TableRowData();

        trd.addTableCellData(new TableCellData("MessageType - 2"));
        trd.addTableCellData(new TableCellData("MessageType - 2 - Description"));
        trd.addTableCellData(new TableCellData("No"));

        td.addDataRow(trd);

        msgTypeTable.populateTable(td);
    }
}
