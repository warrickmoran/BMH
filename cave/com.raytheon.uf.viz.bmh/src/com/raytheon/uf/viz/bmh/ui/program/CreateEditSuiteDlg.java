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
import java.util.TreeMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.common.bmh.datamodel.msg.Program;
import com.raytheon.uf.common.bmh.datamodel.msg.Suite;
import com.raytheon.uf.common.bmh.datamodel.msg.Suite.SuiteType;
import com.raytheon.uf.common.bmh.datamodel.msg.SuiteMessage;
import com.raytheon.uf.common.bmh.request.MessageTypeRequest;
import com.raytheon.uf.common.bmh.request.MessageTypeRequest.MessageTypeAction;
import com.raytheon.uf.common.bmh.request.MessageTypeResponse;
import com.raytheon.uf.common.bmh.request.ProgramRequest;
import com.raytheon.uf.common.bmh.request.ProgramRequest.ProgramAction;
import com.raytheon.uf.common.bmh.request.ProgramResponse;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.bmh.data.BmhUtils;
import com.raytheon.uf.viz.bmh.ui.common.table.ITableActionCB;
import com.raytheon.uf.viz.bmh.ui.common.table.TableCellData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableColumnData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableMoveAction;
import com.raytheon.uf.viz.bmh.ui.common.table.TableRowData;
import com.raytheon.uf.viz.bmh.ui.common.utility.CheckListData;
import com.raytheon.uf.viz.bmh.ui.common.utility.CheckScrollListDlg;
import com.raytheon.uf.viz.bmh.ui.common.utility.UpDownImages;
import com.raytheon.uf.viz.bmh.ui.common.utility.UpDownImages.Arrows;
import com.raytheon.uf.viz.bmh.ui.dialogs.msgtypes.MsgTypeTable;
import com.raytheon.viz.ui.dialogs.CaveSWTDialog;
import com.raytheon.viz.ui.dialogs.ICloseCallback;

/**
 * Dialog that will create or edit a suite.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 20, 2014  #3174      lvenable     Initial creation
 * Jul 24, 2014  #3433     lvenable     Updated for Suite manager
 * Aug 01, 2014  #3479      lvenable    Added additional capability.
 * Aug 12, 2014  #3490      lvenable    Updated to use data from the database.
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */
public class CreateEditSuiteDlg extends CaveSWTDialog {

    /** Status handler for reporting errors. */
    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(CreateEditSuiteDlg.class);

    /** List of all message types. */
    private List<MessageType> allMsgTypesList;

    /** List of all message types. */
    private List<SuiteMessage> msgTypesInSuiteList = new ArrayList<SuiteMessage>();

    /** Message type table data. */
    private TableData selectedMsgTypeTableData;

    /** Message type table data. */
    private TableData availMsgTypeTableData;

    /** Save button. */
    private Button createSaveBtn;

    /** Button to add message types. */
    private Button addMsgTypesBtn;

    /** Button to remove message types. */
    private Button removeMsgTypesBtn;

    /** Table of selected message types. */
    private MsgTypeTable selectedMsgTypeTable;

    /** Table of available message types. */
    private MsgTypeTable availableMsgTypeTable;

    /** Suite name text field. */
    private Text suiteNameTF;

    /** Category combo box. */
    private Combo categoryCbo;

    /** Suite name label. */
    private Label suiteNameLbl;

    /** Button to move the selected message type up in the table. */
    private Button moveUpBtn;

    /** Button to move the selected message type down in the table. */
    private Button moveDownBtn;

    /** Class providing up/down images. */
    private UpDownImages upDownImages;

    /** Button used to set the triggers on the message type(s). */
    private Button setTriggersBtn;

    /** Label containing a list of programs. */
    private Label programListLbl;

    /** Assign program button. */
    private Button assignProgramBtn;

    /** Enumeration of dialog types. */
    public enum DialogType {
        CREATE, EDIT;
    };

    /** Type of dialog (Create or Edit). */
    private DialogType dialogType = DialogType.CREATE;

    /** Show program information flag. */
    private boolean showProgramControls = true;

    /** Table move action that will move items in a table up/down. */
    private TableMoveAction tableMoveAction;

    /** The selected program for this suite. */
    private Program selectedProgram = null;

    /** The selected suite (for editing) */
    private Suite selectedSuite = null;

    /** List of program and associated data. */
    private List<Program> programsArray = new ArrayList<Program>();

    /** List of assigned program for this suite. */
    private List<Program> assignedPrograms = new ArrayList<Program>();

    /**
     * Constructor.
     * 
     * @param parentShell
     *            Parent shell.
     * @param dlgType
     *            Dialog type.
     * @param showProgramControls
     *            Flag indicating if the program controls should be shown.
     * @param selectedSuite
     *            The selected suite.
     */
    public CreateEditSuiteDlg(Shell parentShell, DialogType dlgType,
            boolean showProgramControls, Suite selectedSuite) {
        this(parentShell, dlgType, showProgramControls, null, selectedSuite);
    }

    /**
     * 
     * @param parentShell
     *            Parent shell.
     * @param dlgType
     *            Dialog type.
     * @param showProgramControls
     *            Flag indicating if the program controls should be shown.
     * @param selectedProgram
     *            The selected program for this suite.
     * @param selectedSuite
     *            The selected suite.
     */
    public CreateEditSuiteDlg(Shell parentShell, DialogType dlgType,
            boolean showProgramControls, Program selectedProgram,
            Suite selectedSuite) {
        super(parentShell, SWT.DIALOG_TRIM | SWT.MIN | SWT.PRIMARY_MODAL,
                CAVE.DO_NOT_BLOCK | CAVE.MODE_INDEPENDENT);

        this.dialogType = dlgType;
        this.showProgramControls = showProgramControls;

        this.selectedProgram = selectedProgram;
        this.selectedSuite = selectedSuite;
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
        if (dialogType == DialogType.CREATE) {
            setText("Create New Suite");
        } else if (dialogType == DialogType.EDIT) {
            setText("Edit Suite");
        }

        retrieveProgramDataFromDB();
        retrieveAllMsgTypesFromDB();

        upDownImages = new UpDownImages(shell);

        createSuiteControls();

        if (showProgramControls) {
            createProgramControls();
        }

        createSelectedMsgTypeGroup();
        createAddRemoveButtons();
        createAvailMsgTypeGroup();
        createBottomButtons();

        tableMoveAction = new TableMoveAction(selectedMsgTypeTable);

        populateAssignedPrograms();
        populateMsgTypesInSuiteList();
        populateSelectedMsgTypesTable();
        populateAvailableMessageTypeTable();
        enableDisableAddRemoveBtns();
    }

    /**
     * Create the suite controls.
     */
    private void createSuiteControls() {
        Composite suiteComp = new Composite(shell, SWT.NONE);
        suiteComp.setLayout(new GridLayout(2, false));
        suiteComp
                .setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        Label nameLbl = new Label(suiteComp, SWT.NONE);
        nameLbl.setText("Suite Name: ");

        GridData gd = new GridData(250, SWT.DEFAULT);
        if (dialogType == DialogType.CREATE) {
            suiteNameTF = new Text(suiteComp, SWT.BORDER);
            suiteNameTF.setLayoutData(gd);
        } else if (dialogType == DialogType.EDIT) {
            suiteNameLbl = new Label(suiteComp, SWT.NONE);
            suiteNameLbl.setLayoutData(gd);
            if (selectedSuite != null) {
                suiteNameLbl.setText(selectedSuite.getName());
            }
        }

        Label categoryLbl = new Label(suiteComp, SWT.NONE);
        categoryLbl.setText("Suite Category: ");

        gd = new GridData(SWT.DEFAULT, SWT.DEFAULT, true, true);
        gd.minimumWidth = 175;
        categoryCbo = new Combo(suiteComp, SWT.VERTICAL | SWT.DROP_DOWN
                | SWT.BORDER | SWT.READ_ONLY);
        categoryCbo.setLayoutData(gd);
        populateCategoryCombo();
    }

    /**
     * Create program controls.
     */
    private void createProgramControls() {
        Composite progTransComp = new Composite(shell, SWT.NONE);
        GridLayout gl = new GridLayout(3, false);
        progTransComp.setLayout(gl);
        progTransComp.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true,
                false));

        /*
         * Program controls.
         */
        Label transmitterLbl = new Label(progTransComp, SWT.NONE);
        transmitterLbl.setText("Assigned Programs: ");

        GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, true);
        gd.minimumWidth = 350;
        programListLbl = new Label(progTransComp, SWT.BORDER);
        programListLbl.setLayoutData(gd);

        assignProgramBtn = new Button(progTransComp, SWT.PUSH);
        assignProgramBtn.setText("Assign to Program(s)...");
        assignProgramBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleChangeProgram();
            }
        });
    }

    /**
     * Create a group that will contain the table with the selected message
     * types.
     */
    private void createSelectedMsgTypeGroup() {
        Group msgReplaceGrp = new Group(shell, SWT.SHADOW_OUT);
        GridLayout gl = new GridLayout(1, false);
        msgReplaceGrp.setLayout(gl);
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        msgReplaceGrp.setLayoutData(gd);
        msgReplaceGrp.setText(" Selected Message Types: ");

        selectedMsgTypeTable = new MsgTypeTable(msgReplaceGrp, 550, 150);

        selectedMsgTypeTable.setCallbackAction(new ITableActionCB() {
            @Override
            public void tableSelectionChange(int selectionCount) {
                enableDisableAddRemoveBtns();
            }
        });

        Composite moveComp = new Composite(msgReplaceGrp, SWT.NONE);
        gl = new GridLayout(4, false);
        moveComp.setLayout(gl);
        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        moveComp.setLayoutData(gd);

        Label orderLabel = new Label(moveComp, SWT.NONE);
        orderLabel.setText("Change Order: ");

        int buttonWidth = 120;
        gd = new GridData(buttonWidth, SWT.DEFAULT);
        moveUpBtn = new Button(moveComp, SWT.PUSH);
        moveUpBtn.setImage(upDownImages.getImage(Arrows.UP_THIN));
        moveUpBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                tableMoveAction.moveUp();
            }
        });

        gd = new GridData(buttonWidth, SWT.DEFAULT);
        moveDownBtn = new Button(moveComp, SWT.PUSH);
        moveDownBtn.setImage(upDownImages.getImage(Arrows.DOWN_THIN));
        moveDownBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                tableMoveAction.moveDown();
            }
        });

        gd = new GridData(SWT.RIGHT, SWT.DEFAULT, true, false);
        setTriggersBtn = new Button(moveComp, SWT.PUSH);
        setTriggersBtn.setText(" Set Triggers... ");
        setTriggersBtn.setLayoutData(gd);
        setTriggersBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                displaySetTiggersDlg();
            }
        });
    }

    /**
     * Create the Add/Remove buttons that will add & remove message types from
     * the tables.
     */
    private void createAddRemoveButtons() {
        UpDownImages udi = new UpDownImages(shell);

        Composite btnComp = new Composite(shell, SWT.NONE);
        btnComp.setLayout(new GridLayout(2, false));
        btnComp.setLayoutData(new GridData(SWT.CENTER, SWT.DEFAULT, true, false));

        int buttonWidth = 90;
        GridData gd;

        gd = new GridData(buttonWidth, SWT.DEFAULT);
        addMsgTypesBtn = new Button(btnComp, SWT.PUSH);
        addMsgTypesBtn.setImage(udi.getImage(Arrows.UP_NO_TAIL));
        addMsgTypesBtn.setText("Add");
        addMsgTypesBtn.setToolTipText("Add selected message types");
        addMsgTypesBtn.setLayoutData(gd);
        addMsgTypesBtn.setEnabled(false);

        gd = new GridData(buttonWidth, SWT.DEFAULT);
        removeMsgTypesBtn = new Button(btnComp, SWT.PUSH);
        removeMsgTypesBtn.setImage(udi.getImage(Arrows.DOWN_NO_TAIL));
        removeMsgTypesBtn.setText("Remove");
        removeMsgTypesBtn.setToolTipText("Remove selected message types");
        removeMsgTypesBtn.setLayoutData(gd);
        removeMsgTypesBtn.setEnabled(false);
    }

    /**
     * Create a group that will contain the table with the available message
     * types.
     */
    private void createAvailMsgTypeGroup() {
        Group availMsgGrp = new Group(shell, SWT.SHADOW_OUT);
        GridLayout gl = new GridLayout(1, false);
        availMsgGrp.setLayout(gl);
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        availMsgGrp.setLayoutData(gd);
        availMsgGrp.setText(" Available Message Types: ");

        List<String> columnNames = new ArrayList<String>();
        columnNames.add("Message Type");
        columnNames.add("Message Title");
        availableMsgTypeTable = new MsgTypeTable(availMsgGrp, 550, 150);

        availableMsgTypeTable.setCallbackAction(new ITableActionCB() {
            @Override
            public void tableSelectionChange(int selectionCount) {
                if (selectionCount > 0) {
                    addMsgTypesBtn.setEnabled(true);
                } else {
                    addMsgTypesBtn.setEnabled(false);
                }
            }
        });
    }

    /**
     * Create the bottom Save and Cancel buttons.
     */
    private void createBottomButtons() {
        Composite buttonComp = new Composite(shell, SWT.NONE);
        buttonComp.setLayout(new GridLayout(3, false));
        buttonComp.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true,
                false));

        int buttonWidth = 70;

        GridData gd = new GridData(SWT.RIGHT, SWT.DEFAULT, true, false);
        gd.widthHint = buttonWidth;
        createSaveBtn = new Button(buttonComp, SWT.PUSH);
        if (dialogType == DialogType.CREATE) {
            createSaveBtn.setText("Create");
        } else if (dialogType == DialogType.EDIT) {
            createSaveBtn.setText("Save");
        }
        createSaveBtn.setLayoutData(gd);
        createSaveBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleCreateSaveAction();
            }
        });

        gd = new GridData(SWT.LEFT, SWT.DEFAULT, true, false);
        gd.widthHint = buttonWidth;
        Button cancelBtn = new Button(buttonComp, SWT.PUSH);
        cancelBtn.setText("Cancel");
        cancelBtn.setLayoutData(gd);
        cancelBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                close();
            }
        });
    }

    /**
     * Enable/Disable the Add and Remove buttons.
     */
    private void enableDisableAddRemoveBtns() {
        addMsgTypesBtn.setEnabled(availableMsgTypeTable.hasSelectedItems());
        removeMsgTypesBtn.setEnabled(selectedMsgTypeTable.hasSelectedItems());
    }

    /**
     * Display the set triggers dialog.
     */
    private void displaySetTiggersDlg() {
        CheckListData cld = new CheckListData();
        boolean checked = true;
        for (int i = 0; i < 30; i++) {
            checked = true;
            if (i % 2 == 0) {
                checked = false;
            }
            cld.addDataItem("MessageType:" + i, checked);
        }

        // TODO : need to add functionality
        CheckScrollListDlg checkListDlg = new CheckScrollListDlg(shell,
                "Trigger Selection", "Select Message Type to Trigger:", cld,
                true);
        checkListDlg.setCloseCallback(new ICloseCallback() {
            @Override
            public void dialogClosed(Object returnValue) {
                if (returnValue != null && returnValue instanceof CheckListData) {
                    CheckListData listData = (CheckListData) returnValue;
                    Map<String, Boolean> dataMap = listData.getDataMap();
                    for (String str : dataMap.keySet()) {
                        System.out.println("Type = " + str + "\t Selected: "
                                + dataMap.get(str));
                    }
                }
            }
        });
        checkListDlg.open();
    }

    /**
     * Handle the create/save action.
     */
    private void handleCreateSaveAction() {
        /*
         * TODO: Need to determine if save to the database will be done here or
         * in the parent dialog.
         */

        close();
    }

    /**
     * Populate the category combo control.
     */
    private void populateCategoryCombo() {
        for (SuiteType suiteType : SuiteType.values()) {
            if (suiteType != SuiteType.INTERRUPT) {
                categoryCbo.add(suiteType.name());
            }
        }

        if (selectedSuite != null) {
            int index = categoryCbo.indexOf(selectedSuite.getType().name());
            categoryCbo.select(index);
        } else {
            categoryCbo.select(0);
        }
    }

    /**
     * Populate the assign programs.
     */
    private void populateAssignedPrograms() {

        if (!showProgramControls) {
            return;
        }

        if (selectedProgram == null && selectedSuite != null) {
            for (Program p : programsArray) {
                List<Suite> suitesInProgram = p.getSuites();
                for (Suite s : suitesInProgram) {
                    if (s.getId() == selectedSuite.getId()) {
                        assignedPrograms.add(p);
                    }
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        for (Program p : assignedPrograms) {
            sb.append(p.getName()).append(" ");
        }

        if (sb.length() > 0) {
            programListLbl.setText(sb.toString());
            programListLbl.setToolTipText(sb.toString());
        }
    }

    /**
     * Retrieve the data from the database.
     */
    private void retrieveProgramDataFromDB() {
        ProgramRequest pr = new ProgramRequest();
        pr.setAction(ProgramAction.ProgramSuites);
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
     * Retrieve the data from the database.
     */
    private void retrieveAllMsgTypesFromDB() {
        MessageTypeRequest mtRequest = new MessageTypeRequest();
        mtRequest.setAction(MessageTypeAction.AllMessageTypes);
        MessageTypeResponse mtResponse = null;

        try {
            mtResponse = (MessageTypeResponse) BmhUtils.sendRequest(mtRequest);
            allMsgTypesList = mtResponse.getMessageTypeList();
        } catch (Exception e) {
            statusHandler
                    .error("Error retrieving message type data from the database: ",
                            e);
        }
    }

    /**
     * Get the message types from the selected suite and put them in a list.
     */
    private void populateMsgTypesInSuiteList() {
        if (selectedSuite != null) {
            List<SuiteMessage> suiteMessageArray = selectedSuite
                    .getSuiteMessages();
            Map<Integer, SuiteMessage> suiteMsgMap = new TreeMap<Integer, SuiteMessage>();
            for (SuiteMessage sm : suiteMessageArray) {
                suiteMsgMap.put(sm.getPosition(), sm);
            }

            msgTypesInSuiteList.addAll(suiteMsgMap.values());
        }
    }

    /**
     * Populate the selected message types table. These are the message type
     * associated with the suite.
     */
    private void populateSelectedMsgTypesTable() {
        if (selectedMsgTypeTable.hasTableData() == false) {
            List<TableColumnData> columnNames = new ArrayList<TableColumnData>();
            TableColumnData tcd = new TableColumnData("Message Type", 150);
            columnNames.add(tcd);
            tcd = new TableColumnData("Message Title");
            columnNames.add(tcd);
            tcd = new TableColumnData("Trigger");
            columnNames.add(tcd);
            selectedMsgTypeTableData = new TableData(columnNames);
        } else {
            selectedMsgTypeTableData.deleteAllRows();
        }

        populateSelectedMessageTypeTableData();
        selectedMsgTypeTable.populateTable(selectedMsgTypeTableData);

        if (selectedMsgTypeTable.getItemCount() > 0) {
            selectedMsgTypeTable.select(0);
        }
    }

    /**
     * Populate the selected message type data. This will be used to populate
     * the table.
     */
    private void populateSelectedMessageTypeTableData() {
        for (SuiteMessage sm : msgTypesInSuiteList) {
            TableRowData trd = new TableRowData();

            trd.addTableCellData(new TableCellData(sm.getMsgType().getAfosid()));
            trd.addTableCellData(new TableCellData(sm.getMsgType().getTitle()));
            trd.addTableCellData(new TableCellData(sm.isTrigger() ? "Yes"
                    : "No"));

            selectedMsgTypeTableData.addDataRow(trd);
        }
    }

    /**
     * Populate the Message Type table.
     */
    private void populateAvailableMessageTypeTable() {
        if (availableMsgTypeTable.hasTableData() == false) {
            List<TableColumnData> columnNames = new ArrayList<TableColumnData>();
            TableColumnData tcd = new TableColumnData("Message Type", 150);
            columnNames.add(tcd);
            tcd = new TableColumnData("Message Title");
            columnNames.add(tcd);
            availMsgTypeTableData = new TableData(columnNames);
        } else {
            availMsgTypeTableData.deleteAllRows();
        }

        populateAvailableMessageTypeTableData();
        availableMsgTypeTable.populateTable(availMsgTypeTableData);
    }

    /**
     * Populate the Message Type table data.
     */
    private void populateAvailableMessageTypeTableData() {
        for (MessageType mt : allMsgTypesList) {
            TableRowData trd = new TableRowData();
            trd.addTableCellData(new TableCellData(mt.getAfosid()));
            trd.addTableCellData(new TableCellData(mt.getTitle()));
            availMsgTypeTableData.addDataRow(trd);
        }
    }

    /**********************************************************************
     * 
     * TODO: remove dummy code
     * 
     */

    private void handleChangeProgram() {
        // TODO : Add real code when hooking up
        CheckListData cld = new CheckListData();

        cld.addDataItem("Program - 1", true);
        cld.addDataItem("Program - 2", true);
        cld.addDataItem("Program - 3", true);
        cld.addDataItem("Program - 4", false);
        cld.addDataItem("Program - 5", false);
        cld.addDataItem("Program - 6", false);

        CheckScrollListDlg checkListDlg = new CheckScrollListDlg(shell,
                "Assign Programs", "Add Suite to Selected Programs:", cld, true);
        checkListDlg.setCloseCallback(new ICloseCallback() {
            @Override
            public void dialogClosed(Object returnValue) {
                if (returnValue != null && returnValue instanceof CheckListData) {
                    CheckListData listData = (CheckListData) returnValue;
                    Map<String, Boolean> dataMap = listData.getDataMap();
                    for (String str : dataMap.keySet()) {
                        System.out.println("Type = " + str + "\t Selected: "
                                + dataMap.get(str));
                    }
                }
            }
        });
        checkListDlg.open();
    }
}
