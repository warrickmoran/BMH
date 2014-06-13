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

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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
 * May 28, 2014    3289    mpduff      Initial creation
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public abstract class TableComp extends Composite {

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
    private TableData tableData;

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

        for (TableRowData rowData : tableData.getTableRows()) {
            TableItem ti = new TableItem(table, SWT.NONE);
            List<TableCellData> cellDataList = rowData.getTableCellData();
            for (int i = 0; i < cellDataList.size(); i++) {
                TableCellData cellData = cellDataList.get(i);
                ti.setText(i, cellData.getDisplayString());
                ti.setBackground(i, cellData.getBackgroundColor());
                ti.setForeground(i, cellData.getForegroundColor());
            }
        }

        for (TableColumn tc : table.getColumns()) {
            tc.pack();
        }
    }

    /**
     * Create the table columns.
     */
    private void createColumns() {
        for (TableColumnData tcd : tableData.getColumnNames()) {
            TableColumn tc = new TableColumn(table, SWT.NONE);
            tc.setText(tcd.getText());
            tc.setWidth(tcd.getWidth());
            tc.setAlignment(tcd.getAlignment());
        }

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