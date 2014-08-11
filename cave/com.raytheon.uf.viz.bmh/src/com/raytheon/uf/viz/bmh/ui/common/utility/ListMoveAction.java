package com.raytheon.uf.viz.bmh.ui.common.utility;

import org.eclipse.swt.widgets.List;

/**
 * Class that will move selected list items up/down. If the items are separated
 * (non selected items in between), when the items reach the top or bottom of
 * the list they will move to the top or bottom
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 17, 2014     3173   lvenable    Initial creation
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */
public class ListMoveAction {

    /** List control. */
    private List itemList = null;

    /**
     * Constructor.
     * 
     * @param list
     *            List control.
     */
    public ListMoveAction(List list) {
        this.itemList = list;
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
     * Move the items in the list up or down based on the flag passed in.
     * 
     * @param up
     *            True to move the items up, false to move them down.
     */
    private void move(boolean up) {
        if (itemList.getSelectionCount() <= 0) {
            return;
        }

        int[] selIdxArray = itemList.getSelectionIndices();
        boolean[] selBoolArray = new boolean[itemList.getItemCount()];
        for (int i : selIdxArray) {
            selBoolArray[i] = true;
        }

        if (up) {
            for (int i = 1; i < selBoolArray.length; i++) {

                if (selBoolArray[i] == true && selBoolArray[i - 1] == false) {
                    selBoolArray[i] = false;
                    selBoolArray[i - 1] = true;
                    String str = itemList.getItem(i - 1);
                    itemList.remove(i - 1);
                    itemList.add(str, i);
                }
            }
        } else {
            for (int i = selBoolArray.length - 1; i > 0; i--) {
                if (selBoolArray[i] == false && selBoolArray[i - 1] == true) {
                    selBoolArray[i] = true;
                    selBoolArray[i - 1] = false;
                    String str = itemList.getItem(i - 1);
                    itemList.remove(i - 1);
                    itemList.add(str, i);
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

        itemList.deselectAll();
        itemList.select(selIdxArray);
        itemList.showSelection();
    }
}
