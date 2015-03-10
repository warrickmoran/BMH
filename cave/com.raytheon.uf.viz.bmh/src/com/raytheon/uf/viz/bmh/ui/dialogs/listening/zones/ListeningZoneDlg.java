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
package com.raytheon.uf.viz.bmh.ui.dialogs.listening.zones;

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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.common.bmh.datamodel.transmitter.Area;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Zone;
import com.raytheon.uf.common.bmh.request.ZoneAreaResponse;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.bmh.ui.common.table.GenericTable;
import com.raytheon.uf.viz.bmh.ui.common.table.ITableActionCB;
import com.raytheon.uf.viz.bmh.ui.common.table.TableCellData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableColumnData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableData.SortDirection;
import com.raytheon.uf.viz.bmh.ui.common.table.TableRowData;
import com.raytheon.uf.viz.bmh.ui.common.utility.DialogUtility;
import com.raytheon.uf.viz.bmh.ui.dialogs.AbstractBMHDialog;
import com.raytheon.uf.viz.bmh.ui.dialogs.DlgInfo;
import com.raytheon.uf.viz.bmh.ui.dialogs.listening.ZonesAreasDataManager;
import com.raytheon.viz.ui.dialogs.ICloseCallback;

/**
 * Listening Zone view/edit dialog.
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
 * Oct 08,2014    #3646    rferrel     Convert zoneTableComp  and areaTableComp
 *                                      to GenericTable.
 * Nov 11, 2014  3413      rferrel     Use DlgInfo to get title.
 * Mar 10, 2015  4247      rferrel     Fix sorting after delete and bug in search.
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class ListeningZoneDlg extends AbstractBMHDialog {

    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(ListeningZoneDlg.class);

    /** Zone Table Composite */
    private GenericTable zoneTableComp;

    /** Columns for the zone table */
    private ArrayList<TableColumnData> columns;

    /** Zone TableData object */
    private TableData zoneTableData;

    /** Area TableData object */
    private TableData areaTableData;

    /** Selected Zone label */
    private Label selectedAreaLbl;

    /** Data Manager */
    private final ZonesAreasDataManager dataManager = new ZonesAreasDataManager();

    /** List of {@link Zone}s */
    private List<Zone> zones;

    /** Area table composite */
    private GenericTable areaTableComp;

    /** Area table columns */
    private ArrayList<TableColumnData> areaColumns;

    /** New/Edit zone dialog */
    private NewEditZoneDlg newEditZoneDlg;

    /** Edit zone button */
    private Button editZoneBtn;

    /** Delete zone button */
    private Button deleteZoneBtn;

    /**
     * Constructor.
     * 
     * @param parentShell
     * @param dlgMap
     *            Map of open dialogs
     */
    public ListeningZoneDlg(Shell parentShell,
            Map<AbstractBMHDialog, String> dlgMap) {
        super(dlgMap, DlgInfo.LISTENING_ZONES.getTitle(), parentShell,
                SWT.DIALOG_TRIM, CAVE.PERSPECTIVE_INDEPENDENT
                        | CAVE.DO_NOT_BLOCK);
        setText(DlgInfo.LISTENING_ZONES.getTitle());
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
        lbl.setText("Current Listening Zones:");
        lbl.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

        createListeningZoneTable();
        createListeningAreaControls();

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

        populateZoneTable();
    }

    /**
     * Create the listening zone table
     */
    private void createListeningZoneTable() {
        GridLayout gl = new GridLayout(1, false);
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gd.heightHint = 150;
        zoneTableComp = new GenericTable(getShell(), SWT.BORDER | SWT.V_SCROLL);
        zoneTableComp.setLayout(gl);
        zoneTableComp.setLayoutData(gd);
        zoneTableComp.setCallbackAction(new ITableActionCB() {
            @Override
            public void tableSelectionChange(int selectionCount) {
                zoneTableSelectionAction();
            }
        });

        columns = new ArrayList<TableColumnData>(2);

        TableColumnData tcd = new TableColumnData("Zone Area Code", 90);
        columns.add(tcd);

        tcd = new TableColumnData("Zone Area Name", 150);
        columns.add(tcd);

        zoneTableData = new TableData(columns);

        gl = new GridLayout(3, false);
        gd = new GridData(SWT.CENTER, SWT.DEFAULT, false, false);
        Composite btnComp = new Composite(getShell(), SWT.NONE);
        btnComp.setLayout(gl);
        btnComp.setLayoutData(gd);

        int btnWidth = 105;
        gd = new GridData(btnWidth, SWT.DEFAULT);
        Button addBtn = new Button(btnComp, SWT.PUSH);
        addBtn.setText("New Zone...");
        addBtn.setLayoutData(gd);
        addBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                createEditZone(null);
            }
        });

        gd = new GridData(btnWidth, SWT.DEFAULT);
        editZoneBtn = new Button(btnComp, SWT.PUSH);
        editZoneBtn.setText("Edit Zone...");
        editZoneBtn.setLayoutData(gd);
        editZoneBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                List<TableRowData> rows = zoneTableComp.getSelection();
                if (!rows.isEmpty()) {
                    TableRowData row = rows.get(0);
                    for (Zone z : zones) {
                        if (z.getZoneCode().equals(
                                row.getTableCellData().get(0).getCellText())) {
                            createEditZone(z);
                            return;
                        }
                    }
                }
            }
        });

        gd = new GridData(btnWidth, SWT.DEFAULT);
        deleteZoneBtn = new Button(btnComp, SWT.PUSH);
        deleteZoneBtn.setText("Delete Zone");
        deleteZoneBtn.setLayoutData(gd);
        deleteZoneBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleDeleteAction();
            }
        });

        DialogUtility.addSeparator(getShell(), SWT.HORIZONTAL);
    }

    /**
     * Create the listening area controls
     */
    private void createListeningAreaControls() {
        GridLayout gl = new GridLayout(2, false);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        Composite comp = new Composite(getShell(), SWT.NONE);
        comp.setLayout(gl);
        comp.setLayoutData(gd);

        gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        Label areaLbl = new Label(comp, SWT.NONE);
        areaLbl.setText("Listening Areas Assigned to Zone:  ");
        areaLbl.setLayoutData(gd);

        gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        selectedAreaLbl = new Label(comp, SWT.NONE);
        selectedAreaLbl.setLayoutData(gd);
        selectedAreaLbl.setText("Selected Zone");

        gl = new GridLayout(1, false);
        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gd.heightHint = 150;
        gd.horizontalSpan = 2;
        areaTableComp = new GenericTable(comp, SWT.BORDER | SWT.V_SCROLL);
        areaTableComp.setLayout(gl);
        areaTableComp.setLayoutData(gd);

        areaColumns = new ArrayList<TableColumnData>(2);

        TableColumnData tcd = new TableColumnData("Listening Area", 90);
        areaColumns.add(tcd);

        tcd = new TableColumnData("Location", 150);
        areaColumns.add(tcd);

        areaTableData = new TableData(areaColumns);
    }

    private void populateZoneTable() {
        try {
            zones = dataManager.getZones();
            generateTableData();

            zoneTableData.setSortColumnAndDirection(0, SortDirection.ASCENDING);
            this.zoneTableComp.populateTable(zoneTableData);
        } catch (Exception e) {
            statusHandler
                    .error("Error getting Zones from the BMH database.", e);
        }
    }

    /**
     * Action for zone table section.
     */
    private void zoneTableSelectionAction() {
        List<TableRowData> selection = zoneTableComp.getSelection();
        if (selection.isEmpty()) {
            return;
        }

        List<TableCellData> cellData = selection.get(0).getTableCellData();
        String zone = cellData.get(0).getDisplayString();
        selectedAreaLbl.setText(zone);

        areaTableData = new TableData(areaColumns);
        areaTableComp.removeAllTableItems();
        for (Zone z : zones) {
            if (z.getZoneCode().equals(zone)) {
                if (z.getAreas() != null) {
                    for (Area a : z.getAreas()) {
                        populateAreaTable(a);
                    }
                }
                areaTableData.setSortColumnAndDirection(0,
                        SortDirection.ASCENDING);
                areaTableComp.populateTable(areaTableData);
                break;
            }
        }

        enableZoneButtons(zoneTableComp.getSelection().size() > 0);
    }

    /**
     * Populate the Area TableData object
     * 
     * @param a
     *            The Area to populate
     */
    private void populateAreaTable(Area a) {
        TableRowData row = new TableRowData();
        TableCellData tcd = new TableCellData(a.getAreaCode());
        row.addTableCellData(tcd);

        tcd = new TableCellData(a.getAreaName());
        row.addTableCellData(tcd);

        areaTableData.addDataRow(row);
    }

    /**
     * Create or Edit a {@link Zone}.
     * 
     * @param zone
     *            Zone object to edit, or null if new
     */
    private void createEditZone(Zone zone) {
        if ((newEditZoneDlg == null) || newEditZoneDlg.isDisposed()) {
            try {
                List<Area> areas = dataManager.getAreas();
                List<Zone> zones = dataManager.getZones();
                newEditZoneDlg = new NewEditZoneDlg(getShell(), zone, areas,
                        zones);
                newEditZoneDlg.setCloseCallback(new ICloseCallback() {
                    @Override
                    public void dialogClosed(Object returnValue) {
                        Zone z = (Zone) newEditZoneDlg.getReturnValue();
                        if (z != null) {

                            try {
                                ZoneAreaResponse response = dataManager
                                        .saveZone(z);
                                for (Zone zone : response.getZoneList()) {
                                    populateNewZone(zone);
                                }
                            } catch (Exception e) {
                                statusHandler.error(
                                        "Error saving Zone, " + z.getZoneName(),
                                        e);
                            }
                        }
                    }
                });
                newEditZoneDlg.open();
            } catch (Exception e1) {
                statusHandler.error("Error accessing BMH database", e1);
            }
        } else {
            newEditZoneDlg.bringToTop();
        }
    }

    /**
     * Populate the zone table with the new zone
     * 
     * @param zone
     *            The zone to populate
     */
    private void populateNewZone(Zone zone) {
        // Check if this zone is already in the table
        for (Zone z : zones) {
            if (z.getZoneCode().equals(zone.getZoneCode())) {
                // Found a match
                z.setZoneName(zone.getZoneName());
                z.setAreas(zone.getAreas());
                refreshZoneTable();
                for (TableRowData row : zoneTableData.getTableRows()) {
                    if (row.getTableCellData().get(0).getCellText()
                            .equals(z.getZoneCode())) {
                        zoneTableComp.select(zoneTableData.getTableRows()
                                .indexOf(row));
                    }
                }
                zoneTableSelectionAction();
                enableZoneButtons(zoneTableComp.getSelection().size() > 0);
                return;
            }
        }

        zones.add(zone);

        TableRowData row = new TableRowData();
        TableCellData cell = new TableCellData(zone.getZoneCode());
        row.addTableCellData(cell);
        cell = new TableCellData(zone.getZoneName());
        row.addTableCellData(cell);
        zoneTableData.addDataRow(row);
        zoneTableData.sortData();
        zoneTableComp.populateTable(zoneTableData);
        zoneTableComp.select(zoneTableData.getTableRows().indexOf(row));
        zoneTableSelectionAction();
        enableZoneButtons(zoneTableComp.getSelection().size() > 0);
    }

    /**
     * Reload the zone table
     */
    private void refreshZoneTable() {
        generateTableData();

        zoneTableData.setSortColumnAndDirection(0, SortDirection.ASCENDING);
        zoneTableComp.populateTable(zoneTableData);
    }

    /**
     * Generate the tableData.
     */
    private void generateTableData() {
        // Clear the existing table rows
        zoneTableData.getTableRows().clear();

        for (Zone z : zones) {
            TableRowData row = new TableRowData();
            TableCellData cell = new TableCellData(z.getZoneCode());
            row.addTableCellData(cell);
            cell = new TableCellData(z.getZoneName());
            row.addTableCellData(cell);
            zoneTableData.addDataRow(row);
        }
    }

    /**
     * Enable/disable zone edit and delete buttons
     * 
     * @param enabled
     *            enabled flag
     */
    private void enableZoneButtons(boolean enabled) {
        deleteZoneBtn.setEnabled(enabled);
        editZoneBtn.setEnabled(enabled);
    }

    /**
     * Handle the delete action
     */
    private void handleDeleteAction() {
        int result = DialogUtility
                .showMessageBox(getShell(), SWT.OK | SWT.CANCEL,
                        "Delete Zone?",
                        "Are you sure you want to permenantly delete the selected zone?");

        if (result != SWT.OK) {
            return;
        }

        List<TableRowData> selectionList = zoneTableComp.getSelection();
        if (selectionList.size() > 0) {
            TableRowData row = selectionList.get(0);
            String id = row.getTableCellData().get(0).getCellText();
            Zone toDelete = null;
            for (Zone z : zones) {
                if (z.getZoneCode().equals(id)) {
                    toDelete = z;
                    break;
                }
            }

            if (toDelete != null) {
                zones.remove(toDelete);
                generateTableData();
                zoneTableData.setSortColumnAndDirection(0,
                        SortDirection.ASCENDING);
                zoneTableComp.populateTable(zoneTableData);
                try {
                    dataManager.deleteZone(toDelete);
                } catch (Exception e) {
                    statusHandler.error("Error deleting Zone "
                            + toDelete.getZoneName());
                }
            }
        }

        enableZoneButtons(zoneTableComp.getSelection().size() > 0);
    }

    @Override
    public boolean okToClose() {
        if ((newEditZoneDlg == null) || newEditZoneDlg.isDisposed()) {
            return true;
        }

        return false;
    }
}
