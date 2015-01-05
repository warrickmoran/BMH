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
package com.raytheon.uf.viz.bmh.ui.common.utility;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 
 * Data class used with the CheckListScrollDlg. This class contains the
 * information that will be used to construct the dialog.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 7, 2014  #3360      lvenable     Initial creation
 * Dec 18, 2014 #3865      bsteffen     Implement allChecked
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */
public class CheckListData {

    private Map<String, Boolean> dataMap = new LinkedHashMap<String, Boolean>();

    /**
     * Constructor.
     */
    public CheckListData() {

    }

    /**
     * Add a data item to the data map.
     * 
     * @param name
     *            Unique name to be displayed.
     * @param checked
     *            Flag indicating if the item should be checked.
     */
    public void addDataItem(String name, boolean checked) {
        if (name != null) {
            dataMap.put(name, checked);
        }
    }

    /**
     * Get a list of items from the list that have been checked.
     * 
     * @return List of items that have been checked.
     */
    public List<String> getCheckedItems() {
        List<String> checkedItems = new ArrayList<String>();

        for (String str : dataMap.keySet()) {
            if (dataMap.get(str) == true) {
                checkedItems.add(str);
            }
        }

        return checkedItems;
    }

    /**
     * @return true if all items are checked, false if any items are unchecked.
     */
    public boolean allChecked() {
        for (Entry<String, Boolean> entry : dataMap.entrySet()) {
            if (entry.getValue() == false) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get the map of data items.
     * 
     * @return The map of data items.
     */
    public Map<String, Boolean> getDataMap() {
        return dataMap;
    }
}
