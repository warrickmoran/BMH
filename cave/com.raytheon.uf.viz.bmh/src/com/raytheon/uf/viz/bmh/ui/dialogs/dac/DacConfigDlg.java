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
package com.raytheon.uf.viz.bmh.ui.dialogs.dac;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.bmh.ui.common.table.GenericTable;
import com.raytheon.uf.viz.bmh.ui.common.table.TableCellData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableColumnData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableRowData;
import com.raytheon.uf.viz.bmh.ui.dialogs.AbstractBMHDialog;
import com.raytheon.uf.viz.bmh.ui.dialogs.dac.CreateEditDacConfigDlg.DialogType;
import com.raytheon.viz.ui.dialogs.ICloseCallback;

/**
 * DAC configuration dialog.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 23, 2014  #3660     lvenable     Initial creation
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */

public class DacConfigDlg extends AbstractBMHDialog {

    /** Status handler for reporting errors. */
    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(DacConfigDlg.class);

    /** DACs table. */
    private GenericTable dacTable;

    /** DAC table data. */
    private TableData dacTableData = null;

    /** Edit button. */
    private Button editBtn;

    /** Delete button. */
    private Button deleteBtn;

    /**
     * Constructor.
     * 
     * @param parentShell
     *            Parent shell.
     * @param dlgMap
     *            Map to add this dialog to for closing purposes.
     */
    public DacConfigDlg(Shell parentShell, Map<AbstractBMHDialog, String> dlgMap) {
        super(dlgMap, "DAC Configuration Dialog", parentShell, SWT.DIALOG_TRIM
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

    /**
     * Method to check if the dialog can close.
     * 
     * For example: if there are items that are unsaved then the user should be
     * prompted that the dialog has unsaved items and be given the opportunity
     * to prevent the dialog from closing.
     */
    @Override
    public boolean okToClose() {
        if (isDisposed()) {
            return true;
        }

        return false;
    }

    @Override
    protected void initializeComponents(Shell shell) {
        setText("DAC Configuration");

        createAvailableDacsGroup();
        createBottomActionButtons();
        populateDacTable();
    }

    /**
     * Create the available DACs table and controls.
     */
    private void createAvailableDacsGroup() {
        Group dacGrp = new Group(shell, SWT.SHADOW_OUT);
        GridLayout gl = new GridLayout(1, false);
        dacGrp.setLayout(gl);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        dacGrp.setLayoutData(gd);
        dacGrp.setText(" Available DACs: ");

        dacTable = new GenericTable(dacGrp, SWT.SINGLE, 700, 175);

        /*
         * Action buttons.
         */
        Composite buttonComp = new Composite(dacGrp, SWT.NONE);
        buttonComp.setLayout(new GridLayout(3, false));
        buttonComp.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true,
                false));

        int buttonWidth = 90;

        gd = new GridData(SWT.RIGHT, SWT.DEFAULT, true, false);
        gd.widthHint = buttonWidth;
        Button newBtn = new Button(buttonComp, SWT.PUSH);
        newBtn.setText("New...");
        newBtn.setLayoutData(gd);
        newBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                CreateEditDacConfigDlg createDacConfigDlg = new CreateEditDacConfigDlg(
                        shell, DialogType.CREATE);
                createDacConfigDlg.setCloseCallback(new ICloseCallback() {

                    @Override
                    public void dialogClosed(Object returnValue) {
                        // TODO : add callback code for creating
                    }
                });
                createDacConfigDlg.open();
            }
        });

        gd = new GridData();
        gd.widthHint = buttonWidth;
        editBtn = new Button(buttonComp, SWT.PUSH);
        editBtn.setText("Edit...");
        editBtn.setLayoutData(gd);
        editBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                CreateEditDacConfigDlg createDacConfigDlg = new CreateEditDacConfigDlg(
                        shell, DialogType.EDIT);
                createDacConfigDlg.setCloseCallback(new ICloseCallback() {

                    @Override
                    public void dialogClosed(Object returnValue) {
                        // TODO : add callback code for editing
                    }
                });
                createDacConfigDlg.open();
            }
        });

        gd = new GridData(SWT.LEFT, SWT.DEFAULT, true, false);
        gd.widthHint = buttonWidth;
        deleteBtn = new Button(buttonComp, SWT.PUSH);
        deleteBtn.setText("Delete");
        deleteBtn.setLayoutData(gd);
        deleteBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // TODO : add delete code
            }
        });
    }

    /**
     * Create the bottom Close button.
     */
    private void createBottomActionButtons() {
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

    /**
     * Populate the DAC table.
     */
    private void populateDacTable() {
        List<TableColumnData> columnNames = new ArrayList<TableColumnData>();
        TableColumnData tcd = new TableColumnData("DAC Id", 70);
        tcd.setAlignment(SWT.LEFT);
        columnNames.add(tcd);
        tcd = new TableColumnData("DAC IP", 110);
        tcd.setAlignment(SWT.LEFT);
        columnNames.add(tcd);
        tcd = new TableColumnData("Receive Port", 110);
        tcd.setAlignment(SWT.LEFT);
        columnNames.add(tcd);
        tcd = new TableColumnData("Receive Address", 110);
        tcd.setAlignment(SWT.LEFT);
        columnNames.add(tcd);
        tcd = new TableColumnData("Channels");
        tcd.setAlignment(SWT.LEFT);
        columnNames.add(tcd);

        dacTableData = new TableData(columnNames);

        populateDacTableData();
        dacTable.populateTable(dacTableData);
    }

    /**
     * Populate the DAC table data.
     */
    private void populateDacTableData() {
        /*
         * TODO : Use real data when implementing the dialog.
         */
        TableRowData trd = new TableRowData();
        trd.addTableCellData(new TableCellData(1, "%d"));
        trd.addTableCellData(new TableCellData("192.168.21.41"));
        trd.addTableCellData(new TableCellData("12345"));
        trd.addTableCellData(new TableCellData("192.168.88.88"));
        trd.addTableCellData(new TableCellData("12345, 23456, 34567, 45678"));
        dacTableData.addDataRow(trd);

        trd = new TableRowData();
        trd.addTableCellData(new TableCellData(2, "%d"));
        trd.addTableCellData(new TableCellData("192.168.33.33"));
        trd.addTableCellData(new TableCellData("23456"));
        trd.addTableCellData(new TableCellData("192.168.77.77"));
        trd.addTableCellData(new TableCellData("12345, 23456, 34567, 45678"));
        dacTableData.addDataRow(trd);
    }
}
