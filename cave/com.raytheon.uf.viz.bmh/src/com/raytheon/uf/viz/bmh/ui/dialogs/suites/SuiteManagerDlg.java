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
package com.raytheon.uf.viz.bmh.ui.dialogs.suites;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.common.bmh.datamodel.msg.Program;
import com.raytheon.uf.common.bmh.datamodel.msg.Suite;
import com.raytheon.uf.common.bmh.datamodel.msg.Suite.SuiteType;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.viz.bmh.data.BmhUtils;
import com.raytheon.uf.viz.bmh.ui.common.utility.DialogUtility;
import com.raytheon.uf.viz.bmh.ui.common.utility.InputTextDlg;
import com.raytheon.uf.viz.bmh.ui.dialogs.AbstractBMHDialog;
import com.raytheon.uf.viz.bmh.ui.dialogs.DlgInfo;
import com.raytheon.uf.viz.bmh.ui.program.SuiteConfigGroup;
import com.raytheon.uf.viz.bmh.ui.program.SuiteConfigGroup.SuiteGroupType;
import com.raytheon.viz.ui.dialogs.ICloseCallback;

/**
 * 
 * Main Suite Manager dialog for managing suites that are a part of program or
 * stand-alone.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 24, 2014  #3433     lvenable     Initial creation
 * Jul 27, 2014  #3420     lvenable     Updated to use relationship button.
 * Aug 03, 2014  #3479     lvenable    Updated code for validator changes.
 * Aug 06, 2014  #3490     lvenable    Refactored and moved code to SuiteConfigGroup.
 * Aug 12, 2014  #3490     lvenable    Updated code to use database data.
 * Aug 15, 2014  #3490     lvenable     Sort the list of suites, use suite data manager.
 * Aug 15, 2014  #3490     lvenable     Added copy, rename, other capabilities.
 * Aug 15, 2014  #3490     lvenable     Added existing names.
 * Aug 22, 2014  #3490     lvenable     Added input dialog flag.
 * Oct 08, 2014  #3479     lvenable     Changed MODE_INDEPENDENT to PERSPECTIVE_INDEPENDENT.
 * Nov 11, 2014  3413      rferrel     Use DlgInfo to get title.
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */
public class SuiteManagerDlg extends AbstractBMHDialog {

    /** Status handler for reporting errors. */
    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(SuiteManagerDlg.class);

    /** List of suites. */
    private List<Suite> suiteList = null;

    /** Group that contains the controls for configuring the suites. */
    private SuiteConfigGroup suiteConfigGroup;

    /** Suite data manger. */
    private SuiteDataManager suiteDataMgr = new SuiteDataManager();

    /** Set of existing suite names. */
    private Set<String> suiteNames = new HashSet<String>();

    /**
     * Constructor.
     * 
     * @param parentShell
     *            Parent shell.
     * @param dlgMap
     *            Map to add this dialog to for closing purposes.
     */
    public SuiteManagerDlg(Shell parentShell,
            Map<AbstractBMHDialog, String> dlgMap) {
        super(dlgMap, DlgInfo.SUITE_MANAGER.getTitle(), parentShell,
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
        setText(DlgInfo.SUITE_MANAGER.getTitle());

        retrieveDataFromDB();
        createSuiteTableGroup();
        createBottomButtons();

        suiteConfigGroup.populateSuiteTable(suiteList, false);
    }

    /**
     * Create the Suite group.
     */
    private void createSuiteTableGroup() {
        suiteConfigGroup = new SuiteConfigGroup(shell, " Available Suites: ",
                SuiteGroupType.SUITE_MGR, null, 550, 150);
        suiteConfigGroup.setCallBackAction(new SuiteActionAdapter() {
            @Override
            public void suiteSelected(Suite suite) {
            }

            @Override
            public void suitesUpdated(Suite suite) {
                retrieveDataFromDB();
                suiteConfigGroup.populateSuiteTable(suiteList, true);
            }

            @Override
            public void deleteSuite(Suite suite) {
                handleDeleteSuite(suite);
            }

            @Override
            public void renameSuite(Suite suite) {
                SuiteNameValidator snv = new SuiteNameValidator(suiteNames);

                InputTextDlg inputDlg = new InputTextDlg(shell, "Rename Suite",
                        "Type in a new suite name: ", suite.getName(), snv,
                        false);
                inputDlg.setCloseCallback(new ICloseCallback() {
                    @Override
                    public void dialogClosed(Object returnValue) {
                        if (returnValue != null
                                && returnValue instanceof String) {
                            handleSuiteRename((String) returnValue);
                        }
                    }
                });
                inputDlg.open();
            }

            @Override
            public void copySuite(Suite suite) {
                SuiteNameValidator snv = new SuiteNameValidator(suiteNames);

                InputTextDlg inputDlg = new InputTextDlg(shell, "Copy Suite",
                        "Type in a new suite name: ", suite.getName(), snv,
                        false);
                inputDlg.setCloseCallback(new ICloseCallback() {
                    @Override
                    public void dialogClosed(Object returnValue) {
                        if (returnValue != null
                                && returnValue instanceof String) {
                            handleSuiteCopy((String) returnValue);
                        }
                    }
                });
                inputDlg.open();
            }

            @Override
            public Set<String> getSuiteNames() {
                return suiteNames;
            }
        });
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

    @Override
    public boolean okToClose() {
        // TODO - add code to check if it can close.
        return true;
    }

    /**
     * Retrieve suite data from the database.
     */
    private void retrieveDataFromDB() {
        try {
            suiteList = suiteDataMgr.getAllSuites(new SuiteNameComparator());

            suiteNames.clear();
            for (Suite s : suiteList) {
                suiteNames.add(s.getName());
            }

        } catch (Exception e) {
            statusHandler.error(
                    "Error retrieving suite data from the database: ", e);
        }
    }

    private void handleSuiteCopy(String name) {
        Suite selectedSuite = suiteConfigGroup.getSelectedSuite();

        if (selectedSuite == null) {
            return;
        }

        selectedSuite.setId(0);
        selectedSuite.setName(name);
        try {
            suiteDataMgr.saveSuite(selectedSuite);
        } catch (Exception e) {
            statusHandler.error("Error renaming the suite: ", e);
        }

        // TODO : need to scroll to the renamed suite. SWT showSelected doesn't
        // work so look at coming up with a fix.
        retrieveDataFromDB();
        suiteConfigGroup.populateSuiteTable(suiteList, false);
    }

    private void handleSuiteRename(String name) {
        Suite selectedSuite = suiteConfigGroup.getSelectedSuite();

        if (selectedSuite == null) {
            return;
        }

        selectedSuite.setName(name);
        try {
            suiteDataMgr.saveSuite(selectedSuite);
        } catch (Exception e) {
            statusHandler.error("Error renaming the suite: ", e);
        }

        // TODO : need to scroll to the renamed suite. SWT showSelected doesn't
        // work so look at coming up with a fix.
        retrieveDataFromDB();
        suiteConfigGroup.populateSuiteTable(suiteList, false);
    }

    private void handleDeleteSuite(Suite suite) {
        // Safety check in case the selected suite is null;
        if (suite == null) {
            return;
        }

        try {
            /* Check to see if suite is used by an enabled transmitter. */
            if (suite.getType() == SuiteType.GENERAL) {
                List<TransmitterGroup> enabledTransmitters = BmhUtils
                        .getSuiteEnabledTransmitterGroups(suite);
                if (!enabledTransmitters.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Cannot delete suite ")
                            .append(suite.getName())
                            .append(". Its category is GENERAL and it is used by the following ENABLED transmitter/group(s):\n");
                    for (TransmitterGroup transmitter : enabledTransmitters) {
                        sb.append("\n\t").append(transmitter.getName());
                    }
                    DialogUtility.showMessageBox(shell,
                            SWT.ICON_ERROR | SWT.OK, "Delete Suite",
                            sb.toString());
                    return;
                }
            }
        } catch (Exception e1) {
            statusHandler.handle(Priority.PROBLEM,
                    "Failed to look up ENABLED transmitters associated with suite "
                            + suite.getName() + ". ", e1);
            return;
        }

        List<Program> programs = null;
        try {
            programs = BmhUtils.getSuitePrograms(suite);
        } catch (Exception e1) {
            statusHandler.handle(Priority.PROBLEM,
                    "Failed to lookup programs associated with a suite "
                            + suite.getName() + ". ", e1);
            return;
        }

        StringBuilder sb = new StringBuilder();
        if (!programs.isEmpty()) {
            sb.append("The suite is contained in the following program(s):\n");
            for (Program program : programs) {
                sb.append("\n\t").append(program.getName());
            }
        }

        sb.append("\nDo you wish to delete suite ").append(suite.getName())
                .append("?");

        int result = DialogUtility.showMessageBox(shell, SWT.ICON_WARNING
                | SWT.OK | SWT.CANCEL, "Confirm Delete", sb.toString());

        if (result == SWT.CANCEL) {
            return;
        }

        try {
            suiteDataMgr.deleteSuite(suite);
        } catch (Exception e) {
            statusHandler.error("Error deleting suite " + suite.getName()
                    + " from the database: ", e);
        }

        retrieveDataFromDB();
        suiteConfigGroup.populateSuiteTable(suiteList, true);
    }
}
