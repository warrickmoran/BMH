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
import java.util.Iterator;
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
import com.raytheon.uf.common.bmh.datamodel.msg.ProgramSuite;
import com.raytheon.uf.common.bmh.datamodel.msg.ProgramSummary;
import com.raytheon.uf.common.bmh.datamodel.msg.Suite;
import com.raytheon.uf.common.bmh.datamodel.msg.Suite.SuiteType;
import com.raytheon.uf.common.bmh.datamodel.msg.SuiteMessage;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TxStatus;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.viz.bmh.data.BmhUtils;
import com.raytheon.uf.viz.bmh.ui.common.table.TableCellData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableColumnData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableRowData;
import com.raytheon.uf.viz.bmh.ui.common.utility.CustomToolTip;
import com.raytheon.uf.viz.bmh.ui.common.utility.DialogUtility;
import com.raytheon.uf.viz.bmh.ui.common.utility.InputTextDlg;
import com.raytheon.uf.viz.bmh.ui.dialogs.AbstractBMHDialog;
import com.raytheon.uf.viz.bmh.ui.dialogs.DlgInfo;
import com.raytheon.uf.viz.bmh.ui.dialogs.msgtypes.MsgTypeTable;
import com.raytheon.uf.viz.bmh.ui.dialogs.suites.SuiteActionAdapter;
import com.raytheon.uf.viz.bmh.ui.dialogs.suites.SuiteDataManager;
import com.raytheon.uf.viz.bmh.ui.dialogs.suites.SuiteNameComparator;
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
 * Jul 20, 2014  #3174     lvenable    Initial creation
 * Aug 01, 2014  #3479     lvenable    Added additional capability for managing the controls.
 * Aug 03, 2014  #3479     lvenable    Updated code for validator changes.
 * Aug 06, 2014  #3490     lvenable    Update to populate controls with data from the database.
 * Aug 8,  2014  #3490     lvenable    Updated populate table method call.
 * Aug 12, 2014  #3490     lvenable    Updated to use data from the database.
 * Aug 15, 2014  #3490     lvenable    Updated to use data managers, added rename capability.
 * Aug 21, 2014  #3490     lvenable    Updated for program changes.
 * Aug 22, 2014  #3490     lvenable    Added input dialog flag.
 * Aug 23, 2014  #3490     lvenable    Added capability for add transmitters.
 * Aug 25, 2014  #3490     lvenable    Update suite config group when the program changes.
 * Sep 16, 2014  #3587     bkowal      Updated to only allow trigger assignment for {Program, Suite}
 * Oct 08, 2014  #3479     lvenable    Changed MODE_INDEPENDENT to PERSPECTIVE_INDEPENDENT.
 * Oct 13, 2014  3654      rjpeter     Updated to use MessageTypeSummary.
 * Oct 27, 2014  3750      lvenable    fixed message type table update when adding new suite.
 * Nov 11, 2014  3413      rferrel     Use DlgInfo to get title.
 * Nov 13, 2014  3698      rferrel     Checks to allow only one GENERAL type suite per program.
 * Nov 22, 2014  3842      mpduff      Fully populate dialog on new program.
 * Dec 01, 2014  3698      rferrel     Changed message dialogs to only perform action when OK button is selected.
 * Dec 07, 2014  3846      mpduff      Only load the selected Program rather than all programs.
 *                                      Changing program selection now updates program's transmitters.
 * Dec 09, 2014  3906      lvenable    Changed the grid data for the suite and message type tables.
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
    private final List<Control> programControls = new ArrayList<Control>();

    /** List of PrgramSummary objects */
    private List<ProgramSummary> programSummaryList = new ArrayList<>();

    /** Message Type table data. */
    private TableData msgTypeTableData = null;

    /** Suite group text prefix. */
    private final String suiteGroupTextPrefix = " Suites in Program: ";

    /** The selected program. */
    private Program selectedProgram = null;

    /** List of transmitters for the program. */
    private final List<Transmitter> transmitterList = new ArrayList<Transmitter>();

    /** Program data manager. */
    private final ProgramDataManager programDataMgr = new ProgramDataManager();

    /** Set of existing program names. */
    private final Set<String> existingProgramNames = new HashSet<String>();

    /** Custom tool tip. */
    private CustomToolTip transmitterToolTip = null;

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
        super(dlgMap, DlgInfo.BROADCAST_PROGRAMS.getTitle(), parentShell,
                SWT.DIALOG_TRIM | SWT.MIN | SWT.RESIZE, CAVE.DO_NOT_BLOCK
                        | CAVE.PERSPECTIVE_INDEPENDENT);
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
    protected void opened() {
        shell.setMinimumSize(shell.getSize());
    }

    @Override
    protected void initializeComponents(Shell shell) {
        setText(DlgInfo.BROADCAST_PROGRAMS.getTitle());

        // Get the ProgramSummary data.
        retrieveProgramSummariesFromDB();

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

        suiteConfigGroup.setSelectedProgram(selectedProgram);
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
                CreateNewProgram cnp = new CreateNewProgram(shell,
                        existingProgramNames);
                cnp.setCloseCallback(new ICloseCallback() {
                    @Override
                    public void dialogClosed(Object returnValue) {
                        if ((returnValue != null)
                                && (returnValue instanceof String)) {
                            handleNewProgram((String) returnValue);
                        }
                    }
                });
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
                Set<String> programNames = new HashSet<String>();

                for (ProgramSummary ps : programSummaryList) {
                    programNames.add(ps.getName());
                }

                ProgramNameValidator pnv = new ProgramNameValidator(
                        programNames);

                InputTextDlg inputDlg = new InputTextDlg(shell,
                        "Rename Program", "Type in a new program name:", pnv,
                        false);
                inputDlg.setCloseCallback(new ICloseCallback() {
                    @Override
                    public void dialogClosed(Object returnValue) {
                        if ((returnValue != null)
                                && (returnValue instanceof String)) {
                            handleProgramRename((String) returnValue);
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
                handleDeleteProgram();
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
                handleAssignTransmitter();
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
        suiteConfigGroup
                .setFillStyleOnTable(SWT.FILL, SWT.DEFAULT, true, false);
        suiteConfigGroup.setCallBackAction(new SuiteActionAdapter() {

            @Override
            public void suiteSelected(Suite suite) {
                populateMsgTypeTable(suite);
            }

            @Override
            public void addedSuites(List<Suite> suiteList) {
                handleSuitesAdded(suiteList);
            }

            @Override
            public void suitesUpdated(Suite suite) {
                handleSuitesUpdated(suite);
            }

            @Override
            public void deleteSuite(Suite suite) {
                handleSuiteDeleted(suite);
            }

            @Override
            public Set<String> getSuiteNames() {
                Set<String> suiteNames = new HashSet<String>();
                try {
                    SuiteDataManager suiteDataMgr = new SuiteDataManager();
                    List<Suite> suiteList = suiteDataMgr
                            .getAllSuites(new SuiteNameComparator());

                    for (Suite s : suiteList) {
                        suiteNames.add(s.getName());
                    }

                } catch (Exception e) {
                    statusHandler.error(
                            "Error retrieving suite data from the database: ",
                            e);
                }
                return suiteNames;
            }
        });
    }

    private void handleSuitesAdded(List<Suite> suiteList) {

        int suiteID = Integer.MIN_VALUE;

        for (Suite s : suiteList) {
            if (suiteID == Integer.MIN_VALUE) {
                suiteID = s.getId();
            }
            selectedProgram.addSuite(s);
        }

        try {
            programDataMgr.saveProgram(selectedProgram);
        } catch (Exception e) {
            statusHandler.error("Error adding suite(s) to program "
                    + selectedProgram.getName(), e);
        }

        retrieveProgramSummariesFromDB();
        populateProgramCombo(true);
        populateSuiteTable(true);

        if (suiteID != Integer.MIN_VALUE) {
            suiteConfigGroup.selectSuiteInTable(suiteID);
        }

        populateMsgTypeTable(suiteConfigGroup.getSelectedSuite());
        updateSuiteGroupText();
    }

    /**
     * Assign transmitter action handler.
     */
    private void handleAssignTransmitter() {
        AddTransmittersDlg atd = new AddTransmittersDlg(shell,
                selectedProgram.getName());
        atd.setCloseCallback(new ICloseCallback() {
            @Override
            public void dialogClosed(Object returnValue) {
                if ((returnValue != null) && (returnValue instanceof List<?>)) {

                    List<TransmitterGroup> selectedTransmitters = (List<TransmitterGroup>) returnValue;

                    handleAddTransmitters(selectedTransmitters);
                }
            }
        });
        atd.open();
    }

    /**
     * Handle adding transmitters/groups to the program.
     * 
     * @param selectedTransmitters
     *            List of transmitter groups.
     */
    private void handleAddTransmitters(
            List<TransmitterGroup> selectedTransmitters) {

        for (TransmitterGroup tg : selectedTransmitters) {
            selectedProgram.addTransmitterGroup(tg);
        }

        try {
            programDataMgr.saveProgram(selectedProgram);
        } catch (Exception e) {
            statusHandler.error("Error adding suite(s) to program "
                    + selectedProgram.getName(), e);
        }

        retrieveProgramSummariesFromDB();
        populateProgramCombo(true);
        populateTransmitters();
        populateSuiteTable(true);

        populateMsgTypeTable(suiteConfigGroup.getSelectedSuite());
        updateSuiteGroupText();
    }

    /**
     * Create the message types group.
     */
    private void createMessageTypeGroup() {

        messageTypeGroup = new Group(shell, SWT.SHADOW_OUT);
        GridLayout gl = new GridLayout(1, false);
        messageTypeGroup.setLayout(gl);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        messageTypeGroup.setLayoutData(gd);
        messageTypeGroup.setText(messgaeTypeGrpPrefix);

        msgTypeTable = new MsgTypeTable(messageTypeGroup, 400, 150);
    }

    /**
     * Update the suite group text with the currently selected program name.
     */
    private void updateSuiteGroupText() {
        if ((programCbo.getItemCount() > 0)
                && (programCbo.getSelectionIndex() >= 0)) {
            suiteConfigGroup.updateSuiteGroupText(suiteGroupTextPrefix
                    + selectedProgram.getName());
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
    private void handleDeleteProgram() {

        // Safety check to make sure there is a program selected.
        if (programCbo.getSelectionIndex() < 0) {
            return;
        }

        /*
         * If there is a transmitter that is enabled the program cannot be
         * deleted.
         */
        StringBuilder transmitterMsg = null;
        for (Transmitter t : transmitterList) {
            if (t.getTxStatus() == TxStatus.ENABLED) {
                if (transmitterMsg == null) {
                    transmitterMsg = new StringBuilder(
                            "The following transmitters are enabled:\n\n");
                }
                transmitterMsg.append(t.getName()).append("\n");
            }
        }

        if (transmitterMsg != null) {
            transmitterMsg
                    .append("\nYou must disable the transmitter(s) or reassign the transmitter(s) a ")
                    .append("different program before this program can be deleted");
            DialogUtility.showMessageBox(shell, SWT.ICON_WARNING | SWT.OK,
                    "Enabled Transmitters", transmitterMsg.toString());
            return;
        }

        StringBuilder sb = new StringBuilder();

        sb.append("Are you sure you want to delete the following program: ")
                .append(programCbo.getItem(programCbo.getSelectionIndex()))
                .append("?");

        int result = DialogUtility.showMessageBox(shell, SWT.ICON_WARNING
                | SWT.OK | SWT.CANCEL, "Confirm Delete", sb.toString());

        if (result != SWT.OK) {
            return;
        }

        try {
            programDataMgr.deleteProgram(selectedProgram);
        } catch (Exception e) {
            statusHandler.error(
                    "Error deleting program " + selectedProgram.getName()
                            + " from the database: ", e);
        }

        retrieveProgramSummariesFromDB();
        populateProgramCombo();
        handleProgramChange();
    }

    private void handleNewProgram(String programName) {
        retrieveProgramSummariesFromDB();
        populateProgramCombo(programName);
        populateSuiteTable(true);

        populateMsgTypeTable(suiteConfigGroup.getSelectedSuite());
        updateSuiteGroupText();
        handleProgramChange();
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
     * Delete the specified suite.
     * 
     * @param suite
     *            Suite to be deleted.
     */
    private void handleSuiteDeleted(Suite suite) {

        // Safety check in case the selected suite is null;
        if ((suite == null) || (selectedProgram == null)) {
            return;
        }

        /*
         * Do not allow removal of suite if it is GENERAL and is contained in a
         * program used by an ENABLED transmitter.
         */
        if (suite.getType() == SuiteType.GENERAL) {
            try {
                List<TransmitterGroup> enabledGroups = BmhUtils
                        .getSuiteEnabledTransmitterGroups(suite);

                if (!enabledGroups.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Cannot remove suite ")
                            .append(suite.getName())
                            .append(" since it is category GENERAL and is associated with the following ENABLED transmitter/group(s):");
                    for (TransmitterGroup group : enabledGroups) {
                        sb.append("\n\t").append(group.getName());
                    }
                    DialogUtility.showMessageBox(shell, SWT.ICON_WARNING
                            | SWT.OK, "Suite Removal", sb.toString());
                    return;
                }
            } catch (Exception e) {
                statusHandler.handle(Priority.ERROR,
                        "Unable to perform suite removal. ", e);
                return;
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("\nDo you wish to remove this suite from program: ")
                .append(selectedProgram.getName()).append("?");

        int result = DialogUtility.showMessageBox(getParent().getShell(),
                SWT.ICON_WARNING | SWT.OK | SWT.CANCEL, "Confirm Delete",
                sb.toString());

        if (result != SWT.OK) {
            return;
        }

        List<ProgramSuite> programSuites = selectedProgram.getProgramSuites();
        Iterator<ProgramSuite> programSuiteIterator = programSuites.iterator();
        boolean removed = false;
        while (programSuiteIterator.hasNext()) {
            if (programSuiteIterator.next().getSuite().getId() == suite.getId()) {
                programSuiteIterator.remove();
                removed = true;
                break;
            }
        }

        if (removed == false) {
            return;
        }

        try {
            programDataMgr.saveProgram(selectedProgram);
        } catch (Exception e) {
            statusHandler.error(
                    "Error saving program data from the database: ", e);
        }

        retrieveProgramSummariesFromDB();
        populateProgramCombo(true);
        populateSuiteTable(true);

        populateMsgTypeTable(suiteConfigGroup.getSelectedSuite());
        updateSuiteGroupText();
    }

    private void handleSuitesUpdated(Suite suite) {

        List<Suite> progSuites = selectedProgram.getSuites();

        for (int i = 0; i < progSuites.size(); i++) {
            if (progSuites.get(i).getId() == suite.getId()) {
                progSuites.set(i, suite);

                try {
                    programDataMgr.saveProgram(selectedProgram);
                } catch (Exception e) {
                    statusHandler.error("Error saving the program: ", e);
                }

            }
        }

        retrieveProgramSummariesFromDB();
        populateProgramCombo(true);
        populateSuiteTable(true);

        populateMsgTypeTable(suiteConfigGroup.getSelectedSuite());
        updateSuiteGroupText();
        suiteConfigGroup.selectSuiteInTable(suite.getId());
    }

    /**
     * Handle when the program changes.
     */
    private void handleProgramChange() {
        updateSelectedProgram();
        populateTransmitters();
        populateSuiteTable(false);
        populateMsgTypeTable(suiteConfigGroup.getSelectedSuite());
        updateSuiteGroupText();
    }

    /**
     * Rename the program.
     * 
     * @param name
     *            New program name.
     */
    private void handleProgramRename(String name) {

        selectedProgram.setName(name);
        try {
            programDataMgr.saveProgram(selectedProgram);
        } catch (Exception e) {
            statusHandler.error("Error renaming the program: ", e);
        }

        retrieveProgramSummariesFromDB();
        populateProgramCombo(name);
        populateSuiteTable(true);

        populateMsgTypeTable(suiteConfigGroup.getSelectedSuite());
        updateSuiteGroupText();
    }

    /**
     * Retrieve the ProgramSummary objects from the database.
     */
    private void retrieveProgramSummariesFromDB() {
        try {
            programSummaryList = programDataMgr
                    .getProgramSummaries(new ProgramSummaryNameComparator());
        } catch (Exception e) {
            statusHandler
                    .error("Error retrieving program summary data from the database: ",
                            e);
        }
    }

    /**
     * Populate the program combo box.
     */
    private void populateProgramCombo() {
        populateProgramCombo(null);
    }

    /**
     * Populate the program control and reselect the same item in the combo.
     * 
     * @param reselectProgram
     *            Flag indicating if the same program should be reselected.
     */
    private void populateProgramCombo(boolean reselectProgram) {
        if (programCbo.getSelectionIndex() >= 0) {
            String selectedProgramStr = programCbo.getItem(programCbo
                    .getSelectionIndex());
            populateProgramCombo(selectedProgramStr);
        } else {
            populateProgramCombo(null);
        }
    }

    /**
     * After populating the program combo control, select the name specified.
     * 
     * @param programToSelect
     *            Name to select. If null then select the first item in the
     *            combo box.
     */
    private void populateProgramCombo(String programToSelect) {

        /*
         * If there are items in the combo then remove them before populating.
         */
        if (programCbo.getItemCount() > 0) {
            programCbo.removeAll();
        }

        existingProgramNames.clear();
        for (ProgramSummary ps : programSummaryList) {
            programCbo.add(ps.getName());
            existingProgramNames.add(ps.getName());
        }

        enableProgramControls(false);

        /*
         * If the program name passed in is not null then select it. If it is
         * null or the string is not found then select item 0.
         */
        if (programCbo.getItemCount() > 0) {
            if (programToSelect != null) {
                int idx = programCbo.indexOf(programToSelect);
                if (idx >= 0) {
                    programCbo.select(idx);
                } else {
                    programCbo.select(0);
                }
            } else {
                programCbo.select(0);
            }

            enableProgramControls(true);
            updateSelectedProgram();
            populateTransmitters();
        }
    }

    /**
     * Update the selected program when the table selection changes.
     */
    private void updateSelectedProgram() {
        if (programCbo.getSelectionIndex() >= 0) {
            int idx = programCbo.getSelectionIndex();
            ProgramSummary summ = programSummaryList.get(idx);
            try {
                selectedProgram = programDataMgr.getProgramById(summ.getId());
                if (selectedProgram != null) {
                    suiteConfigGroup.setSelectedProgram(selectedProgram);
                }
            } catch (Exception e) {
                statusHandler.error("Error getting Program " + summ.getName(),
                        e);
            }
        }
    }

    /**
     * Populate the transmitters.
     */
    private void populateTransmitters() {
        int index = programCbo.getSelectionIndex();

        if (index >= 0) {
            transmitterList.clear();
            StringBuilder sb = new StringBuilder();
            StringBuilder sbToolTip = new StringBuilder();
            Set<TransmitterGroup> transGrp = selectedProgram
                    .getTransmitterGroups();
            for (TransmitterGroup tg : transGrp) {
                sbToolTip.append(tg.getName()).append(": ");
                Set<Transmitter> transmitters = tg.getTransmitters();
                for (Transmitter t : transmitters) {
                    transmitterList.add(t);
                    sbToolTip.append(t.getName()).append(", ");
                }
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(tg.getName());
                if (", ".equals(sbToolTip.substring(sbToolTip.length() - 2))) {
                    sbToolTip.setLength(sbToolTip.length() - 2);
                }
                sbToolTip.append("\n");
            }
            transmitterListLbl.setText(sb.toString());

            if (transmitterToolTip == null) {
                transmitterToolTip = new CustomToolTip(transmitterListLbl,
                        sbToolTip.toString());
            } else {
                transmitterToolTip.setText(sbToolTip.toString());
            }
        }
    }

    /**
     * Populate the suite table.
     */
    private void populateSuiteTable(boolean replaceTableItems) {
        if (programCbo.getSelectionIndex() >= 0) {
            List<Suite> suiteList = selectedProgram.getSuites();
            suiteConfigGroup.populateSuiteTable(suiteList, replaceTableItems);
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
            tcd = new TableColumnData("Message Title", 250);
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

            trd.addTableCellData(new TableCellData(sm.getMsgTypeSummary()
                    .getAfosid()));
            trd.addTableCellData(new TableCellData(sm.getMsgTypeSummary()
                    .getTitle()));
            trd.addTableCellData(new TableCellData(this.selectedProgram
                    .isTriggerMsgType(suite, sm.getMsgTypeSummary()) ? "Yes"
                    : "No"));

            msgTypeTableData.addDataRow(trd);
        }
    }
}
