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

import java.util.Arrays;

/**
 * Class used to reorder multiple items in a table.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 31, 2014  #3479     lvenable     Initial creation
 * Aug 8, 2014    #3490     lvenable    Updated populate table method call.
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */
public class TableMoveAction {

    /** Table composite. */
    private TableComp tableComp = null;

    /**
     * Constructor.
     * 
     * @param tableComp
     *            Table with items that need to be reordered.
     */
    public TableMoveAction(TableComp tableComp) {
        this.tableComp = tableComp;
    }

    /**
     * Move the selected item "up" in the list.
     */
    public void moveUp() {
        move(true);
    }

    /**
     * Move the selected items "down" in the list.
     */
    public void moveDown() {
        move(false);
    }

    /**
     * Move the items in the table up or down based on the flag passed in.
     * 
     * @param up
     *            True to move the items up, false to move them down.
     */
    private void move(boolean up) {
        if (!tableComp.hasSelectedItems()) {
            return;
        }

        TableData tableData = tableComp.getTableData();

        int[] selIdxArray = tableComp.getSelectedIndices();
        boolean[] selBoolArray = new boolean[tableData.getTableRowCount()];
        Arrays.fill(selBoolArray, false);
        for (int i : selIdxArray) {
            selBoolArray[i] = true;
        }

        if (up) {
            for (int i = 1; i < selBoolArray.length; i++) {
                if (selBoolArray[i] == true && selBoolArray[i - 1] == false) {
                    selBoolArray[i] = false;
                    selBoolArray[i - 1] = true;
                    TableRowData trd = tableData.deleteRow(i - 1);
                    tableData.addDataRow(trd, i);
                }
            }
        } else {
            for (int i = selBoolArray.length - 1; i > 0; i--) {
                if (selBoolArray[i] == false && selBoolArray[i - 1] == true) {
                    selBoolArray[i] = true;
                    selBoolArray[i - 1] = false;
                    TableRowData trd = tableData.deleteRow(i - 1);
                    tableData.addDataRow(trd, i);
                }
            }
        }

        int count = 0;
        for (int i = 0; i < selBoolArray.length; i++) {
            if (selBoolArray[i] == true) {
                selIdxArray[count] = i;
                ++count;
            }
        }

        tableComp.populateTable(tableData);
        tableComp.deselectAll();
        tableComp.selectRows(selIdxArray);
        tableComp.showSelection();
    }
}
