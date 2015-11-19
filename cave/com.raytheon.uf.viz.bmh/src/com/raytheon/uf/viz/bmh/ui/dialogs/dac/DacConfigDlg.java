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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
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

import com.raytheon.uf.common.bmh.datamodel.dac.Dac;
import com.raytheon.uf.common.bmh.request.DacConfigResponse;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.bmh.ui.common.table.GenericTable;
import com.raytheon.uf.viz.bmh.ui.common.table.ITableActionCB;
import com.raytheon.uf.viz.bmh.ui.common.table.TableCellData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableColumnData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableRowData;
import com.raytheon.uf.viz.bmh.ui.common.utility.DialogUtility;
import com.raytheon.uf.viz.bmh.ui.common.utility.InputTextDlg;
import com.raytheon.uf.viz.bmh.ui.dialogs.AbstractBMHDialog;
import com.raytheon.uf.viz.bmh.ui.dialogs.DlgInfo;
import com.raytheon.uf.viz.bmh.ui.dialogs.dac.CreateEditDacConfigDlg.DialogType;
import com.raytheon.viz.core.mode.CAVEMode;
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
 * Oct 08, 2014  #3479     lvenable     Changed MODE_INDEPENDENT to PERSPECTIVE_INDEPENDENT.
 * Oct 19, 2014  #3699     mpduff       Implement dialog
 * Nov 11, 2014  3413      rferrel     Use DlgInfo to get title.
 * Feb 03, 2015  4056      bsteffen     Pass the data manager to the create/edit dialog.
 * Jun 05, 2015  4490      rjpeter     Updated constructor.
 * Sep 24, 2015  4922      bkowal       Prevent editing / adding / deleting dacs when not
 *                                      in operational mode.
 * Nov 11, 2015  5113      bkowal       Preparing dialog to support auto-configuration of DACs.
 * Nov 12, 2015  5113      bkowal       Support reboot and failover of DACs.
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */

public class DacConfigDlg extends AbstractBMHDialog implements ITableActionCB {

    public static final String MANUFACTURER_DAC_IP = "10.2.69.1";

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

    /** Reboot button. */
    private Button rebootBtn;

    /** Failover button. */
    private Button failoverBtn;

    /** Data access manager */
    private DacDataManager dataManager;

    private DacConfigResponse latestConfigResponse;

    /**
     * Constructor.
     * 
     * @param parentShell
     *            Parent shell.
     */
    public DacConfigDlg(Shell parentShell) {
        super(parentShell, SWT.DIALOG_TRIM | SWT.MIN | SWT.RESIZE,
                CAVE.DO_NOT_BLOCK | CAVE.PERSPECTIVE_INDEPENDENT);
        setText(DlgInfo.DAC_CONFIGURATION.getTitle());
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
        dataManager = new DacDataManager();
        createDacTable();
        createBottomActionButtons();
        populateDacTable();
    }

    /**
     * Create the available DACs table and controls.
     */
    private void createDacTable() {
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
        buttonComp.setLayout(new GridLayout(5, false));
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
                        shell, DialogType.CREATE, dataManager);
                createDacConfigDlg.setCloseCallback(new ICloseCallback() {

                    @Override
                    public void dialogClosed(Object returnValue) {
                        Dac dac = (Dac) returnValue;
                        if (dac != null) {
                            updateTable(dac);
                        }
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
                        shell, DialogType.EDIT, dataManager);
                createDacConfigDlg.setCloseCallback(new ICloseCallback() {

                    @Override
                    public void dialogClosed(Object returnValue) {
                        Dac dac = (Dac) returnValue;
                        if (dac != null) {
                            updateTable(dac);
                        }
                    }
                });
                if (dacTable.getSelectedIndex() >= 0) {
                    TableRowData rowdata = dacTableData.getTableRow(dacTable
                            .getSelectedIndex());
                    createDacConfigDlg.setDac((Dac) rowdata.getData());
                    createDacConfigDlg.open();
                }
            }
        });

        gd = new GridData();
        gd.widthHint = buttonWidth;
        deleteBtn = new Button(buttonComp, SWT.PUSH);
        deleteBtn.setText("Delete");
        deleteBtn.setLayoutData(gd);
        deleteBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleDeleteAction();
            }
        });

        gd = new GridData();
        gd.widthHint = buttonWidth;
        rebootBtn = new Button(buttonComp, SWT.PUSH);
        rebootBtn.setText("Reboot...");
        rebootBtn.setLayoutData(gd);
        rebootBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleRebootAction();
            }
        });

        gd = new GridData(SWT.LEFT, SWT.DEFAULT, true, false);
        gd.widthHint = buttonWidth;
        failoverBtn = new Button(buttonComp, SWT.PUSH);
        failoverBtn.setText("Failover...");
        failoverBtn.setLayoutData(gd);
        failoverBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleFailoverAction();
            }
        });

        /*
         * Disable the {@link Button}s if in practice mode.
         */
        if (CAVEMode.getMode() != CAVEMode.OPERATIONAL) {
            newBtn.setEnabled(false);
            editBtn.setEnabled(false);
            deleteBtn.setEnabled(false);
            rebootBtn.setEnabled(false);
            failoverBtn.setEnabled(false);
        }
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
        TableColumnData tcd = new TableColumnData("DAC Name", 125);
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
        try {
            List<Dac> dacs = dataManager.getDacs();
            Collections.sort(dacs, new DacNameComparator());

            for (Dac dac : dacs) {
                TableRowData trd = createTableRow(dac);
                dacTableData.addDataRow(trd);
            }
        } catch (Exception e) {
            statusHandler.error("Error getting DAC data.", e);
        }
    }

    @Override
    public void tableSelectionChange(int selectionCount) {
        editBtn.setEnabled(selectionCount > 0
                && CAVEMode.getMode() == CAVEMode.OPERATIONAL);
    }

    private void updateTable(Dac dac) {
        for (TableRowData row : dacTableData.getTableRows()) {
            if (row.getData().equals(dac)) {
                row.setCellDataFromString(0, dac.getName());
                row.setCellDataFromString(1, dac.getAddress());
                row.setCellDataFromString(2,
                        String.valueOf(dac.getReceivePort()));
                row.setCellDataFromString(3, dac.getReceiveAddress());
                StringBuilder sb = new StringBuilder();
                for (Integer port : dac.getDataPorts()) {
                    sb.append(port).append(" ");
                }
                row.setCellDataFromString(4, sb.toString());
                row.setData(dac);

                dacTable.populateTable(dacTableData);
                return;
            }
        }

        // New Dac
        TableRowData row = createTableRow(dac);
        dacTableData.addDataRow(row);
        dacTable.populateTable(dacTableData);
    }

    private TableRowData createTableRow(Dac dac) {
        StringBuilder sb = new StringBuilder();
        TableRowData trd = new TableRowData();
        trd.addTableCellData(new TableCellData(dac.getName()));
        trd.addTableCellData(new TableCellData(dac.getAddress()));
        trd.addTableCellData(new TableCellData(String.valueOf(dac
                .getReceivePort())));
        trd.addTableCellData(new TableCellData(dac.getReceiveAddress()));
        List<Integer> ports = new ArrayList<Integer>(dac.getDataPorts());
        Collections.sort(ports);
        for (int port : ports) {
            sb.append(port).append(" ");
        }
        trd.addTableCellData(new TableCellData(sb.toString()));
        trd.setData(dac);

        return trd;
    }

    private void handleDeleteAction() {
        if (dacTable.getSelectedIndex() >= 0) {
            TableRowData rowData = dacTableData.getTableRow(dacTable
                    .getSelectedIndex());
            Dac dac = (Dac) rowData.getData();
            String msg = "Are you sure you want to delete DAC named "
                    + dac.getName() + "?";
            int answer = DialogUtility
                    .showMessageBox(getShell(), SWT.ICON_QUESTION | SWT.YES
                            | SWT.NO, "Confirm Delete", msg);
            if (answer == SWT.YES) {
                try {
                    dataManager.deleteDac(dac);
                    dacTableData.deleteRow(rowData);
                    dacTable.populateTable(dacTableData);
                } catch (Exception e) {
                    statusHandler.error("Error deleting DAC " + dac.getName(),
                            e);
                }
            }
        }
    }

    private void handleRebootAction() {
        if (dacTable.getSelectedIndex() >= 0) {
            TableRowData rowData = dacTableData.getTableRow(dacTable
                    .getSelectedIndex());
            final Dac dac = (Dac) rowData.getData();

            /* Confirm the reboot. */
            int option = DialogUtility.showMessageBox(this.shell,
                    SWT.ICON_QUESTION | SWT.YES | SWT.NO, "DAC Reboot",
                    "Are you sure you want to reboot DAC: " + dac.getName()
                            + "?");
            if (option != SWT.YES) {
                return;
            }

            ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
            try {
                dialog.run(true, false, new IRunnableWithProgress() {
                    @Override
                    public void run(IProgressMonitor monitor)
                            throws InvocationTargetException,
                            InterruptedException {
                        monitor.beginTask("Rebooting DAC: " + dac.getName()
                                + " ...", IProgressMonitor.UNKNOWN);
                        try {
                            latestConfigResponse = dataManager
                                    .configureSaveDac(null, true,
                                            dac.getAddress());

                        } catch (Exception e) {
                            statusHandler.error("Failed to configure DAC: "
                                    + dac.getName() + ".", e);
                        }
                    }
                });
            } catch (Exception e) {
                statusHandler.error("Failed to reboot DAC: " + dac.getName()
                        + ".", e);
                return;
            }

            this.displayDACEventDlg(null);
        }
    }

    private void handleFailoverAction() {
        if (dacTable.getSelectedIndex() >= 0) {
            TableRowData rowData = dacTableData.getTableRow(dacTable
                    .getSelectedIndex());
            final Dac dac = (Dac) rowData.getData();

            /*
             * Confirm that the user ideally has unplugged the failed DAC.
             */
            int option = DialogUtility
                    .showMessageBox(this.shell, SWT.ICON_QUESTION | SWT.YES
                            | SWT.NO, "DAC Failover",
                            "Has the DAC that was previously using this configuration been unplugged?");
            if (option != SWT.YES) {
                return;
            }

            /*
             * Request the IP Address of the replacement DAC.
             */
            final InputTextDlg inputDlg = new InputTextDlg(shell,
                    "Configure DAC",
                    "Enter the IP Address of the DAC to configure:",
                    MANUFACTURER_DAC_IP, new ConfigureDacAddressValidator(),
                    false, true);
            final String configAddress = (String) inputDlg.open();
            if (configAddress == null) {
                // Cancel
                return;
            }
            ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
            try {
                dialog.run(true, false, new IRunnableWithProgress() {
                    @Override
                    public void run(IProgressMonitor monitor)
                            throws InvocationTargetException,
                            InterruptedException {
                        monitor.beginTask("Configuring DAC: " + dac.getName()
                                + " ...", IProgressMonitor.UNKNOWN);
                        try {
                            latestConfigResponse = dataManager
                                    .configureSaveDac(dac, true, configAddress);

                        } catch (Exception e) {
                            statusHandler.error("Failed to configure DAC: "
                                    + dac.getName() + ".", e);
                        }
                    }
                });
            } catch (Exception e) {
                statusHandler.error("Failed to configure DAC: " + dac.getName()
                        + ".", e);
                return;
            }

            final String commonText = (this.latestConfigResponse != null && this.latestConfigResponse
                    .isSuccess()) ? "Transfer audio lines to the replacement DAC."
                    : null;
            this.displayDACEventDlg(commonText);
        }
    }

    private void displayDACEventDlg(final String commonText) {
        if (latestConfigResponse == null) {
            return;
        }

        DacConfigEventDlg dacEventDlg = new DacConfigEventDlg(shell,
                latestConfigResponse.getEvents(), commonText);
        dacEventDlg.open();
    }
}