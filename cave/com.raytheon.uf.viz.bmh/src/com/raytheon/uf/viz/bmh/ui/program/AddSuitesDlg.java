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
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.raytheon.uf.common.bmh.datamodel.msg.Suite;
import com.raytheon.uf.common.bmh.request.SuiteResponse;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.bmh.ui.common.utility.DialogUtility;
import com.raytheon.uf.viz.bmh.ui.dialogs.suites.SuiteActionAdapter;
import com.raytheon.uf.viz.bmh.ui.dialogs.suites.SuiteDataManager;
import com.raytheon.uf.viz.bmh.ui.dialogs.suites.SuiteNameComparator;
import com.raytheon.uf.viz.bmh.ui.dialogs.suites.SuiteNameValidator;
import com.raytheon.uf.viz.bmh.ui.program.SuiteConfigGroup.SuiteGroupType;
import com.raytheon.viz.ui.dialogs.CaveSWTDialog;

/**
 * Dialog that will allow the user to add existing suites into a program.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 16, 2014  #3174     lvenable     Initial creation
 * Jul 24, 2014  #3433     lvenable     Updated for Suite manager
 * Jul 27, 2014  #3420     lvenable     Update to use a relationships button.
 * Aug 12, 2014  #3490     lvenable     Updated to use the suite config group and
 *                                      hooked up data from the database.
 * Aug 15, 2014  #3490     lvenable     Sort the list of suites.
 * Aug 18, 2014  #3490     lvenable     Updated code changes and added To Do reminders.
 * Aug 21, 2014  #3490     lvenable     Updated code to save data to database.
 * Aug 24, 2014  #3490     lvenable     Removed dialog type and fixed copy suite error.
 * Aug 25, 2014  #3490     lvenable     Validate the triggers for message types.
 * Sep 10, 2014  #3490     lvenable     Added check for adding duplicate suites.
 * Sep 11, 2014  #3587     bkowal       Remove validateSuiteTriggerMessages. Updated
 *                                      to only allow trigger assignment for {Program, Suite}
 * Oct 08, 2014  #3479     lvenable     Changed MODE_INDEPENDENT to PERSPECTIVE_INDEPENDENT.
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */
public class AddSuitesDlg extends CaveSWTDialog {

    /** Status handler for reporting errors. */
    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(AddSuitesDlg.class);

    /** Suite name text field. */
    private Text suiteNameTF;

    /** List of suites. */
    private List<Suite> suiteList = null;

    /**
     * Array of controls so actions can be performed on the set that is in the
     * array.
     */
    private List<Control> controlArray = new ArrayList<Control>();

    /** Group that contains the controls for configuring the suites. */
    private SuiteConfigGroup suiteConfigGroup;

    /** Add button. */
    private Button addBtn;

    /** Flag indicating if the suite is existing or a copy. */
    private boolean useExisting = true;

    /** Set of exiting names. */
    private Set<String> existingNames = null;

    /**
     * Constructor.
     * 
     * @param parentShell
     *            Parent shell.
     */
    public AddSuitesDlg(Shell parentShell, Set<String> existingNames) {
        super(parentShell, SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL,
                CAVE.DO_NOT_BLOCK | CAVE.PERSPECTIVE_INDEPENDENT);

        this.existingNames = existingNames;
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
    protected void initializeComponents(Shell shell) {
        setText("Add/Copy Existing Suites");

        retrieveDataFromDB();

        createOptionControls();
        createSuitesTable();
        createBottomButtons();

        suiteConfigGroup.populateSuiteTable(suiteList, false);

        addBtn.setEnabled(suiteConfigGroup.getSelectedSuite() != null);
    }

    /**
     * Create the option controls for using an existing suite or making a copy
     * of an existing suite.
     */
    private void createOptionControls() {
        Composite optionsComp = new Composite(shell, SWT.NONE);
        optionsComp.setLayout(new GridLayout(2, false));
        optionsComp.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true,
                false));

        GridData gd;

        gd = new GridData();
        gd.horizontalSpan = 2;
        Button useExistingSuiteRdo = new Button(optionsComp, SWT.RADIO);
        useExistingSuiteRdo.setText("Use Existing Suites");
        useExistingSuiteRdo.setLayoutData(gd);
        useExistingSuiteRdo.setSelection(true);
        useExistingSuiteRdo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Button rdoBtn = (Button) e.widget;
                if (rdoBtn.getSelection()) {
                    useExisting = true;
                    enableControls(false);
                    suiteConfigGroup.setMultipleSelection(true);
                }
            }
        });

        gd = new GridData();
        gd.horizontalSpan = 2;
        Button copyExistingSuiteRdo = new Button(optionsComp, SWT.RADIO);
        copyExistingSuiteRdo.setText("Copy an Existing Suite:");
        copyExistingSuiteRdo.setLayoutData(gd);
        copyExistingSuiteRdo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Button rdoBtn = (Button) e.widget;
                if (rdoBtn.getSelection()) {
                    useExisting = false;
                    enableControls(true);
                    suiteConfigGroup.setMultipleSelection(false);
                }
            }
        });

        Label suiteNameLbl = new Label(optionsComp, SWT.NONE);
        suiteNameLbl.setText("Enter a Suite Name: ");

        gd = new GridData();
        gd.horizontalIndent = 20;
        suiteNameLbl.setLayoutData(gd);

        gd = new GridData(200, SWT.DEFAULT);
        suiteNameTF = new Text(optionsComp, SWT.BORDER);
        suiteNameTF.setLayoutData(gd);

        controlArray.add(suiteNameLbl);
        controlArray.add(suiteNameTF);
        enableControls(false);
    }

    /**
     * Create the suites table.
     */
    private void createSuitesTable() {

        suiteConfigGroup = new SuiteConfigGroup(shell,
                "  Select Suite to Add: ", SuiteGroupType.ADD_COPY_EXITING,
                null, 550, 150);
        suiteConfigGroup.setMultipleSelection(true);
        suiteConfigGroup.setCallBackAction(new SuiteActionAdapter() {

            @Override
            public void suiteSelected(Suite suite) {
                addBtn.setEnabled(suite != null);
            }
        });
    }

    /**
     * Create the Add and Cancel action buttons.
     */
    private void createBottomButtons() {
        Composite buttonComp = new Composite(shell, SWT.NONE);
        buttonComp.setLayout(new GridLayout(2, false));
        buttonComp.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true,
                false));

        int buttonWidth = 70;

        GridData gd = new GridData(SWT.RIGHT, SWT.DEFAULT, true, false);
        gd.widthHint = buttonWidth;
        addBtn = new Button(buttonComp, SWT.PUSH);
        addBtn.setText(" Add ");
        addBtn.setLayoutData(gd);
        addBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleAddSuites();
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

    /**
     * Enable or disable controls.
     * 
     * @param enableFlag
     *            True to enable, false to disable.
     */
    private void enableControls(boolean enableFlag) {
        for (Control ctrl : controlArray) {
            ctrl.setEnabled(enableFlag);
        }
    }

    /**
     * Handle adding suites.
     */
    private void handleAddSuites() {
        List<Suite> selectedSuites = suiteConfigGroup.getSelectedSuites();

        // Safety check.
        if (selectedSuites.isEmpty()) {
            return;
        }

        if (useExisting) {

            if (validateSuites(selectedSuites) == false) {
                return;
            }

        } else {
            Suite copySuite = selectedSuites.get(0);

            if (copySuite == null) {
                return;
            }

            SuiteNameValidator snv = new SuiteNameValidator(existingNames);
            if (!snv.validateInputText(shell, suiteNameTF.getText().trim())) {
                return;
            }

            SuiteDataManager suiteDataMgr = new SuiteDataManager();
            copySuite.setId(0);
            copySuite.setName(suiteNameTF.getText().trim());
            try {
                SuiteResponse sr = suiteDataMgr.saveSuite(copySuite);
                copySuite = sr.getSuiteList().get(0);
            } catch (Exception e) {
                statusHandler.error("Error renaming the suite for a copy: ", e);
            }

            List<Suite> copySuites = new ArrayList<Suite>(1);
            copySuites.add(copySuite);
            setReturnValue(copySuites);
        }

        close();
    }

    /**
     * Validate the suites that will be added to the program.
     * 
     * @param selectedSuites
     *            List of suites that were selected.
     * @return True if there are valid suites to be added.
     */
    private boolean validateSuites(List<Suite> selectedSuites) {

        List<Suite> validSuites = new ArrayList<Suite>();
        List<String> invalidSuites = new ArrayList<String>();

        for (Suite s : selectedSuites) {
            if (existingNames.contains(s.getName())) {
                invalidSuites.add(s.getName());
            } else {
                validSuites.add(s);
            }
        }

        if (validSuites.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("All of the suites selected already exist in the program.  Do you want to ");
            sb.append("go back and add different suites?");
            int result = DialogUtility.showMessageBox(shell, SWT.ICON_WARNING
                    | SWT.YES | SWT.NO, "Existing Suites", sb.toString());

            // If the user wants to add valid suites then return false so the
            // dialog doesn't close.
            if (result == SWT.YES) {
                return false;
            }

            setReturnValue(null);
            return true;
        }

        if (invalidSuites.isEmpty() == false) {
            StringBuilder sb = new StringBuilder();
            sb.append("The following suites are already in the program and will not be added:\n\n");

            for (String str : invalidSuites) {
                sb.append(str).append("\n");
            }

            DialogUtility.showMessageBox(shell, SWT.ICON_WARNING | SWT.OK,
                    "Existing Suites", sb.toString());
        }

        setReturnValue(validSuites);
        return true;
    }

    /**
     * Retrieve suite data from the database.
     */
    private void retrieveDataFromDB() {
        try {
            SuiteDataManager suiteDataMgr = new SuiteDataManager();
            suiteList = suiteDataMgr.getAllSuites(new SuiteNameComparator());
        } catch (Exception e) {
            statusHandler.error(
                    "Error retrieving suite data from the database: ", e);
        }
    }
}
