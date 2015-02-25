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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.swt.SWT;
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

import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Area;
import com.raytheon.uf.common.bmh.datamodel.transmitter.AreaNameComparator;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterLocationComparator;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Zone;
import com.raytheon.uf.common.bmh.datamodel.transmitter.ZoneNameComparator;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.bmh.ui.common.table.GenericTable;
import com.raytheon.uf.viz.bmh.ui.common.table.TableCellData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableColumnData;
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
 * Aug 13, 2014   3411     mpduff      Populate with data
 * Aug 18, 2014   3432     mpduff      Removed areaObject field
 * Aug 28, 2014   3432     mpduff      Display areaCode, not areaId
 * Sep 11, 2014   3411     mpduff      Populate upon opening.
 * Oct 09, 2014   3646     rferrel     Converted tableComp to GenerticTable.
 * Oct 14, 2014   3728     lvenable    Updated to support weather messages functionality
 * Oct 16, 2014   3657     bkowal      Include affected transmitters in the return object
 * Oct 17, 2014   3655     bkowal      Store transmitter information in the swt data.
 * Oct 21, 2014   3728     lvenable    Updated to handle a string of area codes and zone codes.
 * Oct 21, 2014   3896     lvenable    Fixed the affected transmitters list to be updated when
 *                                     the dialog is opened and there are area codes.
 * Jan 20, 2015   4010     bkowal      Fixed the area selection dialog.
 * Feb 09, 2015   4095     bsteffen    Remove Transmitter Name.
 * Feb 10, 2015   4104     bkowal      Specify the table height so that it does not extend past the
 *                                     edge of the dialog when loaded with a large number of items.
 * Feb 25, 2015  4122      rferrel     Message type constructor now take data to populate table.
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class AreaSelectionDlg extends CaveSWTDialog {
    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(AreaSelectionDlg.class);

    private final String TRANSMITTER = "Transmitter";

    private final String AREA = "Area";

    private final String ZONE = "Zone";

    private final int LIST_WIDTH = 125;

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
    private GenericTable tableComp;

    /**
     * Selected table columns
     */
    private java.util.List<TableColumnData> columns;

    /**
     * The selected table TableData object
     */
    private TableData tableData;

    /**
     * The affected transmitter list
     */
    private List affectedTransmitterList;

    /**
     * List of transmitters in Transmitter tab
     */
    private List xmitList;

    /**
     * Data backing this dialog
     */
    private AreaSelectionData areaSelectionData;

    /**
     * Zone list in zone tab
     */
    private List zoneList;

    /**
     * List of Transmitter objects
     */
    private java.util.List<Transmitter> transmitterObjectList;

    /**
     * List of Zone objects
     */
    private java.util.List<Zone> zoneObjectList;

    /** List of areas for the selected transmitter */
    private List transmitterAreaList;

    /** List of areas for the selected zone */
    private List zoneAreaList;

    /** List of all areas */
    private List areaList;

    /** The Message Type */
    private MessageType messageType = null;

    /**
     * {@link AreaSelectionSaveData} from a previous session or based on an
     * existing input message.
     */
    private AreaSelectionSaveData data = null;

    /**
     * Constructor.
     * 
     * @param parentShell
     *            The parent shell
     */
    public AreaSelectionDlg(Shell parentShell, MessageType messageType,
            AreaSelectionSaveData data) {
        super(parentShell, SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL,
                CAVE.PERSPECTIVE_INDEPENDENT);
        if (messageType == null) {
            throw new IllegalArgumentException(
                    "Required argument messageType can not be NULL.");
        }

        this.messageType = messageType;
        this.data = data;
    }

    /**
     * Constructor that take a string of area codes that will be parsed and
     * displayed.
     * 
     * Example of area code string: "NEZ024-NEZ005-NEZ023-NEZ025-NEZ036-NEZ094"
     * 
     * @param parentShell
     *            Parent shell.
     * @param areaCodes
     *            String of areas codes.
     */
    public AreaSelectionDlg(Shell parentShell, AreaSelectionSaveData data) {
        super(parentShell, SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL,
                CAVE.PERSPECTIVE_INDEPENDENT);
        if (data == null) {
            throw new IllegalArgumentException(
                    "Required argument data can not be NULL.");
        }
        this.data = data;
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
        setText("Area Selection");

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

        gatherData();
        populateTabs();
        populateTableData();
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
        xmitList = new List(comp, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
        xmitList.setLayoutData(gd);
        xmitList.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                populateAreasForTransmitter();
            }
        });

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
        xmitMoveBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                moveTransmitter(false);
            }
        });

        Button xmitMoveAllBtn = new Button(moveComp, SWT.PUSH);
        xmitMoveAllBtn.setText(">>");
        xmitMoveAllBtn.setLayoutData(new GridData(MOVE_BUTTON_WIDTH,
                SWT.DEFAULT));
        xmitMoveAllBtn
                .setToolTipText("Move all Transmitters to the Selected Table");
        xmitMoveAllBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                moveTransmitter(true);
            }
        });

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
        transmitterAreaList = new List(comp, SWT.MULTI | SWT.BORDER
                | SWT.V_SCROLL | SWT.H_SCROLL);
        transmitterAreaList.setLayoutData(gd);

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
        areaMoveBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                moveTransmitterArea(false);
            }
        });

        Button areaMoveAllBtn = new Button(moveComp2, SWT.PUSH);
        areaMoveAllBtn.setText(">>");
        areaMoveAllBtn.setLayoutData(new GridData(MOVE_BUTTON_WIDTH,
                SWT.DEFAULT));
        areaMoveAllBtn.setToolTipText("Move all Areas to the Selected Table");
        areaMoveAllBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                moveTransmitterArea(true);
            }
        });
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
        zoneList = new List(comp, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL
                | SWT.H_SCROLL);
        zoneList.setLayoutData(gd);
        zoneList.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                populateAreasForZone();
            }
        });

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
        zoneMoveBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                moveZone(false);
            }
        });

        Button zoneMoveAllBtn = new Button(moveComp, SWT.PUSH);
        zoneMoveAllBtn.setText(">>");
        zoneMoveAllBtn.setLayoutData(new GridData(MOVE_BUTTON_WIDTH,
                SWT.DEFAULT));
        zoneMoveAllBtn.setToolTipText("Move all Zones to the Selected Table");
        zoneMoveAllBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                moveZone(true);
            }
        });

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
        zoneAreaList = new List(comp, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
        zoneAreaList.setLayoutData(gd);

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
        areaMoveBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                moveZoneArea(false);
            }
        });

        Button areaMoveAllBtn = new Button(moveComp2, SWT.PUSH);
        areaMoveAllBtn.setText(">>");
        areaMoveAllBtn.setLayoutData(new GridData(MOVE_BUTTON_WIDTH,
                SWT.DEFAULT));
        areaMoveAllBtn.setToolTipText("Move all Areas to the Selected Table");
        areaMoveAllBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                moveZoneArea(true);
            }
        });

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
        areaList = new List(comp, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL
                | SWT.H_SCROLL);
        areaList.setLayoutData(gd);

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
        areaMoveBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                moveArea(false);
            }
        });

        Button areaMoveAllBtn = new Button(moveComp, SWT.PUSH);
        areaMoveAllBtn.setText(">>");
        areaMoveAllBtn.setLayoutData(new GridData(MOVE_BUTTON_WIDTH,
                SWT.DEFAULT));
        areaMoveAllBtn.setToolTipText("Move all Areas to the Selected Table");
        areaMoveAllBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                moveArea(true);
            }
        });
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
        gd.heightHint = 350;
        gl = new GridLayout(1, false);
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        tableComp = new GenericTable(selectedComp, SWT.BORDER | SWT.V_SCROLL
                | SWT.MULTI);
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
        removeBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                removeSelectedItemsFromTable();
            }
        });
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

        affectedTransmitterList = new List(comp, SWT.BORDER);
        affectedTransmitterList.setLayoutData(gd);
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
                populateReturnObject();
                close();
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
     * Populate the tabs with data
     */
    private void populateTabs() {
        if (areaSelectionData == null) {
            gatherData();
        }

        this.transmitterObjectList = areaSelectionData.getTransmitterList();
        Collections.sort(transmitterObjectList,
                new TransmitterLocationComparator());
        java.util.List<String> transmitterNameList = new ArrayList<>(
                transmitterObjectList.size());
        for (Transmitter t : transmitterObjectList) {
            transmitterNameList.add(t.getLocation());
        }

        xmitList.setItems(transmitterNameList.toArray(new String[0]));
        if (xmitList.getItemCount() > 0) {
            xmitList.select(0);
            populateAreasForTransmitter();
        }

        this.zoneObjectList = areaSelectionData.getZoneList();
        Collections.sort(zoneObjectList, new ZoneNameComparator());
        java.util.List<String> zoneNameList = new ArrayList<>(
                zoneObjectList.size());
        for (Zone z : zoneObjectList) {
            zoneNameList.add(z.getZoneName());
        }

        zoneList.setItems(zoneNameList.toArray(new String[0]));
        if (zoneList.getItemCount() > 0) {
            zoneList.select(0);
            populateAreasForZone();
        }

        java.util.List<Area> areaObjectList = areaSelectionData.getAreaList();
        Collections.sort(areaObjectList, new AreaNameComparator());
        String[] areaNames = new String[areaObjectList.size()];
        int idx = 0;
        for (Area a : areaObjectList) {
            areaNames[idx] = a.getAreaName();
            idx++;
        }

        areaList.setItems(areaNames);
        if (areaList.getItemCount() > 0) {
            areaList.select(0);
        }
    }

    /**
     * Populate the TableData object.
     */
    private void populateTableData() {

        if (messageType != null) {
            for (Area area : messageType.getDefaultAreas()) {
                TableRowData row = createAreaRow(area);
                tableData.addDataRow(row);
            }

            for (Zone zone : messageType.getDefaultZones()) {
                TableRowData row = createZoneRow(zone);
                tableData.addDataRow(row);
            }

            for (TransmitterGroup group : messageType
                    .getDefaultTransmitterGroups()) {
                for (Transmitter t : group.getTransmitters()) {
                    TableRowData row = createTransmitterRow(t);
                    tableData.addDataRow(row);
                }
            }
        }

        if (this.data != null) {
            for (Area area : this.data.getAreas()) {
                TableRowData row = createAreaRow(area);
                tableData.addDataRow(row);
            }

            for (Zone zone : this.data.getZones()) {
                TableRowData row = createZoneRow(zone);
                tableData.addDataRow(row);
            }

            for (Transmitter transmitter : this.data.getTransmitters()) {
                TableRowData row = createTransmitterRow(transmitter);
                tableData.addDataRow(row);
            }
        }

        this.updateAffectedTransmitters();
    }

    /**
     * Populate the table
     */
    private void populateTable() {
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

        tableData = new TableData(columns);
    }

    /**
     * Populate the area list of the transmitter tab
     */
    private void populateAreasForTransmitter() {
        int[] indices = xmitList.getSelectionIndices();
        java.util.List<Area> areas = new ArrayList<Area>();
        for (int i : indices) {
            Transmitter t = this.transmitterObjectList.get(i);
            areas.addAll(areaSelectionData.getAreasForTransmitter(t));
        }

        String[] areaNames = new String[areas.size()];

        for (int i = 0; i < areas.size(); i++) {
            areaNames[i] = areas.get(i).getAreaName();
        }

        Arrays.sort(areaNames);

        this.transmitterAreaList.setItems(areaNames);
    }

    /**
     * Populate the area list for the zone tab
     */
    private void populateAreasForZone() {
        int[] indices = zoneList.getSelectionIndices();
        java.util.List<Area> areas = new ArrayList<Area>();
        for (int i : indices) {
            Zone z = this.zoneObjectList.get(i);
            if (z.getAreas() != null) {
                areas.addAll(z.getAreas());
            }
        }

        String[] areaNames = new String[areas.size()];

        for (int i = 0; i < areas.size(); i++) {
            areaNames[i] = areas.get(i).getAreaName();
        }

        this.zoneAreaList.setItems(areaNames);
    }

    /**
     * Populate the data object.
     */
    private void gatherData() {
        areaSelectionData = new AreaSelectionData();
        try {
            areaSelectionData.populate();
        } catch (Exception e) {
            statusHandler.error("Error accessing BMH database", e);
        }
    }

    /**
     * Move Transmitter to the selected table
     * 
     * @param moveAll
     *            true moves all, false moves selection
     */
    private void moveTransmitter(boolean moveAll) {
        if (moveAll) {
            for (Transmitter t : transmitterObjectList) {
                addTransmitterRow(t);
            }
        } else {
            int[] indices = xmitList.getSelectionIndices();
            for (int i : indices) {
                addTransmitterRow(transmitterObjectList.get(i));
            }
        }

        updateAffectedTransmitters();
        populateTable();
    }

    /**
     * Move Transmitter's Area to the selected table
     * 
     * @param moveAll
     *            true moves all, false moves selection
     */
    private void moveTransmitterArea(boolean moveAll) {
        if (moveAll) {
            for (String areaName : transmitterAreaList.getItems()) {
                Area area = areaSelectionData.getAreaByName(areaName);
                if (area != null) {
                    addAreaRow(area);
                }
            }
        } else {
            int[] indices = transmitterAreaList.getSelectionIndices();
            for (int i : indices) {
                Area area = areaSelectionData.getAreaByName(transmitterAreaList
                        .getItem(i));
                if (area != null) {
                    addAreaRow(area);
                }
            }
        }

        updateAffectedTransmitters();
        populateTable();
    }

    /**
     * Move Zone to the selected table
     * 
     * @param moveAll
     *            true moves all, false moves selection
     */
    private void moveZone(boolean moveAll) {
        if (moveAll) {
            for (Zone z : zoneObjectList) {
                addZoneRow(z);
            }
        } else {
            int[] indices = zoneList.getSelectionIndices();
            for (int i : indices) {
                addZoneRow(zoneObjectList.get(i));
            }
        }

        updateAffectedTransmitters();
        populateTable();
    }

    /**
     * Move Zone's Area to the selected table
     * 
     * @param moveAll
     *            true moves all, false moves selection
     */
    private void moveZoneArea(boolean moveAll) {
        if (moveAll) {
            for (String areaName : zoneAreaList.getItems()) {
                addAreaRow(areaSelectionData.getAreaByName(areaName));
            }
        } else {
            int[] indices = zoneAreaList.getSelectionIndices();
            for (int i : indices) {
                addAreaRow(areaSelectionData.getAreaByName(zoneAreaList
                        .getItem(i)));
            }
        }

        updateAffectedTransmitters();
        populateTable();
    }

    /**
     * Move Area to the selected table
     * 
     * @param moveAll
     *            true moves all, false moves selection
     */
    private void moveArea(boolean moveAll) {
        if (moveAll) {
            for (String areaName : areaList.getItems()) {
                addAreaRow(areaSelectionData.getAreaByName(areaName));
            }
        } else {
            int[] indices = areaList.getSelectionIndices();
            for (int i : indices) {
                addAreaRow(areaSelectionData.getAreaByName(areaList.getItem(i)));
            }
        }

        updateAffectedTransmitters();
        populateTable();

    }

    /**
     * Add a Transmitter row to the table
     * 
     * @param t
     *            The Transmitter to add
     */
    private void addTransmitterRow(Transmitter t) {
        for (TableRowData trd : tableData.getTableRows()) {
            if (trd.getData() instanceof Transmitter) {
                if (((Transmitter) trd.getData()).equals(t)) {
                    return;
                }
            }
        }

        TableRowData row = createTransmitterRow(t);
        tableData.addDataRow(row);
    }

    /**
     * Add an Area row to the table
     * 
     * @param a
     *            The Area to add
     */
    private void addAreaRow(Area a) {
        for (TableRowData trd : tableData.getTableRows()) {
            if (trd.getData() instanceof Area) {
                if (((Area) trd.getData()).equals(a)) {
                    return;
                }
            }
        }

        TableRowData row = createAreaRow(a);
        tableData.addDataRow(row);
    }

    /**
     * Add a Zone row to the table
     * 
     * @param z
     *            The Zone to add
     */
    private void addZoneRow(Zone z) {
        for (TableRowData trd : tableData.getTableRows()) {
            if (trd.getData() instanceof Zone) {
                if (((Zone) trd.getData()).equals(z)) {
                    return;
                }
            }
        }

        TableRowData row = createZoneRow(z);
        tableData.addDataRow(row);
    }

    /**
     * Remove selected items from the selected table.
     */
    private void removeSelectedItemsFromTable() {
        java.util.List<TableRowData> tableSelection = tableComp.getSelection();
        for (TableRowData trd : tableSelection) {
            tableData.deleteRow(trd);
        }

        updateAffectedTransmitters();
        populateTable();
    }

    /**
     * Update the list of affected transmitters
     */
    private void updateAffectedTransmitters() {
        java.util.List<TableRowData> selectedRows = tableData.getTableRows();
        Set<Transmitter> listOfAffectedTransmitters = new HashSet<Transmitter>();
        for (TableRowData trd : selectedRows) {
            Object o = trd.getData();
            if (o instanceof Transmitter) {
                listOfAffectedTransmitters.add((Transmitter) o);
            } else if (o instanceof Area) {
                Area a = (Area) o;
                listOfAffectedTransmitters.addAll(a.getTransmitters());
            } else if (o instanceof Zone) {
                Zone z = (Zone) o;
                if (z.getAreas() != null) {
                    for (Area a : z.getAreas()) {
                        listOfAffectedTransmitters.addAll(a.getTransmitters());
                    }
                }
            }
        }

        affectedTransmitterList.removeAll();

        // Populate list control with Affected Transmitters
        String[] transmitterNames = new String[listOfAffectedTransmitters
                .size()];

        int idx = 0;
        for (Transmitter t : listOfAffectedTransmitters) {
            transmitterNames[idx] = t.getMnemonic();
            affectedTransmitterList.setData(t.getMnemonic(), t);
            idx++;
        }

        Arrays.sort(transmitterNames);
        affectedTransmitterList.setItems(transmitterNames);
    }

    /**
     * Populate and return the selected data.
     */
    private void populateReturnObject() {
        AreaSelectionSaveData saveData = new AreaSelectionSaveData();
        java.util.List<TableRowData> rows = tableData.getTableRows();
        for (TableRowData row : rows) {
            Object rowData = row.getData();
            if (rowData instanceof Area) {
                saveData.addArea((Area) rowData);
            } else if (rowData instanceof Zone) {
                saveData.addZone((Zone) rowData);
            } else if (rowData instanceof Transmitter) {
                Transmitter t = (Transmitter) rowData;
                saveData.addTransmitter(t);
            } else {
                throw new IllegalArgumentException("Invalid data type: "
                        + rowData.getClass());
            }
        }

        HashSet<Transmitter> affectedTransmitters = new HashSet<>(
                (int) Math.ceil(this.affectedTransmitterList.getItems().length / 0.75));
        for (String t : this.affectedTransmitterList.getItems()) {
            // Skipping NULL check. We believe the map and list will always be
            // in sync.
            affectedTransmitters.add((Transmitter) this.affectedTransmitterList
                    .getData(t));
        }

        saveData.setAffectedTransmitters(affectedTransmitters);

        setReturnValue(saveData);
    }

    /**
     * Create an Area row.
     * 
     * @param a
     *            The Area
     * @return
     */
    private TableRowData createAreaRow(Area a) {
        TableRowData row = new TableRowData();
        TableCellData cell = new TableCellData(String.valueOf(a.getAreaCode()));
        row.addTableCellData(cell);
        cell = new TableCellData(a.getAreaName());
        row.addTableCellData(cell);
        cell = new TableCellData(AREA);
        row.addTableCellData(cell);

        row.setData(a);

        return row;
    }

    /**
     * Create a Zone row.
     * 
     * @param z
     *            The Zone
     * @return
     */
    private TableRowData createZoneRow(Zone z) {
        TableRowData row = new TableRowData();
        TableCellData cell = new TableCellData(String.valueOf(z.getZoneCode()));
        row.addTableCellData(cell);
        cell = new TableCellData(z.getZoneName());
        row.addTableCellData(cell);
        cell = new TableCellData(ZONE);
        row.addTableCellData(cell);

        row.setData(z);

        return row;
    }

    /**
     * Create a transmitter row.
     * 
     * @param t
     *            The Transmitter
     * @return
     */
    private TableRowData createTransmitterRow(Transmitter t) {
        TableRowData row = new TableRowData();
        TableCellData cell = new TableCellData(t.getMnemonic());
        row.addTableCellData(cell);
        cell = new TableCellData(t.getLocation());
        row.addTableCellData(cell);
        cell = new TableCellData(TRANSMITTER);
        row.addTableCellData(cell);

        row.setData(t);

        return row;
    }
}
