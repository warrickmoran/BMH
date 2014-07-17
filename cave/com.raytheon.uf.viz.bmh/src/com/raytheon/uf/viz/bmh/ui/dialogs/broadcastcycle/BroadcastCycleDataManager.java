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
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.eclipse.swt.widgets.Display;

import com.raytheon.uf.common.bmh.datamodel.msg.Suite;
import com.raytheon.uf.common.bmh.datamodel.msg.Suite.SuiteType;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.viz.bmh.ui.common.table.TableCellData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableColumnData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableRowData;

/**
 * The {@link BroadcastCycleDlg} data manager class.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 3, 2014    3432     mpduff      Initial creation
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class BroadcastCycleDataManager {

    public static final String DATE_FORMAT = "MM/dd/yyyy HH:mm:ss";

    public List<Transmitter> transmitterList;

    public Map<String, List<Suite>> suites = new HashMap<String, List<Suite>>();

    /**
     * Constructor
     */
    public BroadcastCycleDataManager() {
        generateTestData();
    }

    /**
     * Get the list of transmitters
     * 
     * @return
     */
    public List<Transmitter> getTransmitterList() {
        return transmitterList;
    }

    /**
     * Set the list of transmitters
     * 
     * @param transmitterList
     */
    public void setTransmitterList(List<Transmitter> transmitterList) {
        this.transmitterList = transmitterList;
    }

    /**
     * Get the names of the transmitters.
     * 
     * @return
     */
    public String[] getTransmitterNames() {
        List<String> transmitters = new ArrayList<String>(
                transmitterList.size());

        for (Transmitter t : transmitterList) {
            transmitters.add(t.getMnemonic() + " - " + t.getName());
        }
        return transmitters.toArray(new String[transmitters.size()]);
    }

    /**
     * Get the suites for the specified transmitter
     * 
     * @param transmitter
     * @return
     */
    public List<Suite> getSuites(String transmitter) {
        return suites.get(transmitter);
    }

    /**
     * Get the Broadcast cycle dialog's main table data
     * 
     * @param transmitter
     *            The transmitter
     * @param program
     *            The program
     * @param suite
     *            The suite
     * @param suiteType
     *            The suiteType
     * @return The populated TableData
     */
    public TableData getTableData(String transmitter, String program,
            String suite, SuiteType suiteType) {
        List<TableColumnData> columns = createColumns();

        TableData data = new TableData(columns);
        List<TableRowData> rows = getTestRows(columns);

        for (TableRowData row : rows) {
            data.addDataRow(row);

        }
        return data;
    }

    /**
     * Create the column objects for the Broadcast Cycle dialog.
     * 
     * @return List of {@link TableColumnData}
     */
    private List<TableColumnData> createColumns() {
        List<TableColumnData> columns = new ArrayList<TableColumnData>(8);
        columns.add(new TableColumnData("Transmit Time"));
        columns.add(new TableColumnData("Message Type"));
        columns.add(new TableColumnData("MessageID"));
        columns.add(new TableColumnData("MRD"));
        columns.add(new TableColumnData("Expiration Time"));
        columns.add(new TableColumnData("Alert"));
        columns.add(new TableColumnData("SAME"));
        columns.add(new TableColumnData("Play Count"));

        return columns;
    }

    /**
     * Get the periodic message table data
     * 
     * @return The TableData
     */
    public TableData getPeriodicMessageTableData() {
        List<TableColumnData> columns = createPeriodicMessageColumns();

        TableData data = new TableData(columns);
        List<TableRowData> rows = getTestPeriodicRows(columns);

        for (TableRowData row : rows) {
            data.addDataRow(row);

        }
        return data;
    }

    private List<TableColumnData> createPeriodicMessageColumns() {
        List<TableColumnData> columns = new ArrayList<TableColumnData>(4);
        columns.add(new TableColumnData("Last Broadcast Time"));
        columns.add(new TableColumnData("Next Predicted Broadcast"));
        columns.add(new TableColumnData("Message Type"));
        columns.add(new TableColumnData("Message ID"));

        return columns;
    }

    /*
     * TODO Test Data - Remove
     */
    private void generateTestData() {
        Suite generalSuite = new Suite();
        generalSuite.setType(SuiteType.GENERAL);
        generalSuite.setName("Suite General");

        Suite exclusiveSuite = new Suite();
        exclusiveSuite.setType(SuiteType.EXCLUSIVE);
        exclusiveSuite.setName("Suite Exclusive");

        Suite highSuite = new Suite();
        highSuite.setType(SuiteType.HIGH);
        highSuite.setName("Suite High");

        List<Suite> suiteList = new ArrayList<Suite>();
        suiteList.add(generalSuite);
        suiteList.add(highSuite);
        suiteList.add(exclusiveSuite);

        transmitterList = new ArrayList<Transmitter>();
        Transmitter t = new Transmitter();
        t.setName("Albion");
        t.setMnemonic("ALB");
        transmitterList.add(t);
        suites.put(t.getMnemonic(), suiteList);

        t = new Transmitter();
        t.setName("Beatrice");
        t.setMnemonic("BIE");
        transmitterList.add(t);
        suites.put(t.getMnemonic(), suiteList);

        t = new Transmitter();
        t.setName("Fremont");
        t.setMnemonic("FRE");
        transmitterList.add(t);
        suites.put(t.getMnemonic(), suiteList);

        t = new Transmitter();
        t.setName("Lincoln");
        t.setMnemonic("LNK");
        transmitterList.add(t);
        suites.put(t.getMnemonic(), suiteList);

        t = new Transmitter();
        t.setName("Omaha");
        t.setMnemonic("OMA");
        transmitterList.add(t);
        suites.put(t.getMnemonic(), suiteList);
    }

    private List<TableRowData> getTestRows(List<TableColumnData> columns) {
        BroadcastCycleColorManager cm = new BroadcastCycleColorManager(
                Display.getCurrent());
        SimpleDateFormat sdf = new SimpleDateFormat(
                BroadcastCycleDataManager.DATE_FORMAT);
        List<TableRowData> rows = new ArrayList<TableRowData>();
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        Calendar expCal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        expCal.set(Calendar.SECOND, 0);
        expCal.set(Calendar.MINUTE, 0);
        expCal.add(Calendar.HOUR_OF_DAY, 6);
        for (int i = 0; i < 10; i++) {// Rows
            TableRowData rowData = new TableRowData();
            TableCellData cell1 = new TableCellData(sdf.format(cal.getTime()));
            rowData.addTableCellData(cell1);

            TableCellData cell2 = new TableCellData("Message Type");
            rowData.addTableCellData(cell2);
            if ((i > 0) && (i < 3)) {
                cell2.setBackgroundColor(cm.getInterruptColor());
            } else if ((i == 5) || (i == 7)) {
                cell2.setBackgroundColor(cm.getReplaceColor());
            } else if ((i == 4) || (i == 9)) {
                cell2.setBackgroundColor(cm.getPeriodicColor());
            }

            TableCellData cell3 = new TableCellData("Message ID Text");
            rowData.addTableCellData(cell3);

            TableCellData cell4 = new TableCellData("123");
            rowData.addTableCellData(cell4);

            TableCellData cell5 = new TableCellData(
                    sdf.format(expCal.getTime()));
            rowData.addTableCellData(cell5);

            TableCellData cell6 = new TableCellData("NONE");
            rowData.addTableCellData(cell6);

            TableCellData cell7 = new TableCellData("NONE");
            rowData.addTableCellData(cell7);

            TableCellData cell8 = new TableCellData(cal.get(Calendar.MINUTE),
                    null);
            rowData.addTableCellData(cell8);

            rows.add(rowData);
            cal.add(Calendar.SECOND, 360 * 15);
            expCal.add(Calendar.SECOND, 360 * 15);
        }

        return rows;
    }

    // columns.add(new TableColumnData("Last Broadcast Time"));
    // columns.add(new TableColumnData("Next Predicted Broadcast"));
    // columns.add(new TableColumnData("Message Type"));
    // columns.add(new TableColumnData("Message ID"));

    private List<TableRowData> getTestPeriodicRows(List<TableColumnData> columns) {
        List<TableRowData> rows = new ArrayList<TableRowData>();
        SimpleDateFormat sdf = new SimpleDateFormat(
                BroadcastCycleDataManager.DATE_FORMAT);

        Calendar cal = TimeUtil.newGmtCalendar();
        cal.add(Calendar.MINUTE, -3);

        for (int i = 0; i < 5; i++) {
            TableRowData rowData = new TableRowData();
            TableCellData cell = new TableCellData(sdf.format(cal.getTime()));
            rowData.addTableCellData(cell);

            cal.add(Calendar.SECOND, 68);
            TableCellData cell2 = new TableCellData(sdf.format(cal.getTime()));
            rowData.addTableCellData(cell2);

            TableCellData cell3 = new TableCellData("Message Type " + i);
            rowData.addTableCellData(cell3);

            TableCellData cell4 = new TableCellData("Message ID " + i);
            rowData.addTableCellData(cell4);

            rows.add(rowData);
        }

        return rows;
    }
}