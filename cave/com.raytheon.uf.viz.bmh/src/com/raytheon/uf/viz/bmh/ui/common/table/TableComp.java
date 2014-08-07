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
 * Generic table composite for use in BMH.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 28, 2014   3289      mpduff      Initial creation
 * Jul 14, 2014   #3377     lvenable    Added callback updates.
 * Jul 17, 2014   3406      mpduff      Added updateTable method.
 * Jul 22, 2014   3411      mpduff      Added PIXEL_BUFFER.
 * Aug 01, 2014   #3479     lvenable    Added additional capability.
 * Aug 5, 2014    #3490     lvenable    Added convenience methods.
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
     * Populate the data table.
     */
    public void populateTable(TableData tableData) {
        this.tableData = tableData;

        createColumns();

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
                    columnWidths[i] = Math.max(gc.stringExtent(ti.getText()).x
                            + PIXEL_BUFFER, columnWidths[i]);
                }
            }
        }

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

        gc.dispose();
    }

    /**
     * Update an already created table with new data. This is a data update
     * only, the table is not recreated, only repopulated.
     * 
     * @param tableData
     *            Updated TableData
     */
    public void updateTable(TableData tableData) {

        this.tableData = tableData;

        refreshTable();
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
     * Refresh the table.
     */
    public void refreshTable() {
        table.removeAll();
        if (table.getColumnCount() == 0) {
            this.createColumns();
        }

        for (TableRowData rowData : tableData.getTableRows()) {
            TableItem ti = new TableItem(table, SWT.NONE);
            ti.setData(rowData);
            List<TableCellData> cellDataList = rowData.getTableCellData();
            for (int i = 0; i < cellDataList.size(); i++) {
                TableCellData cellData = cellDataList.get(i);
                ti.setText(i, cellData.getDisplayString());
            }
        }
    }

    /**
     * Create the table columns.
     */
    protected void createColumns() {
        columnWidths = new int[tableData.getColumnNames().size()];
        int i = 0;
        for (TableColumnData tcd : tableData.getColumnNames()) {
            TableColumn tc = new TableColumn(table, SWT.NONE);
            tc.setText(tcd.getText());
            tc.setWidth(tcd.getMinimumWidth());
            tc.setAlignment(tcd.getAlignment());
            tc.setData(tcd);
            if (tcd.isPack()) {
                columnWidths[i] = -1;
            } else {
                columnWidths[i] = tcd.getMinimumWidth();
            }
            i++;
        }
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