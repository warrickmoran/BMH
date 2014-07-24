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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.raytheon.uf.viz.bmh.ui.common.table.TableCellData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableColumnData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableRowData;
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
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */
public class AddSuitesDlg extends CaveSWTDialog {

    /** Suite name text field. */
    private Text suiteNameTF;

    /** Suite table. */
    private AddSuiteTable suiteTable;

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
    protected void disposed() {
    }

    @Override
    protected void initializeComponents(Shell shell) {
        if (dialogType == SuiteDialogType.ADD_COPY) {
            setText("Add/Copy Existing Suites");
        } else if (dialogType == SuiteDialogType.COPY_ONLY) {
            setText("Copy Existing Suite");
        }

        createOptionControls();
        createSuitesTable();
        createBottomButtons();
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
                        suiteTable.setMultipleSelection(true);
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
                        suiteTable.setMultipleSelection(false);
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
        Group suiteGroup = new Group(shell, SWT.SHADOW_OUT);
        GridLayout gl = new GridLayout(1, false);
        suiteGroup.setLayout(gl);
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        suiteGroup.setLayoutData(gd);
        suiteGroup.setText(" Select Suite to Add: ");

        int tableStyle = SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI;
        suiteTable = new AddSuiteTable(suiteGroup, tableStyle, 600, 150);

        populateSuiteTable();

        if (dialogType == SuiteDialogType.COPY_ONLY) {
            suiteTable.setMultipleSelection(false);
        }

        gd = new GridData(SWT.CENTER, SWT.DEFAULT, true, false);
        Button viewBtn = new Button(suiteGroup, SWT.PUSH);
        viewBtn.setText(" View Suite Information... ");
        viewBtn.setLayoutData(gd);
        viewBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                ViewSuiteDlg vsd = new ViewSuiteDlg(shell);
                vsd.open();
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

    /**********************************************************************
     * 
     * TODO: remove dummy code
     * 
     */

    private void populateSuiteTable() {

        List<TableColumnData> columnNames = new ArrayList<TableColumnData>();
        TableColumnData tcd = new TableColumnData("Suite Name", 200);
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
