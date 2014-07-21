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

import com.raytheon.uf.viz.bmh.ui.common.table.TableCellData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableColumnData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableRowData;
import com.raytheon.uf.viz.bmh.ui.program.CreateEditSuiteDlg.DialogType;

/**
 * 
 * Group/Composite containing suite table and controls.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 20, 2014  #3174     lvenable     Initial creation
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */
public class SuiteConfigGroup extends Composite {

    /** Prefix for the suite group text. */
    private final String suiteGrpPrefix = " Suites in Program: ";

    /** Suite group. */
    private Group suiteGroup;

    /** Suite table. */
    private SuiteTable suiteTable;

    /** Parent composite. */
    private Composite parentComp;

    /**
     * Constructor.
     * 
     * @param parentComp
     *            Parent composite.
     */
    public SuiteConfigGroup(Composite parentComp) {
        super(parentComp, SWT.NONE);

        this.parentComp = parentComp;
        init();
    }

    /**
     * Initialize method.
     */
    private void init() {
        GridLayout gl = new GridLayout(1, false);
        this.setLayout(gl);
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        this.setLayoutData(gd);

        suiteGroup = new Group(this, SWT.SHADOW_OUT);
        gl = new GridLayout(1, false);
        suiteGroup.setLayout(gl);
        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        suiteGroup.setLayoutData(gd);
        suiteGroup.setText(suiteGrpPrefix);

        createFilterControls();
        createTable();
        createSuiteControls();

    }

    /**
     * Create the filter controls.
     */
    private void createFilterControls() {
        Composite filterComp = new Composite(suiteGroup, SWT.NONE);
        GridLayout gl = new GridLayout(5, false);
        filterComp.setLayout(gl);
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        filterComp.setLayoutData(gd);

        Label filerLbl = new Label(filterComp, SWT.NONE);
        filerLbl.setText("Filter Category: ");

        int indent = 10;

        gd = new GridData();
        gd.horizontalIndent = indent;
        Button filterAllRdo = new Button(filterComp, SWT.RADIO);
        filterAllRdo.setText("All");
        filterAllRdo.setSelection(true);
        filterAllRdo.setLayoutData(gd);

        gd = new GridData();
        gd.horizontalIndent = indent;
        Button filterGeneralRdo = new Button(filterComp, SWT.RADIO);
        filterGeneralRdo.setText("General");
        filterGeneralRdo.setLayoutData(gd);

        gd = new GridData();
        gd.horizontalIndent = indent;
        Button filterHighRdo = new Button(filterComp, SWT.RADIO);
        filterHighRdo.setText("High");
        filterHighRdo.setLayoutData(gd);

        gd = new GridData();
        gd.horizontalIndent = indent;
        Button filterExclusiveRdo = new Button(filterComp, SWT.RADIO);
        filterExclusiveRdo.setText("Exclusive");
        filterExclusiveRdo.setLayoutData(gd);

    }

    /**
     * Create the suite table.
     */
    private void createTable() {
        suiteTable = new SuiteTable(suiteGroup, 400, 150);
        populateSuiteTable();
    }

    /**
     * Create suite controls.
     */
    private void createSuiteControls() {
        Composite suiteControlComp = new Composite(suiteGroup, SWT.NONE);
        GridLayout gl = new GridLayout(4, false);
        suiteControlComp.setLayout(gl);
        GridData gd = new GridData(SWT.CENTER, SWT.DEFAULT, true, false);
        suiteControlComp.setLayoutData(gd);

        int minButtonWidth = 80;

        gd = new GridData(minButtonWidth, SWT.DEFAULT);
        Button addNewSuiteBtn = new Button(suiteControlComp, SWT.PUSH);
        addNewSuiteBtn.setText("New...");
        addNewSuiteBtn.setLayoutData(gd);
        addNewSuiteBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                CreateEditSuiteDlg csd = new CreateEditSuiteDlg(parentComp
                        .getShell(), DialogType.CREATE);
                csd.open();
            }
        });

        gd = new GridData();
        gd.minimumWidth = minButtonWidth;
        Button addExistingBtn = new Button(suiteControlComp, SWT.PUSH);
        addExistingBtn.setText(" Add Existing... ");
        addExistingBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                AddSuitesDlg asd = new AddSuitesDlg(getShell());
                asd.open();
            }
        });

        gd = new GridData(minButtonWidth, SWT.DEFAULT);
        Button editSuiteBtn = new Button(suiteControlComp, SWT.PUSH);
        editSuiteBtn.setText("Edit...");
        editSuiteBtn.setLayoutData(gd);
        editSuiteBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                CreateEditSuiteDlg csd = new CreateEditSuiteDlg(parentComp
                        .getShell(), DialogType.EDIT);
                csd.open();
                // NewEditSuiteDlg nsd = new NewEditSuiteDlg(
                // parentComp.getShell(), ActionType.EDIT);
                // nsd.open();
            }
        });

        gd = new GridData(minButtonWidth, SWT.DEFAULT);
        Button deleteSuiteBtn = new Button(suiteControlComp, SWT.PUSH);
        deleteSuiteBtn.setText("Delete");
        deleteSuiteBtn.setLayoutData(gd);
    }

    /**
     * Update the suite name in the Group text.
     * 
     * @param suiteName
     */
    public void updateSuiteGroupText(String suiteName) {
        suiteGroup.setText(suiteGrpPrefix + suiteName);
    }

    /**********************************************************************
     * 
     * TODO: remove dummy code
     * 
     */

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
