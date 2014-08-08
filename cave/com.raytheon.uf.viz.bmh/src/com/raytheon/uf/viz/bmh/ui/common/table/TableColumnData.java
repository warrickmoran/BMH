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

import org.eclipse.swt.SWT;

/**
 * Table column object.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 03, 2014    3289    mpduff      Initial creation
 * Jul 08, 2014    3355    mpduff      Added constructor with alignment option.
 * Aug 8, 2014    #3490    lvenable    Init pack to false;
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class TableColumnData {
    /** Column header text */
    private String text;

    /** Column minimum width */
    private int minimumWidth = 100;

    private boolean pack = false;

    /**
     * Column alignment SWT.LEFT, SWT.CENTER, SWT.RIGHT
     */
    private int alignment = SWT.CENTER;

    /**
     * Constructor for packed column.
     * 
     * @param text
     *            header text
     */
    public TableColumnData(String text) {
        this.text = text;
        this.pack = true;
    }

    /**
     * Constructor for a no pack column of width.
     * 
     * @param text
     *            header text
     * @param width
     *            column width
     */
    public TableColumnData(String text, int minimumwidth) {
        this(text, minimumwidth, SWT.CENTER);
    }

    /**
     * Constructor.
     * 
     * @param text
     *            header text
     * @param width
     *            column width
     * @param alignment
     *            column header alignment
     */
    public TableColumnData(String text, int minimumwidth, int alignment) {
        this.text = text;
        this.minimumWidth = minimumwidth;
        this.alignment = alignment;
    }

    /**
     * @return the text
     */
    public String getText() {
        return text;
    }

    /**
     * @param text
     *            the text to set
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * @return the width
     */
    public int getMinimumWidth() {
        return minimumWidth;
    }

    /**
     * @param width
     *            the width to set
     */
    public void setMinimumWidth(int width) {
        this.minimumWidth = width;
    }

    /**
     * @return the pack
     */
    public boolean isPack() {
        return pack;
    }

    /**
     * @param pack
     *            the pack to set
     */
    public void setPack(boolean pack) {
        this.pack = pack;
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

    @Override
    public String toString() {
        return text;
    }
}