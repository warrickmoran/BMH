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
import java.util.Set;
import java.util.TreeMap;

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

import com.raytheon.uf.common.bmh.datamodel.msg.Program;
import com.raytheon.uf.common.bmh.datamodel.msg.Suite;
import com.raytheon.uf.common.bmh.datamodel.msg.SuiteMessage;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.request.ProgramRequest;
import com.raytheon.uf.common.bmh.request.ProgramRequest.ProgramAction;
import com.raytheon.uf.common.bmh.request.ProgramResponse;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.bmh.data.BmhUtils;
import com.raytheon.uf.viz.bmh.ui.common.table.TableCellData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableColumnData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableRowData;
import com.raytheon.uf.viz.bmh.ui.common.utility.InputTextDlg;
import com.raytheon.uf.viz.bmh.ui.dialogs.AbstractBMHDialog;
import com.raytheon.uf.viz.bmh.ui.dialogs.msgtypes.MsgTypeTable;
import com.raytheon.uf.viz.bmh.ui.dialogs.suites.ISuiteSelection;
import com.raytheon.uf.viz.bmh.ui.program.SuiteConfigGroup.SuiteGroupType;
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
 * Aug 06, 2014  #3490      lvenable    Update to populate controls with data from the database.
 * Aug 8,  2014  #3490      lvenable    Updated populate table method call.
 * Aug 12, 2014  #3490      lvenable    Updated to use data from the database.
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */
public class BroadcastProgramDlg extends AbstractBMHDialog {

    /** Status handler for reporting errors. */
    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(BroadcastProgramDlg.class);

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

    /** List of program and associated data. */
    private List<Program> programsArray = new ArrayList<Program>();

    /** Message Type table data. */
    private TableData msgTypeTableData = null;

    /** Suite group text prefix. */
    private final String suiteGroupTextPrefix = " Suites in Program: ";

    /** The selected program. */
    private Program selectedProgram = null;

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
    protected void initializeComponents(Shell shell) {
        setText("Broadcast Program Configuration");

        // Get the Program data.
        retrieveProgramDataFromDB();

        createProgramControls();
        createTransmitterControls();
        addLabelSeparator(shell);
        createSuiteGroup();
        createMessageTypeGroup();
        createBottomButtons();

        /*
         * Populate the combos and tables
         */
        populateProgramCombo();
        handleProgramChange();
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
                handleProgramChange();
            }
        });

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
        suiteConfigGroup = new SuiteConfigGroup(shell, suiteGroupTextPrefix,
                SuiteGroupType.BROADCAST_PROGRAM, selectedProgram);
        suiteConfigGroup.setCallBackAction(new ISuiteSelection() {
            @Override
            public void suiteSelected(Suite suite) {
                populateMsgTypeTable(suite);
            }

            @Override
            public void suitesUpdated() {
                // TODO : if the suites have been updated then need to reload
                // the data
            }
        });
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
    }

    /**
     * Update the suite group text with the currently selected program name.
     */
    private void updateSuiteGroupText() {
        if (programCbo.getItemCount() > 0
                && programCbo.getSelectionIndex() >= 0) {
            suiteConfigGroup.updateSuiteGroupText(suiteGroupTextPrefix
                    + programCbo.getItem(programCbo.getSelectionIndex()));
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

    /**
     * Handle when the program changes.
     */
    private void handleProgramChange() {
        populateTransmitters();
        populateSuiteTable();
        populateMsgTypeTable(suiteConfigGroup.getSelectedSuite());
        updateSuiteGroupText();
        updateSelectedProgram();
    }

    /**
     * Retrieve the data from the database.
     */
    private void retrieveProgramDataFromDB() {
        ProgramRequest pr = new ProgramRequest();
        pr.setAction(ProgramAction.AllPrograms);
        ProgramResponse progResponse = null;
        try {
            progResponse = (ProgramResponse) BmhUtils.sendRequest(pr);

            programsArray = progResponse.getProgramList();
        } catch (Exception e) {
            statusHandler.error(
                    "Error retrieving program data from the database: ", e);
        }
    }

    /**
     * Populate the program combo box.
     */
    private void populateProgramCombo() {
        for (Program prog : programsArray) {
            programCbo.add(prog.getName());
        }

        if (programCbo.getItemCount() > 0) {
            programCbo.select(0);
            enableProgramControls(true);
            updateSelectedProgram();
        }
    }

    /**
     * Update the selected program when the table selection changes.
     */
    private void updateSelectedProgram() {
        if (programCbo.getSelectionIndex() >= 0) {
            selectedProgram = programsArray.get(programCbo.getSelectionIndex());
        }
    }

    /**
     * Populate the transmitters.
     */
    private void populateTransmitters() {
        int index = programCbo.getSelectionIndex();

        if (index >= 0) {
            StringBuilder sb = new StringBuilder(" ");
            Program prog = programsArray.get(index);
            Set<TransmitterGroup> transGrp = prog.getTransmitterGroups();
            for (TransmitterGroup tg : transGrp) {
                Set<Transmitter> transmitters = tg.getTransmitters();
                for (Transmitter t : transmitters) {
                    sb.append(t.getName()).append(" ");
                }
            }
            transmitterListLbl.setText(sb.toString());
            transmitterListLbl.setToolTipText(sb.toString());
        }
    }

    /**
     * Populate the suite table.
     */
    private void populateSuiteTable() {
        int index = programCbo.getSelectionIndex();

        if (index >= 0) {
            Program prog = programsArray.get(index);
            List<Suite> suiteList = prog.getSuites();
            suiteConfigGroup.populateSuiteTable(suiteList);
        }
    }

    /**
     * Populate the message type table.
     * 
     * @param suite
     *            Associated suite.
     */
    private void populateMsgTypeTable(Suite suite) {
        if (suite == null) {
            if (msgTypeTable.hasTableData() == false) {
                return;
            } else {
                msgTypeTableData.deleteAllRows();
                msgTypeTable.populateTable(msgTypeTableData);
                return;
            }
        }

        if (msgTypeTable.hasTableData() == false) {
            List<TableColumnData> columnNames = new ArrayList<TableColumnData>();
            TableColumnData tcd = new TableColumnData("Message Type", 150);
            columnNames.add(tcd);
            tcd = new TableColumnData("Message Title");
            columnNames.add(tcd);
            tcd = new TableColumnData("Trigger");
            columnNames.add(tcd);

            msgTypeTableData = new TableData(columnNames);
        } else {
            msgTypeTableData.deleteAllRows();
        }

        populateMsgTypeTableData(suite);
        msgTypeTable.populateTable(msgTypeTableData);
    }

    /**
     * Populate the message type table.
     * 
     * @param suite
     *            Suite data containing the message type data.
     */
    private void populateMsgTypeTableData(Suite suite) {

        List<SuiteMessage> suiteMessageArray = suite.getSuiteMessages();

        Map<Integer, SuiteMessage> suiteMsgMap = new TreeMap<Integer, SuiteMessage>();
        for (SuiteMessage sm : suiteMessageArray) {
            suiteMsgMap.put(sm.getPosition(), sm);
        }

        for (SuiteMessage sm : suiteMsgMap.values()) {

            TableRowData trd = new TableRowData();

            trd.addTableCellData(new TableCellData(sm.getMsgType().getAfosid()));
            trd.addTableCellData(new TableCellData(sm.getMsgType().getTitle()));
            trd.addTableCellData(new TableCellData(sm.isTrigger() ? "Yes"
                    : "No"));

            msgTypeTableData.addDataRow(trd);
        }
    }
}
