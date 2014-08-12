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

import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.common.bmh.datamodel.msg.Suite;
import com.raytheon.uf.common.bmh.request.SuiteRequest;
import com.raytheon.uf.common.bmh.request.SuiteRequest.SuiteAction;
import com.raytheon.uf.common.bmh.request.SuiteResponse;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.bmh.data.BmhUtils;
import com.raytheon.uf.viz.bmh.ui.dialogs.AbstractBMHDialog;
import com.raytheon.uf.viz.bmh.ui.program.SuiteConfigGroup;
import com.raytheon.uf.viz.bmh.ui.program.SuiteConfigGroup.SuiteGroupType;

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
        super(dlgMap, "Suite Manager Dialog", parentShell, SWT.DIALOG_TRIM
                | SWT.MIN | SWT.RESIZE, CAVE.DO_NOT_BLOCK
                | CAVE.MODE_INDEPENDENT);
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
        setText("Suite Manager");

        retrieveDataFromDB();
        createSuiteTableGroup();
        createBottomButtons();

        suiteConfigGroup.populateSuiteTable(suiteList);
    }

    /**
     * Create the Suite group.
     */
    private void createSuiteTableGroup() {
        suiteConfigGroup = new SuiteConfigGroup(shell, " Available Suites: ",
                SuiteGroupType.SUITE_MGR, null, 550, 150);
        suiteConfigGroup.setCallBackAction(new ISuiteSelection() {
            @Override
            public void suiteSelected(Suite suite) {
            }

            @Override
            public void suitesUpdated() {
                retrieveDataFromDB();
                suiteConfigGroup.populateSuiteTable(suiteList);
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
