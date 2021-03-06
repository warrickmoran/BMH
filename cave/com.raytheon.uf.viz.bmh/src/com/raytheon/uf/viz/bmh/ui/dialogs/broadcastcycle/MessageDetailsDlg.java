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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg;
import com.raytheon.uf.common.bmh.datamodel.msg.InputMessage;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.common.bmh.datamodel.msg.Program;
import com.raytheon.uf.common.bmh.datamodel.msg.Suite;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Area;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Zone;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.viz.bmh.ui.common.table.GenericTable;
import com.raytheon.uf.viz.bmh.ui.common.table.TableCellData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableColumnData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableRowData;
import com.raytheon.uf.viz.bmh.ui.dialogs.listening.ZonesAreasDataManager;
import com.raytheon.viz.ui.dialogs.CaveSWTDialog;

/**
 * Message information/details dialog.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 04, 2014    3432     mpduff     Initial creation
 * Aug 14, 2014    3432     mpduff     Remaining capabilities
 * Sep 25, 2014    3620     bsteffen   Add seconds to periodicity.
 * Oct 10, 2014    3646     rferrel    Convert to GenericTable.
 * Nov 01, 2014    3782     mpduff     Added Message Name to dialog.
 * Nov 11, 2014    3825     lvenable   Fixed text control to wrap text and
 *                                     populate it after the control has been
 *                                     sized.
 * Nov 15, 2014    3828     mpduff     Use InputMessage for creation date
 * Nov 15, 2014    3818     mpduff     Set return value.
 * Dec 11, 2014    3895     lvenable   Changed SimpleDateFormat to use GMT.
 * Dec 11, 2014    3905     lvenable   Updated to show area codes for the selected message.
 * Jan 20, 2015    4010     bkowal     Include the areas associated with the selected 
 *                                     transmitters (when applicable).
 * Mar 03, 2015    4210     bkowal     Use the periodicity associated with the Message instead
 *                                     of the Message Type.
 * Mar 05, 2015    4222     bkowal     Handle messages that never expire.
 * Mar 10, 2015    4256     rferrel    Make message text read only.
 * Jun 12, 2015    4482     rjpeter    Added DO_NOT_BLOCK.
 * Jan 27, 2016    5160     rjpeter    Added MRD and fixed Time Zone.
 * Jan 28, 2016    5300     rjpeter    Fixed transmitters to be based on broadcast messages.
 * Mar 10, 2016    5465     tgurney    Add missing dialog trim style
 * Aug 04, 2016    5766     bkowal     Alternate between displaying cycle-based periodicity
 *                                     and time-based periodicity.
 * </pre>
 * 
 * @author mpduff
 */

public class MessageDetailsDlg extends CaveSWTDialog {
    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(MessageDetailsDlg.class);

    // TODO - look at making this an enumeration
    private final String[] COMBO_VALUES = { "Broadcast Areas", "Programs",
            "Suites", "Transmitters" };

    private final SimpleDateFormat dateFormat = new SimpleDateFormat(
            "yyyy MM dd HH:mm");

    /**
     * The Stack Layout.
     */
    private final StackLayout stackLayout = new StackLayout();

    /**
     * The Stack Composite.
     */
    private Composite stackComp;

    private GenericTable broadcastAreaTableComp;

    private GenericTable transmitterTableComp;

    private GenericTable suiteTableComp;

    private GenericTable programTableComp;

    /** MessageType for the details */
    private final MessageType messageType;

    private final BroadcastCycleDataManager dataManager;

    private final BroadcastMsg broadcastMsg;

    private Text messageText;

    public MessageDetailsDlg(Shell parent, MessageType mType,
            BroadcastMsg broadcastMsg) {
        super(parent, SWT.DIALOG_TRIM | SWT.MIN, CAVE.INDEPENDENT_SHELL
                | CAVE.PERSPECTIVE_INDEPENDENT | CAVE.DO_NOT_BLOCK);
        this.messageType = mType;
        this.broadcastMsg = broadcastMsg;
        setText("Message Details/Information");
        dataManager = new BroadcastCycleDataManager();
        setReturnValue(broadcastMsg.getId());

        dateFormat.setTimeZone(TimeUtil.GMT_TIME_ZONE);
    }

    @Override
    protected Layout constructShellLayout() {
        GridLayout mainLayout = new GridLayout(1, false);
        return mainLayout;
    }

    @Override
    protected Object constructShellLayoutData() {
        return new GridData(SWT.FILL, SWT.FILL, true, true);
    }

    @Override
    protected void opened() {
        if (broadcastMsg != null) {
            String msgText = broadcastMsg.getInputMessage().getContent();
            messageText.setText(msgText);
        }
    }

    @Override
    protected void initializeComponents(Shell shell) {
        GridLayout gl = new GridLayout(2, false);
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        Composite upperComp = new Composite(shell, SWT.NONE);
        upperComp.setLayout(gl);
        upperComp.setLayoutData(gd);

        gl = new GridLayout(2, false);
        gd = new GridData(SWT.DEFAULT, SWT.DEFAULT, false, false);
        Composite labelComp = new Composite(upperComp, SWT.NONE);
        labelComp.setLayout(gl);
        labelComp.setLayoutData(gd);

        buildLabels(labelComp);

        gl = new GridLayout(1, false);
        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        Composite stackComp = new Composite(upperComp, SWT.NONE);
        stackComp.setLayout(gl);
        stackComp.setLayoutData(gd);
        buildStackLayout(stackComp);

        buildMessageArea();

        gd = new GridData(SWT.CENTER, SWT.DEFAULT, false, false);
        gd.widthHint = 75;
        Button close = new Button(shell, SWT.PUSH);
        close.setText("Close");
        close.setLayoutData(gd);
        close.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                close();
            }
        });
    }

    private void buildLabels(Composite comp) {
        GridData gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
        Label typeLbl = new Label(comp, SWT.NONE);
        typeLbl.setText("Message Type: ");
        typeLbl.setLayoutData(gd);

        gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        Label typeValueLbl = new Label(comp, SWT.NONE);
        typeValueLbl.setText(messageType.getAfosid());
        typeValueLbl.setLayoutData(gd);

        gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
        Label titleLbl = new Label(comp, SWT.NONE);
        titleLbl.setText("Message Title: ");
        titleLbl.setLayoutData(gd);

        gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        Label titleValueLbl = new Label(comp, SWT.NONE);
        titleValueLbl.setText(messageType.getTitle());
        titleValueLbl.setLayoutData(gd);

        gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
        Label nameLbl = new Label(comp, SWT.NONE);
        nameLbl.setText("Message Name: ");
        nameLbl.setLayoutData(gd);

        gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        Label nameValueLbl = new Label(comp, SWT.NONE);
        nameValueLbl.setText(broadcastMsg.getInputMessage().getName());
        nameValueLbl.setLayoutData(gd);

        gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
        Label mrdLbl = new Label(comp, SWT.NONE);
        mrdLbl.setText("MRD: ");
        mrdLbl.setLayoutData(gd);

        gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        Label mrdValueLbl = new Label(comp, SWT.NONE);
        String mrd = this.broadcastMsg.getInputMessage().getMrd();
        if (mrd == null) {
            mrd = "";
        }

        mrdValueLbl.setText(mrd);
        mrdValueLbl.setLayoutData(gd);

        InputMessage im = broadcastMsg.getInputMessage();
        gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
        Label periodicityLbl = new Label(comp, SWT.NONE);
        final boolean cyclesPresent = im.getCycles() != null
                && (im.getPeriodicity() == null || MessageType.DEFAULT_NO_PERIODICITY
                        .equals(im.getPeriodicity()));
        if (cyclesPresent) {
            periodicityLbl.setText("Periodicity\n(Cycle): ");
        } else {
            periodicityLbl.setText("Periodicity\n(DD:HH:MM:SS): ");
        }
        periodicityLbl.setLayoutData(gd);

        gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        Label periodicityValueLbl = new Label(comp, SWT.NONE);
        String periodicity = (cyclesPresent) ? Integer.toString(im.getCycles())
                : getPeriodicity();

        periodicityValueLbl.setText(periodicity);
        periodicityValueLbl.setLayoutData(gd);

        gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
        Label timeZoneLbl = new Label(comp, SWT.NONE);
        timeZoneLbl.setText("Time Zone: ");
        timeZoneLbl.setLayoutData(gd);

        gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        Label timeZoneValueLbl = new Label(comp, SWT.NONE);
        timeZoneValueLbl.setText(this.broadcastMsg.getTransmitterGroup()
                .getTimeZone());
        timeZoneValueLbl.setLayoutData(gd);

        gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
        Label createTimeLbl = new Label(comp, SWT.NONE);
        createTimeLbl.setText("Creation Time: ");
        createTimeLbl.setLayoutData(gd);

        InputMessage msg = broadcastMsg.getInputMessage();
        gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        Label createTimeValueLbl = new Label(comp, SWT.NONE);
        if (broadcastMsg != null) {
            String creationDate = dateFormat.format(msg.getCreationTime()
                    .getTime());
            createTimeValueLbl.setText(creationDate);
        }
        createTimeValueLbl.setLayoutData(gd);

        gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
        Label effectiveTimeLbl = new Label(comp, SWT.NONE);
        effectiveTimeLbl.setText("Effective Time: ");
        effectiveTimeLbl.setLayoutData(gd);

        gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        Label effectiveTimeValueLbl = new Label(comp, SWT.NONE);
        if (broadcastMsg != null) {
            String effectiveDate = dateFormat.format(msg.getEffectiveTime()
                    .getTime());
            effectiveTimeValueLbl.setText(effectiveDate);
        }
        effectiveTimeValueLbl.setLayoutData(gd);

        gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
        Label expirationTimeLbl = new Label(comp, SWT.NONE);
        expirationTimeLbl.setText("Expiration Time: ");
        expirationTimeLbl.setLayoutData(gd);

        gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        Label expirationTimeValueLbl = new Label(comp, SWT.NONE);
        if (broadcastMsg != null) {
            if (msg.getExpirationTime() != null) {
                String expDate = dateFormat.format(msg.getExpirationTime()
                        .getTime());
                expirationTimeValueLbl.setText(expDate);
            } else {
                expirationTimeValueLbl.setText("NEVER");
            }
        }
        expirationTimeValueLbl.setLayoutData(gd);
    }

    private void buildStackLayout(Composite comp) {
        GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        Combo combo = new Combo(comp, SWT.VERTICAL | SWT.DROP_DOWN | SWT.BORDER
                | SWT.READ_ONLY);
        combo.setLayoutData(gd);
        combo.setItems(COMBO_VALUES);
        combo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String selection = ((Combo) e.getSource()).getText();
                handleTypeSelection(selection);
            }
        });
        combo.select(0);

        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gd.heightHint = 160;
        gd.widthHint = 325;
        GridLayout gl = new GridLayout(1, false);
        stackComp = new Composite(comp, SWT.NONE);
        stackComp.setLayout(stackLayout);
        stackComp.setLayoutData(gd);

        // Broadcast Area
        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gl = new GridLayout(1, false);
        broadcastAreaTableComp = new GenericTable(stackComp, SWT.BORDER
                | SWT.V_SCROLL);
        broadcastAreaTableComp.setLayout(gl);
        broadcastAreaTableComp.setLayoutData(gd);

        // Transmitters - name/program
        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gl = new GridLayout(1, false);
        transmitterTableComp = new GenericTable(stackComp, SWT.BORDER
                | SWT.V_SCROLL);
        transmitterTableComp.setLayout(gl);
        transmitterTableComp.setLayoutData(gd);

        // Suites containing this message
        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gl = new GridLayout(1, false);
        suiteTableComp = new GenericTable(stackComp, SWT.BORDER | SWT.V_SCROLL);
        suiteTableComp.setLayout(gl);
        suiteTableComp.setLayoutData(gd);

        // Programs containing this message
        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gl = new GridLayout(1, false);
        programTableComp = new GenericTable(stackComp, SWT.BORDER
                | SWT.V_SCROLL);
        programTableComp.setLayout(gl);
        programTableComp.setLayoutData(gd);

        try {
            // Here for initial load
            populateBroadcastAreaTable();
        } catch (Exception e1) {
            statusHandler.error("Error Populating Areas", e1);
        }
        stackLayout.topControl = broadcastAreaTableComp;
    }

    private void buildMessageArea() {
        GridData gd = new GridData(SWT.LEFT, SWT.DEFAULT, false, false);
        Label msgLbl = new Label(shell, SWT.NONE);
        msgLbl.setText("Message Text:");
        msgLbl.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gd.heightHint = 125;
        messageText = new Text(shell, SWT.BORDER | SWT.V_SCROLL | SWT.WRAP
                | SWT.READ_ONLY);
        messageText.setLayoutData(gd);
        messageText.setLayoutData(gd);
    }

    private void populateBroadcastAreaTable() throws Exception {

        /*
         * Get the input message and the area codes for that input message.
         */
        InputMessage inputMsg = broadcastMsg.getInputMessage();

        if (inputMsg == null) {
            return;
        }

        Set<String> areaZoneCodesInMessage = inputMsg.getAreaCodeSet();
        Set<Area> areas = new HashSet<>();

        try {
            /*
             * Find all the matching areas and add them to a list of areas.
             */
            ZonesAreasDataManager dataManager = new ZonesAreasDataManager();
            List<Area> allAreas = dataManager.getAreas();
            for (Area area : allAreas) {
                if (areaZoneCodesInMessage.contains(area.getAreaCode())) {
                    areas.add(area);
                }
            }

            /*
             * Find all the matching zones and then add their areas to a list of
             * areas.
             */
            List<Zone> zones = dataManager.getZones();
            for (Zone zone : zones) {
                if (areaZoneCodesInMessage.contains(zone.getZoneCode())) {
                    Set<Area> areaSet = zone.getAreas();
                    for (Area area : areaSet) {
                        areas.add(area);
                    }
                }
            }

            /*
             * Determine all areas associated with any specified transmitters
             * that were specified as destinations for this message.
             */
            if ((inputMsg.getSelectedTransmitters() != null)
                    && (inputMsg.getSelectedTransmitters().isEmpty() == false)) {
                for (Transmitter t : inputMsg.getSelectedTransmitters()) {
                    areas.addAll(dataManager.getAreasForTransmitter(t));
                }
            }

        } catch (Exception e) {
            statusHandler.error(
                    "Error retrieving Areas/Zones from database for input message: "
                            + inputMsg.getName(), e);
            return;
        }

        /*
         * Build the table data using the associated areas.
         */
        TableData broadcastAreaTableData = getBroadcastAreaTableData();

        for (Area a : areas) {
            TableRowData row = new TableRowData();
            TableCellData cell = new TableCellData(a.getAreaCode());
            row.addTableCellData(cell);
            cell = new TableCellData(a.getAreaName());
            row.addTableCellData(cell);
            broadcastAreaTableData.addDataRow(row);
        }

        broadcastAreaTableComp.populateTable(broadcastAreaTableData);
    }

    private TableData getBroadcastAreaTableData() {
        List<TableColumnData> columns = new ArrayList<TableColumnData>(2);
        TableColumnData tcd = new TableColumnData("Area Code", 75);
        TableColumnData tcd2 = new TableColumnData("Area Name");
        columns.add(tcd);
        columns.add(tcd2);

        TableData broadcastAreaTableData = new TableData(columns);

        return broadcastAreaTableData;
    }

    private void populateTransmitterTable() throws Exception {
        TableData transmittertableData = getTransmitterTableData();

        List<TransmitterGroup> transmitterGroupList = dataManager
                .getTransmitterGroupsForMessage(broadcastMsg);

        for (TransmitterGroup tg : transmitterGroupList) {
            for (Transmitter t : tg.getTransmitterList()) {
                TableRowData row = new TableRowData();
                TableCellData cell = new TableCellData(t.getLocation());
                row.addTableCellData(cell);
                cell = new TableCellData(t.getMnemonic());
                row.addTableCellData(cell);
                transmittertableData.addDataRow(row);
            }
        }
        transmitterTableComp.populateTable(transmittertableData);
    }

    private TableData getTransmitterTableData() {
        List<TableColumnData> columns = new ArrayList<TableColumnData>(2);
        TableColumnData tcd = new TableColumnData("Name", 75);
        TableColumnData tcd2 = new TableColumnData("ID");
        columns.add(tcd);
        columns.add(tcd2);

        TableData transmittertableData = new TableData(columns);

        return transmittertableData;
    }

    private void populateSuiteTable() throws Exception {
        TableData suiteTableData = getSuiteTableData();

        List<Suite> suiteList = dataManager
                .getSuitesForMessageType(messageType);

        for (Suite s : suiteList) {
            TableRowData row = new TableRowData();
            TableCellData cell = new TableCellData(s.getName());
            row.addTableCellData(cell);
            cell = new TableCellData(s.getType().name());
            row.addTableCellData(cell);
            suiteTableData.addDataRow(row);
        }

        suiteTableComp.populateTable(suiteTableData);
    }

    private TableData getSuiteTableData() {
        List<TableColumnData> columns = new ArrayList<TableColumnData>(2);
        TableColumnData tcd = new TableColumnData("Name", 150);
        TableColumnData tcd2 = new TableColumnData("Category");
        columns.add(tcd);
        columns.add(tcd2);

        TableData suiteTableData = new TableData(columns);
        return suiteTableData;
    }

    private void populateProgramTable() throws Exception {
        TableData programTableData = getProgramTableData();

        List<Program> programList = dataManager
                .getProgramsForMessageType(messageType);

        for (Program p : programList) {
            TableRowData row = new TableRowData();
            TableCellData cell = new TableCellData(p.getName());
            row.addTableCellData(cell);
            programTableData.addDataRow(row);
        }

        programTableComp.populateTable(programTableData);
    }

    private TableData getProgramTableData() {
        List<TableColumnData> columns = new ArrayList<TableColumnData>(2);
        TableColumnData tcd = new TableColumnData("Name", 75);
        columns.add(tcd);

        TableData programTableData = new TableData(columns);
        return programTableData;
    }

    private String getPeriodicity() {
        String periodicity = this.broadcastMsg.getInputMessage()
                .getPeriodicity();
        if ((periodicity == null) || periodicity.isEmpty()) {
            return "00:00:00:00";
        }
        final String COLON = ":";
        StringBuilder sb = new StringBuilder();
        sb.append(periodicity.substring(0, 2)).append(COLON);
        sb.append(periodicity.substring(2, 4)).append(COLON);
        sb.append(periodicity.substring(4, 6)).append(COLON);
        sb.append(periodicity.substring(6, 8));

        return sb.toString();
    }

    /**
     * @param selection
     */
    private void handleTypeSelection(String selection) {
        if (selection.equals(COMBO_VALUES[0])) {
            try {
                populateBroadcastAreaTable();
                stackLayout.topControl = broadcastAreaTableComp;
            } catch (Exception e1) {
                statusHandler.error("Error Populating Areas", e1);
            }
        } else if (selection.equals(COMBO_VALUES[1])) {
            try {
                populateProgramTable();
                stackLayout.topControl = programTableComp;
            } catch (Exception e1) {
                statusHandler.error("Error Populating Programs", e1);
            }
        } else if (selection.equals(COMBO_VALUES[2])) {
            try {
                populateSuiteTable();
                stackLayout.topControl = suiteTableComp;
            } catch (Exception e1) {
                statusHandler.error("Error Populating Suites", e1);
            }
        } else if (selection.equals(COMBO_VALUES[3])) {
            try {
                populateTransmitterTable();
                stackLayout.topControl = transmitterTableComp;
            } catch (Exception e1) {
                statusHandler.error("Error Populating Transmitters", e1);
            }
        }
        stackComp.layout();
    }
}
