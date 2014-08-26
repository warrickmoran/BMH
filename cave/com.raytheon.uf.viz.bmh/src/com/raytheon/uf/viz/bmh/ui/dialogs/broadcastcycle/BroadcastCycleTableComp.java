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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableItem;

import com.raytheon.uf.viz.bmh.ui.common.table.TableCellData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableComp;
import com.raytheon.uf.viz.bmh.ui.common.table.TableRowData;

/**
 * Broadcast Cycle Table Composite
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 3, 2014    3432     mpduff      Initial creation
 * Aug 14, 2014   3432     mpduff      Additional capabilities
 * Aug 24, 2014   3432     mpduff      Added override for select(int row)
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class BroadcastCycleTableComp extends TableComp {

    private int selectedTableIndex;

    public BroadcastCycleTableComp(Composite parent, int tableStyle,
            boolean displayLines, boolean displayHeader) {
        super(parent, tableStyle, displayLines, displayHeader);
        init();
    }

    @Override
    protected void handleTableMouseClick(MouseEvent event) {
        if (event.button == 1) {
            callbackAction.tableSelectionChange(table.getSelectionCount());
        }
    }

    @Override
    protected void handleTableSelection(SelectionEvent event) {
        if ((table.getSelectionIndex() >= 0)
                && (table.getSelectionIndex() < table.getItemCount())) {
            // Draw the blue outline
            selectedTableIndex = table.getSelectionIndex();
            table.deselectAll();
            table.redraw();
        }
    }

    private void init() {
        table.addListener(SWT.PaintItem, new Listener() {

            @Override
            public void handleEvent(Event event) {
                paintAction(event);
            }
        });

        // Color the cells
        TableItem[] tableItems = table.getItems();
        for (TableItem item : tableItems) {
            TableRowData rowData = (TableRowData) item.getData();
            // Check for message type colors
            TableCellData cellData = rowData.getTableCellData().get(1);
            if (cellData.getBackgroundColor() != null) {
                item.setBackground(1, cellData.getBackgroundColor());
            }
        }
    }

    /**
     * Draws the blue outline
     * 
     * @param event
     */
    private void paintAction(Event event) {
        table.deselectAll();
        if ((selectedTableIndex >= 0)
                && (selectedTableIndex < table.getItemCount())) {
            event.gc.setForeground(getShell().getDisplay().getSystemColor(
                    SWT.COLOR_BLUE));
            event.gc.setLineWidth(3);
            TableItem item = table.getItem(selectedTableIndex);
            Rectangle rect = item.getBounds();
            Rectangle tableRect = table.getBounds();
            event.gc.drawRectangle(rect.x - 5, rect.y - 1, tableRect.width - 4,
                    rect.height);
        }
    }

    /**
     * Have to override this since we deselect all and draw a blue outline
     * around the table row.
     * 
     * @return List<TableRowData> selected items
     */
    @Override
    public List<TableRowData> getSelection() {
        if (!(table.getItemCount() > 0) && !(selectedTableIndex >= 0)) {
            return Collections.emptyList();
        }
        TableItem item = table.getItem(selectedTableIndex);
        TableRowData row = (TableRowData) item.getData();
        List<TableRowData> rowList = new ArrayList<TableRowData>(1);
        rowList.add(row);

        return rowList;
    }

    /**
     * Have to override this since we deselect all and draw a blue outline
     * around the table row.
     * 
     * @param row
     *            The row to select
     */
    @Override
    public void select(int row) {
        selectedTableIndex = row;
    }
}