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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

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
import com.raytheon.uf.common.bmh.datamodel.msg.MessageTypeSummary;
import com.raytheon.uf.common.bmh.datamodel.msg.Program;
import com.raytheon.uf.common.bmh.datamodel.msg.Suite;
import com.raytheon.uf.common.bmh.datamodel.msg.Suite.SuiteType;
import com.raytheon.uf.common.bmh.datamodel.msg.SuiteMessage;
import com.raytheon.uf.common.bmh.datamodel.msg.SuiteMessagePk;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.request.SuiteResponse;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.viz.bmh.data.BmhUtils;
import com.raytheon.uf.viz.bmh.ui.common.table.ITableActionCB;
import com.raytheon.uf.viz.bmh.ui.common.table.TableCellData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableColumnData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableMoveAction;
import com.raytheon.uf.viz.bmh.ui.common.table.TableRowData;
import com.raytheon.uf.viz.bmh.ui.common.utility.CheckListData;
import com.raytheon.uf.viz.bmh.ui.common.utility.CheckScrollListDlg;
import com.raytheon.uf.viz.bmh.ui.common.utility.DialogUtility;
import com.raytheon.uf.viz.bmh.ui.common.utility.UpDownImages;
import com.raytheon.uf.viz.bmh.ui.common.utility.UpDownImages.Arrows;
import com.raytheon.uf.viz.bmh.ui.dialogs.msgtypes.MessageTypeDataManager;
import com.raytheon.uf.viz.bmh.ui.dialogs.msgtypes.MsgTypeAfosComparator;
import com.raytheon.uf.viz.bmh.ui.dialogs.msgtypes.MsgTypeTable;
import com.raytheon.uf.viz.bmh.ui.dialogs.suites.SuiteDataManager;
import com.raytheon.uf.viz.bmh.ui.dialogs.suites.SuiteNameValidator;
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
 * Jul 20, 2014  #3174     lvenable    Initial creation
 * Jul 24, 2014  #3433     lvenable    Updated for Suite manager
 * Aug 01, 2014  #3479     lvenable    Added additional capability.
 * Aug 12, 2014  #3490     lvenable    Updated to use data from the database.
 * Aug 15, 2014  #3490     lvenable    Sort the list of message types, use data manager.
 * Aug 18, 2014  #3490     lvenable    Added save, create, add, remove, sort capabilities.
 * Aug 21, 2014  #3490     lvenable    Updated suite capabilities.
 * Aug 22, 2014  #3490     lvenable    Updated controls that disable/enable.
 * Aug 24, 2014  #3490     lvenable    Added assign programs capability and check for triggers on general type.
 * Aug 25, 2014  #3490     lvenable    Added validation code.
 * Aug 26, 2014  #3490     lvenable    Fixed issue found during testing, fixed issue on create in a new suite
 *                                      when creating a new program.
 * Sep 16, 2014  #3587     bkowal      Updated to only allow trigger assignment for {Program, Suite}
 * Oct 08, 2014  #3687     bsteffen    Remove ProgramTrigger.
 * Oct 08, 2014  #3479     lvenable    Changed MODE_INDEPENDENT to PERSPECTIVE_INDEPENDENT.
 * Oct 13, 2014  3654      rjpeter     Updated to use MessageTypeSummary.
 * Oct 15, 2014  3715      bkowal      Create temporary data objects that can be used when
 *                                     a new {@link Suite} is created.
 * Oct 21, 2014  3715      bkowal      Verify that there is an associated program before 
 *                                     accessing it.
 * Oct 26, 2014   3750     mpduff      Null check the suite messages.
 * Oct 28, 2014   3750     bkowal      Add Suite to the SuiteMessage relation when
 *                                     adding Message Types to a Suite.
 * Nov 12, 2014   3815     lvenable    Fixed category not being saved.
 * Nov 13, 2014  3698      rferrel     Checks to allow only one GENERAL type suite per program.
 * Nov 20, 2014   3830     rferrel     Remove assignProgramBtn button and supporting methods.
 * Dec 01, 2014   3838     rferrel     Change for new BmhUtil.getProgramGeneralSuite.
 * Feb 19, 2015   4151     bkowal      Removed the assigned programs field for new suites. Removed
 *                                     the "Set Triggers..." button when creating/editing a suite
 *                                     without an associated program.
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

    /** List of message types in the suite. */
    private final List<SuiteMessage> msgTypesInSuiteList = new ArrayList<SuiteMessage>();

    /**
     * List of suite messages corresponding to a message type that is associated
     * with a trigger that have been removed from the suite.
     **/
    private final List<SuiteMessagePk> triggerSuiteMsgsRemoved = new ArrayList<SuiteMessagePk>();

    private final Set<String> msgTypeNames = new HashSet<String>();

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

    /** Array of Programs to save the create suite to. */
    private final List<Program> newAssignedProgramsArray = new ArrayList<Program>();

    /** List of assigned program for this suite. */
    private final List<Program> assignedPrograms = new ArrayList<Program>();

    /** Existing suite names. */
    private Set<String> existingSuiteNames = null;

    /** Assign program names to the current suite. */
    private final Set<String> assignedProgramNames = new TreeSet<String>();

    private boolean forNewProgram = false;

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
            Suite selectedSuite, Set<String> existingSuiteNames,
            boolean forNewProgram) {
        super(parentShell, SWT.DIALOG_TRIM | SWT.MIN | SWT.PRIMARY_MODAL,
                CAVE.DO_NOT_BLOCK | CAVE.PERSPECTIVE_INDEPENDENT);

        this.dialogType = dlgType;
        this.showProgramControls = showProgramControls;
        this.selectedProgram = selectedProgram;
        this.selectedSuite = selectedSuite;
        this.existingSuiteNames = existingSuiteNames;
        this.forNewProgram = forNewProgram;
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
        if ((dialogType == DialogType.CREATE) && (this.selectedProgram != null)) {
            selectedProgram.cancelNewSuite(selectedSuite);
        }
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

        populateAssignedProgramsLabel();
        populateMsgTypesInSuiteList();
        populateSelectedMsgTypesTable(false);
        populateAvailableMessageTypeTable();
        enableDisableAddRemoveTriggerBtns();
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
        categoryCbo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                enableDisableAddRemoveTriggerBtns();
            }
        });
        populateCategoryCombo();
    }

    /**
     * Create program controls.
     */
    private void createProgramControls() {
        Composite progTransComp = new Composite(shell, SWT.NONE);
        GridLayout gl = new GridLayout(2, false);
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

        if (this.dialogType == DialogType.CREATE) {
            /*
             * do not display the assigned programs field when a new suite is
             * being created.
             */
            transmitterLbl.setVisible(false);
            this.programListLbl.setVisible(false);
        }
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
                enableDisableAddRemoveTriggerBtns();
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
                int[] indices = tableMoveAction.moveUp(msgTypesInSuiteList);

                populateSelectedMsgTypesTable(true);
                selectedMsgTypeTable.deselectAll();
                if (indices != null) {
                    selectedMsgTypeTable.selectRows(indices);
                }
                selectedMsgTypeTable.showSelection();
            }
        });

        gd = new GridData(buttonWidth, SWT.DEFAULT);
        moveDownBtn = new Button(moveComp, SWT.PUSH);
        moveDownBtn.setImage(upDownImages.getImage(Arrows.DOWN_THIN));
        moveDownBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int[] indices = tableMoveAction.moveDown(msgTypesInSuiteList);

                populateSelectedMsgTypesTable(true);
                selectedMsgTypeTable.deselectAll();
                if (indices != null) {
                    selectedMsgTypeTable.selectRows(indices);
                }
                selectedMsgTypeTable.showSelection();
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
        addMsgTypesBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleAddMessageTypes();
            }
        });

        gd = new GridData(buttonWidth, SWT.DEFAULT);
        removeMsgTypesBtn = new Button(btnComp, SWT.PUSH);
        removeMsgTypesBtn.setImage(udi.getImage(Arrows.DOWN_NO_TAIL));
        removeMsgTypesBtn.setText("Remove");
        removeMsgTypesBtn.setToolTipText("Remove selected message types");
        removeMsgTypesBtn.setLayoutData(gd);
        removeMsgTypesBtn.setEnabled(false);
        removeMsgTypesBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleRemoveMessageTypes();
            }
        });
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
        cancelBtn.setText("Cancel");
        cancelBtn.setLayoutData(gd);
        cancelBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setReturnValue(new Boolean(false));
                close();
            }
        });
    }

    /**
     * Enable/Disable the Add and Remove buttons.
     */
    private void enableDisableAddRemoveTriggerBtns() {
        addMsgTypesBtn.setEnabled(availableMsgTypeTable.hasSelectedItems());
        removeMsgTypesBtn.setEnabled(selectedMsgTypeTable.hasSelectedItems());

        // If the suite type is GENERAL then the trigger button is disable else
        // the enabled feature is determined if there are selected message
        // types.
        if ((getSelectedSuiteType() == SuiteType.GENERAL)
                || (this.selectedProgram == null)) {
            setTriggersBtn.setEnabled(false);
            /*
             * Additionally do not even display the "Set Triggers..." button if
             * there is not an associated program.
             */
            if (this.selectedProgram == null) {
                this.setTriggersBtn.setVisible(false);
            }
        } else {
            setTriggersBtn
                    .setEnabled(selectedMsgTypeTable.getTableItemCount() > 0);
        }
    }

    /**
     * Display the set triggers dialog.
     */
    private void displaySetTiggersDlg() {
        CheckListData cld = new CheckListData();

        for (SuiteMessage sm : msgTypesInSuiteList) {
            cld.addDataItem(sm.getAfosid(), this.selectedProgram
                    .isTriggerMsgType(this.selectedSuite,
                            sm.getMsgTypeSummary()));
        }

        CheckScrollListDlg checkListDlg = new CheckScrollListDlg(shell,
                "Trigger Selection", "Select Message Type to Trigger:", cld,
                true);
        checkListDlg.setCloseCallback(new ICloseCallback() {
            @Override
            public void dialogClosed(Object returnValue) {
                if ((returnValue != null)
                        && (returnValue instanceof CheckListData)) {
                    CheckListData listData = (CheckListData) returnValue;
                    Map<String, Boolean> dataMap = listData.getDataMap();

                    selectedProgram.clearTriggerMsgTypes(selectedSuite);

                    for (SuiteMessage sm : msgTypesInSuiteList) {
                        if (dataMap.containsKey(sm.getAfosid())) {
                            if (dataMap.get(sm.getAfosid()) == false) {
                                continue;
                            }
                            selectedProgram.addTriggerMsgType(selectedSuite,
                                    sm.getMsgTypeSummary());
                        }
                    }

                    populateSelectedMsgTypesTable(true);
                }
            }

        });
        checkListDlg.open();
    }

    /**
     * Get the selected suite type.
     * 
     * @return The selected suite type.
     */
    private SuiteType getSelectedSuiteType() {
        String categoryName = categoryCbo.getItem(categoryCbo
                .getSelectionIndex());
        SuiteType suiteType = SuiteType.valueOf(categoryName);

        return suiteType;
    }

    /**
     * Validate the trigger messages for the general type. If the user approves
     * then the trigger will be removed from the message types.
     * 
     * @return
     */
    private boolean validateTriggerMessagesGeneralType() {
        if ((getSelectedSuiteType() == SuiteType.GENERAL)
                && hasTriggerMessages()) {

            String msg = "You have trigger message(s) for the GENERAL suite type.  Select OK "
                    + "to remove the triggers and continue saving or Cancel to edit the suite.";
            int result = DialogUtility.showMessageBox(shell, SWT.ICON_WARNING
                    | SWT.OK | SWT.CANCEL, "Triggers Selected", msg.toString());

            if (result == SWT.CANCEL) {
                return false;
            }

            this.selectedProgram.clearTriggerMsgTypes(this.selectedSuite);
        }

        return true;
    }

    /**
     * Check to to not allow suitType GENERAL when programs for the selected
     * suite already contain a GENERAL suite.
     * 
     * @param suiteType
     *            - purposed new Suite Type
     * @return true when suiteType is valid for all programs using the suite.
     */
    private boolean verifySuiteType(SuiteType suiteType) {
        try {
            String errorMsg = validateSuiteTypeChange(selectedSuite, suiteType);
            if (errorMsg != null) {
                DialogUtility.showMessageBox(shell, SWT.ICON_WARNING | SWT.OK,
                        "Suite Save", errorMsg);
                return false;
            }
        } catch (Exception e) {
            statusHandler.handle(Priority.ERROR,
                    "Error retrieving program data from the database: ", e);
            return false;
        }
        return true;
    }

    /**
     * Validate that Suite's type can be changed.
     * 
     * @param suite
     * @param suiteType
     *            - Purposed new type.
     * @return errorMessage - null if no problem making the change else reason
     *         change cannot be performed.
     */
    private static String validateSuiteTypeChange(Suite suite,
            SuiteType suiteType) throws Exception {
        SuiteType oldType = suite.getType();

        // No change skip checks.
        if (oldType == suiteType) {
            return null;
        }

        /*
         * Verify any programs using this suite must not be used by an enabled
         * transmitter.
         */
        if (oldType == SuiteType.GENERAL) {
            List<TransmitterGroup> enabledGroups = BmhUtils
                    .getSuiteEnabledTransmitterGroups(suite);
            if (!enabledGroups.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                sb.append("Unable to change suite's category. The following ENABLED transmitter/groups(s) dependent on this suite being GENERAL:");
                for (TransmitterGroup group : enabledGroups) {
                    sb.append("\n\t").append(group.getName());
                }
                return sb.toString();
            }
        }

        /*
         * Verify any programs using this suite do not already contain a GENERAL
         * suite.
         */
        if (suiteType == SuiteType.GENERAL) {
            List<Program> suitePrograms = BmhUtils.getSuitePrograms(suite);
            List<Program> haveGeneralSuite = new ArrayList<>();
            for (Program program : suitePrograms) {
                if (BmhUtils.containsGeneralSuite(program)) {
                    haveGeneralSuite.add(program);
                }
            }
            if (!haveGeneralSuite.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                sb.append("Unable to change suite's category. The following program(s) using this suite already contain a suite with a GENERAL category:");
                for (Program program : haveGeneralSuite) {
                    sb.append("\n\t").append(program.getName());
                }
                return sb.toString();
            }
        }

        return null;
    }

    /**
     * Handle the create/save action.
     */
    private void handleSaveAction() {

        SuiteType suiteType = getSelectedSuiteType();

        if (!verifySuiteType(suiteType)) {
            categoryCbo.setText(selectedSuite.getType().name());
            return;
        }

        if (!validMessageTypes(suiteType)) {
            return;
        }

        if (validateTriggerMessagesGeneralType() == false) {
            return;
        }

        SuiteDataManager sdm = new SuiteDataManager();

        if (this.triggerSuiteMsgsRemoved.isEmpty() == false) {
            this.selectedSuite
                    .setRemovedTriggerSuiteMessages(this.triggerSuiteMsgsRemoved);
        }

        Suite savedSuite = null;
        try {
            selectedSuite.setSuiteMessages(msgTypesInSuiteList);
            selectedSuite.setType(suiteType);
            SuiteResponse suiteReponse = sdm.saveSuite(selectedSuite);
            if (suiteReponse.getSuiteList().isEmpty()) {
                return;
            }
            savedSuite = suiteReponse.getSuiteList().get(0);
        } catch (Exception e) {
            statusHandler.error(
                    "Error saving the suite: " + selectedSuite.getName(), e);
            return;
        }

        setReturnValue(savedSuite);
        close();
    }

    /**
     * Handle the create/save action.
     * 
     * Since we are creating a suite from scratch, we need to save the suite and
     * then save the suite to the selected programs.
     */
    private void handleCreateAction() {
        this.createNewSuite();

        SuiteNameValidator snv = new SuiteNameValidator(existingSuiteNames);

        if (!snv.validateInputText(shell, this.selectedSuite.getName())) {
            return;
        }

        if (validateTriggerMessagesGeneralType() == false) {
            return;
        }

        if (!validMessageTypes(this.selectedSuite.getType())) {
            return;
        }
        List<SuiteMessage> suiteMsgs = selectedSuite.getSuiteMessages();
        List<MessageTypeSummary> newTriggerMsgTypes = new ArrayList<>();
        if (suiteMsgs != null && this.selectedProgram != null) {
            for (SuiteMessage suiteMsg : suiteMsgs) {
                if (this.selectedProgram.isTriggerMsgType(this.selectedSuite,
                        suiteMsg.getMsgTypeSummary())) {
                    newTriggerMsgTypes.add(suiteMsg.getMsgTypeSummary());
                }
            }
        }

        try {
            if ((selectedProgram != null)
                    && (getSelectedSuiteType() == SuiteType.GENERAL)) {
                Suite oldGeneralSuite = BmhUtils
                        .getProgramGeneralSuite(selectedProgram);
                if (oldGeneralSuite != null) {
                    int result = DialogUtility
                            .showMessageBox(
                                    shell,
                                    SWT.ICON_WARNING | SWT.OK | SWT.CANCEL,
                                    "Create Suite",
                                    "The \""
                                            + selectedProgram.getName()
                                            + "\" program already contains a GENERAL category suite: "
                                            + oldGeneralSuite.getName()
                                            + ".\n\nSelect OK to replace with new suite: "
                                            + suiteNameTF.getText().trim()
                                            + ".");
                    if (result != SWT.OK) {
                        return;
                    }
                    selectedProgram.removeSuite(oldGeneralSuite);
                }
            }
        } catch (Exception e1) {
            statusHandler
                    .handle(Priority.PROBLEM, e1.getLocalizedMessage(), e1);
        }

        ProgramDataManager pdm = new ProgramDataManager();
        SuiteDataManager sdm = new SuiteDataManager();

        Suite savedSuite = null;
        try {
            SuiteResponse suiteReponse = sdm.saveSuite(this.selectedSuite);
            if (suiteReponse.getSuiteList().isEmpty()) {
                return;
            }
            savedSuite = suiteReponse.getSuiteList().get(0);
        } catch (Exception e) {
            statusHandler.error("Error creating the suite: "
                    + this.selectedSuite.getName(), e);
            return;
        }

        if (newTriggerMsgTypes.isEmpty() == false) {
            savedSuite.setNewTriggerSuiteMessages(newTriggerMsgTypes);
        }

        /*
         * Add the suites to the programs and then save the programs.
         */
        for (Program p : newAssignedProgramsArray) {
            p.addSuite(savedSuite);

            try {
                pdm.saveProgram(p);
            } catch (Exception e) {
                statusHandler.error(
                        "Error saving suite " + selectedSuite.getName()
                                + " to program " + p.getName() + ": ", e);
            }
        }

        setReturnValue(savedSuite);
        close();
    }

    private void createNewSuite() {
        if (this.selectedSuite == null) {
            this.selectedSuite = new Suite();
        }

        this.selectedSuite.setName(suiteNameTF.getText().trim());
        this.selectedSuite.setType(getSelectedSuiteType());

        if ((this.msgTypesInSuiteList != null)
                && (this.msgTypesInSuiteList.isEmpty() == false)) {
            this.selectedSuite.setSuiteMessages(this.msgTypesInSuiteList);
        }
    }

    /**
     * Add message types to the selected message type table.
     */
    private void handleAddMessageTypes() {
        if (this.dialogType == DialogType.CREATE) {
            this.createNewSuite();
        }

        int[] indices = availableMsgTypeTable.getSelectedIndices();

        if (indices.length == 0) {
            return;
        }

        int selectedMsgTypeIndex = selectedMsgTypeTable.getSelectedIndex();

        if (selectedMsgTypeIndex == -1) {
            selectedMsgTypeIndex = 0;
        }

        for (int i = indices.length - 1; i >= 0; --i) {
            MessageType mt = allMsgTypesList.get(indices[i]);

            if (msgTypeNames.contains(mt.getAfosid())) {
                continue;
            }

            SuiteMessage newSuiteMessage = new SuiteMessage();
            newSuiteMessage.setSuite(this.selectedSuite);
            newSuiteMessage.setMsgTypeSummary(mt.getSummary());

            msgTypesInSuiteList.add(selectedMsgTypeIndex, newSuiteMessage);
            msgTypeNames.add(mt.getAfosid());
        }

        populateSelectedMsgTypesTable(true);
        enableDisableAddRemoveTriggerBtns();
    }

    /**
     * Remove selected message types from the selected message types table.
     */
    private void handleRemoveMessageTypes() {
        if (this.dialogType == DialogType.CREATE) {
            this.createNewSuite();
        }

        int[] indices = selectedMsgTypeTable.getSelectedIndices();

        if (indices.length == 0) {
            return;
        }

        ProgramDataManager pdm = new ProgramDataManager();
        for (int i = indices.length - 1; i >= 0; --i) {
            SuiteMessage removalCandidate = msgTypesInSuiteList.get(indices[i]);
            List<Program> triggeredPrograms;

            try {
                triggeredPrograms = pdm.getProgramsWithTrigger(
                        removalCandidate.getSuite(),
                        removalCandidate.getMsgTypeSummary());
            } catch (Exception e) {
                statusHandler.error(
                        "Error checking for programs with trigger: '"
                                + removalCandidate.getMsgTypeSummary()
                                        .getAfosid() + "'!", e);
                continue;
            }

            if ((triggeredPrograms != null)
                    && (triggeredPrograms.isEmpty() == false)) {
                StringBuilder stringBuilder = new StringBuilder("Message Type ");
                stringBuilder.append(removalCandidate.getMsgTypeSummary()
                        .getAfosid());
                stringBuilder
                        .append(" is currently a Trigger for the following programs:\n\n");
                for (Program triggeredProgram : triggeredPrograms) {
                    stringBuilder.append(triggeredProgram.getName());
                    stringBuilder.append("\n");
                }
                stringBuilder
                        .append("\nAre you sure you want to remove the message type from Suite ");
                stringBuilder.append(removalCandidate.getSuite().getName());
                stringBuilder.append("?");

                int result = DialogUtility.showMessageBox(shell,
                        SWT.ICON_WARNING | SWT.YES | SWT.NO,
                        "Remove Message Type", stringBuilder.toString());
                if (result == SWT.NO) {
                    /*
                     * Do not remove anything.
                     */
                    continue;
                }

                /*
                 * Save the message type associated with the triggers until the
                 * user decides to save the changes.
                 */
                this.triggerSuiteMsgsRemoved.add(removalCandidate.getId());
            }

            msgTypeNames
                    .remove(msgTypesInSuiteList.get(indices[i]).getAfosid());
            msgTypesInSuiteList.remove(indices[i]);
            if (this.selectedProgram != null) {
                this.selectedProgram.removeTriggerMsgType(this.selectedSuite,
                        removalCandidate.getMsgTypeSummary());
            }
        }

        populateSelectedMsgTypesTable(true);
        enableDisableAddRemoveTriggerBtns();
    }

    /**
     * Convenience method to determine if there are any trigger messages in the
     * selected message types.
     * 
     * @return True if there are trigger message in the selected message types
     *         table, false otherwise.
     */
    private boolean hasTriggerMessages() {
        if (this.selectedProgram == null) {
            return false;
        }
        return this.selectedProgram.doTriggersExist(this.selectedSuite);
    }

    /**
     * Validate the there are message types and validate if a trigger needs to
     * be applied.
     * 
     * @param suiteType
     *            Suite type.
     * @return True if valid, false if not valid.
     */
    private boolean validMessageTypes(SuiteType suiteType) {
        if (msgTypesInSuiteList.isEmpty()) {
            StringBuilder sb = new StringBuilder();

            sb.append("The Suite name must contain message types.");

            DialogUtility.showMessageBox(shell, SWT.ICON_WARNING | SWT.OK,
                    "No Message Types", sb.toString());

            return false;
        }

        return true;
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
     * Populate the assigned programs label.
     */
    private void populateAssignedProgramsLabel() {
        if (!showProgramControls) {
            return;
        }

        if (selectedSuite != null) {
            for (Program p : programsArray) {
                List<Suite> suitesInProgram = p.getSuites();
                for (Suite s : suitesInProgram) {
                    if (s.getId() == selectedSuite.getId()) {
                        assignedPrograms.add(p);
                    }
                }
            }
        }

        assignedProgramNames.clear();
        if (selectedProgram == null) {
            for (Program p : assignedPrograms) {
                assignedProgramNames.add(p.getName());
            }
        }

        for (Program p : newAssignedProgramsArray) {
            // assignedPrograms.add(p);
            assignedProgramNames.add(p.getName());
        }

        StringBuilder sb = new StringBuilder();
        for (String name : assignedProgramNames) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(name);
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

        ProgramDataManager pdm = new ProgramDataManager();

        try {
            programsArray = pdm.getProgramSuites();
        } catch (Exception e) {
            statusHandler.error(
                    "Error retrieving program data from the database: ", e);
        }
    }

    /**
     * Retrieve the message type data from the database.
     */
    private void retrieveAllMsgTypesFromDB() {

        MessageTypeDataManager msgTypeDataMgr = new MessageTypeDataManager();

        try {
            allMsgTypesList = msgTypeDataMgr
                    .getMessageTypes(new MsgTypeAfosComparator());
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

            for (SuiteMessage sm : msgTypesInSuiteList) {
                msgTypeNames.add(sm.getAfosid());
            }
        }
    }

    /**
     * Populate the selected message types table. These are the message type
     * associated with the suite.
     */
    private void populateSelectedMsgTypesTable(boolean replaceTableItems) {
        if (selectedMsgTypeTable.hasTableData() == false) {
            List<TableColumnData> columnNames = new ArrayList<TableColumnData>();
            TableColumnData tcd = new TableColumnData("Message Type", 150);
            columnNames.add(tcd);
            tcd = new TableColumnData("Message Title", 300);
            columnNames.add(tcd);
            if (this.selectedProgram != null) {
                tcd = new TableColumnData("Trigger");
                columnNames.add(tcd);
            }
            selectedMsgTypeTableData = new TableData(columnNames);
        } else {
            selectedMsgTypeTableData.deleteAllRows();
        }

        populateSelectedMessageTypeTableData();

        if (replaceTableItems) {
            selectedMsgTypeTable.replaceTableItems(selectedMsgTypeTableData);
        } else {
            selectedMsgTypeTable.populateTable(selectedMsgTypeTableData);
        }

        if (selectedMsgTypeTable.getItemCount() > 0) {
            if (!replaceTableItems) {
                selectedMsgTypeTable.select(0);
            }
        }

        createSaveBtn.setEnabled(selectedMsgTypeTable.getItemCount() > 0);
    }

    /**
     * Populate the selected message type data. This will be used to populate
     * the table.
     */
    private void populateSelectedMessageTypeTableData() {
        for (SuiteMessage sm : msgTypesInSuiteList) {
            TableRowData trd = new TableRowData();

            trd.addTableCellData(new TableCellData(sm.getMsgTypeSummary()
                    .getAfosid()));
            trd.addTableCellData(new TableCellData(sm.getMsgTypeSummary()
                    .getTitle()));
            if ((this.selectedProgram != null) && (this.selectedSuite != null)) {
                trd.addTableCellData(new TableCellData(this.selectedProgram
                        .isTriggerMsgType(this.selectedSuite,
                                sm.getMsgTypeSummary()) ? "Yes" : "No"));
            }

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
}
