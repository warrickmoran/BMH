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
import java.util.Collections;
import java.util.List;

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
 * Aug 08, 2014  #3490     lvenable    Updated populate table method call.
 * Aug 18, 2014  #3490     lvenable    Updated to sort the actual list of data.
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
     * Move up.
     * 
     * @param dataArray
     *            List to resort.
     * @return List of selected indices.
     */
    public int[] moveUp(List<?> dataArray) {
        return move(true, dataArray);
    }

    /**
     * Move down.
     * 
     * @param dataArray
     *            List to resort.
     * @return List of selected indices.
     */
    public int[] moveDown(List<?> dataArray) {
        return move(false, dataArray);
    }

    private int[] move(boolean up, List<?> dataArray) {
        if (!tableComp.hasSelectedItems()) {
            return null;
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
                    Collections.swap(dataArray, i - 1, i);
                }
            }
        } else {
            for (int i = selBoolArray.length - 1; i > 0; i--) {
                if (selBoolArray[i] == false && selBoolArray[i - 1] == true) {
                    selBoolArray[i] = true;
                    selBoolArray[i - 1] = false;
                    Collections.swap(dataArray, i - 1, i);
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

        return selIdxArray;
    }
}
