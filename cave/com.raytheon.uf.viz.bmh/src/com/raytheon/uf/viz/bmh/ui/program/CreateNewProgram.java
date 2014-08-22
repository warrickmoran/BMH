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

import com.raytheon.uf.common.bmh.datamodel.msg.Program;
import com.raytheon.uf.common.bmh.datamodel.msg.Suite;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.bmh.ui.common.utility.DialogUtility;
import com.raytheon.uf.viz.bmh.ui.dialogs.suites.SuiteActionAdapter;
import com.raytheon.uf.viz.bmh.ui.dialogs.suites.SuiteDataManager;
import com.raytheon.uf.viz.bmh.ui.program.SuiteConfigGroup.SuiteGroupType;
import com.raytheon.viz.ui.dialogs.CaveSWTDialog;

/**
 * 
 * Dialog to create a new program.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 20, 2014  #3174     lvenable     Initial creation
 * Jul 24, 2014  #3433     lvenable     Updated for Suite manager
 * Aug 12, 2014  #3490      lvenable    Updated method call.
 * Aug 21, 2014  #3490      lvenable    Added database capability.
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */
public class CreateNewProgram extends CaveSWTDialog {

    /** Status handler for reporting errors. */
    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(CreateNewProgram.class);

    /** Save button. */
    private Button saveBtn;

    /** Group that contains the controls for configuring the suites. */
    private SuiteConfigGroup suiteConfigGroup;

    /** Program text field. */
    private Text programTF;

    private Set<String> existingProgramNames = null;

    private List<Suite> addedSuitesList = new ArrayList<Suite>();

    /**
     * Constructor.
     * 
     * @param parentShell
     *            Parent shell.
     */
    public CreateNewProgram(Shell parentShell, Set<String> existingProgramNames) {
        super(parentShell, SWT.DIALOG_TRIM | SWT.MIN | SWT.PRIMARY_MODAL,
                CAVE.DO_NOT_BLOCK | CAVE.MODE_INDEPENDENT);

        this.existingProgramNames = existingProgramNames;
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
        setText("New Program");

        createProgramControlsTable();
        DialogUtility.addSeparator(shell, SWT.HORIZONTAL);
        createSelectedSuitesGroup();
        createBottomButtons();
    }

    /**
     * Create the program controls.
     */
    private void createProgramControlsTable() {
        Composite programComp = new Composite(shell, SWT.NONE);
        GridLayout gl = new GridLayout(2, false);
        gl.marginWidth = 0;
        programComp.setLayout(gl);
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        programComp.setLayoutData(gd);

        Label nameLbl = new Label(programComp, SWT.NONE);
        nameLbl.setText("Program Name: ");

        gd = new GridData(250, SWT.DEFAULT);
        programTF = new Text(programComp, SWT.BORDER);
        programTF.setLayoutData(gd);
    }

    /**
     * Create the group containing the selected suites table and controls.
     */
    private void createSelectedSuitesGroup() {

        suiteConfigGroup = new SuiteConfigGroup(shell, " Selected Suites: ",
                SuiteGroupType.NEW_PROGRAM, null);
        suiteConfigGroup.initializeTableColumns();
        suiteConfigGroup.setCallBackAction(new SuiteActionAdapter() {

            @Override
            public void deleteSuite(Suite suite) {
                handleDeleteSuite(suite);
            }

            @Override
            public void addedSuites(List<Suite> suiteList) {
                handleSuitesAdded(suiteList);
            }

            @Override
            public Set<String> getSuiteNames() {
                Set<String> suiteNames = new HashSet<String>();

                try {
                    // TODO: Need a query to get the suite names for better
                    // performance.

                    SuiteDataManager sdm = new SuiteDataManager();
                    List<Suite> suiteList = sdm.getSuitesMsgTypes();

                    for (Suite s : suiteList) {
                        suiteNames.add(s.getName());
                    }

                } catch (Exception e) {
                    statusHandler.error(
                            "Error retrieving suite data from the database: ",
                            e);
                    return suiteNames;
                }

                return suiteNames;
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
        saveBtn = new Button(buttonComp, SWT.PUSH);
        saveBtn.setText(" Save ");
        saveBtn.setLayoutData(gd);
        saveBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleSaveProgram();

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
                setReturnValue(null);
                close();
            }
        });
    }

    private void handleSuitesAdded(List<Suite> suiteList) {
        addedSuitesList.addAll(suiteList);
        suiteConfigGroup.populateSuiteTable(addedSuitesList, false);
    }

    private void handleDeleteSuite(Suite suite) {
        for (int i = 0; i < addedSuitesList.size(); i++) {
            if (addedSuitesList.get(i).getId() == suite.getId()) {
                addedSuitesList.remove(i);
                break;
            }
        }

        suiteConfigGroup.populateSuiteTable(addedSuitesList, false);
    }

    private void handleSaveProgram() {

        if (addedSuitesList.isEmpty()) {
            DialogUtility.showMessageBox(shell, SWT.ICON_WARNING | SWT.OK,
                    "No Suites Added",
                    "No suites have been added to the program.");
            return;
        }

        ProgramNameValidator pnv = new ProgramNameValidator(
                existingProgramNames);

        if (!pnv.validateInputText(shell, programTF.getText().trim())) {
            return;
        }

        Program newProgram = new Program();
        newProgram.setName(programTF.getText().trim());
        newProgram.setSuites(addedSuitesList);

        ProgramDataManager pdm = new ProgramDataManager();
        try {
            pdm.saveProgram(newProgram);
        } catch (Exception e) {
            statusHandler.error("Error saving program " + newProgram.getName()
                    + " to the database: ", e);

            // TODO : do we really want to return here? Need to think about how
            // to handle this.
            return;
        }

        setReturnValue(programTF.getText().trim());

        close();
    }
}
