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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * Abstract table composite for use in BMH.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 28, 2014   3289      mpduff      Initial creation
 * Jul 14, 2014   3377      lvenable    Added callback updates.
 * Jul 17, 2014   3406      mpduff      Added updateTable method.
 * Jul 22, 2014   3411      mpduff      Added PIXEL_BUFFER.
 * Aug 01, 2014   3479      lvenable    Added additional capability.
 * Aug 5, 2014    3490      lvenable    Added convenience methods.
 * Aug 8, 2014    3490      lvenable    Re-factored populate method.
 * Aug 8, 2014    3490      lvenable    Added a populate method to allow
 *                                      the regeneration of table columns.
 * Aug 15, 2014   3490      lvenable    Added replace table method.
 * Aug 26, 2014   3490      lvenable    Added method to get table item count.
 * Jan 02, 2014   3833      lvenable    Added methods to get the table items
 *                                      and column counts.
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public abstract class TableComp extends Composite {

    /**
     * Buffer for table width
     */
    private static final int PIXEL_BUFFER = 10;

    /**
     * The Table object
     */
    protected Table table;

    /** Display lines flag */
    private final boolean displayLines;

    /** Display column headers flag */
    private final boolean displayHeader;

    /** The table's style bits */
    private final int tableStyle;

    /** The TableData object holding all data for the table */
    protected TableData tableData;

    /** Column widths. */
    private int[] columnWidths;

    /** Table width hint. */
    private int tableWidth = -9999;

    /** Table height hint. */
    private int tableHeight = -9999;

    /** Callback. */
    protected ITableActionCB callbackAction;

    /**
     * Constructor.
     * 
     * @param parent
     *            Parent composite
     * @param tableStyle
     *            table style settings
     * @param displayLines
     *            true to display table lines
     * @param displayHeader
     *            true to display table column headers
     */
    public TableComp(Composite parent, int tableStyle, boolean displayLines,
            boolean displayHeader) {
        super(parent, SWT.NONE);
        this.tableStyle = tableStyle;
        this.displayLines = displayLines;
        this.displayHeader = displayHeader;
        init();
    }

    /**
     * Constructor.
     * 
     * @param parent
     *            Parent composite
     * @param tableStyle
     *            table style settings
     * @param displayLines
     *            true to display table lines
     * @param displayHeader
     *            true to display table column headers
     * @param width
     *            Table width hint.
     * @param height
     *            Table height hint.
     */
    public TableComp(Composite parent, int tableStyle, boolean displayLines,
            boolean displayHeader, int width, int height) {
        super(parent, SWT.NONE);
        this.tableStyle = tableStyle;
        this.displayLines = displayLines;
        this.displayHeader = displayHeader;
        this.tableWidth = width;
        this.tableHeight = height;
        init();
    }

    /**
     * Constructor.
     * 
     * @param parent
     *            Parent composite.
     * @param tableStyle
     *            Table style.
     * @param width
     *            Table width hint.
     * @param height
     *            Table height hint.
     */
    public TableComp(Composite parent, int tableStyle, int width, int height) {
        this(parent, tableStyle, true, true, width, height);
    }

    /**
     * Constructor to display table lines and headers.
     * 
     * @param parent
     *            Parent composite
     * @param tableStyle
     *            table style settings
     */
    public TableComp(Composite parent, int tableStyle) {
        this(parent, tableStyle, true, true);
    }

    /**
     * Initialize method.
     */
    private void init() {
        /*
         * Setup the layout for the composite
         */
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        GridLayout gl = new GridLayout(1, true);
        gl.verticalSpacing = 2;
        gl.marginHeight = 2;
        gl.marginWidth = 2;
        this.setLayout(gl);
        this.setLayoutData(gd);

        createTable();
    }

    /**
     * Create the table.
     */
    private void createTable() {
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);

        if (tableWidth > 0) {
            gd.widthHint = tableWidth;
        }

        if (tableHeight > 0) {
            gd.heightHint = tableHeight;
        }

        table = new Table(this, this.tableStyle);
        table.setLayoutData(gd);
        table.setHeaderVisible(displayHeader);
        table.setLinesVisible(displayLines);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent event) {
                handleTableMouseClick(event);
            }
        });

        table.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleTableSelection(e);
            }
        });
    }

    /**
     * Populate the table generating new columns. If columns are already
     * generated then only the data will be updated. If the columns need to be
     * regenerated then the method populateTable(TableData tableData, boolean
     * regenerateColumns) needs to be called.
     * 
     * @param tableData
     *            Table data.
     */
    public void populateTable(TableData tableData) {
        populateTable(tableData, false);
    }

    /**
     * Populate the table with the option of regenerating the columns. This will
     * allow the table to be reused if the table data and number of columns
     * change.
     * 
     * @param tableData
     *            Table data.
     * @param regenerateColumns
     *            Flag indicating if the columns should be regenerated.
     */
    public void populateTable(TableData tableData, boolean regenerateColumns) {

        /*
         * If there is data in the table then clear the table of the data so it
         * can be re-populated.
         */
        if (hasTableData()) {
            table.removeAll();
        }

        this.tableData = tableData;

        boolean tableColumnsCreated = false;

        /*
         * Check if the columns need to be regenerate or the number of columns
         * is zero. If the columns need to be regenerate and there are columns
         * present then dispose of the columns so they can be recreated.
         */
        if (regenerateColumns || table.getColumnCount() == 0) {
            if (table.getColumnCount() > 0) {
                for (TableColumn tc : table.getColumns()) {
                    tc.dispose();
                }
            }
            createColumns();
            tableColumnsCreated = true;
        }

        GC gc = new GC(table);
        gc.setFont(table.getFont());

        TableColumn[] columns = table.getColumns();

        for (TableRowData rowData : tableData.getTableRows()) {
            TableItem ti = new TableItem(table, SWT.NONE);
            ti.setData(rowData);
            List<TableCellData> cellDataList = rowData.getTableCellData();
            for (int i = 0; i < cellDataList.size(); i++) {
                TableCellData cellData = cellDataList.get(i);
                ti.setText(i, cellData.getDisplayString());
                ti.setBackground(i, cellData.getBackgroundColor());
                ti.setForeground(i, cellData.getForegroundColor());
                if (!((TableColumnData) columns[i].getData()).isPack()) {
                    columnWidths[i] = Math.max(gc.stringExtent(ti.getText(i)).x
                            + PIXEL_BUFFER, columnWidths[i]);
                }
            }
        }

        if (tableColumnsCreated) {
            int index = 0;
            for (TableColumn tc : table.getColumns()) {
                TableColumnData tcd = (TableColumnData) tc.getData();
                if (tcd.isPack()) {
                    tc.pack();
                    tc.setWidth(tc.getWidth() + PIXEL_BUFFER);
                } else {
                    tc.setWidth(columnWidths[index] + PIXEL_BUFFER);
                }
                index++;
            }
        }

        gc.dispose();
    }

    /**
     * Replace/Overwrite the data in the table with the data passed in. For
     * large amounts of data this will keep the position where the table is
     * scrolled and won't have to guess where the selected item is located since
     * the showSelection don't work properly.
     * 
     * @param tableData
     *            Table data used to re-populate the table items.
     */
    public void replaceTableItems(TableData tableData) {
        int tableDataSize = tableData.getTableRowCount();
        int tableSize = table.getItemCount();
        int selectedIndex = table.getSelectionIndex();

        if (tableSize > tableDataSize) {
            for (int i = 0; i < (tableSize - tableDataSize); i++) {
                table.remove(0);
            }
        } else if (tableDataSize > tableSize) {
            for (int i = 0; i < (tableDataSize - tableSize); i++) {
                new TableItem(table, SWT.NONE);
            }
        }

        TableColumn[] columns = table.getColumns();
        int index = 0;
        GC gc = new GC(table);
        gc.setFont(table.getFont());

        for (TableRowData rowData : tableData.getTableRows()) {
            List<TableCellData> cellDataList = rowData.getTableCellData();
            TableItem ti = table.getItem(index);
            ti.setData(rowData);
            for (int i = 0; i < cellDataList.size(); i++) {
                TableCellData cellData = cellDataList.get(i);
                ti.setText(i, cellData.getDisplayString());
                ti.setBackground(i, cellData.getBackgroundColor());
                ti.setForeground(i, cellData.getForegroundColor());
                if (!((TableColumnData) columns[i].getData()).isPack()) {
                    columnWidths[i] = Math.max(gc.stringExtent(ti.getText(i)).x
                            + PIXEL_BUFFER, columnWidths[i]);
                }
            }
            ++index;
        }
        gc.dispose();

        table.deselectAll();

        if (selectedIndex < 0) {
            selectedIndex = 0;
        } else if (selectedIndex > table.getItemCount() - 1) {
            selectedIndex = table.getItemCount() - 1;
        }

        table.select(selectedIndex);
    }

    /**
     * Verify if there is table data for this table.
     * 
     * @return True if there is table data, false otherwise.
     */
    public boolean hasTableData() {
        if (tableData == null && table.getItemCount() == 0) {
            return false;
        }

        return true;
    }

    /**
     * Get the number of items (rows) in the table.
     * 
     * @return The number of items (rows) in the table.
     */
    public int getItemCount() {
        return table.getItemCount();
    }

    /**
     * Create the table columns.
     */
    protected void createColumns() {
        columnWidths = new int[tableData.getColumnNames().size()];
        int i = 0;

        GC gc = new GC(table);
        gc.setFont(table.getFont());

        for (TableColumnData tcd : tableData.getColumnNames()) {
            TableColumn tc = new TableColumn(table, SWT.NONE);
            tc.setText(tcd.getText());
            tc.setWidth(tcd.getMinimumWidth());
            tc.setAlignment(tcd.getAlignment());
            tc.setData(tcd);

            if (tcd.isPack()) {
                columnWidths[i] = -1;
            } else {

                columnWidths[i] = Math.max(gc.stringExtent(tcd.getText()).x
                        + PIXEL_BUFFER, tcd.getMinimumWidth());
            }
            i++;
        }

        gc.dispose();
    }

    /**
     * Get the data for the selected row(s).
     * 
     * @return Row of data.
     */
    public List<TableRowData> getSelection() {
        List<TableRowData> rowList = new ArrayList<TableRowData>();
        TableItem[] tableItems = table.getSelection();
        for (TableItem ti : tableItems) {
            rowList.add((TableRowData) ti.getData());
        }

        return rowList;
    }

    /**
     * Get the selected indices.
     * 
     * @return An array of selected indices.
     */
    public int[] getSelectedIndices() {
        return table.getSelectionIndices();
    }

    /**
     * Get the selected index (first selection).
     * 
     * @return The selected index, -1 if nothing is selected.
     */
    public int getSelectedIndex() {
        return table.getSelectionIndex();
    }

    /**
     * Deselect all the items in the table.
     */
    public void deselectAll() {
        table.deselectAll();
    }

    /**
     * Get the tableData associated with this table.
     * 
     * @return The table data.
     */
    public TableData getTableData() {
        return tableData;
    }

    /**
     * Check if there are selected item(s) in the table.
     * 
     * @return True if there are selected item(s), false otherwise.
     */
    public boolean hasSelectedItems() {
        if (table.getItemCount() > 0) {
            return (table.getSelectionCount() > 0);
        }

        return false;
    }

    /**
     * Returns the number of items (rows) that are currently in the table.
     * 
     * @return The number of items (rows).
     */
    public int getTableItemCount() {
        return table.getItemCount();
    }

    /**
     * Select the row at the provided index
     * 
     * @param row
     *            index of the row to select
     */
    public void select(int row) {
        table.select(row);
    }

    /**
     * Select the rows that are specified in the index array.
     * 
     * @param indexes
     *            Index array.
     */
    public void selectRows(int[] indexes) {
        table.select(indexes);
    }

    /**
     * Show the current selection in the table.
     */
    public void showSelection() {
        // TODO : keep for now. showSelection doesn't really work for List or
        // Table controls. need to find a better solution

        int[] selTableItems = table.getSelectionIndices();
        if (selTableItems.length > 0) {
            table.select(selTableItems[0]);
            table.showSelection();
        }
    }

    /**
     * Remove all rows from the table.
     */
    public void removeAllTableItems() {
        table.removeAll();
    }

    /**
     * Get the table items.
     * 
     * @return The table items.
     */
    public TableItem[] getTableItems() {
        return table.getItems();
    }

    /**
     * Get the number of columns in the table.
     * 
     * @return The number of columns in the table.
     */
    public int getColumnCount() {
        return table.getColumnCount();
    }

    /**
     * Set the callback action on the table.
     * 
     * @param cb
     *            Callback
     */
    public void setCallbackAction(ITableActionCB cb) {
        this.callbackAction = cb;
    }

    /**
     * Handle the mouse click on the table.
     * 
     * @param event
     */
    protected abstract void handleTableMouseClick(MouseEvent event);

    /**
     * Handle a selection on the table.
     * 
     * @param e
     *            Selection event.
     */
    protected abstract void handleTableSelection(SelectionEvent e);

}