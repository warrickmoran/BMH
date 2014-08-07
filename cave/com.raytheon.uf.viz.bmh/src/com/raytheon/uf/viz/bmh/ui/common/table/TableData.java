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
import java.util.Collections;
import java.util.List;

/**
 * Table Data Object,
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 05/27/2014      3289    mpduff      Initial Version.
 * 07/28/2014      3407    mpduff      Added remove row method.
 * Aug 01, 2014   #3479    lvenable    Added additional capability.
 * Aug 06, 2014   #3490    lvenable    Added a method to delete all of the data.
 * 
 * </pre>
 */
public class TableData implements ISortColumn {
    /**
     * The sort direction enumeration
     */
    public enum SortDirection {
        ASCENDING, DESCENDING, NONE
    }

    /**
     * List of table row objects
     */
    private final List<TableRowData> tableRows;

    /**
     * List of column names
     */
    private final List<TableColumnData> columnData;

    /**
     * Default values for the table rows
     */
    private TableRowData defaultValues;

    /**
     * Can sort by column flag
     */
    private boolean canSortByColumns = true;

    /**
     * Current column index to sort on.
     */
    private int currentSortColumnIndex = 0;

    /**
     * Current sort direction.
     */
    private SortDirection currentSortDirection = SortDirection.ASCENDING;

    /**
     * Constructor.
     * 
     * @param columnNameList
     *            List of column names for this table
     */
    public TableData(List<TableColumnData> columnNameList) {
        tableRows = new ArrayList<TableRowData>();
        this.columnData = columnNameList;
    }

    /**
     * Add a data row to the table
     * 
     * @param dataRow
     *            The row data to add
     */
    public void addDataRow(TableRowData dataRow) {
        dataRow.setSortCallback(this);
        tableRows.add(dataRow);
    }

    /**
     * Add a data row to the table at the specified index.
     * 
     * @param dataRow
     *            The row data to add.
     * @param index
     *            Index where the data will be added.
     */
    public void addDataRow(TableRowData dataRow, int index) {
        dataRow.setSortCallback(this);

        if (index < 0) {
            index = 0;
        } else if (index > tableRows.size()) {
            index = tableRows.size();
        }

        tableRows.add(index, dataRow);
    }

    /**
     * Get the default values
     * 
     * @return The default values
     */
    public TableRowData getDefaultValues() {
        return defaultValues;
    }

    /**
     * Set the default values
     * 
     * @param defaultValues
     *            The default values to set
     */
    public void setDefaultValues(TableRowData defaultValues) {
        this.defaultValues = defaultValues;
    }

    /**
     * Get all the table rows in the table
     * 
     * @return List of TableRowData objects
     */
    public List<TableRowData> getTableRows() {
        return tableRows;
    }

    /**
     * Get the number of rows in the table.
     * 
     * @return The number of rows.
     */
    public int getTableRowCount() {
        return tableRows.size();
    }

    /**
     * Get the table row for the provided index
     * 
     * @param index
     *            The index of the row
     * @return The row at the specified index or null if no row for that index
     *         does not exist
     */
    public TableRowData getTableRow(int index) {

        if (index > tableRows.size() - 1 || index < 0) {
            return null;
        }
        return tableRows.get(index);
    }

    /**
     * Delete the row at the provided index
     * 
     * @param index
     *            index of the row to delete
     * @return the delete object from the table
     */
    public TableRowData deleteRow(int index) {
        if (index >= 0 && index < tableRows.size() - 1) {
            return tableRows.remove(index);
        }

        return null;
    }

    /**
     * Delete a {@link TableRowData}.
     * 
     * @param TableRowData
     *            The row to delete
     * @return true if row deleted, false otherwise
     */
    public boolean deleteRow(TableRowData row) {
        return tableRows.remove(row);
    }

    /**
     * Delete all data rows.
     */
    public void deleteAllRows() {
        tableRows.clear();
    }

    /**
     * Sort the table row data
     */
    public void sortData() {
        Collections.sort(tableRows);
    }

    /**
     * Get the column names
     * 
     * @return List of column names
     */
    public List<TableColumnData> getColumnNames() {
        return columnData;
    }

    /**
     * Set the sort column and direction
     * 
     * @param columnIndex
     *            index of the sort column
     * @param direction
     *            sort direction
     */
    public void setSortColumnAndDirection(int columnIndex,
            SortDirection direction) {
        currentSortColumnIndex = columnIndex;
        currentSortDirection = direction;
        sortData();
    }

    /**
     * Set the sorted column
     * 
     * @param columnName
     *            Name of the sorted column
     */
    public void setSortedColumn(int columnIndex) {
        if (columnIndex < 0 || columnIndex >= columnData.size()) {
            columnIndex = 0;
        }

        currentSortColumnIndex = columnIndex;
        sortData();
    }

    /**
     * Mark all data as saved.
     */
    public void markDataAsSaved() {
        for (TableRowData trd : tableRows) {
            trd.markAsSaved();
        }
    }

    /**
     * Set the sort direction.
     * 
     * @param direction
     *            Sort direction (ascending or descending).
     */
    public void setSortDirection(SortDirection direction) {
        currentSortDirection = direction;
        sortData();
    }

    /**
     * Return if this table be sorted by columns
     * 
     * @return true if can be sorted by column
     */
    public boolean canSortByColumns() {
        return canSortByColumns;
    }

    /**
     * Set if this table can be sorted by column.
     * 
     * @param canSortByColumns
     */
    public void setSortByColumns(boolean canSortByColumns) {
        this.canSortByColumns = canSortByColumns;
    }

    @Override
    public int getSortColumn() {

        return currentSortColumnIndex;
    }

    @Override
    public SortDirection getSortDirection() {
        return currentSortDirection;
    }

    @Override
    public boolean getCanSortColumns() {
        return canSortByColumns;
    }
}