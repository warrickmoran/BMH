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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;

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
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */
public class ViewSuiteDlg extends CaveSWTDialog {

    /** Program table. */
    private ProgramTable programTable;

    /** Message Type table. */
    private MsgTypeTable msgTypeTable;

    /**
     * Constructor.
     * 
     * @param parentShell
     *            Parent shell.
     */
    public ViewSuiteDlg(Shell parentShell) {
        super(parentShell, SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL,
                CAVE.DO_NOT_BLOCK | CAVE.MODE_INDEPENDENT);
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
        setText("Add Existing Suites");

        createSuiteInfoControls();
        DialogUtility.addSeparator(shell, SWT.HORIZONTAL);
        createProgramControls();
        createMessageTypeControls();
        createBottomButton();
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
        suiteNameLbl.setText("Omaha_Suite");
        suiteNameLbl.setLayoutData(gd);

        Label categoryLbl = new Label(suiteInfoComp, SWT.NONE);
        categoryLbl.setText("Suite Category: ");

        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        Label suiteCategoryLbl = new Label(suiteInfoComp, SWT.NONE);
        suiteCategoryLbl.setText("Exclusive");
        suiteCategoryLbl.setLayoutData(gd);
    }

    /**
     * Create program controls.
     */
    private void createProgramControls() {
        Composite controlComp = new Composite(shell, SWT.NONE);
        controlComp.setLayout(new GridLayout(1, false));
        controlComp.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true,
                false));

        Label nameLbl = new Label(controlComp, SWT.NONE);
        nameLbl.setText("Associated Programs:");

        programTable = new ProgramTable(controlComp, 500, 100);
        populateProgramTable();
    }

    /**
     * Create the message type controls.
     */
    private void createMessageTypeControls() {
        Composite controlComp = new Composite(shell, SWT.NONE);
        controlComp.setLayout(new GridLayout(1, false));
        controlComp.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true,
                false));

        GridData gd = new GridData();
        gd.verticalIndent = 10;
        Label nameLbl = new Label(controlComp, SWT.NONE);
        nameLbl.setText("Associated Message Types:");
        nameLbl.setLayoutData(gd);

        msgTypeTable = new MsgTypeTable(controlComp, 500, 100);
        populateMsgTypeTable();
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

    /**********************************************************************
     * 
     * TODO: remove dummy code
     * 
     */

    private void populateProgramTable() {

        List<TableColumnData> columnNames = new ArrayList<TableColumnData>();
        TableColumnData tcd = new TableColumnData("Program Name", 150);
        columnNames.add(tcd);
        tcd = new TableColumnData("Title");
        columnNames.add(tcd);

        TableData td = new TableData(columnNames);

        TableRowData trd = new TableRowData();

        trd.addTableCellData(new TableCellData("Program - 1"));
        trd.addTableCellData(new TableCellData("Sample Program 1"));

        td.addDataRow(trd);

        trd = new TableRowData();

        trd.addTableCellData(new TableCellData("Program - 2"));
        trd.addTableCellData(new TableCellData("Sample Program 2"));

        td.addDataRow(trd);

        programTable.populateTable(td);
    }

    private void populateMsgTypeTable() {
        List<TableColumnData> columnNames = new ArrayList<TableColumnData>();
        TableColumnData tcd = new TableColumnData("Message Type", 150);
        columnNames.add(tcd);
        tcd = new TableColumnData("Message Title");
        columnNames.add(tcd);
        TableData td = new TableData(columnNames);

        TableRowData trd = new TableRowData();

        trd.addTableCellData(new TableCellData("MessageType - 1"));
        trd.addTableCellData(new TableCellData("MessageType - 1 - Description"));

        td.addDataRow(trd);

        trd = new TableRowData();

        trd.addTableCellData(new TableCellData("MessageType - 2"));
        trd.addTableCellData(new TableCellData("MessageType - 2 - Description"));

        td.addDataRow(trd);

        msgTypeTable.populateTable(td);
    }
}
