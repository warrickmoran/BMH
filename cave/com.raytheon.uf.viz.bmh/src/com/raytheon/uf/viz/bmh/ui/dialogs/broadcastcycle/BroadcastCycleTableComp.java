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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
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
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class BroadcastCycleTableComp extends TableComp {

    private Menu popupMenu;

    private int tableIndex;

    public BroadcastCycleTableComp(Composite parent, int tableStyle,
            boolean displayLines, boolean displayHeader) {
        super(parent, tableStyle, displayLines, displayHeader);
        init();
    }

    @Override
    protected void handleTableMouseClick(MouseEvent event) {
        if (event.button == 1) {
            System.out.println("Populate Message Text Box");
            return;
        } else if (event.button == 3) {

            if (popupMenu != null) {
                popupMenu.dispose();
            }

            final boolean menuItemsEnabled = table.getSelectionIndices().length > 0;

            popupMenu = new Menu(table);

            MenuItem detailsItem = new MenuItem(popupMenu, SWT.PUSH);
            detailsItem.setText("Message Details...   ");
            detailsItem.setEnabled(menuItemsEnabled);
            detailsItem.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    System.out.println("Details");
                }
            });

            MenuItem periodicMsgItem = new MenuItem(popupMenu, SWT.PUSH);
            periodicMsgItem.setText("Periodic Messages...");
            periodicMsgItem.setEnabled(menuItemsEnabled);
            periodicMsgItem.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    System.out.println("Periodic Messages...");
                }
            });

            MenuItem copyMsgItem = new MenuItem(popupMenu, SWT.PUSH);
            copyMsgItem.setText("Copy Message...");
            copyMsgItem.setEnabled(menuItemsEnabled);
            copyMsgItem.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    System.out.println("Copy Message...");
                }
            });

            MenuItem expDelItem = new MenuItem(popupMenu, SWT.PUSH);
            expDelItem.setText("Expire/Delete");
            expDelItem.setEnabled(menuItemsEnabled);
            expDelItem.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    System.out.println("Expire/Delete");
                }
            });

            table.setMenu(popupMenu);

            popupMenu.setVisible(true);
        }
    }

    @Override
    protected void handleTableSelection(SelectionEvent event) {
        if ((table.getSelectionIndex() >= 0)
                && (table.getSelectionIndex() < table.getItemCount())) {
            tableIndex = table.getSelectionIndex();
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

    private void paintAction(Event event) {
        table.deselectAll();
        if ((tableIndex >= 0) && (tableIndex < table.getItemCount())) {
            event.gc.setForeground(getShell().getDisplay().getSystemColor(
                    SWT.COLOR_BLUE));
            event.gc.setLineWidth(3);
            TableItem item = table.getItem(tableIndex);
            Rectangle rect = item.getBounds();
            Rectangle tableRect = table.getBounds();
            event.gc.drawRectangle(rect.x - 1, rect.y - 1,
                    tableRect.width - 25, rect.height);
        }

        BroadcastCycleColorManager cm = new BroadcastCycleColorManager(
                Display.getCurrent());

        // Update colors for transmit time
        boolean first = true;
        for (TableItem item : table.getItems()) {
            if (first) {
                item.setBackground(0, cm.getActualTransmitTimeColor());
                first = false;
            } else {
                item.setBackground(0, cm.getPredictedTransmitTimeColor());
            }
        }
    }
}