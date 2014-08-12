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
import com.raytheon.uf.common.bmh.request.SuiteRequest;
import com.raytheon.uf.common.bmh.request.SuiteRequest.SuiteAction;
import com.raytheon.uf.common.bmh.request.SuiteResponse;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.bmh.data.BmhUtils;
import com.raytheon.uf.viz.bmh.ui.dialogs.suites.ISuiteSelection;
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

    /** Enumeration of dialog types. */
    public enum SuiteDialogType {
        ADD_COPY, COPY_ONLY;
    };

    /** Type of dialog (Create or Edit). */
    private SuiteDialogType dialogType = SuiteDialogType.ADD_COPY;

    /** Group that contains the controls for configuring the suites. */
    private SuiteConfigGroup suiteConfigGroup;

    /**
     * Constructor.
     * 
     * @param parentShell
     *            Parent shell.
     */
    public AddSuitesDlg(Shell parentShell, SuiteDialogType dlgType) {
        super(parentShell, SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL,
                CAVE.DO_NOT_BLOCK | CAVE.MODE_INDEPENDENT);

        this.dialogType = dlgType;
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
        if (dialogType == SuiteDialogType.ADD_COPY) {
            setText("Add/Copy Existing Suites");
        } else if (dialogType == SuiteDialogType.COPY_ONLY) {
            setText("Copy Existing Suite");
        }

        retrieveDataFromDB();

        createOptionControls();
        createSuitesTable();
        createBottomButtons();

        suiteConfigGroup.populateSuiteTable(suiteList);
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

        if (dialogType == SuiteDialogType.ADD_COPY) {
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
                        enableControls(true);
                        suiteConfigGroup.setMultipleSelection(false);
                    }
                }
            });

        }

        Label suiteNameLbl = new Label(optionsComp, SWT.NONE);
        suiteNameLbl.setText("Enter a Suite Name: ");
        if (dialogType == SuiteDialogType.ADD_COPY) {
            gd = new GridData();
            gd.horizontalIndent = 20;
            suiteNameLbl.setLayoutData(gd);
        }

        gd = new GridData(200, SWT.DEFAULT);
        suiteNameTF = new Text(optionsComp, SWT.BORDER);
        suiteNameTF.setLayoutData(gd);

        if (dialogType == SuiteDialogType.ADD_COPY) {
            controlArray.add(suiteNameLbl);
            controlArray.add(suiteNameTF);
            enableControls(false);
        }
    }

    /**
     * Create the suites table.
     */
    private void createSuitesTable() {

        suiteConfigGroup = new SuiteConfigGroup(shell,
                "  Select Suite to Add: ", SuiteGroupType.ADD_COPY_EXITING,
                null, 550, 150);
        suiteConfigGroup.setMultipleSelection(true);
        suiteConfigGroup.setCallBackAction(new ISuiteSelection() {
            @Override
            public void suiteSelected(Suite suite) {
                // TODO : look at adding code if needed for this method
            }

            @Override
            public void suitesUpdated() {
                // TODO : look at adding code if needed for this method
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
        Button addBtn = new Button(buttonComp, SWT.PUSH);
        addBtn.setText(" Add ");
        addBtn.setLayoutData(gd);
        addBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                close();
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
     * Retrieve suite data from the database.
     */
    private void retrieveDataFromDB() {
        SuiteRequest suiteRequest = new SuiteRequest();
        suiteRequest.setAction(SuiteAction.AllSuites);
        SuiteResponse suiteResponse = null;

        try {
            suiteResponse = (SuiteResponse) BmhUtils.sendRequest(suiteRequest);
            suiteList = suiteResponse.getSuiteList();

        } catch (Exception e) {
            statusHandler.error(
                    "Error retrieving suite data from the database: ", e);
        }
    }
}
