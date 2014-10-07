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
package com.raytheon.uf.edex.bmh.staticmsg;

/**
 * POJO representing a time message fragment. Created to wrap time message
 * fragments so that the dynamic portions of a message could be identified
 * without using potentially risky String matching.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 1, 2014  3642       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class TimeTextFragment {

    public static final String TIME_PLACEHOLDER = "HH:mm z";

    private String text;

    private boolean placeholder;

    protected TimeTextFragment() {
        this.placeholder = true;
        this.text = TIME_PLACEHOLDER;
    }

    /**
     * 
     */
    public TimeTextFragment(final String text) {
        this.text = text;
    }

    public static TimeTextFragment constructPlaceHolderFragment() {
        return new TimeTextFragment();
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isTimePlaceholder() {
        return this.placeholder;
    }
}