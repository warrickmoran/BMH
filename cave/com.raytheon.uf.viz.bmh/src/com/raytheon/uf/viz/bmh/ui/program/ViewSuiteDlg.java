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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.common.bmh.datamodel.msg.Program;
import com.raytheon.uf.common.bmh.datamodel.msg.Suite;
import com.raytheon.uf.common.bmh.datamodel.msg.SuiteMessage;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.bmh.ui.common.table.TableCellData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableColumnData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableRowData;
import com.raytheon.uf.viz.bmh.ui.common.utility.DialogUtility;
import com.raytheon.uf.viz.bmh.ui.dialogs.msgtypes.MsgTypeTable;
import com.raytheon.viz.ui.dialogs.CaveSWTDialog;

/**
 * Dialog to show the Suite's associated Programs and Message Types.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 21, 2014  #3174     lvenable     Initial creation
 * Jul 24, 2014  #3433     lvenable     Updated for Suite manager
 * Aug 12, 2014  #3490     lvenable     Update to use data from the database.
 * Aug 15, 2014  #3490     lvenable     Reworked to use the data manager.
 * Aug 22, 2014  #3490     lvenable     Added resize and minimum size.
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */
public class ViewSuiteDlg extends CaveSWTDialog {

    /** Status handler for reporting errors. */
    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(ViewSuiteDlg.class);

    /** Programs associated with the message type. */
    List<Program> assocProgs = new ArrayList<Program>();

    /** The selected suite to view the information. */
    private Suite selectedSuite = null;

    /** Program table. */
    private ProgramTable programTable;

    /** Message Type table. */
    private MsgTypeTable msgTypeTable;

    /**
     * Constructor.
     * 
     * @param parentShell
     *            Parent shell.
     * @param selectedSuite
     *            The selected suite.
     */
    public ViewSuiteDlg(Shell parentShell, Suite selectedSuite) {
        super(parentShell, SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL | SWT.RESIZE,
                CAVE.DO_NOT_BLOCK | CAVE.MODE_INDEPENDENT);

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
    protected void opened() {
        shell.setMinimumSize(shell.getSize());
    }

    @Override
    protected void initializeComponents(Shell shell) {
        setText("Suite Information");

        retrieveDataFromDB();

        createSuiteInfoControls();
        DialogUtility.addSeparator(shell, SWT.HORIZONTAL);
        createProgramControls();
        createMessageTypeControls();
        createBottomButton();

        populateAssocProgramsTable();
        populateAssocMsgTypesTable();
    }

    /**
     * Create the controls for the suite information.
     */
    private void createSuiteInfoControls() {
        Composite suiteInfoComp = new Composite(shell, SWT.NONE);
        suiteInfoComp.setLayout(new GridLayout(2, false));
        suiteInfoComp.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true,
                false));

        Label nameLbl = new Label(suiteInfoComp, SWT.NONE);
        nameLbl.setText("Suite Name: ");

        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        Label suiteNameLbl = new Label(suiteInfoComp, SWT.NONE);
        suiteNameLbl.setText(selectedSuite.getName());
        suiteNameLbl.setLayoutData(gd);

        Label categoryLbl = new Label(suiteInfoComp, SWT.NONE);
        categoryLbl.setText("Suite Category: ");

        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        Label suiteCategoryLbl = new Label(suiteInfoComp, SWT.NONE);
        suiteCategoryLbl.setText(selectedSuite.getType().name());
        suiteCategoryLbl.setLayoutData(gd);
    }

    /**
     * Create program controls.
     */
    private void createProgramControls() {
        Composite controlComp = new Composite(shell, SWT.NONE);
        controlComp.setLayout(new GridLayout(1, false));
        controlComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        GridData gd = new GridData();
        gd.horizontalIndent = 5;
        Label nameLbl = new Label(controlComp, SWT.NONE);
        nameLbl.setText("Associated Programs:");
        nameLbl.setLayoutData(gd);

        programTable = new ProgramTable(controlComp, 500, 100);
    }

    /**
     * Create the message type controls.
     */
    private void createMessageTypeControls() {
        Composite controlComp = new Composite(shell, SWT.NONE);
        controlComp.setLayout(new GridLayout(1, false));
        controlComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        GridData gd = new GridData();
        gd.verticalIndent = 10;
        gd.horizontalIndent = 5;
        Label nameLbl = new Label(controlComp, SWT.NONE);
        nameLbl.setText("Associated Message Types:");
        nameLbl.setLayoutData(gd);

        msgTypeTable = new MsgTypeTable(controlComp, 500, 100);
    }

    /**
     * Create the bottom action buttons.
     */
    private void createBottomButton() {
        Composite buttonComp = new Composite(shell, SWT.NONE);
        buttonComp.setLayout(new GridLayout(1, false));
        buttonComp.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true,
                false));

        GridData gd = new GridData(SWT.CENTER, SWT.DEFAULT, true, false);
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
     * Retrieve the data from the database.
     */
    private void retrieveDataFromDB() {

        /*
         * TODO: Query should be written to retrieve the data for this specific
         * suite since you are already going to the db, no reason to do client
         * side filtering.
         */

        // All Programs with suite information.
        List<Program> allProgramsArray = null;
        ProgramDataManager pdm = new ProgramDataManager();

        try {
            allProgramsArray = pdm.getProgramSuites();
        } catch (Exception e) {
            statusHandler.error(
                    "Error retrieving program data from the database: ", e);
        }

        findAssociatedPrograms(allProgramsArray);
    }

    /**
     * Loop through all of the programs and find the ones that contain the
     * associated suites that contain the message type.
     */
    private void findAssociatedPrograms(List<Program> allProgramsArray) {
        for (Program p : allProgramsArray) {
            List<Suite> suitesInProgram = p.getSuites();
            for (Suite progSuite : suitesInProgram) {
                // If a Suite is found, add the program and continue to the
                // next program.
                if (progSuite.getId() == selectedSuite.getId()) {
                    assocProgs.add(p);
                    break;
                }

            }
        }
    }

    /**
     * Populate the programs table.
     */
    private void populateAssocProgramsTable() {
        List<TableColumnData> columnNames = new ArrayList<TableColumnData>();
        TableColumnData tcd = new TableColumnData("Program Name");
        columnNames.add(tcd);

        TableData td = new TableData(columnNames);

        for (Program p : assocProgs) {
            TableRowData trd = new TableRowData();

            trd.addTableCellData(new TableCellData(p.getName()));
            td.addDataRow(trd);
        }

        programTable.populateTable(td);
    }

    /**
     * Populate the associated message types table.
     */
    private void populateAssocMsgTypesTable() {

        List<TableColumnData> columnNames = new ArrayList<TableColumnData>();
        TableColumnData tcd = new TableColumnData("Message Type", 150);
        columnNames.add(tcd);
        tcd = new TableColumnData("Message Title");
        columnNames.add(tcd);
        tcd = new TableColumnData("Trigger");
        columnNames.add(tcd);
        TableData msgTypeTableData = new TableData(columnNames);

        List<SuiteMessage> suiteMessageArray = selectedSuite.getSuiteMessages();

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

        msgTypeTable.populateTable(msgTypeTableData);
    }
}
