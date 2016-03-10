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
package com.raytheon.uf.viz.bmh.ui.dialogs.broadcastcycle;

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

import com.raytheon.uf.common.bmh.datamodel.msg.Suite;
import com.raytheon.uf.viz.bmh.ui.common.table.GenericTable;
import com.raytheon.uf.viz.bmh.ui.common.table.TableCellData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableColumnData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableRowData;
import com.raytheon.viz.ui.dialogs.CaveSWTDialog;

/**
 * Suite selection dialog.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 19, 2014    3432    mpduff      Initial creation
 * Sep 25, 2014    3589    dgilling    Return Suite object to caller.
 * Oct 10, 2014    3646    rferrel     Convert tableComp to GenericTable.
 * Oct 15, 2014    3716    bkowal      Allow updates of the suite list table.
 * Mar 31, 2015    4340    bkowal      Allow optional filtering of the currently
 *                                     active suite.
 * Jan 25, 2016    5054    randerso    Change to subclass CaveSWTDialog
 * Mar 10, 2016    5465    tgurney     Add missing trim button style
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class SuiteListDlg extends CaveSWTDialog {

    private GenericTable tableComp;

    private List<Suite> suiteList;

    private String activeSuite;

    private Button okBtn;

    private Button cancelBtn;

    public SuiteListDlg(Shell parent, List<Suite> suiteList, String activeSuite) {
        super(parent, SWT.MIN | SWT.MAX | SWT.RESIZE, CAVE.INDEPENDENT_SHELL
                | CAVE.PERSPECTIVE_INDEPENDENT);
        this.suiteList = suiteList;
        setText("Change Suite");
        this.activeSuite = activeSuite;
    }

    public void updateSuites(List<Suite> suiteList) {
        /* Disable controls during update. */
        this.okBtn.setEnabled(false);
        this.cancelBtn.setEnabled(false);
        this.suiteList = suiteList;
        this.populateTable();
        /* pre-select the first row. */
        this.tableComp.select(0);
        /* Re-enable controls. */
        this.okBtn.setEnabled(true);
        this.cancelBtn.setEnabled(true);
    }

    public void updateActiveSuite(String activeSuite) {
        if (this.activeSuite != null && this.activeSuite.equals(activeSuite)) {
            return;
        }
        this.activeSuite = activeSuite;
        this.updateSuites(this.suiteList);
    }

    @Override
    protected Layout constructShellLayout() {
        GridLayout mainLayout = new GridLayout(1, false);
        mainLayout.horizontalSpacing = 0;
        mainLayout.verticalSpacing = 0;
        return mainLayout;
    }

    @Override
    protected Object constructShellLayoutData() {
        return new GridData(SWT.FILL, SWT.FILL, true, true);
    }

    @Override
    protected void initializeComponents(Shell shell) {
        Label l = new Label(shell, SWT.NONE);
        l.setText("Select a Suite:");

        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.heightHint = 150;
        GridLayout gl = new GridLayout(1, false);
        gl.horizontalSpacing = 0;
        gl.marginWidth = 0;
        tableComp = new GenericTable(shell, SWT.BORDER | SWT.V_SCROLL);
        tableComp.setLayout(gl);
        tableComp.setLayoutData(gd);

        gd = new GridData(SWT.CENTER, SWT.DEFAULT, true, false);
        gl = new GridLayout(2, false);
        Composite btnComp = new Composite(shell, SWT.NONE);
        btnComp.setLayout(gl);
        btnComp.setLayoutData(gd);

        gd = new GridData(75, SWT.DEFAULT);
        okBtn = new Button(btnComp, SWT.PUSH);
        okBtn.setText("OK");
        okBtn.setLayoutData(gd);
        okBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                handleOk();
                close();
            }
        });

        gd = new GridData(75, SWT.DEFAULT);
        cancelBtn = new Button(btnComp, SWT.PUSH);
        cancelBtn.setText("Cancel");
        cancelBtn.setLayoutData(gd);
        cancelBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                close();
            }
        });

        populateTable();
        shell.setMinimumSize(450, 300);
    }

    private void handleOk() {
        List<TableRowData> selections = tableComp.getSelection();
        Object data = selections.get(0).getData();
        Suite selectedSuite = (Suite) data;
        setReturnValue(selectedSuite);
    }

    private void populateTable() {
        List<TableColumnData> columns = new ArrayList<TableColumnData>(2);
        TableColumnData tcd = new TableColumnData("Suite", 225);
        TableColumnData tcd2 = new TableColumnData("Category");
        columns.add(tcd);
        columns.add(tcd2);

        TableData td = new TableData(columns);

        for (Suite s : suiteList) {
            if (this.activeSuite != null
                    && this.activeSuite.equals(s.getName())) {
                continue;
            }
            TableRowData trd = new TableRowData();
            trd.addTableCellData(new TableCellData(s.getName()));
            trd.addTableCellData(new TableCellData(s.getType().name()));
            trd.setData(s);
            td.addDataRow(trd);
        }

        tableComp.populateTable(td);
    }
}
