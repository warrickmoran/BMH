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

import com.raytheon.uf.viz.bmh.ui.common.table.TableData.SortDirection;

/**
 * Table Sort interface
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 05/27/2014     3289     mpduff      Initial Version.
 * </pre>
 */
public interface ISortColumn {
    /**
     * Get the column to be sorted.
     * 
     * @return The column index.
     */
    public int getSortColumn();

    /**
     * Get the sort direction.
     * 
     * @return ASCENDING, DESCENDING, or NONE
     */
    public SortDirection getSortDirection();

    /**
     * Return if the table can be sorted by a column
     * 
     * @return true if can sort by columns
     */
    public boolean getCanSortColumns();
}