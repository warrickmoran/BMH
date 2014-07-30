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
package com.raytheon.uf.viz.bmh.ui.common.table;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.raytheon.uf.viz.bmh.ui.common.table.TableData.SortDirection;

/**
 * Table Row Object,
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 05/27/2014      3289    mpduff      Initial Version.
 * 07/29/2014      3407    mpduff      Added data field to store backing object
 * </pre>
 */
public class TableRowData implements Comparable<TableRowData> {
    /** Unique id */
    private final UUID uuid = UUID.randomUUID();

    /** Data object backing this row */
    private Object data;

    /** Sort callback object */
    private ISortColumn sortCB;

    /**
     * List of table cells making up this row
     */
    private final List<TableCellData> rowCells;

    /**
     * Constructor
     */
    public TableRowData() {
        rowCells = new ArrayList<TableCellData>();
    }

    /**
     * Constructor with list of cell data objects
     * 
     * @param rowCells
     *            list of cell data objects
     */
    public TableRowData(List<TableCellData> rowCells) {
        this(rowCells, null);
    }

    /**
     * Constructor with list of cell data objects and sort callback
     * 
     * @param rowCells
     *            list of cell data objects
     * @param sortCB
     *            sort callback
     */
    public TableRowData(List<TableCellData> rowCells, ISortColumn sortCB) {
        this.rowCells = rowCells;
        this.sortCB = sortCB;
    }

    /**
     * Get the unique row id
     * 
     * @return the unique id
     */
    public UUID getUniqueRowID() {
        return uuid;
    }

    /**
     * Set the sort callback object
     * 
     * @param callback
     *            The sort callback object
     */
    public void setSortCallback(ISortColumn callback) {
        sortCB = callback;
    }

    /**
     * Add a table cell object
     * 
     * @param tcd
     *            The table cell object to add
     */
    public void addTableCellData(TableCellData tcd) {
        rowCells.add(tcd);
    }

    /**
     * Get the table cell objects for this row
     * 
     * @return the table cell objects
     */
    public List<TableCellData> getTableCellData() {
        return rowCells;
    }

    /**
     * Get a list of the cell's display strings
     * 
     * @return List of display strings
     */
    public List<String> getRowOfDisplayStrings() {
        List<String> strArray = new ArrayList<String>();

        for (TableCellData tcd : rowCells) {
            strArray.add(tcd.getDisplayString());
        }

        return strArray;
    }

    /**
     * Set the cell's data from a string
     * 
     * @param index
     *            index of the cell
     * @param data
     *            String data to set
     */
    public void setCellDataFromString(int index, String data) {
        if (index >= 0 && index < rowCells.size()) {
            rowCells.get(index).setDataFromString(data);
        }
    }

    /**
     * @return the data
     */
    public Object getData() {
        return data;
    }

    /**
     * @param data
     *            the data to set
     */
    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public int compareTo(TableRowData otherObj) {
        int selectedIndex = sortCB.getSortColumn();
        SortDirection direction = sortCB.getSortDirection();
        int x = 0;

        Object thisData = rowCells.get(selectedIndex).sortByObject(direction);
        Object otherData = otherObj.rowCells.get(selectedIndex).sortByObject(
                direction);

        if (thisData instanceof String) {
            x = ((String) thisData).compareTo((String) otherData);
        } else if (thisData instanceof Number) {
            double thisNumber = (Float) thisData;
            double otherNumber = (Float) otherData;

            if (thisNumber < otherNumber) {
                x = -1;
            } else if (thisNumber > otherNumber) {
                x = 1;
            } else {
                x = 0;
            }
        } else if (thisData instanceof Boolean) {
            boolean b = (boolean) thisData;
            if (b == false) {
                return -1;
            } else {
                return 1;
            }
        }

        if (direction == SortDirection.DESCENDING) {
            if (x < 0) {
                return 1;
            } else if (x > 0) {
                return -1;
            }
        }

        return x;
    }

    public void markAsSaved() {
        for (TableCellData tcd : rowCells) {
            tcd.markDataAsSaved();
        }
    }
}