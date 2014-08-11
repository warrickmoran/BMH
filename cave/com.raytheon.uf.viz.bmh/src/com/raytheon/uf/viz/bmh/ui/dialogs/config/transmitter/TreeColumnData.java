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
package com.raytheon.uf.viz.bmh.ui.dialogs.config.transmitter;

/**
 * Column data object for the Transmitter config tree
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 1, 2014     3173    mpduff      Initial creation
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class TreeColumnData {
    /** The column header text */
    private String columnText;

    /** The column width */
    private int columnWidth;

    /** The column alignment */
    private int alignment;

    /**
     * @return the columnText
     */
    public String getColumnText() {
        return columnText;
    }

    /**
     * @param columnText
     *            the columnText to set
     */
    public void setColumnText(String columnText) {
        this.columnText = columnText;
    }

    /**
     * @return the columnWidth
     */
    public int getColumnWidth() {
        return columnWidth;
    }

    /**
     * @param columnWidth
     *            the columnWidth to set
     */
    public void setColumnWidth(int columnWidth) {
        this.columnWidth = columnWidth;
    }

    /**
     * @return the alignment
     */
    public int getAlignment() {
        return alignment;
    }

    /**
     * @param alignment
     *            the alignment to set
     */
    public void setAlignment(int alignment) {
        this.alignment = alignment;
    }
}
