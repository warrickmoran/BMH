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
package com.raytheon.uf.viz.bmh.ui.dialogs.listening.areas;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.common.bmh.datamodel.transmitter.Area;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.bmh.request.ZoneAreaResponse;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.bmh.ui.common.table.ITableActionCB;
import com.raytheon.uf.viz.bmh.ui.common.table.TableCellData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableColumnData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableComp;
import com.raytheon.uf.viz.bmh.ui.common.table.TableData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableData.SortDirection;
import com.raytheon.uf.viz.bmh.ui.common.table.TableRowData;
import com.raytheon.uf.viz.bmh.ui.common.utility.DialogUtility;
import com.raytheon.uf.viz.bmh.ui.dialogs.AbstractBMHDialog;
import com.raytheon.uf.viz.bmh.ui.dialogs.listening.AreaTableComp;
import com.raytheon.uf.viz.bmh.ui.dialogs.listening.ZonesAreasDataManager;
import com.raytheon.viz.ui.dialogs.ICloseCallback;

/**
 * Listening area configuration dialog.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 11, 2014   3406     mpduff      Initial creation
 * Aug 05, 2014 3414       rjpeter     Added BMH Thrift interface.
 * Aug 8, 2014    #3490     lvenable    Updated populate table method call.
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class ListeningAreaDlg extends AbstractBMHDialog {
    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(ListeningAreaDlg.class);

    /** Area Table Composite */
    private AreaTableComp areaTableComp;

    /** Columns for the area table */
    private ArrayList<TableColumnData> areaColumns;

    /** Listening area code label */
    private Label lacLbl;

    /** Data manager */
    private final ZonesAreasDataManager dataManager = new ZonesAreasDataManager();

    /** Edit area button */
    private Button editAreaBtn;

    /** Delete area button */
    private Button deleteAreaBtn;

    /** Area table data object */
    private TableData areaTableData;

    /** List of {@link Area} objects */
    private List<Area> areas;

    /** Transmitter table data object */
    private TableData transmitterTableData;

    /** Transmitter table composite */
    private TransmitterTableComp transmitterTableComp;

    /** Columns for the transmitter table */
    private ArrayList<TableColumnData> transmitterColumns;

    /** New/Edit area dialog */
    private NewEditAreaDlg newEditAreaDlg;

    /**
     * Constructor.
     * 
     * @param parentShell
     * @param dlgMap
     *            Map of open dialogs
     */
    public ListeningAreaDlg(Shell parentShell,
            Map<AbstractBMHDialog, String> dlgMap) {
        super(dlgMap, "Listening Area Dialog", parentShell, SWT.DIALOG_TRIM,
                CAVE.PERSPECTIVE_INDEPENDENT | CAVE.DO_NOT_BLOCK);
        setText("Listening Areas");
    }

    @Override
    protected Layout constructShellLayout() {
        return new GridLayout(1, false);
    }

    @Override
    protected Object constructShellLayoutData() {
        return new GridData(SWT.FILL, SWT.FILL, true, true);
    }

    @Override
    protected void initializeComponents(Shell shell) {
        Label lbl = new Label(shell, SWT.NONE);
        lbl.setText("Current Listening Areas:");
        lbl.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

        createListeningAreaTable();
        createTransmitterControls();

        DialogUtility.addSeparator(getShell(), SWT.HORIZONTAL);

        GridLayout gl = new GridLayout(2, false);
        GridData gd = new GridData(SWT.CENTER, SWT.DEFAULT, false, false);
        Composite btnComp = new Composite(getShell(), SWT.NONE);
        btnComp.setLayout(gl);
        btnComp.setLayoutData(gd);

        gd = new GridData(75, SWT.DEFAULT);
        Button closeBtn = new Button(btnComp, SWT.PUSH);
        closeBtn.setText("Close");
        closeBtn.setLayoutData(gd);
        closeBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                close();
            }
        });

        populateAreaTable();
    }

    /**
     * Create the listening area table
     */
    private void createListeningAreaTable() {
        GridLayout gl = new GridLayout(1, false);
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gd.heightHint = 150;
        areaTableComp = new AreaTableComp(getShell(), SWT.BORDER | SWT.V_SCROLL);
        areaTableComp.setLayout(gl);
        areaTableComp.setLayoutData(gd);
        areaTableComp.setCallbackAction(new ITableActionCB() {
            @Override
            public void tableSelectionChange(int selectionCount) {
                areaTableSelectionAction();
            }
        });

        areaColumns = new ArrayList<TableColumnData>(2);

        TableColumnData tcd = new TableColumnData("Listening Area", 75);
        areaColumns.add(tcd);

        tcd = new TableColumnData("Location", 150);
        areaColumns.add(tcd);

        areaTableData = new TableData(areaColumns);

        gl = new GridLayout(3, false);
        gd = new GridData(SWT.CENTER, SWT.DEFAULT, false, false);
        Composite btnComp = new Composite(getShell(), SWT.NONE);
        btnComp.setLayout(gl);
        btnComp.setLayoutData(gd);

        int btnWidth = 105;
        gd = new GridData(btnWidth, SWT.DEFAULT);
        Button addBtn = new Button(btnComp, SWT.PUSH);
        addBtn.setText("New Area...");
        addBtn.setLayoutData(gd);
        addBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                createEditArea(null);
            }
        });

        gd = new GridData(btnWidth, SWT.DEFAULT);
        editAreaBtn = new Button(btnComp, SWT.PUSH);
        editAreaBtn.setText("Edit Area...");
        editAreaBtn.setLayoutData(gd);
        editAreaBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                List<TableRowData> rows = areaTableComp.getSelection();
                if (!rows.isEmpty()) {
                    TableRowData row = rows.get(0);
                    for (Area a : areas) {
                        if (a.getAreaCode().equals(
                                row.getTableCellData().get(0).getCellText())) {
                            createEditArea(a);
                            return;
                        }
                    }
                }
            }
        });

        gd = new GridData(btnWidth, SWT.DEFAULT);
        deleteAreaBtn = new Button(btnComp, SWT.PUSH);
        deleteAreaBtn.setText("Delete Area");
        deleteAreaBtn.setLayoutData(gd);
        deleteAreaBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleDeleteAction();
            }
        });

        DialogUtility.addSeparator(getShell(), SWT.HORIZONTAL);
    }

    /**
     * Create the transmitter section
     */
    private void createTransmitterControls() {
        GridLayout gl = new GridLayout(2, false);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        Composite comp = new Composite(getShell(), SWT.NONE);
        comp.setLayout(gl);
        comp.setLayoutData(gd);

        gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        Label areaLbl = new Label(comp, SWT.NONE);
        areaLbl.setText("Transmitters Assigned to Area:  ");
        areaLbl.setLayoutData(gd);

        gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        lacLbl = new Label(comp, SWT.NONE);
        lacLbl.setLayoutData(gd);
        lacLbl.setText("Selected Area");

        gl = new GridLayout(1, false);
        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gd.heightHint = 150;
        gd.horizontalSpan = 2;
        transmitterTableComp = new TransmitterTableComp(comp, SWT.BORDER
                | SWT.V_SCROLL);
        transmitterTableComp.setLayoutData(gd);
        transmitterTableComp.setLayout(gl);

        transmitterColumns = new ArrayList<TableColumnData>();

        TableColumnData tcd = new TableColumnData("Transmitter Mnemonic", 150);
        transmitterColumns.add(tcd);

        tcd = new TableColumnData("Transmitter Name");
        transmitterColumns.add(tcd);

        transmitterTableData = new TableData(areaColumns);
    }

    /**
     * Populate the area table
     */
    private void populateAreaTable() {
        try {
            areas = dataManager.getAreas();
            generateTableData();

            areaTableData.setSortColumnAndDirection(0, SortDirection.ASCENDING);
            this.areaTableComp.populateTable(areaTableData);
        } catch (Exception e) {
            statusHandler.error("Error occurred during Area query.", e);
        }
    }

    /**
     * Action for area table selection
     */
    private void areaTableSelectionAction() {
        List<TableRowData> selection = areaTableComp.getSelection();
        List<TableCellData> cellData = selection.get(0).getTableCellData();
        String area = cellData.get(0).getDisplayString();
        lacLbl.setText(area);

        transmitterTableData = new TableData(transmitterColumns);
        transmitterTableComp.removeAllTableItems();
        for (Area a : areas) {
            if (a.getAreaCode().equals(area)) {
                if (a.getTransmitters() != null) {
                    for (Transmitter t : a.getTransmitters()) {
                        populateTransmitterTable(t);
                    }
                }
                transmitterTableData.setSortColumnAndDirection(0,
                        SortDirection.ASCENDING);
                transmitterTableComp.populateTable(transmitterTableData);
                break;
            }
        }

        enableZoneButtons(areaTableComp.getSelection().size() > 0);
    }

    /**
     * Populate the transmitter table with the provided {@link Transmitter}
     * 
     * @param t
     *            The Transmitter to add
     */
    private void populateTransmitterTable(Transmitter t) {
        TableRowData row = new TableRowData();
        TableCellData tcd = new TableCellData(t.getMnemonic());
        row.addTableCellData(tcd);

        tcd = new TableCellData(t.getName());
        row.addTableCellData(tcd);

        transmitterTableData.addDataRow(row);
    }

    /**
     * Called to open the New/Edit area dialog.
     * 
     * @param area
     *            The area to edit, or null
     */
    private void createEditArea(Area area) {
        if ((newEditAreaDlg == null) || newEditAreaDlg.isDisposed()) {
            try {
                List<Transmitter> transmitters = dataManager.getTransmitters();
                newEditAreaDlg = new NewEditAreaDlg(getShell(), area,
                        transmitters, areas);
                newEditAreaDlg.setCloseCallback(new ICloseCallback() {
                    @Override
                    public void dialogClosed(Object returnValue) {
                        Area a = (Area) newEditAreaDlg.getReturnValue();
                        if (a != null) {
                            try {
                                ZoneAreaResponse response = dataManager
                                        .saveArea(a);
                                for (Area area : response.getAreaList()) {
                                    populateNewArea(area);
                                }
                            } catch (Exception e) {
                                statusHandler.error(
                                        "Error saving Area: " + a.getAreaCode()
                                                + " - " + a.getAreaName(), e);
                            }
                        }
                    }
                });
                newEditAreaDlg.open();
            } catch (Exception e1) {
                statusHandler.error("Error accessing BMH database", e1);
            }
        } else {
            newEditAreaDlg.bringToTop();
        }
    }

    /**
     * Add the {@link Area} to the table, or update if it already exists
     * 
     * @param area
     *            The area
     */
    private void populateNewArea(Area area) {
        // Check if this area is already in the table
        for (Area a : areas) {
            if (a.getAreaCode().equals(area.getAreaCode())) {
                // Found a match
                a.setAreaName(area.getAreaName());
                a.setTransmitters(area.getTransmitters());
                refreshAreaTable();
                for (TableRowData row : areaTableData.getTableRows()) {
                    if (row.getTableCellData().get(0).getCellText()
                            .equals(a.getAreaCode())) {
                        areaTableComp.select(areaTableData.getTableRows()
                                .indexOf(row));
                    }
                }
                areaTableSelectionAction();
                enableZoneButtons(areaTableComp.getSelection().size() > 0);
                return;
            }
        }

        areas.add(area);

        TableRowData row = new TableRowData();
        TableCellData cell = new TableCellData(area.getAreaCode());
        row.addTableCellData(cell);
        cell = new TableCellData(area.getAreaName());
        row.addTableCellData(cell);
        areaTableData.addDataRow(row);
        areaTableData.sortData();
        areaTableComp.populateTable(areaTableData);
        areaTableComp.select(areaTableData.getTableRows().indexOf(row));
        areaTableSelectionAction();
        enableZoneButtons(areaTableComp.getSelection().size() > 0);
    }

    /**
     * Reload the zone table
     */
    private void refreshAreaTable() {
        generateTableData();

        areaTableData.setSortColumnAndDirection(0, SortDirection.ASCENDING);
        areaTableComp.populateTable(areaTableData);
    }

    /**
     * Generate the tableData.
     */
    private void generateTableData() {
        // Clear the existing table rows
        areaTableData.getTableRows().clear();

        for (Area a : areas) {
            TableRowData row = new TableRowData();
            TableCellData cell = new TableCellData(a.getAreaCode());
            row.addTableCellData(cell);
            cell = new TableCellData(a.getAreaName());
            row.addTableCellData(cell);
            areaTableData.addDataRow(row);
        }
    }

    /**
     * Enable/disable area edit and delete buttons
     * 
     * @param enabled
     *            enabled flag
     */
    private void enableZoneButtons(boolean enabled) {
        deleteAreaBtn.setEnabled(enabled);
        editAreaBtn.setEnabled(enabled);
    }

    /**
     * Delete {@link Area} action handler
     */
    private void handleDeleteAction() {
        int result = DialogUtility
                .showMessageBox(getShell(), SWT.OK | SWT.CANCEL,
                        "Delete Area?",
                        "Are you sure you want to permenantly delete the selected area?");

        if (result == SWT.CANCEL) {
            return;
        }

        List<TableRowData> selectionList = areaTableComp.getSelection();
        if (selectionList.size() > 0) {
            TableRowData row = selectionList.get(0);
            String id = row.getTableCellData().get(0).getCellText();
            Area toDelete = null;
            for (Area a : areas) {
                if (a.getAreaCode().equals(id)) {
                    toDelete = a;
                    break;
                }
            }

            if (toDelete != null) {
                areas.remove(toDelete);
                generateTableData();
                areaTableComp.populateTable(areaTableData);
                try {
                    dataManager.deleteArea(toDelete);
                } catch (Exception e) {
                    statusHandler.error("Error while deleting "
                            + toDelete.getAreaCode() + " - "
                            + toDelete.getAreaName());
                }
            }
        }

        if (areas.size() > 0) {
            areaTableComp.select(0);
            areaTableSelectionAction();
        }
        enableZoneButtons(areaTableComp.getSelection().size() > 0);
    }

    /**
     * Transmitter table composite class.
     */
    private class TransmitterTableComp extends TableComp {

        public TransmitterTableComp(Composite parent, int tableStyle) {
            super(parent, tableStyle);

        }

        @Override
        protected void handleTableMouseClick(MouseEvent event) {
            // no-op

        }

        @Override
        protected void handleTableSelection(SelectionEvent e) {
            if (callbackAction != null) {
                callbackAction.tableSelectionChange(table.getSelectionCount());
            }
        }
    }

    @Override
    public boolean okToClose() {
        if ((newEditAreaDlg == null) || newEditAreaDlg.isDisposed()) {
            return true;
        }

        return false;
    }
}
