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

import java.util.Date;

import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.viz.bmh.ui.common.utility.FilterComp.DateFilterChoice;
import com.raytheon.uf.viz.bmh.ui.common.utility.FilterComp.TextFilterChoice;

/**
 * Data containing information on how the data should be filtered.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 14, 2014  3833      lvenable     Initial creation
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */

public class FilterData {

    /** Start date. */
    private Date startDate = TimeUtil.newGmtCalendar().getTime();

    /** End date. */
    private Date endDate = TimeUtil.newGmtCalendar().getTime();

    /** Text to filter on. */
    private String filterText = null;

    /** Choice on how the text should be filtered. */
    private TextFilterChoice textFilterChoice = TextFilterChoice.CONTAINS;

    /** Choice on how the date should be filtered. */
    private DateFilterChoice dateFilterChoice = DateFilterChoice.ALL;

    /** Flag indicating if the text filter should be case sensitive. */
    private boolean caseSensitive = false;

    /**
     * Constructor.
     * 
     * @param textFilterChoice
     *            Filter choice for text.
     * @param dateFilterChoice
     *            Filter choice for date.
     */
    public FilterData(TextFilterChoice textFilterChoice,
            DateFilterChoice dateFilterChoice) {

        if (textFilterChoice != null) {
            this.textFilterChoice = textFilterChoice;
        }

        if (dateFilterChoice != null) {
            this.dateFilterChoice = dateFilterChoice;
        }
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getFilterText() {
        return filterText;
    }

    public void setFilterText(String filterText) {
        this.filterText = filterText.trim();
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    public TextFilterChoice getTextFilterChoice() {
        return textFilterChoice;
    }

    public DateFilterChoice getDateFilterChoice() {
        return dateFilterChoice;
    }

    /**
     * Determine if the text should be filtered.
     * 
     * @return True if the text should be filtered on, false otherwise.
     */
    public boolean filterOnText() {
        if (filterText != null && filterText.length() > 0) {
            return true;
        }

        return false;
    }

    /**
     * Determine if the date should be filtered.
     * 
     * @return True if the date should be filtered on, false otherwise.
     */
    public boolean filterOnDate() {
        if (dateFilterChoice == DateFilterChoice.ALL) {
            return false;
        }

        return true;
    }
}
