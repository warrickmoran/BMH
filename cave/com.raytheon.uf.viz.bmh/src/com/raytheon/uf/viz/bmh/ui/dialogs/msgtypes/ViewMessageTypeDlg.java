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
package com.raytheon.uf.viz.bmh.ui.dialogs.msgtypes;

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
import com.raytheon.uf.viz.bmh.ui.program.ProgramTable;
import com.raytheon.uf.viz.bmh.ui.program.SuiteTable;
import com.raytheon.viz.ui.dialogs.CaveSWTDialog;

/**
 * 
 * Dialog to show the Message Type's associated Programs and Suites.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 29, 2014  #3420     lvenable     Initial creation
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */
public class ViewMessageTypeDlg extends CaveSWTDialog {

    /** Program table. */
    private ProgramTable programTable;

    /** Suite table. */
    private SuiteTable suiteTable;

    /**
     * Constructor.
     * 
     * @param parentShell
     *            Parent shell.
     */
    public ViewMessageTypeDlg(Shell parentShell) {
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
        setText("Message Type Information");

        createMessageTypeInfoControls();
        DialogUtility.addSeparator(shell, SWT.HORIZONTAL);
        createProgramControls();
        createSuiteControls();
        createBottomButton();
    }

    /**
     * Create the controls for the message type information.
     */
    private void createMessageTypeInfoControls() {
        Composite msgTypeInfoComp = new Composite(shell, SWT.NONE);
        msgTypeInfoComp.setLayout(new GridLayout(2, false));
        msgTypeInfoComp.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true,
                false));

        Label nameLbl = new Label(msgTypeInfoComp, SWT.NONE);
        nameLbl.setText("Message Type: ");

        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        Label messageTypeNameLbl = new Label(msgTypeInfoComp, SWT.NONE);
        messageTypeNameLbl.setText("Message Type - 1"); // TODO : use real data
        messageTypeNameLbl.setLayoutData(gd);

        Label titleLbl = new Label(msgTypeInfoComp, SWT.NONE);
        titleLbl.setText("Title: ");

        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        Label msgTypeTitleLbl = new Label(msgTypeInfoComp, SWT.NONE);
        msgTypeTitleLbl.setText("Message Type - 1 Title"); // TODO : use real
                                                           // data
        msgTypeTitleLbl.setLayoutData(gd);
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
     * Create the Suite controls.
     */
    private void createSuiteControls() {
        Composite controlComp = new Composite(shell, SWT.NONE);
        controlComp.setLayout(new GridLayout(1, false));
        controlComp.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true,
                false));

        GridData gd = new GridData();
        gd.verticalIndent = 10;
        Label nameLbl = new Label(controlComp, SWT.NONE);
        nameLbl.setText("Associated Suites:");
        nameLbl.setLayoutData(gd);

        suiteTable = new SuiteTable(controlComp, 500, 100);
        populateSuiteTable();
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

    private void populateSuiteTable() {
        List<TableColumnData> columnNames = new ArrayList<TableColumnData>();
        TableColumnData tcd = new TableColumnData("Suite Name", 150);
        columnNames.add(tcd);
        tcd = new TableColumnData("Category");
        columnNames.add(tcd);

        TableData td = new TableData(columnNames);

        TableRowData trd = new TableRowData();

        trd.addTableCellData(new TableCellData("Suite - 1"));
        trd.addTableCellData(new TableCellData("General"));

        td.addDataRow(trd);

        trd = new TableRowData();

        trd.addTableCellData(new TableCellData("Suite - 2"));
        trd.addTableCellData(new TableCellData("Exclusive"));

        td.addDataRow(trd);

        suiteTable.populateTable(td);
    }
}
