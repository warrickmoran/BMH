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
package com.raytheon.uf.edex.bmh.xformer.data;

import java.util.Deque;

/**
 * Defines Free Text - text that no transformations can be applied to.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 26, 2014 3302       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public interface IFreeText extends ITextRuling {
    /**
     * Claim some portion of the text between a start index and an end index as
     * eligible for transformation.
     * 
     * @param start
     *            the start index
     * @param end
     *            the end index
     */
    public void claim(int start, int end);

    /**
     * Specifies whether or not the text is eligible for transformation.
     * 
     * @return true, if the text is eligible for transformation; false,
     *         otherwise
     */
    public boolean eligibleForTransformation();

    /**
     * Segregates the text into Transformed Text and Plain Text after applying
     * the latest ruling.
     * 
     * @param rule
     *            the applicable transformation
     * @return the new text divisions
     */
    public Deque<ITextRuling> applyRuling(ITextTransformation rule);
}