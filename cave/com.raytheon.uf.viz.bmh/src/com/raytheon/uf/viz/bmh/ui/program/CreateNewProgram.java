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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.raytheon.uf.viz.bmh.ui.common.table.TableCellData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableColumnData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableRowData;
import com.raytheon.uf.viz.bmh.ui.common.utility.DialogUtility;
import com.raytheon.uf.viz.bmh.ui.program.AddSuitesDlg.SuiteDialogType;
import com.raytheon.uf.viz.bmh.ui.program.CreateEditSuiteDlg.DialogType;
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
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */
public class CreateNewProgram extends CaveSWTDialog {

    /** Save button. */
    private Button saveBtn;

    /** Table of selected suites. */
    private SuiteTable selectedSuiteTable;

    /** Program text field. */
    private Text programTF;

    /**
     * Constructor.
     * 
     * @param parentShell
     *            Parent shell.
     */
    public CreateNewProgram(Shell parentShell) {
        super(parentShell, SWT.DIALOG_TRIM | SWT.MIN | SWT.PRIMARY_MODAL,
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
        Group selectedSuitesGrp = new Group(shell, SWT.SHADOW_OUT);
        GridLayout gl = new GridLayout(1, false);
        selectedSuitesGrp.setLayout(gl);
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        selectedSuitesGrp.setLayoutData(gd);
        selectedSuitesGrp.setText(" Selected Suites: ");

        selectedSuiteTable = new SuiteTable(selectedSuitesGrp, 550, 150);

        populateSelectedSuiteTable();

        Composite btnComp = new Composite(selectedSuitesGrp, SWT.NONE);
        btnComp.setLayout(new GridLayout(2, false));
        btnComp.setLayoutData(new GridData(SWT.CENTER, SWT.DEFAULT, true, false));

        int minButtonWidth = 80;

        gd = new GridData(minButtonWidth, SWT.DEFAULT);
        Button addNewSuiteBtn = new Button(btnComp, SWT.PUSH);
        addNewSuiteBtn.setText("New...");
        addNewSuiteBtn.setLayoutData(gd);
        addNewSuiteBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                CreateEditSuiteDlg csd = new CreateEditSuiteDlg(shell,
                        DialogType.CREATE, false, null);
                csd.open();
            }
        });

        gd = new GridData();
        gd.minimumWidth = minButtonWidth;
        Button addExistingBtn = new Button(btnComp, SWT.PUSH);
        addExistingBtn.setText("Add Existing...");
        addExistingBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                AddSuitesDlg asd = new AddSuitesDlg(getShell(),
                        SuiteDialogType.ADD_COPY);
                asd.open();
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

    /**********************************************************************
     * 
     * TODO: remove dummy code
     * 
     */

    // TODO: remove when bringing in selected suites from other dialogs.

    private void populateSelectedSuiteTable() {

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

        selectedSuiteTable.populateTable(td);
    }
}
