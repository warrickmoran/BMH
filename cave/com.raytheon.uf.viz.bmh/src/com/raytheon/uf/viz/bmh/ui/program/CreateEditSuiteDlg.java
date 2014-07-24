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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.raytheon.uf.viz.bmh.ui.common.table.ITableActionCB;
import com.raytheon.uf.viz.bmh.ui.common.table.TableCellData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableColumnData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableData;
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
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */
public class CreateEditSuiteDlg extends CaveSWTDialog {

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

    /**
     * Constructor.
     * 
     * @param parentShell
     *            Parent shell.
     * @param dlgType
     *            Dialog type.
     */
    public CreateEditSuiteDlg(Shell parentShell, DialogType dlgType,
            boolean showProgramControls) {
        super(parentShell, SWT.DIALOG_TRIM | SWT.MIN | SWT.PRIMARY_MODAL,
                CAVE.DO_NOT_BLOCK | CAVE.MODE_INDEPENDENT);

        this.dialogType = dlgType;
        this.showProgramControls = showProgramControls;

        // TODO - need to pass in the program if used with the Broadcast program
        // dialog.
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

        upDownImages = new UpDownImages(shell);

        createSuiteControls();

        if (showProgramControls) {
            createProgramControls();
        }

        createSelectedMsgTypeGroup();
        createAddRemoveButtons();
        createAvailMsgTypeGroup();
        createBottomButtons();
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
            suiteNameLbl.setText("LincolnSuite");
        }

        Label categoryLbl = new Label(suiteComp, SWT.NONE);
        categoryLbl.setText("Suite Category: ");

        gd = new GridData(SWT.DEFAULT, SWT.DEFAULT, true, true);
        gd.minimumWidth = 175;
        categoryCbo = new Combo(suiteComp, SWT.VERTICAL | SWT.DROP_DOWN
                | SWT.BORDER | SWT.READ_ONLY);
        categoryCbo.setLayoutData(gd);
        populateCategoryCombo();
        categoryCbo.select(0);
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
        populatePrograms();

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

        populateMsgTypeSelectedTable();

        selectedMsgTypeTable.setCallbackAction(new ITableActionCB() {
            @Override
            public void tableSelectionChange(int selectionCount) {
                if (selectionCount > 0) {
                    removeMsgTypesBtn.setEnabled(true);
                } else {
                    removeMsgTypesBtn.setEnabled(false);
                }
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
            }
        });

        gd = new GridData(buttonWidth, SWT.DEFAULT);
        moveDownBtn = new Button(moveComp, SWT.PUSH);
        moveDownBtn.setImage(upDownImages.getImage(Arrows.DOWN_THIN));
        moveDownBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
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

        int buttonWidth = 80;
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

        populateMsgTypeAvailTable();

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

    /*
     * TODO: LIST OF ITEMS TO DO WHEN HOOKING UP...
     * 
     * Need to verify the suite name doesn't already exist.
     */

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
                "Assign Programs", "Select Programs to add to Suite:", cld,
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

    private void populateCategoryCombo() {
        categoryCbo.add("General");
        categoryCbo.add("High");
        categoryCbo.add("Exclusive");
    }

    private void populatePrograms() {
        programListLbl.setText(" Program1, Program2, Program 3");
        programListLbl.setToolTipText("Program1, Program2, Program 3");
    }

    private void populateMsgTypeSelectedTable() {
        List<TableColumnData> columnNames = new ArrayList<TableColumnData>();
        TableColumnData tcd = new TableColumnData("Message Type", 150);
        columnNames.add(tcd);
        tcd = new TableColumnData("Message Title", 300);
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
        selectedMsgTypeTable.populateTable(td);

    }

    private void populateMsgTypeAvailTable() {
        List<TableColumnData> columnNames = new ArrayList<TableColumnData>();
        TableColumnData tcd = new TableColumnData("Message Type", 150);
        columnNames.add(tcd);
        tcd = new TableColumnData("Message Title");
        columnNames.add(tcd);
        TableData td = new TableData(columnNames);

        TableRowData trd = new TableRowData();

        trd.addTableCellData(new TableCellData("MessageType - 3"));
        trd.addTableCellData(new TableCellData("MessageType - 3 - Description"));

        td.addDataRow(trd);

        trd = new TableRowData();

        trd.addTableCellData(new TableCellData("MessageType - 4"));
        trd.addTableCellData(new TableCellData("MessageType - 4 - Description"));

        td.addDataRow(trd);

        availableMsgTypeTable.populateTable(td);
    }
}
