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
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import com.raytheon.uf.viz.bmh.ui.common.table.TableCellData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableColumnData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableComp;
import com.raytheon.uf.viz.bmh.ui.common.table.TableData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableRowData;
import com.raytheon.uf.viz.bmh.ui.common.utility.DialogUtility;
import com.raytheon.viz.ui.dialogs.CaveSWTDialog;

/**
 * Message Type Area Selection dialog.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 21, 2014   3411     mpduff      Initial creation
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class AreaSelectionDlg extends CaveSWTDialog {

    private final int LIST_WIDTH = 100;

    private final int LIST_HEIGHT_125 = 125;

    private final int LIST_HEIGHT_150 = 150;

    private final String[] COLUMN_NAMES = { "ID", "Name", "Type" };

    private final int MOVE_BUTTON_WIDTH = 45;

    /** Main display composite */
    private Composite mainComp;

    /**
     * Tab folder
     */
    private TabFolder tabFolder;

    /**
     * Selected table composite
     */
    private SelectedTableComp tableComp;

    /**
     * Selected table columns
     */
    private ArrayList<TableColumnData> columns;

    /**
     * The selected table TableData object
     */
    private TableData tableData;

    /**
     * The affected tranmitter list
     */
    private List transList;

    /**
     * Constructor.
     * 
     * @param parentShell
     *            The parent shell
     */
    public AreaSelectionDlg(Shell parentShell) {
        super(parentShell, SWT.DIALOG_TRIM, CAVE.PERSPECTIVE_INDEPENDENT);
        setText("Area Selection");
    }

    @Override
    protected Layout constructShellLayout() {
        return new GridLayout(1, false);
    }

    @Override
    protected Object constructShellLayoutData() {
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        return gd;
    }

    @Override
    protected void initializeComponents(Shell shell) {
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        GridLayout gl = new GridLayout(3, false);
        mainComp = new Composite(shell, SWT.NONE);
        mainComp.setLayout(gl);
        mainComp.setLayoutData(gd);

        createTabFolder();

        createSelectedTable();

        createTransmitterList();

        createBottomButtons();

        createColumns();
        populateTable();
    }

    /**
     * Create the tabs
     */
    private void createTabFolder() {
        tabFolder = new TabFolder(mainComp, SWT.BORDER);

        TabItem xmitTab = new TabItem(tabFolder, SWT.NONE);
        xmitTab.setText("Transmitters");

        createTransmitTab(xmitTab);

        TabItem zoneTab = new TabItem(tabFolder, SWT.NONE);
        zoneTab.setText(" Zones ");

        createZoneTab(zoneTab);

        TabItem areaTab = new TabItem(tabFolder, SWT.NONE);
        areaTab.setText(" Areas ");

        createAreaTab(areaTab);

        tabFolder.pack();
    }

    /**
     * Create the transmitter tab
     * 
     * @param xmitTab
     *            The tranmitter tab
     */
    private void createTransmitTab(TabItem xmitTab) {
        GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        GridLayout gl = new GridLayout(2, false);
        Composite comp = new Composite(tabFolder, SWT.NONE);
        comp.setLayout(gl);
        comp.setLayoutData(gd);

        xmitTab.setControl(comp);

        gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        gd.horizontalSpan = 2;
        Label xmitLbl = new Label(comp, SWT.NONE);
        xmitLbl.setText("Transmitters: ");
        xmitLbl.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.heightHint = LIST_HEIGHT_125;
        gd.widthHint = LIST_WIDTH;
        List xmitList = new List(comp, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
        xmitList.setLayoutData(gd);
        // TODO - remove
        xmitList.setItems(new String[] { "Trans1", "Trans2", "Trans3" });

        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.verticalAlignment = SWT.CENTER;
        gl = new GridLayout(1, false);
        Composite moveComp = new Composite(comp, SWT.NONE);
        moveComp.setLayout(gl);
        moveComp.setLayoutData(gd);

        Button xmitMoveBtn = new Button(moveComp, SWT.PUSH);
        xmitMoveBtn.setText(">");
        xmitMoveBtn.setLayoutData(new GridData(MOVE_BUTTON_WIDTH, SWT.DEFAULT));
        xmitMoveBtn
                .setToolTipText("Move selected Transmitter(s) to the Selected Table");

        Button xmitMoveAllBtn = new Button(moveComp, SWT.PUSH);
        xmitMoveAllBtn.setText(">>");
        xmitMoveAllBtn.setLayoutData(new GridData(MOVE_BUTTON_WIDTH,
                SWT.DEFAULT));
        xmitMoveAllBtn
                .setToolTipText("Move all Transmitters to the Selected Table");

        gd = new GridData(SWT.LEFT, SWT.DEFAULT, false, false);
        gd.horizontalSpan = 2;
        Label newLineLbl = new Label(comp, SWT.NONE);
        newLineLbl.setText("");
        newLineLbl.setLayoutData(gd);

        gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        gd.horizontalSpan = 2;
        Label areaLbl = new Label(comp, SWT.NONE);
        areaLbl.setText("Areas for Transmitter(s): ");
        areaLbl.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.heightHint = LIST_HEIGHT_150;
        gd.widthHint = LIST_WIDTH;
        List areaList = new List(comp, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
        areaList.setLayoutData(gd);
        // TODO - remove
        areaList.setItems(new String[] { "Area1", "Area2", "Area3" });

        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.verticalAlignment = SWT.CENTER;
        gl = new GridLayout(1, false);
        Composite moveComp2 = new Composite(comp, SWT.NONE);
        moveComp2.setLayout(gl);
        moveComp2.setLayoutData(gd);

        Button areaMoveBtn = new Button(moveComp2, SWT.PUSH);
        areaMoveBtn.setText(">");
        areaMoveBtn.setLayoutData(new GridData(MOVE_BUTTON_WIDTH, SWT.DEFAULT));
        areaMoveBtn
                .setToolTipText("Move selected Area(s) to the Selected Table");

        Button areaMoveAllBtn = new Button(moveComp2, SWT.PUSH);
        areaMoveAllBtn.setText(">>");
        areaMoveAllBtn.setLayoutData(new GridData(MOVE_BUTTON_WIDTH,
                SWT.DEFAULT));
        areaMoveAllBtn.setToolTipText("Move all Areas to the Selected Table");

    }

    /**
     * Create the zone tab
     * 
     * @param zoneTab
     *            The zone tab
     */
    private void createZoneTab(TabItem zoneTab) {
        GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        GridLayout gl = new GridLayout(2, false);
        Composite comp = new Composite(tabFolder, SWT.NONE);
        comp.setLayout(gl);
        comp.setLayoutData(gd);

        zoneTab.setControl(comp);

        gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        gd.horizontalSpan = 2;
        Label zonesLbl = new Label(comp, SWT.NONE);
        zonesLbl.setText("Zones: ");
        zonesLbl.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.heightHint = LIST_HEIGHT_125;
        gd.widthHint = LIST_WIDTH;
        List zoneList = new List(comp, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
        zoneList.setLayoutData(gd);
        // TODO - remove
        zoneList.setItems(new String[] { "Zone1", "Zone2", "Zone3" });

        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.verticalAlignment = SWT.CENTER;
        gl = new GridLayout(1, false);
        Composite moveComp = new Composite(comp, SWT.NONE);
        moveComp.setLayout(gl);
        moveComp.setLayoutData(gd);

        Button zoneMoveBtn = new Button(moveComp, SWT.PUSH);
        zoneMoveBtn.setText(">");
        zoneMoveBtn.setLayoutData(new GridData(MOVE_BUTTON_WIDTH, SWT.DEFAULT));
        zoneMoveBtn
                .setToolTipText("Move selected Zone(s) to the Selected Table");

        Button zoneMoveAllBtn = new Button(moveComp, SWT.PUSH);
        zoneMoveAllBtn.setText(">>");
        zoneMoveAllBtn.setLayoutData(new GridData(MOVE_BUTTON_WIDTH,
                SWT.DEFAULT));
        zoneMoveAllBtn.setToolTipText("Move all Zones to the Selected Table");

        gd = new GridData(SWT.LEFT, SWT.DEFAULT, false, false);
        gd.horizontalSpan = 2;
        Label newLineLbl = new Label(comp, SWT.NONE);
        newLineLbl.setText("");
        newLineLbl.setLayoutData(gd);

        gd = new GridData(SWT.LEFT, SWT.DEFAULT, false, false);
        gd.horizontalSpan = 2;
        Label areaLbl = new Label(comp, SWT.NONE);
        areaLbl.setText("Areas for Zone(s): ");
        areaLbl.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.heightHint = LIST_HEIGHT_150;
        gd.widthHint = LIST_WIDTH;
        List areaList = new List(comp, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
        areaList.setLayoutData(gd);
        // TODO - remove
        areaList.setItems(new String[] { "Area1", "Area2", "Area3" });

        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.verticalAlignment = SWT.CENTER;
        gl = new GridLayout(1, false);
        Composite moveComp2 = new Composite(comp, SWT.NONE);
        moveComp2.setLayout(gl);
        moveComp2.setLayoutData(gd);

        Button areaMoveBtn = new Button(moveComp2, SWT.PUSH);
        areaMoveBtn.setText(">");
        areaMoveBtn.setLayoutData(new GridData(MOVE_BUTTON_WIDTH, SWT.DEFAULT));
        areaMoveBtn
                .setToolTipText("Move selected Area(s) to the Selected Table");

        Button areaMoveAllBtn = new Button(moveComp2, SWT.PUSH);
        areaMoveAllBtn.setText(">>");
        areaMoveAllBtn.setLayoutData(new GridData(MOVE_BUTTON_WIDTH,
                SWT.DEFAULT));
        areaMoveAllBtn.setToolTipText("Move all Areas to the Selected Table");
    }

    /**
     * create the area tab
     * 
     * @param areaTab
     *            The area tab
     */
    private void createAreaTab(TabItem areaTab) {
        GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        GridLayout gl = new GridLayout(2, false);
        Composite comp = new Composite(tabFolder, SWT.NONE);
        comp.setLayout(gl);
        comp.setLayoutData(gd);

        areaTab.setControl(comp);

        gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        gd.horizontalSpan = 2;
        Label areaLbl = new Label(comp, SWT.NONE);
        areaLbl.setText("All Areas: ");
        areaLbl.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.heightHint = LIST_HEIGHT_125;
        gd.widthHint = LIST_WIDTH;
        List areaList = new List(comp, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
        areaList.setLayoutData(gd);
        // TODO - remove
        areaList.setItems(new String[] { "Area1", "Area2", "Area3" });

        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.verticalAlignment = SWT.CENTER;
        gl = new GridLayout(1, false);
        Composite moveComp = new Composite(comp, SWT.NONE);
        moveComp.setLayout(gl);
        moveComp.setLayoutData(gd);

        Button areaMoveBtn = new Button(moveComp, SWT.PUSH);
        areaMoveBtn.setText(">");
        areaMoveBtn.setLayoutData(new GridData(MOVE_BUTTON_WIDTH, SWT.DEFAULT));
        areaMoveBtn
                .setToolTipText("Move selected Area(s) to the Selected Table");

        Button areaMoveAllBtn = new Button(moveComp, SWT.PUSH);
        areaMoveAllBtn.setText(">>");
        areaMoveAllBtn.setLayoutData(new GridData(MOVE_BUTTON_WIDTH,
                SWT.DEFAULT));
        areaMoveAllBtn.setToolTipText("Move all Areas to the Selected Table");
    }

    /**
     * Create the selected items table
     */
    private void createSelectedTable() {
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        GridLayout gl = new GridLayout(1, false);
        Composite selectedComp = new Composite(mainComp, SWT.NONE);
        selectedComp.setLayout(gl);
        selectedComp.setLayoutData(gd);

        Label l = new Label(selectedComp, SWT.NONE);
        l.setText("Selected Zones/Areas/Transmitters:");

        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.widthHint = 450;
        gl = new GridLayout(1, false);
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        tableComp = new SelectedTableComp(selectedComp, SWT.BORDER
                | SWT.V_SCROLL | SWT.MULTI);
        tableComp.setLayout(gl);
        tableComp.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gd.horizontalAlignment = SWT.CENTER;
        gl = new GridLayout(1, false);
        Composite comp = new Composite(selectedComp, SWT.NONE);
        comp.setLayout(gl);
        comp.setLayoutData(gd);

        Button removeBtn = new Button(comp, SWT.PUSH);
        removeBtn.setText(" Remove Selected ");
    }

    /**
     * Create the affected transmitter list
     */
    private void createTransmitterList() {
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        GridLayout gl = new GridLayout(1, false);
        Composite comp = new Composite(mainComp, SWT.NONE);
        comp.setLayout(gl);
        comp.setLayoutData(gd);

        gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        Label headerLbl = new Label(comp, SWT.NONE);
        headerLbl.setText("Affected\nTransmitters: ");
        headerLbl.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.widthHint = 75;

        transList = new List(comp, SWT.BORDER);
        transList.setLayoutData(gd);

        transList.add("OMA");
        transList.add("LNK");
    }

    /**
     * Create the bottom action buttons.
     */
    private void createBottomButtons() {
        DialogUtility.addSeparator(shell, SWT.HORIZONTAL);

        Composite buttonComp = new Composite(shell, SWT.NONE);
        buttonComp.setLayout(new GridLayout(2, false));
        buttonComp.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true,
                false));

        int buttonWidth = 70;

        GridData gd = new GridData(SWT.RIGHT, SWT.DEFAULT, true, false);
        gd.widthHint = buttonWidth;
        Button okBtn = new Button(buttonComp, SWT.PUSH);
        okBtn.setText("OK");
        okBtn.setLayoutData(gd);
        okBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {

            }
        });

        gd = new GridData(SWT.LEFT, SWT.DEFAULT, true, false);
        gd.widthHint = buttonWidth;
        Button cancelBtn = new Button(buttonComp, SWT.PUSH);
        cancelBtn.setText("Cancel");
        cancelBtn.setLayoutData(gd);
        cancelBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setReturnValue(null);
                close();
            }
        });
    }

    /**
     * Populate the table
     */
    private void populateTable() {
        tableData = new TableData(columns);

        // TODO get data and populate if data are available
        TableRowData row = new TableRowData();
        TableCellData cell = new TableCellData("OMA");
        row.addTableCellData(cell);
        cell = new TableCellData("Omaha Transmitter");
        row.addTableCellData(cell);
        cell = new TableCellData("Transmitter");
        row.addTableCellData(cell);
        tableData.addDataRow(row);

        row = new TableRowData();
        cell = new TableCellData("NEC125");
        row.addTableCellData(cell);
        cell = new TableCellData("Douglas County");
        row.addTableCellData(cell);
        cell = new TableCellData("Area");
        row.addTableCellData(cell);
        tableData.addDataRow(row);

        row = new TableRowData();
        cell = new TableCellData("IAZ698");
        row.addTableCellData(cell);
        cell = new TableCellData("SW Iowa");
        row.addTableCellData(cell);
        cell = new TableCellData("Zone");
        row.addTableCellData(cell);
        tableData.addDataRow(row);

        this.tableComp.populateTable(tableData);

    }

    /**
     * Create the table columns
     */
    private void createColumns() {
        columns = new ArrayList<TableColumnData>(3);
        int width = 80;
        String name = COLUMN_NAMES[0];
        TableColumnData tc = new TableColumnData(name, width);
        tc.setAlignment(SWT.LEFT);
        tc.setPack(false);
        columns.add(tc);

        width = 175;
        name = COLUMN_NAMES[1];
        tc = new TableColumnData(name, width);
        tc.setAlignment(SWT.LEFT);
        tc.setPack(false);
        columns.add(tc);

        name = COLUMN_NAMES[2];
        tc = new TableColumnData(name, width);
        tc.setAlignment(SWT.LEFT);
        tc.setPack(true);
        columns.add(tc);

    }

    /**
     * Selected Table Comp
     */
    private class SelectedTableComp extends TableComp {

        public SelectedTableComp(Composite parent, int tableStyle) {
            super(parent, tableStyle, true, true);
        }

        @Override
        protected void handleTableMouseClick(MouseEvent event) {
            // TODO Auto-generated method stub

        }

        @Override
        protected void handleTableSelection(SelectionEvent e) {
            // TODO Auto-generated method stub

        }
    }
}
